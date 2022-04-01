package com.ryan.chat

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.ryan.chat.databinding.FragmentSignUpBinding
import com.google.firebase.auth.UserProfileChangeRequest as UserProfileChangeRequest

class SignUpFragment : Fragment() {
    companion object {
        val TAG = SignUpFragment::class.java.simpleName
        val instance : SignUpFragment by lazy {
            SignUpFragment()
        }
    }
    private lateinit var binding: FragmentSignUpBinding
    private lateinit var auth: FirebaseAuth
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSignUpBinding.inflate(inflater)
        return binding.root
//        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        val db = Firebase.firestore

        binding.btSignSend.setOnClickListener {

            // 取輸入方塊的值
            val nickName = binding.edSignName.text.toString()
            val userId = binding.edSignUserid.text.toString()
            val email = "$userId@gmail.com"
            val pwd = binding.edSignPwd.text.toString()
            val pwdg = binding.edSignPwdAgain.text.toString()




            var errorText = ""

            errorText =
                when {
                    CheckNumber(userId).userId() == CheckNumber.NumberState.TOOSHORT -> getString(R.string.userid_too_short)
                    CheckNumber(userId).userId() == CheckNumber.NumberState.TOOLONG -> getString(R.string.userid_too_long)
                    CheckNumber(userId).userId() == CheckNumber.NumberState.WRONG -> getString(R.string.userid_is_wrong)
                    CheckNumber(pwd).passWord() == CheckNumber.NumberState.TOOSHORT -> getString(R.string.pwd_too_short)
                    CheckNumber(pwd).passWord() == CheckNumber.NumberState.TOOLONG -> getString(R.string.pwd_too_long)
                    CheckNumber(pwd).passWord() == CheckNumber.NumberState.WRONG -> getString(R.string.pwd_is_wrong)
                    pwd != pwdg -> getString(R.string.pwd_is_not_same)
                    else -> ""
                }

            // 帳密規則正確
            if (errorText == "") {

                signUp(email, userId, pwd, nickName, db)
                updateProfileFirst(nickName)

            // 錯誤訊息對話框
            } else {
                AlertDialog.Builder(requireContext())
                    .setTitle(getString(R.string.wrong_message))
                    .setMessage(errorText)
                    .setPositiveButton("OK",null)
                    .show()
            }

        }


        // 註冊完成的送出按鈕
//        binding.btSignSend.setOnClickListener {
//            // 取輸入方塊的值
//            // 建立登入狀態變數，初始值為 false
//
//            val name = binding.edSignName.text.toString()
//            val user = binding.edSignUserid.text.toString()
//            val pwd = binding.edSignPwd.text.toString()
//            val pwdg = binding.edSignPwdAgain.text.toString()
//            var login_state: Boolean = false
//            val prefUser = requireContext().getSharedPreferences("userinfo", AppCompatActivity.MODE_PRIVATE)
//            val prefLogin = requireContext().getSharedPreferences("login", AppCompatActivity.MODE_PRIVATE)
//            /*
//                帳密規則驗證
//                正確:
//                    將「名稱」「帳號」「密碼」「登入狀態」存在 shared_prefs資料夾
//                    彈出「註冊成功對話框」並詢問是否記住登入狀態爾後跳轉至 MainA
//                錯誤:
//                    彈出「錯誤訊息對話框」
//            */
//            var error_text = ""
//            error_text =
//                when {
//                    CheckNumber(user).userId() == CheckNumber.NumberState.TOOSHORT -> getString(R.string.userid_too_short)
//                    CheckNumber(user).userId() == CheckNumber.NumberState.TOOLONG -> getString(R.string.userid_too_long)
//                    CheckNumber(user).userId() == CheckNumber.NumberState.WRONG -> getString(R.string.userid_is_wrong)
//                    CheckNumber(pwd).passWord() == CheckNumber.NumberState.TOOSHORT -> getString(R.string.pwd_too_short)
//                    CheckNumber(pwd).passWord() == CheckNumber.NumberState.TOOLONG -> getString(R.string.pwd_too_long)
//                    CheckNumber(pwd).passWord() == CheckNumber.NumberState.WRONG -> getString(R.string.pwd_is_wrong)
//                    pwd != pwdg -> getString(R.string.pwd_is_not_same)
//                    else -> ""
//                }
//
//            // 將暱稱帳號密碼存本地 √
//            // 彈出對話框，內容為「註冊成功」並詢問是否要記住登入狀態
//            if (error_text == "") {
//                val parentActivity =  requireActivity() as MainActivity
//
//                // 將帳密和登入狀態一起存入本地
//                prefUser.edit()
//                    .putString("${user}name", name)
//                    .putString("$user", user)
//                    .putString("${user}pwd", pwd)
//                    .apply()
//                prefLogin.edit()
//                    .putBoolean("login_state", true)
//                    .putString("login_userid", user)
//                    .apply()
//
//                Log.d(TAG, "帳密輸入沒問題")
//
//                AlertDialog.Builder(requireContext())
//                    .setTitle(getString(R.string.message))
//                    .setMessage(getString(R.string.sign_up_successfully))
//
//                    // 若按 OK 登入狀態改成 true並將此次帳號存入資料夾
//                    .setPositiveButton(getString(R.string.ok), null)
//                    .show()
//
//                // 跳轉回 Home
//                parentActivity.supportFragmentManager.beginTransaction().run {
//                    replace(R.id.main_container, parentActivity.mainFragments[1])
//                    parentActivity.binding.tvHomeLoginUserid.setText(user)
//                    commit()
//                }
//                binding.edSignName.setText("")
//                binding.edSignPwd.setText("")
//                binding.edSignUserid.setText("")
//                binding.edSignPwdAgain.setText("")
//
//                // 錯誤訊息對話框
//            } else {
//                AlertDialog.Builder(requireContext())
//                    .setTitle(getString(R.string.wrong_message))
//                    .setMessage(error_text)
//                    .setPositiveButton("OK",null)
//                    .show()
//            }
//
//        }

        binding.btBackToLogin.setOnClickListener {
            val parentActivity =  requireActivity() as MainActivity
            parentActivity.supportFragmentManager.beginTransaction().run {
                replace(R.id.main_container, parentActivity.mainFragments[3])
                commit()
            }
        }
    }

    private fun signUp(email:String, userId:String, pwd:String, nickName:String, db:FirebaseFirestore) {
        val parentActivity =  requireActivity() as MainActivity
        val prefLogin = requireContext().getSharedPreferences("login", AppCompatActivity.MODE_PRIVATE)

        auth.createUserWithEmailAndPassword(email, pwd).addOnCompleteListener { task ->
            if (task.isSuccessful) {

                // 註冊成功對話窗
                AlertDialog.Builder(requireContext())
                    .setTitle(getString(R.string.message))
                    .setMessage(getString(R.string.sign_up_successfully))
                    .setPositiveButton(getString(R.string.ok), null)
                    .show()

                prefLogin.edit()
                    .putBoolean("login_state", true)
                    .putString("login_userid", userId)
                    .apply()

                // 將 firebase 自動生成的 uid 取出來
                // 以 uid 當 key 去幫用戶儲存文字資料在 firestore
                val user = auth.currentUser
                val uid = user?.uid

                // 將用戶資訊以 firestore 的儲存格式存入
                val userInfo = hashMapOf(
                    "Nickname" to nickName
                )

                // 將用戶資訊創建在 「userinfo」的 collection 下
                // 並以註冊時產生的 uid 當 key
                db.collection("userinfo").document(uid!!)
                    .set(userInfo)
                    .addOnSuccessListener { Log.d(TAG, "DocumentSnapshot successfully written!") }
                    .addOnFailureListener { e -> Log.w(TAG, "Error writting document", e) }


                // 跳轉回 Home
                parentActivity.supportFragmentManager.beginTransaction().run {
                    replace(R.id.main_container, parentActivity.mainFragments[1])
                    parentActivity.binding.tvHomeLoginUserid.setText(userId)
                    commit()
                }

                // 清除輸入框
                binding.edSignName.text.clear()
                binding.edSignPwd.text.clear()
                binding.edSignUserid.text.clear()
                binding.edSignPwdAgain.text.clear()

            }
        }
    }

    private fun updateProfileFirst(nickName: String) {
        val headUri = Uri.parse("android.resource://${requireContext().packageName}/${R.drawable.picpersonal}")
        auth.currentUser?.let { user ->
            val profileUpdates = UserProfileChangeRequest.Builder()
                .setDisplayName(nickName)
                .setPhotoUri(headUri)
                .build()
            user.updateProfile(profileUpdates)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d(TAG, "User first profile updated.")
                    }
                }
        }
    }
}