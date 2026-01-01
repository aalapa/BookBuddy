package com.bookbuddy.utils

object CategoryColorGenerator {
    // Predefined color palette - distinct, vibrant colors
    private val colorPalette = listOf(
        "#007AFF", // Blue (primary)
        "#34C759", // Green
        "#FF9500", // Orange
        "#FF3B30", // Red
        "#AF52DE", // Purple
        "#FF2D55", // Pink
        "#5AC8FA", // Light Blue
        "#FFCC00", // Yellow
        "#32D74B", // Light Green
        "#FF6B6B", // Coral
        "#4ECDC4", // Teal
        "#95E1D3", // Mint
        "#F38181", // Salmon
        "#AA96DA", // Lavender
        "#FCBAD3", // Rose
        "#A8E6CF", // Mint Green
        "#FFD93D", // Golden Yellow
        "#6BCB77", // Forest Green
        "#4D96FF", // Sky Blue
        "#9B59B6", // Amethyst
    )
    
    private var colorIndex = 0
    
    /**
     * Generates a color for a category based on its name (deterministic)
     * This ensures the same category always gets the same color
     */
    fun generateColorForCategory(categoryName: String): String {
        // Use hash of category name to get a consistent color
        val hash = categoryName.hashCode()
        val index = Math.abs(hash) % colorPalette.size
        return colorPalette[index]
    }
    
    /**
     * Gets the next color from the palette (for sequential assignment)
     */
    fun getNextColor(): String {
        val color = colorPalette[colorIndex % colorPalette.size]
        colorIndex++
        return color
    }
    
    /**
     * Resets the color index (useful for testing)
     */
    fun reset() {
        colorIndex = 0
    }
}

