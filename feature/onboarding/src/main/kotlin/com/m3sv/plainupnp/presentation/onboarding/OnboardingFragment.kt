package com.m3sv.plainupnp.presentation.onboarding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.m3sv.plainupnp.presentation.onboarding.databinding.OnboardingFragmentBinding

class OnboardingFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? = OnboardingFragmentBinding.inflate(inflater, container, false).root

}
