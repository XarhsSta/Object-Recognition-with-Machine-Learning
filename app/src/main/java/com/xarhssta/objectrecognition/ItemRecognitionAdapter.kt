package com.xarhssta.objectrecognition

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

private const val TAG = "ItemRecognitionAdapter"

class ItemRecognitionAdapter (private var itemList: List<ItemRecognition>, private val context: Context):
   RecyclerView.Adapter<ItemRecognitionViewHolder>()
    {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemRecognitionViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_recognition, parent, false)
            Log.d(TAG,".onCreateViewHolder starts")
            return ItemRecognitionViewHolder(view)
        }


        override fun getItemCount(): Int {
            return if(itemList.isNotEmpty()) itemList.size else 1
        }

        override fun onBindViewHolder(holder: ItemRecognitionViewHolder, position: Int) {
            Log.d(TAG,".onBindViewHolder starts")
            if(itemList.isNotEmpty()) {
                holder.itemView.visibility = View.VISIBLE
                val data = itemList.get(position)
                val probabilityString = String.format("%.1f", data.probability * 100.0f)
                holder.itemProbability.text = "$probabilityString %"
                holder.itemName.text = data.label.capitalize()
                val bitmap: Bitmap = Bitmap.createBitmap(25,25, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(bitmap)
                canvas.drawColor(data.color)
                holder.itemColor.setImageBitmap(bitmap)
            } else {
                holder.itemView.visibility = View.INVISIBLE
            }
        }

        fun setRecognitionList(recognitionList:List<ItemRecognition>) {
            this.itemList = recognitionList
            notifyDataSetChanged()
        }
    }

class ItemRecognitionViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    var itemColor: ImageView = view.findViewById(R.id.itemColor)
    var itemName: TextView = view.findViewById(R.id.itemName)
    var itemProbability: TextView = view.findViewById(R.id.itemProbability)

}