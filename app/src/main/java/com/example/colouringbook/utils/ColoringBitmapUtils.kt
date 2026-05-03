package com.example.colouringbook.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Point
import androidx.annotation.ColorInt
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color as ComposeColor
import androidx.compose.ui.unit.IntSize
import com.example.colouringbook.data.ImageSource
import java.util.ArrayDeque
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.sqrt

fun loadMutableBitmap(
    context: Context,
    imageSource: ImageSource
): Bitmap {
    val bitmap = when {
        imageSource.drawableRes != null -> BitmapFactory.decodeResource(context.resources, imageSource.drawableRes)
        imageSource.assetPath != null -> context.assets.open(imageSource.assetPath).use(BitmapFactory::decodeStream)
        else -> null
    } ?: error("ImageSource must provide either drawableRes or assetPath")

    return bitmap.copy(Bitmap.Config.ARGB_8888, true)
}

fun loadImmutableBitmap(
    context: Context,
    imageSource: ImageSource
): Bitmap {
    return when {
        imageSource.drawableRes != null -> BitmapFactory.decodeResource(context.resources, imageSource.drawableRes)
        imageSource.assetPath != null -> context.assets.open(imageSource.assetPath).use(BitmapFactory::decodeStream)
        else -> null
    } ?: error("ImageSource must provide either drawableRes or assetPath")
}

fun mapTapToBitmap(
    tapOffset: Offset,
    containerSize: IntSize,
    bitmapWidth: Int,
    bitmapHeight: Int
): Point? {
    if (containerSize.width == 0 || containerSize.height == 0) return null

    val widthScale = containerSize.width.toFloat() / bitmapWidth
    val heightScale = containerSize.height.toFloat() / bitmapHeight
    val scale = minOf(widthScale, heightScale)

    val renderedWidth = bitmapWidth * scale
    val renderedHeight = bitmapHeight * scale
    val left = (containerSize.width - renderedWidth) / 2f
    val top = (containerSize.height - renderedHeight) / 2f

    if (tapOffset.x !in left..(left + renderedWidth) || tapOffset.y !in top..(top + renderedHeight)) {
        return null
    }

    val bitmapX = ((tapOffset.x - left) / scale).toInt().coerceIn(0, bitmapWidth - 1)
    val bitmapY = ((tapOffset.y - top) / scale).toInt().coerceIn(0, bitmapHeight - 1)

    return Point(bitmapX, bitmapY)
}

fun floodFillWithinOutline(
    bitmap: Bitmap,
    startX: Int,
    startY: Int,
    @ColorInt replacementColor: Int
): Boolean {
    val width = bitmap.width
    val height = bitmap.height
    val pixels = IntArray(width * height)
    bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

    val startIndex = startY * width + startX
    val startColor = pixels[startIndex]
    if (!isFillableArea(startColor) || colorsAreClose(startColor, replacementColor)) {
        return false
    }

    val queue = ArrayDeque<Point>()
    queue.add(Point(startX, startY))
    var changed = false

    while (queue.isNotEmpty()) {
        val point = queue.removeFirst()
        val y = point.y
        if (y !in 0 until height) continue

        var left = point.x
        while (left >= 0 && isFillCandidate(pixels[y * width + left], replacementColor)) {
            left--
        }
        left++

        var right = point.x
        while (right < width && isFillCandidate(pixels[y * width + right], replacementColor)) {
            right++
        }
        right--

        if (left > right) continue

        for (x in left..right) {
            pixels[y * width + x] = replacementColor
            changed = true
        }

        enqueueAdjacentRuns(
            pixels = pixels,
            width = width,
            height = height,
            y = y - 1,
            left = left,
            right = right,
            replacementColor = replacementColor,
            queue = queue
        )
        enqueueAdjacentRuns(
            pixels = pixels,
            width = width,
            height = height,
            y = y + 1,
            left = left,
            right = right,
            replacementColor = replacementColor,
            queue = queue
        )
    }

    if (changed) {
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height)
    }

    return changed
}

fun buildFillRegionMask(
    bitmap: Bitmap,
    startX: Int,
    startY: Int
): BooleanArray? {
    val width = bitmap.width
    val height = bitmap.height
    val pixels = IntArray(width * height)
    bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

    val startIndex = startY * width + startX
    if (!isFillableArea(pixels[startIndex])) {
        return null
    }

    val mask = BooleanArray(width * height)
    val queue = ArrayDeque<Point>()
    queue.add(Point(startX, startY))

    while (queue.isNotEmpty()) {
        val point = queue.removeFirst()
        val y = point.y
        if (y !in 0 until height) continue

        var left = point.x
        while (left >= 0 && isMaskCandidate(pixels[y * width + left], mask[y * width + left])) {
            left--
        }
        left++

        var right = point.x
        while (right < width && isMaskCandidate(pixels[y * width + right], mask[y * width + right])) {
            right++
        }
        right--

        if (left > right) continue

        for (x in left..right) {
            mask[y * width + x] = true
        }

        enqueueMaskRuns(pixels, mask, width, height, y - 1, left, right, queue)
        enqueueMaskRuns(pixels, mask, width, height, y + 1, left, right, queue)
    }

    return mask
}

fun applyBrushStrokeWithinMask(
    bitmap: Bitmap,
    regionMask: BooleanArray,
    startX: Int,
    startY: Int,
    endX: Int,
    endY: Int,
    @ColorInt replacementColor: Int,
    brushRadiusPx: Int
): Boolean {
    var changed = false
    val deltaX = (endX - startX).toFloat()
    val deltaY = (endY - startY).toFloat()
    val steps = maxOf(1, ceil(sqrt(deltaX * deltaX + deltaY * deltaY).toDouble()).toInt())

    for (step in 0..steps) {
        val fraction = step / steps.toFloat()
        val x = (startX + deltaX * fraction).toInt()
        val y = (startY + deltaY * fraction).toInt()
        if (paintBrushDot(bitmap, regionMask, x, y, replacementColor, brushRadiusPx)) {
            changed = true
        }
    }

    return changed
}

fun restoreRegionFromSource(
    targetBitmap: Bitmap,
    sourceBitmap: Bitmap,
    startX: Int,
    startY: Int
): Boolean {
    val regionMask = buildFillRegionMask(sourceBitmap, startX, startY) ?: return false
    return restoreMaskedPixels(
        targetBitmap = targetBitmap,
        sourceBitmap = sourceBitmap,
        regionMask = regionMask
    )
}

fun restoreBrushStrokeWithinMask(
    targetBitmap: Bitmap,
    sourceBitmap: Bitmap,
    regionMask: BooleanArray,
    startX: Int,
    startY: Int,
    endX: Int,
    endY: Int,
    brushRadiusPx: Int
): Boolean {
    var changed = false
    val deltaX = (endX - startX).toFloat()
    val deltaY = (endY - startY).toFloat()
    val steps = maxOf(1, ceil(sqrt(deltaX * deltaX + deltaY * deltaY).toDouble()).toInt())

    for (step in 0..steps) {
        val fraction = step / steps.toFloat()
        val x = (startX + deltaX * fraction).toInt()
        val y = (startY + deltaY * fraction).toInt()
        if (restoreBrushDot(targetBitmap, sourceBitmap, regionMask, x, y, brushRadiusPx)) {
            changed = true
        }
    }

    return changed
}

fun restoreBitmapFromSource(
    targetBitmap: Bitmap,
    sourceBitmap: Bitmap
): Boolean {
    val width = minOf(targetBitmap.width, sourceBitmap.width)
    val height = minOf(targetBitmap.height, sourceBitmap.height)
    var changed = false

    for (y in 0 until height) {
        for (x in 0 until width) {
            val sourceColor = sourceBitmap.getPixel(x, y)
            if (targetBitmap.getPixel(x, y) == sourceColor) continue
            targetBitmap.setPixel(x, y, sourceColor)
            changed = true
        }
    }

    return changed
}

private fun enqueueAdjacentRuns(
    pixels: IntArray,
    width: Int,
    height: Int,
    y: Int,
    left: Int,
    right: Int,
    @ColorInt replacementColor: Int,
    queue: ArrayDeque<Point>
) {
    if (y !in 0 until height) return

    var x = left
    while (x <= right) {
        while (x <= right && !isFillCandidate(pixels[y * width + x], replacementColor)) {
            x++
        }
        if (x > right) break

        queue.add(Point(x, y))

        while (x <= right && isFillCandidate(pixels[y * width + x], replacementColor)) {
            x++
        }
    }
}

private fun enqueueMaskRuns(
    pixels: IntArray,
    mask: BooleanArray,
    width: Int,
    height: Int,
    y: Int,
    left: Int,
    right: Int,
    queue: ArrayDeque<Point>
) {
    if (y !in 0 until height) return

    var x = left
    while (x <= right) {
        while (x <= right && !isMaskCandidate(pixels[y * width + x], mask[y * width + x])) {
            x++
        }
        if (x > right) break

        queue.add(Point(x, y))

        while (x <= right && isMaskCandidate(pixels[y * width + x], mask[y * width + x])) {
            x++
        }
    }
}

private fun paintBrushDot(
    bitmap: Bitmap,
    regionMask: BooleanArray,
    centerX: Int,
    centerY: Int,
    @ColorInt replacementColor: Int,
    brushRadiusPx: Int
): Boolean {
    val width = bitmap.width
    val height = bitmap.height
    val radiusSquared = brushRadiusPx * brushRadiusPx
    var changed = false

    for (y in (centerY - brushRadiusPx)..(centerY + brushRadiusPx)) {
        if (y !in 0 until height) continue
        for (x in (centerX - brushRadiusPx)..(centerX + brushRadiusPx)) {
            if (x !in 0 until width) continue

            val dx = x - centerX
            val dy = y - centerY
            if (dx * dx + dy * dy > radiusSquared) continue

            val index = y * width + x
            if (!regionMask[index]) continue

            val currentColor = bitmap.getPixel(x, y)
            if (colorsAreClose(currentColor, replacementColor)) continue

            bitmap.setPixel(x, y, replacementColor)
            changed = true
        }
    }

    return changed
}

private fun restoreMaskedPixels(
    targetBitmap: Bitmap,
    sourceBitmap: Bitmap,
    regionMask: BooleanArray
): Boolean {
    val width = minOf(targetBitmap.width, sourceBitmap.width)
    val height = minOf(targetBitmap.height, sourceBitmap.height)
    var changed = false

    for (y in 0 until height) {
        for (x in 0 until width) {
            val index = y * width + x
            if (!regionMask[index]) continue
            val sourceColor = sourceBitmap.getPixel(x, y)
            if (targetBitmap.getPixel(x, y) == sourceColor) continue
            targetBitmap.setPixel(x, y, sourceColor)
            changed = true
        }
    }

    return changed
}

private fun restoreBrushDot(
    targetBitmap: Bitmap,
    sourceBitmap: Bitmap,
    regionMask: BooleanArray,
    centerX: Int,
    centerY: Int,
    brushRadiusPx: Int
): Boolean {
    val width = minOf(targetBitmap.width, sourceBitmap.width)
    val height = minOf(targetBitmap.height, sourceBitmap.height)
    val radiusSquared = brushRadiusPx * brushRadiusPx
    var changed = false

    for (y in (centerY - brushRadiusPx)..(centerY + brushRadiusPx)) {
        if (y !in 0 until height) continue
        for (x in (centerX - brushRadiusPx)..(centerX + brushRadiusPx)) {
            if (x !in 0 until width) continue

            val dx = x - centerX
            val dy = y - centerY
            if (dx * dx + dy * dy > radiusSquared) continue

            val index = y * width + x
            if (!regionMask[index]) continue

            val sourceColor = sourceBitmap.getPixel(x, y)
            if (targetBitmap.getPixel(x, y) == sourceColor) continue

            targetBitmap.setPixel(x, y, sourceColor)
            changed = true
        }
    }

    return changed
}

private fun isFillCandidate(
    @ColorInt color: Int,
    @ColorInt replacementColor: Int
): Boolean {
    return isFillableArea(color) && !colorsAreClose(color, replacementColor)
}

private fun isMaskCandidate(
    @ColorInt color: Int,
    alreadyInMask: Boolean
): Boolean {
    return !alreadyInMask && isFillableArea(color)
}

private fun isFillableArea(@ColorInt color: Int): Boolean {
    val alpha = Color.alpha(color)
    if (alpha < 16) return true

    val red = Color.red(color)
    val green = Color.green(color)
    val blue = Color.blue(color)
    val brightness = (red + green + blue) / 3

    return brightness > 208
}

private fun colorsAreClose(@ColorInt first: Int, @ColorInt second: Int): Boolean {
    val redDiff = abs(Color.red(first) - Color.red(second))
    val greenDiff = abs(Color.green(first) - Color.green(second))
    val blueDiff = abs(Color.blue(first) - Color.blue(second))
    return redDiff + greenDiff + blueDiff < 24
}

fun ComposeColor.toArgbInt(): Int {
    return Color.argb(
        (alpha * 255).toInt(),
        (red * 255).toInt(),
        (green * 255).toInt(),
        (blue * 255).toInt()
    )
}
