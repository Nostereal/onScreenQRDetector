package com.nostereal.qrdetector.ui

import android.app.Dialog
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
        view.bottomSheetDialogTitle.text = "QR Data"
        view.rv_qr_codes.init(LinearLayoutManager(view.context), adapter)

        Log.d("M_RoundedBottomSheet", "Creating view...")
        return view
    }

    fun passQrsToAdapter(barcodes: List<FirebaseVisionBarcode>) {
        adapter.qrCodes = barcodes
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        BottomSheetDialog(requireContext(), theme)
            .apply { window!!.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS) }
}