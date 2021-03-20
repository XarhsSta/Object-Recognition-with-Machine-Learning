package com.xarhssta.objectrecognition

import android.graphics.RectF
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ItemRecognitionViewModel: ViewModel() {

    private val recognitionListMutable = MutableLiveData<List<ItemRecognition>>()
    val recognitionList : LiveData<List<ItemRecognition>> = recognitionListMutable

    fun updateData(recognitions: List<ItemRecognition>) {
        recognitionListMutable.postValue(recognitions)
    }
}

data class ItemRecognition(val id:String, val label:String, val probability: Float, val location: RectF) {

    private val probabilityString = String.format("%.1f", probability * 100.0f)

    override fun toString(): String {
        return "$id |$label | $probabilityString |$location"
    }
}