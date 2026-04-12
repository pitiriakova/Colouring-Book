package com.example.colouringbook.data

import android.content.Context
import androidx.annotation.DrawableRes
import androidx.compose.ui.graphics.Color
import com.example.colouringbook.R

data class Category(
    val id: String,
    val title: String,
    val imageSource: ImageSource
)

data class ColouringImage(
    val id: String,
    val name: String,
    val imageSource: ImageSource
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
    Category("cats", "Cats", ImageSource(assetPath = "sections/cats/cat_2.png")),
    Category("dinos", "Dinos", ImageSource(assetPath = "sections/dinos/dino_1.png")),
    Category("nicole", "Nicole", ImageSource(assetPath = "sections/nicole/nicole_slide.png")),
    Category("balls", "Balls", ImageSource(assetPath = "sections/balls/ball_1.png"))
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
                imageSource = ImageSource(assetPath = "$sectionPath/$fileName")
            )
        }
    }

    return List(8) { index ->
        ColouringImage(
            id = "${category.id}-${index + 1}",
            name = "${category.title} ${index + 1}",
            imageSource = category.imageSource
        )
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
