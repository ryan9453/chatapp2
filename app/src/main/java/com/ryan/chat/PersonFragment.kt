package com.ryan.chat

import android.Manifest.permission.*
import android.app.Activity
import android.app.ProgressDialog
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
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
import androidx.core.net.toUri
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.google.common.io.ByteStreams.copy
import com.google.common.io.Files.getFileExtension
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.storage.ktx.component1
import com.google.firebase.storage.ktx.component2
import java.io.*

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
    private val headViewModel by viewModels<HeadViewModel>()
    var count = 0

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
        auth = FirebaseAuth.getInstance()

        val parentActivity = requireActivity() as MainActivity
        val prefLogin = requireContext().getSharedPreferences("login", AppCompatActivity.MODE_PRIVATE)
        var login_userid = prefLogin.getString("login_userid", "")

        // 顯示用戶暱稱和帳號用
        val prefUser = requireContext().getSharedPreferences("userinfo", AppCompatActivity.MODE_PRIVATE)
        // 用取得的帳號去 userinfo資料夾索引取得暱稱
        val username = prefUser.getString("${login_userid}name", "")
        binding.tvPersonShowUserid.text = auth.currentUser?.email
        binding.tvPersonShowName.text = auth.currentUser?.displayName
        Glide.with(parentActivity).load(auth.currentUser?.photoUrl.toString())
            .into(parentActivity.binding.imHead)
        headViewModel.getHeadImageByGlide()
        headViewModel.head.value?.let { uri ->
            displayHeadImage(uri)
//            MainActivity().displaySmallHeadImage(uri)
        }

        headViewModel.head.observe(viewLifecycleOwner) { uri ->
            displayHeadImage(uri)
//            parentActivity.displaySmallHeadImage(uri)
        }

        // 登出按鈕
        // 登出後，將 login_state 改成 false 存回 shared_pref
        // 將首頁的 小頭貼跟名字隱藏
        binding.btLogout.setOnClickListener {
            val parentActivity =  requireActivity() as MainActivity
            prefLogin.edit()
                .putBoolean("login_state", false)
                .putString("login_userid", "")
                .commit()

            val resolver = requireActivity().contentResolver
            val defaultImagePath = "android.resource://com.ryan.chat/drawable/picpersonal"
            val defaultImageUri = Uri.parse(defaultImagePath)
            val bitMap = MediaStore.Images.Media.getBitmap(resolver, defaultImageUri)
            parentActivity.binding.tvHomeLoginUserid.text = ""
//            parentActivity.binding.imHead.setImageBitmap(bitMap)
            parentActivity.binding.imHead.visibility = View.GONE

            parentActivity.supportFragmentManager.beginTransaction().run {
                // mainFragments[3] = LoginFragment
                replace(R.id.main_container, parentActivity.mainFragments[3])
                commit()
            }
            auth.signOut()
        }


        // Camera 按鈕：危險權限檢查，如果都有權限，直接呼叫 Camera 方法
        // Album 按鈕：同上
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


    }

    // 若是剛得到使用者允許，專門檢查的方法
    // 確認有拿到權限後，呼叫相應的方法
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

    // 老師的寫法 (使用相機)
    private fun openCamera() {
        Log.d(TAG, "openCamera: 有開相機方法")

        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.TITLE, "My Picture")
            put(MediaStore.Images.Media.DESCRIPTION, "Testing")
        }

        // 利用 contentResolver 的 insert 方法，得到一個在「外部儲存位置」上的 新的 Uri
        // 且 Title = My Picture, Description = Testing
        imageUri = requireActivity().contentResolver.insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
//        imageUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI

        // 指定 cameraIntent OUTPUT 的 Uri 設為 imageUri
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
        Log.d(TAG, "openCamera: MediaStore.EXTRA_OUTPUT = ${MediaStore.EXTRA_OUTPUT}")
        Log.d(TAG, "openCamera: imageUri = $imageUri")
        startActivityForResult(cameraIntent, REQUEST_CAPTURE)
    }

    // 網路大神的寫法 (使用相機)
    private fun takeImageFromCameraWithIntent() {
        Log.d(TAG, "take image from camera")

        // 將照片暫時存入「外部暫存(快取)資料夾」，並命名為 My_Captured_Photo
        val captureImage = File(requireContext().externalCacheDir, "My_Captured_Photo")

        // 如果原本已經有就刪掉再新增
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

    // 網路大神的寫法 (使用相簿)
    private fun takeImageFromAlbumWithIntent() {
        Log.d(TAG, "take image from album")
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, ACTION_ALBUM_REQUEST_CODE)
    }

    // 從 RequestCode 去解析並作出相應的動作
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val parentActivity = requireActivity() as MainActivity

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
                    val resolver = requireActivity().contentResolver
//                    val bitMap = MediaStore.Images.Media.getBitmap(resolver, imageUri)
                    val imageString = imageUri.toString()
                    displayHeadImage(imageString)
                    parentActivity.displaySmallHeadImage(imageString)

                    // 上傳至 storage
//                    uploadImageToStorage(imageUri!!)

                    // 更新個人資訊
                    updateImageToProfile(imageUri!!)
                }
            }
            ACTION_ALBUM_REQUEST_CODE -> {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    val resolver = requireActivity().contentResolver
                    val bitMap = MediaStore.Images.Media.getBitmap(resolver, data.data)
                    imageUri = data.data!!
                    Log.d(TAG, "Action_album : imageUri = $imageUri")
//                    val albumFile = fileFromContentUri(requireContext(), imageUri!!)
//                    imageUri = albumFile.toUri()
                    val imageString = imageUri.toString()
                    Log.d(TAG, "Action_album : imageUri = $imageUri")
                    displayHeadImage(imageString)
                    parentActivity.displaySmallHeadImage(imageString)
                    // 上傳至 storage
//                    uploadImageToStorage(imageUri!!)

                    // 更新個人資訊
                    updateImageToProfile(imageUri!!)
                }
            }

        }
    }

    fun displayHeadImage(uri : String) {
        Glide.with(this).load(uri)
            .into(binding.imMyHead)
    }


    private fun uploadImageToStorage(uri:Uri) {

        val uid = auth.currentUser?.uid

        val storage = FirebaseStorage.getInstance()

        // 取得目前頭貼個數，新的檔名為既有頭貼數 +1
        val listRef = storage.reference.child("$uid/head")

        listRef.listAll().addOnSuccessListener { (items, prefixes) ->
            count = items.size+1
            Log.d(TAG, "uploadImageToStorage: count = $count")
            val storageRefImage = storage.reference.child("$uid/head/$count")
            storageRefImage.putFile(uri).addOnSuccessListener {
                Log.d(TAG, "uploadImageToStorage: 上傳成功")
            } .addOnFailureListener {
                Log.d(TAG, "uploadImageToStorage: 上傳失敗")
            }
        }

        Log.d(TAG, "uploadImageToStorage: count = $count")

        // 上傳中的示意視窗
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


    private fun fileFromContentUri(context: Context, contentUri: Uri): File {
        // Preparing Temp file name
        val fileExtension = getFileExtension(context, contentUri)
        val fileName = "temp_file" + if (fileExtension != null) ".$fileExtension" else ""

        // Creating Temp file
        val tempFile = File(context.cacheDir, fileName)
        tempFile.createNewFile()

        try {
            val oStream = FileOutputStream(tempFile)
            val inputStream = context.contentResolver.openInputStream(contentUri)

            inputStream?.let {
                copy(inputStream, oStream)
            }

            oStream.flush()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return tempFile
    }
    private fun getFileExtension(context: Context, uri: Uri): String? {
        val fileType: String? = context.contentResolver.getType(uri)
        return MimeTypeMap.getSingleton().getExtensionFromMimeType(fileType)
    }
    @Throws(IOException::class)
    private fun copy(source: InputStream, target: OutputStream) {
        val buf = ByteArray(8192)
        var length: Int
        while (source.read(buf).also { length = it } > 0) {
            target.write(buf, 0, length)
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

}