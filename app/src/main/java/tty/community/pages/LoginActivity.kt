package tty.community.pages

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_login.*
import org.json.JSONObject
import tty.community.R
import tty.community.data.MainDBHelper
import tty.community.model.Login
import tty.community.model.Shortcut
import tty.community.network.AsyncTaskUtil
import tty.community.values.Values

class LoginActivity : AppCompatActivity() {

    private val map = HashMap<String, String>()
    private var loginType = "nickname"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        login_btn.setOnClickListener {
            val account = login_account.text.trim().toString()
            val password = login_password.text.trim().toString()
            if (account.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "账号/密码不能为空", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            map["login_type"] = loginType
            map[loginType] = account
            map["password"] = password
            map["platform"] = "mobile"
            login()
        }

        login_change_method.setOnClickListener {
            if(loginType == "nickname"){
                loginType = "id"
                login_method.text = "账    号"
            } else {
                loginType = "nickname"
                login_method.text = "用 户 名"
            }
        }

        login_register_btn.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

    }


    private fun login() {
        AsyncTaskUtil.AsyncNetUtils.post("${Values.api["user"]}/login", map, object : AsyncTaskUtil.AsyncNetUtils.Callback{
            override fun onResponse(response: String) {
                Log.d(MainDBHelper.TAG, response)
                val result  = JSONObject(response)
                val msg = result.optString("msg", "unknown error")
                when(val shortcut = Shortcut.phrase(result.optString("shortcut", "UNKNOWN"))) {
                    Shortcut.OK -> {
                        val data = result.getJSONObject("data")
                        val id = data.optString("id")
                        val nickname = data.optString("nickname")
                        val email = data.optString("email")
                        val token = data.optString("token")
                        val values = Login(id, nickname, token, email).getValues()
                        if(values != null) {
                            MainDBHelper(this@LoginActivity).login(this@LoginActivity, values)
                            Toast.makeText(this@LoginActivity, msg, Toast.LENGTH_SHORT).show()
                            finish()
                        } else {
                            Toast.makeText(this@LoginActivity, "error code: IR, invalid response", Toast.LENGTH_SHORT).show()
                        }
                    }

                    else -> {
                        Toast.makeText(this@LoginActivity, "error code: ${shortcut.name}, $msg", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }
}