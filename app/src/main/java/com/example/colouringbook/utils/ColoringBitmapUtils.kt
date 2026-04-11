package com.example.colouringbook.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Point
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color as ComposeColor
import androidx.compose.ui.unit.IntSize
import java.util.ArrayDeque
import kotlin.math.abs

fun loadMutableBitmap(
    context: Context,
    @DrawableRes imageRes: Int
): Bitmap {
    return BitmapFactory.decodeResource(context.resources, imageRes)
        .copy(Bitmap.Config.ARGB_8888, true)
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

private fun isFillCandidate(
    @ColorInt color: Int,
    @ColorInt replacementColor: Int
): Boolean {
    return isFillableArea(color) && !colorsAreClose(color, replacementColor)
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
