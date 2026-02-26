package com.example.islamiccorpus.ui.screens.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import com.example.islamiccorpus.ui.screens.home.components.ContinueStrip
import com.example.islamiccorpus.ui.screens.home.components.FolderGrid
import com.example.islamiccorpus.ui.screens.home.components.FolderSectionHeader
import com.example.islamiccorpus.ui.screens.home.components.HomeFab
import com.example.islamiccorpus.ui.screens.home.components.HomeSearchBar
import com.example.islamiccorpus.ui.screens.home.components.HomeTopBar
import com.example.islamiccorpus.ui.screens.home.components.PinnedSection
import com.example.islamiccorpus.ui.screens.home.components.RecentSection

@Composable
fun HomeScreen(contentPadding: PaddingValues) {
    val layoutDir = LocalLayoutDirection.current
    val listPadding = PaddingValues(
        start = contentPadding.calculateStartPadding(layoutDir),
        top = contentPadding.calculateTopPadding(),
        end = contentPadding.calculateEndPadding(layoutDir),
        bottom = contentPadding.calculateBottomPadding() + 90.dp
    )

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = listPadding
        ) {
            item {
                HomeTopBar(
                    onSettingsClick = {
                        // TODO: wire settings action
                    }
                )
            }

            item {
                HomeSearchBar(
                    placeholder = "Search notes, folders, texts...",
                    onSearchClick = {
                        // TODO: open search
                    },
                    onFilterClick = {
                        // TODO: open filter
                    }
                )
            }

            item {
                ContinueStrip(
                    onClick = {
                        // TODO: open continue reading
                    }
                )
            }

            item { FolderSectionHeader() }
            item { FolderGrid() }

            item { PinnedSection() }

            item { RecentSection() }
        }

        HomeFab(
            onClick = {
                // TODO: add action
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(
                    end = contentPadding.calculateEndPadding(layoutDir) + 16.dp,
                    bottom = contentPadding.calculateBottomPadding() + 18.dp
                )
        )
    }
}

private fun PaddingValues.calculateEndPadding(layoutDirection: LayoutDirection) =
    if (layoutDirection == LayoutDirection.Ltr) {
        calculateRightPadding(layoutDirection)
    } else {
        calculateLeftPadding(layoutDirection)
    }

private fun PaddingValues.calculateStartPadding(layoutDirection: LayoutDirection) =
    if (layoutDirection == LayoutDirection.Ltr) {
        calculateLeftPadding(layoutDirection)
    } else {
        calculateRightPadding(layoutDirection)
    }
