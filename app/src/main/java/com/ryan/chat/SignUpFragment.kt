package com.ryan.chat

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.ryan.chat.databinding.FragmentSignUpBinding

class SignUpFragment : Fragment() {
    companion object {
        val TAG = SignUpFragment::class.java.simpleName
        val instance : SignUpFragment by lazy {
            SignUpFragment()
        }
    }
    lateinit var binding: FragmentSignUpBinding
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


        // 註冊完成的送出按鈕
        binding.btSignSend.setOnClickListener {
            // 取輸入方塊的值
            // 建立登入狀態變數，初始值為 false
            var name = binding.edSignName.text.toString()
            var user = binding.edSignUserid.text.toString()
            var pwd = binding.edSignPwd.text.toString()
            var pwdg = binding.edSignPwdAgain.text.toString()
            var login_state: Boolean = false
            val prefUser = requireContext().getSharedPreferences("userinfo", AppCompatActivity.MODE_PRIVATE)
            val prefLogin = requireContext().getSharedPreferences("login", AppCompatActivity.MODE_PRIVATE)
            /*
                帳密規則驗證
                正確:
                    將「名稱」「帳號」「密碼」「登入狀態」存在 shared_prefs資料夾
                    彈出「註冊成功對話框」並詢問是否記住登入狀態爾後跳轉至 MainA
                錯誤:
                    彈出「錯誤訊息對話框」
            */
            var error_text = ""
            error_text =
                when {
                    CheckNumber(user).userId() == CheckNumber.NumberState.TOOSHORT -> getString(R.string.userid_too_short)
                    CheckNumber(user).userId() == CheckNumber.NumberState.TOOLONG -> getString(R.string.userid_too_long)
                    CheckNumber(user).userId() == CheckNumber.NumberState.WRONG -> getString(R.string.userid_is_wrong)
                    CheckNumber(pwd).passWord() == CheckNumber.NumberState.TOOSHORT -> getString(R.string.pwd_too_short)
                    CheckNumber(pwd).passWord() == CheckNumber.NumberState.TOOLONG -> getString(R.string.pwd_too_long)
                    CheckNumber(pwd).passWord() == CheckNumber.NumberState.WRONG -> getString(R.string.pwd_is_wrong)
                    pwd != pwdg -> getString(R.string.pwd_is_not_same)
                    else -> ""
                }

            // 將暱稱帳號密碼存本地 √
            // 彈出對話框，內容為「註冊成功」並詢問是否要記住登入狀態
            if (error_text == "") {
                val parentActivity =  requireActivity() as MainActivity

                // 將帳密和登入狀態一起存入本地
                prefUser.edit()
                    .putString("${user}name", name)
                    .putString("$user", user)
                    .putString("${user}pwd", pwd)
                    .apply()
                prefLogin.edit()
                    .putBoolean("login_state", true)
                    .putString("login_userid", user)
                    .apply()

                Log.d(TAG, "帳密輸入沒問題")

                AlertDialog.Builder(requireContext())
                    .setTitle(getString(R.string.message))
                    .setMessage(getString(R.string.sign_up_successfully))

                    // 若按 OK 登入狀態改成 true並將此次帳號存入資料夾
                    .setPositiveButton(getString(R.string.ok), null)
                    .show()

                // 跳轉回 Home
                parentActivity.supportFragmentManager.beginTransaction().run {
                    replace(R.id.main_container, parentActivity.mainFragments[1])
                    parentActivity.binding.tvHomeLoginUserid.setText(user)
                    commit()
                }
                binding.edSignName.setText("")
                binding.edSignPwd.setText("")
                binding.edSignUserid.setText("")
                binding.edSignPwdAgain.setText("")

                // 錯誤訊息對話框
            } else {
                AlertDialog.Builder(requireContext())
                    .setTitle(getString(R.string.wrong_message))
                    .setMessage(error_text)
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
}