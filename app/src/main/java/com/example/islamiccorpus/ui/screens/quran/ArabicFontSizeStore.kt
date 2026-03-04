package com.example.islamiccorpus.ui.screens.quran

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.quranDataStore by preferencesDataStore(name = "quran_prefs")

class ArabicFontSizeStore(
    private val context: Context
) {
    private val keyArabicFontSize = floatPreferencesKey("arabic_font_size")
    private val keyTajweedEnabled = booleanPreferencesKey("tajweed_enabled")
    private val keyTranslationEnabled = booleanPreferencesKey("translation_enabled")

    val fontSizeFlow: Flow<Float> = context.quranDataStore.data.map { prefs: Preferences ->
        prefs[keyArabicFontSize] ?: 20f
    }

    val tajweedEnabledFlow: Flow<Boolean> = context.quranDataStore.data.map { prefs: Preferences ->
        prefs[keyTajweedEnabled] ?: false
    }

    val translationEnabledFlow: Flow<Boolean> = context.quranDataStore.data.map { prefs: Preferences ->
        prefs[keyTranslationEnabled] ?: false
    }

    suspend fun setFontSize(value: Float) {
        context.quranDataStore.edit { prefs ->
            prefs[keyArabicFontSize] = value
        }
    }

    suspend fun setTajweedEnabled(value: Boolean) {
        context.quranDataStore.edit { prefs ->
            prefs[keyTajweedEnabled] = value
        }
    }

    suspend fun setTranslationEnabled(value: Boolean) {
        context.quranDataStore.edit { prefs ->
            prefs[keyTranslationEnabled] = value
        }
    }
}
