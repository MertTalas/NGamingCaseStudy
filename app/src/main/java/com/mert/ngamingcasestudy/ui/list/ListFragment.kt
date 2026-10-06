package com.mert.ngamingcasestudy.ui.list

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import com.google.android.material.divider.MaterialDividerItemDecoration
import com.google.android.material.snackbar.Snackbar
import com.mert.ngamingcasestudy.R
import com.mert.ngamingcasestudy.databinding.FragmentListBinding
import com.mert.ngamingcasestudy.domain.model.Post
import com.mert.ngamingcasestudy.ui.common.BaseFragment
import com.mert.ngamingcasestudy.ui.common.applySystemBarInsetsAsPadding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ListFragment : BaseFragment<FragmentListBinding>(
    R.layout.fragment_list,
    FragmentListBinding::bind,
) {

    private val viewModel: ListViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val adapter = PostAdapter(
            onPostClick = ::openDetail,
            onPostDelete = viewModel::onPostDeleted,
        )

        setupTopBar()
        setupList(adapter)
        binding.retryButton.setOnClickListener { viewModel.onRetry() }

        viewModel.uiState.collectWithLifecycle { render(it, adapter) }
        viewModel.events.collectWithLifecycle { handleEvent(it) }
    }

    private fun setupTopBar() = with(binding.topBar) {
        topBarTitle.setText(R.string.list_title)
        root.applySystemBarInsetsAsPadding(top = true)
    }

    private fun setupList(adapter: PostAdapter) = with(binding.postList) {
        this.adapter = adapter
        setHasFixedSize(true)
        addItemDecoration(
            MaterialDividerItemDecoration(context, MaterialDividerItemDecoration.VERTICAL).apply {
                dividerThickness = resources.getDimensionPixelSize(R.dimen.divider_thickness)
                dividerColor = context.getColor(R.color.outline)
            }
        )
        ItemTouchHelper(PostSwipeCallback(onPostSwiped = viewModel::onPostDeleted)).attachToRecyclerView(this)
        applySystemBarInsetsAsPadding(bottom = true)
    }

    private fun render(state: ListUiState, adapter: PostAdapter) = with(binding) {
        loadingSkeleton.isVisible = state is ListUiState.Loading
        errorGroup.isVisible = state is ListUiState.Error
        postList.isVisible = state is ListUiState.Success
        emptyText.isVisible = state is ListUiState.Success && state.posts.isEmpty()
        if (state is ListUiState.Success) adapter.submitList(state.posts)
    }

    private fun handleEvent(event: ListEvent) = when (event) {
        ListEvent.ShowUndoDelete -> showUndoDelete()
    }

    private fun showUndoDelete() {
        Snackbar.make(binding.root, R.string.post_deleted, Snackbar.LENGTH_LONG)
            .setAction(R.string.post_undo) { viewModel.onUndoDelete() }
            .show()
    }

    private fun openDetail(post: Post, imagePosition: Int) {
        val navController = findNavController()
        if (navController.currentDestination?.id != R.id.listFragment) return
        navController.navigate(ListFragmentDirections.actionListToDetail(post.id, imagePosition))
    }
}
