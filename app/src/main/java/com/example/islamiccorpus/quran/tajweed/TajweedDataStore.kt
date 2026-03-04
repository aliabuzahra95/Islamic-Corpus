package com.example.islamiccorpus.quran.tajweed

import android.content.Context
import android.util.Log
import org.json.JSONArray
import org.json.JSONObject
import org.json.JSONTokener
import kotlin.math.max
import kotlin.math.min

data class TajweedEntry(
    val plainText: String,
    val spans: List<TajweedSpan>
)

data class TajweedSpan(
    val start: Int,
    val end: Int,
    val className: String
)

object TajweedDataStore {
    private const val TAG = "TAJWEED_JSON"
    private const val ASSET_FILE = "tajweed.hafs.uthmani-pause-sajdah.json"

    @Volatile
    private var cachedMap: Map<String, TajweedEntry>? = null

    fun load(
        context: Context,
        baseTextByKey: Map<String, String>
    ): Map<String, TajweedEntry> {
        cachedMap?.let { return it }
        synchronized(this) {
            cachedMap?.let { return it }
            val raw = context.assets.open(ASSET_FILE).bufferedReader(Charsets.UTF_8).use { it.readText() }
            val root = JSONTokener(raw).nextValue()
            val out = mutableMapOf<String, TajweedEntry>()
            when (root) {
                is JSONArray -> parseArray(root, baseTextByKey, out)
                is JSONObject -> parseObject(root, baseTextByKey, out)
            }
            val result = out.toMap()
            cachedMap = result
            Log.d(TAG, "parsedEntries=${result.size}")
            return result
        }
    }

    private fun parseArray(
        array: JSONArray,
        baseTextByKey: Map<String, String>,
        out: MutableMap<String, TajweedEntry>
    ) {
        for (i in 0 until array.length()) {
            when (val value = array.opt(i)) {
                is JSONObject -> parseAyahObject(value, baseTextByKey, out) ?: parseObject(value, baseTextByKey, out)
                is JSONArray -> parseArray(value, baseTextByKey, out)
            }
        }
    }

    private fun parseObject(
        obj: JSONObject,
        baseTextByKey: Map<String, String>,
        out: MutableMap<String, TajweedEntry>
    ) {
        parseAyahObject(obj, baseTextByKey, out)
        val keys = obj.keys()
        while (keys.hasNext()) {
            val key = keys.next()
            when (val value = obj.opt(key)) {
                is JSONObject -> parseObject(value, baseTextByKey, out)
                is JSONArray -> parseArray(value, baseTextByKey, out)
            }
        }
    }

    private fun parseAyahObject(
        obj: JSONObject,
        baseTextByKey: Map<String, String>,
        out: MutableMap<String, TajweedEntry>
    ): TajweedEntry? {
        val mapKey = extractKey(obj) ?: return null
        val spansFromRanges = parseRangeSpans(obj)
        val entryFromSegments = parseSegmentTokens(obj)
        val textCandidate = extractTextField(obj)
        val entryFromTagged = textCandidate?.let { parseTaggedText(it) }
        val entry = when {
            entryFromTagged != null -> entryFromTagged
            entryFromSegments != null -> entryFromSegments
            spansFromRanges.isNotEmpty() -> {
                val plain = baseTextByKey[mapKey] ?: return null
                TajweedEntry(
                    plainText = plain,
                    spans = mergeAdjacentSameClass(
                        spansFromRanges.mapNotNull { adjustToCluster(plain, it) }
                    )
                )
            }
            else -> null
        } ?: return null
        out[mapKey] = entry
        return entry
    }

    private fun parseSegmentTokens(obj: JSONObject): TajweedEntry? {
        val arrays = listOf("tokens", "segments").mapNotNull { obj.optJSONArray(it) }
        if (arrays.isEmpty()) return null

        val plain = StringBuilder()
        val spans = mutableListOf<TajweedSpan>()
        arrays.forEach { arr ->
            for (i in 0 until arr.length()) {
                val segment = arr.optJSONObject(i) ?: continue
                val text = segment.optString("text", "")
                if (text.isEmpty()) continue
                val start = plain.length
                plain.append(text)
                val end = plain.length
                val className = segment.optString(
                    "rule",
                    segment.optString("class", segment.optString("name", ""))
                )
                if (className.isNotBlank()) {
                    spans += TajweedSpan(start = start, end = end, className = className)
                }
            }
        }

        val plainText = plain.toString()
        if (plainText.isBlank()) return null
        return TajweedEntry(
            plainText = plainText,
            spans = mergeAdjacentSameClass(spans.mapNotNull { adjustToCluster(plainText, it) })
        )
    }

    private fun extractKey(obj: JSONObject): String? {
        val surah = obj.optInt("surah", -1).takeIf { it > 0 }
            ?: obj.optInt("sura", -1).takeIf { it > 0 }
        val ayah = obj.optInt("ayah", -1).takeIf { it > 0 }
            ?: obj.optInt("verse", -1).takeIf { it > 0 }
        if (surah != null && ayah != null) return "$surah|$ayah"

        val verseKey = obj.optString("verse_key", "")
        if (verseKey.contains(':')) {
            val parts = verseKey.split(':')
            if (parts.size == 2) {
                val s = parts[0].toIntOrNull()
                val a = parts[1].toIntOrNull()
                if (s != null && a != null) return "$s|$a"
            }
        }
        return null
    }

    private fun extractTextField(obj: JSONObject): String? {
        val keys = listOf("text", "tajweed_text", "plainText", "plain_text", "content", "uthmani")
        for (key in keys) {
            val value = obj.optString(key, "")
            if (value.isNotBlank()) return value
        }
        return null
    }

    private fun parseRangeSpans(obj: JSONObject): List<TajweedSpan> {
        val arrays = listOf("annotations", "spans", "segments", "tokens")
            .mapNotNull { key -> obj.optJSONArray(key) }
        if (arrays.isEmpty()) return emptyList()

        val out = mutableListOf<TajweedSpan>()
        arrays.forEach { arr ->
            for (i in 0 until arr.length()) {
                val item = arr.optJSONObject(i) ?: continue
                val start = item.optInt("start", -1)
                val end = item.optInt("end", -1)
                val className = item.optString("rule", item.optString("class", item.optString("name", "")))
                if (start >= 0 && end > start && className.isNotBlank()) {
                    out += TajweedSpan(start = start, end = end, className = className)
                }
            }
        }
        return out
    }

    private fun parseTaggedText(source: String): TajweedEntry? {
        if (!source.contains("<")) return null
        val plain = StringBuilder(source.length)
        val spans = mutableListOf<TajweedSpan>()
        val stack = ArrayDeque<Pair<String, Int>>()
        var i = 0
        while (i < source.length) {
            val ch = source[i]
            if (ch == '<') {
                val close = source.indexOf('>', startIndex = i + 1)
                if (close == -1) break
                val tagBody = source.substring(i + 1, close).trim()
                if (tagBody.startsWith("/")) {
                    val className = tagBody.removePrefix("/").trim()
                    val open = stack.removeLastOrNull()
                    if (open != null) {
                        val resolvedClass = if (open.first.isNotBlank()) open.first else className
                        if (resolvedClass.isNotBlank() && plain.length > open.second) {
                            spans += TajweedSpan(open.second, plain.length, resolvedClass)
                        }
                    }
                } else {
                    val classRegex = Regex("""class\s*=\s*['"]([^'"]+)['"]""")
                    val className = classRegex.find(tagBody)?.groupValues?.get(1).orEmpty()
                    stack += className to plain.length
                }
                i = close + 1
            } else {
                plain.append(ch)
                i++
            }
        }
        val text = plain.toString()
        if (text.isBlank()) return null
        return TajweedEntry(
            plainText = text,
            spans = mergeAdjacentSameClass(spans.mapNotNull { adjustToCluster(text, it) })
        )
    }

    private fun adjustToCluster(text: String, span: TajweedSpan): TajweedSpan? {
        if (text.isEmpty()) return null
        var start = min(max(span.start, 0), text.length)
        var end = min(max(span.end, 0), text.length)
        if (end <= start) return null

        while (start > 0 && isCombiningMark(text[start])) {
            start--
        }
        while (end < text.length && isCombiningMark(text[end])) {
            end++
        }
        if (end <= start) return null
        return TajweedSpan(start = start, end = end, className = span.className)
    }

    private fun isCombiningMark(char: Char): Boolean {
        return when (Character.getType(char.code)) {
            Character.NON_SPACING_MARK.toInt(),
            Character.COMBINING_SPACING_MARK.toInt(),
            Character.ENCLOSING_MARK.toInt() -> true
            else -> false
        }
    }

    private fun mergeAdjacentSameClass(spans: List<TajweedSpan>): List<TajweedSpan> {
        if (spans.isEmpty()) return emptyList()
        val sorted = spans.sortedWith(compareBy<TajweedSpan> { it.start }.thenBy { it.end })
        val merged = mutableListOf<TajweedSpan>()
        sorted.forEach { next ->
            val last = merged.lastOrNull()
            if (last != null && last.className == next.className && next.start <= last.end) {
                merged[merged.lastIndex] = last.copy(end = max(last.end, next.end))
            } else {
                merged += next
            }
        }
        return merged
    }
}
