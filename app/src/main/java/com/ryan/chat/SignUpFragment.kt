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
import androidx.fragment.app.activityViewModels
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.ryan.chat.databinding.FragmentSignUpBinding
import com.google.firebase.auth.UserProfileChangeRequest as UserProfileChangeRequest

class SignUpFragment : Fragment() {
    companion object {
        val TAG: String = SignUpFragment::class.java.simpleName
        val instance : SignUpFragment by lazy {
            SignUpFragment()
        }
    }
    private lateinit var binding: FragmentSignUpBinding
    private lateinit var auth: FirebaseAuth
    private val userViewModel by activityViewModels<UserViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSignUpBinding.inflate(inflater)
        return binding.root
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

            // 錯誤訊息對話框
            } else {
                AlertDialog.Builder(requireContext())
                    .setTitle(getString(R.string.wrong_message))
                    .setMessage(errorText)
                    .setPositiveButton("OK",null)
                    .show()
            }
        }

        binding.btBackToLogin.setOnClickListener {
            val parentActivity =  requireActivity() as MainActivity
            parentActivity.supportFragmentManager.beginTransaction().run {
                replace(R.id.main_container, parentActivity.mainFragments[3])
                commit()
            }
        }
    }

    private fun signUp(email:String, userId:String, pwd:String, nickName:String, db:FirebaseFirestore) {
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

                // 將預設照片跟暱稱上傳至 firebase
                updateProfileFirst(nickName)

                // 將用戶資訊以 firestore 的儲存格式存入
                val userInfo = hashMapOf(
                    "Nickname" to nickName
                )

                // 將用戶資訊創建在 「userinfo」的 collection 下
                // 並以註冊時產生的 uid 當 key
                db.collection("userinfo").document(uid!!)
                    .set(userInfo)
                    .addOnSuccessListener { Log.d(TAG, "DocumentSnapshot successfully written!") }
                    .addOnFailureListener { e -> Log.w(TAG, "Error writing document", e) }

            }
        }
    }

    private fun updateProfileFirst(nickName: String) {
        val headUri = Uri.parse("android.resource://${requireContext().packageName}/${R.drawable.picpersonal}")
        auth.currentUser?.let { user ->
            Log.d(TAG, "updateProfileFirst: nickname = $nickName")
            val profileUpdates = UserProfileChangeRequest.Builder()
                .setDisplayName(nickName)
                .setPhotoUri(headUri)
                .build()
            user.updateProfile(profileUpdates)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        userViewModel.getFireUserInfo()
                        updateUI(nickName)
                        Log.d(TAG, "User first profile updated.")
                    }
                }
        }
    }

    fun updateUI(nickName: String) {
        val parentActivity = requireActivity() as MainActivity
         //跳轉回 Home
        parentActivity.binding.tvHomeLoginNickname.text = nickName
//        parentActivity.supportFragmentManager.beginTransaction().run {
//            replace(R.id.main_container, parentActivity.mainFragments[1])
//
//            commit()
//        }
        parentActivity.binding.bottomBar.selectTabAt(0, true)

        // 清除輸入框
        binding.edSignName.text.clear()
        binding.edSignPwd.text.clear()
        binding.edSignUserid.text.clear()
        binding.edSignPwdAgain.text.clear()
    }
}