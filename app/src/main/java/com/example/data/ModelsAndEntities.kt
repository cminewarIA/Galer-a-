package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "network_connections")
data class NetworkConnection(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val label: String,                // User-friendly connection name, e.g., "Home NAS - Photos"
    val type: String,                 // "WebDAV", "SMB", "Local Network Emulator"
    val serverUrl: String,            // URL or virtual net folder path
    val username: String,
    val password: String,
    val isActive: Boolean = true,
    val syncIntervalMinutes: Int = 15
)

@Entity(tableName = "media_assets")
data class MediaAsset(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val connectionId: Int,             // Related NetworkConnection ID
    val fileName: String,              // e.g., "DSC_1002.NEF"
    val relativePath: String,          // Relative path inside the share, e.g., "/RAW/DSC_1002.NEF"
    val fileExtension: String,         // "NEF", "CR2", "ARW", "DNG", "MP4", "MKV", "MOV", "JPG"
    val fileSizeBytes: Long,
    val dateTimeOriginal: Long,        // Capture / Modification timestamp
    val tags: String,                  // Comma-separated tags, e.g., "patagonia,landscape,raw"
    val locationName: String,          // Human readable location, e.g., "Torres del Paine, Chile"
    val latitude: Double? = null,
    val longitude: Double? = null,
    val rating: Int = 0,               // Star rating 0-5
    val xmpSyncStatus: String = "SYNCED", // "SYNCED", "PENDING", "ERROR"
    val cloudSyncStatus: String = "NOT_SYNCED", // "NOT_SYNCED", "PENDING", "SYNCED", "ERROR"
    val localUri: String? = null,      // Local Android content URI if cached, or simulated drawable name
    val dimensions: String = "6000x4000",
    val cameraModel: String = "Nikon Z7 II",
    val exifAperture: String = "f/4.0",
    val exifShutter: String = "1/250s",
    val exifIso: Int = 64
)

@Entity(tableName = "cloud_sync_configs")
data class CloudSyncConfig(
    @PrimaryKey val id: Int = 1,       // Single config row
    val provider: String = "Nextcloud",// "Nextcloud", "Owncloud", "Generic WebDAV"
    val endpointUrl: String = "",
    val username: String = "",
    val password: String = "",
    val isAutoSyncEnabled: Boolean = false,
    val lastSyncTime: Long = 0L,
    val syncStatus: String = "IDLE"    // "IDLE", "SYNCING", "COMPLETED", "ERROR"
)
