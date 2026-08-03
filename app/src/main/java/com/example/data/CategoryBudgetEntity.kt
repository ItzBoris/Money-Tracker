package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "category_budgets")
data class CategoryBudgetEntity(
    @PrimaryKey val categoryName: String,
    val monthlyBudget: Double,
    val iconName: String,
    val colorHex: String
)
