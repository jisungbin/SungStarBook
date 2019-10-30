package com.sungbin.sungstarbook.view.activity

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_error_activty.*

import kotlinx.android.synthetic.main.content_error_activty.*
import android.content.Intent
import com.sungbin.sungstarbook.utils.Utils


@Suppress("DEPRECATION")
class ErrorActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.sungbin.sungstarbook.R.layout.activity_error_activty)
        toolbar.title = ""
        setSupportActionBar(toolbar)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            with(window) {
                decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                statusBarColor = Color.WHITE
                navigationBarColor = Color.WHITE
            }
        }

        error_show.text = intent.getStringExtra("error")
    }

}
