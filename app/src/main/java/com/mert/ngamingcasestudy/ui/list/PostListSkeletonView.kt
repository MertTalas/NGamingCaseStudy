package com.mert.ngamingcasestudy.ui.list

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import com.mert.ngamingcasestudy.R
import com.mert.ngamingcasestudy.databinding.ItemPostSkeletonBinding

class PostListSkeletonView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : LinearLayout(context, attrs) {

    init {
        orientation = VERTICAL
        val titleWidths = resources.obtainTypedArray(R.array.skeleton_title_widths)
        val firstLineWidths = resources.obtainTypedArray(R.array.skeleton_first_line_widths)
        val secondLineWidths = resources.obtainTypedArray(R.array.skeleton_second_line_widths)
        val inflater = LayoutInflater.from(context)

        repeat(resources.getInteger(R.integer.skeleton_row_count)) { row ->
            val pattern = row % titleWidths.length()
            ItemPostSkeletonBinding.inflate(inflater, this, true).apply {
                skeletonTitle.setWidth(titleWidths, pattern)
                skeletonFirstLine.setWidth(firstLineWidths, pattern)
                skeletonSecondLine.setWidth(secondLineWidths, pattern)
            }
        }

        titleWidths.recycle()
        firstLineWidths.recycle()
        secondLineWidths.recycle()
    }

    private fun View.setWidth(widths: TypedArray, index: Int) {
        layoutParams = layoutParams.apply { width = widths.getDimensionPixelSize(index, 0) }
    }
}
