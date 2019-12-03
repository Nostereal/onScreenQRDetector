package com.nostereal.qrdetector.di.qr

import com.nostereal.qrdetector.ui.RoundedBottomSheetDialogFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class QrFragmentBuildersModule {

    @ContributesAndroidInjector
    abstract fun contributeBottomSheetDialogFragment(): RoundedBottomSheetDialogFragment
}