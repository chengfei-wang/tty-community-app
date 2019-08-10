package tty.community.pages.fragment


import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_create_blog_short.*
import tty.community.R
import tty.community.adapter.ImageListAdapter
import tty.community.data.MainDBHelper
import tty.community.network.AsyncTaskUtil
import tty.community.values.Values

class CreateBlogShortFragment : Fragment(), ImageListAdapter.OnItemClickListener {
    override fun onClick(p0: View?) {

    }

    override fun onItemClick(v: View?, position: Int) {
        Log.d(TAG, "pos: $position")
        if (position + 1 < imagesAdapter.itemCount) {
            imagesAdapter.delete(position)
        } else {
            val bitmap = Bitmap.createBitmap(128, 128, Bitmap.Config.ARGB_8888)
            bitmap.eraseColor(Color.GRAY)
            imagesAdapter.add(bitmap)
        }
    }


    private var id = ""
    private var token = ""
    private lateinit var imagesAdapter: ImageListAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_create_blog_short, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setAdapter()

    }

    private fun getToken() {
        val user = this.context?.let { MainDBHelper(it).findUser() }
        if (user != null) {
            id = user.id
            token = user.token
        } else {
            Toast.makeText(this.context, "登录已过期，请先登录", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setAdapter() {
        imagesAdapter = ImageListAdapter()
        val layoutManager= LinearLayoutManager(this.context)
        layoutManager.orientation= LinearLayoutManager.HORIZONTAL
        create_blog_short_images.adapter = imagesAdapter
        create_blog_short_images.layoutManager = layoutManager
        imagesAdapter.setOnItemClickListener(this)
    }

    fun submit() {
        getToken()

        val map = HashMap<String, String>()
        map["id"] = id
        map["token"] = token
        map["title"] = TITLE
        map["type"] = TYPE
        map["content"] = create_blog_short_content.text.toString()
        map["introduction"] = "${map["content"]?.substring(0, 15)}"
        map["tag"] = "#default#"
        map["file_count"] = "${imagesAdapter.itemCount - 1}"


        AsyncTaskUtil.AsyncNetUtils.postMultipleForm("${Values.api["blog"]}/create", mapOf(), arrayOf(), object : AsyncTaskUtil.AsyncNetUtils.Callback {
            override fun onResponse(response: String) {
                Log.d(TAG, response)
            }

        })
    }

    companion object {
        const val TAG = "CreateBlogShortFragment"
        const val TYPE = "SHORT"
        const val TITLE = "DEFAULT"
    }

}