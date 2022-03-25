package com.ryan.chat

import android.Manifest.permission.CAMERA
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.ryan.chat.databinding.FragmentPersonBinding

class PersonFragment : Fragment() {
    companion object {
        val TAG = PersonFragment::class.java.simpleName
        val instance : PersonFragment by lazy {
            PersonFragment()
        }
        private var imageUri: Uri?=null
        private  val REQUEST_CAPTURE = 500
    }
    val PERMISSION_CAMERA = 300
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

        val prefLogin = requireContext().getSharedPreferences("login", AppCompatActivity.MODE_PRIVATE)
        var login_userid = prefLogin.getString("login_userid", "")

        // 顯示用戶暱稱和帳號用
        val prefUser = requireContext().getSharedPreferences("userinfo", AppCompatActivity.MODE_PRIVATE)
        // 用取得的帳號去 userinfo資料夾索引取得暱稱
        var username = prefUser.getString("${login_userid}name", "")
        binding.tvPersonShowUserid.setText(login_userid)
        binding.tvPersonShowName.setText(username)


        var path = "picpersonal"
        var imgPath = "android.resource://"+requireContext().packageName+"/drawable/$path"
        var imageUriFirst = Uri.parse(imgPath)
//        binding.imageView.setImageResource(R.drawable.jojo)
//        binding.imMyHead.setImageResource(R.drawable.picpersonal)
        binding.imMyHead.setImageURI(imageUriFirst)

        binding.btLogout.setOnClickListener {
            val parentActivity =  requireActivity() as MainActivity
            var login = prefLogin.getBoolean("login_state", false)
            login = false
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
            if (ActivityCompat.checkSelfPermission(requireContext(), CAMERA)
                == PackageManager.PERMISSION_DENIED ||
                    ActivityCompat.checkSelfPermission(requireContext(), WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_DENIED) {
                    ActivityCompat.requestPermissions(requireActivity(),
                    arrayOf(CAMERA, WRITE_EXTERNAL_STORAGE),
                        PERMISSION_CAMERA)
            } else {
                openCamera()
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_CAMERA) {
            openCamera()
        } else {
            Toast.makeText(requireContext(), "Permission denied", Toast.LENGTH_LONG)
        }
    }

    private fun openCamera() {
        val camera = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.TITLE, "My Picture")
            put(MediaStore.Images.Media.DESCRIPTION, "Testing")
        }
        val imageUri = requireActivity().contentResolver.insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        camera.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
        startActivityForResult(camera, REQUEST_CAPTURE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CAPTURE) {
            if (resultCode == Activity.RESULT_OK) {
                binding.imMyHead.setImageURI(imageUri)
            }
        }
    }
}