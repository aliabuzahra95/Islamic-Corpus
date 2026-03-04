package com.example.islamiccorpus.ui.screens.quran

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.continueReadingDataStore by preferencesDataStore(name = "continue_reading_prefs")

data class ContinueReadingState(
    val surah: Int,
    val ayah: Int,
    val updatedAt: Long
)

class ContinueReadingStore(
    private val context: Context
) {
    private val keySurah = intPreferencesKey("last_read_surah")
    private val keyAyah = intPreferencesKey("last_read_ayah")
    private val keyUpdatedAt = longPreferencesKey("last_read_updated_at")

    val continueReadingFlow: Flow<ContinueReadingState?> = context.continueReadingDataStore.data.map { prefs ->
        val surah = prefs[keySurah]
        val ayah = prefs[keyAyah]
        if (surah == null || ayah == null) {
            null
        } else {
            ContinueReadingState(
                surah = surah,
                ayah = ayah,
                updatedAt = prefs[keyUpdatedAt] ?: 0L
            )
        }
    }

    suspend fun setLastRead(surah: Int, ayah: Int) {
        context.continueReadingDataStore.edit { prefs ->
            prefs[keySurah] = surah
            prefs[keyAyah] = ayah
            prefs[keyUpdatedAt] = System.currentTimeMillis()
        }
    }

    suspend fun clear() {
        context.continueReadingDataStore.edit { prefs ->
            prefs.remove(keySurah)
            prefs.remove(keyAyah)
            prefs.remove(keyUpdatedAt)
        }
    }
}
