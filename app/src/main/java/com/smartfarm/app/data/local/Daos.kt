package com.smartfarm.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface CropDao {
    @Query("SELECT * FROM cached_crops ORDER BY id DESC")
    fun observeAll(): Flow<List<CropEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<CropEntity>)

    @Query("DELETE FROM cached_crops")
    suspend fun clear()

    @Transaction
    suspend fun replaceAll(items: List<CropEntity>) {
        clear()
        insertAll(items)
    }
}

@Dao
interface JournalDao {
    @Query("SELECT * FROM cached_journals ORDER BY date DESC, id DESC")
    fun observeAll(): Flow<List<JournalEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<JournalEntity>)

    @Query("DELETE FROM cached_journals")
    suspend fun clear()

    @Transaction
    suspend fun replaceAll(items: List<JournalEntity>) {
        clear()
        insertAll(items)
    }
}

@Dao
interface TaskDao {
    @Query("SELECT * FROM cached_tasks ORDER BY dueDate ASC, id DESC")
    fun observeAll(): Flow<List<TaskEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<TaskEntity>)

    @Query("DELETE FROM cached_tasks")
    suspend fun clear()

    @Transaction
    suspend fun replaceAll(items: List<TaskEntity>) {
        clear()
        insertAll(items)
    }
}
