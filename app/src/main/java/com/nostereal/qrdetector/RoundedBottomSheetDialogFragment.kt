package com.nostereal.qrdetector

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.bottom_sheet_dialog_layout.*
import kotlinx.android.synthetic.main.bottom_sheet_dialog_layout.view.*
import kotlinx.android.synthetic.main.item_qr_data.view.*

class RoundedBottomSheetDialogFragment : BottomSheetDialogFragment() {
    val adapter: QrAdapter = QrAdapter()

    override fun getTheme(): Int = R.style.BottomSheetDialogTheme

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.bottom_sheet_dialog_layout, container, false)
        view.bottomSheetDialogTitle.text = "QR Data"
        view.rv_qr_codes.apply {
            layoutManager = LinearLayoutManager(view.context)
            adapter = this@RoundedBottomSheetDialogFragment.adapter
        }
        Log.d("M_RoundedBottomSheet", "Creating view...")
        return view
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        BottomSheetDialog(requireContext(), theme)

    companion object {
        private var instance: RoundedBottomSheetDialogFragment? = null
        fun getInstance(): RoundedBottomSheetDialogFragment {
            if (instance == null)
                instance = RoundedBottomSheetDialogFragment()
            return instance!!
        }
    }
}