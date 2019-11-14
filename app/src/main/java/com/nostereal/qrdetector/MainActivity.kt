package com.nostereal.qrdetector

import android.content.Intent
import android.graphics.*
import android.graphics.drawable.Drawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.view.View
import androidx.core.graphics.drawable.toBitmap
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.FirebaseApp
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetectorOptions
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import kotlinx.android.synthetic.main.activity_main.*

@Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class MainActivity : AppCompatActivity() {
    private var lastUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        imageView.apply {
            layoutParams.width = dpToPx(150)
            layoutParams.height = dpToPx(150)
        }
    }

    override fun onStart() {
        super.onStart()
        if (intent != null) {
            FirebaseApp.initializeApp(this)
            handleSendImage(intent)
        }
    }

    private fun handleSendImage(intent: Intent) {
        (intent.getParcelableExtra<Parcelable>(Intent.EXTRA_STREAM) as? Uri).let { uri ->
            if (uri == null || uri == lastUri) {
                Log.d("M_MainActivity", "Uri from intent is null or the same as the last\n" +
                        "Current URI: $uri, Last URI: $lastUri")
                return
            }
            Log.d("M_MainActivity", "Uri: $uri")
            lastUri = uri
            val inputStream = contentResolver.openInputStream(uri)
            val drawableImg = Drawable.createFromStream(inputStream, uri.toString())


            // TODO: fix the error when qr already handled and you send new qr
            // expected: new qr replaces previous qr
            // actual: nothing is happen
            imageView.apply {
                setImageDrawable(drawableImg)
                setOnClickListener { showQRs(null) }
                layoutParams.width = drawableImg.intrinsicWidth
                layoutParams.height = drawableImg.intrinsicHeight
            }
            textView.visibility = View.GONE

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

        detector.detectInImage(image)
            .addOnSuccessListener { barcodes ->
                val boundsList = mutableListOf<Rect?>()
                val sortedBarcodes = barcodes.sortedWith(compareBy { it.cornerPoints!!.filterNotNull()[0].x })
                for (barcode in sortedBarcodes) {
                    boundsList.add(barcode.boundingBox)
                    val rawValue = barcode.rawValue
                    val valueType = barcode.valueType

                    Log.d("M_MainActivity", "QR code detected successfully. Data: $rawValue")
                    if (valueType == FirebaseVisionBarcode.TYPE_URL) {
                        val title = barcode.url!!.title
                        val url = barcode.url!!.url
                        Log.d("M_MainActivity", "qr title: $title, url: $url")
                    }
                }
                imageView.setImageBitmap(getBitmapWithRects(bitmapImage, boundsList))
                showQRs(sortedBarcodes)
            }
            .addOnFailureListener { e ->
                Log.e("M_MainActivity", "QR: Something went wrong. $e")
                Snackbar.make(root_view, e.message.toString(), Snackbar.LENGTH_LONG).show()
            }
    }

    private fun showQRs(barcodes: List<FirebaseVisionBarcode>?) {
        Log.d("M_MainActivity", "Showing bottom sheet...")
        val bottomSheetDialogFragment =
            RoundedBottomSheetDialogFragment.getInstance()
        bottomSheetDialogFragment.show(supportFragmentManager, "qrBottomSheet")
        if (barcodes != null) {
            bottomSheetDialogFragment.adapter.qrCodes = barcodes
        }
    }

//    private fun showQRs() {
//        val bottomSheetDialogFragment =
//            RoundedBottomSheetDialogFragment.getInstance()
//        bottomSheetDialogFragment.show(supportFragmentManager, "qrBottomSheetWithEmptyData")
//    }


    private fun redirectToBrowser(url: String) {
        val intent = Intent().apply {
            action = Intent.ACTION_VIEW
            data = Uri.parse(url)
        }
        startActivity(intent)
    }

    private fun getBitmapWithRects(bitmap: Bitmap, boundsList: List<Rect?>): Bitmap {
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

        Log.d("M_MainAct", "Borders was drawn: $bordersDrawn")
        return bmpCopy
    }
}