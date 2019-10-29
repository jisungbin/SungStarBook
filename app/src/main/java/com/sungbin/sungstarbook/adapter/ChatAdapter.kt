@file:Suppress("NAME_SHADOWING")

package com.sungbin.sungstarbook.adapter

import android.annotation.SuppressLint
import  android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.AsyncTask
import android.os.Build
import android.os.Environment
import android.util.Log
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.sungbin.sungstarbook.dto.ChattingItem
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.RelativeLayout
import androidx.annotation.NonNull
import androidx.core.app.ActivityOptionsCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.facebook.FacebookSdk
import com.google.firebase.storage.FirebaseStorage
import com.shashank.sony.fancytoastlib.FancyToast
import com.sungbin.sungstarbook.R
import com.sungbin.sungstarbook.utils.Utils
import com.sungbin.sungstarbook.view.activity.ImageViewerActivity
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL


class ChatAdapter(private val list: ArrayList<ChattingItem>?, private val act:Activity) :
    RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

    inner class ChatViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var layout_L: RelativeLayout = view.findViewById(R.id.leftLayout)
        var layout_R: RelativeLayout = view.findViewById(R.id.rightLayout)
        var profilePic_L: ImageView = view.findViewById(R.id.profile_image_L)
        var profilePic_R: ImageView = view.findViewById(R.id.profile_image)
        var name_L: TextView = view.findViewById(R.id.name_L)
        var name_R: TextView = view.findViewById(R.id.name)
        var time_L: TextView = view.findViewById(R.id.time_L)
        var time_R: TextView = view.findViewById(R.id.time)
        var msg_L: TextView = view.findViewById(R.id.msg_L)
        var msg_R: TextView = view.findViewById(R.id.msg)
        var content_image_L: ImageView = view.findViewById(R.id.content_image_L)
        var content_image_R: ImageView = view.findViewById(R.id.content_image)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ChatViewHolder {
        val view = LayoutInflater.from(viewGroup.context).inflate(R.layout.chat_content_view, viewGroup, false)
        return ChatViewHolder(view)
    }

    @SuppressLint("DefaultLocale")
    override fun onBindViewHolder(@NonNull viewholder: ChatViewHolder, position: Int) {
        var name = list!![position].name
        val time = list[position].time
        val type = list[position].type
        val msg = list[position].msg
        val uid = list[position].uid
        val contentUid = list[position].contentUri
        val profilePicUri = list[position].profilePicUri

        val myUid = Utils.readData(act, "uid", "null")!!
        val myName = Utils.readData(act, "myName", "null")!!

        val options = RequestOptions()
            .skipMemoryCache(true)
            .format(DecodeFormat.PREFER_ARGB_8888)
            .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
            .placeholder(R.drawable.loading_image)

        if(uid == myUid) name = myName

        if (myName == name) { //오른쪽
            viewholder.msg_R.text = msg
            viewholder.time_R.text = time
            viewholder.name_R.text = name
            viewholder.msg_R.setOnClickListener {
                Utils.toast(act, "메세지가 복사되었습니다.",
                    FancyToast.LENGTH_SHORT, FancyToast.SUCCESS)
                Utils.copy(act, msg!!, false)
            }

            val imageFile = File(Environment.getExternalStorageDirectory().absolutePath +
                    "/SungStarBook/Image/${myUid}.png")
            if(imageFile.exists())
                Glide.with(act).load(imageFile)
                    .apply(options).into(viewholder.profilePic_R)
            else {
                ImageDownloadTask().execute(myUid, profilePicUri)
                Glide.with(act).load(profilePicUri)
                    .apply(options).into(viewholder.profilePic_R)
            }

            if(type == "image"){
                viewholder.msg_R.text = time
                viewholder.time_R.visibility = View.GONE

                val imageType = msg!!.substring(msg.lastIndexOf(".") + 1, msg.length).toLowerCase()
                val isGif = imageType.contains("gif")
                val storage = FirebaseStorage.getInstance()
                val storageRef = storage.reference
                    .child("Chatting_Image/$uid/$contentUid")
                storageRef.downloadUrl.addOnSuccessListener {
                    val imageFile = File(Environment.getExternalStorageDirectory().absolutePath +
                            "/SungStarBook/Image/${contentUid}.gif")
                    if(imageFile.exists()) {
                        Log.d("SS", "SS")
                        if(isGif) Glide.with(act).asGif().load(imageFile).apply(options)
                            .into(viewholder.content_image_R)
                        else Glide.with(act).load(imageFile).apply(options)
                            .into(viewholder.content_image_R)
                    }
                    else {
                        ImageDownloadTask().execute(contentUid, it.toString())
                        if(isGif) Glide.with(act).asGif().load(it).apply(options)
                            .into(viewholder.content_image_R)
                        else Glide.with(act).load(it).apply(options)
                            .into(viewholder.content_image_R)
                    }
                    viewholder.content_image_R.visibility = View.VISIBLE
                }
                storageRef.downloadUrl.addOnFailureListener {
                    Glide.with(act).load("https://cdn.pixabay.com/photo/2017/03/09/12/31/error-2129569_960_720.jpg")
                        .apply(options)
                        .into(viewholder.content_image_R)
                    viewholder.content_image_R.visibility = View.VISIBLE
                }

                if(!isGif) {
                    viewholder.content_image_R.setOnClickListener {
                        Utils.toast(act, msg, FancyToast.LENGTH_SHORT, FancyToast.INFO)
                        val intent = Intent(act, ImageViewerActivity::class.java)
                            .putExtra("image", contentUid).putExtra("tag", "content_image_R")
                            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                            act,
                            viewholder.content_image_R,
                            "content_image_R"
                        )
                        if (Build.VERSION.SDK_INT >= 21) {
                            act.startActivity(intent, options.toBundle())
                        } else act.startActivity(intent)
                    }
                }
            }
            viewholder.profilePic_R.setOnClickListener {
                val intent = Intent(act, ImageViewerActivity::class.java)
                    .putExtra("image", myUid).putExtra("tag", "profilePic_R")
                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                    act,
                    viewholder.profilePic_R,
                    "profilePic_R"
                )
                if (Build.VERSION.SDK_INT >= 21) {
                    act.startActivity(intent, options.toBundle())
                } else act.startActivity(intent)
            }
        } else { //왼쪽
            viewholder.layout_R.visibility = View.GONE
            viewholder.layout_L.visibility = View.VISIBLE

            viewholder.msg_L.text = msg
            viewholder.time_L.text = time
            viewholder.name_L.text = name
            viewholder.msg_L.setOnClickListener {
                Utils.toast(act, "메세지가 복사되었습니다.",
                    FancyToast.LENGTH_SHORT, FancyToast.SUCCESS)
                Utils.copy(act, msg!!, false)
            }

            val imageFile = File(Environment.getExternalStorageDirectory().absolutePath +
                    "/SungStarBook/Image/${myUid}.png")
            if(imageFile.exists())
                Glide.with(act).load(imageFile).apply(options)
                    .into(viewholder.profilePic_L)
            else {
                ImageDownloadTask().execute(myUid, profilePicUri)
                Glide.with(act).load(profilePicUri).apply(options)
                    .into(viewholder.profilePic_L)
            }

            if(type == "image"){
                viewholder.msg_L.text = time
                viewholder.time_L.visibility = View.GONE

                val imageType = msg!!.substring(msg.lastIndexOf(".") + 1, msg.length).toLowerCase()
                val isGif = imageType.contains("gif")
                val storage = FirebaseStorage.getInstance()
                val storageRef = storage.reference
                    .child("Chatting_Image/$uid/$contentUid")
                storageRef.downloadUrl.addOnSuccessListener {
                    val imageFile = File(Environment.getExternalStorageDirectory().absolutePath +
                            "/SungStarBook/Image/${contentUid}.gif")
                    if(imageFile.exists()) {
                        if(isGif) Glide.with(act).asGif().load(imageFile).apply(options)
                            .into(viewholder.content_image_L)
                        else Glide.with(act).load(imageFile).apply(options)
                            .into(viewholder.content_image_L)
                    }
                    else {
                        ImageDownloadTask().execute(contentUid, it.toString())
                        if(isGif) Glide.with(act).asGif().load(it)
                            .apply(options).into(viewholder.content_image_L)
                        else Glide.with(act).load(it)
                            .apply(options).into(viewholder.content_image_L)
                    }
                    viewholder.content_image_L.visibility = View.VISIBLE
                }
                storageRef.downloadUrl.addOnFailureListener {
                    Glide.with(act).load("https://cdn.pixabay.com/photo/2017/03/09/12/31/error-2129569_960_720.jpg")
                        .apply(options)
                        .into(viewholder.content_image_L)
                    viewholder.content_image_L.visibility = View.VISIBLE
                }

                if(!isGif) {
                    viewholder.content_image_L.setOnClickListener {
                        Utils.toast(act, msg, FancyToast.LENGTH_SHORT, FancyToast.INFO)
                        val intent = Intent(act, ImageViewerActivity::class.java)
                            .putExtra("image", contentUid).putExtra("tag", "content_image_L")
                            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                            act,
                            viewholder.content_image_L,
                            "content_image_L"
                        )
                        if (Build.VERSION.SDK_INT >= 21) {
                            act.startActivity(intent, options.toBundle())
                        } else act.startActivity(intent)
                    }
                }
            }
            viewholder.profilePic_L.setOnClickListener {
                val intent = Intent(act, ImageViewerActivity::class.java)
                    .putExtra("image", myUid).putExtra("tag", "profilePic_L")
                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                    act,
                    viewholder.profilePic_L,
                    "profilePic_L"
                )
                if (Build.VERSION.SDK_INT >= 21) {
                    act.startActivity(intent, options.toBundle())
                } else act.startActivity(intent)
            }
        }

    }

    override fun getItemCount(): Int {
        return list?.size ?: 0
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    fun getItem(position: Int): ChattingItem {
        return list!![position]
    }

    private class ImageDownloadTask : AsyncTask<String?, Void?, Void?>() {
        override fun doInBackground(vararg params: String?): Void? {
            val savePath = Environment.getExternalStorageDirectory().absolutePath +
                    "/SungStarBook/Image/"
            if(!File(savePath).exists()) File(savePath).mkdirs()
            val localPath = "$savePath/${params[0]}.gif"

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
