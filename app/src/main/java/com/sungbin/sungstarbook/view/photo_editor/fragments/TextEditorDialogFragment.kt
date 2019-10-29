package com.sungbin.sungstarbook.view.photo_editor.fragments

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sungbin.sungstarbook.R
import com.sungbin.sungstarbook.view.photo_editor.ColorPickerAdapter

import java.util.Objects


class TextEditorDialogFragment : DialogFragment() {
    private var mAddTextEditText: EditText? = null
    private var mInputMethodManager: InputMethodManager? = null
    private var mColorCode: Int = 0
    private var mTextEditor: TextEditor? = null

    interface TextEditor {
        fun onDone(inputText: String, colorCode: Int)
    }

    override fun onStart() {
        super.onStart()
        val dialog = dialog
        if (dialog != null) {
            val width = ViewGroup.LayoutParams.MATCH_PARENT
            val height = ViewGroup.LayoutParams.MATCH_PARENT
            Objects.requireNonNull<Window>(dialog.window).setLayout(width, height)
            dialog.window!!.setBackgroundDrawable(ColorDrawable(android.graphics.Color.TRANSPARENT))
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.add_text_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mAddTextEditText = view.findViewById(R.id.add_text_edit_text)
        mInputMethodManager =
            Objects.requireNonNull<FragmentActivity>(activity).getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
        val mAddTextDoneTextView = view.findViewById<TextView>(R.id.add_text_done_tv)

        val addTextColorPickerRecyclerView = view.findViewById<RecyclerView>(R.id.add_text_color_picker_recycler_view)
        val layoutManager = LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
        addTextColorPickerRecyclerView.layoutManager = layoutManager
        addTextColorPickerRecyclerView.setHasFixedSize(true)
        val colorPickerAdapter = ColorPickerAdapter(activity!!)

        colorPickerAdapter.setOnColorPickerClickListener(object : ColorPickerAdapter.OnColorPickerClickListener {
            override fun onColorPickerClickListener(colorCode: Int) {
                mColorCode = colorCode
                mAddTextEditText!!.setTextColor(colorCode)
            }
        })

        addTextColorPickerRecyclerView.adapter = colorPickerAdapter
        assert(arguments != null)
        mAddTextEditText!!.setText(arguments!!.getString(EXTRA_INPUT_TEXT))
        mColorCode = arguments!!.getInt(EXTRA_COLOR_CODE)
        mAddTextEditText!!.setTextColor(mColorCode)
        mInputMethodManager!!.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)

        mAddTextDoneTextView.setOnClickListener {
            mInputMethodManager!!.hideSoftInputFromWindow(it.windowToken, 0)
            dismiss()
            val inputText = mAddTextEditText!!.text.toString()
            if (!TextUtils.isEmpty(inputText) && mTextEditor != null) {
                mTextEditor!!.onDone(inputText, mColorCode)
            }
        }

    }

    fun setOnTextEditorListener(textEditor: TextEditor) {
        mTextEditor = textEditor
    }

    companion object {

        private val TAG = TextEditorDialogFragment::class.java.simpleName
        private const val EXTRA_INPUT_TEXT = "extra_input_text"
        private const val EXTRA_COLOR_CODE = "extra_color_code"

        @JvmOverloads
        fun show(
            appCompatActivity: AppCompatActivity,
            inputText: String = "",
            @ColorInt colorCode: Int = ContextCompat.getColor(appCompatActivity, R.color.white)
        ): TextEditorDialogFragment {
            val args = Bundle()
            args.putString(EXTRA_INPUT_TEXT, inputText)
            args.putInt(EXTRA_COLOR_CODE, colorCode)
            val fragment = TextEditorDialogFragment()
            fragment.arguments = args
            fragment.show(appCompatActivity.supportFragmentManager, TAG)
            return fragment
        }
    }
}