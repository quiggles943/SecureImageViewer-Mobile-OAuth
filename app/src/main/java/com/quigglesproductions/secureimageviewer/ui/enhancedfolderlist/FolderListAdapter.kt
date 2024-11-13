package com.quigglesproductions.secureimageviewer.ui.enhancedfolderlist

import android.content.Context
import android.graphics.Bitmap
import android.graphics.PorterDuff
import android.opengl.Visibility
import android.util.Log
import android.view.ContextMenu
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadType
import androidx.paging.PagingDataAdapter
import androidx.paging.RemoteMediator
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.room.withTransaction
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
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
import com.quigglesproductions.secureimageviewer.ui.adapter.filelist.EnhancedFolderFilesListOnClickListener
import com.quigglesproductions.secureimageviewer.ui.adapter.loadstate.MyLoadStateAdapter
import dagger.hilt.android.qualifiers.ActivityContext
import jp.wasabeef.glide.transformations.BitmapTransformation
import jp.wasabeef.glide.transformations.BlurTransformation
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
    //private lateinit var onClickListener: FolderListOnClickListener
    private lateinit var onClickListener: (position:Int) -> Unit
    private lateinit var onCreateContextMenu: (menu: ContextMenu?, v: View?, menuInfo: ContextMenu.ContextMenuInfo?) -> Unit
    private val selected = ArrayList<Int>()
    private var fileUpdates = FileUpdateTracker()
    private var selectionModeChangeListener: SelectionChangedListener? =
        null

    private var offlineFolders: List<RoomUnifiedFolder> = emptyList()

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val options :RequestOptions = RequestOptions().error(R.drawable.ic_broken_image).skipMemoryCache(true)
        val folder: IDisplayFolder? = getItem(position)
        if(folder != null) {
            try {
                CoroutineScope(Dispatchers.IO).launch {
                    val dataSource: Any? =
                        folder.dataSource?.getThumbnailFromDataSourceSuspend(mContext, database)
                    //val transform = BlurTransformation(25,5)
                    withContext(Dispatchers.Main) {
                        Glide.with(viewHolder.itemView.context)
                            .setDefaultRequestOptions(options)
                            .load(dataSource)
                            .signature(ChecksumSignature(folder.thumbnailChecksum))
                            .thumbnail(0.10f)
                            .dontTransform()
                            .fitCenter()
                            .into(viewHolder.getImageView())
                    }
                }

            } catch (ex: MalformedURLException) {
                ex.printStackTrace()
            }
            //viewHolder.setSelected(mContext, getIsSelected(position))
            if (folder.sourceType == IFolderDataSource.FolderSourceType.ONLINE) {
                /*if (!folder.isAvailableOfflineSet) {
                    runBlocking {
                        if (database.folderDao().loadFolderByOnlineId(folder.onlineId) != null) {
                            folder.isAvailableOffline = true
                        }
                    }
                }
                if (folder.isAvailableOffline) {
                    viewHolder.setDownloadedIconVisible(true)
                } else {
                    viewHolder.setDownloadedIconVisible(false)
                }*/
            } else {
                if (fileUpdates.doesFolderHaveUpdates(folder.onlineId)) {
                    viewHolder.setSyncIconVisible(true)
                } else
                    viewHolder.setSyncIconVisible(false)
            }
            //viewHolder.setSyncIconVisible(folder.hasUpdates())
            viewHolder.setFolderName(folder.name)
            viewHolder.itemView.setOnClickListener {
                onClickListener.invoke(
                    viewHolder.absoluteAdapterPosition
                )
            }
            viewHolder.itemView.setOnCreateContextMenuListener { menu, v, menuInfo ->
                var menuInfo = menuInfo
                menuInfo = AdapterView.AdapterContextMenuInfo(
                    viewHolder.itemView,
                    viewHolder.absoluteAdapterPosition,
                    0
                )
                onCreateContextMenu.invoke(menu, v, menuInfo)
            }
            /*viewHolder.itemView.setOnLongClickListener {
            if (onClickListener != null) onClickListener.onLongClick(viewHolder.adapterPosition)
            true
        }*/
            viewHolder.setEnabled(mContext, folder.isAvailable)
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

    /*fun setOnClickListener(onClickListener: FolderListOnClickListener) {
        this.onClickListener = onClickListener
    }*/
    fun setOnSelectionModeChangeListener(selectionModeChangeListener: SelectionChangedListener) {
        this.selectionModeChangeListener = selectionModeChangeListener
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

    fun setOnClickListener(callback: (position: Int) -> Unit) {
        this.onClickListener = callback
    }
    fun setOnCreateOptionsMenuListener(callback: (menu: ContextMenu?, v: View?, menuInfo: ContextMenu.ContextMenuInfo?) -> Unit){
        this.onCreateContextMenu = callback
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
        if(oldItem.hasUpdates() != newItem.hasUpdates())
            isSame = false
        if(oldItem.isAvailableOffline != newItem.isAvailableOffline)
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


