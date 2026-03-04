package com.example.islamiccorpus.ui.screens.quran

import android.content.Context
import android.text.Html
import android.util.Log
import org.json.JSONObject

object TafsirRepo {
    private const val TAG = "TAFSIR_ASSET"
    private const val TAFSIR_FILE = "en-tafsir-ibn-kathir.json"
    private val multiNewlineRegex = Regex("\\n{3,}")
    @Volatile
    private var didDebugCheck = false
    @Volatile
    private var didKeyInspect = false

    @Volatile
    private var cachedRoot: JSONObject? = null

    fun debugCheckAsset(context: Context) {
        if (didDebugCheck) return
        synchronized(this) {
            if (didDebugCheck) return
            try {
                val assets = context.assets.list("")?.toList().orEmpty().sorted()
                Log.d(TAG, "assets=${assets.joinToString()}")
                context.assets.open(TAFSIR_FILE).use { input ->
                    Log.d(TAG, "opened_ok bytes=${input.available()}")
                }
                didDebugCheck = true
            } catch (e: Exception) {
                Log.e(TAG, "error", e)
            }
        }
    }

    fun getTafsirPlain(context: Context, surah: Int, ayah: Int): String? {
        return try {
            val root = getRoot(context) ?: return null
            val tried = triedKeys(surah, ayah)
            val key = findKey(root, surah, ayah)
            if (key == null) {
                Log.w("TAFSIR_LOOKUP", "MISS s=$surah a=$ayah tried=${tried.joinToString()}")
                return null
            }
            val entry = root.optJSONObject(key) ?: return null
            val html = entry.optString("text", "").takeIf { it.isNotBlank() } ?: return null
            val plain = Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY).toString()
            plain.replace(multiNewlineRegex, "\n\n").trim().ifBlank { null }
        } catch (e: Exception) {
            Log.e(TAG, "getTafsirPlain failed surah=$surah ayah=$ayah", e)
            null
        }
    }

    private fun getRoot(context: Context): JSONObject? {
        debugCheckAsset(context)
        cachedRoot?.let { return it }
        synchronized(this) {
            cachedRoot?.let { return it }
            return try {
                val raw = context.assets.open(TAFSIR_FILE).bufferedReader(Charsets.UTF_8).use { it.readText() }
                val parsed = JSONObject(raw)
                cachedRoot = parsed
                Log.d(TAG, "parse_ok entries=${parsed.length()}")
                inspectKeys(parsed)
                parsed
            } catch (e: Exception) {
                Log.e(TAG, "error", e)
                null
            }
        }
    }

    private fun inspectKeys(root: JSONObject) {
        if (didKeyInspect) return
        synchronized(this) {
            if (didKeyInspect) return
            try {
                val iterator = root.keys()
                val first = mutableListOf<String>()
                while (iterator.hasNext() && first.size < 30) {
                    first += iterator.next()
                }
                Log.d("TAFSIR_KEYS", "totalKeys=${root.length()}")
                Log.d("TAFSIR_KEYS", "first30=${first.joinToString()}")
                Log.d(
                    "TAFSIR_KEYS",
                    "has(3:2)=${root.has("3:2")} has(3|2)=${root.has("3|2")} has(3-2)=${root.has("3-2")} has(3_2)=${root.has("3_2")} has(003:002)=${root.has("003:002")}"
                )
                didKeyInspect = true
            } catch (e: Exception) {
                Log.e("TAFSIR_KEYS", "error", e)
            }
        }
    }

    private fun triedKeys(s: Int, a: Int): List<String> = listOf(
        "$s:$a",
        "$s|$a",
        "$s-$a",
        "${s}_$a",
        "%03d:%03d".format(s, a),
        "%d:%d".format(s, a)
    ).distinct()

    private fun findKey(root: JSONObject, s: Int, a: Int): String? {
        return triedKeys(s, a).firstOrNull { root.has(it) }
    }
}
