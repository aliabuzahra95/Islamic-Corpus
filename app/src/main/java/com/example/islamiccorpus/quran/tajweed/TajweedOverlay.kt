package com.example.islamiccorpus.quran.tajweed

import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString

private const val TAG = "TAJWEED_JSON"

fun buildTajweedAnnotatedString(
    baseText: String,
    tajweed: TajweedEntry?,
    colorResolver: (String) -> Color
): AnnotatedString {
    if (tajweed == null) return AnnotatedString(baseText)
    if (tajweed.plainText != baseText) {
        Log.w(TAG, "Mismatch baseLen=${baseText.length} tajLen=${tajweed.plainText.length}")
        Log.w(TAG, "baseSnippet=${baseText.take(80)}")
        Log.w(TAG, "tajSnippet=${tajweed.plainText.take(80)}")
        return AnnotatedString(baseText)
    }
    return buildAnnotatedString {
        append(baseText)
        tajweed.spans.forEach { span ->
            if (span.start in 0 until span.end && span.end <= baseText.length) {
                addStyle(
                    style = SpanStyle(color = colorResolver(span.className)),
                    start = span.start,
                    end = span.end
                )
            }
        }
    }
}
