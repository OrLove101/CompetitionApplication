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
import com.google.android.material.snackbar.Snackbar
import com.orlove101.android.casersapp.R
import com.orlove101.android.casersapp.databinding.FragmentWaitingCarsBinding
import com.orlove101.android.casersapp.domain.models.CarDomain
import com.orlove101.android.casersapp.ui.adapters.WaitingCarsAdapter
import com.orlove101.android.casersapp.ui.viewmodels.WaitingCarsViewModel
import com.orlove101.android.casersapp.utils.*
import com.orlove101.android.casersapp.utils.contracts.CropImageContract
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import android.content.DialogInterface

import android.text.Editable

import dagger.hilt.android.qualifiers.ActivityContext

import android.widget.EditText




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
            viewModel.plombNumber.collect { response ->
                when (response) {
                    is Resource.Error -> {
                        response.message?.let { message ->
                            showSnackbarOnParsingFail(message)
                        }
                    }
                    is Resource.Success -> {
                        response.data?.let { message ->
                            if (viewModel.allPlombParsed()) {
                                carsAdapter.differ.apply {
                                    val newList = ArrayList(this.currentList)

                                    newList.remove(viewModel.currentCar)
                                    submitList(newList)
                                }
                                showToast(message)
                            } else {
                                showToast(message)
                                cropResultLauncher.launch(MIMETYPE_IMAGES)
                            }
                        }
                    }
                }
            }
        }

        lifecycleScope.launchWhenStarted {
            viewModel.cars.collect { response ->
                when (response) {
                    is Resource.Error -> {
                        hideProgressBar()
                        response.message?.let { message ->
                            Log.e(TAG, getString(R.string.an_error_occured) + message)
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

        carsAdapter.setOnLetCarGoButtonClickListener { car ->
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED
            ) {
                viewModel.startParseNewCar(car)
                cropResultLauncher.launch(MIMETYPE_IMAGES)
            } else {
                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }

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

    private fun showTypeInPlombNumberDialog() {
        val alert = AlertDialog.Builder(requireContext())
        val edittext = EditText(requireContext())

        alert.setMessage(getString(R.string.type_in_plomb_num_msg))
        alert.setTitle(getString(R.string.type_in_plomb_num_title))
        alert.setView(edittext)
        alert.setPositiveButton(
            getString(R.string.type_in_lomb_confirm)
        ) { _, _ ->
            val enteredPlombNumber = edittext.text.toString()

            viewModel.processPlombNumber(enteredPlombNumber, requireContext())
        }
        alert.setNegativeButton(
            getString(R.string.deny_type_in_plomb)
        ) { _, _ ->
            // do nothing
        }
        alert.show()
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    private fun showSnackbarOnParsingFail(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG)
            .apply {
                setAction(getString(R.string.type_in_manually)) {
                    showTypeInPlombNumberDialog()
                }
                setAction(getString(R.string.retry)) {
                    cropResultLauncher.launch(MIMETYPE_IMAGES)
                }
                show()
            }
    }
}

private const val TAG = "WaitingCarsFragment"