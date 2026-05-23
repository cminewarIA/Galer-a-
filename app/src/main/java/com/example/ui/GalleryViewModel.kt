package com.example.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.*
import com.example.network.GeminiManager
import com.example.network.NetworkShareManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class GalleryViewModel(private val repository: GalleryRepository) : ViewModel() {

    // Connections List
    val connections: StateFlow<List<NetworkConnection>> = repository.connections
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // All Assets List
    val allAssets: StateFlow<List<MediaAsset>> = repository.allAssets
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Cloud Config List
    val cloudConfig: StateFlow<CloudSyncConfig?> = repository.cloudConfig
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Sync progress indicators
    val syncProgress: StateFlow<Float?> = repository.syncProgress
    val syncFileName: StateFlow<String> = repository.syncFileName
    val syncLogs: StateFlow<List<SyncLog>> = repository.syncLogs

    // OpenClaw Interface bindings
    val openClawGatewayUrl = com.example.network.OpenClawGatewayManager.gatewayUrl
    val openClawGatewayToken = com.example.network.OpenClawGatewayManager.gatewayToken
    val openClawNodeName = com.example.network.OpenClawGatewayManager.nodeName
    val openClawNodeUuid = com.example.network.OpenClawGatewayManager.nodeUuid
    val openClawStatus = com.example.network.OpenClawGatewayManager.nodeStatus
    val isOpenClawActiveSearch = com.example.network.OpenClawGatewayManager.isClawManagedSearch
    val openClawTagsCount = com.example.network.OpenClawGatewayManager.indexedTagsCount
    val openClawClusterNodes = com.example.network.OpenClawGatewayManager.networkNodeCluster

    // Search and Filters
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _selectedMediaType = MutableStateFlow("ALL") // "ALL", "RAW", "VIDEO"
    val selectedMediaType = _selectedMediaType.asStateFlow()

    private val _selectedConnectionId = MutableStateFlow<Int?>(null) // null means all connections
    val selectedConnectionId = _selectedConnectionId.asStateFlow()

    // Advanced filters state
    private val _selectedTags = MutableStateFlow<Set<String>>(emptySet())
    val selectedTags = _selectedTags.asStateFlow()

    private val _selectedLocations = MutableStateFlow<Set<String>>(emptySet())
    val selectedLocations = _selectedLocations.asStateFlow()

    private val _selectedDatePreset = MutableStateFlow("ALL") // "ALL", "TODAY", "WEEK", "MONTH", "YEAR"
    val selectedDatePreset = _selectedDatePreset.asStateFlow()

    private val _selectedExtensions = MutableStateFlow<Set<String>>(emptySet())
    val selectedExtensions = _selectedExtensions.asStateFlow()

    // Available values for filters (computed from assets)
    val availableTags: StateFlow<List<String>> = allAssets.map { assets ->
        assets.flatMap { it.tags.split(",") }
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .distinct()
            .sorted()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val availableLocations: StateFlow<List<String>> = allAssets.map { assets ->
        assets.map { it.locationName.trim() }
            .filter { it.isNotEmpty() }
            .distinct()
            .sorted()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val availableExtensions: StateFlow<List<String>> = allAssets.map { assets ->
        assets.map { it.fileExtension.uppercase().trim() }
            .filter { it.isNotEmpty() }
            .distinct()
            .sorted()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Direct dynamic suggestions based on current query
    val searchSuggestions: StateFlow<List<SearchSuggestion>> = combine(
        allAssets,
        _searchQuery,
        com.example.network.OpenClawGatewayManager.nodeStatus,
        com.example.network.OpenClawGatewayManager.isClawManagedSearch
    ) { assets, query, clawStatus, clawManaged ->
        if (query.trim().length < 1) {
            return@combine emptyList<SearchSuggestion>()
        }
        val cleanQuery = query.lowercase().trim()
        val suggestions = mutableListOf<SearchSuggestion>()
        val isOpenClawConnected = (clawStatus == "CONNECTED" && clawManaged)

        // Find matches in tags
        val tagsSet = assets.flatMap { asset ->
            asset.tags.split(",").map { it.trim() }
        }.filter { it.isNotEmpty() }.distinct()
        tagsSet.filter { it.lowercase().contains(cleanQuery) }.take(4).forEach { tag ->
            suggestions.add(SearchSuggestion(tag, "tag", isClawOptimized = isOpenClawConnected))
        }

        // Find matches in locations
        val locationsSet = assets.map { it.locationName.trim() }.filter { it.isNotEmpty() }.distinct()
        locationsSet.filter { it.lowercase().contains(cleanQuery) }.take(4).forEach { loc ->
            suggestions.add(SearchSuggestion(loc, "location", isClawOptimized = isOpenClawConnected))
        }

        // Find matches in filenames
        assets.filter { it.fileName.lowercase().contains(cleanQuery) }.take(3).forEach { asset ->
            suggestions.add(SearchSuggestion(asset.fileName, "file", isClawOptimized = isOpenClawConnected))
        }

        // Special OpenClaw Cluster nodes simulated tag recommendations when connected!
        if (isOpenClawConnected) {
            val clusterTags = listOf("claw-cluster-backup", "gateway-verified", "nodo-distribuido")
            clusterTags.filter { it.contains(cleanQuery) }.forEach { cTag ->
                suggestions.add(SearchSuggestion(cTag, "cluster_tag", isClawOptimized = true))
            }
        }

        suggestions.distinctBy { it.text.lowercase() + "_" + it.type }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filtered Assets incorporating multi-criteria logic
    val filteredAssets: StateFlow<List<MediaAsset>> = combine(
        allAssets,
        _searchQuery,
        _selectedMediaType,
        _selectedConnectionId,
        _selectedTags,
        _selectedLocations,
        _selectedDatePreset,
        _selectedExtensions
    ) { FlowArgs ->
        val assets = FlowArgs[0] as List<MediaAsset>
        val query = FlowArgs[1] as String
        val mediaType = FlowArgs[2] as String
        val connId = FlowArgs[3] as Int?
        val tags = FlowArgs[4] as Set<String>
        val locations = FlowArgs[5] as Set<String>
        val datePreset = FlowArgs[6] as String
        val extensions = FlowArgs[7] as Set<String>

        assets.filter { asset ->
            // Filter by Connection
            if (connId != null && asset.connectionId != connId) return@filter false

            // Filter by Media Type (General Category)
            val isVideo = asset.fileExtension.lowercase() in listOf("mp4", "mkv", "mov")
            val isRaw = asset.fileExtension.lowercase() in listOf("nef", "cr2", "arw", "dng")
            when (mediaType) {
                "RAW" -> if (!isRaw) return@filter false
                "VIDEO" -> if (!isVideo) return@filter false
            }

            // Filter by selected Tags (Multi-criteria)
            if (tags.isNotEmpty()) {
                val assetTags = asset.tags.split(",").map { it.trim().lowercase() }
                val hasMatch = tags.any { selected -> assetTags.contains(selected.lowercase()) }
                if (!hasMatch) return@filter false
            }

            // Filter by selected Locations (Multi-criteria)
            if (locations.isNotEmpty()) {
                val matchesLocation = locations.any { selected -> asset.locationName.equals(selected, ignoreCase = true) }
                if (!matchesLocation) return@filter false
            }

            // Filter by selected individual extensions (Multi-criteria)
            if (extensions.isNotEmpty()) {
                if (!extensions.contains(asset.fileExtension.uppercase())) return@filter false
            }

            // Filter by Date Preset
            if (datePreset != "ALL") {
                val now = System.currentTimeMillis()
                val assetTime = asset.dateTimeOriginal
                val diffMs = now - assetTime
                val oneDayMs = 24L * 60 * 60 * 1000
                when (datePreset) {
                    "TODAY" -> if (diffMs > oneDayMs) return@filter false
                    "WEEK" -> if (diffMs > oneDayMs * 7) return@filter false
                    "MONTH" -> if (diffMs > oneDayMs * 30) return@filter false
                    "YEAR" -> if (diffMs > oneDayMs * 365) return@filter false
                }
            }

            // Filter by Search Query (FileName, Location, Tags)
            if (query.isNotEmpty()) {
                val cleanQuery = query.lowercase().trim()
                val filenameMatches = asset.fileName.lowercase().contains(cleanQuery)
                val locationMatches = asset.locationName.lowercase().contains(cleanQuery)
                val tagsMatch = asset.tags.lowercase().contains(cleanQuery)
                if (!filenameMatches && !locationMatches && !tagsMatch) return@filter false
            }

            true
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active Asset Selection
    private val _selectedAssetId = MutableStateFlow<Int?>(null)
    val selectedAssetId = _selectedAssetId.asStateFlow()

    val selectedAsset: StateFlow<MediaAsset?> = _selectedAssetId
        .flatMapLatest { id ->
            if (id == null) flowOf(null)
            else repository.getAssetById(id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // AI suggestions loading state
    private val _isAiLoading = MutableStateFlow(false)
    val isAiLoading = _isAiLoading.asStateFlow()

    private val _aiRecommendationMessage = MutableStateFlow<String?>(null)
    val aiRecommendationMessage = _aiRecommendationMessage.asStateFlow()

    init {
        // Auto initialize on first empty launch with a simulated NAS share connection
        viewModelScope.launch {
            repository.connections.first().let { currentList ->
                if (currentList.isEmpty()) {
                    repository.addConnection(
                        label = NetworkShareManager.DEFAULT_SHARE_LABEL,
                        type = "SMB (Compartido NAS)",
                        serverUrl = NetworkShareManager.EMULATOR_URL,
                        user = "Yonah",
                        pass = "Yasmany11"
                    )
                    // Set default cloud config mock to show Nextcloud capability
                    repository.saveCloudConfig(
                        CloudSyncConfig(
                            provider = "Nextcloud Photo Sync",
                            endpointUrl = "https://cloud.estudio.net/remote.php/webdav/backups",
                            username = "l_gonzalez",
                            password = "TokenSincronizadoSec",
                            isAutoSyncEnabled = true
                        )
                    )
                }
            }
        }
    }

    /**
     * UI actions
     */
    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setMediaTypeFilter(type: String) {
        _selectedMediaType.value = type
    }

    fun selectConnection(connId: Int?) {
        _selectedConnectionId.value = connId
    }

    fun selectAsset(assetId: Int?) {
        _selectedAssetId.value = assetId
        _aiRecommendationMessage.value = null
    }

    fun addNetworkShare(label: String, type: String, url: String, user: String, pass: String) {
        viewModelScope.launch {
            repository.addConnection(label, type, url, user, pass)
        }
    }

    fun removeNetworkShare(conn: NetworkConnection) {
        viewModelScope.launch {
            if (_selectedConnectionId.value == conn.id) {
                _selectedConnectionId.value = null
            }
            if (selectedAsset.value?.connectionId == conn.id) {
                _selectedAssetId.value = null
            }
            repository.deleteConnection(conn)
        }
    }

    fun updateMetadata(assetId: Int, tags: List<String>, location: String, rating: Int) {
        viewModelScope.launch {
            repository.updateAssetMetadata(assetId, tags, location, rating)
        }
    }

    fun saveCloudSetup(config: CloudSyncConfig) {
        viewModelScope.launch {
            repository.saveCloudConfig(config)
        }
    }

    fun triggerMassiveCloudSync() {
        viewModelScope.launch {
            repository.runFullCloudSync()
        }
    }

    fun requestAiMetadataSuggestions(asset: MediaAsset) {
        viewModelScope.launch {
            _isAiLoading.value = true
            _aiRecommendationMessage.value = null
            try {
                val suggestions = GeminiManager.suggestTags(asset)
                // Suggest joining into input field
                _aiRecommendationMessage.value = "IA recomendó: " + suggestions.joinToString(", ")
                // Append or offer suggestions
            } catch (e: Exception) {
                _aiRecommendationMessage.value = "Fallo de respuesta IA. Fallback local generado."
            } finally {
                _isAiLoading.value = false
            }
        }
    }

    fun toggleTagFilter(tag: String) {
        val current = _selectedTags.value
        _selectedTags.value = if (current.contains(tag)) current - tag else current + tag
    }

    fun toggleLocationFilter(location: String) {
        val current = _selectedLocations.value
        _selectedLocations.value = if (current.contains(location)) current - location else current + location
    }

    fun toggleExtensionFilter(ext: String) {
        val current = _selectedExtensions.value
        _selectedExtensions.value = if (current.contains(ext)) current - ext else current + ext
    }

    fun setDatePresetFilter(preset: String) {
        _selectedDatePreset.value = preset
    }

    fun clearAllFilters() {
        _searchQuery.value = ""
        _selectedTags.value = emptySet()
        _selectedLocations.value = emptySet()
        _selectedExtensions.value = emptySet()
        _selectedDatePreset.value = "ALL"
        _selectedMediaType.value = "ALL"
    }

    fun clearLogs() {
        repository.clearLogs()
    }

    /**
     * OpenClaw control triggers
     */
    fun connectToOpenClaw(url: String, name: String, token: String) {
        com.example.network.OpenClawGatewayManager.connectToGateway(url, name, token, repository, viewModelScope)
    }

    fun disconnectFromOpenClaw() {
        com.example.network.OpenClawGatewayManager.disconnectFromGateway(repository, viewModelScope)
    }

    fun syncOpenClawTags() {
        com.example.network.OpenClawGatewayManager.syncNodeTags(repository, viewModelScope)
    }

    fun toggleOpenClawSearch(enabled: Boolean) {
        com.example.network.OpenClawGatewayManager.toggleClawManagedSearch(enabled, repository)
    }
}

data class SearchSuggestion(
    val text: String,
    val type: String,
    val isClawOptimized: Boolean = false
)

class GalleryViewModelFactory(private val repository: GalleryRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GalleryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return GalleryViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
