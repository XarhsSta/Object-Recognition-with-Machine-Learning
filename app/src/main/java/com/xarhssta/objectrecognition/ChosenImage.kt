package com.xarhssta.objectrecognition

import android.graphics.*
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chosen_image)
        setSupportActionBar(findViewById(R.id.toolbar))
        Log.d(TAG, ".onCreate starts")

        val itemList: RecyclerView = findViewById(R.id.itemList)
        itemList.layoutManager = LinearLayoutManager(this)
        itemList.adapter = itemRecognitionAdapter

        chosenImageView = findViewById(R.id.chosenImageView)
        val data = intent.getByteArrayExtra("photo")
        val imageBitmap = BitmapFactory.decodeByteArray(data, 0, data!!.size)
        val convertedBitmap = convert(imageBitmap, Bitmap.Config.ARGB_8888)
        chosenImageView?.setImageBitmap(imageBitmap)

        val options = ObjectDetector.ObjectDetectorOptions
            .builder()
            .setMaxResults(1)
            .build()

        val objectDetector = ObjectDetector.createFromFileAndOptions(
            this,
            "ssd_mobilenet.tflite",
            options
        )
        val tfImage = TensorImage.fromBitmap(convertedBitmap)
        val results: List<Detection> = objectDetector.detect(tfImage)
        Log.d(TAG, "$results")
        val items:ArrayList<ItemRecognition> = resultConvert(results)

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
}