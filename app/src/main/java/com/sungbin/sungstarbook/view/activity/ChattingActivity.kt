@file:Suppress("DEPRECATION")

package com.sungbin.sungstarbook.view.activity

import android.Manifest
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import com.sungbin.sungstarbook.dto.ChattingItem
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.sungbin.sungstarbook.R
import kotlinx.android.synthetic.main.content_chat.*
import com.sungbin.sungstarbook.adapter.ChatAdapter
import com.sungbin.sungstarbook.utils.Utils
import kotlinx.android.synthetic.main.activity_chat.*
import android.annotation.SuppressLint
import android.content.Intent
import android.view.*
import cn.pedant.SweetAlert.SweetAlertDialog
import com.shashank.sony.fancytoastlib.FancyToast
import com.yarolegovich.slidingrootnav.SlideGravity
import com.yarolegovich.slidingrootnav.SlidingRootNav
import com.yarolegovich.slidingrootnav.SlidingRootNavBuilder
import org.apache.commons.lang3.StringUtils
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import kotlinx.android.synthetic.main.chat_right_drawer_menu.*
import com.google.firebase.database.DataSnapshot
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.TedPermission
import com.sungbin.sungstarbook.utils.FirebaseUtils
import com.sungbin.sungstarbook.view.editor.EditImageActivity
import gun0912.tedbottompicker.TedBottomPicker


@Suppress("PLUGIN_WARNING", "NAME_SHADOWING", "DEPRECATION")
@SuppressLint("SimpleDateFormat")
class ChattingActivity : AppCompatActivity() {

    private var reference: DatabaseReference? = null
    private var items: ArrayList<ChattingItem>? = null
    private var adapter: ChatAdapter? = null
    private var slidingRootNav: SlidingRootNav? = null
    private var lastMsg: String = ""
    private var uid:String? = null
    private var myName:String? = null
    private var profilePicUri:String? = null
    private var roomName:String? = null
    private var roomUid:String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        roomName = intent.getStringExtra("name")
        roomUid = intent.getStringExtra("roomUid").replace(":","_")

        toolbar.title = ""
        toolbar_title.text = roomName
        setSupportActionBar(toolbar)

        slidingRootNav = SlidingRootNavBuilder(this)
            .withMenuOpened(false)
            .withContentClickableWhenMenuOpened(false)
            .withSavedState(savedInstanceState)
            .withGravity(SlideGravity.RIGHT)
            .withMenuLayout(R.layout.chat_right_drawer_menu)
            .inject()

        if(!Utils.readData(applicationContext, roomUid!!, "true")!!.toBoolean()){
            Glide.with(applicationContext)
                .load(R.drawable.ic_notifications_off_gray_24dp)
                .into(noti_onoff)
        } else FirebaseUtils.subscribe(roomUid!!, applicationContext)

        noti_onoff.setOnClickListener {
            val roomState = Utils.readData(applicationContext, roomUid!!, "true")!!.toBoolean()
            if(roomState){
                Glide.with(applicationContext)
                    .load(R.drawable.ic_notifications_off_gray_24dp)
                    .into(noti_onoff)
                Utils.saveData(applicationContext, roomUid!!, "false")
                FirebaseUtils.unSubscribe(roomUid!!, applicationContext)
            } else {
                Glide.with(applicationContext)
                    .load(R.drawable.ic_notifications_gray_24dp)
                    .into(noti_onoff)
                Utils.saveData(applicationContext, roomUid!!, "true")
                FirebaseUtils.subscribe(roomUid!!, applicationContext)
            }
        }

        getRoomUid.setOnClickListener {
            Utils.toast(applicationContext, "방의 입장코드가 복사되었습니다.",
                FancyToast.LENGTH_SHORT, FancyToast.SUCCESS)
            Utils.copy(applicationContext, roomUid!!.replace("_", ":"), false)
        }

        reference = FirebaseDatabase.getInstance().reference.child("ChatDB")
            .child(roomUid!!).child("chat")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            with(window) {
                decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                statusBarColor = Color.WHITE
                navigationBarColor = Color.WHITE
            }
        }

        uid = Utils.readData(applicationContext, "uid", "null")!!
        myName = Utils.readData(applicationContext, "myName", "null")!!

        items = ArrayList()
        adapter = ChatAdapter(items, this)

        (chatView as RecyclerView).layoutManager = LinearLayoutManager(applicationContext)
        (chatView as RecyclerView).setHasFixedSize(true)
        (chatView as RecyclerView).adapter = adapter

        val storage = FirebaseStorage.getInstance()
        val storageRef = storage.reference
            .child("Profile_Image/$uid/Profile.png")
        storageRef.downloadUrl.addOnSuccessListener { uri ->
            profilePicUri = uri.toString()
        }
        storageRef.downloadUrl.addOnFailureListener {
            profilePicUri = "https://cdn.pixabay.com/photo/2015/10/05/22/37/blank-profile-picture-973460_960_720.png"
        }

        reference!!.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {
                try {
                    val chatData = dataSnapshot.getValue(ChattingItem::class.java)
                    if(lastMsg == chatData!!.msg){
                        return
                    }
                    else lastMsg = chatData.msg!!
                    items!!.add(chatData)
                    adapter!!.notifyDataSetChanged()
                    chatView.scrollToPosition(adapter!!.itemCount - 1)
                } catch (e: Exception) {
                    Utils.error(applicationContext, e)
                }

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

        sendText.setOnClickListener {
            if(StringUtils.isBlank(inputText!!.text.toString())) {
                Utils.toast(applicationContext,
                    "내용을 입력해 주세요.",
                    FancyToast.LENGTH_SHORT,
                    FancyToast.WARNING)
            }
            else {
                val chatData = ChattingItem(
                    myName,
                    getTime(),
                    inputText!!.text.toString(),
                    "msg",
                    profilePicUri,
                    "null",
                    uid
                )
                FirebaseUtils.showNoti(myName!!, inputText!!.text.toString(), roomUid!!)
                reference!!.push().setValue(chatData)
                inputText!!.setText("")
            }
        }

        sendMore.setOnClickListener {
            val permissionlistener = object : PermissionListener {
                override fun onPermissionGranted() {
                    TedBottomPicker.with(this@ChattingActivity)
                        .setImageProvider { imageView, imageUri ->
                            val options = RequestOptions().centerCrop()
                            Glide.with(baseContext).load(imageUri).apply(options).into(imageView)
                        }
                        .show {
                            val imageName = it.toString().substring(it.toString().lastIndexOf("/") + 1, it.toString().length)
                            val imageType = imageName.substring(imageName.lastIndexOf(".") + 1, imageName!!.length).toLowerCase()
                            val isGif = imageType.contains("gif")
                            val resultUri = it.toString()
                            val imageUid = getRandomString(13)
                            if(!isGif) {
                                val intent = Intent(this@ChattingActivity, EditImageActivity::class.java)
                                    .putExtra("imageUri", resultUri)
                                    .putExtra("imageUid", imageUid)
                                    .putExtra("myUid", uid)
                                    .putExtra("myName", myName)
                                    .putExtra("roomUid", roomUid)
                                    .putExtra("imageName", imageName)
                                    .putExtra("profileImageUri", profilePicUri)
                                startActivity(intent)
                            } else {
                                val pDialog = SweetAlertDialog(this@ChattingActivity, SweetAlertDialog.PROGRESS_TYPE)
                                pDialog.progressHelper.barColor = Color.parseColor("#A5DC86")
                                pDialog.setTitle("사진 업로드중...")
                                pDialog.setCancelable(false)
                                pDialog.show()
                                val storage = FirebaseStorage.getInstance()
                                val storageRef = storage.reference
                                    .child("Chatting_Image/$uid/$imageUid")
                                storageRef.putFile(it).addOnFailureListener { exception ->
                                    pDialog.dismissWithAnimation()
                                    FancyToast.makeText(
                                        applicationContext,
                                        "사진 업로드중에 문제가 발생하였습니다.\nError: " + exception.cause,
                                        FancyToast.LENGTH_SHORT,
                                        FancyToast.ERROR, false
                                    ).show()
                                    pDialog.dismissWithAnimation()
                                }.addOnSuccessListener {
                                    pDialog.dismissWithAnimation()
                                    FancyToast.makeText(
                                        applicationContext,
                                        "사진이 업로드 되었습니다.",
                                        FancyToast.LENGTH_SHORT,
                                        FancyToast.SUCCESS, false
                                    ).show()
                                    val chatData = ChattingItem(
                                        myName,
                                        getTime(),
                                        imageName,
                                        "image",
                                        profilePicUri,
                                        imageUid,
                                        uid
                                    )
                                    FirebaseDatabase.getInstance().reference.child("ChatDB")
                                        .child(roomUid!!).child("chat").push().setValue(chatData)
                                }.addOnProgressListener { it ->
                                    val progress = 100 * it.bytesTransferred / it.totalByteCount.toInt()
                                    pDialog.setTitle("사진 업로드중... ($progress/100)")
                                }
                            }
                        }
                }

                override fun onPermissionDenied(deniedPermissions: List<String>) {
                    Utils.toast(applicationContext, "권한 사용에 동의 해야 전송할 사진을 불러올 수 있습니다.\n" +
                            "내부메모리 접근 권한 사용이 거절되어, 사진 전송 기능이 비활성화 됩니다.",
                        FancyToast.WARNING, FancyToast.LENGTH_SHORT)
                }
            }

            TedPermission.with(this)
                .setPermissionListener(permissionlistener)
                .setRationaleTitle("권한 필요")
                .setRationaleMessage("채팅창에 보낼 사진을 불러오기 위해서 내부메모리에 접근 권한이 필요합니다.\n" +
                        "권한 사용을 허용해 주세요.")
                .setDeniedTitle("내부 메모리 접근 권한 필요")
                .setDeniedMessage("채팅창에 보낼 사진을 불러오기 위해서 내부메모리에 접근 권한이 필요합니다.\n" +
                        "어플 설정애서 해당 권한의 사용을 허락해 주세요.")
                .setPermissions(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .check()
        }

    }

    private fun getTime(): String{
        val sdf = SimpleDateFormat("aa hh:mm")
        return sdf.format(Date(System.currentTimeMillis()))
    }

    private fun getRandomString(length: Int): String {
        val buffer = StringBuffer()
        val random = Random()

        val strings = charArrayOf('a','b','c','d','e','f','g','h','i',
            'j','k','l','m','n','o','p','q','r','s','t','u','v','w','x','y','z')
        for (i in 0..length) {
            buffer.append(strings[random.nextInt(strings.size)])
        }
        return buffer.toString()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.chat_toolbar_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.show_user -> {
                slidingRootNav!!.openMenu()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

}