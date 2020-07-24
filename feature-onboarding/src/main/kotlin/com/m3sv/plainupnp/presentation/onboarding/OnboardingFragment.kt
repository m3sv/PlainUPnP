package com.m3sv.plainupnp.presentation.onboarding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.m3sv.plainupnp.presentation.base.BaseFragment
import com.m3sv.plainupnp.presentation.onboarding.databinding.OnboardingFragmentBinding

class OnboardingFragment : BaseFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = OnboardingFragmentBinding.inflate(inflater, container, false).root

}
