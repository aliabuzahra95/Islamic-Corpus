package com.example.islamiccorpus.ui.screens.library

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.islamiccorpus.ui.screens.home.components.FolderCard
import com.example.islamiccorpus.ui.screens.home.components.HomeSearchBar

@Composable
fun LibraryScreen(contentPadding: PaddingValues) {
    val folders = listOf(
        Triple("Tafsir Al-Qur'an", "24 notes", Color(0xFF2F80ED)),
        Triple("Usool al-Fiqh", "18 notes", Color(0xFFF2C94C)),
        Triple("Hadith Sciences", "41 notes", Color(0xFF27AE60)),
        Triple("Aqeedah References", "9 notes", Color(0xFF9B51E0)),
        Triple("Arabic Grammar", "33 notes", Color(0xFFEB5757)),
        Triple("Research Papers", "7 notes", Color(0xFF2D9CDB))
    )

    val rows = folders.chunked(3)
    val chipLabels = listOf("All", "Notes", "PDFs", "Images", "Audio")

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
    ) {
        item {
            Text(
                text = "LIBRARY",
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
            LazyRow(
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(chipLabels) { label ->
                    FilterChip(
                        selected = false,
                        onClick = {},
                        label = { Text(text = label) }
                    )
                }
            }
        }

        itemsIndexed(rows) { index, rowItems ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = 16.dp,
                        end = 16.dp,
                        bottom = if (index == rows.lastIndex) 24.dp else 7.dp
                    ),
                horizontalArrangement = Arrangement.spacedBy(7.dp)
            ) {
                rowItems.forEach { folder ->
                    FolderCard(
                        modifier = Modifier.weight(1f),
                        title = folder.first,
                        meta = folder.second,
                        lipColor = folder.third,
                        onClick = {}
                    )
                }
                repeat(3 - rowItems.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}
