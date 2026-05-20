package com.example.mobilefinalproject.models.driver

import androidx.annotation.DrawableRes
import androidx.core.graphics.toColorInt
import android.graphics.Color
import com.example.mobilefinalproject.R

data class ButtonConfig(
    val text: String,
    @param:DrawableRes val backgroundRes: Int,
    val textColor: Int,
    val weight: Float
)

data class ActiveDeliveryConfig(
    @param:DrawableRes val badgeDrawable: Int,
    val strokeColor: String,
    val buttons: List<ButtonConfig>
)

val activeDeliveryConfigs = mapOf<String, ActiveDeliveryConfig>(
    "accepted" to ActiveDeliveryConfig(
        badgeDrawable = R.drawable.badge_accepted,
        strokeColor = "#FFC107",
        buttons = listOf(
            ButtonConfig("Start", R.drawable.button_blue, Color.WHITE, 0.5f),
            ButtonConfig(
                "Details",
                R.drawable.button_light_purple,
                "#FF9C27B0".toColorInt(),
                0.25f
            ),
            ButtonConfig("Cancel", R.drawable.button_red, Color.WHITE, 0.25f),
        )
    ),
    "in_progress" to ActiveDeliveryConfig(
        badgeDrawable = R.drawable.badge_in_progress,
        strokeColor = "#2196F3",
        buttons = listOf(
            ButtonConfig("Complete", R.drawable.button_green, Color.WHITE, 0.5f),
            ButtonConfig(
                "Details",
                R.drawable.button_light_purple,
                "#FF9C27B0".toColorInt(),
                0.5f
            ),
        )
    ),
    "completed" to ActiveDeliveryConfig(
        badgeDrawable = R.drawable.badge_completed,
        strokeColor = "#388E3C",
        buttons = emptyList()
    ),
    "cancelled" to ActiveDeliveryConfig(
        badgeDrawable = R.drawable.badge_cancelled,
        strokeColor = "#F44336",
        buttons = emptyList()
    ),
)
