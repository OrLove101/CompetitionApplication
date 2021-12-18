package com.orlove101.android.casersapp.ui.fragments

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.work.*
import com.orlove101.android.casersapp.R
import com.orlove101.android.casersapp.data.repository.CarsRepositoryImpl
import com.orlove101.android.casersapp.databinding.FragmentWaitingCarsBinding
import com.orlove101.android.casersapp.ui.adapters.WaitingCarsAdapter
import com.orlove101.android.casersapp.ui.viewmodels.WaitingCarsViewModel
import com.orlove101.android.casersapp.utils.*
import com.orlove101.android.casersapp.utils.contracts.CropImageContract
import com.orlove101.android.casersapp.utils.works.SyncWorker
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import java.util.concurrent.TimeUnit

@AndroidEntryPoint
class WaitingCarsFragment: Fragment() {
    private var binding by autoCleared<FragmentWaitingCarsBinding>()
    private val viewModel: WaitingCarsViewModel by viewModels()
    private val carsAdapter by lazy(LazyThreadSafetyMode.NONE) { WaitingCarsAdapter(requireContext()) }
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
    private var isLastPage = false

    @ExperimentalCoroutinesApi
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentWaitingCarsBinding.inflate(inflater, container, false)

        viewModel.setUpWaitingCarsSync(requireContext())

        setupRecyclerView()

        setupSearch()

        lifecycleScope.launchWhenStarted {
            viewModel.cars.collect { response ->
                when (response) {
                    is Resource.Error -> {
                        hideProgressBar()
                        response.message?.let { message ->
                            Log.e(TAG, "An error occurred: $message")
                        }
                    }
                    is Resource.Loading -> {
                        showProgressBar()
                    }
                    is Resource.Success -> {
                        response.data?.let { carsResponse ->
                            hideProgressBar()
                            carsAdapter.differ.submitList(carsResponse.toList())
                            isLastPage = viewModel.carsInApiQuantity == carsResponse.size.toLong()
                            if (isLastPage) {
                                binding.rvWaitingCars.setPadding(0,0,0,0)
                            }
                        }
                    }
                }
            }
        }

        lifecycleScope.launchWhenCreated {
            viewModel.query.collect {
                updateSearchQuery(it)
            }
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
                    viewModel.setQuery(editable.toString())
                    if (!dataLoading) {
                        dataLoading = true
                        viewModel.searchWaitingCars(isNewQuery = true)
                    }
            }
        }
    }

    private val scrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            val layoutManager = recyclerView.layoutManager as LinearLayoutManager
            val visibleItemCount = layoutManager.childCount
            val totalItemCount = layoutManager.itemCount
            val pastVisiblesItems = layoutManager.findFirstVisibleItemPosition()
            val isAtLastItem = (visibleItemCount + pastVisiblesItems) >= totalItemCount
            val isScrolledDown = dy > 0
            val shouldPaginate = !dataLoading && isAtLastItem && isScrolledDown && !isLastPage

            if (shouldPaginate) {
                viewModel.searchWaitingCars()
            } else if (!isScrolledDown && isAtLastItem) {
                hideProgressBar()
            }
        }
    }

    private fun hideProgressBar() {
        binding.paginationProgressBar.visibility = View.INVISIBLE
        dataLoading = false
    }

    private fun showProgressBar() {
        binding.paginationProgressBar.visibility = View.VISIBLE
        dataLoading = true
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