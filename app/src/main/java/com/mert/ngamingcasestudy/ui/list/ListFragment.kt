package com.mert.ngamingcasestudy.ui.list

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.divider.MaterialDividerItemDecoration
import com.google.android.material.snackbar.Snackbar
import com.mert.ngamingcasestudy.R
import com.mert.ngamingcasestudy.databinding.FragmentListBinding
import com.mert.ngamingcasestudy.ui.common.BaseFragment
import com.mert.ngamingcasestudy.ui.common.applySystemBarInsetsAsPadding
import com.mert.ngamingcasestudy.ui.common.showProgress
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
        setupRefresh()
        binding.retryButton.setOnClickListener { viewModel.onRetry() }
        binding.reloadButton.setOnClickListener { viewModel.onRefresh() }

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
        adapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                val layoutManager = layoutManager as LinearLayoutManager
                if (positionStart <= layoutManager.findFirstCompletelyVisibleItemPosition()) {
                    scrollToPosition(positionStart)
                }
            }
        })
        applySystemBarInsetsAsPadding(bottom = true)
    }

    private fun setupRefresh() = with(binding.swipeRefresh) {
        setColorSchemeResources(R.color.on_primary)
        setProgressBackgroundColorSchemeResource(R.color.primary)
        setOnRefreshListener { viewModel.onRefresh() }
    }

    private fun render(state: ListUiState, adapter: PostAdapter) = with(binding) {
        loadingSkeleton.isVisible = state is ListUiState.Loading
        errorGroup.isVisible = state is ListUiState.Error
        swipeRefresh.isVisible = state is ListUiState.Success
        emptyGroup.isVisible = state is ListUiState.Success && state.items.isEmpty()
        when (state) {
            is ListUiState.Success -> renderSuccess(state, adapter)
            is ListUiState.Error -> retryButton.showProgress(state.isRetrying, R.drawable.ic_refresh)
            ListUiState.Loading -> Unit
        }
    }

    private fun renderSuccess(state: ListUiState.Success, adapter: PostAdapter) = with(binding) {
        adapter.submitList(state.items)
        swipeRefresh.isRefreshing = state.isRefreshing && state.items.isNotEmpty()
        reloadButton.showProgress(state.isRefreshing && state.items.isEmpty(), R.drawable.ic_refresh)
    }

    private fun handleEvent(event: ListEvent) = when (event) {
        ListEvent.ShowUndoDelete -> showUndoDelete()
        ListEvent.ShowRefreshError -> showRefreshError()
    }

    private fun showRefreshError() {
        Snackbar.make(binding.root, R.string.list_refresh_error, Snackbar.LENGTH_LONG).show()
    }

    private fun showUndoDelete() {
        Snackbar.make(binding.root, R.string.post_deleted, Snackbar.LENGTH_LONG)
            .setAction(R.string.post_undo) { viewModel.onUndoDelete() }
            .show()
    }

    private fun openDetail(item: PostListItem) {
        val navController = findNavController()
        if (navController.currentDestination?.id != R.id.listFragment) return
        navController.navigate(ListFragmentDirections.actionListToDetail(item.post.id, item.imagePosition))
    }
}
