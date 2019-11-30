package com.nostereal.qrdetector.viewmodels

import android.app.Application
import android.content.Intent
import android.graphics.*
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Parcelable
import android.util.Log
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.drawable.toDrawable
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.tasks.Task
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetectorOptions
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import kotlin.math.sqrt

class QrViewModel @Inject constructor(private val context: Application) :
    AndroidViewModel(context) {

    private var lastUri: Uri? = null
    private var drawableWithDetectedQrs: Drawable? = null
    var sortedBarcodes: List<FirebaseVisionBarcode>? = null
        private set

    suspend fun getDrawableFromIntent(intent: Intent): Drawable? = withContext(Dispatchers.IO) {
        (intent.getParcelableExtra<Parcelable>(Intent.EXTRA_STREAM) as? Uri).let { uri ->
            Log.d("M_QrViewModel", "Uri from intent: $uri")
            if (uri == null || uri == lastUri) return@withContext null

            lastUri = uri

            val inputStream = context.contentResolver.openInputStream(uri)
            return@withContext Drawable.createFromStream(inputStream, uri.toString())
        }
    }

    suspend fun getDrawableWithDetectedQrs(drawable: Drawable): Drawable? {
        tryDetectQrCodesOnDrawable(drawable)
        Log.d("M_QrViewModel", "drawable with detected qrs = $drawableWithDetectedQrs")
        return drawableWithDetectedQrs
    }


    private suspend fun tryDetectQrCodesOnDrawable(drawable: Drawable) =
        withContext(Dispatchers.Default) {
            val bitmapQr = drawable.toBitmap()
            val image = FirebaseVisionImage.fromBitmap(bitmapQr)
            val options = FirebaseVisionBarcodeDetectorOptions.Builder()
                .setBarcodeFormats(FirebaseVisionBarcode.FORMAT_QR_CODE)
                .build()
            val detector =
                FirebaseVision.getInstance().getVisionBarcodeDetector(options)

            suspendCoroutine<Drawable?> { continuation ->
                detector.detectInImage(image)
                    .addOnSuccessListener { barcodes ->
                        val boundsList = mutableListOf<Rect?>()
                        val sortedBarcodes =
                            barcodes.sortedWith(compareBy { barcode ->
                                val upperLeftCorner = barcode.cornerPoints!!.filterNotNull()[0]
                                sqrt(
                                    (upperLeftCorner.x * upperLeftCorner.x +
                                            upperLeftCorner.y * upperLeftCorner.y).toFloat()
                                )
                            })

                        sortedBarcodes.forEach { barcode ->
                            boundsList.add(barcode.boundingBox)
                            val rawValue = barcode.rawValue
                            val valueType = barcode.valueType

                            Log.d("M_QrViewModel", "QR code detected successfully. Data: $rawValue")
                            if (valueType == FirebaseVisionBarcode.TYPE_URL) {
                                val title = barcode.url!!.title
                                val url = barcode.url!!.url
                                Log.d("M_QrViewModel", "qr title: $title, url: $url")
                            }
                        }

                        this@QrViewModel.sortedBarcodes = sortedBarcodes
                        drawableWithDetectedQrs = getBitmapWithRects(bitmapQr, boundsList)
                        continuation.resume(drawableWithDetectedQrs)
                    }
                    .addOnFailureListener { e ->
                        Log.e("M_QrViewModel", "Error while detecting qr: $e")
                        drawableWithDetectedQrs = null
                        continuation.resumeWithException(e)
                    }
            }
        }

    private fun getBitmapWithRects(bitmap: Bitmap, boundsList: List<Rect?>): Drawable {
        val bmpCopy = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        val cvs = Canvas(bmpCopy)
        val paint = Paint().apply {
            style = Paint.Style.FILL
            color = Color.argb(90, 255, 255, 255)
            strokeWidth = 7f
        }
        var bordersDrawn = 0
        boundsList
            .filterNotNull()
            .forEach { cvs.drawRect(it, paint); bordersDrawn++ }

        Log.d("M_QrViewModel", "Borders were drawn: $bordersDrawn")
        return bmpCopy.toDrawable(context.resources)
    }
}