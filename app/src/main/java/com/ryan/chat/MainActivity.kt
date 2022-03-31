package com.ryan.chat

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.fragment.app.Fragment
import com.ryan.chat.databinding.ActivityMainBinding
import android.graphics.Bitmap
import android.widget.ImageView
import com.google.firebase.auth.FirebaseAuth


class MainActivity : AppCompatActivity() {
    companion object {
        val TAG = MainActivity::class.java.simpleName
    }

    lateinit var binding: ActivityMainBinding
    val mainFragments = mutableListOf<Fragment>()
    val chatFragments = mutableListOf<Fragment>()
    private val roomViewModel by viewModels<RoomViewModel>()
    private val headViewModel by viewModels<HeadViewModel>()
    lateinit var auth : FirebaseAuth


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
//        val sysLanguage = Locale.getDefault().getLanguage()
//        Log.d(TAG, "目前語言是 = $sysLanguage")

        // 小頭貼設置方式為置中
        binding.imHead.scaleType = ImageView.ScaleType.CENTER_CROP

        // 初始化權限實例
        auth = FirebaseAuth.getInstance()


        // 頭貼觀察者
        headViewModel.headImage.observe(this) { bitmap ->
            displaySmallHeadImage(bitmap)
        }
        // 使用頭貼 ViewModel 獲取此 user 當前 head
        headViewModel.getHeadImageByUid(auth.uid!!)
        headViewModel.getHeadImageByUserProfile(contentResolver)

        binding.searchContainer.visibility = View.GONE

        initFragments()


        binding.bottonNavBar.setOnItemSelectedListener { item ->

            when (item.itemId) {
                R.id.action_home -> {
                    supportFragmentManager.beginTransaction().run {
                        replace(R.id.main_container, mainFragments[1])
                        commit()
                    }
                    binding.searchContainer.visibility = View.GONE
                    true
                }
                R.id.action_person -> {
                    val prefLogin = getSharedPreferences("login", AppCompatActivity.MODE_PRIVATE)
                    val login = prefLogin.getBoolean("login_state", false)
                    Log.d(TAG, "login_state = $login")
                    if (login) {
                        Log.d(TAG, "有登入去個人資訊")
                        supportFragmentManager.beginTransaction().run {
                            replace(R.id.main_container, mainFragments[2])
                            commit() }
                    } else {
                        Log.d(TAG, "未登入去登入頁面")
                        supportFragmentManager.beginTransaction().run {
                            replace(R.id.main_container, mainFragments[3])
                            commit() }
                    }
                    binding.searchContainer.visibility = View.GONE
                    true
                }
                R.id.action_search -> {
                    supportFragmentManager.beginTransaction().run {
                        replace(R.id.main_container, mainFragments[5])
                        commit()
                    }
                    binding.searchContainer.visibility = View.VISIBLE
                    roomViewModel.chatRooms.observe(this) { rooms ->
                        HomeFragment().adapter.submitRooms(rooms)
                        Log.d(TAG, "第一間房間是 = ${rooms[0].nickname}")
                    }
                    roomViewModel.getHitRooms()
                    true
                }
                else -> true
            }
        }

    }

    private fun displaySmallHeadImage(bitmap: Bitmap?) {
        binding.imHead.setImageBitmap(bitmap)
    }

    override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        if (currentUser != null) {
            reload()
        }
    }

    private fun reload() {
        //
    }

    private fun initFragments() {
        mainFragments.add(0, EmptyFragment())
        mainFragments.add(1, HomeFragment.instance)
        mainFragments.add(2, PersonFragment.instance)
        mainFragments.add(3, LoginFragment.instance)
        mainFragments.add(4, SignUpFragment.instance)
        mainFragments.add(5, HitFragment.instance)
        mainFragments.add(6, PhotoFragment.instance)

        chatFragments.add(0, EmptyFragment.instance)
        chatFragments.add(1, RoomFragment.instance)
        supportFragmentManager.beginTransaction().run {
            add(R.id.main_container, mainFragments[1])
            add(R.id.chat_container, chatFragments[0])
            add(R.id.search_container, SearchFragment.instance)
            commit()
        }

    }
}