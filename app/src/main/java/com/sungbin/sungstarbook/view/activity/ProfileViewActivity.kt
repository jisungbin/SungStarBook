package com.sungbin.sungstarbook.view.activity

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import java.io.ByteArrayOutputStream
import android.os.Build
import android.text.InputFilter
import android.util.Log
import android.view.*
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.annotation.Nullable
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityOptionsCompat
import androidx.fragment.app.FragmentActivity
import cn.pedant.SweetAlert.SweetAlertDialog
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.RequestOptions.bitmapTransform
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.TedPermission
import com.shashank.sony.fancytoastlib.FancyToast
import com.sungbin.sungstarbook.R
import com.sungbin.sungstarbook.dto.ChatRoomListItem
import com.sungbin.sungstarbook.dto.ChattingItem
import com.sungbin.sungstarbook.utils.Utils
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import gun0912.tedbottompicker.TedBottomPicker
import jp.wasabeef.glide.transformations.BlurTransformation
import kotlinx.android.synthetic.main.activity_profile_view.*
import kotlinx.android.synthetic.main.content_chat.*
import kotlinx.android.synthetic.main.content_profile_view.*
import kotlinx.android.synthetic.main.content_profile_view.profile_image
import kotlinx.android.synthetic.main.profile_edit_navigation_view.*
import kotlinx.android.synthetic.main.room_action_navigation_view.*

class ProfileViewActivity : AppCompatActivity() {

    private lateinit var uid: String
    private var reference: DatabaseReference? = null
    private var profileImageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN)

        setContentView(R.layout.activity_profile_view)

        uid = Utils.readData(applicationContext, "uid", "null")!!

        val storage = FirebaseStorage.getInstance()
        val storageRefProfileImage = storage.reference
            .child("Profile_Image/$uid/Profile.png")
        storageRefProfileImage.downloadUrl.addOnSuccessListener { uri ->
            Glide.with(applicationContext).load(uri)
                .format(DecodeFormat.PREFER_ARGB_8888)
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE).into(profile_image)
            profileImageUri = uri}
        storageRefProfileImage.downloadUrl.addOnFailureListener { e ->
            Utils.toast(applicationContext!!, "프로필 사진을 불러올 수 없습니다.\n\n$e", FancyToast.LENGTH_SHORT, FancyToast.WARNING) }

        val storageRefBackImage = storage.reference
            .child("Profile_Image/$uid/Back.png")
        storageRefBackImage.downloadUrl.addOnSuccessListener { uri ->
            Glide.with(applicationContext).load(uri).format(DecodeFormat.PREFER_ARGB_8888)
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE).into(back_image) }
        storageRefBackImage.downloadUrl.addOnFailureListener {
            Glide.with(applicationContext).load(profileImageUri)
                .apply(bitmapTransform(BlurTransformation(25, 3)))
                .format(DecodeFormat.PREFER_ARGB_8888)
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE).into(back_image)
             }

        fab.setOnClickListener {
            val bottomSheetDialog = ProfileEditSheetDialog(
                this@ProfileViewActivity,
                uid, reference!!)
            bottomSheetDialog.show(supportFragmentManager, "More Login")
        }

        reference = FirebaseDatabase.getInstance().reference.child("UserDB")
            .child(Utils.readData(applicationContext, "uid", "null")!!).child("now_text")
        reference!!.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {
                try {
                    now_text.post {
                        now_text.text = dataSnapshot.value.toString()
                    }
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

        profile_image.setOnClickListener {
            val image = profile_image.drawable
            val sendBitmap = (image as BitmapDrawable).bitmap
            val stream = ByteArrayOutputStream()
            sendBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            val byteArray = stream.toByteArray()
            val intent = Intent(applicationContext, ImageViewerActivity::class.java)
                .putExtra("image", byteArray).putExtra("tag", "profile_image")
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                this,
                findViewById(R.id.profile_image),
                "profile_image")
            if (Build.VERSION.SDK_INT >= 21) {
                startActivity(intent, options.toBundle())
            } else startActivity(intent)
        }

        back_image.setOnClickListener {
            val image = back_image.drawable
            val sendBitmap = (image as BitmapDrawable).bitmap
            val stream = ByteArrayOutputStream()
            sendBitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
            val byteArray = stream.toByteArray()
            val intent = Intent(applicationContext, ImageViewerActivity::class.java)
                .putExtra("image", byteArray).putExtra("tag", "back_image")
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                this,
                findViewById(R.id.profile_image),
                "back_image")
            if (Build.VERSION.SDK_INT >= 21) {
                startActivity(intent, options.toBundle())
            } else startActivity(intent)
        }

    }

    @SuppressLint("MissingSuperCall")
    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result = CropImage.getActivityResult(data)
            if (resultCode == Activity.RESULT_OK) {
                val uri = result.uri
                val pDialog = SweetAlertDialog(this@ProfileViewActivity, SweetAlertDialog.PROGRESS_TYPE)
                pDialog.progressHelper.barColor = Color.parseColor("#A5DC86")
                pDialog.titleText = "프로필 사진 업로드중..."
                pDialog.setCancelable(false)
                pDialog.show()

                val storageRef = FirebaseStorage.getInstance().reference
                    .child("Profile_Image/$uid/Profile.png")
                storageRef.putFile(uri).addOnSuccessListener {
                    pDialog.dismissWithAnimation()
                    Utils.toast(applicationContext, "프로필 사진이 업로드 되었습니다.",
                        FancyToast.LENGTH_SHORT, FancyToast.SUCCESS)
                    Glide.with(applicationContext)
                        .load(uri)
                        .format(DecodeFormat.PREFER_ARGB_8888)
                        .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                        .into(profile_image)
                }.addOnFailureListener {
                    pDialog.dismissWithAnimation()
                    Utils.toast(applicationContext, "프로필 사진이 업로드중에 문제가 발생하였습니다.\n\n${it.cause}",
                        FancyToast.LENGTH_SHORT, FancyToast.WARNING)
                }.addOnProgressListener {
                    val progress = (100 * it.bytesTransferred) /  it.totalByteCount.toInt()
                    pDialog.titleText = "프로필 사진 업로드중... ($progress/100)"
                }
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                val error = result.error
                Utils.error(this, "프로필 사진을 선택하는 도중에 오류가 발생했습니다.\n$error")
            }
        }
    }

    class ProfileEditSheetDialog constructor(
        private var act: FragmentActivity,
        private var uid: String,
        private var reference: DatabaseReference) : BottomSheetDialogFragment(), View.OnClickListener {

        private var name_edit: LinearLayout? = null
        private var now_text_edit: LinearLayout? = null
        private var profile_image_edit: LinearLayout? = null
        private var back_image_edit: LinearLayout? = null

        @Nullable
        override fun onCreateView(inflater: LayoutInflater, @Nullable container: ViewGroup?, @Nullable savedInstanceState: Bundle?): View? {
            val view = inflater.inflate(R.layout.profile_edit_navigation_view, container, false)

            name_edit = view.findViewById(R.id.name_edit)
            now_text_edit = view.findViewById(R.id.now_text_edit)
            profile_image_edit = view.findViewById(R.id.profile_image_edit)
            back_image_edit = view.findViewById(R.id.back_image_edit)

            name_edit!!.setOnClickListener(this)
            now_text_edit!!.setOnClickListener(this)
            profile_image_edit!!.setOnClickListener(this)
            back_image_edit!!.setOnClickListener(this)

            return view
        }

        override fun onClick(view: View) {
            when (view.id) {
                R.id.name_edit ->{
                    val dialog = AlertDialog.Builder(act)
                    dialog.setTitle("닉네임 수정")

                    val textInputLayout = TextInputLayout(act)
                    textInputLayout.isFocusableInTouchMode = true
                    textInputLayout.isCounterEnabled = true

                    val textInputEditText = TextInputEditText(act)
                    textInputEditText.filters = arrayOf(InputFilter.LengthFilter(20))
                    textInputEditText.hint = "수정할 닉네임..."
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
                    dialog.setNegativeButton("취소", null)
                    dialog.setPositiveButton("확인") { _, _ ->
                        Utils.saveData(act, "myName", textInputEditText.text.toString())
                        Utils.toast(act, "닉네임이 수정되었습니다.",
                            FancyToast.LENGTH_SHORT,
                            FancyToast.SUCCESS)
                    }
                    dialog.show()
                }
                R.id.back_image_edit ->{
                    val permissionlistener = object : PermissionListener {
                        override fun onPermissionGranted() {
                            TedBottomPicker.with(act)
                                .setImageProvider { imageView, imageUri ->
                                    val options = RequestOptions().centerCrop()
                                    Glide.with(act).load(imageUri).apply(options)
                                        .format(DecodeFormat.PREFER_ARGB_8888)
                                        .diskCacheStrategy(DiskCacheStrategy.RESOURCE).into(imageView)
                                }
                                .show { uri ->
                                    val pDialog = SweetAlertDialog(act, SweetAlertDialog.PROGRESS_TYPE)
                                    pDialog.progressHelper.barColor = Color.parseColor("#A5DC86")
                                    pDialog.titleText = "배경 사진 업로드중..."
                                    pDialog.setCancelable(false)
                                    pDialog.show()

                                    val storageRef = FirebaseStorage.getInstance().reference
                                        .child("Profile_Image/$uid/Back.png")
                                    storageRef.putFile(uri).addOnSuccessListener {
                                        pDialog.dismissWithAnimation()
                                        Utils.toast(act, "배경 사진이 업로드 되었습니다.",
                                            FancyToast.LENGTH_SHORT, FancyToast.SUCCESS)
                                        Glide.with(act)
                                            .load(uri)
                                            .format(DecodeFormat.PREFER_ARGB_8888)
                                            .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                                            .into(back_image)
                                    }.addOnFailureListener {
                                        pDialog.dismissWithAnimation()
                                        Utils.toast(act, "배경 사진이 업로드중에 문제가 발생하였습니다.\n\n${it.cause}",
                                            FancyToast.LENGTH_SHORT, FancyToast.WARNING)
                                    }.addOnProgressListener {
                                        val progress = (100 * it.bytesTransferred) /  it.totalByteCount.toInt()
                                        pDialog.titleText = "배경 사진 업로드중... ($progress/100)"
                                    }
                                }
                        }

                        override fun onPermissionDenied(deniedPermissions: List<String>) {
                            Utils.toast(act, "권한 사용에 동의 해 주셔야 배경 사진을 불러올 수 있습니다.\n" +
                                    "내부메모리 접근 권한 사용이 거절되어, 배경 사진이 기본 사진으로 대채됩니다.",
                                FancyToast.WARNING, FancyToast.LENGTH_SHORT)
                        }
                    }

                    TedPermission.with(act)
                        .setPermissionListener(permissionlistener)
                        .setRationaleTitle("권한 필요")
                        .setRationaleMessage("배경 사진으로 지정할 사진을 불러오기 위해서 내부메모리에 접근 권한이 필요합니다.\n" +
                                "권한 사용을 허용해 주세요.")
                        .setDeniedTitle("내부 메모리 접근 권한 필요")
                        .setDeniedMessage("배경 사진으로 지정할 사진을 불러오기 위해서 내부메모리에 접근 권한이 필요합니다.\n" +
                                "어플 설정애서 해당 권한의 사용을 허락해 주세요.")
                        .setPermissions(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        .check()
                }
                R.id.profile_image_edit ->{
                    val permissionlistener = object : PermissionListener {
                        override fun onPermissionGranted() {
                            TedBottomPicker.with(act)
                                .setImageProvider { imageView, imageUri ->
                                    val options = RequestOptions().centerCrop()
                                    Glide.with(act).load(imageUri).apply(options).into(imageView)
                                }
                                .show { uri ->
                                    CropImage.activity(uri)
                                        .setCropShape(CropImageView.CropShape.OVAL)
                                        .setScaleType(CropImageView.ScaleType.FIT_CENTER)
                                        .setAutoZoomEnabled(true)
                                        .setGuidelines(CropImageView.Guidelines.ON)
                                        .setFixAspectRatio(true)
                                        .start(act)
                                }
                        }

                        override fun onPermissionDenied(deniedPermissions: List<String>) {
                            Utils.toast(act, "권한 사용에 동의 해 주셔야 프로필 사진을 불러올 수 있습니다.\n" +
                                    "내부메모리 접근 권한 사용이 거절되어, 프로필 사진이 기본 사진으로 대채됩니다.",
                                FancyToast.WARNING, FancyToast.LENGTH_SHORT)
                        }
                    }

                    TedPermission.with(act)
                        .setPermissionListener(permissionlistener)
                        .setRationaleTitle("권한 필요")
                        .setRationaleMessage("프로필 사진으로 지정할 사진을 불러오기 위해서 내부메모리에 접근 권한이 필요합니다.\n" +
                                "권한 사용을 허용해 주세요.")
                        .setDeniedTitle("내부 메모리 접근 권한 필요")
                        .setDeniedMessage("프로필 사진으로 지정할 사진을 불러오기 위해서 내부메모리에 접근 권한이 필요합니다.\n" +
                                "어플 설정애서 해당 권한의 사용을 허락해 주세요.")
                        .setPermissions(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        .check()
                }
                R.id.now_text_edit ->{
                    val dialog = AlertDialog.Builder(act)
                    dialog.setTitle("상태메세지 수정")

                    val textInputLayout = TextInputLayout(act)
                    textInputLayout.isFocusableInTouchMode = true
                    textInputLayout.isCounterEnabled = true

                    val textInputEditText = TextInputEditText(act)
                    textInputEditText.filters = arrayOf(InputFilter.LengthFilter(20))
                    textInputEditText.hint = "수정할 내용..."
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
                    dialog.setNegativeButton("취소", null)
                    dialog.setPositiveButton("확인") { _, _ ->
                        reference.push().setValue(textInputEditText.text.toString())
                        Utils.toast(act, "상태 메세지가 수정되었습니다.",
                            FancyToast.LENGTH_SHORT,
                            FancyToast.SUCCESS)
                    }
                    dialog.show()
                }
            }
            dismiss()
        }
    }

}
