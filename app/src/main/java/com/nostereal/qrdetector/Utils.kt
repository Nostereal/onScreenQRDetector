package com.nostereal.qrdetector

import android.content.res.Resources

fun dpToPx(dp: Int) = (dp * Resources.getSystem().displayMetrics.density).toInt()

fun pxToDp(px: Int) = (px / Resources.getSystem().displayMetrics.density).toInt()