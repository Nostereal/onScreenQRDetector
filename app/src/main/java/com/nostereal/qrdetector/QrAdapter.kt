package com.nostereal.qrdetector

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode
import kotlinx.android.synthetic.main.item_qr_data.view.*
import kotlin.properties.Delegates

class QrAdapter : RecyclerView.Adapter<QrAdapter.QrDataViewHolder>() {
    var qrCodes: List<FirebaseVisionBarcode> by Delegates.observable(emptyList()) { _, old, new ->
       notifyChanges(old, new)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QrDataViewHolder {
        return QrDataViewHolder(parent)
    }

    override fun getItemCount(): Int = qrCodes.size

    override fun onBindViewHolder(holder: QrDataViewHolder, position: Int) {
        holder.bindData(qrCodes[position])
    }

    class QrDataViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        constructor(parent: ViewGroup) :
                this(LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_qr_data, parent,false))

        fun bindData(barcode: FirebaseVisionBarcode) {
            itemView.apply {
                val itemFormatData = when (barcode.valueType) {
                    FirebaseVisionBarcode.TYPE_URL -> barcode.url!!.title to barcode.url!!.url
                    FirebaseVisionBarcode.TYPE_TEXT -> "Text" to barcode.displayValue
                    else -> "Unsupported type" to barcode.displayValue
                }

                val title = itemFormatData.first
                val content = itemFormatData.second
                Log.d("M_QrDataViewHolder", "Title: $title, content: $content")
                if (content != null) {
                    if (title.isNullOrEmpty())
                        qrTitle.visibility = View.GONE
                    else
                        qrTitle.text = title
                    qrRawValue.text = content
                }
            }
        }
    }

    private fun notifyChanges(oldList: List<FirebaseVisionBarcode>, newList: List<FirebaseVisionBarcode>) {
        val diff = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return oldList[oldItemPosition] == newList[newItemPosition]
            }

            override fun getOldListSize(): Int = oldList.size
            override fun getNewListSize(): Int = newList.size

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return oldList[oldItemPosition] == newList[newItemPosition]
            }
        })

        diff.dispatchUpdatesTo(this)
    }
}