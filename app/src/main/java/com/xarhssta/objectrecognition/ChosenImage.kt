package com.xarhssta.objectrecognition

import android.graphics.*
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.mlkit.common.model.CustomRemoteModel
import com.google.mlkit.common.model.LocalModel
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabel
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.custom.CustomImageLabelerOptions
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import com.google.mlkit.vision.objects.DetectedObject
import com.google.mlkit.vision.objects.ObjectDetection
import com.google.mlkit.vision.objects.defaults.ObjectDetectorOptions
import org.tensorflow.lite.task.vision.detector.Detection
import java.util.*
import kotlin.collections.ArrayList


private const val TAG = "ChosenImage"


class ChosenImage : BaseActivity() {

    private var chosenImageView: ImageView? = null
    private val itemRecognitions: ArrayList<ItemRecognition> = ArrayList()
    private val itemRecognitionAdapter = ItemRecognitionAdapter(itemRecognitions, this)
    private val colorTable: List<Int> = listOf(Color.GREEN, Color.RED, Color.BLUE, Color.BLACK, Color.YELLOW, Color.CYAN, Color.DKGRAY, Color.LTGRAY)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chosen_image)
        activateToolbar(true)
        Log.d(TAG, ".onCreate starts")
        chosenImageView = findViewById(R.id.chosenImageView)
        val data = intent.getByteArrayExtra("photo")
        val imageBitmap = BitmapFactory.decodeByteArray(data, 0, data!!.size)
        val convertedBitmap = convert(imageBitmap, Bitmap.Config.ARGB_8888)
        chosenImageView?.setImageBitmap(convertedBitmap)
        locateObjects(convertedBitmap!!)

        val itemList: RecyclerView = findViewById(R.id.itemList)
        itemList.layoutManager = LinearLayoutManager(this)
        itemList.adapter = itemRecognitionAdapter


    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main,menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun convert(bitmap: Bitmap, config: Bitmap.Config): Bitmap? {
        val convertedBitmap = Bitmap.createBitmap(bitmap.width, bitmap.height, config)
        val canvas = Canvas(convertedBitmap)
        val paint = Paint()
        paint.color = Color.BLACK
        val zero: Float = .0f
        canvas.drawBitmap(bitmap, zero, zero, paint)
        return convertedBitmap
    }


    private fun locateObjects(bitmap: Bitmap) {
        // Finding the object
        val objectDetectorOptions = ObjectDetectorOptions.Builder()
                .setDetectorMode(ObjectDetectorOptions.SINGLE_IMAGE_MODE)
                .enableMultipleObjects()
                .enableClassification()
                .build()

        val objectDetector = ObjectDetection.getClient(objectDetectorOptions)

        val inputImage = InputImage.fromBitmap(bitmap, 0)
        var croppedBitmap: Bitmap = Bitmap.createBitmap(bitmap)
        objectDetector.process(inputImage)
                .addOnFailureListener { e -> Log.e(TAG, "FAILED! ${e.message}") }
                .addOnSuccessListener { results ->
                    for ((count, detectedObject) in results.withIndex()) {
                        val boundingBox = detectedObject.boundingBox
                        val trackingId = detectedObject.trackingId
                        croppedBitmap = cropObject(bitmap, boundingBox)
                        val localModel = LocalModel.Builder()
                                .setAssetFilePath("efficientnet_lite0_fp32_2.tflite")
                                .build()
                        val customImageLabelerOptions = CustomImageLabelerOptions.Builder(localModel)
                                .setMaxResultCount(10)
                                .setConfidenceThreshold(0.1f)
                                .build()
                        val labeler = ImageLabeling.getClient(customImageLabelerOptions)
                        labeler.process(InputImage.fromBitmap(croppedBitmap, 0))
                                .addOnFailureListener{e-> Log.e(TAG, "FAILED! ${e.message}")}
                                .addOnSuccessListener { labels ->
                                    Log.d(TAG, "Success")
                                    for (label in labels) {
                                        itemRecognitions.add(ItemRecognition(trackingId,
                                                label.text,
                                                label.confidence,
                                                boundingBox,
                                                colorTable[count]
                                        ))
                                    }
                                    paintAround(bitmap, detectedObject, labels[0], count)
                                    itemRecognitionAdapter.notifyDataSetChanged()
                                }
                    }
                }
    }

    private fun cropObject(bitmap: Bitmap,rect: Rect) :Bitmap {
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

    private fun paintAround(bitmap:Bitmap, detectedObject: DetectedObject, label: ImageLabel, count: Int) {
        val canvas:Canvas = Canvas(bitmap)
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

        chosenImageView?.setImageBitmap(bitmap)
    }
}
