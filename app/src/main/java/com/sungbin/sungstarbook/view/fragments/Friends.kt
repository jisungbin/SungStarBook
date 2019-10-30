package com.sungbin.sungstarbook.view.fragments

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.app.ActivityOptionsCompat
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.facebook.FacebookSdk
import com.facebook.FacebookSdk.getApplicationContext
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.karlgao.materialroundbutton.MaterialButton
import com.makeramen.roundedimageview.RoundedImageView
import com.shashank.sony.fancytoastlib.FancyToast
import com.sungbin.sungstarbook.R
import com.sungbin.sungstarbook.dto.MyInformationItem
import com.sungbin.sungstarbook.utils.FirebaseUtils
import com.sungbin.sungstarbook.utils.Utils
import com.sungbin.sungstarbook.view.activity.ImageViewerActivity
import com.sungbin.sungstarbook.view.activity.ProfileViewActivity
import com.sungbin.sungstarbook.view.adapter.FriendsListAdapter
import de.hdodenhof.circleimageview.CircleImageView
import org.w3c.dom.Text
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

class Friends : Fragment() {
    @SuppressLint("InflateParams")
    private lateinit var uid: String

    @SuppressLint("InflateParams")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)

        uid = Utils.readData(context!!, "uid", "null")!!

        val view = inflater.inflate(R.layout.fragment_friends, null)

        val reference = FirebaseDatabase.getInstance().reference
            .child("UserDB").child(uid)
        reference.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {
                val data: MyInformationItem = dataSnapshot.getValue(MyInformationItem::class.java)!!
                view.findViewById<TextView>(R.id.my_profile_name).text = data.name
                view.findViewById<TextView>(R.id.my_profile_msg).text = data.msg
            }

            override fun onChildChanged(dataSnapshot: DataSnapshot, s: String?) {

            }

            override fun onChildRemoved(dataSnapshot: DataSnapshot) {

            }

            override fun onChildMoved(dataSnapshot: DataSnapshot, s: String?) {

            }

            override fun onCancelled(databaseError: DatabaseError) {

            }
        })

        view.findViewById<MaterialButton>(R.id.see_my_profile).setOnClickListener {
            startActivity(
                Intent(context, ProfileViewActivity::class.java)
                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        }

        val options = RequestOptions()
            .skipMemoryCache(true)
            .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
            .format(DecodeFormat.PREFER_ARGB_8888)
            .placeholder(R.drawable.profile_image_preview)

        val imageFile = File(
            Environment.getExternalStorageDirectory().absolutePath +
                    "/SungStarBook/Profile Image/$uid.png"
        )
        if(imageFile.exists()) {
            Glide.with(activity!!).load(imageFile).apply(options)
                .into(view.findViewById<CircleImageView>(R.id.my_profile_image))
        }
        else {
            val storage = FirebaseStorage.getInstance()
            val storageRef = storage.reference
                .child("Profile_Image/$uid/Profile.png")
            storageRef.downloadUrl.addOnSuccessListener {
                ImageDownloadTask().execute(uid, it.toString())
                Glide.with(activity!!).load(it).apply(options)
                    .into(view.findViewById<CircleImageView>(R.id.my_profile_image))
            }
            storageRef.downloadUrl.addOnFailureListener {
                Glide.with(activity!!)
                    .load("https://cdn.pixabay.com/photo/2017/03/09/12/31/error-2129569_960_720.jpg")
                    .apply(options)
                    .into(view.findViewById<CircleImageView>(R.id.my_profile_image))
            }
        }

        return view
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
                Utils.error(getApplicationContext(), e.toString())
            }

            return null
        }

        override fun onPostExecute(result: Void?) {
            return
        }

    }

}