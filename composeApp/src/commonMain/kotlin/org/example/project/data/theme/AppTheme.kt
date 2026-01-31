package org.example.project.data.theme

import androidx.compose.ui.graphics.Color

object AppTheme {

    val color: AppColors = AppColors.fromHex(
        surfaceColor = 0xFF0F1115,
        cardOnSurface = 0xFF161A20,
        primaryButton = 0xFF7C5CFF,
        primaryText = 0xFFE6E8EB,
        disabledText = 0xFF555B66,
        secondaryText = 0xFFA9AFB8,
        draftIndicator = 0xFF8B90A0,
        actionRequiredIndicator = 0xFFE0B454,
        caughtUpIndicator = 0xFF4CAF7A
    )

}

data class AppColors(
    val surfaceColor: Color,
    val cardOnSurface: Color,
    val primaryButton: Color,
    val primaryText: Color,
    val secondaryText: Color,
    val disabledText: Color,
    val draftIndicator: Color,
    val actionRequiredIndicator: Color,
    val caughtUpIndicator: Color,
) {

    companion object {
        fun fromHex(
            surfaceColor: Long,
            cardOnSurface: Long,
            primaryButton: Long,
            primaryText: Long,
            disabledText: Long,
            secondaryText: Long,
            draftIndicator: Long,
            actionRequiredIndicator: Long,
            caughtUpIndicator: Long
        ): AppColors {
            return AppColors(
                surfaceColor = Color(surfaceColor),
                cardOnSurface = Color(cardOnSurface),
                primaryButton = Color(primaryButton),
                primaryText = Color(primaryText),
                disabledText = Color(disabledText),
                secondaryText = Color(secondaryText),
                draftIndicator = Color(draftIndicator),
                actionRequiredIndicator = Color(actionRequiredIndicator),
                caughtUpIndicator = Color(caughtUpIndicator)
            )
        }
    }

}