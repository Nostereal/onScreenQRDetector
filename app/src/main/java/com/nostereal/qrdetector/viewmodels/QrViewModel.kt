package com.nostereal.qrdetector.viewmodels

import android.app.Application
import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Parcelable
import android.util.Log
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.withContext
import javax.inject.Inject

class QrViewModel @Inject constructor(private val context: Application) : AndroidViewModel(context) {

    private var lastUri: Uri? = null

    lateinit var qrDrawable: MutableLiveData<Drawable>

    suspend fun getDrawableFromIntent(intent: Intent) = withContext(Dispatchers.IO) {
        (intent.getParcelableExtra<Parcelable>(Intent.EXTRA_STREAM) as? Uri).let { uri ->
            Log.d("M_QrViewModel", "Uri from intent: $uri")
            if (uri == null || uri == lastUri) return@withContext

            lastUri = uri

            val inputStream = context.contentResolver.openInputStream(uri)
            val drawableImg = Drawable.createFromStream(inputStream, uri.toString())

            qrDrawable.postValue(drawableImg)

            val bitmap = drawableImg.toBitmap()
//            handleDetectedQR(bitmap)
        }
    }
}