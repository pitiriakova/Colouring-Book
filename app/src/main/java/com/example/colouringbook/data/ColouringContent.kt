package com.example.colouringbook.data

import androidx.annotation.DrawableRes
import androidx.compose.ui.graphics.Color
import com.example.colouringbook.R

data class Category(
    val id: String,
    val title: String,
    @param:DrawableRes val imageRes: Int
)

data class ColouringImage(
    val id: String,
    val name: String,
    @param:DrawableRes val imageRes: Int
)

val homeCategories = listOf(
    Category("cats", "Cats", R.drawable.cat_coloring),
    Category("dinos", "Dinos", R.drawable.cat_coloring),
    Category("dolls", "Dolls", R.drawable.cat_coloring),
    Category("balls", "Balls", R.drawable.cat_coloring)
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
    Color(0xFFE4D8CF)
)

fun categoryImages(category: Category): List<ColouringImage> =
    List(8) { index ->
        ColouringImage(
            id = "${category.id}-${index + 1}",
            name = "${category.title} ${index + 1}",
            imageRes = category.imageRes
        )
    }
