package me.kaylunasa.tahanapp.util

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import androidx.core.graphics.createBitmap
import androidx.core.graphics.scale

fun Bitmap?.getScaledBitmap(maxSize: Int) : Bitmap? {
    this ?: return null

    val width = this.width
    val height = this.height

    val ratio: Float = maxSize.toFloat() / width.coerceAtMost(height);

    val newWidth = (width * ratio).toInt()
    val newHeight = (height * ratio).toInt()

    val output: Bitmap = createBitmap(maxSize, maxSize, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(output)

    val left: Float = (maxSize - newWidth) / 2f
    val top: Float = (maxSize - newHeight) / 2f

    val matrix = Matrix()
    matrix.setScale(ratio, ratio)
    matrix.postTranslate(left, top)

    canvas.drawBitmap(this, matrix, null)
    return output
}