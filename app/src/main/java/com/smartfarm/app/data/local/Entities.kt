package com.smartfarm.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Offline cache entities for key data, mirroring server models 1:1 for simplicity. */

@Entity(tableName = "cached_crops")
data class CropEntity(
    @PrimaryKey val id: Int,
    val userId: Int,
    val name: String,
    val variety: String?,
    val fieldLocation: String?,
    val area: Double?,
    val plantingDate: String?,
    val expectedHarvestDate: String?,
    val status: String,
    val memo: String?,
    val image: String?,
    val createdAt: String?,
)

@Entity(tableName = "cached_journals")
data class JournalEntity(
    @PrimaryKey val id: Int,
    val userId: Int,
    val cropId: Int?,
    val cropName: String?,
    val date: String,
    val workType: String?,
    val weather: String?,
    val content: String?,
    val image: String?,
    val createdAt: String?,
)

@Entity(tableName = "cached_tasks")
data class TaskEntity(
    @PrimaryKey val id: Int,
    val userId: Int,
    val cropId: Int?,
    val cropName: String?,
    val title: String,
    val memo: String?,
    val dueDate: String?,
    val priority: String,
    val status: String,
    val createdAt: String?,
)
