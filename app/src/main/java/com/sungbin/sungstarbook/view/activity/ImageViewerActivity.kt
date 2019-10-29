package com.sungbin.sungstarbook.view.activity

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.ImageView
import androidx.annotation.Nullable
import androidx.appcompat.app.AppCompatActivity
import android.graphics.BitmapFactory
import android.os.Environment
import android.view.*
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.sungbin.sungstarbook.R
import java.io.File
import kotlin.math.hypot


@Suppress("DEPRECATION")
@SuppressLint("ClickableViewAccessibility")
class ImageViewerActivity : AppCompatActivity() {

    private var xCoOrdinate: Float = 0.toFloat()
    private var yCoOrdinate: Float = 0.toFloat()
    private var screenCenterX: Double = 0.toDouble()
    private var screenCenterY: Double = 0.toDouble()
    private var alpha: Int = 0
    private lateinit var imageView: ImageView
    internal lateinit var view: View

    private val statusBarHeight: Int
        get() {
            var result = 0
            val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
            if (resourceId > 0) {
                result = resources.getDimensionPixelSize(resourceId)
            }
            return result
        }


    override fun onCreate(@Nullable savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN)

        setContentView(R.layout.activity_image_viewer)

        imageView = findViewById(R.id.imageView)
        imageView.transitionName = intent.getStringExtra("tag")

        view = findViewById(R.id.layout)
        view.background.alpha = 255

        val contentUid = intent.getStringExtra("image")
        val contentFile = File(Environment.getExternalStorageDirectory().absolutePath +
                "/SungStarBook/Image/${contentUid}.gif")

        Glide.with(applicationContext).load(contentFile)
            .format(DecodeFormat.PREFER_ARGB_8888)
            .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
            .into(imageView)

        val display = resources.displayMetrics
        screenCenterX = (display.widthPixels / 2).toDouble()
        screenCenterY = ((display.heightPixels - statusBarHeight) / 2).toDouble()
        val maxHypo = hypot(screenCenterX, screenCenterY)

        imageView.setOnTouchListener(View.OnTouchListener { _, event ->
            val centerYPos = (imageView.y + imageView.height / 2).toDouble()
            val centerXPos = (imageView.x + imageView.width / 2).toDouble()
            val a = screenCenterX - centerXPos
            val b = screenCenterY - centerYPos
            val hypo = hypot(a, b)
            alpha = (hypo * 255).toInt() / maxHypo.toInt()
            if (alpha < 255)
                view.background.alpha = 255 - alpha

            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    xCoOrdinate = imageView.x - event.rawX
                    yCoOrdinate = imageView.y - event.rawY
                }
                MotionEvent.ACTION_MOVE -> imageView.animate().x(event.rawX + xCoOrdinate).y(event.rawY + yCoOrdinate).setDuration(
                    0
                ).start()
                MotionEvent.ACTION_UP -> {
                    if (alpha > 70) {
                        supportFinishAfterTransition()
                        return@OnTouchListener false
                    } else {
                        imageView.animate().x(0f).y(screenCenterY.toFloat() - imageView.height / 2).setDuration(100)
                            .start()
                        view.background.alpha = 255
                    }
                    return@OnTouchListener false
                }
                else -> return@OnTouchListener false
            }
            true
        })
    }

}