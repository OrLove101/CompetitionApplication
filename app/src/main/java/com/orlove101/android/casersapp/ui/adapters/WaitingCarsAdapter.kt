package com.orlove101.android.casersapp.ui.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.orlove101.android.casersapp.R
import com.orlove101.android.casersapp.databinding.ItemCarPreviewBinding
import com.orlove101.android.casersapp.domain.models.CarDomain

class WaitingCarsAdapter(
    private val context: Context
): RecyclerView.Adapter<WaitingCarsAdapter.CarsViewHolder>() {

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun onBindViewHolder(holder: CarsViewHolder, position: Int) {
        val car = differ.currentList[position]

        holder.bind(car)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CarsViewHolder {
        val binding = ItemCarPreviewBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CarsViewHolder(binding)
    }

    inner class CarsViewHolder(private val binding: ItemCarPreviewBinding)
        : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: CarDomain?) {
            item?.let {
                binding.apply {
                    tvCarNumber.text = context.getString(R.string.car_number_template, item.carNumber)
                    tvCargoDescription.text = item.cargoDescription
                    tvReadyFrom.text = context.getString(R.string.car_start_waiting_template, item.startWaitingAt)
                    tvPlombQnt.text = context.getString(R.string.plomb_quantity, item.plombQuantity)
                    bnLetCarGo.setOnClickListener {
                        onLetCarGoClickListener?.let {
                            it(item)
                        }
                    }
                }
            }
        }
    }

    private val differCallback = object : DiffUtil.ItemCallback<CarDomain>() {
        override fun areItemsTheSame(oldItem: CarDomain, newItem: CarDomain): Boolean {
            return oldItem.uuid == newItem.uuid
        }

        override fun areContentsTheSame(oldItem: CarDomain, newItem: CarDomain): Boolean {
            return oldItem == newItem
        }
    }

    val differ = AsyncListDiffer(this, differCallback)

    private var onLetCarGoClickListener: ((CarDomain) -> Unit)? = null

    fun setOnLetCarGoButtonClickListener(listener: (CarDomain) -> Unit) {
        onLetCarGoClickListener = listener
    }
}

