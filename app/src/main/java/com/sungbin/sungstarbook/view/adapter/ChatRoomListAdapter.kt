package com.sungbin.sungstarbook.view.adapter

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Build
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
import com.sungbin.sungstarbook.R
import com.sungbin.sungstarbook.dto.ChatRoomListItem
import com.sungbin.sungstarbook.view.activity.ImageViewerActivity
import com.sungbin.sungstarbook.view.activity.ChattingActivity
import de.hdodenhof.circleimageview.CircleImageView
import java.io.ByteArrayOutputStream


class ChatRoomListAdapter(private val list: ArrayList<ChatRoomListItem>?,
                          private val act: Activity) :
    RecyclerView.Adapter<ChatRoomListAdapter.RoomViewHolder>() {

    inner class RoomViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var roomImage: CircleImageView = view.findViewById(R.id.room_image)
        var roomTitle: TextView = view.findViewById(R.id.room_title)
        var roomMsg: TextView = view.findViewById(R.id.room_msg)
        var roomTime: TextView = view.findViewById(R.id.room_time)
        var view: CardView = view.findViewById(R.id.my_profile_card_view)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): RoomViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.chat_room_list_view, viewGroup, false)
        return RoomViewHolder(view)
    }

    override fun onBindViewHolder(@NonNull viewholder: RoomViewHolder, position: Int) {
        val name = list!![position].name
        val time = list[position].time
        val msg = list[position].msg
        val roomPicUri = list[position].roomPicUri
        val roomUid = list[position].roomUid

        viewholder.view.setOnClickListener {
            act.startActivity(Intent(act, ChattingActivity::class.java)
                .putExtra("name", name)
                .putExtra("roomUid", roomUid)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        }
        viewholder.roomMsg.text = msg
        viewholder.roomTime.text = time
        viewholder.roomTitle.text = name
        Glide.with(act).load(roomPicUri)
            .format(DecodeFormat.PREFER_ARGB_8888)
            .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
            .into(viewholder.roomImage)

        viewholder.roomImage.setOnClickListener {
            val image = viewholder.roomImage.drawable
            val sendBitmap = (image as BitmapDrawable).bitmap
            val stream = ByteArrayOutputStream()
            sendBitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
            val byteArray = stream.toByteArray()
            val intent = Intent(act, ImageViewerActivity::class.java)
                .putExtra("image", byteArray).putExtra("tag", "room_image")
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                act,
                viewholder.roomImage,
                "room_image")
            if (Build.VERSION.SDK_INT >= 21) {
                act.startActivity(intent, options.toBundle())
            } else act.startActivity(intent)

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

    fun getItem(position: Int): ChatRoomListItem {
        return list!![position]
    }

}
