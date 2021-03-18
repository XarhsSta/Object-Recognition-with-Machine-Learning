package com.xarhssta.objectrecognition

import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class ChosenImage : AppCompatActivity() {

    private var chosenImageView: ImageView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chosen_image)
        setSupportActionBar(findViewById(R.id.toolbar))

        chosenImageView = findViewById(R.id.chosenImageView)
        val data = intent.getByteArrayExtra("photo")
        chosenImageView?.setImageBitmap(BitmapFactory.decodeByteArray(data, 0 , data!!.size))
    }
}