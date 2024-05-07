package com.quigglesproductions.secureimageviewer.ui.enhancedfolderlist

import android.content.Context
import android.graphics.PorterDuff
import android.opengl.Visibility
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.signature.ObjectKey
import com.quigglesproductions.secureimageviewer.R
import com.quigglesproductions.secureimageviewer.dagger.hilt.annotations.DownloadDatabase
import com.quigglesproductions.secureimageviewer.datasource.folder.IFolderDataSource
import com.quigglesproductions.secureimageviewer.glide.ChecksumSignature
import com.quigglesproductions.secureimageviewer.models.FileUpdateTracker
import com.quigglesproductions.secureimageviewer.models.enhanced.EnhancedFileUpdateResponse
import com.quigglesproductions.secureimageviewer.models.enhanced.folder.IDisplayFolder
import com.quigglesproductions.secureimageviewer.recycler.RecyclerViewSelectionMode
import com.quigglesproductions.secureimageviewer.room.databases.unified.UnifiedFileDatabase
import com.quigglesproductions.secureimageviewer.room.databases.unified.entity.RoomUnifiedFolder
import dagger.hilt.android.qualifiers.ActivityContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.net.MalformedURLException
import javax.inject.Inject

class FolderListAdapter @Inject constructor(@ActivityContext context: Context,@DownloadDatabase val database: UnifiedFileDatabase) : PagingDataAdapter<IDisplayFolder, ViewHolder>(
    FolderDiffCallBack()
) {
    private var mContext : Context = context
    private lateinit var onClickListener: FolderListOnClickListener
    private val selected = ArrayList<Int>()
    private var multiSelect = false
    private var fileUpdates = FileUpdateTracker()
    private var selectionModeChangeListener: SelectionChangedListener? =
        null

    private var offlineFolders: List<RoomUnifiedFolder> = emptyList()
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val options :RequestOptions = RequestOptions().error(R.drawable.ic_broken_image).skipMemoryCache(true)
        val folder: IDisplayFolder? = getItem(position)
        try {
            CoroutineScope(Dispatchers.IO).launch{
                val dataSource : Any? = folder?.dataSource?.getThumbnailFromDataSourceSuspend(mContext,database)
                withContext(Dispatchers.Main) {
                    Glide.with(viewHolder.itemView.context)
                        .setDefaultRequestOptions(options)
                .load(dataSource).signature(ChecksumSignature(folder?.thumbnailChecksum))
                      .fitCenter().into(viewHolder.getImageView())
                }
            }

        } catch (ex: MalformedURLException) {
            ex.printStackTrace()
        }
        viewHolder.setSelected(mContext,getIsSelected(position))
        if(folder?.sourceType == IFolderDataSource.FolderSourceType.ONLINE){
            if(!folder.isAvailableOfflineSet) {
                runBlocking {
                    if (database.folderDao().loadFolderByOnlineId(folder.onlineId) != null) {
                        folder.isAvailableOffline = true
                    }
                }
            }
            if(folder.isAvailableOffline){
                viewHolder.setDownloadedIconVisible(true)
            }
            else {
                viewHolder.setDownloadedIconVisible(false)
            }
        }
        else{
            if(fileUpdates.doesFolderHaveUpdates(folder!!.onlineId)){
                viewHolder.setSyncIconVisible(true)
            }
            else
                viewHolder.setSyncIconVisible(false)
        }
        //viewHolder.setSyncIconVisible(folder.hasUpdates())
        viewHolder.setFolderName(folder.name)
        viewHolder.itemView.setOnClickListener {
            if (onClickListener != null) onClickListener.onClick(
                viewHolder.adapterPosition
            )
        }
        viewHolder.itemView.setOnLongClickListener {
            if (onClickListener != null) onClickListener.onLongClick(viewHolder.adapterPosition)
            true
        }
        viewHolder.setEnabled(mContext,folder.isAvailable)
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

    fun getSelectedFolders() : List<IDisplayFolder>{
        val selectedFolders: ArrayList<IDisplayFolder> = ArrayList()
        for(folderId: Int in selected){
            val folder: IDisplayFolder? = peek(folderId)
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

    fun setOfflineFolders(offlineFolders: List<RoomUnifiedFolder>) {
        this.offlineFolders = offlineFolders
    }

    fun setFileUpdates(value: FileUpdateTracker?) {
        fileUpdates = value ?: FileUpdateTracker()
    }
}


class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    private val imageView: ImageView
    private val folderNameView: TextView
    private val syncView: ImageView
    private val progressBar: ProgressBar
    private val downloadView: ImageView

    init {
        itemView.setAllowClickWhenDisabled(false)
        imageView = view.findViewById<View>(R.id.grid_item_image) as ImageView
        folderNameView = view.findViewById(R.id.grid_item_label)
        syncView = view.findViewById(R.id.sync_icon)
        downloadView = view.findViewById(R.id.download_icon)
        progressBar = view.findViewById(R.id.grid_item_progressBar)
    }

    fun getImageView(): ImageView{
        return imageView
    }
    fun setSelected(mContext: Context, isEnabled: Boolean){
        if(isEnabled) {
            imageView.setColorFilter(
                ContextCompat.getColor(
                    mContext, R.color.selected
                ), PorterDuff.Mode.SRC_ATOP
            )
        }
        else
            imageView.colorFilter = null
    }
    fun setSyncIconDrawable(@DrawableRes id: Int){
        syncView.setImageResource(id)
    }
    fun setSyncIconVisible(value: Boolean){
        if (value) {
            setDownloadedIconVisible(false)
            syncView.setVisibility(View.VISIBLE)
        }else
            syncView.setVisibility(View.GONE)

    }

    fun setDownloadedIconVisible(value: Boolean){
        if (value) {
            setSyncIconVisible(false)
            downloadView.setVisibility(View.VISIBLE)
        }else
            downloadView.setVisibility(View.GONE)

    }
    fun setEnabled(mContext: Context, value: Boolean){
        if(value)
            enableView()
        else
            disableView(mContext)
    }
    private fun enableView(){
        itemView.isEnabled = true
        progressBar.visibility = View.VISIBLE
    }
    private fun disableView(mContext: Context){
        itemView.isEnabled = false

        imageView.setColorFilter(
            ContextCompat.getColor(
                mContext, R.color.progressBar_overlay_background
            ), PorterDuff.Mode.SRC_ATOP
        )
        progressBar.visibility = View.VISIBLE
    }

    fun setFolderName(name: String?) {
        folderNameView.text = name
    }
}


class FolderDiffCallBack : DiffUtil.ItemCallback<IDisplayFolder>() {
    override fun areItemsTheSame(oldItem: IDisplayFolder, newItem: IDisplayFolder): Boolean {
        var isSame = true
        if(oldItem.onlineId != newItem.onlineId)
            isSame = false
        if(oldItem.fileGroupingType != newItem.fileGroupingType)
            isSame = false
        return isSame
    }

    override fun areContentsTheSame(oldItem: IDisplayFolder, newItem: IDisplayFolder): Boolean {
        var isSame = true
        if(oldItem.isAvailable != newItem.isAvailable)
            isSame = false
        if(oldItem.fileGroupingType != newItem.fileGroupingType)
            isSame = false
        return isSame
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


