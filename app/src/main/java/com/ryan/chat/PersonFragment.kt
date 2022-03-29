package com.ryan.chat

import android.Manifest.permission.*
import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.ryan.chat.databinding.FragmentPersonBinding
import android.widget.ImageView

class PersonFragment : Fragment() {
    companion object {
        val TAG = PersonFragment::class.java.simpleName
        val instance : PersonFragment by lazy {
            PersonFragment()
        }
        private var imageUri: Uri?=null
        private val REQUEST_CAPTURE = 500
        private val ACTION_CAMERA_REQUEST_CODE = 100
        private val ACTION_ALBUM_REQUEST_CODE = 200

        private val PERMISSION_CAMERA = 300
        private val PERMISSION_ALBUM = 400
    }
    lateinit var binding: FragmentPersonBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentPersonBinding.inflate(inflater)
//        return super.onCreateView(inflater, container, savedInstanceState)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.imMyHead.scaleType = ImageView.ScaleType.CENTER_CROP

        val prefLogin = requireContext().getSharedPreferences("login", AppCompatActivity.MODE_PRIVATE)
        var login_userid = prefLogin.getString("login_userid", "")

        // 顯示用戶暱稱和帳號用
        val prefUser = requireContext().getSharedPreferences("userinfo", AppCompatActivity.MODE_PRIVATE)
        // 用取得的帳號去 userinfo資料夾索引取得暱稱
        val username = prefUser.getString("${login_userid}name", "")
        binding.tvPersonShowUserid.setText(login_userid)
        binding.tvPersonShowName.setText(username)



        binding.btLogout.setOnClickListener {
            val parentActivity =  requireActivity() as MainActivity
            val login: Boolean = false
            prefLogin.edit()
                .putBoolean("login_state", login)
                .putString("login_userid", "")
                .apply()
            Log.d(TAG, "Login_state = $login")
            parentActivity.binding.tvHomeLoginUserid.setText("")
            parentActivity.binding.imHead.visibility = View.GONE
            parentActivity.supportFragmentManager.beginTransaction().run {
                // mainFragments[3] = LoginFragment
                replace(R.id.main_container, parentActivity.mainFragments[3])
                commit()
            }
        }

        binding.btEditHead.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("take a picture")
                .setMessage("from Camera or Album")
                .setPositiveButton("Camera") { d, w ->
                    Log.d(TAG, "onViewCreated: ")
                    if (ActivityCompat.checkSelfPermission(requireContext(), CAMERA)
                        == PackageManager.PERMISSION_DENIED ||
                        ActivityCompat.checkSelfPermission(requireContext(), WRITE_EXTERNAL_STORAGE)
                        == PackageManager.PERMISSION_DENIED) {
                        ActivityCompat.requestPermissions(requireActivity(),
                            arrayOf(CAMERA, WRITE_EXTERNAL_STORAGE),
                            PERMISSION_CAMERA)
                    } else takeImageFromCameraWithIntent()
                }
                .setNeutralButton("Album") { d, w ->
                    if (ActivityCompat.checkSelfPermission(requireContext(), READ_EXTERNAL_STORAGE)
                        == PackageManager.PERMISSION_DENIED ||
                        ActivityCompat.checkSelfPermission(requireContext(), WRITE_EXTERNAL_STORAGE)
                        == PackageManager.PERMISSION_DENIED
                    ) {
                        ActivityCompat.requestPermissions(
                            requireActivity(),
                            arrayOf(READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE),
                            PERMISSION_ALBUM)
                    } else takeImageFromAlbumWithIntent()
                }
                .show()
        }


    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_CAMERA) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                takeImageFromCameraWithIntent()
            }
        } else if (requestCode == PERMISSION_ALBUM) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                takeImageFromAlbumWithIntent()
            }
        } else {
            Toast.makeText(requireContext(), "Permission denied", Toast.LENGTH_LONG).show()
        }
    }

    // 老師的寫法 (使用相機)
    private fun openCamera() {
        Log.d(TAG, "openCamera: 有開相機方法")
        val camera = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.TITLE, "My Picture")
            put(MediaStore.Images.Media.DESCRIPTION, "Testing")
        }
        imageUri = requireActivity().contentResolver.insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        camera.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
        Log.d(TAG, "openCamera: MediaStore.EXTRA_OUTPUT = ${MediaStore.EXTRA_OUTPUT}")
        Log.d(TAG, "openCamera: imageUri = $imageUri")
        startActivityForResult(camera, REQUEST_CAPTURE)
    }

    // 網路大神的寫法 (使用相機)
    private fun takeImageFromCameraWithIntent() {
        Log.d(TAG, "take image from camera")
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent, ACTION_CAMERA_REQUEST_CODE)
    }

    // 網路大神的寫法 (使用相簿)
    private fun takeImageFromAlbumWithIntent() {
        Log.d(TAG, "take image from album")
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, ACTION_ALBUM_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            REQUEST_CAPTURE -> {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    Log.d(TAG, "成功")
                    Log.d(TAG, "imageUri = $imageUri")
                    binding.imMyHead.setImageURI(imageUri)
                }
            }

            ACTION_CAMERA_REQUEST_CODE -> {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    displayImage(data.extras?.get("data") as Bitmap)
                }
            }
            ACTION_ALBUM_REQUEST_CODE -> {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    val resolver = requireActivity().contentResolver
                    val bitmap = MediaStore.Images.Media.getBitmap(resolver, data.data)
                    displayImage(bitmap)
                }
            }

        }
    }

    private fun displayImage(bitmap: Bitmap) {
        binding.imMyHead.setImageBitmap(bitmap)
    }

}