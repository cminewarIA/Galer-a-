package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface NetworkConnectionDao {
    @Query("SELECT * FROM network_connections ORDER BY label ASC")
    fun getAllConnections(): Flow<List<NetworkConnection>>

    @Query("SELECT * FROM network_connections WHERE id = :id LIMIT 1")
    suspend fun getConnectionById(id: Int): NetworkConnection?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConnection(connection: NetworkConnection): Long

    @Update
    suspend fun updateConnection(connection: NetworkConnection)

    @Delete
    suspend fun deleteConnection(connection: NetworkConnection)
}

@Dao
interface MediaAssetDao {
    @Query("SELECT * FROM media_assets ORDER BY dateTimeOriginal DESC")
    fun getAllAssets(): Flow<List<MediaAsset>>

    @Query("SELECT * FROM media_assets WHERE connectionId = :connectionId ORDER BY dateTimeOriginal DESC")
    fun getAssetsByConnection(connectionId: Int): Flow<List<MediaAsset>>

    @Query("SELECT * FROM media_assets WHERE id = :id LIMIT 1")
    fun getAssetById(id: Int): Flow<MediaAsset?>

    @Query("SELECT * FROM media_assets WHERE id = :id LIMIT 1")
    suspend fun getAssetByIdDirect(id: Int): MediaAsset?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAsset(asset: MediaAsset): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAssets(assets: List<MediaAsset>)

    @Update
    suspend fun updateAsset(asset: MediaAsset)

    @Delete
    suspend fun deleteAsset(asset: MediaAsset)

    @Query("SELECT * FROM media_assets WHERE xmpSyncStatus = 'PENDING'")
    suspend fun getPendingXmpSyncAssets(): List<MediaAsset>

    @Query("SELECT * FROM media_assets WHERE cloudSyncStatus = 'PENDING'")
    suspend fun getPendingCloudSyncAssets(): List<MediaAsset>
    
    @Query("DELETE FROM media_assets WHERE connectionId = :connectionId")
    suspend fun deleteAssetsByConnection(connectionId: Int)
}

@Dao
interface CloudSyncDao {
    @Query("SELECT * FROM cloud_sync_configs WHERE id = 1 LIMIT 1")
    fun getConfig(): Flow<CloudSyncConfig?>

    @Query("SELECT * FROM cloud_sync_configs WHERE id = 1 LIMIT 1")
    suspend fun getConfigDirect(): CloudSyncConfig?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateConfig(config: CloudSyncConfig)
}
