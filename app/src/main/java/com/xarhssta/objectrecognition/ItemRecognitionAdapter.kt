package com.xarhssta.objectrecognition

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView

private const val TAG = "ItemRecognitionAdapter"

class ItemRecognitionAdapter (private var itemList: List<String>):
    ListAdapter<ItemRecognition, ItemRecognitionViewHolder>(ItemRecognitionDiffUtil()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemRecognitionViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_recognition, parent, false)
        return ItemRecognitionViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemRecognitionViewHolder, position: Int) {
        if(itemList.isEmpty()) {
            holder.itemName.text = "Item Name"
            holder.itemProbability.text = "0 %"
        } else {
            holder.itemName.text = "Example"
            holder.itemProbability.text = "50 %"
        }
    }

    override fun getItemCount(): Int {
        return if(itemList.isNotEmpty()) itemList.size else 1
    }

    private class ItemRecognitionDiffUtil: DiffUtil.ItemCallback<ItemRecognition> () {

        override fun areItemsTheSame(oldItem: ItemRecognition, newItem: ItemRecognition): Boolean {
            return oldItem.label == newItem.label
        }

        override fun areContentsTheSame(
            oldItem: ItemRecognition,
            newItem: ItemRecognition
        ): Boolean {
            return oldItem.probability == newItem.probability
        }
    }

}

class ItemRecognitionViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    var itemName: TextView = view.findViewById(R.id.itemName)
    var itemProbability: TextView = view.findViewById(R.id.itemProbability)

}