package com.xarhssta.objectrecognition

import android.graphics.*
import android.util.Log
import com.google.mlkit.common.model.LocalModel
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabel
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.custom.CustomImageLabelerOptions
import com.google.mlkit.vision.objects.DetectedObject
import com.google.mlkit.vision.objects.ObjectDetection
import com.google.mlkit.vision.objects.defaults.ObjectDetectorOptions
import java.util.*

private const val TAG = "ObjectDetection"

class ObjectDetection(private val callback: ObjectCallback) {

    private val colorTable: List<Int> = listOf(Color.GREEN, Color.RED, Color.BLUE, Color.BLACK, Color.YELLOW, Color.CYAN, Color.DKGRAY, Color.LTGRAY)
    private val itemRecognition: MutableList<ItemRecognition> = mutableListOf()
//    private var recognizedBitmap: Bitmap? = null

    interface ObjectCallback{
        fun onObjectRecognized(itemList: List<ItemRecognition>, bitmap: Bitmap)
    }


    fun locateObjects(bitmap: Bitmap, modelChosen: Int) {

        // Setting the model
        val filePath = when (modelChosen) {
            1 -> "object dataset"
            2 -> "electronics dataset"
            3 -> "flower dataset"
            else -> ""
        }
        
        // Finding the object
        val objectDetectorOptions = ObjectDetectorOptions.Builder()
            .setDetectorMode(ObjectDetectorOptions.SINGLE_IMAGE_MODE)
            .enableMultipleObjects()
            .enableClassification()
            .build()
        Log.d(TAG,"locateObjects starts")

        val objectDetector = ObjectDetection.getClient(objectDetectorOptions)
        val inputImage = InputImage.fromBitmap(bitmap, 0)
        var recognizedBitmap: Bitmap = bitmap
        var croppedBitmap: Bitmap

        objectDetector.process(inputImage)
            .addOnFailureListener { e -> Log.e(TAG, "FAILED! ${e.message}") }
            .addOnSuccessListener { results ->
                for ((count, detectedObject) in results.withIndex()) {
                    val boundingBox = detectedObject.boundingBox
                    val trackingId = detectedObject.trackingId
                    croppedBitmap = cropObject(bitmap, boundingBox)

                    val localModel = LocalModel.Builder()
                        .setAssetFilePath("$filePath/model.tflite")
                        .build()

                    val customImageLabelerOptions = CustomImageLabelerOptions.Builder(localModel)
                        .setMaxResultCount(3)
                        .build()

                    val labeler = ImageLabeling.getClient(customImageLabelerOptions)
                    labeler.process(InputImage.fromBitmap(croppedBitmap, 0))
                        .addOnFailureListener { e -> Log.e(TAG, "FAILED! ${e.message}") }
                        .addOnSuccessListener { labels ->
                            Log.d(TAG, "Success")
                            if (labels.isNotEmpty()) {
                                for (label in labels) {

                                    itemRecognition.add(ItemRecognition(trackingId,
                                                label.text,
                                                label.confidence,
                                                boundingBox,
                                                colorTable[count]
                                        )
                                    )
                                }
                               recognizedBitmap = paintAround(recognizedBitmap, detectedObject, labels[0], count)
                            }
                            callback.onObjectRecognized(itemRecognition, recognizedBitmap)
                        }
                }
            }
    }

    private fun cropObject(bitmap: Bitmap, rect: Rect) : Bitmap {
        return try {
            val width = rect.right - rect.left
            val height = rect.bottom - rect.top
            Bitmap.createBitmap(bitmap,
                rect.left,
                rect.top,
                width,
                height)
        } catch (e:IllegalArgumentException){
            bitmap
        }
    }

    private fun paintAround(bitmap: Bitmap, detectedObject: DetectedObject, label: ImageLabel, count: Int): Bitmap {
        val canvas: Canvas = Canvas(bitmap)
        val paint: Paint = Paint()
        paint.style = Paint.Style.STROKE
        paint.color = colorTable[count]
        paint.strokeCap = Paint.Cap.ROUND
        paint.strokeWidth = 10.0f
        paint.strokeJoin = Paint.Join.ROUND
        paint.strokeMiter = 100f
        paint.isAntiAlias = true
        canvas.drawRect(detectedObject.boundingBox, paint)

        // Drawing the rectangle for the object name
        val textPaint = Paint()
        textPaint.color = Color.WHITE
        textPaint.textSize = 50f

        val boxPaint = Paint()
        boxPaint.color = colorTable[count]
        boxPaint.style = Paint.Style.FILL

        val textToShow = label.text.capitalize(Locale.ROOT) + " " + String.format("%.1f", label.confidence * 100.0f) + " %"
        val textWidth = textPaint.measureText(textToShow)

        val rectLeft = detectedObject.boundingBox.left.toFloat()
        val rectTop = detectedObject.boundingBox.top - 50.toFloat()
        val rectRight = detectedObject.boundingBox.left + textWidth
        val rectBottom = detectedObject.boundingBox.top.toFloat()

        canvas.drawRect(rectLeft, rectTop, rectRight, rectBottom, boxPaint)
        canvas.drawText(textToShow, rectLeft, rectBottom, textPaint)
        return bitmap
    }


}