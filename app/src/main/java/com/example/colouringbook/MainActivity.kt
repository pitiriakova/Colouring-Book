package com.example.colouringbook

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.colouringbook.data.Category
import com.example.colouringbook.data.ColouringImage
import com.example.colouringbook.data.ImageSource
import com.example.colouringbook.data.categoryImages
import com.example.colouringbook.data.homeCategories
import com.example.colouringbook.data.pastelPalette
import com.example.colouringbook.ui.theme.ColouringBookTheme
import com.example.colouringbook.utils.floodFillWithinOutline
import com.example.colouringbook.utils.loadMutableBitmap
import com.example.colouringbook.utils.mapTapToBitmap
import com.example.colouringbook.utils.toArgbInt
import coil.compose.AsyncImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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

@Composable
fun ColouringBookApp(modifier: Modifier = Modifier) {
    val context = LocalContext.current
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
            val images = categoryImages(context, category)

            CategoryScreen(
                category = category,
                images = images,
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
            val images = categoryImages(context, category)
            val image = images.firstOrNull { it.id == imageId } ?: images.first()

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
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                AppImage(
                    imageSource = category.imageSource,
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
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                AppImage(
                    imageSource = image.imageSource,
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
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var selectedColor by remember { mutableStateOf(pastelPalette.first()) }
    var isFilling by remember(image.id) { mutableStateOf(false) }
    val colouringBitmap = remember(image.id, image.imageSource) {
        loadMutableBitmap(context, image.imageSource)
    }
    var bitmapVersion by remember(image.id) { mutableStateOf(0) }
    var imageContainerSize by remember { mutableStateOf(IntSize.Zero) }
    val imageBitmap = remember(bitmapVersion, colouringBitmap) {
        colouringBitmap.asImageBitmap()
    }

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
        Spacer(modifier = Modifier.size(16.dp))
        ColorPaletteRow(
            colors = pastelPalette,
            selectedColor = selectedColor,
            onColorSelected = { selectedColor = it }
        )
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
                    .background(Color.White)
                    .padding(20.dp)
                    .onSizeChanged { imageContainerSize = it }
                    .pointerInput(selectedColor, colouringBitmap, imageContainerSize, isFilling) {
                        detectTapGestures { tapOffset ->
                            if (isFilling) return@detectTapGestures

                            val bitmapOffset = mapTapToBitmap(
                                tapOffset = tapOffset,
                                containerSize = imageContainerSize,
                                bitmapWidth = colouringBitmap.width,
                                bitmapHeight = colouringBitmap.height
                            ) ?: return@detectTapGestures

                            isFilling = true
                            coroutineScope.launch {
                                val wasFilled = withContext(Dispatchers.Default) {
                                    floodFillWithinOutline(
                                        bitmap = colouringBitmap,
                                        startX = bitmapOffset.x,
                                        startY = bitmapOffset.y,
                                        replacementColor = selectedColor.toArgbInt()
                                    )
                                }

                                if (wasFilled) {
                                    bitmapVersion++
                                }
                                isFilling = false
                            }
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Image(
                    bitmap = imageBitmap,
                    contentDescription = image.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
                BrushIndicator(
                    selectedColor = selectedColor,
                    modifier = Modifier.align(Alignment.BottomStart)
                )
            }
        }
    }
}

@Composable
fun AppImage(
    imageSource: ImageSource,
    contentDescription: String,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Fit
) {
    when {
        imageSource.drawableRes != null -> Image(
            painter = painterResource(id = imageSource.drawableRes),
            contentDescription = contentDescription,
            modifier = modifier,
            contentScale = contentScale
        )

        imageSource.assetPath != null -> AsyncImage(
            model = "file:///android_asset/${imageSource.assetPath}",
            contentDescription = contentDescription,
            modifier = modifier,
            contentScale = contentScale
        )
    }
}

@Composable
fun ColorPaletteRow(
    colors: List<Color>,
    selectedColor: Color,
    onColorSelected: (Color) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        colors.forEachIndexed { index, color ->
            ColorSwatch(
                color = color,
                isSelected = color == selectedColor,
                onClick = { onColorSelected(color) }
            )
            if (index != colors.lastIndex) {
                Spacer(modifier = Modifier.width(8.dp))
            }
        }
    }
}

@Composable
fun ColorSwatch(
    color: Color,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val circleShape = androidx.compose.foundation.shape.CircleShape

    Box(
        modifier = modifier
            .size(28.dp)
            .shadow(
                elevation = if (isSelected) 6.dp else 0.dp,
                shape = circleShape,
                clip = false
            )
            .clip(circleShape)
            .background(color)
            .border(
                width = if (isSelected) 3.dp else 1.dp,
                color = if (isSelected) Color.White else Color(0xFFDCCFC3),
                shape = circleShape
            )
            .clickable(onClick = onClick)
    )
}

@Composable
fun BrushIndicator(
    selectedColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
        color = Color.White.copy(alpha = 0.96f),
        shadowElevation = 6.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(18.dp)
                    .clip(androidx.compose.foundation.shape.CircleShape)
                    .background(selectedColor)
                    .border(1.dp, Color.White, androidx.compose.foundation.shape.CircleShape)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Icon(
                imageVector = Icons.Filled.Brush,
                contentDescription = "Brush color",
                tint = selectedColor,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
