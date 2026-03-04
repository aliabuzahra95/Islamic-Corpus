package com.example.islamiccorpus.ui.screens.quran

import android.content.Context
import android.util.Log
import org.json.JSONObject
import org.json.JSONTokener

object TranslationRepo {
    private const val TAG = "QURAN_TRANSLATION"
    private const val ASSET_NAME = "en-sahih-international-chunks.json"

    @Volatile
    private var cachedRoot: JSONObject? = null

    fun getTranslation(context: Context, surah: Int, ayah: Int): String? {
        return try {
            val root = getRoot(context) ?: return null
            val key = "$surah:$ayah"
            val entry = root.optJSONObject(key) ?: return null
            val tArray = entry.optJSONArray("t") ?: return null
            val text = StringBuilder()
            for (i in 0 until tArray.length()) {
                val part = tArray.opt(i)
                if (part is String) {
                    text.append(part)
                }
            }
            text.toString().trim().ifBlank { null }
        } catch (e: Exception) {
            Log.e(TAG, "getTranslation failed surah=$surah ayah=$ayah", e)
            null
        }
    }

    private fun getRoot(context: Context): JSONObject? {
        cachedRoot?.let { return it }
        synchronized(this) {
            cachedRoot?.let { return it }
            return try {
                val raw = context.assets.open(ASSET_NAME).bufferedReader(Charsets.UTF_8).use { it.readText() }
                val parsed = JSONTokener(raw).nextValue() as? JSONObject
                cachedRoot = parsed
                parsed
            } catch (e: Exception) {
                Log.w(TAG, "Failed loading translation asset: $ASSET_NAME", e)
                null
            }
        }
    }
}
