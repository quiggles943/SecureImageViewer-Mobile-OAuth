package com.quigglesproductions.secureimageviewer.ui.enhancedfolderviewer.kotlin

import android.content.Context
import android.util.Log
import android.view.ContextMenu
import android.view.ContextMenu.ContextMenuInfo
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnCreateContextMenuListener
import android.view.View.OnLongClickListener
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ImageView
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.quigglesproductions.secureimageviewer.R
import com.quigglesproductions.secureimageviewer.aurora.appauth.AuroraAuthenticationManager
import com.quigglesproductions.secureimageviewer.datasource.file.IFileDataSource.DataSourceCallback
import com.quigglesproductions.secureimageviewer.datasource.file.RetrofitFileDataSource
import com.quigglesproductions.secureimageviewer.glide.CustomGlideModule
import com.quigglesproductions.secureimageviewer.models.enhanced.file.IDisplayFile
import com.quigglesproductions.secureimageviewer.models.enhanced.folder.IDisplayFolder
import com.quigglesproductions.secureimageviewer.room.databases.paging.file.entity.relations.RoomEmbeddedFile
import com.quigglesproductions.secureimageviewer.ui.enhancedfolderviewer.EnhancedFileGridRecyclerAdapter.EnhancedRecyclerViewOnClickListener
import dagger.hilt.android.qualifiers.ActivityContext
import org.acra.ACRA.errorReporter
import java.net.MalformedURLException
import javax.inject.Inject

class FolderFilesListAdapter @Inject constructor(@ActivityContext context: Context?, authenticationManager: AuroraAuthenticationManager) : PagingDataAdapter<RoomEmbeddedFile, ViewHolder>(FileDiffCallBack()) {
    var mContext : Context? = context
    var authenticationManager : AuroraAuthenticationManager = authenticationManager
    private lateinit var onClickListener: FolderFilesListOnClickListener
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val file: IDisplayFile? = getItem(position)
        viewHolder.imageView
            .setOnClickListener(View.OnClickListener { onClickListener.onClick(viewHolder.absoluteAdapterPosition) })
        viewHolder.imageView.setOnLongClickListener(OnLongClickListener { false })
        viewHolder.imageView
            .setOnCreateContextMenuListener(OnCreateContextMenuListener { menu, v, menuInfo ->
                var menuInfo = menuInfo
                menuInfo = AdapterView.AdapterContextMenuInfo(
                    viewHolder.itemView,
                    viewHolder.absoluteAdapterPosition,
                    0
                )
                onClickListener.onCreateContextMenu(menu, v, menuInfo)
            })
        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        try {
            if(file != null) {
                file.dataSource = RetrofitFileDataSource(file, authenticationManager)
                file.dataSource.getFileThumbnailDataSource(mContext, object : DataSourceCallback {
                    override fun FileDataSourceRetrieved(dataSource: Any?, exception: Exception?) {}
                    override fun FileThumbnailDataSourceRetrieved(
                        dataSource: Any?,
                        exception: Exception?
                    ) {
                        mContext?.let {
                            Glide.with(it)
                                .addDefaultRequestListener(object : RequestListener<Any?> {
                                    override fun onLoadFailed(
                                        e: GlideException?,
                                        model: Any?,
                                        target: Target<Any?>,
                                        isFirstResource: Boolean
                                    ): Boolean {
                                        Log.e("Image Load Fail", e!!.message!!)
                                        e.logRootCauses("Image Load Fail")
                                        return false
                                    }

                                    override fun onResourceReady(
                                        resource: Any,
                                        model: Any,
                                        target: Target<Any?>?,
                                        dataSource: DataSource,
                                        isFirstResource: Boolean
                                    ): Boolean {
                                        return false
                                    }
                                }).load(dataSource).into(viewHolder.imageView).clearOnDetach()
                        }
                    }

                    override fun FileRetrievalDataSourceRetrieved(
                        fileDataSource: Any?,
                        fileThumbnailDataSource: Any?,
                        exception: Exception?
                    ) {
                    }
                })
            }
        } catch (e: MalformedURLException) {
            errorReporter.handleSilentException(e)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.filegrid_layout_constrained, parent, false)
        return ViewHolder(
            view
        )
    }

    fun setOnClickListener(onClickListener: FolderFilesListOnClickListener) {
        this.onClickListener = onClickListener
    }
}


class ViewHolder(view: View) : RecyclerView.ViewHolder(view),
    OnCreateContextMenuListener {
    val imageView: ImageView

    init {
        // Define click listener for the ViewHolder's View
        imageView = view.findViewById<View>(R.id.grid_item_image) as ImageView
        view.setOnCreateContextMenuListener(this)
    }

    override fun onCreateContextMenu(menu: ContextMenu?, v: View?, menuInfo: ContextMenuInfo?) {}

    }

class FileDiffCallBack : DiffUtil.ItemCallback<RoomEmbeddedFile>() {
    override fun areItemsTheSame(oldItem: RoomEmbeddedFile, newItem: RoomEmbeddedFile): Boolean {
        return oldItem.onlineId == newItem.onlineId
    }

    override fun areContentsTheSame(oldItem: RoomEmbeddedFile, newItem: RoomEmbeddedFile): Boolean {
        return oldItem.onlineId == newItem.onlineId
    }
}


interface FolderFilesListOnClickListener {
    fun onClick(position: Int)
    fun onCreateContextMenu(menu: ContextMenu?, v: View?, menuInfo: ContextMenuInfo?)
}

