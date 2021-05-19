package com.xarhssta.objectrecognition

import android.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

open class BaseActivity : AppCompatActivity() {

    internal fun activateToolbar(enableHome: Boolean) {
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(enableHome)
    }

    internal fun showAboutDialog() {
        val messageView = layoutInflater.inflate(R.layout.activity_about, null, false)
        val builder = AlertDialog.Builder(this)
        val aboutDialog: AlertDialog = builder.setView(messageView).create()
        aboutDialog.setCanceledOnTouchOutside(true)
        aboutDialog.show()
    }
}