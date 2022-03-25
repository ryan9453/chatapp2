package com.ryan.chat

import android.Manifest.permission.CAMERA
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.ryan.chat.databinding.ActivityCameraBinding

class CameraActivity : AppCompatActivity() {

    companion object {
        val TAG = CameraActivity::class.java.simpleName
        val PERMISSION_CAMERA = 300
        private var imageUri: Uri? = null
        private val REQUEST_CAPTURE = 500
    }
    lateinit var binding : ActivityCameraBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCameraBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (ActivityCompat.checkSelfPermission(this, CAMERA)
            == PackageManager.PERMISSION_DENIED ||
                ActivityCompat.checkSelfPermission(this, WRITE_EXTERNAL_STORAGE)
            ==PackageManager.PERMISSION_DENIED) {
                    ActivityCompat.requestPermissions(this,
                    arrayOf(CAMERA, WRITE_EXTERNAL_STORAGE),
                    PERMISSION_CAMERA)
        } else {
            openCamera()
        }

    }

    private fun openCamera() {
        val camera = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.TITLE, "My Picture")
            put(MediaStore.Images.Media.DESCRIPTION, "Testing")
        }
        val imageUri = contentResolver.insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        camera.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
        startActivityForResult(camera, REQUEST_CAPTURE)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_CAMERA) {
            if (grantResults.size > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera()
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CAPTURE) {
            if (resultCode == Activity.RESULT_OK) {
//                binding.imCamera.setImageURI(imageUri)
                finish()
                PersonFragment().binding.imMyHead.setImageURI(imageUri)
            }
        }
    }
}