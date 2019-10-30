package com.sungbin.sungstarbook.view.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sungbin.sungstarbook.R
import com.sungbin.sungstarbook.adapter.ChatRoomListAdapter
import com.sungbin.sungstarbook.dto.ChatRoomListItem
import com.sungbin.sungstarbook.utils.Utils
import org.apache.commons.lang3.StringUtils
import com.shashank.sony.fancytoastlib.FancyToast
import java.lang.Exception
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.firebase.database.*
import com.sungbin.sungstarbook.listener.SwipeController
import com.sungbin.sungstarbook.listener.SwipeControllerActions
import androidx.recyclerview.widget.ItemTouchHelper
import android.graphics.Canvas
import android.text.InputFilter
import android.view.Gravity
import android.widget.LinearLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.TedPermission
import de.hdodenhof.circleimageview.CircleImageView
import gun0912.tedbottompicker.TedBottomPicker


@SuppressLint("InflateParams")
class ChatRoom : Fragment() {

    private var uid:String? = null
    private var items:ArrayList<ChatRoomListItem>? = null
    @SuppressLint("StaticFieldLeak")
    private var adapter:ChatRoomListAdapter? = null
    private var reference:DatabaseReference? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)

        reference = FirebaseDatabase.getInstance().reference.child("RoomDB")
        uid = Utils.readData(context!!, "uid", "null")!!
        items = ArrayList()
        adapter = ChatRoomListAdapter(items, activity!!)

        var myRoom = Utils.readData(context!!, "myRoom", "null")
        var myRoomCount = StringUtils.countMatches(myRoom, "/")
        val view = inflater.inflate(R.layout.fragment_chatting, null)

        val chatRoomListView = view.findViewById<RecyclerView>(R.id.chatRoomList)
        chatRoomListView.adapter = adapter
        chatRoomListView.layoutManager = LinearLayoutManager(context)

        val swipeController = SwipeController(object : SwipeControllerActions() {
            override fun onRightClicked(position: Int) { //방 삭제
                super.onLeftClicked(position)
                val roomUid = items!![position].roomUid
                val preMyRoom = Utils.readData(context!!, "myRoom", "null")
                val nowMyRoom = preMyRoom!!.replace("/$roomUid", "")
                Utils.saveData(context!!, "myRoom", nowMyRoom)
                items!!.remove(items!![position])
                Utils.toast(context!!, "방에서 퇴장하셨습니다.\n화면을 위에서 아래로 당겨서 새로고침을 해주세요.",
                    FancyToast.LENGTH_SHORT, FancyToast.SUCCESS)
            }
            override fun onLeftClicked(position: Int) { //방 정보 설정
                super.onRightClicked(position)
                setRoomInformation(items!![position])
            }
        })

        val itemTouchHelper = ItemTouchHelper(swipeController)
        itemTouchHelper.attachToRecyclerView(chatRoomListView)

        chatRoomListView.addItemDecoration(object : RecyclerView.ItemDecoration() {
            override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
                swipeController.onDraw(c)
            }
        })

        val postListener = object : ValueEventListener {
            @SuppressLint("RestrictedApi")
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                try {
                    val roomData = dataSnapshot.getValue(ChatRoomListItem::class.java)
                    items!!.add(roomData!!)
                    adapter!!.notifyDataSetChanged()
                    chatRoomListView.scrollToPosition(adapter!!.itemCount - 1)
                }
                catch (e: Exception){
                    Utils.toast(context!!, "일부 방을 불러올 수 없습니다.",
                        FancyToast.LENGTH_SHORT, FancyToast.ERROR)
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {

            }
        }

        if(myRoom != "null") {
            items!!.clear()
            for (i in 1..myRoomCount) {
                try {
                    reference!!.child(myRoom!!.split("/")[i])
                        .addValueEventListener(postListener)
                }
                catch (e: Exception){
                    Utils.toast(context!!, "방을 불러올 수 없습니다.", FancyToast.LENGTH_SHORT, FancyToast.WARNING)
                }
            }
        }

        view.findViewById<SwipeRefreshLayout>(R.id.refresh).setOnRefreshListener {
            items!!.clear()
            myRoom = Utils.readData(context!!, "myRoom", "null")
            myRoomCount = StringUtils.countMatches(myRoom, "/")

            if(myRoom != "null") {
                for (i in 1..myRoomCount) {
                    try {
                        reference!!.child(myRoom!!.split("/")[i])
                            .addValueEventListener(postListener)
                    }
                    catch (e: Exception){
                        Utils.toast(context!!, "방을 불러올 수 없습니다.", FancyToast.LENGTH_SHORT, FancyToast.WARNING)
                    }
                }
            }

            view.findViewById<SwipeRefreshLayout>(R.id.refresh).setColorSchemeColors(Color.parseColor("#9e9e9e"))
            view.findViewById<SwipeRefreshLayout>(R.id.refresh).isRefreshing = false

        }

        return view
    }

    private fun setRoomInformation(roomData: ChatRoomListItem){
        Utils.toast(context!!, "방 사진을 누르시면 사진을 설정하실 수 있습니다.",
            FancyToast.LENGTH_SHORT, FancyToast.INFO)
        var roomPicUri:String? = roomData.roomPicUri

        val dialog = AlertDialog.Builder(context)
        dialog.setTitle("방 정보 수정")

        val layout = LinearLayout(context)
        layout.orientation = LinearLayout.VERTICAL

        val roomPicView = CircleImageView(context)
        roomPicView.borderWidth = 8
        roomPicView.borderColor = Color.parseColor("#9e9e9e")
        Glide.with(context!!).load(roomData.roomPicUri).format(DecodeFormat.PREFER_ARGB_8888)
            .diskCacheStrategy(DiskCacheStrategy.RESOURCE).into(roomPicView)

        roomPicView.setOnClickListener {
            val permissionlistener = object : PermissionListener {
                override fun onPermissionGranted() {
                    TedBottomPicker.with(activity)
                        .setImageProvider { imageView, imageUri ->
                            val options = RequestOptions().centerCrop()
                            Glide.with(context!!).load(imageUri).apply(options).into(imageView)
                        }
                        .show {
                            roomPicUri = it.toString()
                            Glide.with(context!!).load(it).format(DecodeFormat.PREFER_ARGB_8888)
                                .diskCacheStrategy(DiskCacheStrategy.RESOURCE).into(roomPicView)
                        }
                }

                override fun onPermissionDenied(deniedPermissions: List<String>) {
                    Utils.toast(context!!, "권한 사용에 동의 해 주셔야 사진을 불러올 수 있습니다.\n" +
                            "내부메모리 접근 권한 사용이 거절되어, 사진이 기본 방 사진으로 대채됩니다.",
                        FancyToast.WARNING, FancyToast.LENGTH_SHORT)
                }
            }

            TedPermission.with(activity)
                .setPermissionListener(permissionlistener)
                .setRationaleTitle("권한 필요")
                .setRationaleMessage("방 사진으로 지정할 사진을 불러오기 위해서 내부메모리에 접근 권한이 필요합니다.\n" +
                        "권한 사용을 허용해 주세요.")
                .setDeniedTitle("내부 메모리 접근 권한 필요")
                .setDeniedMessage("방 사진으로 지정할 사진을 불러오기 위해서 내부메모리에 접근 권한이 필요합니다.\n" +
                        "어플 설정애서 해당 권한의 사용을 허락해 주세요.")
                .setPermissions(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .check()
        }

        val textInputLayout = TextInputLayout(context!!)
        textInputLayout.isFocusableInTouchMode = true
        textInputLayout.isCounterEnabled = true

        val textInputEditText = TextInputEditText(context)
        textInputEditText.filters = arrayOf(InputFilter.LengthFilter(20))
        textInputEditText.hint = "방 이름 입력..."
        textInputLayout.addView(textInputEditText)

        val params = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
        )

        params.leftMargin = resources.getDimensionPixelSize(R.dimen.dialog_margin)
        params.rightMargin = resources.getDimensionPixelSize(R.dimen.dialog_margin)
        params.topMargin = resources.getDimensionPixelSize(R.dimen.dialog_margin)

        val params2 = LinearLayout.LayoutParams(500, 500)
        params2.topMargin = resources.getDimensionPixelSize(R.dimen.dialog_margin)
        params2.gravity = Gravity.CENTER

        roomPicView.layoutParams = params2
        textInputLayout.layoutParams = params

        layout.addView(roomPicView)
        layout.addView(textInputLayout)

        dialog.setView(layout)
        dialog.setPositiveButton("수정 완료") { _, _ ->
            val newRoomData = ChatRoomListItem(
                textInputEditText.text.toString(),
                roomData.time,
                "[방 정보가 변경되었습니다]",
                roomPicUri,
                roomData.roomUid
            )
            reference!!.child(roomData.roomUid!!).setValue(newRoomData)
            Utils.toast(context!!,
                "버그가 생길수 있으니 방 새로고침을 해 주세요.",
                FancyToast.LENGTH_SHORT,
                FancyToast.WARNING)
        }
        dialog.setNegativeButton("취소", null)
        dialog.show()
    }
}