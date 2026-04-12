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
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.FormatColorFill
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.example.colouringbook.utils.applyBrushStrokeWithinMask
import com.example.colouringbook.utils.buildFillRegionMask
import com.example.colouringbook.utils.loadImmutableBitmap
import com.example.colouringbook.utils.loadMutableBitmap
import com.example.colouringbook.utils.mapTapToBitmap
import com.example.colouringbook.utils.toArgbInt
import coil.compose.AsyncImage
import android.graphics.Point

private const val BRUSH_THICKNESS_PX = 15
private const val BRUSH_RADIUS_PX = BRUSH_THICKNESS_PX / 2
private val PaletteSwatchSpacing = 12.dp
private val ToolIconSize = 36.dp
private val ToolIconPaddingHorizontal = 12.dp
private val ToolIconPaddingVertical = 10.dp

private enum class ColoringTool {
    Brush,
    Fill
}

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
    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFFFF8EC))
            .padding(horizontal = 20.dp, vertical = 24.dp)
    ) {
        val gridSpacing = 12.dp
        val availableGridHeight = maxHeight.coerceAtLeast(240.dp)
        val tileHeight = ((availableGridHeight - gridSpacing) / 2).coerceAtLeast(140.dp)

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxWidth()
                .height(availableGridHeight),
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp),
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 8.dp),
            userScrollEnabled = false
        ) {
            items(categories) { category ->
                CategoryTileCard(
                    category = category,
                    onClick = { onCategoryClick(category) },
                    modifier = Modifier.height(tileHeight)
                )
            }
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
                    .weight(1f)
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
            Spacer(modifier = Modifier.size(8.dp))
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
    var selectedColor by remember { mutableStateOf(pastelPalette.first()) }
    var selectedTool by remember(image.id) { mutableStateOf(ColoringTool.Brush) }
    var isDrawing by remember(image.id) { mutableStateOf(false) }
    var activeRegionMask by remember(image.id) { mutableStateOf<BooleanArray?>(null) }
    var previousDrawPoint by remember(image.id) { mutableStateOf<Point?>(null) }
    val outlineBitmap = remember(image.id, image.imageSource) {
        loadImmutableBitmap(context, image.imageSource)
    }
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
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 12.dp),
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(16.dp)
        ) {
            PaletteSidebar(
                colors = pastelPalette,
                selectedColor = selectedColor,
                onColorSelected = { selectedColor = it },
                selectedTool = selectedTool,
                onToolSelected = { selectedTool = it },
                modifier = Modifier.width(140.dp)
            )
            Surface(
                modifier = Modifier
                    .weight(1f),
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
                        .pointerInput(selectedTool, selectedColor, colouringBitmap, outlineBitmap, imageContainerSize) {
                            if (selectedTool == ColoringTool.Fill) {
                                detectTapGestures { tapOffset ->
                                    val bitmapOffset = mapTapToBitmap(
                                        tapOffset = tapOffset,
                                        containerSize = imageContainerSize,
                                        bitmapWidth = colouringBitmap.width,
                                        bitmapHeight = colouringBitmap.height
                                    ) ?: return@detectTapGestures

                                    if (
                                        com.example.colouringbook.utils.floodFillWithinOutline(
                                            bitmap = colouringBitmap,
                                            startX = bitmapOffset.x,
                                            startY = bitmapOffset.y,
                                            replacementColor = selectedColor.toArgbInt()
                                        )
                                    ) {
                                        bitmapVersion++
                                    }
                                }
                            } else {
                                detectDragGestures(
                                    onDragStart = { dragStart ->
                                        val startPoint = mapTapToBitmap(
                                            tapOffset = dragStart,
                                            containerSize = imageContainerSize,
                                            bitmapWidth = colouringBitmap.width,
                                            bitmapHeight = colouringBitmap.height
                                        ) ?: return@detectDragGestures

                                        val regionMask = buildFillRegionMask(
                                            bitmap = outlineBitmap,
                                            startX = startPoint.x,
                                            startY = startPoint.y
                                        ) ?: return@detectDragGestures

                                        activeRegionMask = regionMask
                                        previousDrawPoint = startPoint
                                        isDrawing = true

                                        if (
                                            applyBrushStrokeWithinMask(
                                                bitmap = colouringBitmap,
                                                regionMask = regionMask,
                                                startX = startPoint.x,
                                                startY = startPoint.y,
                                                endX = startPoint.x,
                                                endY = startPoint.y,
                                                replacementColor = selectedColor.toArgbInt(),
                                                brushRadiusPx = BRUSH_RADIUS_PX
                                            )
                                        ) {
                                            bitmapVersion++
                                        }
                                    },
                                    onDragCancel = {
                                        activeRegionMask = null
                                        previousDrawPoint = null
                                        isDrawing = false
                                    },
                                    onDragEnd = {
                                        activeRegionMask = null
                                        previousDrawPoint = null
                                        isDrawing = false
                                    }
                                ) { change, _ ->
                                    val regionMask = activeRegionMask ?: return@detectDragGestures
                                    val lastPoint = previousDrawPoint ?: return@detectDragGestures
                                    val mappedPoint = mapTapToBitmap(
                                        tapOffset = change.position,
                                        containerSize = imageContainerSize,
                                        bitmapWidth = colouringBitmap.width,
                                        bitmapHeight = colouringBitmap.height
                                    ) ?: return@detectDragGestures

                                    if (
                                        applyBrushStrokeWithinMask(
                                            bitmap = colouringBitmap,
                                            regionMask = regionMask,
                                            startX = lastPoint.x,
                                            startY = lastPoint.y,
                                            endX = mappedPoint.x,
                                            endY = mappedPoint.y,
                                            replacementColor = selectedColor.toArgbInt(),
                                            brushRadiusPx = BRUSH_RADIUS_PX
                                        )
                                    ) {
                                        bitmapVersion++
                                    }

                                    previousDrawPoint = mappedPoint
                                    change.consume()
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
                }
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
    swatchSize: androidx.compose.ui.unit.Dp,
    onColorSelected: (Color) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(PaletteSwatchSpacing),
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(PaletteSwatchSpacing),
        userScrollEnabled = false
    ) {
        items(colors) { color ->
            ColorSwatch(
                color = color,
                isSelected = color == selectedColor,
                baseSize = swatchSize,
                onClick = { onColorSelected(color) }
            )
        }
    }
}

@Composable
private fun PaletteSidebar(
    colors: List<Color>,
    selectedColor: Color,
    onColorSelected: (Color) -> Unit,
    selectedTool: ColoringTool,
    onToolSelected: (ColoringTool) -> Unit,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(
        modifier = modifier.fillMaxHeight()
    ) {
        val rowCount = ((colors.size + 1) / 2).coerceAtLeast(1)
        val reservedHeight = 150.dp
        val availablePaletteHeight = (maxHeight - reservedHeight).coerceAtLeast(140.dp)
        val swatchSize = ((availablePaletteHeight - (PaletteSwatchSpacing * (rowCount - 1))) / rowCount)
            .coerceIn(26.dp, 42.dp)

        Surface(
            modifier = Modifier.fillMaxSize(),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
        color = Color.White.copy(alpha = 0.96f),
        shadowElevation = 6.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(androidx.compose.foundation.shape.CircleShape)
                    .background(selectedColor)
                    .border(2.dp, Color.White, androidx.compose.foundation.shape.CircleShape)
            )
            Spacer(modifier = Modifier.size(12.dp))
            ColorPaletteRow(
                colors = colors,
                selectedColor = selectedColor,
                swatchSize = swatchSize,
                onColorSelected = onColorSelected,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.size(12.dp))
            ToolIndicator(
                selectedColor = selectedColor,
                selectedTool = selectedTool,
                onToolSelected = onToolSelected
            )
        }
    }
    }
}

@Composable
fun ColorSwatch(
    color: Color,
    isSelected: Boolean,
    baseSize: androidx.compose.ui.unit.Dp,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val circleShape = androidx.compose.foundation.shape.CircleShape
    val size = if (isSelected) baseSize else (baseSize - 6.dp).coerceAtLeast(20.dp)

    Box(
        modifier = modifier
            .size(size)
            .shadow(
                elevation = if (isSelected) 10.dp else 0.dp,
                shape = circleShape,
                clip = false
            )
            .clip(circleShape)
            .background(color)
            .border(
                width = if (isSelected) 4.dp else 2.dp,
                color = if (isSelected) Color.White else Color(0xFFDCCFC3),
                shape = circleShape
            )
            .clickable(onClick = onClick)
    )
}

@Composable
private fun ToolIndicator(
    selectedColor: Color,
    selectedTool: ColoringTool,
    onToolSelected: (ColoringTool) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ToolIcon(
            icon = Icons.Filled.Brush,
            contentDescription = "Brush tool",
            selected = selectedTool == ColoringTool.Brush,
            tint = selectedColor,
            onClick = { onToolSelected(ColoringTool.Brush) }
        )
        ToolIcon(
            icon = Icons.Filled.FormatColorFill,
            contentDescription = "Fill tool",
            selected = selectedTool == ColoringTool.Fill,
            tint = selectedColor,
            onClick = { onToolSelected(ColoringTool.Fill) }
        )
    }
}

@Composable
private fun ToolIcon(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    selected: Boolean,
    tint: Color,
    onClick: () -> Unit
) {
    Surface(
        color = if (selected) Color(0xFFF3E7D7) else Color.Transparent,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(14.dp),
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier
                .padding(
                    horizontal = ToolIconPaddingHorizontal,
                    vertical = ToolIconPaddingVertical
                )
                .size(ToolIconSize)
        )
    }
}
