package com.example.colouringbook

import android.graphics.Point
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Close
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
import com.example.colouringbook.ui.theme.MintLine
import com.example.colouringbook.ui.theme.MintLineStrong
import com.example.colouringbook.ui.theme.MintMist
import com.example.colouringbook.ui.theme.MintMistDeep
import com.example.colouringbook.ui.theme.MintProgressEnd
import com.example.colouringbook.ui.theme.MintProgressStart
import com.example.colouringbook.ui.theme.MintText
import com.example.colouringbook.ui.theme.MintTextSecondary
import com.example.colouringbook.ui.theme.MintTextMuted
import com.example.colouringbook.utils.applyBrushStrokeWithinMask
import com.example.colouringbook.utils.buildFillRegionMask
import com.example.colouringbook.utils.floodFillWithinOutline
import com.example.colouringbook.utils.loadImmutableBitmap
import com.example.colouringbook.utils.loadMutableBitmap
import com.example.colouringbook.utils.mapTapToBitmap
import com.example.colouringbook.utils.restoreBitmapFromSource
import com.example.colouringbook.utils.restoreBrushStrokeWithinMask
import com.example.colouringbook.utils.restoreRegionFromSource
import com.example.colouringbook.utils.toArgbInt
import kotlin.random.Random

private const val BRUSH_THICKNESS_PX = 15
private const val BRUSH_RADIUS_PX = BRUSH_THICKNESS_PX / 2
private val ScreenPaddingHorizontal = 24.dp
private val ScreenPaddingTop = 28.dp
private val ScreenPaddingBottom = 20.dp
private val HomeGridGap = 14.dp
private val CardRadius = 20.dp
private val HeaderBackSize = 76.dp
private val HeaderBackIconSize = 32.dp
private val ToolbarButtonSize = 84.dp
private val ToolbarButtonIconSize = 34.dp
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
                onCategoryClick = {
                    if (it.id == "surprise") {
                        navController.navigate("surprise")
                    } else {
                        navController.navigate("category/${it.id}")
                    }
                }
            )
        }
        composable("surprise") {
            val surpriseCategory = homeCategories.firstOrNull { it.id == "surprise" } ?: homeCategories.first()
            val eligibleCategories = remember {
                homeCategories.filter { category -> category.id != "surprise" }
            }
            val surpriseImage = remember {
                val imagePool = eligibleCategories.flatMap { categoryImages(context, it) }
                imagePool[Random.nextInt(imagePool.size)]
            }

            DrawingScreen(
                category = surpriseCategory,
                image = surpriseImage,
                onBackClick = { navController.popBackStack() }
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
        val columns = 4
        val rowCount = ((categories.size + columns - 1) / columns).coerceAtLeast(1)
        val tileHeight = 252.dp
        val gridHeight = (tileHeight * rowCount) + (HomeGridGap * (rowCount - 1))

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
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(columns),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(gridHeight),
                    horizontalArrangement = Arrangement.spacedBy(HomeGridGap),
                    verticalArrangement = Arrangement.spacedBy(HomeGridGap),
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
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(195.dp)
                        .aspectRatio(1f)
                        .clip(androidx.compose.foundation.shape.CircleShape)
                        .border(3.5.dp, category.borderColor, androidx.compose.foundation.shape.CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    AppImage(
                        imageSource = category.tileImageSource,
                        contentDescription = category.title,
                        modifier = Modifier.fillMaxSize(0.9f),
                        contentScale = ContentScale.Fit
                    )
                }
            }
            Spacer(modifier = Modifier.size(8.dp))
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
            eyebrow = "Category",
            title = category.title,
            onBackClick = onBackClick
        )
        Text(
            text = "Choose a picture to colour",
            style = MaterialTheme.typography.bodyMedium,
            color = MintTextMuted,
            modifier = Modifier.padding(start = 86.dp, end = 24.dp, bottom = 8.dp)
        )
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(top = 4.dp, bottom = 14.dp)
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
    eyebrow: String? = null,
    title: String,
    onBackClick: () -> Unit,
    centerContent: @Composable (() -> Unit)? = null,
    actions: @Composable androidx.compose.foundation.layout.RowScope.() -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 24.dp, end = 24.dp, top = 14.dp, bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Surface(
            modifier = Modifier
                .size(HeaderBackSize)
                .clickable(onClick = onBackClick),
            color = MintCard,
            shape = androidx.compose.foundation.shape.CircleShape,
            border = androidx.compose.foundation.BorderStroke(1.5.dp, MintLineStrong)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = MintText,
                    modifier = Modifier.size(HeaderBackIconSize)
                )
            }
        }
        if (centerContent == null) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                if (eyebrow != null) {
                    Text(
                        text = eyebrow.uppercase(),
                        style = MaterialTheme.typography.labelLarge,
                        color = MintTextMuted
                    )
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    color = MintText
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), content = actions)
        } else {
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                centerContent()
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), content = actions)
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
            .aspectRatio(1.22f)
            .clickable(onClick = onClick),
        color = MintCard,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp),
        border = androidx.compose.foundation.BorderStroke(2.5.dp, image.borderColor),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            AppImage(
                imageSource = image.imageSource,
                contentDescription = image.name,
                modifier = Modifier.fillMaxSize(0.8f),
                contentScale = ContentScale.Fit
            )
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
    var isColorPickerOpen by remember(image.id) { mutableStateOf(false) }
    var pendingSelectedColor by remember(image.id) { mutableStateOf(selectedColor) }
    val revealStrokes = remember(image.id) { mutableStateListOf<RevealStroke>() }
    var revealedCells by remember(image.id) { mutableStateOf(setOf<Int>()) }
    var activeRegionMask by remember(image.id) { mutableStateOf<BooleanArray?>(null) }
    var previousDrawPoint by remember(image.id) { mutableStateOf<Point?>(null) }
    var isDrawing by remember(image.id) { mutableStateOf(false) }
    var hasShownUnlockMessage by remember(image.id) { mutableStateOf(false) }
    var confettiBurstId by remember(image.id) { mutableStateOf(0) }

    val usesMagicMist = category.id == "surprise"
    val revealBrushRadiusPx = with(density) { RevealBrushRadius.toPx() }
    val revealProgress = revealedCells.size.toFloat() / (RevealGridColumns * RevealGridRows).toFloat()
    val isRevealComplete = !usesMagicMist || revealProgress >= RevealCompletionRatio
    val imageBitmap = remember(bitmapVersion, colouringBitmap) { colouringBitmap.asImageBitmap() }

    LaunchedEffect(usesMagicMist, isRevealComplete) {
        if (usesMagicMist && isRevealComplete) {
            hasShownUnlockMessage = true
            confettiBurstId++
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MintBackground)
            .padding(bottom = 14.dp)
    ) {
        if (usesMagicMist && !isRevealComplete) {
            MistHintRow(onBackClick = onBackClick)
        } else {
            ColoringToolbar(
                selectedTool = selectedTool,
                onToolSelected = { selectedTool = it },
                onBackClick = onBackClick,
                onClearClick = {
                    if (restoreBitmapFromSource(colouringBitmap, outlineBitmap)) {
                        bitmapVersion++
                    }
                },
                modifier = Modifier.padding(top = 14.dp, bottom = 10.dp)
            )
        }

        if (usesMagicMist && isRevealComplete && hasShownUnlockMessage) {
            UnlockToast("Picture unlocked! Now choose a colour and start drawing!")
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
        ) {
            if (usesMagicMist && confettiBurstId > 0) {
                ConfettiBurst(
                    burstId = confettiBurstId,
                    modifier = Modifier.fillMaxSize()
                )
            }

            if (usesMagicMist && !isRevealComplete) {
                Column(modifier = Modifier.fillMaxSize()) {
                    DrawingCanvasCard(
                        modifier = Modifier.weight(1f),
                        imageBitmap = imageBitmap,
                        imageName = image.name,
                        isRevealComplete = false,
                        imageContainerSize = imageContainerSize,
                        onSizeChanged = { imageContainerSize = it },
                        selectedColor = selectedColor,
                        revealStrokes = revealStrokes,
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
                    RevealProgressBar(
                        progress = revealProgress,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    DrawingPaletteSidebar(
                        selectedColor = selectedColor,
                        colors = pastelPalette.take(5),
                        onColorSelected = { selectedColor = it },
                        onMoreColorsClick = {
                            pendingSelectedColor = selectedColor
                            isColorPickerOpen = true
                        }
                    )
                    DrawingCanvasCard(
                        modifier = Modifier.weight(1f),
                        imageBitmap = imageBitmap,
                        imageName = image.name,
                        isRevealComplete = true,
                        imageContainerSize = imageContainerSize,
                        onSizeChanged = { imageContainerSize = it },
                        selectedTool = selectedTool,
                        selectedColor = selectedColor,
                        colouringBitmap = colouringBitmap,
                        outlineBitmap = outlineBitmap,
                        activeRegionMask = activeRegionMask,
                        previousDrawPoint = previousDrawPoint,
                        onActiveRegionMaskChange = { activeRegionMask = it },
                        onPreviousDrawPointChange = { previousDrawPoint = it },
                        onDrawingChange = { isDrawing = it },
                        onBitmapChanged = { bitmapVersion++ }
                    )
                }

                if (isColorPickerOpen) {
                    ExpandedColorPicker(
                        selectedColor = pendingSelectedColor,
                        colors = pastelPalette,
                        onColorSelected = { pendingSelectedColor = it },
                        onApply = {
                            selectedColor = pendingSelectedColor
                            isColorPickerOpen = false
                        },
                        onDismiss = {
                            pendingSelectedColor = selectedColor
                            isColorPickerOpen = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun MistHintRow(
    onBackClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 24.dp, end = 24.dp, top = 14.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Surface(
            modifier = Modifier
                .size(HeaderBackSize)
                .clickable(onClick = onBackClick),
            color = MintCard,
            shape = androidx.compose.foundation.shape.CircleShape,
            border = BorderStroke(1.5.dp, MintLineStrong)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = MintText,
                    modifier = Modifier.size(HeaderBackIconSize)
                )
            }
        }
        Text(text = "\u2728", style = MaterialTheme.typography.titleMedium, color = MintTextSecondary)
        Text(
            text = "Swipe to wipe away the magic mist!",
            style = MaterialTheme.typography.bodyLarge,
            color = MintTextSecondary
        )
    }
}

@Composable
private fun DrawingCanvasCard(
    modifier: Modifier = Modifier,
    imageBitmap: androidx.compose.ui.graphics.ImageBitmap,
    imageName: String,
    isRevealComplete: Boolean,
    imageContainerSize: IntSize,
    onSizeChanged: (IntSize) -> Unit,
    selectedColor: Color,
    revealStrokes: List<RevealStroke> = emptyList(),
    onRevealStroke: ((androidx.compose.ui.geometry.Offset, androidx.compose.ui.geometry.Offset) -> Unit)? = null,
    selectedTool: ColoringTool = ColoringTool.Brush,
    colouringBitmap: android.graphics.Bitmap? = null,
    outlineBitmap: android.graphics.Bitmap? = null,
    activeRegionMask: BooleanArray? = null,
    previousDrawPoint: Point? = null,
    onActiveRegionMaskChange: (BooleanArray?) -> Unit = {},
    onPreviousDrawPointChange: (Point?) -> Unit = {},
    onDrawingChange: (Boolean) -> Unit = {},
    onBitmapChanged: () -> Unit = {}
) {
    Surface(
        modifier = Modifier
            .then(modifier)
            .fillMaxHeight(),
        color = MintCard,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(CardRadius),
        border = BorderStroke(1.5.dp, MintLine)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .onSizeChanged(onSizeChanged)
                .pointerInput(
                    selectedTool,
                    selectedColor,
                    imageContainerSize,
                    isRevealComplete,
                    colouringBitmap,
                    outlineBitmap
                ) {
                    if (!isRevealComplete || colouringBitmap == null || outlineBitmap == null) {
                        return@pointerInput
                    }

                    var currentRegionMask: BooleanArray? = null
                    var currentPreviousDrawPoint: Point? = null

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
                                onBitmapChanged()
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

                                currentRegionMask = regionMask
                                currentPreviousDrawPoint = startPoint
                                onActiveRegionMaskChange(regionMask)
                                onPreviousDrawPointChange(startPoint)
                                onDrawingChange(true)

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
                                    onBitmapChanged()
                                }
                            },
                            onDragEnd = {
                                currentRegionMask = null
                                currentPreviousDrawPoint = null
                                onActiveRegionMaskChange(null)
                                onPreviousDrawPointChange(null)
                                onDrawingChange(false)
                            },
                            onDragCancel = {
                                currentRegionMask = null
                                currentPreviousDrawPoint = null
                                onActiveRegionMaskChange(null)
                                onPreviousDrawPointChange(null)
                                onDrawingChange(false)
                            }
                        ) { change, _ ->
                            val regionMask = currentRegionMask ?: return@detectDragGestures
                            val lastPoint = currentPreviousDrawPoint ?: return@detectDragGestures
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
                                onBitmapChanged()
                            }

                            currentPreviousDrawPoint = mappedPoint
                            onPreviousDrawPointChange(mappedPoint)
                            change.consume()
                        }
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Image(
                bitmap = imageBitmap,
                contentDescription = imageName,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(28.dp),
                contentScale = ContentScale.Fit
            )

            if (!isRevealComplete && onRevealStroke != null) {
                RevealOverlay(
                    revealStrokes = revealStrokes,
                    brushRadius = RevealBrushRadius,
                    onRevealStroke = onRevealStroke
                )
            }
        }
    }
}

@Composable
private fun DrawingPaletteSidebar(
    selectedColor: Color,
    colors: List<Color>,
    onColorSelected: (Color) -> Unit,
    onMoreColorsClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .width(92.dp)
            .fillMaxHeight(),
        color = MintCard,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(26.dp),
        border = BorderStroke(1.5.dp, MintLine)
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp, vertical = 12.dp)
        ) {
            val selectedSize = 48.dp
            val plusSize = 56.dp
            val spacing = 12.dp
            val availableHeight = maxHeight - selectedSize - plusSize - 32.dp - (spacing * (colors.size - 1))
            val swatchSize = (availableHeight / colors.size).coerceIn(44.dp, 62.dp)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 2.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(selectedSize)
                        .clip(androidx.compose.foundation.shape.CircleShape)
                        .background(selectedColor)
                        .border(3.dp, Color.White, androidx.compose.foundation.shape.CircleShape)
                        .border(2.5.dp, MintLineStrong, androidx.compose.foundation.shape.CircleShape)
                )
                Spacer(modifier = Modifier.height(16.dp))
                colors.forEachIndexed { index, color ->
                    PaletteSwatch(
                        color = color,
                        selected = selectedColor == color,
                        size = swatchSize,
                        onClick = { onColorSelected(color) }
                    )
                    if (index != colors.lastIndex) {
                        Spacer(modifier = Modifier.height(spacing))
                    }
                }
                Spacer(modifier = Modifier.weight(1f))
                PlusColorButton(
                    size = plusSize,
                    onClick = onMoreColorsClick
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
            .border(
                width = if (selected) 2.5.dp else 0.dp,
                color = MintLineStrong,
                shape = androidx.compose.foundation.shape.CircleShape
            )
            .padding(if (selected) 4.dp else 0.dp)
            .size(size - if (selected) 8.dp else 0.dp)
            .clip(androidx.compose.foundation.shape.CircleShape)
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .padding(top = 5.dp)
                .clip(androidx.compose.foundation.shape.CircleShape)
                .background(color.copy(alpha = 0.22f))
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(androidx.compose.foundation.shape.CircleShape)
                .background(color)
                .border(
                    width = if (selected) 2.5.dp else if (color == Color.White) 1.5.dp else 0.dp,
                    color = if (selected) Color.White else MintLineStrong,
                    shape = androidx.compose.foundation.shape.CircleShape
                )
        )
    }
}

@Composable
private fun PlusColorButton(
    size: Dp,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .size(size)
            .clickable(onClick = onClick),
        color = MintBackgroundSoft,
        shape = androidx.compose.foundation.shape.CircleShape,
        border = BorderStroke(1.5.dp, MintLine)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = "More colors",
                tint = MintTextSecondary,
                modifier = Modifier.size(size * 0.48f)
            )
        }
    }
}

@Composable
private fun ExpandedColorPicker(
    selectedColor: Color,
    colors: List<Color>,
    onColorSelected: (Color) -> Unit,
    onApply: () -> Unit,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0x4DFFFFFF))
            .clickable(onClick = onDismiss),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .width(520.dp)
                .clickable(enabled = false, onClick = {}),
            color = MintCard,
            shape = androidx.compose.foundation.shape.RoundedCornerShape(32.dp),
            border = BorderStroke(1.5.dp, MintLine)
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 28.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(84.dp)
                        .clip(androidx.compose.foundation.shape.CircleShape)
                        .background(selectedColor)
                        .border(4.dp, Color.White, androidx.compose.foundation.shape.CircleShape)
                        .border(2.5.dp, MintLineStrong, androidx.compose.foundation.shape.CircleShape)
                )
                Spacer(modifier = Modifier.height(22.dp))
                repeat(4) { rowIndex ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(18.dp),
                        modifier = Modifier.padding(bottom = if (rowIndex == 3) 0.dp else 18.dp)
                    ) {
                        repeat(5) { columnIndex ->
                            val colorIndex = rowIndex * 5 + columnIndex
                            val color = colors[colorIndex]
                            PaletteSwatch(
                                color = color,
                                selected = selectedColor == color,
                                size = 64.dp,
                                onClick = { onColorSelected(color) }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(26.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    PickerActionButton(
                        icon = Icons.Filled.Check,
                        containerColor = Color(0xFFE3F7E9),
                        iconTint = Color(0xFF2D7A44),
                        onClick = onApply
                    )
                    PickerActionButton(
                        icon = Icons.Filled.Close,
                        containerColor = Color(0xFFFFECEC),
                        iconTint = Color(0xFFD94A4A),
                        onClick = onDismiss
                    )
                }
            }
        }
    }
}

@Composable
private fun PickerActionButton(
    icon: ImageVector,
    containerColor: Color,
    iconTint: Color,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .size(ToolbarButtonSize)
            .clickable(onClick = onClick),
        color = containerColor,
        shape = androidx.compose.foundation.shape.CircleShape,
        border = BorderStroke(1.5.dp, MintLine)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(ToolbarButtonIconSize)
            )
        }
    }
}

@Composable
private fun ColoringToolbar(
    selectedTool: ColoringTool,
    onToolSelected: (ColoringTool) -> Unit,
    onBackClick: () -> Unit,
    onClearClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        ToolbarCircleButton(
            icon = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Back",
            onClick = onBackClick
        )

        ToolToggleGroup(
            selectedTool = selectedTool,
            onToolSelected = onToolSelected
        )

        HeaderIconButton(
            icon = Icons.Filled.CleaningServices,
            contentDescription = "Clear drawing",
            onClick = onClearClick,
            size = HeaderBackSize
        )
    }
}

@Composable
private fun ToolToggleGroup(
    selectedTool: ColoringTool,
    onToolSelected: (ColoringTool) -> Unit
) {
    Row(
        modifier = Modifier
            .clip(androidx.compose.foundation.shape.RoundedCornerShape(999.dp))
            .background(MintBackgroundSoft)
            .border(BorderStroke(1.5.dp, MintLine), androidx.compose.foundation.shape.RoundedCornerShape(999.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        listOf(
            ColoringTool.Brush to Icons.Filled.Brush,
            ColoringTool.Fill to Icons.Filled.FormatColorFill
        ).forEach { (tool, icon) ->
            ToolButton(
                label = tool.name,
                icon = icon,
                selected = selectedTool == tool,
                onClick = { onToolSelected(tool) }
            )
        }
    }
}

@Composable
private fun ToolButton(
    label: String,
    icon: ImageVector?,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .size(ToolbarButtonSize)
            .clickable(onClick = onClick),
        color = if (selected) Color.White else Color.Transparent,
        shape = androidx.compose.foundation.shape.CircleShape
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = if (selected) MintText else MintTextMuted,
                    modifier = Modifier.size(ToolbarButtonIconSize)
                )
            } else {
                Text(
                    text = "⌫",
                    color = if (selected) MintText else MintTextMuted,
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.scale(1.05f)
                )
            }
        }
    }
}

@Composable
private fun HeaderIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    size: Dp = HeaderBackSize
) {
    ToolbarCircleButton(
        icon = icon,
        contentDescription = contentDescription,
        onClick = onClick,
        size = size
    )
}

@Composable
private fun ToolbarCircleButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    size: Dp = HeaderBackSize
) {
    Surface(
        modifier = Modifier
            .size(size)
            .clickable(onClick = onClick),
        color = MintCard,
        shape = androidx.compose.foundation.shape.CircleShape,
        border = BorderStroke(1.5.dp, MintLineStrong)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = MintTextSecondary,
                modifier = Modifier.size(if (size >= HeaderBackSize) HeaderBackIconSize else 18.dp)
            )
        }
    }
}

@Composable
private fun UnlockToast(text: String) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 24.dp, end = 24.dp, bottom = 10.dp),
        color = Color(0xFFE3F7E9),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MintProgressStart)
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
private fun RevealProgressBar(
    progress: Float,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(6.dp)
            .clip(androidx.compose.foundation.shape.RoundedCornerShape(999.dp))
            .background(MintLine)
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
}

@Composable
private fun HintBar(text: String) {
    Row(
        modifier = Modifier
            .defaultMinSize(minHeight = 0.dp)
            .widthIn(min = 0.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(text = "\u2728", color = MintTextSecondary)
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MintTextSecondary
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
