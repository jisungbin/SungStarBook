package com.sungbin.sungstarbook.view.main_fragment

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.app.ActivityOptionsCompat
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.facebook.FacebookSdk.getApplicationContext
import com.google.firebase.storage.FirebaseStorage
import com.karlgao.materialroundbutton.MaterialButton
import com.makeramen.roundedimageview.RoundedImageView
import com.shashank.sony.fancytoastlib.FancyToast
import com.sungbin.sungstarbook.R
import com.sungbin.sungstarbook.utils.Utils
import com.sungbin.sungstarbook.view.activity.ImageViewerActivity
import com.sungbin.sungstarbook.view.activity.ProfileViewActivity
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

        view.findViewById<MaterialButton>(R.id.see_my_profile).setOnClickListener {
            startActivity(
                Intent(context, ProfileViewActivity::class.java)
                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        }

        view.findViewById<ImageView>(R.id.my_profile_image).setOnClickListener {
            val image = view.findViewById<ImageView>(R.id.my_profile_image).drawable
            val sendBitmap = (image as BitmapDrawable).bitmap
            val stream = ByteArrayOutputStream()
            sendBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            val byteArray = stream.toByteArray()
            val intent = Intent(context, ImageViewerActivity::class.java)
                .putExtra("image", byteArray).putExtra("tag", "profile_image")
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                activity!!,
                view.findViewById(R.id.my_profile_image),
                "profile_image"
            )
            if (Build.VERSION.SDK_INT >= 21) {
                startActivity(intent, options.toBundle())
            } else startActivity(intent)
        }

        val options = RequestOptions()
            .skipMemoryCache(true)
            .centerInside()
            .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
            .format(DecodeFormat.PREFER_ARGB_8888)
            .placeholder(R.drawable.profile_image_preview)
            .transform(CircleCrop())

        val storage = FirebaseStorage.getInstance()
        val storageRef = storage.reference
            .child("Profile_Image/$uid/Profile.png")
        storageRef.downloadUrl.addOnSuccessListener { uri ->
            try {
                val imageFile = File(Environment.getExternalStorageDirectory().absolutePath +
                        "/SungStarBook/Image/${uid}.png")
                 if(imageFile.exists()) Glide.with(context!!).load(imageFile).apply(options)
                     .into(view.findViewById<RoundedImageView>(R.id.my_profile_image))
                 else {
                     Glide.with(context!!).load(uri).apply(options)
                         .into(view.findViewById<RoundedImageView>(R.id.my_profile_image))
                     ImageDownloadTask().execute(uid, uri.toString())

                 }
            }
            catch (e: Exception){
            }
        }
        storageRef.downloadUrl.addOnFailureListener { e ->
            Utils.toast(context!!, "프로필 사진을 불러올 수 없습니다.\n\n$e", FancyToast.LENGTH_SHORT, FancyToast.WARNING)
        }

        return view
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
                Utils.error(getApplicationContext(), e.toString())
            }

            return null
        }

        override fun onPostExecute(result: Void?) {
            return
        }

    }
}