package com.mert.ngamingcasestudy.ui.common

import android.view.View
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.viewbinding.ViewBinding
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

abstract class BaseFragment<VB : ViewBinding>(
    @LayoutRes layoutRes: Int,
    bind: (View) -> VB,
) : Fragment(layoutRes) {

    protected val binding: VB by viewBinding(bind)

    protected fun <T> Flow<T>.collectWithLifecycle(
        minActiveState: Lifecycle.State = Lifecycle.State.STARTED,
        action: suspend (T) -> Unit,
    ) {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(minActiveState) {
                collect { action(it) }
            }
        }
    }
}
