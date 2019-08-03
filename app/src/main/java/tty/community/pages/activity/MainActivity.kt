package tty.community.pages.activity

import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONObject
import tty.community.R
import tty.community.adapter.MainFragmentAdapter
import tty.community.data.MainDBHelper
import tty.community.model.Shortcut
import tty.community.network.AsyncTaskUtil
import tty.community.values.Values

class MainActivity : AppCompatActivity(), ViewPager.OnPageChangeListener,
    BottomNavigationView.OnNavigationItemSelectedListener {
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        return when(item.itemId){
            R.id.nav_home -> {
                main_viewPager.currentItem = 0
                true
            }
            R.id.nav_square -> {
                main_viewPager.currentItem = 1
                true
            }
            R.id.nav_me -> {
                main_viewPager.currentItem = 2
                true
            }
            else -> false
        }
    }

    override fun onPageScrollStateChanged(state: Int) { }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) { }

    override fun onPageSelected(position: Int) {
        main_nav.selectedItemId = when (position) {
            0 -> R.id.nav_home
            1 -> R.id.nav_square
            2 -> R.id.nav_me
            else -> return
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

    }

    override fun onResume() {
        super.onResume()
        autoLogin()
        setAdapter()
    }

    private fun autoLogin() {
        val user = MainDBHelper(this).findUser()
        if (user == null) {
            Toast.makeText(this, "您还未登录账号，请先登录", Toast.LENGTH_SHORT).show()
//            startActivity(Intent(this, LoginActivity::class.java))
        } else {
            val map = HashMap<String, String>()
            map["id"] = user.id!!
            map["token"] = user.token!!
            map["platform"] = "mobile"
            AsyncTaskUtil.AsyncNetUtils.post("${Values.api["user"]}/auto_login", map, object : AsyncTaskUtil.AsyncNetUtils.Callback {
                override fun onResponse(response: String) {
                    Log.d(TAG, response)
                    val result = JSONObject(response)
                    val msg = result.optString("msg", "unknown error")
                    when(val shortcut = Shortcut.phrase(result.optString("shortcut", "UNKNOWN"))) {
                        Shortcut.OK -> {
                            val data = result.getJSONObject("data")
//                            Toast.makeText(this@MainActivity, msg, Toast.LENGTH_SHORT).show()
                            val values = ContentValues()
                            values.put("email", data.getString("email"))
                            MainDBHelper(this@MainActivity).updateUser(user.id!!, values)
                        }

                        else -> {
                            Toast.makeText(this@MainActivity, "error: ${shortcut.name}, $msg", Toast.LENGTH_SHORT).show()
//                            startActivity(Intent(this@MainActivity, LoginActivity::class.java))
                        }
                    }
                }
            })
        }
    }

    private fun setAdapter() {
        if (adapter == null) {
            adapter = MainFragmentAdapter(supportFragmentManager)
        }
        main_viewPager.adapter = adapter
        main_viewPager.addOnPageChangeListener(this)
        main_nav.setOnNavigationItemSelectedListener(this)
        main_viewPager.currentItem = when (main_nav.selectedItemId) {
            R.id.nav_home -> 0
            R.id.nav_square -> 1
            R.id.nav_me -> 2
            else -> return
        }
    }

    companion object {
        const val TAG = "MainActivity"
        private var adapter: MainFragmentAdapter? = null
    }


}
