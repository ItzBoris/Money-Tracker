package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ml_rules")
data class MlRuleEntity(
    @PrimaryKey val keyword: String,
    val categoryName: String,
    val weight: Float = 1.0f
)
