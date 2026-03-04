package com.example.islamiccorpus.ui.screens.home.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private data class RecentItem(
    val title: String,
    val preview: String,
    val time: String,
    val type: String
)

private val recentItems = listOf(
    RecentItem(
        title = "Notes on Naskh wa Mansukh",
        preview = "The abrogated rulings in Surah Al-Baqarah concerning…",
        time = "2 hours ago",
        type = "Note"
    ),
    RecentItem(
        title = "Kitab al-Umm – Volume III",
        preview = "Fiqh of ritual purity according to Imam al-Shafi'i",
        time = "Yesterday",
        type = "PDF"
    ),
    RecentItem(
        title = "Principles of Arabic Morphology",
        preview = "Derived forms (mushtaqq) and their grammatical roles…",
        time = "2 days ago",
        type = "Note"
    ),
    RecentItem(
        title = "Hadith Sciences",
        preview = "Added subfolder: Mustalah al-Hadith",
        time = "3 days ago",
        type = "Folder"
    ),
    RecentItem(
        title = "Ijma' as a Source of Law",
        preview = "Consensus of the scholars after the Prophet's era…",
        time = "5 days ago",
        type = "Note"
    )
)

@Composable
fun RecentSection(modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "RECENT ACTIVITY",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.6.sp,
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 10.dp)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, bottom = 0.dp)
        ) {
            recentItems.forEach { item ->
                RecentItemRow(
                    title = item.title,
                    preview = item.preview,
                    time = item.time,
                    type = item.type,
                    onClick = {
                        // TODO: open recent item
                    }
                )
            }
        }
    }
}
