package com.example.pocketapi.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "saved_requests")
data class SavedRequestEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val folderName: String = "General",
    val url: String,
    val method: String,
    val headersJson: String,
    val authJson: String,
    val requestBody: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Dao
interface SavedRequestDao {
    @Query("SELECT * FROM saved_requests ORDER BY createdAt DESC")
    fun getAllSavedRequests(): Flow<List<SavedRequestEntity>>

    @Query("SELECT DISTINCT folderName FROM saved_requests ORDER BY folderName ASC")
    fun getAllFolders(): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(request: SavedRequestEntity): Long

    @Delete
    suspend fun delete(request: SavedRequestEntity)

    @Query("DELETE FROM saved_requests WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM saved_requests WHERE folderName = :folderName")
    suspend fun deleteByFolder(folderName: String)
}