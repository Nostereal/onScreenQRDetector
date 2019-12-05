package com.nostereal.qrdetector

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