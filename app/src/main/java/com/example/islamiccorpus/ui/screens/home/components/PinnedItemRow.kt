package com.example.islamiccorpus.ui.screens.home.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PinnedItemRow(
    title: String,
    type: String,
    icon: PinnedIconType,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(targetValue = if (isPressed) 0.99f else 1f, label = "pinnedScale")

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        shape = RoundedCornerShape(9.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(28.dp),
                shape = RoundedCornerShape(7.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    PinnedGlyph(
                        type = icon,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            Row(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = type,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.62f),
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Normal,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Icon(
                    imageVector = Icons.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.52f),
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

internal data class PinnedItem(
    val title: String,
    val type: String,
    val icon: PinnedIconType
)

internal val pinnedItems = listOf(
    PinnedItem(
        title = "Principles of Ijaz al-Qur'an",
        type = "Note · Tafsir Al-Qur'an",
        icon = PinnedIconType.Document
    ),
    PinnedItem(
        title = "Mustalah al-Hadith",
        type = "Subfolder · Hadith Sciences",
        icon = PinnedIconType.Folder
    ),
    PinnedItem(
        title = "Al-Risalah – Summary",
        type = "Attachment · PDF",
        icon = PinnedIconType.Chat
    )
)

enum class PinnedIconType {
    Document,
    Folder,
    Chat
}

@Composable
private fun PinnedGlyph(type: PinnedIconType, modifier: Modifier = Modifier) {
    val color = MaterialTheme.colorScheme.primary
    Canvas(modifier = modifier) {
        when (type) {
            PinnedIconType.Document -> {
                val stroke = 1.8.dp.toPx()
                drawRoundRect(
                    color = color,
                    topLeft = Offset(size.width * 0.14f, size.height * 0.1f),
                    size = androidx.compose.ui.geometry.Size(size.width * 0.72f, size.height * 0.8f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(2.dp.toPx(), 2.dp.toPx()),
                    style = Stroke(width = stroke)
                )
                drawLine(
                    color = color,
                    start = Offset(size.width * 0.28f, size.height * 0.52f),
                    end = Offset(size.width * 0.72f, size.height * 0.52f),
                    strokeWidth = stroke,
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = color,
                    start = Offset(size.width * 0.28f, size.height * 0.72f),
                    end = Offset(size.width * 0.66f, size.height * 0.72f),
                    strokeWidth = stroke,
                    cap = StrokeCap.Round
                )
            }

            PinnedIconType.Folder -> {
                val stroke = 1.8.dp.toPx()
                drawRoundRect(
                    color = color,
                    topLeft = Offset(size.width * 0.08f, size.height * 0.25f),
                    size = androidx.compose.ui.geometry.Size(size.width * 0.84f, size.height * 0.58f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(2.dp.toPx(), 2.dp.toPx()),
                    style = Stroke(width = stroke)
                )
                drawLine(
                    color = color,
                    start = Offset(size.width * 0.12f, size.height * 0.25f),
                    end = Offset(size.width * 0.42f, size.height * 0.25f),
                    strokeWidth = stroke,
                    cap = StrokeCap.Round
                )
            }

            PinnedIconType.Chat -> {
                val stroke = 1.8.dp.toPx()
                drawRoundRect(
                    color = color,
                    topLeft = Offset(size.width * 0.1f, size.height * 0.14f),
                    size = androidx.compose.ui.geometry.Size(size.width * 0.8f, size.height * 0.64f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(3.dp.toPx(), 3.dp.toPx()),
                    style = Stroke(width = stroke)
                )
                drawLine(
                    color = color,
                    start = Offset(size.width * 0.38f, size.height * 0.78f),
                    end = Offset(size.width * 0.28f, size.height * 0.96f),
                    strokeWidth = stroke,
                    cap = StrokeCap.Round
                )
            }
        }
    }
}
