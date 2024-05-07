package com.quigglesproductions.secureimageviewer.ui.adapter.filelist

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
import com.quigglesproductions.secureimageviewer.databinding.FilegridHeaderBinding
import com.quigglesproductions.secureimageviewer.databinding.FilegridLayoutConstrainedBinding
import com.quigglesproductions.secureimageviewer.datasource.file.IFileDataSource.DataSourceCallback
import com.quigglesproductions.secureimageviewer.glide.ChecksumSignature
import com.quigglesproductions.secureimageviewer.room.databases.unified.entity.relations.RoomUnifiedEmbeddedFile
import com.quigglesproductions.secureimageviewer.ui.adapter.itemmodel.folderfileviewer.FolderFileViewerModel
import dagger.hilt.android.qualifiers.ActivityContext
import javax.inject.Inject

class EnhancedFolderFilesListAdapter @Inject constructor(@ActivityContext context: Context?) : PagingDataAdapter<FolderFileViewerModel, RecyclerView.ViewHolder>(
    FileDiffCallBack()
) {
    private var mContext : Context? = context
    private lateinit var onClickListener: EnhancedFolderFilesListOnClickListener

    override fun getItemViewType(position: Int): Int {
        return when(peek(position)){
            is FolderFileViewerModel.FileModel -> R.layout.filegrid_layout_constrained
            is FolderFileViewerModel.HeaderModel -> R.layout.filegrid_header
            else -> R.layout.filegrid_layout_constrained
        }
    }
    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        when(viewHolder){
            is FileViewHolder ->{
                val file = getItem(position) as FolderFileViewerModel.FileModel?
                file.let { viewHolder.bind(it?.file,onClickListener) }
            }
            is HeaderViewHolder ->{
                val item = getItem(position) as FolderFileViewerModel.HeaderModel?
                item.let { viewHolder.bind(it?.title) }

            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when(viewType){
            R.layout.filegrid_layout_constrained -> FileViewHolder.from(mContext, parent)
            R.layout.filegrid_header -> HeaderViewHolder.from(mContext, parent)
            else -> throw ClassCastException("Unknown viewType ${viewType}")
        }
    }

    fun setOnClickListener(onClickListener: EnhancedFolderFilesListOnClickListener) {
        this.onClickListener = onClickListener
    }



class EnhancedFolderFileViewHolder(view: View) : RecyclerView.ViewHolder(view),
    OnCreateContextMenuListener {
    val imageView: ImageView

    init {
        // Define click listener for the ViewHolder's View
        imageView = view.findViewById<View>(R.id.grid_item_image) as ImageView
        view.setOnCreateContextMenuListener(this)
    }

    override fun onCreateContextMenu(menu: ContextMenu?, v: View?, menuInfo: ContextMenuInfo?) {}

    }

    class FileViewHolder private constructor(val context: Context?,val binding: FilegridLayoutConstrainedBinding): RecyclerView.ViewHolder(binding.root){
        fun bind(file: RoomUnifiedEmbeddedFile?,onClickListener: EnhancedFolderFilesListOnClickListener){
            file?.dataSource?.getFileThumbnailDataSource(context,object : DataSourceCallback {
                override fun FileDataSourceRetrieved(dataSource: Any?, exception: Exception?) {}
                override fun FileThumbnailDataSourceRetrieved(
                    dataSource: Any?,
                    exception: Exception?
                ) {
                    context?.let {
                        Glide.with(it)
                            /*.addDefaultRequestListener(object : RequestListener<Any?> {
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
                            })*/
                            .load(dataSource).signature(ChecksumSignature(file.file.checksum)).into(binding.gridItemImage).clearOnDetach()
                    }
                }

                override fun FileRetrievalDataSourceRetrieved(
                    fileDataSource: Any?,
                    fileThumbnailDataSource: Any?,
                    exception: Exception?
                ) {
                }
            })
            binding.gridItemImage
                .setOnClickListener(View.OnClickListener { onClickListener.onClick(absoluteAdapterPosition) })
            binding.gridItemImage.setOnLongClickListener(OnLongClickListener { false })
            binding.gridItemImage
                .setOnCreateContextMenuListener(OnCreateContextMenuListener { menu, v, menuInfo ->
                    var menuInfo = menuInfo
                    menuInfo = AdapterView.AdapterContextMenuInfo(
                        itemView,
                        absoluteAdapterPosition,
                        0
                    )
                    onClickListener.onCreateContextMenu(menu, v, menuInfo)
                })
        }
        companion object {
            fun from(context: Context?,parent: ViewGroup): FileViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = FilegridLayoutConstrainedBinding.inflate(layoutInflater, parent, false)
                return FileViewHolder(context, binding)
            }
        }
    }

    class HeaderViewHolder private constructor(val context: Context?,val binding: FilegridHeaderBinding): RecyclerView.ViewHolder(binding.root){
        fun bind(title: String?){

            binding.filegridHeaderText.text = title
        }
        companion object {
            fun from(context: Context?,parent: ViewGroup): HeaderViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = FilegridHeaderBinding.inflate(layoutInflater, parent, false)
                return HeaderViewHolder(context, binding)
            }
        }
    }

class FileDiffCallBack : DiffUtil.ItemCallback<FolderFileViewerModel>() {
    override fun areItemsTheSame(oldItem: FolderFileViewerModel, newItem: FolderFileViewerModel): Boolean {
        return if(oldItem is FolderFileViewerModel.FileModel && newItem is FolderFileViewerModel.FileModel)
            oldItem.file.onlineId == newItem.file.onlineId;
        else if(oldItem is FolderFileViewerModel.HeaderModel && newItem is FolderFileViewerModel.HeaderModel)
            oldItem.title == newItem.title
        else
            false
    }

    override fun areContentsTheSame(oldItem: FolderFileViewerModel, newItem: FolderFileViewerModel): Boolean {
        return if(oldItem is FolderFileViewerModel.FileModel && newItem is FolderFileViewerModel.FileModel)
            oldItem.file.onlineId == newItem.file.onlineId;
        else if(oldItem is FolderFileViewerModel.HeaderModel && newItem is FolderFileViewerModel.HeaderModel)
            oldItem.title == newItem.title
        else
            false
    }
}
}

interface EnhancedFolderFilesListOnClickListener {
    fun onClick(position: Int)
    fun onCreateContextMenu(menu: ContextMenu?, v: View?, menuInfo: ContextMenuInfo?)
}

