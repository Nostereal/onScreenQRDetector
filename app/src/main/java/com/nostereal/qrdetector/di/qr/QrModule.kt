package com.nostereal.qrdetector.di.qr

import com.nostereal.qrdetector.adapters.QrAdapter
import com.nostereal.qrdetector.ui.RoundedBottomSheetDialogFragment
import dagger.Module
import dagger.Provides

@Module
class QrModule {

    @Provides
    fun provideQrAdapter(): QrAdapter = QrAdapter()
}
