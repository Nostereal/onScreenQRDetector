package com.nostereal.qrdetector.ui

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

@Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class QrActivity : DaggerAppCompatActivity() {

    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory
    private lateinit var viewModel: QrViewModel

    @Inject lateinit var bottomSheetDialogFragment: RoundedBottomSheetDialogFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewModel = provideViewModel(viewModelFactory)
    }

    override fun onStart() {
        super.onStart()

        intent?.also { intent ->
            FirebaseApp.initializeApp(this)
            viewModel.viewModelScope.launch {
                val drawable = viewModel.getDrawableFromIntent(intent) ?: return@launch

                imageView.apply {
                    visibility = View.VISIBLE
                    setImageDrawable(drawable)
                    setOnClickListener { showQRs(null) }
                }
                textView.visibility = View.GONE
                selectImageFromGalleryBtn.visibility = View.GONE

                viewModel.getDrawableWithDetectedQrs(drawable)?.also {
                    imageView.setImageDrawable(it)
                    showQRs(viewModel.sortedBarcodes)
                }
            }
        }
    }

    private fun showQRs(barcodes: List<FirebaseVisionBarcode>?) {
        Log.d("M_MainActivity", "Showing bottom sheet...")
        bottomSheetDialogFragment.show(supportFragmentManager, "qrBottomSheet")

        barcodes?.also(bottomSheetDialogFragment::passQrsToAdapter)
    }
}