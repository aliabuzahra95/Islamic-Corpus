package com.example.islamiccorpus.ui.screens.home.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private data class FolderItem(
    val title: String,
    val meta: String,
    val lipColor: Color
)

private val folders = listOf(
    FolderItem("Tafsir Al-Qur'an", "24 notes", Color(0xFF2F80ED)),
    FolderItem("Usool al-Fiqh", "18 notes", Color(0xFFF2C94C)),
    FolderItem("Hadith Sciences", "41 notes", Color(0xFF27AE60)),
    FolderItem("Aqeedah References", "9 notes", Color(0xFF9B51E0)),
    FolderItem("Arabic Grammar", "33 notes", Color(0xFFEB5757)),
    FolderItem("Research Papers", "7 notes", Color(0xFF2D9CDB))
)

@Composable
fun FolderGrid(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        folders.chunked(3).forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(7.dp)
            ) {
                rowItems.forEach { folder ->
                    FolderCard(
                        modifier = Modifier.weight(1f),
                        title = folder.title,
                        meta = folder.meta,
                        lipColor = folder.lipColor,
                        onClick = {
                            // TODO: open folder
                        }
                    )
                }

                repeat(3 - rowItems.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}
