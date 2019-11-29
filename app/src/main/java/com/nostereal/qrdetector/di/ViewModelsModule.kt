package com.nostereal.qrdetector.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.nostereal.qrdetector.viewmodels.QrViewModel
import com.nostereal.qrdetector.viewmodels.ViewModelFactory
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class ViewModelsModule {

    @Binds
    abstract fun bindViewModelFactory(factory: ViewModelFactory): ViewModelProvider.Factory

    @Binds
    @IntoMap
    @ViewModelKey(QrViewModel::class)
    abstract fun bindQrViewModel(qrViewModel: QrViewModel): ViewModel
}