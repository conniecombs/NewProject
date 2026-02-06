package com.nutritiontracker.util

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.content.Context
import android.util.Base64
import androidx.exifinterface.media.ExifInterface
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

object ImageProcessor {

    private const val MAX_DIMENSION = 1024
    private const val JPEG_QUALITY = 80

    /**
     * Process image from URI: strip metadata, correct rotation, resize, compress.
     * Returns base64-encoded JPEG string ready for API upload.
     */
    fun processImageFromUri(context: Context, uri: Uri): String? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream.close()

            // Try to read EXIF for rotation correction
            val rotatedBitmap = try {
                val exifStream = context.contentResolver.openInputStream(uri) ?: return@try bitmap
                val exif = ExifInterface(exifStream)
                val rotation = exifOrientationToDegrees(
                    exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
                )
                exifStream.close()
                rotateBitmap(bitmap, rotation)
            } catch (e: Exception) {
                bitmap
            }

            processAndEncode(rotatedBitmap)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Process image from file: strip metadata, correct rotation, resize, compress.
     * Returns base64-encoded JPEG string ready for API upload.
     */
    fun processImageFromFile(file: File): String? {
        return try {
            val bitmap = BitmapFactory.decodeFile(file.absolutePath) ?: return null

            // Read EXIF for rotation
            val exif = ExifInterface(file.absolutePath)
            val rotation = exifOrientationToDegrees(
                exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
            )
            val rotatedBitmap = rotateBitmap(bitmap, rotation)

            processAndEncode(rotatedBitmap)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Save a clean copy of the image with all metadata stripped.
     * Returns the path to the clean file.
     */
    fun saveCleanImage(context: Context, uri: Uri): File? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream.close()

            val cleanFile = File(context.filesDir, "food_images/${System.currentTimeMillis()}.jpg")
            cleanFile.parentFile?.mkdirs()

            FileOutputStream(cleanFile).use { out ->
                val scaled = scaleBitmap(bitmap)
                scaled.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, out)
            }

            // The re-saved JPEG has no EXIF metadata by default
            cleanFile
        } catch (e: Exception) {
            null
        }
    }

    private fun processAndEncode(bitmap: Bitmap): String {
        val scaled = scaleBitmap(bitmap)
        val outputStream = ByteArrayOutputStream()
        scaled.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, outputStream)
        return Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
    }

    private fun scaleBitmap(bitmap: Bitmap): Bitmap {
        if (bitmap.width <= MAX_DIMENSION && bitmap.height <= MAX_DIMENSION) {
            return bitmap
        }
        val scale = MAX_DIMENSION.toFloat() / maxOf(bitmap.width, bitmap.height)
        return Bitmap.createScaledBitmap(
            bitmap,
            (bitmap.width * scale).toInt(),
            (bitmap.height * scale).toInt(),
            true
        )
    }

    private fun rotateBitmap(bitmap: Bitmap, degrees: Int): Bitmap {
        if (degrees == 0) return bitmap
        val matrix = Matrix().apply { postRotate(degrees.toFloat()) }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    private fun exifOrientationToDegrees(orientation: Int): Int {
        return when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90
            ExifInterface.ORIENTATION_ROTATE_180 -> 180
            ExifInterface.ORIENTATION_ROTATE_270 -> 270
            else -> 0
        }
    }
}
