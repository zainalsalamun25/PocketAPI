package com.example.pocketapi.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Delete
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "request_history")
data class RequestHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val url: String,
    val method: String,
    val headersJson: String,
    val requestBody: String,
    val statusCode: Int,
    val responseTimeMs: Long,
    val isFavorite: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

@Dao
interface RequestHistoryDao {
    @Query("SELECT * FROM request_history ORDER BY timestamp DESC")
    fun getAllHistory(): Flow<List<RequestHistoryEntity>>

    @Query("SELECT * FROM request_history WHERE isFavorite = 1 ORDER BY timestamp DESC")
    fun getFavoriteHistory(): Flow<List<RequestHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(history: RequestHistoryEntity): Long

    @Delete
    suspend fun delete(history: RequestHistoryEntity)

    @Query("DELETE FROM request_history WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM request_history")
    suspend fun clearAll()

    @Query("UPDATE request_history SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun updateFavorite(id: Long, isFavorite: Boolean)
}