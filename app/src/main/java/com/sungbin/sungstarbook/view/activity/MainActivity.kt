@file:Suppress("DEPRECATION")

package com.sungbin.sungstarbook.view.activity

import android.Manifest
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.iammert.library.readablebottombar.ReadableBottomBar
import com.sungbin.sungstarbook.R
import com.sungbin.sungstarbook.utils.Utils
import com.sungbin.sungstarbook.view.fragments.ChatRoom
import com.sungbin.sungstarbook.view.fragments.Friends
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Uri
import android.text.InputFilter
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.appcompat.app.AlertDialog
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.storage.FirebaseStorage
import com.shashank.sony.fancytoastlib.FancyToast
import com.sungbin.sungstarbook.dto.ChatRoomListItem
import kotlinx.android.synthetic.main.activity_main.toolbar
import java.text.SimpleDateFormat
import java.util.*
import android.view.animation.TranslateAnimation
import android.widget.LinearLayout
import androidx.annotation.Nullable
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.database.*
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.TedPermission
import com.sungbin.sungstarbook.BuildConfig
import com.sungbin.sungstarbook.utils.Utils.toast
import java.lang.Exception


@SuppressLint("SimpleDateFormat")
class MainActivity : AppCompatActivity() {

    private var fm: FragmentManager? = null
    private var fragmentTransaction: FragmentTransaction? = null
    private val reference = FirebaseDatabase.getInstance().reference.child("RoomDB")
    @SuppressLint("StaticFieldLeak")
    private val remoteConfig = FirebaseRemoteConfig.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        FirebaseMessaging.getInstance().isAutoInitEnabled = true

        val permissionlistener = object : PermissionListener {
            override fun onPermissionGranted() {
                if(!checkInternet()) {
                    toast(applicationContext, "SungStarBook을 이용하기 위해서는 인터넷 연결이 필요합니다.\n" +
                            "인터넷 연결후 다시 접속해 주세요.\n\n" +
                            "앱을 종료합니다.",
                        FancyToast.LENGTH_SHORT,
                        FancyToast.WARNING)
                    finish()
                } else {
                    val configSettings = FirebaseRemoteConfigSettings.Builder()
                    .setDeveloperModeEnabled(BuildConfig.DEBUG)
                    .build()
                    remoteConfig.setConfigSettings(configSettings)

                    remoteConfig.fetch(60).addOnCompleteListener { task ->
                        if(task.isSuccessful) {
                            remoteConfig.activateFetched()
                        } else {
                            Utils.error(applicationContext,
                                "서버에서 데이터를 불러오는 중에 오류가 발생했습니다.\n${task.exception}")
                        }
                        displayMessage()
                    }
                }
            }

            override fun onPermissionDenied(deniedPermissions: List<String>) {
                toast(applicationContext, "최신버전 체크가 되어야 SungStarBook을 이용하실수 있습니다.\n" +
                        "앱이 종료됩니다.",
                    FancyToast.WARNING, FancyToast.LENGTH_SHORT)
                finish()
            }
        }

        TedPermission.with(this)
            .setPermissionListener(permissionlistener)
            .setRationaleTitle("권한 필요")
            .setRationaleMessage("최신버전 체크를 하기 위해서 WiFi의 상태를 가져오는 권한이 필요합니다.\n" +
                    "권한 사용을 허용해 주세요.")
            .setDeniedTitle("WiFi 접근 권한 필요")
            .setDeniedMessage("최신버전 체크를 하기 위해서 WiFi의 상태를 가져오는 권한이 필요합니다.\n" +
                    "어플 설정애서 해당 권한의 사용을 허락해 주세요.")
            .setPermissions(Manifest.permission.ACCESS_WIFI_STATE)
            .check()

        toolbar.title = ""
        setSupportActionBar(toolbar)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            with(window) {
                decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                statusBarColor = Color.WHITE
                navigationBarColor = Color.WHITE
            }
        }

        fm = supportFragmentManager
        fragmentTransaction = fm!!.beginTransaction().apply {
            replace(R.id.page, Friends())
            commit()
        }

        val uid = Utils.readData(applicationContext, "uid", "null")!!
        var profilePicUri:String? = null

        val storage = FirebaseStorage.getInstance()
        val storageRef = storage.reference
            .child("Profile_Image/$uid/Profile.png")
        storageRef.downloadUrl.addOnSuccessListener { uri ->
            profilePicUri = uri.toString()
        }
        storageRef.downloadUrl.addOnFailureListener { e ->
            Utils.error(applicationContext, e)
        }

        fab.setOnClickListener {
            val bottomSheetDialog = ChatAddSheetDialog(
                this@MainActivity,
                makeRandomString(),
                getTime(),
                profilePicUri!!,
                reference
            )
            bottomSheetDialog.show(supportFragmentManager, "More Login")
        }

        bottomBar.setOnItemSelectListener( object : ReadableBottomBar.ItemSelectListener{
            override fun onItemSelected(index: Int) {
                when(index){
                    0 -> {
                        fragmentTransaction = fm!!.beginTransaction().apply {
                            replace(R.id.page, Friends())
                            commit()
                        }

                        if(fab.visibility == View.VISIBLE) {
                            val animate = TranslateAnimation(0F, fab.width.toFloat() + 50F, 0F, 0F) //탭 바꿨을때 왼쪽에서 오른쪽으로 빠지는 부분
                            animate.duration = 400
                            animate.fillAfter = true
                            fab.startAnimation(animate)
                            fab.visibility = View.INVISIBLE
                        }
                    }
                    1 -> {
                        fragmentTransaction = fm!!.beginTransaction().apply {
                            replace(R.id.page, ChatRoom())
                            commit()
                        }

                        if(fab.visibility == View.INVISIBLE) {
                            val animate = TranslateAnimation(fab.width.toFloat(), 0F, 0F, 0F)
                            animate.duration = 300
                            animate.fillAfter = true
                            fab.startAnimation(animate)
                            fab.visibility = View.VISIBLE
                        }
                    }
                    else -> {
                        toast(applicationContext, "개발중...")
                        fragmentTransaction = fm!!.beginTransaction().apply {
                            replace(R.id.page, Friends())
                            commit()
                        }

                        if(fab.visibility == View.VISIBLE) {
                            val animate = TranslateAnimation(0F, fab.width.toFloat() + 50F, 0F, 0F) //탭 바꿨을때 왼쪽에서 오른쪽으로 빠지는 부분
                            animate.duration = 400
                            animate.fillAfter = true
                            fab.startAnimation(animate)
                            fab.visibility = View.INVISIBLE
                        }
                    }
                }
            }
        })

    }

     private fun getAppVersionName(): String{
         return try{
             val packageInfo = packageManager.getPackageInfo(packageName, 0)
             packageInfo.versionName
         } catch (e: Exception) {
             "null"
         }
    }

    private fun displayMessage() {
        val newVersionName = remoteConfig.getString("version_name")
        val nowVersionName = getAppVersionName()

        val noticeCode = remoteConfig.getString("notice_code")
        val notice = remoteConfig.getString("notice_msg").replace("\\n", "\n")
        val wasShowNotice = Utils.readData(applicationContext, noticeCode, "false")!!.toBoolean()

        if(newVersionName != nowVersionName){
            toast(applicationContext, "최신버전으로 업데이트가 필요합니다.", FancyToast.LENGTH_LONG, FancyToast.INFO)
            showNewVersionDialog()
            toast(applicationContext, "SungStarBook 채팅방에서 최신버전을 다운받아 주세요.")
            finish()
        }

        if(!wasShowNotice){
            showNotice(notice)
            Utils.saveData(applicationContext, noticeCode, "true")
        }
    }

    private fun showNotice(content: String){
        val dialog = AlertDialog.Builder(this@MainActivity)
        dialog.setTitle("공지사항")
        dialog.setMessage(content)
        dialog.setPositiveButton("닫기", null)
        dialog.setCancelable(false)
        dialog.show()
    }

    private fun showNewVersionDialog(){
        val i = Intent(Intent.ACTION_VIEW)
        i.data = Uri.parse("market://details?id=com.sungbin.sungstarbook")
        startActivity(i)
    }

    private fun checkInternet(): Boolean{
        val manager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val isMobileAvailable = manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).isAvailable
        val isMobileConnect = manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).isConnectedOrConnecting
        val isWifiAvailable = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).isAvailable
        val isWifiConnect = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).isConnectedOrConnecting

        return (isWifiAvailable && isWifiConnect) || (isMobileAvailable && isMobileConnect)
}

    private fun makeRandomString(): String{
        val rnd = Random()
        val buf = StringBuffer()

        for (i in 0..10) {
            if (rnd.nextBoolean()) {
                buf.append((rnd.nextInt(26) + 97).toChar())
            } else {
                buf.append(rnd.nextInt(10))
            }
        }

        return buf.toString() + getTimeForUid()
    }

    private fun getTime(): String{
        val sdf = SimpleDateFormat("aa hh:mm")
        return sdf.format(Date(System.currentTimeMillis()))
    }

    private fun getTimeForUid(): String{
        val sdf = SimpleDateFormat("hh:mm:ss")
        return sdf.format(Date(System.currentTimeMillis()))
    }

    class ChatAddSheetDialog constructor(
        private var act: Activity,
        private var roomUid: String,
        private var time: String,
        private var profilePicUri: String,
        private var reference: DatabaseReference) : BottomSheetDialogFragment(), View.OnClickListener {

        private var room_join: LinearLayout? = null
        private var room_add: LinearLayout? = null

        @Nullable
        override fun onCreateView(inflater: LayoutInflater, @Nullable container: ViewGroup?, @Nullable savedInstanceState: Bundle?): View? {
            val view = inflater.inflate(R.layout.room_action_navigation_view, container, false)

            room_join= view.findViewById(R.id.room_join)
            room_add = view.findViewById(R.id.room_add)

            room_join!!.setOnClickListener(this)
            room_add!!.setOnClickListener(this)

            return view
        }

        override fun onClick(view: View) {
            when (view.id) {
                R.id.room_add ->{
                    val dialog = AlertDialog.Builder(act)
                    dialog.setTitle("채팅 방 생성")

                    val textInputLayout = TextInputLayout(act)
                    textInputLayout.isFocusableInTouchMode = true
                    textInputLayout.isCounterEnabled = true

                    val textInputEditText = TextInputEditText(act)
                    textInputEditText.filters = arrayOf(InputFilter.LengthFilter(20))
                    textInputEditText.hint = "생성할 방 이름..."
                    textInputLayout.addView(textInputEditText)

                    val container = FrameLayout(act)
                    val params = FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
                    )

                    params.leftMargin = resources.getDimensionPixelSize(R.dimen.dialog_margin)
                    params.rightMargin = resources.getDimensionPixelSize(R.dimen.dialog_margin)
                    params.topMargin = resources.getDimensionPixelSize(R.dimen.dialog_margin)

                    textInputLayout.layoutParams = params
                    container.addView(textInputLayout)

                    dialog.setView(container)
                    dialog.setPositiveButton("확인") { _, _ ->
                        val roomData = ChatRoomListItem(
                            textInputEditText.text.toString(),
                            time,
                            "[채팅방을 생성했습니다]",
                            profilePicUri,
                            roomUid
                        )
                        reference.child(roomUid).setValue(roomData)
                        toast(act, "채팅방이 생성되었습니다." +
                                "\n화면을 위에서 아래로 당겨서 새로고침을 해주세요.",
                            FancyToast.LENGTH_SHORT, FancyToast.SUCCESS)
                    }
                    dialog.show()

                }
                R.id.room_join ->{
                    val dialog = AlertDialog.Builder(act)
                    dialog.setTitle("채팅 방 입장")

                    val textInputLayout = TextInputLayout(act)
                    textInputLayout.isFocusableInTouchMode = true
                    textInputLayout.isCounterEnabled = true

                    val textInputEditText = TextInputEditText(act)
                    textInputEditText.filters = arrayOf(InputFilter.LengthFilter(20))
                    textInputEditText.hint = "입장할 방의 참여코드..."
                    textInputLayout.addView(textInputEditText)

                    val container = FrameLayout(act)
                    val params = FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
                    )

                    params.leftMargin = resources.getDimensionPixelSize(R.dimen.dialog_margin)
                    params.rightMargin = resources.getDimensionPixelSize(R.dimen.dialog_margin)
                    params.topMargin = resources.getDimensionPixelSize(R.dimen.dialog_margin)

                    textInputLayout.layoutParams = params
                    container.addView(textInputLayout)

                    dialog.setView(container)
                    dialog.setPositiveButton("확인") { _, _ ->
                        val roomUid = textInputEditText.text.toString()
                        reference.child(roomUid)
                            .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onCancelled(p0: DatabaseError) {

                            }

                            override fun onDataChange(snapshot: DataSnapshot) {
                                if (snapshot.value == null) {
                                    toast(act, "존재하지 않는 참여코드 입니다.",
                                        FancyToast.LENGTH_SHORT, FancyToast.WARNING)
                                }
                                else {


                                    toast(act, "채팅방에 입장하였습니다." +
                                            "\n당겨서 새로고침을 해주세요.",
                                        FancyToast.LENGTH_SHORT, FancyToast.SUCCESS)
                                }
                            }
                        })
                    }
                    dialog.show()
                }
            }
            dismiss()
        }
    }

}