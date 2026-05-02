package com.example.colouringbook

import android.graphics.Point
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.FormatColorFill
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import coil.compose.AsyncImage
import com.example.colouringbook.data.Category
import com.example.colouringbook.data.ColouringImage
import com.example.colouringbook.data.ImageSource
import com.example.colouringbook.data.categoryImages
import com.example.colouringbook.data.homeCategories
import com.example.colouringbook.data.pastelPalette
import com.example.colouringbook.ui.theme.ColouringBookTheme
import com.example.colouringbook.ui.theme.MintBackground
import com.example.colouringbook.ui.theme.MintBackgroundSoft
import com.example.colouringbook.ui.theme.MintCard
import com.example.colouringbook.ui.theme.MintMist
import com.example.colouringbook.ui.theme.MintMistDeep
import com.example.colouringbook.ui.theme.MintProgressEnd
import com.example.colouringbook.ui.theme.MintProgressStart
import com.example.colouringbook.ui.theme.MintText
import com.example.colouringbook.ui.theme.MintTextMuted
import com.example.colouringbook.utils.applyBrushStrokeWithinMask
import com.example.colouringbook.utils.buildFillRegionMask
import com.example.colouringbook.utils.floodFillWithinOutline
import com.example.colouringbook.utils.loadImmutableBitmap
import com.example.colouringbook.utils.loadMutableBitmap
import com.example.colouringbook.utils.mapTapToBitmap
import com.example.colouringbook.utils.restoreBrushStrokeWithinMask
import com.example.colouringbook.utils.restoreRegionFromSource
import com.example.colouringbook.utils.toArgbInt

private const val BRUSH_THICKNESS_PX = 15
private const val BRUSH_RADIUS_PX = BRUSH_THICKNESS_PX / 2
private val ScreenPaddingHorizontal = 24.dp
private val ScreenPaddingTop = 28.dp
private val ScreenPaddingBottom = 20.dp
private val HomeGridGap = 14.dp
private val CardRadius = 28.dp
private val HeaderBackSize = 64.dp
private val RevealBrushRadius = 108.dp
private const val RevealGridColumns = 48
private const val RevealGridRows = 72
private const val RevealCompletionRatio = 0.995f

private data class RevealStroke(
    val start: androidx.compose.ui.geometry.Offset,
    val end: androidx.compose.ui.geometry.Offset
)

private enum class ColoringTool {
    Brush,
    Fill,
    Eraser
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
                onCategoryClick = { navController.navigate("category/${it.id}") }
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
                images = categoryImages(context, category),
                onBackClick = { navController.popBackStack() },
                onImageClick = { navController.navigate("image/${category.id}/${it.id}") }
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

            DrawingScreen(
                category = category,
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
            .background(MintBackground)
            .padding(
                start = ScreenPaddingHorizontal,
                end = ScreenPaddingHorizontal,
                top = ScreenPaddingTop,
                bottom = ScreenPaddingBottom
            )
    ) {
        val availableGridHeight = (maxHeight - 70.dp).coerceAtLeast(320.dp)
        val rowCount = ((categories.size + 1) / 2).coerceAtLeast(1)
        val tileHeight = (
            (availableGridHeight - (HomeGridGap * (rowCount - 1))) / rowCount
        ).coerceAtLeast(120.dp)

        Column(modifier = Modifier.fillMaxSize()) {
            Text(
                text = "Nicole Draws \uD83C\uDFA8",
                style = MaterialTheme.typography.headlineLarge,
                color = MintText
            )
            Spacer(modifier = Modifier.size(2.dp))
            Text(
                text = "Pick a category to colour!",
                style = MaterialTheme.typography.bodyLarge,
                color = MintTextMuted
            )
            Spacer(modifier = Modifier.size(18.dp))
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(availableGridHeight),
                horizontalArrangement = Arrangement.spacedBy(HomeGridGap),
                verticalArrangement = Arrangement.spacedBy(HomeGridGap),
                userScrollEnabled = rowCount > 2
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

@Composable
private fun CategoryTileCard(
    category: Category,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        color = Color.Transparent,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(CardRadius),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .aspectRatio(1f)
                        .clip(androidx.compose.foundation.shape.CircleShape)
                        .border(4.dp, category.borderColor, androidx.compose.foundation.shape.CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    AppImage(
                        imageSource = category.tileImageSource,
                        contentDescription = category.title,
                        modifier = Modifier.fillMaxSize(0.92f),
                        contentScale = ContentScale.Fit
                    )
                }
            }
            Text(
                text = category.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MintText
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
            .background(MintBackground)
    ) {
        ScreenHeader(
            title = category.title,
            subtitle = "Choose a picture to colour",
            onBackClick = onBackClick
        )
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp)
        ) {
            items(images) { image ->
                ImageTile(
                    category = category,
                    image = image,
                    onClick = { onImageClick(image) }
                )
            }
        }
    }
}

@Composable
private fun ScreenHeader(
    title: String,
    subtitle: String,
    onBackClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 24.dp, end = 24.dp, top = 28.dp, bottom = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Surface(
            modifier = Modifier
                .size(HeaderBackSize)
                .clickable(onClick = onBackClick),
            color = Color(0xFFCFE5FF),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = MintText,
                    modifier = Modifier.size(26.dp)
                )
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                color = MintText
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyLarge,
                color = MintTextMuted
            )
        }
    }
}

@Composable
private fun ImageTile(
    category: Category,
    image: ColouringImage,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        color = MintCard,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(CardRadius),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(androidx.compose.foundation.shape.RoundedCornerShape(28.dp))
                    .background(Color.White)
                    .border(
                        width = 4.dp,
                        color = image.borderColor,
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(28.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                AppImage(
                    imageSource = image.imageSource,
                    contentDescription = image.name,
                    modifier = Modifier.fillMaxSize(0.88f),
                    contentScale = ContentScale.Fit
                )
            }
        }
    }
}

@Composable
private fun DrawingScreen(
    category: Category,
    image: ColouringImage,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val outlineBitmap = remember(image.id, image.imageSource) {
        loadImmutableBitmap(context, image.imageSource)
    }
    val colouringBitmap = remember(image.id, image.imageSource) {
        loadMutableBitmap(context, image.imageSource)
    }
    var selectedColor by remember(image.id) { mutableStateOf(pastelPalette.first()) }
    var selectedTool by remember(image.id) { mutableStateOf(ColoringTool.Brush) }
    var bitmapVersion by remember(image.id) { mutableStateOf(0) }
    var imageContainerSize by remember(image.id) { mutableStateOf(IntSize.Zero) }
    val revealStrokes = remember(image.id) { mutableStateListOf<RevealStroke>() }
    var revealedCells by remember(image.id) { mutableStateOf(setOf<Int>()) }
    var activeRegionMask by remember(image.id) { mutableStateOf<BooleanArray?>(null) }
    var previousDrawPoint by remember(image.id) { mutableStateOf<Point?>(null) }
    var isDrawing by remember(image.id) { mutableStateOf(false) }
    var hasShownUnlockMessage by remember(image.id) { mutableStateOf(false) }
    var confettiBurstId by remember(image.id) { mutableStateOf(0) }

    val revealBrushRadiusPx = with(density) { RevealBrushRadius.toPx() }
    val revealProgress = revealedCells.size.toFloat() / (RevealGridColumns * RevealGridRows).toFloat()
    val isRevealComplete = revealProgress >= RevealCompletionRatio
    val sidebarWidth by animateDpAsState(
        targetValue = if (isRevealComplete) 136.dp else 80.dp,
        animationSpec = tween(durationMillis = 220),
        label = "sidebarWidth"
    )
    val imageBitmap = remember(bitmapVersion, colouringBitmap) { colouringBitmap.asImageBitmap() }

    LaunchedEffect(isRevealComplete) {
        if (isRevealComplete) {
            hasShownUnlockMessage = true
            confettiBurstId++
        }
    }

    Row(
        modifier = modifier
            .fillMaxSize()
            .background(MintBackground)
    ) {
        DrawingSidebar(
            width = sidebarWidth,
            unlocked = isRevealComplete,
            selectedColor = selectedColor,
            colors = pastelPalette,
            selectedTool = selectedTool,
            onBackClick = onBackClick,
            onColorSelected = { selectedColor = it },
            onToolSelected = { selectedTool = it }
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .padding(start = 16.dp, end = 20.dp, top = 20.dp, bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (isRevealComplete) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = category.title,
                        style = MaterialTheme.typography.titleLarge,
                        color = MintText
                    )
                    Text(
                        text = image.name,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MintTextMuted
                    )
                }
            }

            if (!isRevealComplete) {
                HintBar("Swipe to wipe away the magic mist!")
            } else if (hasShownUnlockMessage) {
                UnlockBar("Picture unlocked! Now choose a colour and start drawing!")
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                if (confettiBurstId > 0) {
                    ConfettiBurst(
                        burstId = confettiBurstId,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Transparent,
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(CardRadius),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.White)
                            .onSizeChanged { imageContainerSize = it }
                            .pointerInput(
                                selectedTool,
                                selectedColor,
                                imageContainerSize,
                                isRevealComplete,
                                colouringBitmap,
                                outlineBitmap
                            ) {
                                if (!isRevealComplete) return@pointerInput

                                if (selectedTool == ColoringTool.Fill || selectedTool == ColoringTool.Eraser) {
                                    detectTapGestures { tapOffset ->
                                        val bitmapOffset = mapTapToBitmap(
                                            tapOffset = tapOffset,
                                            containerSize = imageContainerSize,
                                            bitmapWidth = colouringBitmap.width,
                                            bitmapHeight = colouringBitmap.height
                                        ) ?: return@detectTapGestures

                                        val changed = if (selectedTool == ColoringTool.Eraser) {
                                            restoreRegionFromSource(
                                                targetBitmap = colouringBitmap,
                                                sourceBitmap = outlineBitmap,
                                                startX = bitmapOffset.x,
                                                startY = bitmapOffset.y
                                            )
                                        } else {
                                            floodFillWithinOutline(
                                                bitmap = colouringBitmap,
                                                startX = bitmapOffset.x,
                                                startY = bitmapOffset.y,
                                                replacementColor = selectedColor.toArgbInt()
                                            )
                                        }

                                        if (changed) {
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

                                            val changed = if (selectedTool == ColoringTool.Eraser) {
                                                restoreBrushStrokeWithinMask(
                                                    targetBitmap = colouringBitmap,
                                                    sourceBitmap = outlineBitmap,
                                                    regionMask = regionMask,
                                                    startX = startPoint.x,
                                                    startY = startPoint.y,
                                                    endX = startPoint.x,
                                                    endY = startPoint.y,
                                                    brushRadiusPx = BRUSH_RADIUS_PX
                                                )
                                            } else {
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
                                            }

                                            if (changed) {
                                                bitmapVersion++
                                            }
                                        },
                                        onDragEnd = {
                                            activeRegionMask = null
                                            previousDrawPoint = null
                                            isDrawing = false
                                        },
                                        onDragCancel = {
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

                                        val changed = if (selectedTool == ColoringTool.Eraser) {
                                            restoreBrushStrokeWithinMask(
                                                targetBitmap = colouringBitmap,
                                                sourceBitmap = outlineBitmap,
                                                regionMask = regionMask,
                                                startX = lastPoint.x,
                                                startY = lastPoint.y,
                                                endX = mappedPoint.x,
                                                endY = mappedPoint.y,
                                                brushRadiusPx = BRUSH_RADIUS_PX
                                            )
                                        } else {
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
                                        }

                                        if (changed) {
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
                            modifier = Modifier.fillMaxSize(0.88f),
                            contentScale = ContentScale.Fit
                        )

                        if (!isRevealComplete) {
                            RevealOverlay(
                                revealStrokes = revealStrokes,
                                brushRadius = RevealBrushRadius,
                                onRevealStroke = { start, end ->
                                    revealStrokes.add(RevealStroke(start, end))
                                    val revealedByBrush = revealCellsForStroke(
                                        startPoint = start,
                                        endPoint = end,
                                        containerSize = imageContainerSize,
                                        brushRadiusPx = revealBrushRadiusPx
                                    )
                                    if (revealedByBrush.isNotEmpty()) {
                                        revealedCells = revealedCells + revealedByBrush
                                    }
                                }
                            )
                        }
                    }
                }
            }

            if (!isRevealComplete) {
                RevealProgressBar(progress = revealProgress)
            }
        }
    }
}

@Composable
private fun DrawingSidebar(
    width: Dp,
    unlocked: Boolean,
    selectedColor: Color,
    colors: List<Color>,
    selectedTool: ColoringTool,
    onBackClick: () -> Unit,
    onColorSelected: (Color) -> Unit,
    onToolSelected: (ColoringTool) -> Unit
) {
    Surface(
        modifier = Modifier
            .width(width)
            .fillMaxHeight(),
        color = Color.Transparent,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(topEnd = CardRadius, bottomEnd = CardRadius),
    ) {
        BoxWithConstraints {
            val swatchRows = ((colors.size + 1) / 2)
            val spacing = 8.dp
            val reservedHeight = 260.dp
            val paletteHeight = (maxHeight - reservedHeight).coerceAtLeast(220.dp)
            val swatchSize = ((paletteHeight - (spacing * (swatchRows - 1))) / swatchRows)
                .coerceIn(20.dp, 34.dp)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp, vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SidebarBackButton(
                    unlocked = unlocked,
                    onBackClick = onBackClick
                )

                if (unlocked) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(androidx.compose.foundation.shape.CircleShape)
                            .background(selectedColor)
                            .border(4.dp, Color.White, androidx.compose.foundation.shape.CircleShape)
                    )
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height((swatchSize * swatchRows) + (spacing * (swatchRows - 1))),
                        horizontalArrangement = Arrangement.spacedBy(spacing),
                        verticalArrangement = Arrangement.spacedBy(spacing),
                        userScrollEnabled = false
                    ) {
                        items(colors) { color ->
                            PaletteSwatch(
                                color = color,
                                selected = selectedColor == color,
                                size = swatchSize,
                                onClick = { onColorSelected(color) }
                            )
                        }
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .height(1.5.dp)
                            .background(MintBackgroundSoft)
                    )
                    ToolRow(
                        selectedTool = selectedTool,
                        selectedColor = selectedColor,
                        onToolSelected = onToolSelected
                    )
                }
            }
        }
    }
}

@Composable
private fun SidebarBackButton(
    unlocked: Boolean,
    onBackClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .clickable(onClick = onBackClick),
        color = Color.Transparent,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = MintText,
                modifier = Modifier.size(if (unlocked) 22.dp else 26.dp)
            )
            if (unlocked) {
                Spacer(modifier = Modifier.size(8.dp))
                Text(
                    text = "Back",
                    style = MaterialTheme.typography.titleMedium,
                    color = MintText
                )
            }
        }
    }
}

@Composable
private fun PaletteSwatch(
    color: Color,
    selected: Boolean,
    size: Dp,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(androidx.compose.foundation.shape.CircleShape)
            .background(color)
            .border(
                width = if (selected) 3.dp else 0.dp,
                color = Color.White,
                shape = androidx.compose.foundation.shape.CircleShape
            )
            .clickable(onClick = onClick)
    )
}

@Composable
private fun ToolRow(
    selectedTool: ColoringTool,
    selectedColor: Color,
    onToolSelected: (ColoringTool) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ToolButton(
                label = "Brush",
                icon = Icons.Filled.Brush,
                selected = selectedTool == ColoringTool.Brush,
                selectedColor = selectedColor,
                modifier = Modifier.weight(1f),
                onClick = { onToolSelected(ColoringTool.Brush) }
            )
            ToolButton(
                label = "Fill",
                icon = Icons.Filled.FormatColorFill,
                selected = selectedTool == ColoringTool.Fill,
                selectedColor = selectedColor,
                modifier = Modifier.weight(1f),
                onClick = { onToolSelected(ColoringTool.Fill) }
            )
        }
        Row(modifier = Modifier.fillMaxWidth()) {
            ToolButton(
                label = "Erase",
                icon = null,
                selected = selectedTool == ColoringTool.Eraser,
                selectedColor = selectedColor,
                modifier = Modifier.weight(1f),
                onClick = { onToolSelected(ColoringTool.Eraser) }
            )
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun ToolButton(
    label: String,
    icon: ImageVector?,
    selected: Boolean,
    selectedColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clickable(onClick = onClick),
            color = if (selected) selectedColor else MintBackground,
            shape = androidx.compose.foundation.shape.CircleShape,
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        tint = if (selected) Color.White else MintTextMuted,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text(
                        text = "⌫",
                        color = if (selected) Color.White else MintTextMuted,
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.scale(1.05f)
                    )
                }
            }
        }
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MintTextMuted
        )
    }
}

@Composable
private fun HintBar(text: String) {
    Surface(
        color = Color.Transparent,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(14.dp),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text = "\u2728", color = MintTextMuted)
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = MintTextMuted
            )
        }
    }
}

@Composable
private fun UnlockBar(text: String) {
    Surface(
        color = Color(0xFFE3F7E9),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(text = "\uD83C\uDF89")
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                color = Color(0xFF2D7A44)
            )
        }
    }
}

@Composable
private fun RevealProgressBar(progress: Float) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .height(10.dp)
                .clip(androidx.compose.foundation.shape.RoundedCornerShape(999.dp))
                .background(MintBackgroundSoft)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress.coerceIn(0f, 1f))
                    .fillMaxHeight()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(MintProgressStart, MintProgressEnd)
                        )
                    )
            )
        }
        Text(
            text = "${(progress * 100).toInt()}%",
            style = MaterialTheme.typography.labelLarge,
            color = MintTextMuted
        )
    }
}

@Composable
private fun RevealOverlay(
    revealStrokes: List<RevealStroke>,
    brushRadius: Dp,
    onRevealStroke: (androidx.compose.ui.geometry.Offset, androidx.compose.ui.geometry.Offset) -> Unit
) {
    val sparklePositions = remember {
        listOf(
            0.12f to 0.18f,
            0.28f to 0.08f,
            0.70f to 0.14f,
            0.88f to 0.26f,
            0.06f to 0.52f,
            0.44f to 0.60f,
            0.78f to 0.72f,
            0.22f to 0.82f,
            0.58f to 0.30f,
            0.92f to 0.58f,
            0.14f to 0.70f,
            0.66f to 0.90f
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                var previousPoint: androidx.compose.ui.geometry.Offset? = null
                detectDragGestures(
                    onDragStart = { offset ->
                        previousPoint = offset
                        onRevealStroke(offset, offset)
                    },
                    onDragEnd = { previousPoint = null },
                    onDragCancel = { previousPoint = null }
                ) { change, _ ->
                    val currentPoint = change.position
                    val startPoint = previousPoint ?: currentPoint
                    onRevealStroke(startPoint, currentPoint)
                    previousPoint = currentPoint
                    change.consume()
                }
            }
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
        ) {
            drawRect(
                brush = Brush.linearGradient(
                    colors = listOf(MintMist, MintMistDeep, Color(0xFFEADBF3))
                )
            )
            revealStrokes.forEach { stroke ->
                drawLine(
                    color = Color.Transparent,
                    start = stroke.start,
                    end = stroke.end,
                    strokeWidth = brushRadius.toPx() * 2f,
                    cap = StrokeCap.Round,
                    blendMode = BlendMode.Clear
                )
            }
        }

        sparklePositions.forEachIndexed { index, (x, y) ->
            FloatingSparkle(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .graphicsLayer {
                        translationX = x * 800
                        translationY = y * 500
                    },
                size = 12.dp + ((index % 3) * 3).dp,
                delay = index * 120
            )
        }
    }
}

@Composable
private fun ConfettiBurst(
    burstId: Int,
    modifier: Modifier = Modifier
) {
    val particles = remember(burstId) {
        List(28) { index ->
            val lane = index % 7
            ConfettiParticle(
                startX = 0.10f + (lane * 0.12f),
                drift = if (index % 2 == 0) -0.08f else 0.08f,
                delay = index * 22,
                color = listOf(
                    Color(0xFFFFB4A0),
                    Color(0xFF90D9A8),
                    Color(0xFFF4A8C4),
                    Color(0xFFFFD966),
                    Color(0xFF8DD6EA)
                )[index % 5],
                size = 8.dp + ((index % 3) * 3).dp
            )
        }
    }
    val progress = remember(burstId) { Animatable(0f) }
    LaunchedEffect(burstId) {
        progress.snapTo(0f)
        progress.animateTo(1f, animationSpec = tween(durationMillis = 1700))
    }

    Box(modifier = modifier) {
        particles.forEach { particle ->
            val localProgress = ((progress.value * 1.15f) - (particle.delay / 1700f)).coerceIn(0f, 1f)
            val alpha = (1f - localProgress).coerceIn(0f, 1f)
            if (alpha > 0.01f) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .graphicsLayer {
                            translationX = (particle.startX + particle.drift * localProgress) * 900f
                            translationY = localProgress * 520f
                            rotationZ = 360f * localProgress
                            this.alpha = alpha
                        }
                        .size(width = particle.size, height = particle.size * 1.4f)
                        .clip(androidx.compose.foundation.shape.RoundedCornerShape(3.dp))
                        .background(particle.color)
                )
            }
        }
    }
}

private data class ConfettiParticle(
    val startX: Float,
    val drift: Float,
    val delay: Int,
    val color: Color,
    val size: Dp
)

@Composable
private fun FloatingSparkle(
    modifier: Modifier = Modifier,
    size: Dp,
    delay: Int
) {
    val transition = rememberInfiniteTransition(label = "sparkle")
    val alpha by transition.animateFloat(
        initialValue = 0.25f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2400, delayMillis = delay),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )
    val scale by transition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2400, delayMillis = delay),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Icon(
        imageVector = Icons.Filled.Star,
        contentDescription = null,
        tint = Color(0xFFFFFBEA),
        modifier = modifier
            .size(size)
            .graphicsLayer(alpha = alpha, scaleX = scale, scaleY = scale)
    )
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

private fun revealCellsForStroke(
    startPoint: androidx.compose.ui.geometry.Offset,
    endPoint: androidx.compose.ui.geometry.Offset,
    containerSize: IntSize,
    brushRadiusPx: Float
): Set<Int> {
    if (containerSize.width == 0 || containerSize.height == 0) {
        return emptySet()
    }

    val columnWidth = containerSize.width.toFloat() / RevealGridColumns
    val rowHeight = containerSize.height.toFloat() / RevealGridRows
    val minX = minOf(startPoint.x, endPoint.x) - brushRadiusPx
    val maxX = maxOf(startPoint.x, endPoint.x) + brushRadiusPx
    val minY = minOf(startPoint.y, endPoint.y) - brushRadiusPx
    val maxY = maxOf(startPoint.y, endPoint.y) + brushRadiusPx
    val minColumn = (minX / columnWidth).toInt().coerceIn(0, RevealGridColumns - 1)
    val maxColumn = (maxX / columnWidth).toInt().coerceIn(0, RevealGridColumns - 1)
    val minRow = (minY / rowHeight).toInt().coerceIn(0, RevealGridRows - 1)
    val maxRow = (maxY / rowHeight).toInt().coerceIn(0, RevealGridRows - 1)

    val revealed = mutableSetOf<Int>()
    for (row in minRow..maxRow) {
        for (column in minColumn..maxColumn) {
            val cellCenter = androidx.compose.ui.geometry.Offset(
                x = (column + 0.5f) * columnWidth,
                y = (row + 0.5f) * rowHeight
            )
            if (distanceToSegment(cellCenter, startPoint, endPoint) <= brushRadiusPx) {
                revealed += (row * RevealGridColumns) + column
            }
        }
    }
    return revealed
}

private fun distanceToSegment(
    point: androidx.compose.ui.geometry.Offset,
    segmentStart: androidx.compose.ui.geometry.Offset,
    segmentEnd: androidx.compose.ui.geometry.Offset
): Float {
    val dx = segmentEnd.x - segmentStart.x
    val dy = segmentEnd.y - segmentStart.y
    if (dx == 0f && dy == 0f) {
        return point.minus(segmentStart).getDistance()
    }

    val t = (((point.x - segmentStart.x) * dx) + ((point.y - segmentStart.y) * dy)) /
        ((dx * dx) + (dy * dy))
    val clampedT = t.coerceIn(0f, 1f)
    val projection = androidx.compose.ui.geometry.Offset(
        x = segmentStart.x + (dx * clampedT),
        y = segmentStart.y + (dy * clampedT)
    )
    return point.minus(projection).getDistance()
}

@Preview(showBackground = true)
@Composable
private fun HomePreview() {
    ColouringBookTheme {
        HomeScreen(categories = homeCategories, onCategoryClick = {})
    }
}
