package com.example.islamiccorpus.ui.screens.quran

import android.content.Context
import com.example.islamiccorpus.quran.tajweed.TajweedDataStore
import com.example.islamiccorpus.quran.tajweed.TajweedEntry

class QuranViewModel(
    appContext: Context
) {
    private val context = appContext.applicationContext

    private val tajweedMap: Map<String, TajweedEntry> by lazy {
        TajweedDataStore.load(
            context = context,
            baseTextByKey = loadQuranUthmani(context).associate { ayah ->
                "${ayah.surah}|${ayah.ayah}" to stripMushafSigns(ayah.text).trim()
            }
        )
    }

    fun loadedEntriesCount(): Int = tajweedMap.size

    fun getTajweedEntry(surah: Int, ayah: Int): TajweedEntry? = tajweedMap["$surah|$ayah"]
}
