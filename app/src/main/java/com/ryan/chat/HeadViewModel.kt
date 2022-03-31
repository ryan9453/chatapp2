package com.ryan.chat

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.provider.MediaStore
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import java.io.File

class HeadViewModel : ViewModel() {

    companion object {
        val TAG = HeadViewModel::class.java.simpleName
    }
    lateinit var auth : FirebaseAuth

    val headImage = MutableLiveData<Bitmap>()

    fun getHeadImageByUid(uid: String) {
        val storage = FirebaseStorage.getInstance()
        val storageRef = storage.reference.child("$uid/head/head")
        val localFile = File.createTempFile("tempImage", "jpg")

        storageRef.getFile(localFile).addOnSuccessListener {
            val bitMap = BitmapFactory.decodeFile(localFile.absolutePath)
            headImage.value = bitMap
        }
    }

    fun getHeadImageByUserProfile(resolver: ContentResolver) {
        auth = FirebaseAuth.getInstance()
        val user = auth.currentUser
        user?.let {
            val headUri = user.photoUrl
            val bitMap = MediaStore.Images.Media.getBitmap(resolver, headUri)
            headImage.value = bitMap

        }
    }

}