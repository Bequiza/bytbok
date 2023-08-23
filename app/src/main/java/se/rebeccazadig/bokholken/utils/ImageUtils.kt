package se.rebeccazadig.bokholken.utils

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import java.io.ByteArrayOutputStream

class ImageUtils {

    companion object {
        fun Bitmap.toByteArray(): ByteArray {
            val baos = ByteArrayOutputStream()
            this.compress(Bitmap.CompressFormat.JPEG, 80, baos)
            return baos.toByteArray()
        }

        fun getBitmapFromUri(contentResolver: ContentResolver, uri: Uri): Bitmap? {
            val source = ImageDecoder.createSource(contentResolver, uri)
            return ImageDecoder.decodeBitmap(source)
        }

        fun resizeBitmap(bitmap: Bitmap, newWidth: Int): Bitmap {
            val imageScale = newWidth.toDouble() / bitmap.width
            return Bitmap.createScaledBitmap(
                bitmap,
                newWidth,
                (bitmap.height * imageScale).toInt(),
                false
            )
        }
    }
}