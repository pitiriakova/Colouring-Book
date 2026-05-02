package com.example.colouringbook.data

import android.content.Context
import androidx.annotation.DrawableRes
import androidx.compose.ui.graphics.Color
import com.example.colouringbook.R

data class Category(
    val id: String,
    val title: String,
    val imageSource: ImageSource,
    val tileImageSource: ImageSource = imageSource,
    val accentColor: Color,
    val borderColor: Color
)

data class ColouringImage(
    val id: String,
    val name: String,
    val imageSource: ImageSource,
    val borderColor: Color
)

data class ImageSource(
    @param:DrawableRes val drawableRes: Int? = null,
    val assetPath: String? = null
)

private val nicoleImageNames = mapOf(
    "nicole_cap" to "Cap Smile",
    "nicole_grandma_baby" to "Mama and Baby",
    "nicole_sunglasses" to "Cool Sunglasses",
    "nicole_slide" to "Slide Time",
    "nicole_winter_hat" to "Winter Hat",
    "nicole_tree" to "By the Tree",
    "nicole_slide_2" to "Slide Adventure"
)

val homeCategories = listOf(
    Category(
        id = "cats",
        title = "Cats",
        imageSource = ImageSource(assetPath = "sections/cats/cat_2.png"),
        tileImageSource = ImageSource(assetPath = "tiles/tile_cats.png"),
        accentColor = Color(0xFFFFE0C8),
        borderColor = Color(0xFFFFB4A0)
    ),
    Category(
        id = "dinos",
        title = "Dinos",
        imageSource = ImageSource(assetPath = "sections/dinos/dino_1.png"),
        tileImageSource = ImageSource(assetPath = "tiles/tile_dinos.png"),
        accentColor = Color(0xFFC8F0D8),
        borderColor = Color(0xFF90D9A8)
    ),
    Category(
        id = "nicole",
        title = "Nicole",
        imageSource = ImageSource(assetPath = "sections/nicole/nicole_slide.png"),
        tileImageSource = ImageSource(assetPath = "tiles/tile_nicole.png"),
        accentColor = Color(0xFFFFD6E8),
        borderColor = Color(0xFFF4A8C4)
    ),
    Category(
        id = "balls",
        title = "Balls",
        imageSource = ImageSource(assetPath = "sections/balls/ball_1.png"),
        tileImageSource = ImageSource(assetPath = "tiles/tile_balls.png"),
        accentColor = Color(0xFFFFF3C0),
        borderColor = Color(0xFFFFD966)
    )
)

val pastelPalette = listOf(
    Color(0xFFF8C8C8),
    Color(0xFFF7D9AE),
    Color(0xFFF9EDAF),
    Color(0xFFD8EDC7),
    Color(0xFFC9E8D7),
    Color(0xFFC8E5F6),
    Color(0xFFD7D5F8),
    Color(0xFFE9D4F5),
    Color(0xFFF6D3E0),
    Color(0xFFE4D8CF),
    Color(0xFFFF8A80),
    Color(0xFFFFB74D),
    Color(0xFFFFF176),
    Color(0xFFA5D6A7),
    Color(0xFF4DD0E1),
    Color(0xFF64B5F6),
    Color(0xFF9575CD),
    Color(0xFFF06292)
)

fun categoryImages(context: Context, category: Category): List<ColouringImage> {
    val sectionPath = "sections/${category.id}"
    val borderPalette = imageBorderPalette(category)
    val assetFiles = context.assets.list(sectionPath)
        ?.filter { file -> file.substringAfterLast('.', "").lowercase() in setOf("png", "jpg", "jpeg", "webp") }
        ?.sorted()
        .orEmpty()

    if (assetFiles.isNotEmpty()) {
        return assetFiles.mapIndexed { index, fileName ->
            val baseName = fileName.substringBeforeLast('.')
            ColouringImage(
                id = "${category.id}-${index + 1}",
                name = displayNameFor(category.id, baseName, index + 1),
                imageSource = ImageSource(assetPath = "$sectionPath/$fileName"),
                borderColor = borderPalette[index % borderPalette.size]
            )
        }
    }

    return List(8) { index ->
        ColouringImage(
            id = "${category.id}-${index + 1}",
            name = "${category.title} ${index + 1}",
            imageSource = category.imageSource,
            borderColor = borderPalette[index % borderPalette.size]
        )
    }
}

private fun imageBorderPalette(category: Category): List<Color> {
    return when (category.id) {
        "cats" -> listOf(
            Color(0xFFFFB4A0),
            Color(0xFFFFC4B5),
            Color(0xFFFFD1C5),
            Color(0xFFFFA996)
        )
        "dinos" -> listOf(
            Color(0xFF90D9A8),
            Color(0xFFA8E2B8),
            Color(0xFF7FCE9A),
            Color(0xFFBDEAC8)
        )
        "nicole" -> listOf(
            Color(0xFFF4A8C4),
            Color(0xFFF7BCD3),
            Color(0xFFEE95B7),
            Color(0xFFF9CCE0)
        )
        "balls" -> listOf(
            Color(0xFFFFD966),
            Color(0xFFFFE27F),
            Color(0xFFFFCD47),
            Color(0xFFFFE99E)
        )
        else -> listOf(category.borderColor)
    }
}

private fun displayNameFor(categoryId: String, baseName: String, position: Int): String {
    if (categoryId == "nicole") {
        return nicoleImageNames[baseName] ?: prettifyName(baseName)
    }

    return prettifyName(baseName).ifBlank {
        "${categoryId.replaceFirstChar { it.uppercase() }} $position"
    }
}

private fun prettifyName(value: String): String {
    return value
        .replace('_', ' ')
        .trim()
        .split(Regex("\\s+"))
        .filter { it.isNotBlank() }
        .joinToString(" ") { word ->
            word.replaceFirstChar { char -> char.uppercase() }
        }
}
