package com.orlove101.android.casersapp.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.orlove101.android.casersapp.databinding.FragmentParsedCarsBinding
import com.orlove101.android.casersapp.ui.viewmodels.ParsedCarsViewModel
import com.orlove101.android.casersapp.utils.autoCleared
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ParsedCarsFragment: Fragment() {
    private var binding by autoCleared<FragmentParsedCarsBinding>()
    private val viewModel: ParsedCarsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentParsedCarsBinding.inflate(inflater, container, false)

        return binding.root
    }
}