package com.nostereal.qrdetector.di

import com.nostereal.qrdetector.di.qr.QrFragmentBuildersModule
import com.nostereal.qrdetector.di.qr.QrModule
import com.nostereal.qrdetector.ui.QrActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ActivityBuildersModule {

    @ContributesAndroidInjector(
        modules = [
            QrFragmentBuildersModule::class,
            QrModule::class
        ]
    )
    abstract fun contributeQrActivity(): QrActivity
}