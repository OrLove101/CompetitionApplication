package com.orlove101.android.casersapp.ui.fragments

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.paging.CombinedLoadStates
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.orlove101.android.casersapp.R
import com.orlove101.android.casersapp.databinding.FragmentWaitingCarsBinding
import com.orlove101.android.casersapp.ui.adapters.CarsAdapter
import com.orlove101.android.casersapp.ui.viewmodels.WaitingCarsViewModel
import com.orlove101.android.casersapp.utils.CropImageContract
import com.orlove101.android.casersapp.utils.MIMETYPE_IMAGES
import com.orlove101.android.casersapp.utils.SEARCH_NEWS_TIME_DELAY
import com.orlove101.android.casersapp.utils.autoCleared
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.onEach

@AndroidEntryPoint
class WaitingCarsFragment: Fragment() {
    private var binding by autoCleared<FragmentWaitingCarsBinding>()
    private val viewModel: WaitingCarsViewModel by viewModels()
    private val carsAdapter by lazy(LazyThreadSafetyMode.NONE) { CarsAdapter() }
    private val cropResultLauncher = registerForActivityResult(CropImageContract()) { uri ->
        viewModel.handleImageResult(uri, requireContext())
    }
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (!isGranted) {
                showRationalDialog(
                    getString(R.string.rationale_title),
                    getString(R.string.rationale_desc),
                    Manifest.permission.CAMERA
                )
            }
        }
    private var dataLoading = false

    @ExperimentalCoroutinesApi
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentWaitingCarsBinding.inflate(inflater, container, false)

        setupRecyclerView()

        setupSearch()

        lifecycleScope.launchWhenStarted {
            viewModel.cars.collectLatest { response ->
                // not submit
                carsAdapter.differ.submitList(response.toList())
                dataLoading = false
                Log.d(TAG, "onCreateView: listSubmitted")
            }
        }

        lifecycleScope.launchWhenCreated {
            viewModel.query.onEach {
                updateSearchQuery(it)
                Log.d(TAG, "onCreateView: query $it")
            }
            // TODO check if it run after screen rotation
        }

        carsEventHandler()

        return binding.root
    }

    private fun setupSearch() {
        var job: Job? = null

        binding.etSearch.addTextChangedListener { editable ->
            job?.cancel()
            job = lifecycleScope.launch {
                delay(SEARCH_NEWS_TIME_DELAY)
                editable?.let {
                    if(editable.toString().isNotEmpty()) {
                        viewModel.setQuery(editable.toString())

                        if (!dataLoading) {
                            dataLoading = true
                            viewModel.searchWaitingCars(
                                editable.toString(),
                                isNewQuery = true
                            )
                        }
                    }
                }
            }
        }
    }

    private val scrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            if (dy > 0) { // check for scroll down
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val visibleItemCount = layoutManager.childCount
                val totalItemCount = layoutManager.itemCount
                val pastVisiblesItems = layoutManager.findFirstVisibleItemPosition()

                if (!dataLoading) {
                    if ((visibleItemCount + pastVisiblesItems) >= totalItemCount) {
                        dataLoading = true
                        viewModel.searchWaitingCars()
                    }
                }
            }
        }
    }

    private fun updateSearchQuery(searchQuery: String) {
        with(binding.etSearch) {
            if ((text?.toString() ?: "") != searchQuery) {
                setText(searchQuery)
            }
        }
    }

    private fun setupRecyclerView() {
        binding.rvWaitingCars.apply {
            adapter = carsAdapter
            layoutManager = LinearLayoutManager(activity)
            addOnScrollListener(scrollListener)
        }
//        carsAdapter.addLoadStateListener { state: CombinedLoadStates ->
//            binding.apply {
//                rvWaitingCars.isVisible = state.refresh != LoadState.Loading
//                paginationProgressBar.isVisible = state.refresh == LoadState.Loading
//            }
//        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.root.setOnClickListener {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED
            ) {
                cropResultLauncher.launch(MIMETYPE_IMAGES)
            } else {
                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    private fun showRationalDialog(
        title: String,
        message: String,
        permission: String
    ) {
        val builder: AlertDialog.Builder = AlertDialog.Builder(requireContext())
        builder.setTitle(title)
            .setMessage(message)
            .setPositiveButton(getString(R.string.ok)) { _, _ ->
                requestPermissionLauncher.launch(permission)
            }
        builder.create().show()
    }

    private fun carsEventHandler() {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.carsEvent.collect { event ->
                when (event) {
                    is WaitingCarsViewModel.WaitingCarsEvents.ShowToast -> {
                        val text = event.text ?: event.textId?.let { getString(it) } ?: ""

                        showToast(text)
                    }
                }
            }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
}

private const val TAG = "WaitingCarsFragment"