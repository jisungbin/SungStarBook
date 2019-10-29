@file:Suppress("DEPRECATION")

package com.sungbin.sungstarbook.view.photo_editor.fragments

import android.annotation.SuppressLint
import android.app.Dialog
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.sungbin.sungstarbook.R

class StickerBSFragment : BottomSheetDialogFragment() {

    private var mStickerListener: StickerListener? = null

    private val mBottomSheetBehaviorCallback = object : BottomSheetBehavior.BottomSheetCallback() {

        override fun onStateChanged(bottomSheet: View, newState: Int) {
            if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                dismiss()
            }

        }

        override fun onSlide(bottomSheet: View, slideOffset: Float) {}
    }

    fun setStickerListener(stickerListener: StickerListener) {
        mStickerListener = stickerListener
    }

    interface StickerListener {
        fun onStickerClick(bitmap: Bitmap)
    }


    @SuppressLint("RestrictedApi")
    override fun setupDialog(dialog: Dialog, style: Int) {
        super.setupDialog(dialog, style)
        val contentView = View.inflate(context, R.layout.fragment_bottom_sticker_emoji_dialog, null)
        dialog.setContentView(contentView)
        val params = (contentView.parent as View).layoutParams as CoordinatorLayout.LayoutParams
        val behavior = params.behavior

        if (behavior is BottomSheetBehavior<*>) {
            behavior.setBottomSheetCallback(mBottomSheetBehaviorCallback)
        }
        (contentView.parent as View).setBackgroundColor(resources.getColor(android.R.color.transparent))
        val rvEmoji = contentView.findViewById<RecyclerView>(R.id.rvEmoji)

        val gridLayoutManager = GridLayoutManager(activity, 3)
        rvEmoji.layoutManager = gridLayoutManager
        val stickerAdapter = StickerAdapter()
        rvEmoji.adapter = stickerAdapter
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }

    inner class StickerAdapter : RecyclerView.Adapter<StickerAdapter.ViewHolder>() {

        internal var stickerList = intArrayOf(R.drawable.aa, R.drawable.bb)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.row_sticker, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.imgSticker.setImageResource(stickerList[position])
        }

        override fun getItemCount(): Int {
            return stickerList.size
        }

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            var imgSticker: ImageView = itemView.findViewById(R.id.imgSticker)

            init {

                itemView.setOnClickListener {
                    if (mStickerListener != null) {
                        mStickerListener!!.onStickerClick(
                            BitmapFactory.decodeResource(
                                resources,
                                stickerList[layoutPosition]
                            )
                        )
                    }
                    dismiss()
                }
            }
        }
    }

    private fun convertEmoji(emoji: String): String {
        var returnedEmoji = ""
        try {
            val convertEmojiToInt = Integer.parseInt(emoji.substring(2), 16)
            returnedEmoji = getEmojiByUnicode(convertEmojiToInt)
        } catch (e: NumberFormatException) {
            returnedEmoji = ""
        }

        return returnedEmoji
    }

    private fun getEmojiByUnicode(unicode: Int): String {
        return String(Character.toChars(unicode))
    }
}