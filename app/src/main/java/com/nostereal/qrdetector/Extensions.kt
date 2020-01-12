package com.nostereal.qrdetector

import android.app.Activity
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.RecyclerView

inline fun <reified VM : ViewModel> FragmentActivity.provideViewModel(
    provider: ViewModelProvider.Factory
) = ViewModelProviders.of(this, provider).get(VM::class.java)

fun <LM, ADAPTER> RecyclerView.init(
    lm: LM,
    adapter: ADAPTER
) where LM : RecyclerView.LayoutManager,
        ADAPTER : RecyclerView.Adapter<*> = apply {
    layoutManager = lm
    this.adapter = adapter
}

fun Activity.requestPermission(permission: String, requestCode: Int) =
    ActivityCompat.requestPermissions(this, arrayOf(permission), requestCode)

inline fun Activity.handlePermission(
    permission: String,
    requestCode: Int,
    onGranted: (Int) -> Unit,
    onDenied: (String, Int) -> Unit,
    onExplanationNeeded: (String, Int) -> Unit
) {
    if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {

        if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
            // Need to show explanation
            onExplanationNeeded(permission, requestCode)
        } else {
            // No explanation needed, we can request permission
            onDenied(permission, requestCode)
        }
    } else {
        // Permission has already been granted
        onGranted(requestCode)
    }
}