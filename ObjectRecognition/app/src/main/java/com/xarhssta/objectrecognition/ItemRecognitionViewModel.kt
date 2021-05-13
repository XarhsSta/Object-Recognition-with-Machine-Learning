package com.xarhssta.objectrecognition

import android.graphics.*
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.util.*

private const val TAG = "ItemRecognitionViewModel."
val EMPTY_ITEM_LIST:List<ItemRecognition> = Collections.emptyList()

class ItemRecognitionViewModel: ViewModel(), ObjectDetection.ObjectCallback {

    private val recognitionListMutable = MutableLiveData<List<ItemRecognition>>()
    private val recognizedBitmapMutable = MutableLiveData<Bitmap>()

    val recognitionList : LiveData<List<ItemRecognition>>
        get() = recognitionListMutable
    val recognizedBitmap : LiveData<Bitmap>
        get() = recognizedBitmapMutable

    init {
        recognitionListMutable.postValue(EMPTY_ITEM_LIST)
    }

    override fun onObjectRecognized(itemList: List<ItemRecognition>, bitmap: Bitmap) {
        Log.d(TAG,"objectRecognized")
        recognitionListMutable.value = itemList
        recognizedBitmapMutable.value = bitmap
    }

    fun recognizeObjects(bitmap: Bitmap, modelChosen: Int) {
        Log.d(TAG,".recognizeObjects starts")
        var objectDetection = ObjectDetection(this)
        objectDetection.locateObjects(bitmap, modelChosen)
    }

}

data class ItemRecognition(val id:Int?, val label:String, val probability: Float, val location: Rect, val color: Int) {

    private val probabilityString = String.format("%.1f", probability * 100.0f)

    override fun toString(): String {
        return "$id |$label | $probabilityString |$location |$color"
    }
}