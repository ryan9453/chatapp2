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
import com.ryan.chat.databinding.FragmentLoginBinding
import com.ryan.chat.databinding.FragmentPersonBinding

class LoginFragment : Fragment() {
    companion object {
        val TAG = LoginFragment::class.java.simpleName
        val instance : LoginFragment by lazy {
            LoginFragment()
        }
    }
    lateinit var binding: FragmentLoginBinding
    // 將打勾情形使用 remember紀錄起來，預設 false
    var remember = false
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentLoginBinding.inflate(inflater)
//        return super.onCreateView(inflater, container, savedInstanceState)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 呼叫 getSharedPreferences()方法，產生一個檔名為 chat.xml的設定儲存檔，並只供本專案 (app)可讀取。

        val pref = requireContext().getSharedPreferences("chat", AppCompatActivity.MODE_PRIVATE)

        // 呼叫"rem_username"的 Boolean，true代表上次打勾
        // 第一次登入 "rem_username"不會存在，預設給 false
        val checked = pref.getBoolean("rem_username", false)

        // 複製上次的打勾情形
        binding.cbRemember.isChecked = checked
        Log.d(TAG, "onCreate: checked=${checked}")

        // 開啟畫面後將 chat資料夾裡的 USER取出來作為記住帳號
        // 並將記住帳號顯示在 userid輸入框裡
        // 由前面控制 chat資料夾裡的 USER鍵值對
        // 若上次按記住我，其值會是上次登入時的帳號，反之為空字串
        val rem_user = pref.getString("USER", "")
        binding.edLoginUserid.setText(rem_user)

        // 將上次打勾情形存入 remember
        remember = checked

        // 如果有改變打勾情形會進入此情況
        binding.cbRemember.setOnCheckedChangeListener { compoundButton, check ->

            // 將現在打勾情形存入 remember
            remember = check
            Log.d(TAG, "onCreate: remember=${remember}")


            // 也把現在打勾情形存起來以供下次判斷：是否要顯示帳號
            pref.edit().putBoolean("rem_username", remember).apply()

            // 如果 把打勾按掉，就把記錄的 userid改成空字串
            if (!checked) {
                pref.edit().putString("USER", "").apply()
            }
        }

        // 登入按鈕，登入成功跳轉回 MainA
        // 登入失敗，提示錯誤訊息
        binding.btLogin.setOnClickListener {

            // 存取帳密用，從 "userinfo"資料夾檢查
            val prefUser = requireContext().getSharedPreferences("userinfo", AppCompatActivity.MODE_PRIVATE)

            // 檢查紀錄帳號用，從 "chat"資料夾做檢查
            val pref = requireContext().getSharedPreferences("chat", AppCompatActivity.MODE_PRIVATE)

            // 存取登入狀態用
            val prefLogin = requireContext().getSharedPreferences("login", AppCompatActivity.MODE_PRIVATE)

            // 取目前輸入的帳號密碼準備做檢查
            var ed_user = binding.edLoginUserid.text.toString()
            var ed_pwd = binding.edLoginPwd.text.toString()
            var check_user = prefUser.getString("${ed_user}","")
            var check_pwd = prefUser.getString("${ed_user}pwd","")
            var error_text = ""

            error_text =
                when {
                    check_user == "" -> getString(R.string.the_userid_is_not_exist)
                    ed_pwd != check_pwd -> getString(R.string.wrong_password)
                    else -> ""
                }

            if (error_text == "") {
                val parentActivity =  requireActivity() as MainActivity
                prefLogin.edit()
                    .putBoolean("login_state", true)
                    .putString("login_userid", ed_user)
                    .apply()
                // 根據前面的 remember變化決定是否重新紀錄帳號
                // 若沒改變 remember = checked，若有改變 remember = check
                if (remember) {
                    // 把帳號存在 本地的 chat資料夾以供 記住帳號的邏輯使用
                    pref.edit().putString("USER", ed_user).apply()
                    Log.d(TAG, "btLogin: 有重新記住帳號")
                }
                Log.d(TAG, "帳號密碼正確 並印出remember=${remember}")
                parentActivity.binding.tvHomeLoginUserid.setText(ed_user)
                parentActivity.binding.imHead.visibility = View.VISIBLE
                parentActivity.binding.tvHomeLoginUserid.visibility = View.VISIBLE

                // 登入成功對話框，按OK後都會跳轉到 MainA
                // 登入成功後，會把登入狀態紀錄到本地資料夾
                AlertDialog.Builder(requireContext())
                    .setTitle(getString(R.string.message))
                    .setMessage(getString(R.string.log_in_successfully))
                    .setPositiveButton(getString(R.string.ok), null)
                    .show()
                val login = prefLogin.getBoolean("login_state", false)
                Log.d(TAG, "login_state = ${login}")
                parentActivity.supportFragmentManager.beginTransaction().run {
                    replace(R.id.main_container, parentActivity.mainFragments[1])
                    commit()
                }

                // 錯誤訊息對話框
            } else {
                AlertDialog.Builder(requireContext())
                    .setTitle(getString(R.string.wrong_message))
                    .setMessage(error_text)
                    .setPositiveButton(getString(R.string.ok),null)
                    .show()
            }

        }

        binding.btSignUp.setOnClickListener {
            val parentActivity =  requireActivity() as MainActivity
            parentActivity.supportFragmentManager.beginTransaction().run {
                replace(R.id.main_container, parentActivity.mainFragments[4])
                commit()
            }
        }
    }
}