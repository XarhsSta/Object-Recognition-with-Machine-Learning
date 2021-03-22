package com.xarhssta.objectrecognition

import androidx.appcompat.app.AppCompatActivity

open class BaseActivity: AppCompatActivity() {

    internal fun activateToolbar(enableHome: Boolean) {
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(enableHome)
    }
}