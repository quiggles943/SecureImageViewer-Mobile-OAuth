package com.quigglesproductions.secureimageviewer.ui.enhancedfolderlist

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.PorterDuff
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.quigglesproductions.secureimageviewer.R
import com.quigglesproductions.secureimageviewer.dagger.hilt.annotations.DownloadDatabase
import com.quigglesproductions.secureimageviewer.datasource.folder.IFolderDataSource.FolderDataSourceCallback
import com.quigglesproductions.secureimageviewer.recycler.RecyclerViewSelectionMode
import com.quigglesproductions.secureimageviewer.room.databases.unified.UnifiedFileDatabase
import com.quigglesproductions.secureimageviewer.room.databases.unified.entity.RoomUnifiedFolder
import dagger.hilt.android.qualifiers.ActivityContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.net.MalformedURLException
import javax.inject.Inject

class FolderListAdapter @Inject constructor(@ActivityContext context: Context,@DownloadDatabase val database: UnifiedFileDatabase) : PagingDataAdapter<RoomUnifiedFolder, ViewHolder>(
    FolderDiffCallBack()
) {
    private var mContext : Context = context
    private lateinit var onClickListener: FolderListOnClickListener
    private val selected = ArrayList<Int>()
    private var multiSelect = false
    private var selectionModeChangeListener: SelectionChangedListener? =
        null
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val folder: RoomUnifiedFolder? = getItem(position)

        try {
            val dataSource : Any? =
            runBlocking {
                    folder?.dataSource
                        ?.getThumbnailFromDataSourceSuspend(mContext,database)
            }
            Glide.with(viewHolder.itemView.context)
                .load(dataSource).error(R.drawable.ic_broken_image)
                .fitCenter().into(viewHolder.imageView)

        } catch (ex: MalformedURLException) {
            ex.printStackTrace()
        }
        if (getIsSelected(position)) viewHolder.imageView.setColorFilter(
            ContextCompat.getColor(
                mContext!!, R.color.selected
            ), PorterDuff.Mode.SRC_ATOP
        ) else viewHolder.imageView.colorFilter = null
        if (folder!!.hasUpdates()) {
            viewHolder.syncView.setVisibility(View.VISIBLE)
        } else viewHolder.syncView.setVisibility(View.GONE)
        viewHolder.folderNameView.text = folder!!.name
        viewHolder.itemView.setOnClickListener {
            if (onClickListener != null) onClickListener.onClick(
                viewHolder.adapterPosition
            )
        }
        viewHolder.itemView.setOnLongClickListener {
            if (onClickListener != null) onClickListener.onLongClick(viewHolder.adapterPosition)
            true
        }
    }

    fun setMultiselect(isMultiselect: Boolean){
        multiSelect = isMultiselect;
        if (selectionModeChangeListener != null) {
            var mode:RecyclerViewSelectionMode
            if(multiSelect)
                mode = RecyclerViewSelectionMode.MULTI;
            else {
                mode = RecyclerViewSelectionMode.SINGLE
                val listToRemove = ArrayList(selected)
                for(position in listToRemove){
                    removeFromSelected(position)
                }
            }
            selectionModeChangeListener!!.selectionModeChanged(mode)
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.foldergrid_layout_constrained_tile, parent, false)
        return ViewHolder(
            view
        )
    }

    fun setOnClickListener(onClickListener: FolderListOnClickListener) {
        this.onClickListener = onClickListener
    }
    fun isMultiSelect():Boolean{
        return multiSelect
    }
    fun setOnSelectionModeChangeListener(selectionModeChangeListener: SelectionChangedListener) {
        this.selectionModeChangeListener = selectionModeChangeListener
    }
    fun addToSelected(position: Int) {
        selected.add(position)
        notifyItemChanged(position)
        if (selectionModeChangeListener != null) selectionModeChangeListener!!.selectionAdded(
            position
        )
    }

    fun removeFromSelected(position: Int) {
        selected.removeAt(selected.indexOf(position))
        notifyItemChanged(position)
        if (selectionModeChangeListener != null) selectionModeChangeListener!!.selectionRemoved(
            position
        )
    }

    fun getSelectedFolders() : List<RoomUnifiedFolder>{
        val selectedFolders: ArrayList<RoomUnifiedFolder> = ArrayList()
        for(folderId: Int in selected){
            val folder: RoomUnifiedFolder? = peek(folderId)
            if(folder != null)
                selectedFolders.add(folder)
        }
        return selectedFolders
    }
    fun getIsSelected(position: Int): Boolean {
        return selected.contains(position)
    }
    fun getSelectedCount(): Int {
        return selected.size
    }
}


class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    val imageView: ImageView
    val folderNameView: TextView
    val syncView: ImageView

    init {
        imageView = view.findViewById<View>(R.id.grid_item_image) as ImageView
        folderNameView = view.findViewById(R.id.grid_item_label)
        syncView = view.findViewById(R.id.sync_icon)
    }
}


class FolderDiffCallBack : DiffUtil.ItemCallback<RoomUnifiedFolder>() {
    override fun areItemsTheSame(oldItem: RoomUnifiedFolder, newItem: RoomUnifiedFolder): Boolean {
        return oldItem.onlineId == newItem.onlineId
    }

    override fun areContentsTheSame(oldItem: RoomUnifiedFolder, newItem: RoomUnifiedFolder): Boolean {
        return oldItem.onlineId == newItem.onlineId
    }
}


interface FolderListOnClickListener {
    fun onClick(position: Int)
    fun onLongClick(position: Int)
}


interface SelectionChangedListener {
    fun selectionModeChanged(selectionMode: RecyclerViewSelectionMode?)
    fun selectionAdded(position: Int)
    fun selectionRemoved(position: Int)
}


