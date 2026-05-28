package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tryout_history")
data class TryoutHistoryItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val mode: String,
    val subject: String,
    val difficulty: String,
    val score: Int,
    val correctPts: Int,
    val wrongPts: Int,
    val mistakesJson: String, // Moshi serialized JSON string list of topics
    val timeUsed: Int,
    val timestamp: Long
)
