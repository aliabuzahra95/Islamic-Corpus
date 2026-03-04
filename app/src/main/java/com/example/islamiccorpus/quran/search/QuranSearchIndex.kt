package com.example.islamiccorpus.quran.search

import android.content.Context
import com.example.islamiccorpus.ui.screens.quran.SurahInfo

data class AyahEntry(
    val surah: Int,
    val ayah: Int,
    val text: String,
    val normalized: String
)

object QuranSearchIndex {
    @Volatile
    private var loaded = false
    private var ayahs: List<AyahEntry> = emptyList()
    private var byKey: Map<String, AyahEntry> = emptyMap()

    private val jumpRegex = Regex("""^\s*(\d{1,3})\s*[:\-\s]\s*(\d{1,3})\s*$""")

    fun ensureLoaded(context: Context) {
        if (loaded) return
        synchronized(this) {
            if (loaded) return
            val parsed = context.assets.open("quran-uthmani.txt")
                .bufferedReader(Charsets.UTF_8)
                .useLines { lines ->
                    lines.mapNotNull { line ->
                        if (line.isBlank() || line.startsWith("#")) return@mapNotNull null
                        val parts = line.split('|', limit = 3)
                        if (parts.size != 3) return@mapNotNull null
                        val surah = parts[0].toIntOrNull() ?: return@mapNotNull null
                        val ayah = parts[1].toIntOrNull() ?: return@mapNotNull null
                        val text = parts[2].trim()
                        AyahEntry(
                            surah = surah,
                            ayah = ayah,
                            text = text,
                            normalized = normalizeArabic(text)
                        )
                    }.toList()
                }
            ayahs = parsed
            byKey = parsed.associateBy { "${it.surah}:${it.ayah}" }
            loaded = true
        }
    }

    fun searchSurahs(query: String, surahs: List<SurahInfo>): List<SurahInfo> {
        val q = query.trim()
        if (q.isBlank()) return emptyList()
        val qLower = q.lowercase()
        val qNorm = normalizeArabic(q)

        return surahs.filter { surah ->
            val numberMatch = q.toIntOrNull()?.let { it == surah.number } ?: false
            val englishMatch = surah.englishName.lowercase().contains(qLower)
            val arabicMatch = normalizeArabic(surah.arabicName).contains(qNorm)
            numberMatch || englishMatch || arabicMatch
        }.take(20)
    }

    fun searchAyahs(query: String, limit: Int = 50): List<AyahEntry> {
        val q = query.trim()
        if (q.isBlank()) return emptyList()

        jumpRegex.matchEntire(q)?.let { match ->
            val s = match.groupValues[1].toIntOrNull()
            val a = match.groupValues[2].toIntOrNull()
            if (s != null && a != null) {
                val exact = byKey["$s:$a"]
                if (exact != null) return listOf(exact)
            }
        }

        val qNorm = normalizeArabic(q)
        if (qNorm.isBlank()) return emptyList()
        return ayahs.asSequence()
            .filter { it.normalized.contains(qNorm) }
            .take(limit)
            .toList()
    }

    private fun normalizeArabic(input: String): String {
        val sb = StringBuilder(input.length)
        var i = 0
        while (i < input.length) {
            val cp = input.codePointAt(i)
            val remove =
                cp in 0x064B..0x0652 ||
                    cp == 0x0670 ||
                    cp in 0x0653..0x0655 ||
                    cp == 0x0640
            if (!remove) {
                sb.appendCodePoint(cp)
            }
            i += Character.charCount(cp)
        }
        return sb.toString()
            .replace(Regex("""[\p{P}\p{S}]"""), " ")
            .replace(Regex("""\s+"""), " ")
            .trim()
    }
}
