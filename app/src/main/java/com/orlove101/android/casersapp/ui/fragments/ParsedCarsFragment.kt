package com.orlove101.android.casersapp.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.orlove101.android.casersapp.databinding.FragmentParsedCarsBinding
import com.orlove101.android.casersapp.ui.adapters.NewsLoaderStateAdapter
import com.orlove101.android.casersapp.ui.adapters.ParsedCarsAdapter
import com.orlove101.android.casersapp.ui.viewmodels.ParsedCarsViewModel
import com.orlove101.android.casersapp.utils.autoCleared
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect

@AndroidEntryPoint
class ParsedCarsFragment: Fragment() {
    private var binding by autoCleared<FragmentParsedCarsBinding>()
    private val viewModel: ParsedCarsViewModel by viewModels()
    private val carsAdapter by lazy(LazyThreadSafetyMode.NONE) { ParsedCarsAdapter(requireContext()) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentParsedCarsBinding.inflate(inflater, container, false)

        setupRecyclerView()

        lifecycleScope.launchWhenStarted {
            viewModel.cars.collect(carsAdapter::submitData)
        }

        carsEventHandler()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val itemTouchHelperCallback = getItemTouchHelper()

        ItemTouchHelper(itemTouchHelperCallback).apply {
            attachToRecyclerView(binding.rvParsedCars)
        }
    }

    private fun getItemTouchHelper(): ItemTouchHelper.SimpleCallback {
        return object : ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.UP or ItemTouchHelper.DOWN,
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val article = carsAdapter.snapshot()[position]

                article?.let { viewModel.deleteCar(it) }
            }
        }
    }

    private fun setupRecyclerView() {
        binding.rvParsedCars.apply {
            adapter = carsAdapter.withLoadStateHeaderAndFooter(
                header = NewsLoaderStateAdapter(context),
                footer = NewsLoaderStateAdapter(context)
            )
            layoutManager = LinearLayoutManager(activity)
        }
    }

    private fun carsEventHandler() {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.carsEvent.collect { event ->
                when (event) {
                    is ParsedCarsViewModel.ParsedCarsEvents.ShowArticleDeletedSnackbar -> {
                        Snackbar.make(binding.root, getString(event.msgId), Snackbar.LENGTH_LONG)
                            .apply {
                                setAction(getString(event.actonMsgId)) {
                                    viewModel.saveCar(event.article)
                                }
                                show()
                            }
                    }
                }
            }
        }
    }
}