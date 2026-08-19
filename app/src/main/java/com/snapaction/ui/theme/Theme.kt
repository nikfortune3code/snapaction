package com.snapaction.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// Semantic Badge Color Holder
data class CategoryColor(val container: Color, val content: Color)

data class CategoryColors(
    val event: CategoryColor,
    val grocery: CategoryColor,
    val expense: CategoryColor,
    val bookmark: CategoryColor
)

val LocalCategoryColors = staticCompositionLocalOf {
    CategoryColors(
        event = CategoryColor(LightBadgeEventContainer, LightBadgeEventContent),
        grocery = CategoryColor(LightBadgeGroceryContainer, LightBadgeGroceryContent),
        expense = CategoryColor(LightBadgeExpenseContainer, LightBadgeExpenseContent),
        bookmark = CategoryColor(LightBadgeBookmarkContainer, LightBadgeBookmarkContent)
    )
}

private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimaryAccent,
    onPrimary = Color.White,
    primaryContainer = DarkAccentMutedContainer,
    onPrimaryContainer = DarkPrimaryAccent,
    
    background = DarkBackgroundBase,
    onBackground = DarkTextPrimary,
    
    surface = DarkSurface,
    onSurface = DarkTextPrimary,
    onSurfaceVariant = DarkTextSecondary,
    
    outline = DarkBorderSubtle,
    outlineVariant = DarkBorderSubtle,
    
    tertiary = DarkStatusSuccess,
    tertiaryContainer = DarkStatusSuccessBg,
    onTertiaryContainer = DarkStatusSuccess
)

private val LightColorScheme = lightColorScheme(
    primary = LightPrimaryAccent,
    onPrimary = Color.White,
    primaryContainer = LightAccentMutedContainer,
    onPrimaryContainer = LightPrimaryAccent,
    
    background = LightBackgroundBase,
    onBackground = LightTextPrimary,
    
    surface = LightSurface,
    onSurface = LightTextPrimary,
    onSurfaceVariant = LightTextSecondary,
    
    outline = LightBorderSubtle,
    outlineVariant = LightBorderSubtle,
    
    tertiary = LightStatusSuccess,
    tertiaryContainer = LightStatusSuccessBg,
    onTertiaryContainer = LightStatusSuccess
)

@Composable
fun SnapActionTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    
    val categoryColors = if (darkTheme) {
        CategoryColors(
            event = CategoryColor(DarkBadgeEventContainer, DarkBadgeEventContent),
            grocery = CategoryColor(DarkBadgeGroceryContainer, DarkBadgeGroceryContent),
            expense = CategoryColor(DarkBadgeExpenseContainer, DarkBadgeExpenseContent),
            bookmark = CategoryColor(DarkBadgeBookmarkContainer, DarkBadgeBookmarkContent)
        )
    } else {
        CategoryColors(
            event = CategoryColor(LightBadgeEventContainer, LightBadgeEventContent),
            grocery = CategoryColor(LightBadgeGroceryContainer, LightBadgeGroceryContent),
            expense = CategoryColor(LightBadgeExpenseContainer, LightBadgeExpenseContent),
            bookmark = CategoryColor(LightBadgeBookmarkContainer, LightBadgeBookmarkContent)
        )
    }

    CompositionLocalProvider(
        LocalCategoryColors provides categoryColors
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            content = content
        )
    }
}
