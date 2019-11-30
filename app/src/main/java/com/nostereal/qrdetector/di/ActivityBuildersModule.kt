package com.nostereal.qrdetector.di

import com.nostereal.qrdetector.ui.QrActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ActivityBuildersModule {

    @ContributesAndroidInjector
    abstract fun contributeQrActivity(): QrActivity
}