package com.sungbin.sungstarbook.view.adapter

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.AsyncTask
import android.os.Build
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityOptionsCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.facebook.FacebookSdk
import com.google.firebase.storage.FirebaseStorage
import com.shashank.sony.fancytoastlib.FancyToast
import com.sungbin.sungstarbook.R
import com.sungbin.sungstarbook.dto.ChatRoomListItem
import com.sungbin.sungstarbook.dto.FriendsListItem
import com.sungbin.sungstarbook.utils.Utils
import com.sungbin.sungstarbook.view.activity.ImageViewerActivity
import com.sungbin.sungstarbook.view.activity.ChattingActivity
import de.hdodenhof.circleimageview.CircleImageView
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL


class FriendsListAdapter(private val list: ArrayList<FriendsListItem>?,
                          private val act: Activity) :
    RecyclerView.Adapter<FriendsListAdapter.FriendViewHolder>() {

    inner class FriendViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var profileImage: CircleImageView = view.findViewById(R.id.profile_image)
        var profileName: TextView = view.findViewById(R.id.profile_name)
        var profileMsg: TextView = view.findViewById(R.id.profile_msg)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): FriendViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.friends_list_view, viewGroup, false)
        return FriendViewHolder(view)
    }

    override fun onBindViewHolder(@NonNull viewholder: FriendViewHolder, position: Int) {
        val name = list!![position].name
        val uid = list[position].uid
        val msg = list[position].msg

        setImageDrawable(viewholder, uid!!)
        viewholder.profileName.text = name
        viewholder.profileMsg.text = msg

        viewholder.profileImage.setOnClickListener {
            val intent = Intent(act, ImageViewerActivity::class.java)
                .putExtra("image", uid).putExtra("tag", "content_image")
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                act,
                viewholder.profileImage,
                "content_image"
            )
            if (Build.VERSION.SDK_INT >= 21) {
                act.startActivity(intent, options.toBundle())
            } else act.startActivity(intent)
        }
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun getItemCount(): Int {
        return list?.size ?: 0
    }

    fun getItem(position: Int): FriendsListItem {
        return list!![position]
    }

    private fun setImageDrawable(viewholder: FriendViewHolder, uid: String){
        val options = RequestOptions()
            .skipMemoryCache(true)
            .format(DecodeFormat.PREFER_ARGB_8888)
            .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
            .placeholder(R.drawable.loading_image)

        val imageFile = File(
            Environment.getExternalStorageDirectory().absolutePath +
                    "/SungStarBook/Profile Image/$uid.png"
        )
        if(imageFile.exists()) {
            Glide.with(act).load(imageFile).apply(options)
                .into(viewholder.profileImage)
        }
        else {
            val storage = FirebaseStorage.getInstance()
            val storageRef = storage.reference
                .child("Profile_Image/$uid/Profile.png")
            storageRef.downloadUrl.addOnSuccessListener {
                ImageDownloadTask().execute(uid, it.toString())
                Glide.with(act).load(it).apply(options)
                    .into(viewholder.profileImage)
            }
            storageRef.downloadUrl.addOnFailureListener {
                Glide.with(act)
                    .load("https://cdn.pixabay.com/photo/2017/03/09/12/31/error-2129569_960_720.jpg")
                    .apply(options)
                    .into(viewholder.profileImage)
            }
        }
    }

    private class ImageDownloadTask : AsyncTask<String?, Void?, Void?>() {
        override fun doInBackground(vararg params: String?): Void? {
            val savePath = Environment.getExternalStorageDirectory().absolutePath +
                    "/SungStarBook/Profile Image/"
            if(!File(savePath).exists()) File(savePath).mkdirs()
            val localPath = "$savePath/${params[0]}.png"

            try {
                val imgUrl = URL(params[1])
                val conn = imgUrl.openConnection() as HttpURLConnection
                val len = conn.contentLength
                val tmpByte = ByteArray(len)
                val `is` = conn.inputStream
                val file = File(localPath)
                val fos = FileOutputStream(file)
                var read: Int

                while (true) {
                    read = `is`.read(tmpByte)
                    if (read <= 0) {
                        break
                    }
                    fos.write(tmpByte, 0, read)
                }

                `is`.close()
                fos.close()
                conn.disconnect()
            } catch (e: Exception) {
                Utils.error(FacebookSdk.getApplicationContext(), e.toString())
            }

            return null
        }

        override fun onPostExecute(result: Void?) {
            return
        }

    }

}
