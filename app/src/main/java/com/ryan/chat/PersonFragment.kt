package com.ryan.chat

import android.Manifest.permission.*
import android.app.Activity
import android.app.ProgressDialog
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.ryan.chat.databinding.FragmentPersonBinding
import android.widget.ImageView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import androidx.core.content.FileProvider
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.storage.ktx.component1
import com.google.firebase.storage.ktx.component2
import java.io.*
import java.text.SimpleDateFormat
import java.util.*
import android.view.View as View1

class PersonFragment : Fragment() {
    companion object {
        val TAG = PersonFragment::class.java.simpleName
        val instance : PersonFragment by lazy {
            PersonFragment()
        }
        private var imageUri: Uri?=null
        private const val REQUEST_CAPTURE = 500
        private const val ACTION_CAMERA_REQUEST_CODE = 100
        private const val ACTION_ALBUM_REQUEST_CODE = 200

        private const val PERMISSION_CAMERA = 300
        private const val PERMISSION_ALBUM = 400
    }
    lateinit var binding: FragmentPersonBinding
    lateinit var auth : FirebaseAuth
    private val userViewModel by activityViewModels<UserViewModel>()
    var count = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View1? {
        binding = FragmentPersonBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View1, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btEditNameOk.visibility = android.view.View.INVISIBLE
        binding.edPersonName.visibility = android.view.View.INVISIBLE
        binding.tvPersonShowUserid.text = userViewModel.userIdLive.value
        binding.tvPersonShowName.text = userViewModel.nickNameLive.value
        userViewModel.headLive.value?.let { displayHeadImage(it) }

        // ?????? user ????????????
        binding.imMyHead.scaleType = ImageView.ScaleType.CENTER_CROP


        val parentActivity = requireActivity() as MainActivity

        // ????????????
        // ???????????? ????????????????????????
        binding.btLogout.setOnClickListener {
            auth = FirebaseAuth.getInstance()

            // firebase ??????
            auth.signOut()
            userViewModel.getFireUserInfo()

            //  ????????? Login ??????
            parentActivity.supportFragmentManager.beginTransaction().run {
                // mainFragments[3] = LoginFragment
                replace(R.id.main_container, parentActivity.mainFragments[3])
                commit()
            }

        }

        // Camera ??????????????????????????????????????????????????????????????? Camera ??????
        // Album ???????????????
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
                    } else {
                        takeImageFromCameraWithIntent()
//                        openCamera()
                    }
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

        binding.btEditName.setOnClickListener {
            binding.tvPersonShowName.visibility = android.view.View.INVISIBLE
            binding.btEditName.visibility = android.view.View.INVISIBLE
            binding.edPersonName.visibility = android.view.View.VISIBLE
            binding.btEditNameOk.visibility = android.view.View.VISIBLE
        }

        binding.btEditNameOk.setOnClickListener {
            binding.tvPersonShowName.visibility = android.view.View.VISIBLE
            binding.btEditName.visibility = android.view.View.VISIBLE
            binding.edPersonName.visibility = android.view.View.INVISIBLE
            binding.btEditNameOk.visibility = android.view.View.INVISIBLE
            val newName = binding.edPersonName.text.toString()
            updateNickNameToProfile(newName)
            userViewModel.nickNameLive.value = newName
            binding.tvPersonShowName.text = newName
            parentActivity.binding.tvHomeLoginNickname.text = newName

        }

    }


    // ??????????????????????????????????????????????????????
    // ????????????????????????????????????????????????
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_CAMERA) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                takeImageFromCameraWithIntent()
//                openCamera()
            }
        } else if (requestCode == PERMISSION_ALBUM) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                takeImageFromAlbumWithIntent()
            }
        } else {
            Toast.makeText(requireContext(), "Permission denied", Toast.LENGTH_LONG).show()
        }
    }

    // ??????????????? (????????????)
    private fun openCamera() {
        Log.d(TAG, "openCamera: ??????????????????")

        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.TITLE, "My Picture")
            put(MediaStore.Images.Media.DESCRIPTION, "Testing")
        }

        // ?????? contentResolver ??? insert ?????????????????????????????????????????????????????? ?????? Uri
        // ??? Title = My Picture, Description = Testing
        imageUri = requireActivity().contentResolver.insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
//        imageUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI

        // ?????? cameraIntent OUTPUT ??? Uri ?????? imageUri
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
        Log.d(TAG, "openCamera: MediaStore.EXTRA_OUTPUT = ${MediaStore.EXTRA_OUTPUT}")
        Log.d(TAG, "openCamera: imageUri = $imageUri")
        startActivityForResult(cameraIntent, REQUEST_CAPTURE)
    }

    // ????????????????????? (????????????)
    private fun takeImageFromCameraWithIntent() {
        Log.d(TAG, "take image from camera")

        val formatter = SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.getDefault())
        val now = Date()
        val fileName = formatter.format(now)

        // ????????????????????????????????????(??????)??????????????????????????? My_Captured_Photo
        val captureImage = File(requireContext().externalCacheDir, fileName)

        // ???????????????????????????????????????
        if (captureImage.exists()) {
            captureImage.delete()
        }
        captureImage.createNewFile()

        imageUri = if (Build.VERSION.SDK_INT >= 24) {
            FileProvider.getUriForFile(requireContext(), "com.ryan.chat.fileProvider", captureImage)
        } else {
            Uri.fromFile(captureImage)
        }
        Log.d(TAG, "takeImageFromCameraWithIntent: imageUri = $imageUri")
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
        startActivityForResult(intent, ACTION_CAMERA_REQUEST_CODE)
    }

    // ????????????????????? (????????????)
    private fun takeImageFromAlbumWithIntent() {
        Log.d(TAG, "take image from album")
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, ACTION_ALBUM_REQUEST_CODE)
    }

    // ??? RequestCode ?????????????????????????????????
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val parentActivity = requireActivity() as MainActivity

        when (requestCode) {
            REQUEST_CAPTURE -> {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    Log.d(TAG, "??????")
                    Log.d(TAG, "imageUri = $imageUri")
                    binding.imMyHead.setImageURI(imageUri)
                }
            }

            ACTION_CAMERA_REQUEST_CODE -> {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    displayHeadImage(imageUri.toString())
//                    parentActivity.displaySmallHeadImage(imageUri.toString())
                    userViewModel.getNewHead(imageUri.toString())

                    // ????????? storage
//                    uploadImageToStorage(imageUri!!)

                    // ??????????????????
                    updateImageToProfile(imageUri!!)
                }
            }
            ACTION_ALBUM_REQUEST_CODE -> {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    imageUri = data.data!!
                    Log.d(TAG, "Action_album : imageUri = $imageUri")
//                    val albumFile = fileFromContentUri(requireContext(), imageUri!!)
//                    imageUri = albumFile.toUri()

                    // ???????????????
                    displayHeadImage(imageUri.toString())
                    userViewModel.getNewHead(imageUri.toString())
//                    parentActivity.displaySmallHeadImage(imageUri.toString())

                    // ????????? storage
//                    uploadImageToStorage(imageUri!!)

                    // ??????????????????
                    updateImageToProfile(imageUri!!)
                }
            }

        }
    }

    private fun displayHeadImage(uri : String) {
        Log.d(TAG, "displayHeadImage: uri = $uri")
        Glide.with(this@PersonFragment).load(uri)
            .into(binding.imMyHead)
    }


    private fun uploadImageToStorage(uri:Uri) {

        auth = FirebaseAuth.getInstance()
        val uid = auth.currentUser?.uid

        val storage = FirebaseStorage.getInstance()

        // ????????????????????????????????????????????????????????? +1
        val listRef = storage.reference.child("$uid/head")

        listRef.listAll().addOnSuccessListener { (items, prefixes) ->
            count = items.size+1
            Log.d(TAG, "uploadImageToStorage: count = $count")
            val storageRefImage = storage.reference.child("$uid/head/$count")
            storageRefImage.putFile(uri).addOnSuccessListener {
                Log.d(TAG, "uploadImageToStorage: ????????????")
            } .addOnFailureListener {
                Log.d(TAG, "uploadImageToStorage: ????????????")
            }
        }

        Log.d(TAG, "uploadImageToStorage: count = $count")

        // ????????????????????????
        val progressDialog = ProgressDialog(requireContext())
        progressDialog.setMessage("Uploading File...")
        progressDialog.setCancelable(false)
        progressDialog.show()

        val storageRefHead = storage.reference.child("$uid/head/head")
        storageRefHead.let { ref ->
            ref.putFile(uri).
            addOnSuccessListener {
                Toast.makeText(requireContext(), "Successfully upload", Toast.LENGTH_LONG).show()
                if (progressDialog.isShowing) progressDialog.dismiss()
            } .addOnFailureListener {

                Toast.makeText(requireContext(), "Failed", Toast.LENGTH_LONG).show()
                if (progressDialog.isShowing) progressDialog.dismiss()
            }
        }
    }

    private fun updateImageToProfile(headUri:Uri) {
        auth = FirebaseAuth.getInstance()
        auth.currentUser?.let { user ->
            val profileUpdates = UserProfileChangeRequest.Builder()
                .setPhotoUri(headUri)
                .build()
            user.updateProfile(profileUpdates)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d(SignUpFragment.TAG, "new image is updated.")
                    }
                }
        }
    }

    private fun updateNickNameToProfile(newName : String) {
        auth = FirebaseAuth.getInstance()
        auth.currentUser?.let { user ->
            val profileUpdates = UserProfileChangeRequest.Builder()
                .setDisplayName(newName)
                .build()
            user.updateProfile(profileUpdates)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d(SignUpFragment.TAG, "new name is updated.")
                    }
                }
        }
    }
}