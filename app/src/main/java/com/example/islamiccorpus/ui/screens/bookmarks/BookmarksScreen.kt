package com.example.islamiccorpus.ui.screens.bookmarks

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.islamiccorpus.ui.screens.home.components.HomeSearchBar
import com.example.islamiccorpus.ui.screens.home.components.PinnedIconType
import com.example.islamiccorpus.ui.screens.home.components.PinnedItemRow

@Composable
fun BookmarksScreen(contentPadding: PaddingValues) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
    ) {
        item {
            Text(
                text = "BOOKMARKS",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.6.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.78f),
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 12.dp)
            )
        }

        item {
            HomeSearchBar(
                placeholder = "Search notes, folders, texts…",
                onSearchClick = {},
                onFilterClick = {}
            )
        }

        item {
            PinnedItemRow(
                title = "Principles of Ijaz al-Qur'an",
                type = "Note · Tafsir Al-Qur'an",
                icon = PinnedIconType.Document,
                onClick = {},
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 7.dp)
            )
        }

        item {
            PinnedItemRow(
                title = "Mustalah al-Hadith",
                type = "Subfolder · Hadith Sciences",
                icon = PinnedIconType.Folder,
                onClick = {},
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 7.dp)
            )
        }

        item {
            PinnedItemRow(
                title = "Al-Risalah – Summary",
                type = "Attachment · PDF",
                icon = PinnedIconType.Chat,
                onClick = {},
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 24.dp)
            )
        }
    }
}
