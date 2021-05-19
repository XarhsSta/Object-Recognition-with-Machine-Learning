package com.xarhssta.objectrecognition

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import java.io.ByteArrayOutputStream
import java.io.File

private const val TAG = "MainActivity"

class MainActivity : BaseActivity() {

    private val requestImageFromStorage = 1
    private val requestPictureCode = 2
    private var imageUri: Uri? = Uri.EMPTY
    private lateinit var mySharedPreferences: SharedPreferences
    private lateinit var mySharedPreferencesEditor: SharedPreferences.Editor
    private var radioButtonSelected: Int = 0

    @SuppressLint("CommitPrefEdits")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar))
        mySharedPreferences = getSharedPreferences("radioChecked", MODE_PRIVATE)
        mySharedPreferencesEditor = mySharedPreferences.edit()
        radioButtonSelected = mySharedPreferences.getInt("number", 1)
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

    fun selectImage(view: View) {
        if (checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
                requestImageFromStorage
            )
        } else {
            getPhoto()
        }
    }

    private fun getPhoto() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, 1)
    }

    private fun checkForBlankFile() {
        val fileDelete: File = File(getPath(imageUri))
        if (fileDelete.length().toInt() == 0) {
            contentResolver.delete(imageUri!!, null, null)
        }
    }

    private fun getPath(uri: Uri?): String {
        val projection = arrayOf(MediaStore.Images.Media.DATA)
        val cursor = contentResolver.query(uri!!, projection, null, null, null)
        val columnIndex = cursor?.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
        cursor?.moveToFirst()
        val s = cursor?.getString(columnIndex!!)
        cursor?.close()
        return s!!
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, dataIntent: Intent?) {
        super.onActivityResult(requestCode, resultCode, dataIntent)
        var image: Bitmap? = null
        if (requestCode == requestPictureCode) {
            checkForBlankFile()
        }
        if (dataIntent != null && requestCode == requestImageFromStorage && resultCode == Activity.RESULT_OK) {
            val selectedImage = dataIntent.data
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
        if (image != null) {
            startChosenImage(image)
        }
    }

    private fun startChosenImage(image: Bitmap) {
        val byteArrayOutputStream = ByteArrayOutputStream()
        image.compress(Bitmap.CompressFormat.JPEG, 25, byteArrayOutputStream)
        val bitmapData = byteArrayOutputStream.toByteArray()

        val intent = Intent(this, ChosenImage::class.java)
        intent.putExtra("photo", bitmapData)
        intent.putExtra("model", radioButtonSelected)
        mySharedPreferencesEditor.remove("number").apply()
        startActivity(intent)
    }

    fun openCamera(view: View) {
        if (checkSelfPermission(android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(android.Manifest.permission.CAMERA), 1)
        } else if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)
        } else {
            dispatchTakePictureIntent()
        }
    }

    private fun dispatchTakePictureIntent() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent.resolveActivity(packageManager) != null) {
            val values = ContentValues()
            values.put(MediaStore.Images.Media.TITLE, "myPicture")
            values.put(
                MediaStore.Images.Media.DESCRIPTION,
                "Taken On " + System.currentTimeMillis()
            )
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