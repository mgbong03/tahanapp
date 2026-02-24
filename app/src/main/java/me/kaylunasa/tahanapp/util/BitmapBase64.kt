package me.kaylunasa.tahanapp.util

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.ByteArrayOutputStream
import android.util.Base64

fun Bitmap.toBase64(format: Bitmap.CompressFormat = Bitmap.CompressFormat.PNG, quality: Int = 100) : String {
    val byteArrayOutputStream = ByteArrayOutputStream()
    this.compress(format, quality, byteArrayOutputStream)
    val bytes = byteArrayOutputStream.toByteArray()
    return Base64.encodeToString(bytes, Base64.NO_WRAP)
}

fun String.base64ToBitmap(): Bitmap? {
    val bytes = Base64.decode(this, Base64.NO_WRAP)
    return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
}