package com.xarhssta.objectrecognition

import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.core.content.FileProvider
import java.io.*
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*

private const val TAG = "MainActivity"

class MainActivity : BaseActivity() {

    private val requestImageFromStorage = 1
    private val requestPictureCode = 2
    private var imageUri: Uri? = Uri.EMPTY
    private lateinit var mySharedPreferences: SharedPreferences
    private lateinit var mySharedPreferencesEditor: SharedPreferences.Editor
    private var radioButtonSelected: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar))
        Log.d(TAG,".onCreate starts")
        mySharedPreferences = getSharedPreferences("radioChecked", MODE_PRIVATE)
        mySharedPreferencesEditor = mySharedPreferences.edit()
        radioButtonSelected = mySharedPreferences.getInt("number",1)
        Log.d(TAG, "$radioButtonSelected")
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
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

    fun selectImage( view: View ) {
        Log.d(TAG, ".selectImage starts")
        if (checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, ".selectImage inside if")
            requestPermissions(arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE), requestImageFromStorage)
        } else {
            Log.d(TAG,".selectImage inside else")
            getPhoto()
        }
            Log.d(TAG,".selectImage finished")
    }

    private fun getPhoto() {
        Log.d(TAG,".getPhoto() starts")
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, 1)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, dataIntent: Intent?) {
        super.onActivityResult(requestCode, resultCode, dataIntent)
        Log.d(TAG,".onActivityResult starts")
        Log.d(TAG, requestCode.toString())
        Log.d(TAG, resultCode.toString())
        Log.d(TAG, dataIntent.toString())
            var image: Bitmap? = null
            if (requestCode == requestImageFromStorage && resultCode == Activity.RESULT_OK) {
                val selectedImage = dataIntent?.data
                try {
                    image = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        val source = ImageDecoder.createSource(this.contentResolver, selectedImage!!)
                        ImageDecoder.decodeBitmap(source)
                    } else {
                        MediaStore.Images.Media.getBitmap(this.contentResolver, selectedImage)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, e.message!!)
                }
            } else if (requestCode == requestPictureCode && resultCode == Activity.RESULT_OK) {
                image = MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
            }
                    val byteArrayOutputStream = ByteArrayOutputStream()
                    image?.compress(Bitmap.CompressFormat.JPEG, 25, byteArrayOutputStream)
                    val bitmapData = byteArrayOutputStream.toByteArray()

                    val intent = Intent(this, ChosenImage::class.java)
                    intent.putExtra("photo", bitmapData)
                    intent.putExtra("model", radioButtonSelected)
                    mySharedPreferencesEditor.remove("number").apply()
                    startActivity(intent)
        }


    fun openCamera(view: View) {
        when {
            checkSelfPermission(android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED -> {
                requestPermissions(arrayOf(android.Manifest.permission.CAMERA), 1)
            }
            checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED -> {
                requestPermissions(arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)
            }
            else -> {
                dispatchTakePictureIntent()
            }
        }
    }

    private fun dispatchTakePictureIntent() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent.resolveActivity(packageManager) != null) {
            val values = ContentValues()
            values.put(MediaStore.Images.Media.TITLE, "myPicture")
            values.put(MediaStore.Images.Media.DESCRIPTION, "Taken On "+System.currentTimeMillis())
            imageUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
            startActivityForResult(takePictureIntent, requestPictureCode)
        }
    }

    fun radioButtonChecked(view: View) {
        radioButtonSelected = when (view.tag) {
            "common objects" -> 1
            "electronics" -> 2
            "flower" -> 3
            else -> 0
        }
        mySharedPreferencesEditor.putInt("number", radioButtonSelected)
                .apply()
    }
}