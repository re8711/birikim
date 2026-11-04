package com.rslsoft.birikim.data.model

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.rslsoft.birikim.R

@Entity(tableName = "categories")
data class Category(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val iconKey: String,
    val colorHex: String,
    val isDefault: Boolean = false
) {
    companion object {
        val DEFAULT_CATEGORIES = listOf(
            Category(id = 1, name = "Fatura", iconKey = "receipt", colorHex = "#3B82F6", isDefault = true),
            Category(id = 2, name = "Kira", iconKey = "home", colorHex = "#8B5CF6", isDefault = true),
            Category(id = 3, name = "Abonelik", iconKey = "subscriptions", colorHex = "#EC4899", isDefault = true),
            Category(id = 4, name = "Market", iconKey = "shopping_cart", colorHex = "#10B981", isDefault = true),
            Category(id = 5, name = "Ulaşım", iconKey = "directions_bus", colorHex = "#F59E0B", isDefault = true),
            Category(id = 6, name = "Sağlık", iconKey = "medical_services", colorHex = "#EF4444", isDefault = true),
            Category(id = 7, name = "Eğlence", iconKey = "sports_esports", colorHex = "#6366F1", isDefault = true),
            Category(id = 8, name = "Eğitim", iconKey = "school", colorHex = "#14B8A6", isDefault = true),
            Category(id = 9, name = "Maaş", iconKey = "payments", colorHex = "#22C55E", isDefault = true),
            Category(id = 10, name = "Ek Gelir", iconKey = "add_chart", colorHex = "#0EA5E9", isDefault = true),
            Category(id = 11, name = "Diğer", iconKey = "more_horiz", colorHex = "#78909C", isDefault = true)
        )
    }
}

@Composable
fun Category.getDisplayName(): String {
    return when (id) {
        1L -> stringResource(R.string.cat_bills)
        2L -> stringResource(R.string.cat_rent)
        3L -> stringResource(R.string.cat_subscription)
        4L -> stringResource(R.string.cat_groceries)
        5L -> stringResource(R.string.cat_transport)
        6L -> stringResource(R.string.cat_health)
        7L -> stringResource(R.string.cat_entertainment)
        8L -> stringResource(R.string.cat_education)
        9L -> stringResource(R.string.cat_salary)
        10L -> stringResource(R.string.cat_side_income)
        11L -> stringResource(R.string.cat_other)
        else -> name
    }
}