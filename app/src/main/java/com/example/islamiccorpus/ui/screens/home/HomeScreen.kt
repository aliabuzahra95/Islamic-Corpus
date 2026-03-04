package com.example.islamiccorpus.ui.screens.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.islamiccorpus.ui.screens.home.components.ContinueStrip
import com.example.islamiccorpus.ui.screens.home.components.FolderGrid
import com.example.islamiccorpus.ui.screens.home.components.FolderSectionHeader
import com.example.islamiccorpus.ui.screens.home.components.HomeFab
import com.example.islamiccorpus.ui.screens.home.components.HomeSearchBar
import com.example.islamiccorpus.ui.screens.home.components.HomeTopBar
import com.example.islamiccorpus.ui.screens.home.components.PinnedSection
import com.example.islamiccorpus.ui.screens.home.components.RecentSection
import com.example.islamiccorpus.ui.screens.quran.ContinueReadingState
import com.example.islamiccorpus.ui.screens.quran.ContinueReadingStore
import com.example.islamiccorpus.ui.screens.quran.SurahCatalog
import com.example.islamiccorpus.ui.search.HomeQuranSearchSheet

@Composable
fun HomeScreen(
    contentPadding: PaddingValues,
    onContinueReadingClick: (ContinueReadingState) -> Unit,
    onSearchResultClick: (ContinueReadingState) -> Unit
) {
    val context = LocalContext.current
    val continueStore = remember(context) { ContinueReadingStore(context.applicationContext) }
    val continueState by continueStore.continueReadingFlow.collectAsState(initial = null)
    var showSearchSheet by remember { mutableStateOf(false) }
    val continueLabel = continueState?.let { state ->
        val name = SurahCatalog.firstOrNull { it.number == state.surah }?.englishName ?: "Surah ${state.surah}"
        "$name – Ayah ${state.ayah}"
    }
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
                        showSearchSheet = true
                    },
                    onFilterClick = {
                        // TODO: open filter
                    }
                )
            }

            if (continueState != null) {
                item {
                    ContinueStrip(
                        subtitle = continueLabel.orEmpty(),
                        onClick = { onContinueReadingClick(continueState!!) }
                    )
                }
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

    if (showSearchSheet) {
        HomeQuranSearchSheet(
            surahs = SurahCatalog,
            onDismiss = { showSearchSheet = false },
            onSurahSelected = { surah ->
                onSearchResultClick(
                    ContinueReadingState(
                        surah = surah,
                        ayah = 1,
                        updatedAt = System.currentTimeMillis()
                    )
                )
            },
            onAyahSelected = { surah, ayah ->
                onSearchResultClick(
                    ContinueReadingState(
                        surah = surah,
                        ayah = ayah,
                        updatedAt = System.currentTimeMillis()
                    )
                )
            }
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
