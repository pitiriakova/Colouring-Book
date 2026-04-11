package com.example.colouringbook

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.colouringbook.ui.theme.ColouringBookTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ColouringBookTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    ColouringBookApp(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

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

private val homeCategories = listOf(
    Category("cats", "Cats", R.drawable.cat_coloring),
    Category("dinos", "Dinos", R.drawable.cat_coloring),
    Category("dolls", "Dolls", R.drawable.cat_coloring),
    Category("balls", "Balls", R.drawable.cat_coloring)
)

private fun categoryImages(category: Category): List<ColouringImage> =
    List(8) { index ->
        ColouringImage(
            id = "${category.id}-${index + 1}",
            name = "${category.title} ${index + 1}",
            imageRes = category.imageRes
        )
    }

@Composable
fun ColouringBookApp(modifier: Modifier = Modifier) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "home",
        modifier = modifier
    ) {
        composable("home") {
            HomeScreen(
                categories = homeCategories,
                onCategoryClick = { category ->
                    navController.navigate("category/${category.id}")
                }
            )
        }
        composable(
            route = "category/{categoryId}",
            arguments = listOf(navArgument("categoryId") { type = NavType.StringType })
        ) { backStackEntry ->
            val categoryId = backStackEntry.arguments?.getString("categoryId")
            val category = homeCategories.firstOrNull { it.id == categoryId } ?: homeCategories.first()

            CategoryScreen(
                category = category,
                images = categoryImages(category),
                onBackClick = { navController.popBackStack() },
                onImageClick = { image ->
                    navController.navigate("image/${category.id}/${image.id}")
                }
            )
        }
        composable(
            route = "image/{categoryId}/{imageId}",
            arguments = listOf(
                navArgument("categoryId") { type = NavType.StringType },
                navArgument("imageId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val categoryId = backStackEntry.arguments?.getString("categoryId")
            val imageId = backStackEntry.arguments?.getString("imageId")
            val category = homeCategories.firstOrNull { it.id == categoryId } ?: homeCategories.first()
            val image = categoryImages(category).firstOrNull { it.id == imageId } ?: categoryImages(category).first()

            FullScreenImageScreen(
                categoryTitle = category.title,
                image = image,
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}

@Composable
fun HomeScreen(
    categories: List<Category>,
    onCategoryClick: (Category) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFFFF8EC))
            .padding(horizontal = 20.dp, vertical = 24.dp)
    ) {
        Text(
            text = "Colouring Book",
            fontSize = 30.sp
        )
        Spacer(modifier = Modifier.size(8.dp))
        Text(
            text = "Pick a category to start colouring",
            color = Color(0xFF5F5A52)
        )
        Spacer(modifier = Modifier.size(20.dp))
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(16.dp),
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            items(categories) { category ->
                CategoryTileCard(
                    category = category,
                    onClick = { onCategoryClick(category) }
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    ColouringBookTheme {
        HomeScreen(
            categories = homeCategories,
            onCategoryClick = {}
        )
    }
}

@Composable
fun CategoryTileCard(
    category: Category,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shadowElevation = 6.dp,
        tonalElevation = 2.dp,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
        color = Color.White
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(androidx.compose.foundation.shape.RoundedCornerShape(18.dp))
                    .background(Color(0xFFF8F1E4)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = category.imageRes),
                    contentDescription = category.title,
                    modifier = Modifier.fillMaxWidth(),
                    contentScale = ContentScale.Fit
                )
            }
            Spacer(modifier = Modifier.size(12.dp))
            Text(
                text = category.title,
                fontSize = 18.sp
            )
        }
    }
}

@Composable
fun CategoryScreen(
    category: Category,
    images: List<ColouringImage>,
    onBackClick: () -> Unit,
    onImageClick: (ColouringImage) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFFFF8EC))
            .padding(horizontal = 20.dp, vertical = 24.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back"
                )
            }
            Text(
                text = category.title,
                fontSize = 28.sp
            )
        }
        Spacer(modifier = Modifier.size(8.dp))
        Text(
            text = "Choose a picture to colour",
            color = Color(0xFF5F5A52)
        )
        Spacer(modifier = Modifier.size(20.dp))
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(16.dp),
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            items(images) { image ->
                ColouringImageTile(
                    image = image,
                    onClick = { onImageClick(image) }
                )
            }
        }
    }
}

@Composable
fun ColouringImageTile(
    image: ColouringImage,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shadowElevation = 4.dp,
        tonalElevation = 1.dp,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
        color = Color.White
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(androidx.compose.foundation.shape.RoundedCornerShape(18.dp))
                    .background(Color(0xFFF8F1E4)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = image.imageRes),
                    contentDescription = image.name,
                    modifier = Modifier.fillMaxWidth(),
                    contentScale = ContentScale.Fit
                )
            }
            Spacer(modifier = Modifier.size(12.dp))
            Text(
                text = image.name,
                fontSize = 16.sp
            )
        }
    }
}

@Composable
fun FullScreenImageScreen(
    categoryTitle: String,
    image: ColouringImage,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFFFF8EC))
            .padding(horizontal = 20.dp, vertical = 24.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back"
                )
            }
            Column {
                Text(
                    text = categoryTitle,
                    fontSize = 24.sp
                )
                Text(
                    text = image.name,
                    color = Color(0xFF5F5A52)
                )
            }
        }
        Spacer(modifier = Modifier.size(20.dp))
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 12.dp),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(28.dp),
            color = Color.White,
            shadowElevation = 8.dp
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFF8F1E4))
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = image.imageRes),
                    contentDescription = image.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            }
        }
    }
}
