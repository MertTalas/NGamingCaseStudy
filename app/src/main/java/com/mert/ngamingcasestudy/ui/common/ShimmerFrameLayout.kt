package com.mert.ngamingcasestudy.ui.common

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Shader
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.FrameLayout
import com.mert.ngamingcasestudy.R

class ShimmerFrameLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : FrameLayout(context, attrs) {

    private val highlightColor = context.getColor(R.color.shimmer_highlight)
    private val shaderMatrix = Matrix()
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP)
    }
    private var bandWidth = 0f
    private var progress = 0f

    private val animator = ValueAnimator.ofFloat(0f, 1f).apply {
        duration = resources.getInteger(R.integer.shimmer_duration_ms).toLong()
        repeatCount = ValueAnimator.INFINITE
        interpolator = LinearInterpolator()
        addUpdateListener {
            progress = it.animatedValue as Float
            invalidate()
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        bandWidth = w * BAND_WIDTH_FRACTION
        paint.shader = LinearGradient(
            0f, 0f, bandWidth, 0f,
            intArrayOf(Color.TRANSPARENT, highlightColor, Color.TRANSPARENT),
            null,
            Shader.TileMode.CLAMP,
        )
    }

    override fun dispatchDraw(canvas: Canvas) {
        val shader = paint.shader
        if (shader == null || !animator.isRunning) {
            super.dispatchDraw(canvas)
            return
        }
        val layer = canvas.saveLayer(0f, 0f, width.toFloat(), height.toFloat(), null)
        super.dispatchDraw(canvas)
        val travelled = -bandWidth + (width + bandWidth) * progress
        val bandStart = if (layoutDirection == LAYOUT_DIRECTION_RTL) width - travelled - bandWidth else travelled
        shaderMatrix.setTranslate(bandStart, 0f)
        shader.setLocalMatrix(shaderMatrix)
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
        canvas.restoreToCount(layer)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        updateAnimation()
    }

    override fun onDetachedFromWindow() {
        animator.cancel()
        super.onDetachedFromWindow()
    }

    override fun onVisibilityChanged(changedView: View, visibility: Int) {
        super.onVisibilityChanged(changedView, visibility)
        updateAnimation()
    }

    private fun updateAnimation() {
        if (isAttachedToWindow && isShown) {
            if (!animator.isStarted) animator.start()
        } else {
            animator.cancel()
        }
    }

    private companion object {
        const val BAND_WIDTH_FRACTION = 0.6f
    }
}
