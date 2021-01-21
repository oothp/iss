package com.mig.iss.view

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mig.iss.databinding.ViewSlide1Binding
import com.mig.iss.databinding.ViewSlide2Binding
import com.mig.iss.databinding.ViewSlide3Binding
import com.mig.iss.viewmodel.ItemDataViewModel

class ViewPagerAdapter(private val context: Context) : RecyclerView.Adapter<ViewPagerAdapter.MainViewHolder>() {

    companion object {
        private const val TYPE_1 = 0
        private const val TYPE_2 = 1
        private const val TYPE_3 = 2
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainViewHolder =
        when (viewType) {
            TYPE_1 -> {
                val layoutInflater = LayoutInflater.from(parent.context)
                Slide1Holder(ViewSlide1Binding.inflate(layoutInflater, parent, false))
            }
//            TYPE_1 -> Slide1Holder(
//                LayoutInflater.from(context).inflate(R.layout.view_slide1, parent, false)
//            )
            TYPE_2 -> {
                val layoutInflater = LayoutInflater.from(parent.context)
                Slide2Holder(ViewSlide2Binding.inflate(layoutInflater, parent, false))
            }
            TYPE_3 -> {
                val layoutInflater = LayoutInflater.from(parent.context)
                Slide3Holder(ViewSlide3Binding.inflate(layoutInflater, parent, false))
            }
            else -> throw IllegalArgumentException("Illegal Item View Type")
        }

    override fun onBindViewHolder(holder: MainViewHolder, position: Int) {
        when (holder) {
            is Slide1Holder -> holder.bind("11111")
            is Slide2Holder -> holder.bind("222222")
            is Slide3Holder -> holder.bind("33333")
        }
    }

    override fun getItemCount(): Int = 3

    override fun getItemViewType(position: Int): Int = when (position) {
        0 -> TYPE_1
        1 -> TYPE_2
        2 -> TYPE_3
        else -> throw IllegalArgumentException("Illegal Position")
    }

    open inner class MainViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    inner class Slide1Holder(private val binding: ViewSlide1Binding) : MainViewHolder(binding.root) {
        fun bind(str: String) {
            binding.tv.text = str
            binding.executePendingBindings()
        }
    }
    inner class Slide2Holder(private val binding: ViewSlide2Binding) : MainViewHolder(binding.root) {
        fun bind(str: String) {
            binding.tv.text = str
            binding.executePendingBindings()
        }
    }
    inner class Slide3Holder(private val binding: ViewSlide3Binding) : MainViewHolder(binding.root) {
        fun bind(str: String) {
            binding.tv.text = str
            binding.executePendingBindings()
        }
    }

    fun addPeople(item: List<ItemDataViewModel>) {

    }
}