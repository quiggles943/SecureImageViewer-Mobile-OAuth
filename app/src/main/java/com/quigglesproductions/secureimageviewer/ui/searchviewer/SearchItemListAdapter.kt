package com.quigglesproductions.secureimageviewer.ui.searchviewer

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.quigglesproductions.secureimageviewer.R
import com.quigglesproductions.secureimageviewer.room.databases.unified.entity.RoomUnifiedSearchItem
import javax.inject.Inject

class SearchItemListAdapter @Inject constructor() : RecyclerView.Adapter<ViewHolder>(
) {
    private var onClickListener: ((Int) -> Unit?)? = null
    private var searchItems: ArrayList<RoomUnifiedSearchItem> = ArrayList()

    fun addItem(searchItem: RoomUnifiedSearchItem){
        searchItems.add(searchItem)
        notifyItemInserted(searchItems.indexOf(searchItem))
    }
    fun removeItem(searchItem: RoomUnifiedSearchItem)
    {
        val index = searchItems.indexOf(searchItem)
        removeItem(index)
    }

    fun removeItem(position: Int){
        searchItems.removeAt(position)
        notifyItemRemoved(position)
    }
    fun getItem(position: Int):RoomUnifiedSearchItem{
        return searchItems[position]
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val searchItem: RoomUnifiedSearchItem = getItem(position)
        viewHolder.getText().text = searchItem.name
        viewHolder.getRemoveTagBtn().setOnClickListener {
            onClickListener?.invoke(viewHolder.absoluteAdapterPosition)
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.searchitem_layout_grid_item, parent, false)
        return ViewHolder(
            view
        )
    }

    override fun getItemCount(): Int {
        return searchItems.size
    }

    fun setOnRemoveTagClickListener(callback: (position: Int) -> Unit) {
        this.onClickListener = callback
    }

    fun getItems(): List<RoomUnifiedSearchItem> {
        return searchItems

    }

    fun updateItems(it: List<RoomUnifiedSearchItem>) {
        searchItems = ArrayList(it)
        notifyDataSetChanged()
    }

    fun clearItems() {
        searchItems.clear()
        notifyDataSetChanged()
    }
}


class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    private val tagName: TextView
    private val removeTagBtn: ImageButton

    init {
        itemView.setAllowClickWhenDisabled(false)
        tagName = view.findViewById<View>(R.id.searchitem_text) as TextView
        removeTagBtn = view.findViewById(R.id.searchitem_button)
    }

    fun getText():TextView{
        return tagName
    }
    fun getRemoveTagBtn():ImageButton{
        return removeTagBtn
    }
}


