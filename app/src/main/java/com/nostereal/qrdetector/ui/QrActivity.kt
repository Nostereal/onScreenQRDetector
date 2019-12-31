package com.nostereal.qrdetector.ui

import android.app.Activity
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.firebase.FirebaseApp
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode
import com.nostereal.qrdetector.R
import com.nostereal.qrdetector.provideViewModel
import com.nostereal.qrdetector.viewmodels.QrViewModel
import dagger.android.support.DaggerAppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.launch
import javax.inject.Inject

class QrActivity : DaggerAppCompatActivity() {

    companion object {
        const val PICK_IMAGE_REQUEST_CODE = 101
    }

    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory
    private lateinit var viewModel: QrViewModel

    @Inject lateinit var bottomSheetDialogFragment: RoundedBottomSheetDialogFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewModel = provideViewModel(viewModelFactory)

        selectImageFromGalleryBtn.setOnClickListener {
            val intent = Intent().apply {
                type = "image/*"
                action = Intent.ACTION_GET_CONTENT
            }
            startActivityForResult(Intent.createChooser(intent, "Select a picture"), PICK_IMAGE_REQUEST_CODE)
        }
    }

    override fun onStart() {
        super.onStart()

        intent?.also { intent ->
            FirebaseApp.initializeApp(this)
            viewModel.viewModelScope.launch {
                val drawable = viewModel.getDrawableFromIntent(intent) ?: return@launch
                showQRs(drawable)
            }
        }
    }

    private fun showQRs(drawable: Drawable) {
        imageView.apply {
            visibility = View.VISIBLE
            setImageDrawable(drawable)
            setOnClickListener { openBottomSheetWithQRs(null) }
        }
        textView.visibility = View.GONE
        selectImageFromGalleryBtn.visibility = View.GONE

        viewModel.viewModelScope.launch {
            viewModel.getDrawableWithDetectedQrs(drawable)?.also {
                imageView.setImageDrawable(it)
                openBottomSheetWithQRs(viewModel.sortedBarcodes)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            data?.data?.also { uri ->
                val inputStream = contentResolver.openInputStream(uri)
                val drawable = Drawable.createFromStream(inputStream, uri.toString())
                showQRs(drawable)
            }
        }
    }

    private fun openBottomSheetWithQRs(barcodes: List<FirebaseVisionBarcode>?) {
        Log.d("M_MainActivity", "Showing bottom sheet...")
        bottomSheetDialogFragment.show(supportFragmentManager, "qrBottomSheet")

        barcodes?.also(bottomSheetDialogFragment::passQrsToAdapter)
    }
}