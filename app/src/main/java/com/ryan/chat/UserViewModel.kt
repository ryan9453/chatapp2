package com.ryan.chat

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.storage.FirebaseStorage
import java.io.File

class UserViewModel : ViewModel() {

    companion object {
        val TAG = UserViewModel::class.java.simpleName
    }
    lateinit var auth : FirebaseAuth
    val headImage = MutableLiveData<Bitmap>()
    val headLive = MutableLiveData<String>()
    val userLive = MutableLiveData<FirebaseUser?>()
    val nickNameLive = MutableLiveData<String>()
    val userIdLive = MutableLiveData<String>()

    fun getFireUser() {
        val auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser
        userLive.postValue(currentUser)
    }

    fun getFireUserInfo() {
        val auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser
        userLive.postValue(currentUser)
        if (currentUser != null) {
            val email = currentUser.email
            val userId = email?.subSequence(0, email.indexOf('@'))
            headLive.postValue(auth.currentUser?.photoUrl.toString())
            nickNameLive.postValue(auth.currentUser?.displayName)
            userIdLive.postValue(userId.toString())

        }
        Log.d(TAG, "getFireUserInfo: user = ${userLive.value}")
    }


    fun getHeadImageByUid(uid: String) {
        val storage = FirebaseStorage.getInstance()
        val storageRef = storage.reference.child("$uid/head/head")
        val localFile = File.createTempFile("tempImage", "jpg")

        storageRef.getFile(localFile).addOnSuccessListener {
            val bitMap = BitmapFactory.decodeFile(localFile.absolutePath)
            headImage.postValue(bitMap)
        }
    }

    fun getHeadImageByUserProfile(resolver: ContentResolver) {
        auth = FirebaseAuth.getInstance()
        val user = auth.currentUser

        val defaultImagePath = "android.resource://com.ryan.chat/drawable/picpersonal"
        val defaultImageUri = Uri.parse(defaultImagePath)

        user?.let {
           val headUri = if (user.photoUrl != null) user.photoUrl else defaultImageUri
            Log.d(TAG, "headUri = $headUri")
            val bitMap = MediaStore.Images.Media.getBitmap(resolver, headUri)
            headImage.postValue(bitMap)
        }
    }

    fun getHeadImageByGlide() {
        auth = FirebaseAuth.getInstance()
        val user = auth.currentUser
        val defaultImagePath = "android.resource://com.ryan.chat/drawable/picpersonal"
        val defaultImageUri = Uri.parse(defaultImagePath)

        user?.let {
            val headUri = if (user.photoUrl != null) user.photoUrl else defaultImageUri
            Log.d(TAG, "headUri = $headUri")

            val headUriString = headUri.toString()
            headLive.postValue(headUriString)
        }

    }



}