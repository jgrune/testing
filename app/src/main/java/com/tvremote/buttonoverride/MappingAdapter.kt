package com.tvremote.buttonoverride

import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

class MappingAdapter(
    private val onEdit: (ButtonMapping) -> Unit,
    private val onDelete: (ButtonMapping) -> Unit
) : ListAdapter<ButtonMapping, MappingAdapter.ViewHolder>(DIFF) {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val buttonName: TextView = view.findViewById(R.id.text_button_name)
        val keyCode: TextView = view.findViewById(R.id.text_keycode)
        val appName: TextView = view.findViewById(R.id.text_app_name)
        val btnEdit: View = view.findViewById(R.id.btn_edit)
        val btnDelete: View = view.findViewById(R.id.btn_delete)

        init {
            // Make the whole row and buttons focusable for TV navigation
            view.isFocusable = true
            view.isFocusableInTouchMode = true
            btnEdit.isFocusable = true
            btnDelete.isFocusable = true
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_button_mapping, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val mapping = getItem(position)
        holder.buttonName.text = mapping.buttonName
        holder.keyCode.text = "Keycode: ${mapping.keyCode} (${KeyEvent.keyCodeToString(mapping.keyCode)})"
        holder.appName.text = mapping.appName

        holder.btnEdit.setOnClickListener { onEdit(mapping) }
        holder.btnDelete.setOnClickListener { onDelete(mapping) }

        // Allow click on the whole row to edit
        holder.itemView.setOnClickListener { onEdit(mapping) }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<ButtonMapping>() {
            override fun areItemsTheSame(a: ButtonMapping, b: ButtonMapping) =
                a.keyCode == b.keyCode

            override fun areContentsTheSame(a: ButtonMapping, b: ButtonMapping) = a == b
        }
    }
}
