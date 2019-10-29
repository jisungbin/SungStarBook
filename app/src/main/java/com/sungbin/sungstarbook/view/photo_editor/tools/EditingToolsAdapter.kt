package com.sungbin.sungstarbook.view.photo_editor.tools


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.sungbin.sungstarbook.R
import java.util.ArrayList

class EditingToolsAdapter(private val mOnItemSelected: OnItemSelected) :
    RecyclerView.Adapter<EditingToolsAdapter.ViewHolder>() {

    private val mToolList = ArrayList<ToolModel>()

    init {
        mToolList.add(ToolModel("연필", R.drawable.ic_brush, ToolType.BRUSH))
        mToolList.add(ToolModel("텍스트", R.drawable.ic_text, ToolType.TEXT))
        mToolList.add(ToolModel("지우개", R.drawable.ic_eraser, ToolType.ERASER))
        mToolList.add(ToolModel("필터", R.drawable.ic_photo_filter, ToolType.FILTER))
        mToolList.add(ToolModel("이모티콘", R.drawable.ic_insert_emoticon, ToolType.EMOJI))
        mToolList.add(ToolModel("스티커", R.drawable.ic_sticker, ToolType.STICKER))
    }

    interface OnItemSelected {
        fun onToolSelected(toolType: ToolType)
    }

    internal inner class ToolModel(
        val mToolName: String,
        val mToolIcon: Int,
        val mToolType: ToolType
    )

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.row_editing_tools, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = mToolList[position]
        holder.txtTool.text = item.mToolName
        holder.imgToolIcon.setImageResource(item.mToolIcon)
    }

    override fun getItemCount(): Int {
        return mToolList.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var imgToolIcon: ImageView = itemView.findViewById(R.id.imgToolIcon)
        var txtTool: TextView = itemView.findViewById(R.id.txtTool)
        init {
            itemView.setOnClickListener {
                mOnItemSelected.onToolSelected(mToolList[layoutPosition].mToolType)
            }
        }
    }
}
