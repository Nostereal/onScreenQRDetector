package com.nostereal.qrdetector.ui

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.FirebaseApp
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode
import com.nostereal.qrdetector.R
import com.nostereal.qrdetector.provideViewModel
import com.nostereal.qrdetector.handlePermission
import com.nostereal.qrdetector.requestPermission
import com.nostereal.qrdetector.viewmodels.QrViewModel
import dagger.android.support.DaggerAppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.launch
import javax.inject.Inject

class QrActivity : DaggerAppCompatActivity() {

    companion object {
        const val PICK_IMAGE_REQUEST_CODE = 101
    }

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private lateinit var viewModel: QrViewModel

    @Inject
    lateinit var bottomSheetDialogFragment: RoundedBottomSheetDialogFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewModel = provideViewModel(viewModelFactory)

        selectImageFromGalleryBtn.setOnClickListener {

            handlePermission(
                permission = Manifest.permission.READ_EXTERNAL_STORAGE,
                requestCode = PICK_IMAGE_REQUEST_CODE,
                onGranted = { requestCode -> pickImageFromGallery(requestCode) },
                onDenied = { permission, requestCode -> requestPermission(permission, requestCode) },
                onExplanationNeeded = { permission, requestCode ->
                    Snackbar
                        .make(
                            root_view,
                            R.string.read_external_storage_rationale,
                            Snackbar.LENGTH_INDEFINITE
                        )
                        .setAction(R.string.snackbar_grant_action) { requestPermission(permission, requestCode) }
                        .show()
                })
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

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when(requestCode) {
            PICK_IMAGE_REQUEST_CODE -> {
                if (permissions.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    pickImageFromGallery(requestCode)
                } else {
                    // permission wasn't granted :(
                    // disable all stuff related to this permission
                    Snackbar.make(
                        root_view,
                        R.string.permission_not_granted_message,
                        Snackbar.LENGTH_LONG
                    ).show()
                }
            }
            else -> {  }
        }

    }

    private fun pickImageFromGallery(requestCode: Int) {
        val intent = Intent().apply {
            type = "image/*"
            action = Intent.ACTION_PICK
        }
        startActivityForResult(Intent.createChooser(intent, "Select a picture"), requestCode)
    }

    private fun openBottomSheetWithQRs(barcodes: List<FirebaseVisionBarcode>?) {
        Log.d("M_MainActivity", "Showing bottom sheet...")
        bottomSheetDialogFragment.show(supportFragmentManager, "qrBottomSheet")

        barcodes?.also(bottomSheetDialogFragment::passQrsToAdapter)
    }
}