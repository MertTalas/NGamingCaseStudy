package com.mert.ngamingcasestudy.ui.detail

import android.graphics.Rect
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import coil3.load
import coil3.request.crossfade
import coil3.request.error
import coil3.request.placeholder
import coil3.request.transformations
import coil3.transform.CircleCropTransformation
import com.mert.ngamingcasestudy.R
import com.mert.ngamingcasestudy.databinding.FragmentDetailBinding
import com.mert.ngamingcasestudy.ui.common.BaseFragment
import com.mert.ngamingcasestudy.ui.common.applySystemBarInsetsAsPadding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DetailFragment : BaseFragment<FragmentDetailBinding>(
    R.layout.fragment_detail,
    FragmentDetailBinding::bind,
) {

    private val viewModel: DetailViewModel by viewModels()

    private var loadedImageUrl: String? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadedImageUrl = null
        setupTopBar()
        setupInputs()
        binding.contentScroll.applySystemBarInsetsAsPadding()
        binding.saveContainer.applySystemBarInsetsAsPadding(bottom = true, ime = true)
        binding.saveButton.setOnClickListener { viewModel.onSave() }

        viewModel.uiState.collectWithLifecycle(action = ::render)
        viewModel.events.collectWithLifecycle(action = ::handleEvent)
    }

    private fun setupTopBar() = with(binding.topBar) {
        topBarTitle.setText(R.string.detail_title)
        topBarBack.setOnClickListener { navigateBack() }
        root.applySystemBarInsetsAsPadding(top = true)
    }

    private fun setupInputs() = with(binding) {
        titleInput.setHorizontallyScrolling(false)
        titleInput.maxLines = Int.MAX_VALUE
        titleInput.bindLabel(titleLabel)
        bodyInput.bindLabel(bodyLabel)
        titleInput.doAfterTextChanged { viewModel.onTitleChanged(it.toString()) }
        bodyInput.doAfterTextChanged { viewModel.onBodyChanged(it.toString()) }
        contentScroll.addOnLayoutChangeListener { _, _, _, _, bottom, _, _, _, oldBottom ->
            if (bottom < oldBottom) contentScroll.post(::revealFocusedInput)
        }
    }

    private fun revealFocusedInput() {
        val focused = binding.contentScroll.findFocus() as? EditText ?: return
        focused.requestRectangleOnScreen(Rect(0, 0, focused.width, focused.height))
    }

    private fun EditText.bindLabel(label: TextView) {
        setOnFocusChangeListener { _, hasFocus -> label.isSelected = hasFocus }
    }

    private fun render(state: DetailUiState) = with(binding) {
        contentScroll.isVisible = state is DetailUiState.Editing
        saveContainer.isVisible = state is DetailUiState.Editing
        notFoundText.isVisible = state is DetailUiState.NotFound
        if (state !is DetailUiState.Editing) return@with

        postNumber.text = getString(R.string.detail_post_number, state.postId)
        titleInput.setTextIfChanged(state.title)
        bodyInput.setTextIfChanged(state.body)
        saveButton.isEnabled = state.isSaveEnabled
        loadAvatar(state.imageUrl)
    }

    private fun EditText.setTextIfChanged(value: String) {
        if (text.toString() != value) setText(value)
    }

    private fun loadAvatar(imageUrl: String) {
        if (loadedImageUrl == imageUrl) return
        loadedImageUrl = imageUrl
        binding.avatar.load(imageUrl) {
            crossfade(true)
            placeholder(R.drawable.bg_avatar_placeholder)
            error(R.drawable.bg_avatar_placeholder)
            transformations(CircleCropTransformation())
        }
    }

    private fun handleEvent(event: DetailEvent) = when (event) {
        DetailEvent.Saved -> navigateBack()
    }

    private fun navigateBack() {
        val navController = findNavController()
        if (navController.currentDestination?.id != R.id.detailFragment) return
        WindowCompat.getInsetsController(requireActivity().window, requireView())
            .hide(WindowInsetsCompat.Type.ime())
        navController.navigateUp()
    }
}
