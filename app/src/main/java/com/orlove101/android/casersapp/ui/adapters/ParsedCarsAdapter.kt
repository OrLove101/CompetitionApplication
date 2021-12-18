package com.orlove101.android.casersapp.ui.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.orlove101.android.casersapp.R
import com.orlove101.android.casersapp.databinding.ItemCarPreviewBinding
import com.orlove101.android.casersapp.domain.models.CarDomain

class ParsedCarsAdapter(
    private val context: Context
): PagingDataAdapter<CarDomain, ParsedCarsAdapter.CarViewHolder>(ArticleDifferCallback) {

    inner class CarViewHolder(private val binding: ItemCarPreviewBinding)
        : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: CarDomain?) {
            item?.let {
                binding.apply {
                    tvCarNumber.text = context.getString(R.string.car_number_template, item.carNumber)
                    tvCargoDescription.text = item.cargoDescription
                    tvReadyFrom.text = context.getString(R.string.car_stop_waiting_template, item.stopWaitingAt)
                    bnLetCarGo.apply {
                        text = context.getString(R.string.car_is_parsed)
                        isEnabled = false
                    }
                }
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ParsedCarsAdapter.CarViewHolder {
        val binding = ItemCarPreviewBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CarViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ParsedCarsAdapter.CarViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

private object ArticleDifferCallback: DiffUtil.ItemCallback<CarDomain>() {

    override fun areItemsTheSame(oldItem: CarDomain, newItem: CarDomain): Boolean {
        return oldItem.uuid == newItem.uuid
    }

    override fun areContentsTheSame(oldItem: CarDomain, newItem: CarDomain): Boolean {
        return oldItem == newItem
    }
}