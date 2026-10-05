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
    private val onPostClick: (Post) -> Unit,
    private val onPostDelete: (Post) -> Unit,
) : ListAdapter<Post, PostAdapter.PostViewHolder>(PostDiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val binding = ItemPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PostViewHolder(binding, onPostClick, onPostDelete)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        holder.bind(getItem(position), postImageUrl(position))
    }

    class PostViewHolder(
        private val binding: ItemPostBinding,
        onPostClick: (Post) -> Unit,
        onPostDelete: (Post) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        var boundPost: Post? = null
            private set

        val foreground: View get() = binding.foreground

        val deleteBackground: View get() = binding.deleteBackground

        init {
            binding.foreground.setOnClickListener { boundPost?.let(onPostClick) }
            ViewCompat.addAccessibilityAction(
                binding.foreground,
                binding.root.context.getString(R.string.post_delete),
            ) { _, _ ->
                boundPost?.let(onPostDelete)
                true
            }
        }

        fun bind(post: Post, imageUrl: String) {
            boundPost = post
            binding.title.text = post.title
            binding.body.text = post.body.replace('\n', ' ')
            binding.avatar.load(imageUrl) {
                crossfade(true)
                placeholder(R.drawable.bg_avatar_placeholder)
                error(R.drawable.bg_avatar_placeholder)
                transformations(CircleCropTransformation())
            }
        }
    }

    private object PostDiffCallback : DiffUtil.ItemCallback<Post>() {
        override fun areItemsTheSame(oldItem: Post, newItem: Post) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Post, newItem: Post) = oldItem == newItem
    }
}
