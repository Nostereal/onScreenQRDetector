package com.nostereal.qrdetector.ui

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode
import com.nostereal.qrdetector.adapters.QrAdapter
import com.nostereal.qrdetector.R
import com.nostereal.qrdetector.init
import kotlinx.android.synthetic.main.bottom_sheet_dialog_layout.view.*
import kotlinx.coroutines.*
import javax.inject.Inject

class RoundedBottomSheetDialogFragment @Inject constructor(private val adapter: QrAdapter) : DaggerBottomSheetDialogFragment() {

    override fun getTheme(): Int =
        R.style.BottomSheetDialogTheme

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.bottom_sheet_dialog_layout, container, false)

        view.apply {
            bottomSheetDialogTitle.text = "QR Data"
            rv_qr_codes.init(LinearLayoutManager(view.context), adapter)
        }

        Log.d("M_BottomSheet", "Creating view...")
        return view
    }
    
    fun passQrsToAdapter(barcodes: List<FirebaseVisionBarcode>) {
        adapter.qrCodes = barcodes
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        BottomSheetDialog(requireContext(), theme)
            .apply { window!!.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS) }



    // default bottom sheet was displayed closing animation on pause
    // so i set animation res to 0 => bottom sheet has no animation
    // until it will resumed
    override fun onPause() {
        if (this.isVisible)
            dialog!!.window!!.setWindowAnimations(0)
        super.onPause()
    }

    // default bottom sheet was displayed opening animation on resume
    // so i restore bottom sheet's animations when it's resumed
    override fun onResume() {
        super.onResume()
        restoreAnimations(R.style.DialogAnim)
    }

    private fun restoreAnimations(animRes: Int) {
        CoroutineScope(Dispatchers.Main).launch {
            delay(40)
            if (this@RoundedBottomSheetDialogFragment.isVisible)
                dialog!!.window!!.setWindowAnimations(animRes)
        }
    }
}