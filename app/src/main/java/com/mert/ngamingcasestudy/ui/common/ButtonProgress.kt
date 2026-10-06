package com.mert.ngamingcasestudy.ui.common

import android.graphics.drawable.Drawable
import androidx.annotation.DrawableRes
import androidx.appcompat.content.res.AppCompatResources
import com.google.android.material.button.MaterialButton
import com.google.android.material.progressindicator.CircularProgressIndicatorSpec
import com.google.android.material.progressindicator.IndeterminateDrawable
import com.mert.ngamingcasestudy.R

fun MaterialButton.showProgress(isLoading: Boolean, @DrawableRes idleIcon: Int) {
    icon = if (isLoading) progressDrawable() else AppCompatResources.getDrawable(context, idleIcon)
    isClickable = !isLoading
}

private fun MaterialButton.progressDrawable(): Drawable {
    (getTag(R.id.tag_button_progress) as? Drawable)?.let { return it }
    val spec = CircularProgressIndicatorSpec(context, null, 0, R.style.Widget_NGaming_ButtonProgress).apply {
        indicatorColors = intArrayOf(iconTint.defaultColor)
    }
    return IndeterminateDrawable.createCircularDrawable(context, spec)
        .also { setTag(R.id.tag_button_progress, it) }
}
