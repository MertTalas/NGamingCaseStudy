package com.mert.ngamingcasestudy.ui.list

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil3.load
import coil3.request.crossfade
import coil3.request.error
import coil3.request.placeholder
import coil3.request.transformations
import coil3.transform.CircleCropTransformation
import com.mert.ngamingcasestudy.R
import com.mert.ngamingcasestudy.databinding.ItemPostBinding
import com.mert.ngamingcasestudy.domain.model.Post

class PostAdapter(
    private val onPostClick: (PostListItem) -> Unit,
    private val onPostDelete: (Post) -> Unit,
) : ListAdapter<PostListItem, PostAdapter.PostViewHolder>(PostDiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val binding = ItemPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PostViewHolder(binding, onPostClick, onPostDelete)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class PostViewHolder(
        private val binding: ItemPostBinding,
        onPostClick: (PostListItem) -> Unit,
        onPostDelete: (Post) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        private var boundItem: PostListItem? = null

        val boundPost: Post? get() = boundItem?.post

        val foreground: View get() = binding.foreground

        val deleteBackground: View get() = binding.deleteBackground

        init {
            binding.foreground.setOnClickListener { boundItem?.let(onPostClick) }
            ViewCompat.addAccessibilityAction(
                binding.foreground,
                binding.root.context.getString(R.string.post_delete),
            ) { _, _ ->
                boundPost?.let(onPostDelete)
                true
            }
        }

        fun bind(item: PostListItem) {
            boundItem = item
            binding.title.text = item.post.title
            binding.body.text = item.post.body.replace('\n', ' ')
            binding.avatar.load(item.imageUrl) {
                crossfade(true)
                placeholder(R.drawable.bg_avatar_placeholder)
                error(R.drawable.bg_avatar_placeholder)
                transformations(CircleCropTransformation())
            }
        }
    }

    private object PostDiffCallback : DiffUtil.ItemCallback<PostListItem>() {
        override fun areItemsTheSame(oldItem: PostListItem, newItem: PostListItem) = oldItem.post.id == newItem.post.id
        override fun areContentsTheSame(oldItem: PostListItem, newItem: PostListItem) = oldItem == newItem
    }
}
