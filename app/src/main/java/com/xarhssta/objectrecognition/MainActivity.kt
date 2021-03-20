package com.xarhssta.objectrecognition

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import java.io.ByteArrayOutputStream
import java.lang.Exception

private const val TAG = "MainActivity"

class MainActivity : AppCompatActivity() {

    private val requestImageFromStorage = 1
    private val requestPictureCode = 2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar))
        Log.d(TAG,".onCreate starts")
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> true
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d(TAG,".onActivityResult starts")
        val selectedImage = data!!.data

        if (requestCode == requestImageFromStorage && resultCode == Activity.RESULT_OK && data != null) {
            try {
                val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, selectedImage)
                val byteArrayOutputStream = ByteArrayOutputStream()
                bitmap?.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
                val bitmapData = byteArrayOutputStream.toByteArray()

                val intent = Intent(this, ChosenImage::class.java)
                intent.putExtra("photo", bitmapData)
                startActivity(intent)
            } catch (e : Exception) {
                e.printStackTrace()
            }
        } else if (requestCode == requestPictureCode && resultCode == Activity.RESULT_OK && data != null) {
            try {
                val cameraImage = data.extras?.get("data") as Bitmap
                val byteArrayOutputStream = ByteArrayOutputStream()
                cameraImage.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
                val bitmapData = byteArrayOutputStream.toByteArray()

                val intent = Intent(this, ChosenImage::class.java)
                intent.putExtra("photo", bitmapData)
                startActivity(intent)
            } catch (e : Exception) {
                e.printStackTrace()
            }
        }
    }

    fun openCamera(view: View) {
        Log.d(TAG, ".openCamera starts")
            Log.d(TAG, ".openCamera inside else for Write_External_Storage")
            if (checkSelfPermission(android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, ".openCamera inside if for Camera")
                requestPermissions(arrayOf(android.Manifest.permission.CAMERA), 1)
            } else {
                Log.d(TAG, ".openCamera inside else for Camera")
                dispatchTakePictureIntent()
            }
    }

    private fun dispatchTakePictureIntent() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        try {
            startActivityForResult(takePictureIntent, requestPictureCode)
        } catch (e: Exception) {
            Log.e(TAG,"Error Message: ${e.message}")
        }
    }

}