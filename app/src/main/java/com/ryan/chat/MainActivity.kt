package com.ryan.chat

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.fragment.app.Fragment
import com.ryan.chat.databinding.ActivityMainBinding
import android.widget.ImageView
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import nl.joery.animatedbottombar.AnimatedBottomBar
import java.util.*
import kotlin.math.log
import kotlin.properties.Delegates


class MainActivity : AppCompatActivity() {
    companion object {
        val TAG = MainActivity::class.java.simpleName
    }

    lateinit var binding: ActivityMainBinding
    val mainFragments = mutableListOf<Fragment>()
    val chatFragments = mutableListOf<Fragment>()
    private val userViewModel by viewModels<UserViewModel>()
    private val roomViewModel by viewModels<RoomViewModel>()
    lateinit var headObserver: Observer<String>
    lateinit var nickNameObserver: Observer<String>
    lateinit var loginObserver: Observer<Boolean>
//    var loginState by Delegates.notNull<Boolean>()


    override fun onResume() {
        super.onResume()
        if (userViewModel.loginLive.value == true) {
//            binding.imHead.visibility = View.VISIBLE
//            binding.tvHomeLoginNickname.visibility = View.VISIBLE
            userViewModel.headLive.value?.let { displaySmallHeadImage(it) }
            Log.d(TAG, "head.value = ${userViewModel.headLive.value}")
            Log.d(TAG, "login.value = true")
        } else {
            binding.tvHomeLoginNickname.visibility = View.GONE
            binding.imHead.visibility = View.GONE
            Log.d(TAG, "login.value = false")
        }
        Log.d(TAG, "onResume: ")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 隱藏 ActionBar
        supportActionBar?.hide()

        // 小頭貼設置方式為置中
        binding.imHead.scaleType = ImageView.ScaleType.CENTER_CROP

        binding.searchContainer.visibility = View.GONE

        initFragments()

        loginObserver = Observer { login ->
            if (login) {
//                userViewModel.headLive.value?.let { displaySmallHeadImage(it) }
                binding.tvHomeLoginNickname.text = userViewModel.nickNameLive.value
                Log.d(TAG, "onStart: change to true")
            } else {
                Log.d(TAG, "onStart: change to false")
            }
        }


         // 設置頭貼觀察者
//        userViewModel.headLive.observe(this) { uri ->
//            Log.d(TAG, "onCreate: uri = $uri")
//            displaySmallHeadImage(uri)
//        }

//         初始化並設置頭貼觀察者
//        headObserver = Observer { uri ->
//            Log.d(TAG, "onStart: uri = $uri")
//            val defaultImagePath = "android.resource://com.ryan.chat/drawable/picpersonal"
//            val defaultImageUri = Uri.parse(defaultImagePath)
//            if (uri == defaultImageUri.toString()) {
//                binding.imHead.visibility = View.GONE
//                Log.d(TAG, "onStart: 有進來")
//            } else displaySmallHeadImage(uri)
//        }

//        // 設置暱稱觀察者
//        userViewModel.nickNameLive.observe(this) { nickName ->
//            binding.tvHomeLoginNickname.text = nickName
//            Log.d(TAG, "onCreate: nickName = $nickName")
//        }
//        初始化並設置暱稱觀察者
//        nickNameObserver = Observer { nickName ->
//            binding.tvHomeLoginNickname.text = nickName
//            Log.d(TAG, "onStart: nickName = $nickName")
//        }

        userViewModel.getFireUserInfo()

         // 設置登入觀察者
        userViewModel.loginLive.observe(this) { login ->
            if (login) {
//                userViewModel.headLive.value?.let { displaySmallHeadImage(it) }
                binding.tvHomeLoginNickname.text = userViewModel.nickNameLive.value
                Log.d(TAG, "onCreate: change to true")
            } else {
                Log.d(TAG, "onCreate: change to false")
            }
        }
        binding.bottomBar.setOnTabSelectListener(object : AnimatedBottomBar.OnTabSelectListener{
            override fun onTabSelected(
                lastIndex: Int,
                lastTab: AnimatedBottomBar.Tab?,
                newIndex: Int,
                newTab: AnimatedBottomBar.Tab
            ) {
                when (newTab.id) {
                    R.id.action_home -> {
                        if (userViewModel.loginLive.value == true) {
                            userViewModel.headLive.value?.let { displaySmallHeadImage(it) }
                            binding.tvHomeLoginNickname.visibility = View.VISIBLE
                            binding.imHead.visibility = View.VISIBLE
                            Log.d(TAG, "R.id_home: 這邊是 true")
                        } else {
                            binding.tvHomeLoginNickname.visibility = View.GONE
                            binding.imHead.visibility = View.GONE
                            Log.d(TAG, "R.id_home: 這邊是 false")
                        }
                        roomViewModel.getAllRooms()
                        supportFragmentManager.beginTransaction().run {
                            replace(R.id.main_container, mainFragments[1])
                            commit()
                        }
                        binding.searchContainer.visibility = View.GONE
                    }
                    R.id.action_person -> {
//                        binding.tvHomeLoginNickname.visibility = View.GONE
//                        binding.imHead.visibility = View.GONE
                        if (userViewModel.loginLive.value == true) {
                            Log.d(TAG, "有登入去個人資訊")
                            supportFragmentManager.beginTransaction().run {
                                replace(R.id.main_container, mainFragments[2])
                                commit()
                            }
                        } else {
                            Log.d(TAG, "未登入去登入頁面")
                            supportFragmentManager.beginTransaction().run {
                                replace(R.id.main_container, mainFragments[3])
                                commit()
                            }
                        }
                        binding.searchContainer.visibility = View.GONE
                    }
                    R.id.action_search -> {
                        if (userViewModel.loginLive.value == true) {
                            binding.tvHomeLoginNickname.visibility = View.VISIBLE
                            binding.imHead.visibility = View.VISIBLE
                        } else {
                            binding.tvHomeLoginNickname.visibility = View.GONE
                            binding.imHead.visibility = View.GONE
                        }
                        roomViewModel.getHitRooms()
                        supportFragmentManager.beginTransaction().run {
                            replace(R.id.main_container, mainFragments[1])
                        }
                        // 跳轉至 HitFragment
//                        supportFragmentManager.beginTransaction().run {
//                            replace(R.id.main_container, mainFragments[5])
//                            commit()
//                        }
//                        roomViewModel.getHitRooms()
                        binding.searchContainer.visibility = View.VISIBLE
                    }
                }
            }

        })


//        binding.bottomNavBar.setOnItemSelectedListener { item ->
//
//            when (item.itemId) {
//                R.id.action_home -> {
//                    if (userViewModel.loginLive.value == true) {
//                        binding.tvHomeLoginNickname.visibility = View.VISIBLE
//                        binding.imHead.visibility = View.VISIBLE
//                    } else {
//                        binding.tvHomeLoginNickname.visibility = View.GONE
//                        binding.imHead.visibility = View.GONE
//                    }
//                    supportFragmentManager.beginTransaction().run {
//                        replace(R.id.main_container, mainFragments[1])
//                        commit()
//                    }
//                    binding.searchContainer.visibility = View.GONE
//                    true
//                }
//                R.id.action_person -> {
//                    binding.tvHomeLoginNickname.visibility = View.GONE
//                    binding.imHead.visibility = View.GONE
//                    if (userViewModel.loginLive.value == true) {
//                        Log.d(TAG, "有登入去個人資訊")
//                        supportFragmentManager.beginTransaction().run {
//                            replace(R.id.main_container, mainFragments[2])
//                            commit()
//                        }
//                    } else {
//                        Log.d(TAG, "未登入去登入頁面")
//                        supportFragmentManager.beginTransaction().run {
//                            replace(R.id.main_container, mainFragments[3])
//                            commit()
//                        }
//                    }
//                    binding.searchContainer.visibility = View.GONE
//                    true
//                }
//                R.id.action_search -> {
//                    if (userViewModel.loginLive.value == true) {
//                        binding.tvHomeLoginNickname.visibility = View.VISIBLE
//                        binding.imHead.visibility = View.VISIBLE
//                    } else {
//                        binding.tvHomeLoginNickname.visibility = View.GONE
//                        binding.imHead.visibility = View.GONE
//                    }
//                    supportFragmentManager.beginTransaction().run {
//                        replace(R.id.main_container, mainFragments[5])
//                        commit()
//                    }
//                    binding.searchContainer.visibility = View.VISIBLE
//                    true
//                }
//                else -> true
//            }
//        }
    }

    fun displaySmallHeadImage(uri: String) {
        Glide.with(this).load(uri)
            .into(binding.imHead)
    }


    private fun initFragments() {
        mainFragments.add(0, EmptyFragment())
        mainFragments.add(1, HomeFragment.instance)
        mainFragments.add(2, PersonFragment.instance)
        mainFragments.add(3, LoginFragment.instance)
        mainFragments.add(4, SignUpFragment.instance)

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