package com.xarhssta.objectrecognition

import android.graphics.*
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.task.vision.detector.Detection
import org.tensorflow.lite.task.vision.detector.ObjectDetector


private const val TAG = "ChosenImage"


class ChosenImage : AppCompatActivity() {

    private var chosenImageView: ImageView? = null
    private val itemRecognitionAdapter = ItemRecognitionAdapter(ArrayList())
    private val colorTable: List<Int> = listOf(Color.GREEN, Color.RED, Color.BLUE, Color.BLACK, Color.YELLOW)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chosen_image)
        setSupportActionBar(findViewById(R.id.toolbar2))
        Log.d(TAG, ".onCreate starts")

        val itemList: RecyclerView = findViewById(R.id.itemList)
        itemList.layoutManager = LinearLayoutManager(this)
        itemList.adapter = itemRecognitionAdapter
        val itemNameTextView: TextView = findViewById(R.id.itemNameTextView)
        val itemScoreTextView: TextView = findViewById(R.id.itemScoreTextView)
        chosenImageView = findViewById(R.id.chosenImageView)
        val data = intent.getByteArrayExtra("photo")
        val imageBitmap = BitmapFactory.decodeByteArray(data, 0, data!!.size)
        var convertedBitmap = convert(imageBitmap, Bitmap.Config.ARGB_8888)

        convertedBitmap = locateObjects(convertedBitmap!!)
        chosenImageView?.setImageBitmap(convertedBitmap)
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

    private fun resultConvert(result: List<Detection>): ArrayList<ItemRecognition> {
        val recognitions: ArrayList<ItemRecognition> = ArrayList()
        var cnt = 0
        for (detection in result) {
            recognitions.add(
                ItemRecognition(
                    "" + cnt++,
                    detection.categories[0].label,
                    detection.categories[0].score,
                    detection.boundingBox
                )
            )
            Log.d(TAG,recognitions[cnt-1].toString())
        }
        return recognitions
    }

    private fun locateObjects(bitmap: Bitmap): Bitmap {
        // Finding the object
        val options = ObjectDetector.ObjectDetectorOptions
                .builder()
                .setMaxResults(3)
                .build()

        val objectDetector = ObjectDetector.createFromFileAndOptions(
                this,
                "ssd_mobilenet.tflite",
                options
        )
        val tfImage = TensorImage.fromBitmap(bitmap)
        val results: List<Detection> = objectDetector.detect(tfImage)
        Log.d(TAG, "${results.size}")
        val items:ArrayList<ItemRecognition> = resultConvert(results)
        // Setting the texts
//        itemNameTextView.text = items[0].label
//        itemScoreTextView.text = String.format("%.1f", items[0].probability * 100.0f)
        // Drawing the rectangle around the object
        val canvas: Canvas = Canvas(bitmap)
        var i:Int = 0
        for (item in items) {
            val paint: Paint = Paint()
            paint.style = Paint.Style.STROKE
            paint.color = colorTable[i]
            paint.strokeCap = Paint.Cap.ROUND
            paint.strokeWidth = 10.0f
            paint.strokeJoin = Paint.Join.ROUND
            paint.strokeMiter = 100f
            paint.isAntiAlias = true
            canvas.drawRect(item.location, paint)
            // Drawing the rectangle for the object name
            val textPaint = Paint()
            textPaint.color = Color.WHITE
            textPaint.textSize = 50f

            val boxPaint = Paint()
            boxPaint.color = colorTable[i]
            boxPaint.style = Paint.Style.FILL

            val textToShow = item.label.capitalize() + " " + String.format("%.1f", item.probability * 100.0f) + " %"
            val textWidth = textPaint.measureText(textToShow)
            val rectLeft = item.location.left
            val rectTop = item.location.top - 50
            val rectRight = item.location.left + textWidth
            val rectBottom = item.location.top

            canvas.drawRect(rectLeft, rectTop, rectRight, rectBottom, boxPaint)
            canvas.drawText(textToShow, item.location.left, item.location.top, textPaint)
            i++
        }
        return bitmap
    }
}