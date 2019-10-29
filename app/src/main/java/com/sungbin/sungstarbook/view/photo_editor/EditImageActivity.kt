@file:Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")

package com.sungbin.sungstarbook.view.photo_editor

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.transition.ChangeBounds
import android.transition.TransitionManager
import android.view.View
import android.view.animation.AnticipateOvershootInterpolator
import android.widget.ImageView
import android.widget.TextView

import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Objects
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cn.pedant.SweetAlert.SweetAlertDialog
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.shashank.sony.fancytoastlib.FancyToast
import com.sungbin.sungstarbook.R
import com.sungbin.sungstarbook.dto.ChattingItem
import com.sungbin.sungstarbook.view.photo_editor.base.BaseActivity
import com.sungbin.sungstarbook.view.photo_editor.filters.FilterListener
import com.sungbin.sungstarbook.view.photo_editor.filters.FilterViewAdapter
import com.sungbin.sungstarbook.view.photo_editor.fragments.EmojiBSFragment
import com.sungbin.sungstarbook.view.photo_editor.fragments.PropertiesBSFragment
import com.sungbin.sungstarbook.view.photo_editor.fragments.StickerBSFragment
import com.sungbin.sungstarbook.view.photo_editor.fragments.TextEditorDialogFragment
import com.sungbin.sungstarbook.view.photo_editor.tools.EditingToolsAdapter
import com.sungbin.sungstarbook.view.photo_editor.tools.ToolType
import ja.burhanrashid52.photoeditor.OnPhotoEditorListener
import ja.burhanrashid52.photoeditor.PhotoEditor
import ja.burhanrashid52.photoeditor.PhotoEditorView
import ja.burhanrashid52.photoeditor.SaveSettings
import ja.burhanrashid52.photoeditor.ViewType
import ja.burhanrashid52.photoeditor.PhotoFilter

@Suppress("NAME_SHADOWING")
class EditImageActivity : BaseActivity(), OnPhotoEditorListener,
    View.OnClickListener, PropertiesBSFragment.Properties, EmojiBSFragment.EmojiListener,
    StickerBSFragment.StickerListener, EditingToolsAdapter.OnItemSelected, FilterListener {
    private var mPhotoEditor: PhotoEditor? = null
    private var mPhotoEditorView: PhotoEditorView? = null
    private var mPropertiesBSFragment: PropertiesBSFragment? = null
    private var mEmojiBSFragment: EmojiBSFragment? = null
    private var mStickerBSFragment: StickerBSFragment? = null
    private var mTxtCurrentTool: TextView? = null
    private var mRvTools: RecyclerView? = null
    private var mRvFilters: RecyclerView? = null
    private val mEditingToolsAdapter = EditingToolsAdapter(this)
    private val mFilterViewAdapter = FilterViewAdapter(this)
    private var mRootView: ConstraintLayout? = null
    private val mConstraintSet = ConstraintSet()
    private var mIsFilterVisible: Boolean = false
    private var imageUri: String? = null
    private var imageUid: String? = null
    private var roomUid: String? = null
    private var myName: String? = null
    private var myUid: String? = null
    private var imageName: String? = null
    private var profileImageUri: String? = null

    private val time: String
        @SuppressLint("SimpleDateFormat")
        get() {
            val sdf = SimpleDateFormat("aa hh:mm")
            return sdf.format(Date(System.currentTimeMillis()))
        }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        makeFullScreen()
        setContentView(R.layout.activity_edit_image)

        val i = intent
        myUid = i.getStringExtra("myUid")
        myName = i.getStringExtra("myName")
        roomUid = i.getStringExtra("roomUid")
        imageUri = i.getStringExtra("imageUri")
        imageUid = i.getStringExtra("imageUid")
        imageName = i.getStringExtra("imageName")
        profileImageUri = i.getStringExtra("profileImageUri")

        initViews()

        mPropertiesBSFragment = PropertiesBSFragment()
        mEmojiBSFragment = EmojiBSFragment()
        mStickerBSFragment = StickerBSFragment()
        mStickerBSFragment!!.setStickerListener(this)
        mEmojiBSFragment!!.setEmojiListener(this)
        mPropertiesBSFragment!!.setPropertiesChangeListener(this)

        val llmTools = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        mRvTools!!.layoutManager = llmTools
        mRvTools!!.adapter = mEditingToolsAdapter

        val llmFilters = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        mRvFilters!!.layoutManager = llmFilters
        mRvFilters!!.adapter = mFilterViewAdapter

        mPhotoEditor = PhotoEditor.Builder(this, mPhotoEditorView!!)
            .setPinchTextScalable(true)
            .build()

        mPhotoEditor!!.setOnPhotoEditorListener(this)
    }

    private fun initViews() {
        val imgUndo: ImageView = findViewById(R.id.imgUndo)
        val imgRedo: ImageView = findViewById(R.id.imgRedo)
        val imgCamera: ImageView = findViewById(R.id.imgCamera)
        val imgGallery: ImageView = findViewById(R.id.imgGallery)
        val imgSave: ImageView = findViewById(R.id.imgSave)
        val imgClose: ImageView = findViewById(R.id.imgClose)

        mPhotoEditorView = findViewById(R.id.photoEditorView)
        mTxtCurrentTool = findViewById(R.id.txtCurrentTool)
        mRvTools = findViewById(R.id.rvConstraintTools)
        mRvFilters = findViewById(R.id.rvFilterView)
        mRootView = findViewById(R.id.rootView)

        imgUndo.setOnClickListener(this)
        imgRedo.setOnClickListener(this)
        imgCamera.setOnClickListener(this)
        imgGallery.setOnClickListener(this)
        imgSave.setOnClickListener(this)
        imgClose.setOnClickListener(this)
        mPhotoEditorView!!.source.setImageURI(Uri.parse(imageUri))
    }

    override fun onEditTextChangeListener(rootView: View, text: String, colorCode: Int) {
        val textEditorDialogFragment = TextEditorDialogFragment.show(this, text, colorCode)
        textEditorDialogFragment.setOnTextEditorListener(object : TextEditorDialogFragment.TextEditor {
            override fun onDone(inputText: String, colorCode: Int) {
                mPhotoEditor!!.editText(rootView, inputText, colorCode);
                mTxtCurrentTool!!.setText(R.string.label_text);
            }
        })
    }

    override fun onAddViewListener(viewType: ViewType, numberOfAddedViews: Int) {}
    override fun onRemoveViewListener(numberOfAddedViews: Int) {}
    override fun onRemoveViewListener(viewType: ViewType, numberOfAddedViews: Int) {}
    override fun onStartViewChangeListener(viewType: ViewType) {}
    override fun onStopViewChangeListener(viewType: ViewType) {}

    override fun onClick(view: View) {
        when (view.id) {
            R.id.imgUndo -> mPhotoEditor!!.undo()

            R.id.imgRedo -> mPhotoEditor!!.redo()

            R.id.imgSave -> shareImage()

            R.id.imgClose -> onBackPressed()

            R.id.imgCamera -> {
                val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                startActivityForResult(cameraIntent, CAMERA_REQUEST)
            }

            R.id.imgGallery -> {
                val intent = Intent()
                intent.type = "image/*"
                intent.action = Intent.ACTION_GET_CONTENT
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_REQUEST)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK)
            when (requestCode) {
                CAMERA_REQUEST -> {
                    mPhotoEditor!!.clearAllViews()
                    val photo = data!!.extras.get("data") as Bitmap
                    mPhotoEditorView!!.source.setImageBitmap(photo)
                }
                PICK_REQUEST -> try {
                    mPhotoEditor!!.clearAllViews()
                    val uri = data!!.data
                    val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)
                    mPhotoEditorView!!.source.setImageBitmap(bitmap)
                } catch (e: IOException) {
                    e.printStackTrace()
                }

            }
    }

    override fun onColorChanged(colorCode: Int) {
        mPhotoEditor!!.brushColor = colorCode
        mTxtCurrentTool!!.setText(R.string.label_brush)
    }

    override fun onOpacityChanged(opacity: Int) {
        mPhotoEditor!!.setOpacity(opacity)
        mTxtCurrentTool!!.setText(R.string.label_brush)
    }

    override fun onBrushSizeChanged(brushSize: Int) {
        mPhotoEditor!!.brushSize = brushSize.toFloat()
        mTxtCurrentTool!!.setText(R.string.label_brush)
    }

    override fun onEmojiClick(emojiUnicode: String) {
        mPhotoEditor!!.addEmoji(emojiUnicode)
        mTxtCurrentTool!!.setText(R.string.label_emoji)

    }

    override fun onStickerClick(bitmap: Bitmap) {
        mPhotoEditor!!.addImage(bitmap)
        mTxtCurrentTool!!.setText(R.string.label_sticker)
    }

    override fun isPermissionGranted(isGranted: Boolean, permission: String) {

    }

    private fun showSaveDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setMessage("이미지 전송을 취소하시겠습니까?")
        builder.setPositiveButton("네") { _, _ -> shareImage() }
        builder.setNegativeButton("아니요") { dialog, _ -> dialog.dismiss() }
        builder.create().show()
    }

    @SuppressLint("MissingPermission")
    private fun shareImage() {
        if (requestPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

            val file = File(
                Environment.getExternalStorageDirectory().absolutePath
                        + "/" + "shareImage.png"
            )

            val pDialog = SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE)
            pDialog.progressHelper.barColor = Color.parseColor("#A5DC86")
            pDialog.setTitle("사진 추출중...")
            pDialog.setCancelable(false)
            pDialog.show()

            try {
                file.createNewFile()

                val saveSettings = SaveSettings.Builder()
                    .setClearViewsEnabled(true)
                    .setTransparencyEnabled(true)
                    .build()

                mPhotoEditor!!.saveAsFile(file.absolutePath, saveSettings, object : PhotoEditor.OnSaveListener {
                    override fun onSuccess(imagePath: String) {
                        mPhotoEditorView!!.source.setImageURI(Uri.fromFile(File(imagePath)))
                        val storage = FirebaseStorage.getInstance()
                        val storageRef = storage.reference
                            .child("Chatting_Image/$myUid/$imageUid")
                        storageRef.putFile(Uri.fromFile(file)).addOnFailureListener { exception ->
                            pDialog.dismissWithAnimation()
                            FancyToast.makeText(
                                applicationContext,
                                "사진 업로드중에 문제가 발생하였습니다.\nError: " + exception.cause,
                                FancyToast.LENGTH_SHORT,
                                FancyToast.ERROR, false
                            ).show()
                            file.delete()
                        }.addOnSuccessListener { taskSnapshot ->
                            pDialog.dismissWithAnimation()
                            FancyToast.makeText(
                                applicationContext,
                                "사진이 업로드 되었습니다.",
                                FancyToast.LENGTH_SHORT,
                                FancyToast.SUCCESS, false
                            ).show()
                            val chatData = ChattingItem(
                                myName,
                                time,
                                imageName,
                                "image",
                                profileImageUri,
                                imageUid,
                                myUid
                            )
                            FirebaseDatabase.getInstance().reference.child("ChatDB")
                                .child(roomUid!!).child("chat").push().setValue(chatData)
                            file.delete()
                            finish()
                        }.addOnProgressListener { it ->
                            val progress = 100 * it.bytesTransferred / it.totalByteCount.toInt()
                            pDialog.setTitle("사진 업로드중... ($progress/100)")
                        }
                    }

                    override fun onFailure(exception: Exception) {
                        FancyToast.makeText(
                            applicationContext,
                            "사진 작업중 문제가 발생하였습니다.\nError: " + exception.cause,
                            FancyToast.LENGTH_SHORT,
                            FancyToast.ERROR, false
                        ).show()
                    }
                })
            } catch (e: IOException) {
                FancyToast.makeText(
                    applicationContext,
                    "사진 추출중에 문제가 발생하였습니다.\nError: " + e.cause,
                    FancyToast.LENGTH_SHORT,
                    FancyToast.ERROR, false
                ).show()
            }

        }
    }

    override fun onFilterSelected(photoFilter: PhotoFilter) {
        mPhotoEditor!!.setFilterEffect(photoFilter)
    }

    override fun onToolSelected(toolType: ToolType) {
        when (toolType) {
            ToolType.BRUSH -> {
                mPhotoEditor!!.setBrushDrawingMode(true)
                mTxtCurrentTool!!.setText(R.string.label_brush)
                mPropertiesBSFragment!!.show(supportFragmentManager, mPropertiesBSFragment!!.tag)
            }
            ToolType.TEXT -> {
                val textEditorDialogFragment = TextEditorDialogFragment.show(this)
                textEditorDialogFragment.setOnTextEditorListener(object : TextEditorDialogFragment.TextEditor {
                    override fun onDone(inputText: String, colorCode: Int) {
                        mPhotoEditor!!.addText(inputText, colorCode);
                        mTxtCurrentTool!!.setText(R.string.label_text);
                    }
                })
            }
            ToolType.ERASER -> {
                mPhotoEditor!!.brushEraser()
                mTxtCurrentTool!!.setText(R.string.label_eraser)
            }
            ToolType.FILTER -> {
                mTxtCurrentTool!!.setText(R.string.label_filter)
                showFilter(true)
            }
            ToolType.EMOJI -> mEmojiBSFragment!!.show(supportFragmentManager, mEmojiBSFragment!!.tag)
            ToolType.STICKER -> mStickerBSFragment!!.show(supportFragmentManager, mStickerBSFragment!!.tag)
        }
    }

    internal fun showFilter(isVisible: Boolean) {
        mIsFilterVisible = isVisible
        mConstraintSet.clone(mRootView!!)

        if (isVisible) {
            mConstraintSet.clear(mRvFilters!!.id, ConstraintSet.START)
            mConstraintSet.connect(
                mRvFilters!!.id, ConstraintSet.START,
                ConstraintSet.PARENT_ID, ConstraintSet.START
            )
            mConstraintSet.connect(
                mRvFilters!!.id, ConstraintSet.END,
                ConstraintSet.PARENT_ID, ConstraintSet.END
            )
        } else {
            mConstraintSet.connect(
                mRvFilters!!.id, ConstraintSet.START,
                ConstraintSet.PARENT_ID, ConstraintSet.END
            )
            mConstraintSet.clear(mRvFilters!!.id, ConstraintSet.END)
        }

        val changeBounds = ChangeBounds()
        changeBounds.duration = 350
        changeBounds.interpolator = AnticipateOvershootInterpolator(1.0f)
        TransitionManager.beginDelayedTransition(mRootView, changeBounds)

        mConstraintSet.applyTo(mRootView!!)
    }

    override fun onBackPressed() {
        if (mIsFilterVisible) {
            showFilter(false)
            mTxtCurrentTool!!.text = "사진 편집"
        } else if (!mPhotoEditor!!.isCacheEmpty) {
            showSaveDialog()
        } else {
            super.onBackPressed()
        }
    }

    companion object {
        private const val CAMERA_REQUEST = 52
        private const val PICK_REQUEST = 53
    }
}
