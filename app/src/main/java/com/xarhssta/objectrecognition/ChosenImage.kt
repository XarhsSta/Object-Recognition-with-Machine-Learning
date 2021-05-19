package com.xarhssta.objectrecognition

import android.graphics.*
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

private const val STATE_RECOGNIZE = "Image"

class ChosenImage : BaseActivity() {

    private var data: ByteArray? = null
    private var chosenImageView: ImageView? = null
    private val itemRecognitions: List<ItemRecognition> = EMPTY_ITEM_LIST
    private val itemRecognitionAdapter = ItemRecognitionAdapter(itemRecognitions)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chosen_image)
        setSupportActionBar(findViewById(R.id.toolbar))
        activateToolbar(true)

        val viewModel = ViewModelProvider(this).get(ItemRecognitionViewModel::class.java)
        chosenImageView = findViewById(R.id.chosenImageView)

        data = if (savedInstanceState == null) {
            intent.getByteArrayExtra("photo")!!
        } else {
            savedInstanceState.getByteArray(STATE_RECOGNIZE)!!
        }

        val modelChosen = intent.getIntExtra("model", 0)

        val imageBitmap = BitmapFactory.decodeByteArray(data, 0, data!!.size)
        val convertedBitmap = convert(imageBitmap)

        val itemList: RecyclerView = findViewById(R.id.itemList)
        itemList.layoutManager = LinearLayoutManager(this)
        itemList.adapter = itemRecognitionAdapter

        viewModel.recognitionList.observe(this,
            {
                itemRecognitionAdapter.setRecognitionList(it ?: EMPTY_ITEM_LIST)
            })

        viewModel.recognizedBitmap.observe(this,
            {
                chosenImageView?.setImageBitmap(it)
            })
        if (savedInstanceState == null) {
            viewModel.recognizeObjects(convertedBitmap, modelChosen)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_about -> {
                showAboutDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun convert(bitmap: Bitmap): Bitmap {
        val convertedBitmap =
            Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(convertedBitmap)
        val paint = Paint()
        paint.color = Color.BLACK
        val zero = .0f
        canvas.drawBitmap(bitmap, zero, zero, paint)
        return convertedBitmap
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putByteArray(STATE_RECOGNIZE, data!!)
    }
}
