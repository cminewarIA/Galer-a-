package com.example.ui

import android.text.format.Formatter
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.*
import com.example.network.NetworkShareManager
import java.text.SimpleDateFormat
import java.util.*

// Studio Charcoal Matte Premium Dark Palette Tokens
private val DeepCharcoal = Color(0xFF0F1013)
private val CarbonSlate = Color(0xFF16181D)
private val DarkCardBg = Color(0xFF1F2229)
private val PhotoAmber = Color(0xFFF39C12) // Golden Amber Accent
private val OceanBlue = Color(0xFF2980B9)  // Deep Raw Ocean Accent
private val EmeraldSync = Color(0xFF2ECC71) // Active Synced
private val ErrorCrimson = Color(0xFFE74C3C) // Error red
private val CodeGrey = Color(0xFF111215)
private val LightTextMuted = Color(0xFF8B949E)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GalleryApp(viewModel: GalleryViewModel) {
    val connections by viewModel.connections.collectAsStateWithLifecycle()
    val filteredAssets by viewModel.filteredAssets.collectAsStateWithLifecycle()
    val selectedAsset by viewModel.selectedAsset.collectAsStateWithLifecycle()
    val cloudConfig by viewModel.cloudConfig.collectAsStateWithLifecycle()
    
    // UI selection values
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedMediaType by viewModel.selectedMediaType.collectAsStateWithLifecycle()
    val selectedConnectionId by viewModel.selectedConnectionId.collectAsStateWithLifecycle()

    val selectedTags by viewModel.selectedTags.collectAsStateWithLifecycle()
    val selectedLocations by viewModel.selectedLocations.collectAsStateWithLifecycle()
    val selectedDatePreset by viewModel.selectedDatePreset.collectAsStateWithLifecycle()
    val selectedExtensions by viewModel.selectedExtensions.collectAsStateWithLifecycle()

    val availableTags by viewModel.availableTags.collectAsStateWithLifecycle()
    val availableLocations by viewModel.availableLocations.collectAsStateWithLifecycle()
    val availableExtensions by viewModel.availableExtensions.collectAsStateWithLifecycle()

    val searchSuggestions by viewModel.searchSuggestions.collectAsStateWithLifecycle()
    
    // Live cloud telemetry
    val syncProgress by viewModel.syncProgress.collectAsStateWithLifecycle()
    val syncFileName by viewModel.syncFileName.collectAsStateWithLifecycle()
    val syncLogs by viewModel.syncLogs.collectAsStateWithLifecycle()

    // ViewModel property state collection for bottom drawer suggestions
    val isAiLoading by viewModel.isAiLoading.collectAsStateWithLifecycle()
    val aiMessage by viewModel.aiRecommendationMessage.collectAsStateWithLifecycle()

    // OpenClaw states
    val openClawGatewayUrl by viewModel.openClawGatewayUrl.collectAsStateWithLifecycle()
    val openClawNodeName by viewModel.openClawNodeName.collectAsStateWithLifecycle()
    val openClawNodeUuid by viewModel.openClawNodeUuid.collectAsStateWithLifecycle()
    val openClawStatus by viewModel.openClawStatus.collectAsStateWithLifecycle()
    val isOpenClawActiveSearch by viewModel.isOpenClawActiveSearch.collectAsStateWithLifecycle()
    val openClawTagsCount by viewModel.openClawTagsCount.collectAsStateWithLifecycle()
    val openClawClusterNodes by viewModel.openClawClusterNodes.collectAsStateWithLifecycle()

    // Dialog sheets state
    var showAddShareDialog by remember { mutableStateOf(false) }
    var showCloudDialog by remember { mutableStateOf(false) }
    var showTelemetryConsole by remember { mutableStateOf(false) }
    var showOpenClawDialog by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = DeepCharcoal
    ) {
        Scaffold(
            topBar = {
                // Main Header featuring real-time cloud sync telemetry indicator
                TopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Image(
                                painter = painterResource(id = com.example.R.drawable.img_app_logo),
                                contentDescription = "Logo",
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(6.dp))
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "NetGallery",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 22.sp,
                                    letterSpacing = 0.5.sp
                                ),
                                color = Color.White
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = CarbonSlate,
                        titleContentColor = Color.White,
                        actionIconContentColor = Color.White
                    ),
                    actions = {
                        // Sync telemetry progress icon
                        if (syncProgress != null) {
                            Column(
                                horizontalAlignment = Alignment.End,
                                modifier = Modifier
                                    .padding(end = 12.dp)
                                    .clickable { showTelemetryConsole = true }
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    CircularProgressIndicator(
                                        progress = { syncProgress ?: 0.0f },
                                        modifier = Modifier.size(20.dp),
                                        color = EmeraldSync,
                                        strokeWidth = 3.dp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "${(syncProgress!! * 100).toInt()}%",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = EmeraldSync,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Text(
                                    text = "Sincronizando...",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = LightTextMuted
                                )
                            }
                        } else {
                            IconButton(
                                onClick = { showTelemetryConsole = true },
                                modifier = Modifier.testTag("telemetry_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Send, // Standard Core Send
                                    contentDescription = "Sync Logs",
                                    tint = if (cloudConfig?.isAutoSyncEnabled == true) EmeraldSync else LightTextMuted
                                )
                            }
                        }

                        // OpenClaw Node Setup Access
                        IconButton(
                            onClick = { showOpenClawDialog = true },
                            modifier = Modifier.testTag("openclaw_settings_button")
                        ) {
                            val clawColor = when (openClawStatus) {
                                "CONNECTED" -> EmeraldSync
                                "CONNECTING" -> PhotoAmber
                                "SYNC_INDEX" -> OceanBlue
                                else -> LightTextMuted
                            }
                            Icon(
                                imageVector = Icons.Default.Build, // Represent node link hub
                                contentDescription = "OpenClaw Node",
                                tint = clawColor
                            )
                        }

                        // Cloud Setup Access
                        IconButton(
                            onClick = { showCloudDialog = true },
                            modifier = Modifier.testTag("cloud_settings_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share, // Standard Core Share
                                contentDescription = "Cloud Config",
                                tint = PhotoAmber
                            )
                        }
                    }
                )
            },
            containerColor = DeepCharcoal
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // 1. Connection Header & Mounting points
                NetworkConnectionsSection(
                    connections = connections,
                    selectedConnectionId = selectedConnectionId,
                    onSelectConnection = { id -> viewModel.selectConnection(id) },
                    onAddShareClicked = { showAddShareDialog = true },
                    onDeleteConnection = { conn -> viewModel.removeNetworkShare(conn) }
                )

                // 2. Search row and media filters
                FilterAndSearchSection(
                    searchQuery = searchQuery,
                    onSearchQueryChanged = { valStr -> viewModel.setSearchQuery(valStr) },
                    selectedMediaType = selectedMediaType,
                    onMediaTypeSelected = { mType -> viewModel.setMediaTypeFilter(mType) },
                    selectedTags = selectedTags,
                    selectedLocations = selectedLocations,
                    selectedDatePreset = selectedDatePreset,
                    selectedExtensions = selectedExtensions,
                    availableTags = availableTags,
                    availableLocations = availableLocations,
                    availableExtensions = availableExtensions,
                    searchSuggestions = searchSuggestions,
                    onToggleTag = { tag -> viewModel.toggleTagFilter(tag) },
                    onToggleLocation = { loc -> viewModel.toggleLocationFilter(loc) },
                    onToggleExtension = { ext -> viewModel.toggleExtensionFilter(ext) },
                    onSetDatePreset = { preset -> viewModel.setDatePresetFilter(preset) },
                    onClearAllFilters = { viewModel.clearAllFilters() }
                )

                // 3. Main catalog layout
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    if (filteredAssets.isEmpty()) {
                        EmptyStatePanel(
                            hasFilters = searchQuery.isNotEmpty() || selectedMediaType != "ALL" || selectedConnectionId != null || selectedTags.isNotEmpty() || selectedLocations.isNotEmpty() || selectedExtensions.isNotEmpty() || selectedDatePreset != "ALL"
                        )
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Adaptive(minSize = 160.dp),
                            modifier = Modifier
                                .fillMaxSize()
                                .testTag("media_grid"),
                            contentPadding = PaddingValues(vertical = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(filteredAssets, key = { it.id }) { asset ->
                                MediaAssetCard(
                                    asset = asset,
                                    isSelected = selectedAsset?.id == asset.id,
                                    onClick = { viewModel.selectAsset(asset.id) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // 4. Slide-Up Metadata bottom drawer
    selectedAsset?.let { asset ->
        ModalBottomSheet(
            onDismissRequest = { viewModel.selectAsset(null) },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = CarbonSlate
        ) {
            MetadataDetailsDrawer(
                asset = asset,
                isAiLoading = isAiLoading,
                aiMessage = aiMessage,
                onClose = { viewModel.selectAsset(null) },
                onSave = { tags, loc, rating ->
                    viewModel.updateMetadata(asset.id, tags, loc, rating)
                },
                onRequestAiSuggestions = { viewModel.requestAiMetadataSuggestions(asset) }
            )
        }
    }

    // 5. Connection Creator Dialog
    if (showAddShareDialog) {
        AddShareDialog(
            onDismiss = { showAddShareDialog = false },
            onSubmit = { label, type, serverUrl, username, password ->
                viewModel.addNetworkShare(label, type, serverUrl, username, password)
                showAddShareDialog = false
            }
        )
    }

    // 6. Cloud Config Dialog
    if (showCloudDialog) {
        CloudConfigDialog(
            currentConfig = cloudConfig,
            onDismiss = { showCloudDialog = false },
            onSubmit = { config ->
                viewModel.saveCloudSetup(config)
                showCloudDialog = false
            }
        )
    }

    // 7. Telemetry & Console logs console
    if (showTelemetryConsole) {
        TelemetryConsoleDialog(
            logs = syncLogs,
            isSyncing = syncProgress != null,
            progress = syncProgress,
            activeFile = syncFileName,
            onDismiss = { showTelemetryConsole = false },
            onStartSync = { viewModel.triggerMassiveCloudSync() },
            onClear = { viewModel.clearLogs() }
        )
    }

    // 8. OpenClaw Distributed Gateway Dialog
    if (showOpenClawDialog) {
        OpenClawDialog(
            status = openClawStatus,
            url = openClawGatewayUrl,
            name = openClawNodeName,
            uuid = openClawNodeUuid,
            activeSearch = isOpenClawActiveSearch,
            indexedTags = openClawTagsCount,
            clusterNodes = openClawClusterNodes,
            onDismiss = { showOpenClawDialog = false },
            onConnect = { urlConfig, labelConfig -> viewModel.connectToOpenClaw(urlConfig, labelConfig) },
            onDisconnect = { viewModel.disconnectFromOpenClaw() },
            onSync = { viewModel.syncOpenClawTags() },
            onToggleSearch = { enabled -> viewModel.toggleOpenClawSearch(enabled) }
        )
    }
}

// -------------------------------------------------------------
// CHILD UI COMPONENTS
// -------------------------------------------------------------

@Composable
fun NetworkConnectionsSection(
    connections: List<NetworkConnection>,
    selectedConnectionId: Int?,
    onSelectConnection: (Int?) -> Unit,
    onAddShareClicked: () -> Unit,
    onDeleteConnection: (NetworkConnection) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(CarbonSlate)
            .padding(vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "ALMACENAMIENTOS DE RED (NAS)",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                ),
                color = PhotoAmber
            )

            // Inline "+ Conectar" trigger
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .clickable { onAddShareClicked() }
                    .background(DarkCardBg)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                    .testTag("add_nas_button"),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Agregar Share",
                    tint = PhotoAmber,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Añadir",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // "Todos" selection button
            item {
                FilterChip(
                    selected = selectedConnectionId == null,
                    onClick = { onSelectConnection(null) },
                    label = { Text("Toda la Red") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = PhotoAmber,
                        selectedLabelColor = DeepCharcoal,
                        containerColor = DarkCardBg,
                        labelColor = Color.White
                    ),
                    border = null
                )
            }

            items(connections, key = { "conn-${it.id}" }) { conn ->
                val isSelected = selectedConnectionId == conn.id
                
                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onSelectConnection(conn.id) }
                        .border(
                            width = 1.dp,
                            color = if (isSelected) PhotoAmber else Color.Transparent,
                            shape = RoundedCornerShape(8.dp)
                        ),
                    color = if (isSelected) DarkCardBg else DarkCardBg.copy(alpha = 0.6f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Home, // Core Home Icon
                            contentDescription = "Folder Network",
                            tint = if (isSelected) PhotoAmber else OceanBlue,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = conn.label,
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                            Text(
                                text = conn.serverUrl,
                                style = MaterialTheme.typography.labelSmall,
                                color = LightTextMuted,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.widthIn(max = 140.dp)
                            )
                        }

                        // Unmount Network option
                        if (conn.label != NetworkShareManager.DEFAULT_SHARE_LABEL) {
                            Spacer(modifier = Modifier.width(10.dp))
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Desconectar",
                                tint = ErrorCrimson.copy(alpha = 0.7f),
                                modifier = Modifier
                                    .size(16.dp)
                                    .clickable { onDeleteConnection(conn) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FilterAndSearchSection(
    searchQuery: String,
    onSearchQueryChanged: (String) -> Unit,
    selectedMediaType: String,
    onMediaTypeSelected: (String) -> Unit,
    selectedTags: Set<String>,
    selectedLocations: Set<String>,
    selectedDatePreset: String,
    selectedExtensions: Set<String>,
    availableTags: List<String>,
    availableLocations: List<String>,
    availableExtensions: List<String>,
    searchSuggestions: List<SearchSuggestion>,
    onToggleTag: (String) -> Unit,
    onToggleLocation: (String) -> Unit,
    onToggleExtension: (String) -> Unit,
    onSetDatePreset: (String) -> Unit,
    onClearAllFilters: () -> Unit
) {
    var showAdvancedPanel by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // Search Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp)
            ) {
                TextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChanged,
                    placeholder = { Text("Buscar por etiquetas, ubicación, archivo...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("search_field"),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Buscar",
                            tint = LightTextMuted
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { onSearchQueryChanged("") }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear",
                                    tint = LightTextMuted
                                )
                            }
                        }
                    },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = CarbonSlate,
                        unfocusedContainerColor = CarbonSlate,
                        disabledContainerColor = CarbonSlate,
                        focusedIndicatorColor = PhotoAmber,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedPlaceholderColor = LightTextMuted,
                        unfocusedPlaceholderColor = LightTextMuted
                    ),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Expand Advanced search options button
            IconButton(
                onClick = { showAdvancedPanel = !showAdvancedPanel },
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (showAdvancedPanel) PhotoAmber else CarbonSlate)
                    .testTag("advanced_search_toggle_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Opciones avanzadas",
                    tint = if (showAdvancedPanel) DeepCharcoal else LightTextMuted
                )
            }
        }

        // Suggestions Dropdown Panel
        AnimatedVisibility(
            visible = searchSuggestions.isNotEmpty(),
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkCardBg),
                border = BorderStroke(1.dp, CarbonSlate),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .testTag("suggestions_card"),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column {
                    Text(
                        text = "SUGERENCIAS DE BÚSQUEDA",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        ),
                        color = PhotoAmber,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                    searchSuggestions.forEach { suggestion ->
                        val (icon, typeText, action) = when (suggestion.type) {
                            "tag" -> Triple(Icons.Filled.List, "Etiqueta", {
                                onToggleTag(suggestion.text)
                                onSearchQueryChanged("") // auto clear
                            })
                            "location" -> Triple(Icons.Filled.LocationOn, "Ubicación", {
                                onToggleLocation(suggestion.text)
                                onSearchQueryChanged("") // auto clear
                            })
                            "cluster_tag" -> Triple(Icons.Filled.Share, "Clúster Claw", {
                                onToggleTag(suggestion.text)
                                onSearchQueryChanged("") // auto clear
                            })
                            else -> Triple(Icons.Filled.Home, "Archivo", {
                                onSearchQueryChanged(suggestion.text)
                            })
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { action() }
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = if (suggestion.isClawOptimized) PhotoAmber else LightTextMuted,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = suggestion.text,
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (suggestion.isClawOptimized) PhotoAmber else Color.White,
                                modifier = Modifier.weight(1f)
                            )
                            if (suggestion.isClawOptimized) {
                                Box(
                                    modifier = Modifier
                                        .padding(end = 8.dp)
                                        .background(PhotoAmber.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "OpenClaw",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 9.sp,
                                            color = PhotoAmber
                                        )
                                    )
                                }
                            }
                            Text(
                                text = typeText,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (suggestion.isClawOptimized) PhotoAmber else LightTextMuted
                            )
                        }
                        Divider(color = CarbonSlate)
                    }
                }
            }
        }

        // Expanded Advanced Search Parameters Drawer Panel
        AnimatedVisibility(
            visible = showAdvancedPanel,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = CarbonSlate),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .testTag("advanced_filter_box"),
                shape = RoundedCornerShape(10.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "FILTRADO SELECTIVO MULTI-CRITERIO",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = PhotoAmber
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    // 1. Files extension / formats list
                    Text(
                        text = "Formatos de Archivo:",
                        style = MaterialTheme.typography.labelSmall,
                        color = LightTextMuted
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(availableExtensions) { ext ->
                            val isSelected = selectedExtensions.contains(ext)
                            FilterChip(
                                selected = isSelected,
                                onClick = { onToggleExtension(ext) },
                                label = { Text(ext) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = OceanBlue,
                                    selectedLabelColor = Color.White,
                                    containerColor = DarkCardBg,
                                    labelColor = LightTextMuted
                                ),
                                border = null
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // 2. Date presets
                    Text(
                        text = "Filtro Temporal Original:",
                        style = MaterialTheme.typography.labelSmall,
                        color = LightTextMuted
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val dates = listOf(
                            Pair("ALL", "Todos"),
                            Pair("TODAY", "Hoy"),
                            Pair("WEEK", "Semana"),
                            Pair("MONTH", "Mes"),
                            Pair("YEAR", "Año")
                        )
                        dates.forEach { (preset, label) ->
                            val isSelected = selectedDatePreset == preset
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(6.dp))
                                    .clickable { onSetDatePreset(preset) }
                                    .background(if (isSelected) PhotoAmber else DarkCardBg)
                                    .padding(vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) DeepCharcoal else Color.White
                                    )
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // 3. Available Tags Row list
                    if (availableTags.isNotEmpty()) {
                        Text(
                            text = "Añadir Etiquetas en Filtro:",
                            style = MaterialTheme.typography.labelSmall,
                            color = LightTextMuted
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(availableTags) { tag ->
                                val isSelected = selectedTags.contains(tag)
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { onToggleTag(tag) },
                                    label = { Text(tag) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = PhotoAmber,
                                        selectedLabelColor = DeepCharcoal,
                                        containerColor = DarkCardBg,
                                        labelColor = Color.White
                                    ),
                                    border = null
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    // 4. Available Locations list
                    if (availableLocations.isNotEmpty()) {
                        Text(
                            text = "Ubicaciones Almacenadas:",
                            style = MaterialTheme.typography.labelSmall,
                            color = LightTextMuted
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(availableLocations) { loc ->
                                val isSelected = selectedLocations.contains(loc)
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { onToggleLocation(loc) },
                                    label = { Text(loc) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = PhotoAmber,
                                        selectedLabelColor = DeepCharcoal,
                                        containerColor = DarkCardBg,
                                        labelColor = Color.White
                                    ),
                                    border = null
                                )
                            }
                        }
                    }
                }
            }
        }

        // Active filters row display with dynamic dismissal capability
        val hasActiveFilters = searchQuery.isNotEmpty() || selectedTags.isNotEmpty() || selectedLocations.isNotEmpty() || selectedExtensions.isNotEmpty() || selectedDatePreset != "ALL" || selectedMediaType != "ALL"

        if (hasActiveFilters) {
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Clear all chip
                InputChip(
                    selected = false,
                    onClick = onClearAllFilters,
                    label = { Text("Borrar Filtros x") },
                    colors = InputChipDefaults.inputChipColors(
                        containerColor = ErrorCrimson.copy(alpha = 0.2f),
                        labelColor = ErrorCrimson
                    ),
                    modifier = Modifier.testTag("clear_all_filters_btn")
                )

                if (selectedMediaType != "ALL") {
                    InputChip(
                        selected = false,
                        onClick = { onMediaTypeSelected("ALL") },
                        label = { Text("Categoría: $selectedMediaType") },
                        trailingIcon = { Icon(Icons.Default.Clear, "remove", modifier = Modifier.size(10.dp)) },
                        colors = InputChipDefaults.inputChipColors(containerColor = DarkCardBg, labelColor = Color.White)
                    )
                }

                if (selectedDatePreset != "ALL") {
                    InputChip(
                        selected = false,
                        onClick = { onSetDatePreset("ALL") },
                        label = { Text("Modificado: $selectedDatePreset") },
                        trailingIcon = { Icon(Icons.Default.Clear, "remove", modifier = Modifier.size(10.dp)) },
                        colors = InputChipDefaults.inputChipColors(containerColor = DarkCardBg, labelColor = Color.White)
                    )
                }

                selectedTags.forEach { t ->
                    InputChip(
                        selected = false,
                        onClick = { onToggleTag(t) },
                        label = { Text("#$t") },
                        trailingIcon = { Icon(Icons.Default.Clear, "remove", modifier = Modifier.size(10.dp)) },
                        colors = InputChipDefaults.inputChipColors(containerColor = DarkCardBg, labelColor = Color.White)
                    )
                }

                selectedLocations.forEach { loc ->
                    InputChip(
                        selected = false,
                        onClick = { onToggleLocation(loc) },
                        label = { Text(loc) },
                        leadingIcon = { Icon(Icons.Default.LocationOn, "Ubicación", modifier = Modifier.size(10.dp), tint = PhotoAmber) },
                        trailingIcon = { Icon(Icons.Default.Clear, "remove", modifier = Modifier.size(10.dp)) },
                        colors = InputChipDefaults.inputChipColors(containerColor = DarkCardBg, labelColor = Color.White)
                    )
                }

                selectedExtensions.forEach { ext ->
                    InputChip(
                        selected = false,
                        onClick = { onToggleExtension(ext) },
                        label = { Text(ext) },
                        trailingIcon = { Icon(Icons.Default.Clear, "remove", modifier = Modifier.size(10.dp)) },
                        colors = InputChipDefaults.inputChipColors(containerColor = DarkCardBg, labelColor = Color.White)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Format selector category chips (General Filters)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val filters = listOf(
                MediaTypeFilterItem("ALL", "Todos", Icons.Default.Home),
                MediaTypeFilterItem("RAW", "RAW", Icons.Default.Done),
                MediaTypeFilterItem("VIDEO", "Video", Icons.Default.PlayArrow)
            )

            filters.forEach { filterItem ->
                val isSelected = selectedMediaType == filterItem.id
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onMediaTypeSelected(filterItem.id) }
                        .background(if (isSelected) PhotoAmber else CarbonSlate)
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = filterItem.icon,
                        contentDescription = filterItem.label,
                        tint = if (isSelected) DeepCharcoal else LightTextMuted,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = filterItem.label,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) DeepCharcoal else Color.White
                        )
                    )
                }
            }
        }
    }
}

data class MediaTypeFilterItem(val id: String, val label: String, val icon: ImageVector)

@Composable
fun MediaAssetCard(
    asset: MediaAsset,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .border(
                width = 2.dp,
                color = if (isSelected) PhotoAmber else Color.Transparent,
                shape = RoundedCornerShape(12.dp)
            )
            .testTag("media_card_${asset.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CarbonSlate)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
            ) {
                LandscapeCanvas(
                    fileName = asset.fileName,
                    ext = asset.fileExtension,
                    modifier = Modifier.fillMaxSize()
                )

                val isVideo = asset.fileExtension.lowercase() in listOf("mp4", "mkv", "mov")
                Box(
                    modifier = Modifier
                        .padding(8.dp)
                        .align(Alignment.TopStart)
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (isVideo) OceanBlue else PhotoAmber)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = if (isVideo) "VIDEO" else "RAW",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Black,
                            fontSize = 8.sp,
                            color = if (isVideo) Color.White else DeepCharcoal
                        )
                    )
                }

                // Star Rating Overlay
                if (asset.rating > 0) {
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color.Black.copy(alpha = 0.6f))
                            .padding(horizontal = 4.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Rating",
                            tint = PhotoAmber,
                            modifier = Modifier.size(10.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = asset.rating.toString(),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.sp
                            ),
                            color = Color.White
                        )
                    }
                }
            }

            // Metatags and Sync State block
            Column(
                modifier = Modifier.padding(10.dp)
            ) {
                Text(
                    text = asset.fileName,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Text(
                    text = asset.locationName.ifEmpty { "Sin Ubicación" },
                    style = MaterialTheme.typography.labelSmall,
                    color = LightTextMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(6.dp))

                // XMP Status & Cloud Status badges
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // XMP sidecar sync state
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val color = if (asset.xmpSyncStatus == "SYNCED") EmeraldSync else PhotoAmber
                        Canvas(modifier = Modifier.size(6.dp)) {
                            drawCircle(color = color)
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "XMP",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                            color = color
                        )
                    }

                    // Cloud sync status
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val (color, text) = when (asset.cloudSyncStatus) {
                            "SYNCED" -> Pair(EmeraldSync, "NUBE")
                            "PENDING" -> Pair(PhotoAmber, "PEND")
                            else -> Pair(LightTextMuted, "LOCAL")
                        }
                        Canvas(modifier = Modifier.size(6.dp)) {
                            drawCircle(color = color)
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = text,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                            color = color
                        )
                    }
                }
            }
        }
    }
}

/**
 * Custom photographic layouts canvas generator
 */
@Composable
fun LandscapeCanvas(fileName: String, ext: String, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val name = fileName.lowercase()
        
        val gradient = when {
            name.contains("uyuni") -> Brush.linearGradient(
                colors = listOf(Color(0xFF8E44AD), Color(0xFF3498DB), Color(0xFF2980B9))
            )
            name.contains("volcano") || name.contains("andes") -> Brush.linearGradient(
                colors = listOf(Color(0xFFD35400), Color(0xFFE67E22), Color(0xFF2C3E50))
            )
            name.contains("tokyo") || name.contains("neon") -> Brush.linearGradient(
                colors = listOf(Color(0xFF2C3E50), Color(0xFF9B59B6), Color(0xFFD81B60))
            )
            name.contains("iceland") || name.contains("waterfall") -> Brush.linearGradient(
                colors = listOf(Color(0xFF1B4F72), Color(0xFF2E86C1), Color(0xFF76D7C4))
            )
            name.contains("patagonia") || name.contains("peaks") -> Brush.linearGradient(
                colors = listOf(Color(0xFF2C3E50), Color(0xFF34495E), Color(0xFF85929E))
            )
            else -> Brush.sweepGradient(
                colors = listOf(Color(0xFF1ABC9C), Color(0xFF16A085), Color(0xFF2C3E50))
            )
        }

        drawRect(brush = gradient)

        val path = Path()
        if (name.contains("volcano") || name.contains("andes") || name.contains("peaks") || name.contains("patagonia")) {
            path.moveTo(0f, height)
            path.lineTo(width * 0.3f, height * 0.45f)
            path.lineTo(width * 0.5f, height * 0.65f)
            path.lineTo(width * 0.75f, height * 0.35f)
            path.lineTo(width, height)
            path.close()
            drawPath(path = path, color = Color(0xFF212529).copy(alpha = 0.85f))

            val snow = Path()
            snow.moveTo(width * 0.75f, height * 0.35f)
            snow.lineTo(width * 0.7f, height * 0.45f)
            snow.lineTo(width * 0.8f, height * 0.45f)
            snow.close()
            drawPath(path = snow, color = Color.White.copy(alpha = 0.9f))
        } else if (name.contains("uyuni") || name.contains("reflex")) {
            val refLine = height * 0.65f
            drawLine(
                color = Color.White.copy(alpha = 0.6f),
                start = Offset(0f, refLine),
                end = Offset(width, refLine),
                strokeWidth = 3f
            )
            drawCircle(
                color = Color(0xFFF1C40F).copy(alpha = 0.3f),
                radius = 35f,
                center = Offset(width * 0.5f, refLine + 20f)
            )
            drawCircle(
                color = Color(0xFFF1C40F).copy(alpha = 0.7f),
                radius = 35f,
                center = Offset(width * 0.5f, refLine - 20f)
            )
        } else if (name.contains("tokyo") || name.contains("neon")) {
            drawRect(
                color = Color(0xFF111215),
                topLeft = Offset(width * 0.1f, height * 0.25f),
                size = androidx.compose.ui.geometry.Size(width * 0.25f, height * 0.75f)
            )
            drawRect(
                color = Color(0xFF16181D),
                topLeft = Offset(width * 0.5f, height * 0.35f),
                size = androidx.compose.ui.geometry.Size(width * 0.35f, height * 0.65f)
            )
            drawCircle(color = Color(0xFFD81B60), radius = 6f, center = Offset(width * 0.2f, height * 0.5f))
            drawCircle(color = Color(0xFF00FFCC), radius = 6f, center = Offset(width * 0.7f, height * 0.6f))
        } else if (name.contains("iceland") || name.contains("waterfall") || name.contains("aerial")) {
            val water = Path()
            water.moveTo(width * 0.4f, height * 0.1f)
            water.lineTo(width * 0.6f, height * 0.1f)
            water.lineTo(width * 0.65f, height)
            water.lineTo(width * 0.35f, height)
            water.close()
            drawPath(path = water, color = Color(0xFFAED6F1).copy(alpha = 0.8f))
        }

        val hudColor = Color.White.copy(alpha = 0.4f)
        val len = 15f
        drawLine(hudColor, Offset(10f, 10f), Offset(10f + len, 10f), 3f)
        drawLine(hudColor, Offset(10f, 10f), Offset(10f, 10f + len), 3f)
        drawLine(hudColor, Offset(width - 10f, height - 10f), Offset(width - 10f - len, height - 10f), 3f)
        drawLine(hudColor, Offset(width - 10f, height - 10f), Offset(width - 10f, height - 10f - len), 3f)
    }
}

@Composable
fun EmptyStatePanel(hasFilters: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = if (hasFilters) Icons.Default.Warning else Icons.Default.List, // Standard core icons
            contentDescription = "No images",
            tint = LightTextMuted,
            modifier = Modifier.size(72.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = if (hasFilters) "Sin resultados coincidentes" else "No hay contenidos en red",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = Color.White,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = if (hasFilters) 
                "Prueba ajustando tus parámetros de búsqueda, quitando filtros de RAW/Video, o seleccionando otra carpeta de red." 
                else "Conecta un servidor NAS o WebDAV para escanear y catalogar automáticamente imágenes RAW y videos con sidecars XMP.",
            style = MaterialTheme.typography.bodyMedium,
            color = LightTextMuted,
            textAlign = TextAlign.Center
        )
    }
}

// -------------------------------------------------------------
// METADATA DETAILS & XMP CODES DRAWER
// -------------------------------------------------------------

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MetadataDetailsDrawer(
    asset: MediaAsset,
    isAiLoading: Boolean,
    aiMessage: String?,
    onClose: () -> Unit,
    onSave: (List<String>, String, Int) -> Unit,
    onRequestAiSuggestions: () -> Unit
) {
    val context = LocalContext.current
    
    var inputTags by remember(asset) { mutableStateOf(asset.tags) }
    var inputLocation by remember(asset) { mutableStateOf(asset.locationName) }
    var inputRating by remember(asset) { mutableStateOf(asset.rating) }

    val tagsList = remember(inputTags) {
        inputTags.split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 10.dp)
            .navigationBarsPadding(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Metadatos y Sidecar XMP",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                    Text(
                        text = "Escribiendo en: ${asset.fileName}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = PhotoAmber
                    )
                }
                
                IconButton(onClick = onClose) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                }
            }
        }

        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .clip(RoundedCornerShape(8.dp))
            ) {
                LandscapeCanvas(fileName = asset.fileName, ext = asset.fileExtension, modifier = Modifier.fillMaxSize())
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkCardBg),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "EXIF ORIGINAL (CÁMARA)",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = PhotoAmber
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        ExifStatCol("Modelo", asset.cameraModel)
                        ExifStatCol("Apertura", asset.exifAperture)
                        ExifStatCol("Velocidad", asset.exifShutter)
                        ExifStatCol("ISO", asset.exifIso.toString())
                    }
                    
                    Divider(modifier = Modifier.padding(vertical = 10.dp), color = CarbonSlate)
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        ExifStatCol("Resolución", asset.dimensions)
                        ExifStatCol("Formato", asset.fileExtension)
                        val sizeFormatted = Formatter.formatShortFileSize(context, asset.fileSizeBytes)
                        ExifStatCol("Peso", sizeFormatted)
                        
                        val dateString = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                            .format(Date(asset.dateTimeOriginal))
                        ExifStatCol("Fecha", dateString)
                    }
                }
            }
        }

        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "VALORACIÓN FOTOGRÁFICA",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = LightTextMuted
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (starIndex in 1..5) {
                        val isLit = starIndex <= inputRating
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Star $starIndex",
                            tint = if (isLit) PhotoAmber else LightTextMuted.copy(alpha = 0.4f),
                            modifier = Modifier
                                .size(36.dp)
                                .clickable { inputRating = starIndex }
                                .testTag("rate_star_$starIndex")
                        )
                    }
                    if (inputRating > 0) {
                        IconButton(onClick = { inputRating = 0 }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear",
                                tint = ErrorCrimson
                            )
                        }
                    }
                }
            }
        }

        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "UBICACIÓN GEOGRÁFICA",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = LightTextMuted
                )
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = inputLocation,
                    onValueChange = { inputLocation = it },
                    modifier = Modifier.fillMaxWidth().testTag("location_input"),
                    placeholder = { Text("Ej. Salar de Uyuni, Bolivia") },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = DarkCardBg,
                        unfocusedContainerColor = DarkCardBg,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedIndicatorColor = PhotoAmber
                    ),
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true
                )
            }
        }

        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "ETIQUETAS ORGANIZATIVAS",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = LightTextMuted
                    )

                    Button(
                        onClick = onRequestAiSuggestions,
                        enabled = !isAiLoading,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PhotoAmber.copy(alpha = 0.2f),
                            contentColor = PhotoAmber
                        ),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                        modifier = Modifier.height(28.dp).testTag("gemini_suggest_button")
                    ) {
                        if (isAiLoading) {
                            CircularProgressIndicator(color = PhotoAmber, modifier = Modifier.size(14.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(imageVector = Icons.Default.Build, contentDescription = "IA", modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Sugerir con Gemini", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                        }
                    }
                }

                aiMessage?.let { msg ->
                    Spacer(modifier = Modifier.height(6.dp))
                    Surface(
                        color = OceanBlue.copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, OceanBlue.copy(alpha = 0.3f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .clickable {
                                val extracted = msg.substringAfter("IA recomendó: ").trim()
                                if (extracted.isNotEmpty() && extracted != "Fallo de respuesta IA. Fallback local generado.") {
                                    val current = inputTags.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                                    val added = extracted.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                                    inputTags = (current + added).distinct().joinToString(", ")
                                }
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(imageVector = Icons.Default.Info, contentDescription = "", tint = OceanBlue, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "$msg (Toca para autocompletar)",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (tagsList.isNotEmpty()) {
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        tagsList.forEach { tag ->
                            InputChip(
                                selected = false,
                                onClick = {},
                                label = { Text(tag) },
                                trailingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Remove",
                                        tint = Color.White,
                                        modifier = Modifier
                                            .size(12.dp)
                                            .clickable {
                                                val remains = tagsList - tag
                                                inputTags = remains.joinToString(", ")
                                            }
                                    )
                                },
                                colors = InputChipDefaults.inputChipColors(
                                    containerColor = CarbonSlate,
                                    labelColor = Color.White
                                )
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }

                TextField(
                    value = inputTags,
                    onValueChange = { inputTags = it },
                    placeholder = { Text("Escribe etiquetas separadas por comas (ejemp: frio, nieve)") },
                    modifier = Modifier.fillMaxWidth().testTag("tags_input"),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = DarkCardBg,
                        unfocusedContainerColor = DarkCardBg,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedIndicatorColor = PhotoAmber
                    ),
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true
                )
            }
        }

        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "VISTA PREVIA DE ARCHIVO SIDECAR (.XMP)",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = LightTextMuted
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.CheckCircle, contentDescription = "", tint = EmeraldSync, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Adobe Compatible XML", style = MaterialTheme.typography.labelSmall, color = EmeraldSync)
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                
                val simulatedXmp = NetworkShareManager.generateXmpContent(
                    asset.fileName,
                    tagsList,
                    inputLocation,
                    inputRating
                )

                Surface(
                    color = CodeGrey,
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(1.dp, CarbonSlate),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = simulatedXmp,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = EmeraldSync.copy(alpha = 0.85f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        maxLines = 8,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        item {
            Button(
                onClick = {
                    onSave(tagsList, inputLocation, inputRating)
                    onClose()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = PhotoAmber,
                    contentColor = DeepCharcoal
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("save_metadata_button")
            ) {
                Icon(imageVector = Icons.Default.Check, contentDescription = "Guardar", tint = DeepCharcoal)
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Guardar Sidecar XMP en Servidor",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun ExifStatCol(label: String, value: String) {
    Column {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = LightTextMuted)
        Text(text = value, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = Color.White)
    }
}

// -------------------------------------------------------------
// SERVERS & NETWORK MOUNTS CONTROLLER DIALOG
// -------------------------------------------------------------

@Composable
fun AddShareDialog(
    onDismiss: () -> Unit,
    onSubmit: (String, String, String, String, String) -> Unit
) {
    var label by remember { mutableStateOf("") }
    var shareType by remember { mutableStateOf("SMB (Samba/Compartido)") }
    var serverUrl by remember { mutableStateOf("smb://192.168.1.") }
    var user by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = CarbonSlate),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Conectar Carpeta de Red",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )

                TextField(
                    value = label,
                    onValueChange = { label = it },
                    label = { Text("Nombre de la Carpeta") },
                    placeholder = { Text("E.g., NAS-Estudio-Respaldo") },
                    colors = TextFieldDefaults.colors(focusedContainerColor = DarkCardBg, unfocusedContainerColor = DarkCardBg, focusedLabelColor = PhotoAmber),
                    modifier = Modifier.fillMaxWidth().testTag("add_share_label")
                )

                Column {
                    Text(text = "Protocolo de Red", style = MaterialTheme.typography.labelSmall, color = LightTextMuted)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        listOf("SMB (NAS)", "WebDAV").forEach { opt ->
                            val isSelected = shareType.startsWith(opt)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .clickable { 
                                        shareType = "$opt (Red)"
                                        serverUrl = if (opt == "WebDAV") "http://192.168.1.5:80/webdav" else "smb://192.168.1.100/StudioShare"
                                    }
                                    .background(if (isSelected) PhotoAmber else DarkCardBg)
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = opt,
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                    color = if (isSelected) DeepCharcoal else Color.White
                                )
                            }
                        }
                    }
                }

                TextField(
                    value = serverUrl,
                    onValueChange = { serverUrl = it },
                    label = { Text("Ruta / Endpoint Servidor") },
                    colors = TextFieldDefaults.colors(focusedContainerColor = DarkCardBg, unfocusedContainerColor = DarkCardBg, focusedLabelColor = PhotoAmber),
                    modifier = Modifier.fillMaxWidth().testTag("add_share_url")
                )

                TextField(
                    value = user,
                    onValueChange = { user = it },
                    label = { Text("Usuario Acceso") },
                    colors = TextFieldDefaults.colors(focusedContainerColor = DarkCardBg, unfocusedContainerColor = DarkCardBg, focusedLabelColor = PhotoAmber),
                    modifier = Modifier.fillMaxWidth().testTag("add_share_user")
                )

                TextField(
                    value = pass,
                    onValueChange = { pass = it },
                    label = { Text("Contraseña") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    colors = TextFieldDefaults.colors(focusedContainerColor = DarkCardBg, unfocusedContainerColor = DarkCardBg, focusedLabelColor = PhotoAmber),
                    modifier = Modifier.fillMaxWidth().testTag("add_share_pass")
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancelar", color = LightTextMuted)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (label.isNotEmpty() && serverUrl.isNotEmpty()) {
                                onSubmit(label, shareType, serverUrl, user, pass)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PhotoAmber, contentColor = DeepCharcoal),
                        modifier = Modifier.testTag("submit_share_button")
                    ) {
                        Text("Conectar", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// CLOUD BACKUPS SETUP DIALOG
// -------------------------------------------------------------

@Composable
fun CloudConfigDialog(
    currentConfig: CloudSyncConfig?,
    onDismiss: () -> Unit,
    onSubmit: (CloudSyncConfig) -> Unit
) {
    var epUrl by remember { mutableStateOf(currentConfig?.endpointUrl ?: "https://nextcloud.corp.net/remote.php/webdav/photos") }
    var user by remember { mutableStateOf(currentConfig?.username ?: "") }
    var pass by remember { mutableStateOf(currentConfig?.password ?: "") }
    var activeAuto by remember { mutableStateOf(currentConfig?.isAutoSyncEnabled ?: true) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = CarbonSlate),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Settings, contentDescription = "", tint = PhotoAmber, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Sincronizador en la Nube",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                }

                Text(
                    text = "Configura un servicio compatible con WebDAV (Nextcloud o servidor propio) para respaldar tus assets RAW, metrajes y sidecars XMP automáticamente.",
                    style = MaterialTheme.typography.bodySmall,
                    color = LightTextMuted
                )

                TextField(
                    value = epUrl,
                    onValueChange = { epUrl = it },
                    label = { Text("Servidor WebDAV Cloud") },
                    colors = TextFieldDefaults.colors(focusedContainerColor = DarkCardBg, unfocusedContainerColor = DarkCardBg, focusedLabelColor = PhotoAmber),
                    modifier = Modifier.fillMaxWidth().testTag("cloud_endpoint_input")
                )

                TextField(
                    value = user,
                    onValueChange = { user = it },
                    label = { Text("Usuario WebDAV") },
                    colors = TextFieldDefaults.colors(focusedContainerColor = DarkCardBg, unfocusedContainerColor = DarkCardBg, focusedLabelColor = PhotoAmber),
                    modifier = Modifier.fillMaxWidth().testTag("cloud_user_input")
                )

                TextField(
                    value = pass,
                    onValueChange = { pass = it },
                    label = { Text("Token/Contraseña") },
                    visualTransformation = PasswordVisualTransformation(),
                    colors = TextFieldDefaults.colors(focusedContainerColor = DarkCardBg, unfocusedContainerColor = DarkCardBg, focusedLabelColor = PhotoAmber),
                    modifier = Modifier.fillMaxWidth().testTag("cloud_pass_input")
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Sincronizar en tiempo real", style = MaterialTheme.typography.bodyMedium, color = Color.White)
                    Switch(
                        checked = activeAuto,
                        onCheckedChange = { activeAuto = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = PhotoAmber,
                            checkedTrackColor = PhotoAmber.copy(alpha = 0.4f),
                            uncheckedThumbColor = LightTextMuted,
                            uncheckedTrackColor = DarkCardBg
                        ),
                        modifier = Modifier.testTag("cloud_sync_switch")
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancelar", color = LightTextMuted)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            onSubmit(
                                CloudSyncConfig(
                                    provider = "Nextcloud Photo Sync",
                                    endpointUrl = epUrl,
                                    username = user,
                                    password = pass,
                                    isAutoSyncEnabled = activeAuto
                                )
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PhotoAmber, contentColor = DeepCharcoal),
                        modifier = Modifier.testTag("save_cloud_button")
                    ) {
                        Text("Guardar", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// TELEMETRY CONSOLE & LOGS CONSOLE DIALOG
// -------------------------------------------------------------

@Composable
fun TelemetryConsoleDialog(
    logs: List<SyncLog>,
    isSyncing: Boolean,
    progress: Float?,
    activeFile: String,
    onDismiss: () -> Unit,
    onStartSync: () -> Unit,
    onClear: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = DeepCharcoal),
            border = BorderStroke(1.dp, CarbonSlate),
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.List, contentDescription = "", tint = EmeraldSync, modifier = Modifier.size(22.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Monitoreo en Tiempo Real", style = MaterialTheme.typography.titleMedium, color = Color.White)
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }

                if (isSyncing && progress != null) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = CarbonSlate),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "ENVIANDO: $activeFile",
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                    color = PhotoAmber,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = "${(progress * 100).toInt()}%",
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                    color = EmeraldSync
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            LinearProgressIndicator(
                                progress = { progress },
                                color = EmeraldSync,
                                trackColor = DarkCardBg,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(CodeGrey)
                        .border(1.dp, CarbonSlate)
                ) {
                    if (logs.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Consola inactiva. Esperando comandos...",
                                style = MaterialTheme.typography.labelSmall,
                                color = LightTextMuted
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            items(logs) { log ->
                                val timeStr = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(log.timestamp))
                                val (color, prefix) = when (log.type) {
                                    SyncLogType.Success -> Pair(EmeraldSync, "[OK]")
                                    SyncLogType.Warning -> Pair(PhotoAmber, "[WARN]")
                                    SyncLogType.Error -> Pair(ErrorCrimson, "[ERROR]")
                                    else -> Pair(Color.Cyan, "[INFO]")
                                }
                                Row(modifier = Modifier.fillMaxWidth()) {
                                    Text(
                                        text = "$timeStr ",
                                        style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                                        color = LightTextMuted
                                    )
                                    Text(
                                        text = "$prefix ${log.message}",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.SemiBold
                                        ),
                                        color = color
                                    )
                                }
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = onClear,
                        enabled = logs.isNotEmpty()
                    ) {
                        Text("Limpiar Logs", color = LightTextMuted)
                    }

                    Button(
                        onClick = onStartSync,
                        enabled = !isSyncing,
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldSync, contentColor = DeepCharcoal),
                        modifier = Modifier.testTag("action_sync_now_button")
                    ) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = "", tint = DeepCharcoal, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Sincronizar Todo", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun OpenClawDialog(
    status: String,
    url: String,
    name: String,
    uuid: String,
    activeSearch: Boolean,
    indexedTags: Int,
    clusterNodes: List<String>,
    onDismiss: () -> Unit,
    onConnect: (String, String) -> Unit,
    onDisconnect: () -> Unit,
    onSync: () -> Unit,
    onToggleSearch: (Boolean) -> Unit
) {
    var gatewayInput by remember { mutableStateOf(url) }
    var nodeNameInput by remember { mutableStateOf(name) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = CarbonSlate),
            border = BorderStroke(1.dp, Color(0xFF2C3E50)),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("openclaw_dialog_box")
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth()
            ) {
                // Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Share, 
                        contentDescription = "OpenClaw Node",
                        tint = PhotoAmber,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "OpenClaw Gateway",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                        Text(
                            text = "Consola de Nodo Distribuido",
                            style = MaterialTheme.typography.labelSmall,
                            color = LightTextMuted
                        )
                    }
                    
                    // Connected/Active pulse dot
                    val (statusColor, statusText) = when (status) {
                        "CONNECTED" -> Pair(EmeraldSync, "CONECTADO")
                        "CONNECTING" -> Pair(PhotoAmber, "CONECTANDO")
                        "SYNC_INDEX" -> Pair(OceanBlue, "PUBLICANDO")
                        else -> Pair(LightTextMuted, "DESCONECTADO")
                    }
                    
                    Box(
                        modifier = Modifier
                            .background(statusColor.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(statusColor)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = statusText,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = statusColor
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Divider(color = Color(0xFF2C3E50))
                Spacer(modifier = Modifier.height(16.dp))

                if (status == "CONNECTED" || status == "SYNC_INDEX") {
                    // Node identity parameters
                    Text(
                        text = "ESTADO OPERACIONAL DEL NODO",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = PhotoAmber
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Card(
                        colors = CardDefaults.cardColors(containerColor = DarkCardBg),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Nombre del Nodo:", style = MaterialTheme.typography.bodySmall, color = LightTextMuted)
                                Text(name, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = Color.White)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("ID del Nodo (UUID):", style = MaterialTheme.typography.bodySmall, color = LightTextMuted)
                                Text(uuid, style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace), color = LightTextMuted)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Gateway Server:", style = MaterialTheme.typography.bodySmall, color = LightTextMuted)
                                Text(url, style = MaterialTheme.typography.bodySmall, color = Color.White)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Etiquetas en Clúster:", style = MaterialTheme.typography.bodySmall, color = LightTextMuted)
                                Text("$indexedTags Registradas", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = PhotoAmber)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Toggle distributed tags search
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Búsqueda Delegada", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = Color.White)
                            Text("Sugerir etiquetas y organizar búsquedas distribuidas vía OpenClaw Gateway", style = MaterialTheme.typography.bodySmall, color = LightTextMuted)
                        }
                        Switch(
                            checked = activeSearch,
                            onCheckedChange = onToggleSearch,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = PhotoAmber,
                                checkedTrackColor = PhotoAmber.copy(alpha = 0.4f),
                                uncheckedThumbColor = LightTextMuted,
                                uncheckedTrackColor = DarkCardBg
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Cluster Neighbors Topology
                    Text(
                        text = "OTROS NODOS EN EL CLÚSTER OPENCLAW",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = LightTextMuted
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        clusterNodes.forEach { neighbor ->
                            Box(
                                modifier = Modifier
                                    .background(DarkCardBg, RoundedCornerShape(6.dp))
                                    .border(1.dp, Color(0xFF2C3E50), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.List, "neighbor", modifier = Modifier.size(10.dp), tint = LightTextMuted)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(neighbor, style = MaterialTheme.typography.labelSmall, color = Color.White)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Actions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = onDisconnect,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = ErrorCrimson),
                            border = BorderStroke(1.dp, ErrorCrimson.copy(alpha = 0.5f))
                        ) {
                            Text("Desconectar")
                        }

                        Button(
                            onClick = onSync,
                            enabled = status == "CONNECTED",
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = PhotoAmber, contentColor = DeepCharcoal)
                        ) {
                            Icon(Icons.Filled.Refresh, null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Sincronizar", fontWeight = FontWeight.Bold)
                        }
                    }
                } else {
                    // Setup configuration form
                    Text(
                        text = "CONEXIÓN A GATEWAY CLAW",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = PhotoAmber
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = gatewayInput,
                        onValueChange = { gatewayInput = it },
                        label = { Text("URL del Gateway") },
                        placeholder = { Text("http://192.168.1.50:9000") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = PhotoAmber,
                            unfocusedBorderColor = Color(0xFF2C3E50)
                        ),
                        singleLine = true,
                        enabled = status != "CONNECTING"
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = nodeNameInput,
                        onValueChange = { nodeNameInput = it },
                        label = { Text("Nombre del Nodo Local") },
                        placeholder = { Text("Ingresar nombre descriptivo") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = PhotoAmber,
                            unfocusedBorderColor = Color(0xFF2C3E50)
                        ),
                        singleLine = true,
                        enabled = status != "CONNECTING"
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        TextButton(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.textButtonColors(contentColor = LightTextMuted)
                        ) {
                            Text("Cancelar")
                        }

                        Button(
                            onClick = { onConnect(gatewayInput, nodeNameInput) },
                            enabled = gatewayInput.isNotEmpty() && nodeNameInput.isNotEmpty() && status != "CONNECTING",
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = PhotoAmber, contentColor = DeepCharcoal)
                        ) {
                            if (status == "CONNECTING") {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), color = DeepCharcoal, strokeWidth = 2.dp)
                            } else {
                                Text("Acoplar Nodo", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
