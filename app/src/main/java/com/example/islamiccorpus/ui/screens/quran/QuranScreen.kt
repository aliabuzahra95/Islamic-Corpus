package com.example.islamiccorpus.ui.screens.quran

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.Slider
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.window.PopupProperties
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.util.Log
import com.example.islamiccorpus.ui.screens.home.components.HomeSearchBar
import com.example.islamiccorpus.ui.theme.QpcHafsFontFamily
import com.example.islamiccorpus.quran.tajweed.TajweedEntry
import com.example.islamiccorpus.quran.tajweed.buildTajweedAnnotatedString
import kotlin.math.roundToInt
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.filterNotNull

private const val BASMALA = "بِسْمِ ٱللَّهِ ٱلرَّحْمَٰنِ ٱلرَّحِيمِ"

private data class RenderedAyah(
    val realAyahNumber: Int,
    val displayAyahNumber: Int,
    val text: String
)

private data class TafsirSheetData(
    val surah: Int,
    val ayah: Int,
    val text: String
)

private object TajweedColorResolver {
    private val mapped = mapOf(
        "madd_2" to Color(0xFF8E24AA),
        "madd_246" to Color(0xFF8E24AA),
        "madd_6" to Color(0xFF8E24AA),
        "madd_munfasil" to Color(0xFF8E24AA),
        "madd_muttasil" to Color(0xFF8E24AA),
        "ghunnah" to Color(0xFFFB8C00),
        "ikhfa" to Color(0xFFE53935),
        "ikhfa_shafawi" to Color(0xFF43A047),
        "idghaam_ghunnah" to Color(0xFF8E24AA),
        "idghaam_mutajanisayn" to Color(0xFF1E88E5),
        "idghaam_mutaqaribayn" to Color(0xFF1E88E5),
        "idghaam_no_ghunnah" to Color(0xFF8E24AA),
        "idghaam_shafawi" to Color(0xFF8E24AA),
        "iqlab" to Color(0xFF1E88E5),
        "qalqalah" to Color(0xFF43A047),
        "lam_shamsiyyah" to Color(0xFFFB8C00),
        "hamzat_wasl" to Color.Unspecified,
        "silent" to Color.Unspecified
    )
    private val warnedUnknown = mutableSetOf<String>()

    fun resolve(className: String): Color {
        val key = className.trim().lowercase()
        mapped[key]?.let { return it }
        synchronized(warnedUnknown) {
            if (warnedUnknown.add(key)) {
                Log.w("TAJWEED_COLOR", "Unmapped class=$key")
            }
        }
        return Color.Unspecified
    }

    fun toHex(color: Color): String {
        return if (color == Color.Unspecified) {
            "UNSPECIFIED"
        } else {
            String.format("#%08X", color.toArgb())
        }
    }
}

private object TajweedAudit {
    private val classes = mutableSetOf<String>()
    private var renderedAyahCount = 0
    private var dumped = false

    fun onAyahRendered(spanClasses: Set<String>) {
        synchronized(this) {
            classes += spanClasses.map { it.trim().lowercase() }
            renderedAyahCount++
            if (!dumped && renderedAyahCount >= 5 && classes.isNotEmpty()) {
                val sorted = classes.toList().sorted()
                Log.d("TAJWEED_AUDIT", "classes=${sorted.joinToString()}")
                sorted.forEach { className ->
                    val hex = TajweedColorResolver.toHex(TajweedColorResolver.resolve(className))
                    Log.d("TAJWEED_AUDIT", "$className -> $hex")
                }
                dumped = true
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuranScreen(
    contentPadding: PaddingValues,
    continueReadingOverride: ContinueReadingState? = null,
    onContinueReadingConsumed: () -> Unit = {}
) {
    val context = LocalContext.current
    val viewModel = remember(context) { QuranViewModel(context.applicationContext) }
    val fontSizeStore = remember(context) { ArabicFontSizeStore(context.applicationContext) }
    val continueStore = remember(context) { ContinueReadingStore(context.applicationContext) }
    val arabicFontSize by fontSizeStore.fontSizeFlow.collectAsState(initial = 20f)
    val tajweedEnabled by fontSizeStore.tajweedEnabledFlow.collectAsState(initial = false)
    val translationEnabled by fontSizeStore.translationEnabledFlow.collectAsState(initial = false)
    val continueReadingState by continueStore.continueReadingFlow.collectAsState(initial = null)
    val scope = rememberCoroutineScope()
    var showSheet by remember { mutableStateOf(false) }
    var sheetFontSize by remember(arabicFontSize) { mutableStateOf(arabicFontSize) }
    var tafsirSheetData by remember { mutableStateOf<TafsirSheetData?>(null) }
    var pendingTargetAyah by remember { mutableStateOf<Int?>(null) }
    val quranAyahs = remember(context) { loadQuranUthmani(context) }
    val ayahsBySurah = remember(quranAyahs) { quranAyahs.groupBy { it.surah } }
    var selectedSurah by remember { mutableStateOf<SurahInfo?>(null) }

    LaunchedEffect(continueReadingOverride) {
        val target = continueReadingOverride ?: return@LaunchedEffect
        val surahInfo = SurahCatalog.firstOrNull { it.number == target.surah } ?: return@LaunchedEffect
        selectedSurah = surahInfo
        pendingTargetAyah = target.ayah
        onContinueReadingConsumed()
    }

    if (selectedSurah == null) {
        SurahListScreen(
            contentPadding = contentPadding,
            arabicFontSize = arabicFontSize,
            continueReadingState = continueReadingState,
            onContinueReadingClick = { state ->
                val surahInfo = SurahCatalog.firstOrNull { it.number == state.surah }
                if (surahInfo != null) {
                    selectedSurah = surahInfo
                    pendingTargetAyah = state.ayah
                }
            },
            onFilterClick = { showSheet = true },
            onSurahSelected = { selectedSurah = it }
        )
    } else {
        SurahReaderScreen(
            contentPadding = contentPadding,
            surah = selectedSurah!!,
            ayahs = ayahsBySurah[selectedSurah!!.number].orEmpty(),
            arabicFontSize = arabicFontSize,
            tajweedEnabled = tajweedEnabled,
            translationEnabled = translationEnabled,
            initialTargetAyah = pendingTargetAyah,
            viewModel = viewModel,
            continueStore = continueStore,
            onInitialTargetHandled = { pendingTargetAyah = null },
            onToggleTranslation = { enabled -> scope.launch { fontSizeStore.setTranslationEnabled(enabled) } },
            onToggleTajweed = { enabled -> scope.launch { fontSizeStore.setTajweedEnabled(enabled) } },
            onOpenTafsir = { surahNum, ayahNum ->
                TafsirRepo.debugCheckAsset(context)
                val text = TafsirRepo.getTafsirPlain(context, surahNum, ayahNum)
                Log.d("TAFSIR_LOOKUP", "key=$surahNum:$ayahNum found=${text != null}")
                tafsirSheetData = TafsirSheetData(
                    surah = surahNum,
                    ayah = ayahNum,
                    text = text ?: "Tafsir not found for $surahNum:$ayahNum (check asset + key)."
                )
            },
            onBack = { selectedSurah = null }
        )
    }

    if (showSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSheet = false },
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface,
            shape = RoundedCornerShape(14.dp),
            dragHandle = {
                BottomSheetDefaults.DragHandle(
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Tajweed",
                    style = MaterialTheme.typography.bodyMedium
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (tajweedEnabled) "ON" else "OFF",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Switch(
                        checked = tajweedEnabled,
                        onCheckedChange = { enabled ->
                            scope.launch { fontSizeStore.setTajweedEnabled(enabled) }
                        }
                    )
                }
                Text(
                    text = "Translation",
                    style = MaterialTheme.typography.bodyMedium
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (translationEnabled) "ON" else "OFF",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Switch(
                        checked = translationEnabled,
                        onCheckedChange = { enabled ->
                            scope.launch { fontSizeStore.setTranslationEnabled(enabled) }
                        }
                    )
                }
                Text(
                    text = "Arabic Font Size",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = sheetFontSize.roundToInt().toString(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Slider(
                    value = sheetFontSize,
                    onValueChange = { raw ->
                        val v = raw.roundToInt().toFloat().coerceIn(14f, 28f)
                        sheetFontSize = v
                        scope.launch { fontSizeStore.setFontSize(v) }
                    },
                    valueRange = 14f..28f,
                    steps = 13
                )
            }
        }
    }

    tafsirSheetData?.let { tafsir ->
        ModalBottomSheet(
            onDismissRequest = { tafsirSheetData = null },
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface,
            shape = RoundedCornerShape(14.dp),
            dragHandle = {
                BottomSheetDefaults.DragHandle(
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "Tafsir Ibn Kathir",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "${tafsir.surah}:${tafsir.ayah}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp, bottom = 10.dp)
                )
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = tafsir.text,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
private fun SurahListScreen(
    contentPadding: PaddingValues,
    arabicFontSize: Float,
    continueReadingState: ContinueReadingState?,
    onContinueReadingClick: (ContinueReadingState) -> Unit,
    onFilterClick: () -> Unit,
    onSurahSelected: (SurahInfo) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
    ) {
        item {
            Text(
                text = "QURAN",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.6.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.78f),
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 12.dp)
            )
        }

        item {
            HomeSearchBar(
                placeholder = "Search surah or ayah…",
                onSearchClick = {},
                onFilterClick = onFilterClick
            )
        }

        if (continueReadingState != null) {
            item {
                val surahLabel = SurahCatalog.firstOrNull { it.number == continueReadingState.surah }?.englishName
                    ?: "Surah ${continueReadingState.surah}"
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, bottom = 10.dp)
                        .clickable { onContinueReadingClick(continueReadingState) },
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 1.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Continue reading",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "$surahLabel — Ayah ${continueReadingState.ayah}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1
                        )
                    }
                }
            }
        }

        items(SurahCatalog) { surah ->
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, bottom = 10.dp)
                    .clickable { onSurahSelected(surah) },
                shape = androidx.compose.foundation.shape.RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 1.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = surah.number.toString(),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = surah.arabicName,
                            fontSize = arabicFontSize.sp,
                            fontFamily = QpcHafsFontFamily,
                            textAlign = TextAlign.End,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Text(
                            text = surah.englishName,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.72f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SurahReaderScreen(
    contentPadding: PaddingValues,
    surah: SurahInfo,
    ayahs: List<QuranAyah>,
    arabicFontSize: Float,
    tajweedEnabled: Boolean,
    translationEnabled: Boolean,
    initialTargetAyah: Int?,
    viewModel: QuranViewModel,
    continueStore: ContinueReadingStore,
    onInitialTargetHandled: () -> Unit,
    onToggleTranslation: (Boolean) -> Unit,
    onToggleTajweed: (Boolean) -> Unit,
    onOpenTafsir: (Int, Int) -> Unit,
    onBack: () -> Unit
) {
    BackHandler {
        onBack()
    }
    val context = LocalContext.current
    val listState = rememberLazyListState()
    val renderedAyahs = remember(surah.number, ayahs) {
        buildRenderedAyahs(surah.number, ayahs)
    }
    var loadedLogDoneForSurah by remember(surah.number) { mutableStateOf(false) }
    val firstFiveLoggedAyahs = remember(surah.number, tajweedEnabled) { mutableSetOf<Int>() }

    LaunchedEffect(tajweedEnabled, surah.number) {
        if (tajweedEnabled && !loadedLogDoneForSurah) {
            Log.d("TAJWEED_JSON", "loadedEntries=${viewModel.loadedEntriesCount()}")
            loadedLogDoneForSurah = true
        }
    }

    LaunchedEffect(initialTargetAyah, surah.number, renderedAyahs) {
        val target = initialTargetAyah ?: return@LaunchedEffect
        val targetIndex = renderedAyahs.indexOfFirst { it.realAyahNumber == target }
        if (targetIndex >= 0) {
            val headerCount = 2 + if (surah.number != 9) 1 else 0
            listState.scrollToItem(headerCount + targetIndex)
        }
        onInitialTargetHandled()
    }

    LaunchedEffect(surah.number, renderedAyahs, listState) {
        val headerCount = 2 + if (surah.number != 9) 1 else 0
        var lastSaved: Pair<Int, Int>? = null
        snapshotFlow { listState.firstVisibleItemIndex }
            .map { firstVisible ->
                val ayahIndex = (firstVisible - headerCount).coerceAtLeast(0)
                renderedAyahs.getOrNull(ayahIndex)?.realAyahNumber
            }
            .filterNotNull()
            .debounce(500)
            .distinctUntilChanged()
            .collect { realAyah ->
                val next = surah.number to realAyah
                if (lastSaved != next) {
                    continueStore.setLastRead(surah.number, realAyah)
                    lastSaved = next
                }
            }
    }

    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
    ) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Back",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable(onClick = onBack)
                )
                Text(
                    text = "${surah.number}. ${surah.englishName}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.78f)
                )
            }
        }

        item {
            Text(
                text = surah.arabicName,
                fontSize = arabicFontSize.sp,
                fontFamily = QpcHafsFontFamily,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            )
        }

        if (surah.number != 9) {
            item {
                Text(
                    text = "بِسْمِ ٱللَّهِ ٱلرَّحْمَٰنِ ٱلرَّحِيمِ",
                    fontSize = arabicFontSize.sp,
                    fontFamily = QpcHafsFontFamily,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }

        items(renderedAyahs) { ayah ->
            val baseText = ayah.text
            val translation = remember(surah.number, ayah.realAyahNumber) {
                TranslationRepo.getTranslation(context, surah.number, ayah.realAyahNumber)
            }
            var menuExpanded by remember(surah.number, ayah.realAyahNumber) { mutableStateOf(false) }
            val adjustedTajweedEntry = adjustTajweedEntryForDisplay(
                surahNumber = surah.number,
                ayahNumber = ayah.realAyahNumber,
                entry = viewModel.getTajweedEntry(surah.number, ayah.realAyahNumber)
            )
            val applied = adjustedTajweedEntry != null && adjustedTajweedEntry.plainText == baseText
            if (tajweedEnabled && firstFiveLoggedAyahs.size < 5 && firstFiveLoggedAyahs.add(ayah.realAyahNumber)) {
                Log.d(
                    "TAJWEED_JSON",
                    "surah=${surah.number} ayah=${ayah.realAyahNumber} applied=$applied"
                )
            }
            if (tajweedEnabled) {
                TajweedAudit.onAyahRendered(
                    adjustedTajweedEntry?.spans?.map { it.className }?.toSet().orEmpty()
                )
            }
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, bottom = 10.dp),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 1.dp
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        IconButton(
                            onClick = { menuExpanded = true },
                            modifier = Modifier.align(Alignment.TopEnd)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.MoreVert,
                                contentDescription = "Ayah actions",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false },
                            modifier = Modifier
                                .widthIn(min = 180.dp)
                                .clip(RoundedCornerShape(16.dp)),
                            properties = PopupProperties(focusable = true),
                            containerColor = MaterialTheme.colorScheme.surface,
                            tonalElevation = 0.dp,
                            shadowElevation = 0.dp
                        ) {
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = if (translationEnabled) "Turn OFF Translation" else "Turn ON Translation",
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                },
                                onClick = {
                                    runCatching {
                                        onToggleTranslation(!translationEnabled)
                                    }.onFailure { e ->
                                        Log.e(
                                            "QURAN_ACTION_FAIL",
                                            "s=${surah.number} aReal=${ayah.realAyahNumber} action=TOGGLE_TRANSLATION tajweed=$tajweedEnabled translation=$translationEnabled",
                                            e
                                        )
                                    }
                                    menuExpanded = false
                                }
                            )
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = if (tajweedEnabled) "Turn OFF Tajweed" else "Turn ON Tajweed",
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                },
                                onClick = {
                                    runCatching {
                                        onToggleTajweed(!tajweedEnabled)
                                    }.onFailure { e ->
                                        Log.e(
                                            "QURAN_ACTION_FAIL",
                                            "s=${surah.number} aReal=${ayah.realAyahNumber} action=TOGGLE_TAJWEED tajweed=$tajweedEnabled translation=$translationEnabled",
                                            e
                                        )
                                    }
                                    menuExpanded = false
                                }
                            )
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = "Copy Arabic",
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                },
                                onClick = {
                                    runCatching {
                                        copyToClipboard(context, "Arabic Ayah", baseText)
                                    }.onFailure { e ->
                                        Log.e(
                                            "QURAN_ACTION_FAIL",
                                            "s=${surah.number} aReal=${ayah.realAyahNumber} action=COPY_ARABIC tajweed=$tajweedEnabled translation=$translationEnabled",
                                            e
                                        )
                                    }
                                    menuExpanded = false
                                }
                            )
                            if (!translation.isNullOrBlank()) {
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = "Copy Translation",
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    },
                                    onClick = {
                                        runCatching {
                                            copyToClipboard(context, "Ayah Translation", translation)
                                        }.onFailure { e ->
                                            Log.e(
                                                "QURAN_ACTION_FAIL",
                                                "s=${surah.number} aReal=${ayah.realAyahNumber} action=COPY_TRANSLATION tajweed=$tajweedEnabled translation=$translationEnabled",
                                                e
                                            )
                                        }
                                        menuExpanded = false
                                    }
                                )
                            }
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = "Share",
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                },
                                onClick = {
                                    runCatching {
                                        shareAyah(context, baseText, translation)
                                    }.onFailure { e ->
                                        Log.e(
                                            "QURAN_ACTION_FAIL",
                                            "s=${surah.number} aReal=${ayah.realAyahNumber} action=SHARE tajweed=$tajweedEnabled translation=$translationEnabled",
                                            e
                                        )
                                    }
                                    menuExpanded = false
                                }
                            )
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = "Tafsir",
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                },
                                onClick = {
                                    runCatching {
                                        Log.d(
                                            "TAFSIR_NUMBERS",
                                            "surah=${surah.number} displayAyah=${ayah.displayAyahNumber} sourceAyah=${ayah.realAyahNumber}"
                                        )
                                        onOpenTafsir(surah.number, ayah.realAyahNumber)
                                    }.onFailure { e ->
                                        Log.e(
                                            "QURAN_ACTION_FAIL",
                                            "s=${surah.number} aReal=${ayah.realAyahNumber} action=TAFSIR tajweed=$tajweedEnabled translation=$translationEnabled",
                                            e
                                        )
                                    }
                                    menuExpanded = false
                                }
                            )
                        }
                    }

                    if (tajweedEnabled) {
                        val annotated = buildTajweedAnnotatedString(
                            baseText = baseText,
                            tajweed = adjustedTajweedEntry,
                            colorResolver = { className -> TajweedColorResolver.resolve(className) }
                        )
                        Text(
                            text = annotated,
                            fontSize = arabicFontSize.sp,
                            fontFamily = QpcHafsFontFamily,
                            lineHeight = 46.sp,
                            textAlign = TextAlign.End,
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        Text(
                            text = baseText,
                            fontSize = arabicFontSize.sp,
                            fontFamily = QpcHafsFontFamily,
                            lineHeight = 46.sp,
                            textAlign = TextAlign.End,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    if (translationEnabled && !translation.isNullOrBlank()) {
                        Text(
                            text = translation,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp)
                        )
                    }
                    Text(
                        text = "Ayah ${ayah.displayAyahNumber}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.72f),
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }
    }
}

private fun buildRenderedAyahs(
    surahNumber: Int,
    ayahs: List<QuranAyah>
): List<RenderedAyah> {
    val tempAyahs = mutableListOf<Pair<Int, String>>()
    ayahs.forEach { ayah ->
        var cleaned = stripMushafSigns(ayah.text).trim()
        if (surahNumber != 9) {
            if (cleaned == BASMALA) {
                return@forEach
            }
            if (cleaned.startsWith(BASMALA)) {
                cleaned = cleaned.removePrefix(BASMALA).trimStart()
            }
        }
        if (cleaned.isBlank()) return@forEach
        tempAyahs += ayah.ayah to cleaned
    }
    return tempAyahs.mapIndexed { index, (realAyahNumber, text) ->
        RenderedAyah(
            realAyahNumber = realAyahNumber,
            displayAyahNumber = index + 1,
            text = text
        )
    }
}

private fun adjustTajweedEntryForDisplay(
    surahNumber: Int,
    ayahNumber: Int,
    entry: TajweedEntry?
): TajweedEntry? {
    if (entry == null) return null
    if (surahNumber == 9) return entry
    var text = entry.plainText.trim()
    var spans = entry.spans

    if (text == BASMALA) {
        return TajweedEntry(plainText = "", spans = emptyList())
    }
    if (ayahNumber == 1 && text.startsWith(BASMALA)) {
        var cutoff = BASMALA.length
        while (cutoff < text.length && text[cutoff].isWhitespace()) {
            cutoff++
        }
        text = text.substring(cutoff)
        spans = spans.mapNotNull { span ->
            val start = (span.start - cutoff).coerceAtLeast(0)
            val end = (span.end - cutoff).coerceAtMost(text.length)
            if (end > start) span.copy(start = start, end = end) else null
        }
    }
    return TajweedEntry(plainText = text, spans = spans)
}

private fun copyToClipboard(context: Context, label: String, value: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager ?: return
    clipboard.setPrimaryClip(ClipData.newPlainText(label, value))
}

private fun shareAyah(context: Context, arabic: String, translation: String?) {
    val body = buildString {
        append(arabic)
        if (!translation.isNullOrBlank()) {
            append("\n\n")
            append(translation)
        }
    }
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, body)
    }
    val chooser = Intent.createChooser(intent, "Share Ayah").apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    context.startActivity(chooser)
}
