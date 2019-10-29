package com.nostereal.qrdetector

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.widget.Toast
import androidx.core.graphics.drawable.toBitmap
import com.google.firebase.FirebaseApp
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetectorOptions
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import kotlinx.android.synthetic.main.activity_main.*

@Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onStart() {
        super.onStart()
        if (intent != null)
            FirebaseApp.initializeApp(this)
            handleSendImage(intent)
    }

    private fun handleSendImage(intent: Intent) {
        (intent.getParcelableExtra<Parcelable>(Intent.EXTRA_STREAM) as? Uri).let { uri ->
            if (uri == null) {
                Log.e("M_MainActivity", "Uri from intent is null")
                return
            }
            val inputStream = contentResolver.openInputStream(uri)
            val drawableImg = Drawable.createFromStream(inputStream, uri.toString())
            imageView.setImageDrawable(drawableImg)
            Log.d("M_MainActivity", "An Image must be loaded")

            // qr
            val bitmap = drawableImg.toBitmap()
            handleDetectedQR(bitmap)
        }
    }

    private fun handleDetectedQR(bitmapImage: Bitmap) {
        val image = FirebaseVisionImage.fromBitmap(bitmapImage)
        val options = FirebaseVisionBarcodeDetectorOptions.Builder()
            .setBarcodeFormats(FirebaseVisionBarcode.FORMAT_QR_CODE)
            .build()
        val detector =
            FirebaseVision.getInstance().getVisionBarcodeDetector(options)

        val result = detector.detectInImage(image)
            .addOnSuccessListener { barcodes ->
                for (barcode in barcodes) {
                    val bounds = barcode.boundingBox
                    val corners = barcode.cornerPoints
                    val rawValue = barcode.rawValue
                    val valueType = barcode.valueType

                    Log.d("M_MainActivity", "QR code detected successfully. Data: $rawValue")

                    if (valueType == FirebaseVisionBarcode.TYPE_URL) {
                        val title = barcode.url!!.title
                        val url = barcode.url!!.url
                        Log.d("M_MainActivity", "QR data is an url. Url: $url")
                    }
                    // todo: draw bounds and other visual features
                    Toast.makeText(this, "QR detected", Toast.LENGTH_LONG).show()
                }
            }
            .addOnFailureListener { e ->
                Log.e("M_MainActivity", "QR: Something went wrong. $e")
            }
    }

}

