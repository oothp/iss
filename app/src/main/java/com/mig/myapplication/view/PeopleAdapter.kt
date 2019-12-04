package com.mig.myapplication.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mig.myapplication.databinding.ViewItemPersonBinding
import com.mig.myapplication.viewmodel.ItemDataViewModel

class PeopleAdapter : RecyclerView.Adapter<PeopleAdapter.ViewHolder>() {

    private var items = listOf<ItemDataViewModel>()

    inner class ViewHolder(private val binding: ViewItemPersonBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ItemDataViewModel) {
            binding.item = item
            binding.executePendingBindings()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return ViewHolder(ViewItemPersonBinding.inflate(inflater, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int = items.size

    fun addItems(list: List<ItemDataViewModel>) {
        items = list
        this.notifyDataSetChanged()
    }
}
