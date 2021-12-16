package com.orlove101.android.casersapp.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.orlove101.android.casersapp.data.models.Car
import com.orlove101.android.casersapp.databinding.ItemCarPreviewBinding

class CarsAdapter: RecyclerView.Adapter<CarsAdapter.CarsViewHolder>() {

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

        fun bind(item: Car?) {
            item?.let {
                binding.apply {
                    tvCarNumber.text = item.carNumber
                    tvCargoDescription.text = item.cargoDescription
                    tvReadyFrom.text = item.startWaitingAt
                    root.setOnClickListener {
                        onItemClickListener?.let {
                            it(item)
                        }
                    }
                }
            }
        }
    }

    private val differCallback = object : DiffUtil.ItemCallback<Car>() {
        override fun areItemsTheSame(oldItem: Car, newItem: Car): Boolean {
            return oldItem.uuid == newItem.uuid
        }

        override fun areContentsTheSame(oldItem: Car, newItem: Car): Boolean {
            return oldItem == newItem
        }
    }

    val differ = AsyncListDiffer(this, differCallback)

    private var onItemClickListener: ((Car) -> Unit)? = null

    fun setOnItemClickListener(listener: (Car) -> Unit) {
        onItemClickListener = listener
    }
}

