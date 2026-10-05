package com.mert.ngamingcasestudy.ui.list

import android.graphics.Canvas
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.mert.ngamingcasestudy.domain.model.Post

class PostSwipeCallback(
    private val onPostSwiped: (Post) -> Unit,
) : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.START) {

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder,
    ) = false

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        viewHolder.asPostViewHolder().boundPost?.let(onPostSwiped)
    }

    override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
        viewHolder?.let { getDefaultUIUtil().onSelected(it.asPostViewHolder().foreground) }
    }

    override fun onChildDraw(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean,
    ) {
        getDefaultUIUtil().onDraw(
            c, recyclerView, viewHolder.asPostViewHolder().foreground,
            dX, dY, actionState, isCurrentlyActive,
        )
    }

    override fun onChildDrawOver(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder?,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean,
    ) {
        val foreground = viewHolder?.asPostViewHolder()?.foreground ?: return
        getDefaultUIUtil().onDrawOver(c, recyclerView, foreground, dX, dY, actionState, isCurrentlyActive)
    }

    override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
        getDefaultUIUtil().clearView(viewHolder.asPostViewHolder().foreground)
    }

    private fun RecyclerView.ViewHolder.asPostViewHolder() = this as PostAdapter.PostViewHolder
}
