package com.mert.ngamingcasestudy.ui.detail

import com.mert.ngamingcasestudy.R
import com.mert.ngamingcasestudy.databinding.FragmentDetailBinding
import com.mert.ngamingcasestudy.ui.common.BaseFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DetailFragment : BaseFragment<FragmentDetailBinding>(
    R.layout.fragment_detail,
    FragmentDetailBinding::bind,
)
