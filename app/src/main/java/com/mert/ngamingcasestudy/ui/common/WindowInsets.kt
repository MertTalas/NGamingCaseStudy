package com.mert.ngamingcasestudy.ui.common

import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding

fun View.applySystemBarInsetsAsPadding(top: Boolean = false, bottom: Boolean = false) {
    val initialLeft = paddingLeft
    val initialTop = paddingTop
    val initialRight = paddingRight
    val initialBottom = paddingBottom
    ViewCompat.setOnApplyWindowInsetsListener(this) { view, insets ->
        val bars = insets.getInsets(
            WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout()
        )
        view.updatePadding(
            left = initialLeft + bars.left,
            top = initialTop + if (top) bars.top else 0,
            right = initialRight + bars.right,
            bottom = initialBottom + if (bottom) bars.bottom else 0,
        )
        insets
    }
}
