package com.quigglesproductions.secureimageviewer.ui.adapter.filelist

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.quigglesproductions.secureimageviewer.R
import com.quigglesproductions.secureimageviewer.datasource.file.IFileDataSource
import com.quigglesproductions.secureimageviewer.models.enhanced.file.IDisplayFile
import com.quigglesproductions.secureimageviewer.recycler.RecyclerViewSelectionMode
import org.acra.ACRA
import java.net.MalformedURLException

class EnhancedFileListRecyclerAdapter<T : IDisplayFile?> :
    RecyclerView.Adapter<EnhancedFileListRecyclerAdapter.ViewHolder> {
    private var files: MutableList<T> = ArrayList()
    private var mContext: Context
    private var onClickListener: ((position: Int) -> Unit)? = null

    fun setOnClickListener(callback: (position: Int) -> Unit){
        onClickListener = callback
    }

    fun getItem(position: Int): T {
        return files[position]
    }

    fun add(files: T) {
        this.files.add(files)
        notifyDataSetChanged()
    }

    fun setFiles(files: MutableList<T>) {
        this.files = files
        notifyDataSetChanged()
    }

    fun removeFile(folder: T) {
        val position = files.indexOf(folder)
        files.removeAt(position)
        notifyItemRemoved(position)
    }

    fun getFiles(): List<T> {
        return files
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView = view.findViewById<View>(R.id.grid_item_image) as ImageView
    }

    constructor(context: Context) {
        mContext = context
    }

    constructor(context: Context, files: ArrayList<T>) {
        mContext = context
        this.files = files
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.filegrid_layout_constrained, viewGroup, false)
        return ViewHolder(view)
    }

    override fun onViewRecycled(holder: ViewHolder) {
        Glide.with(mContext).clear(holder.imageView)
        super.onViewRecycled(holder)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {

        viewHolder.itemView.setOnClickListener {
            onClickListener?.invoke(viewHolder.absoluteAdapterPosition)
        }
        // Get element from your dataset at this position and replace the
        // contents of the view with that element

        val file: IDisplayFile? = getItem(position)
        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        try {
            file?.dataSource?.getFileThumbnailDataSource(mContext, object :
                IFileDataSource.DataSourceCallback {
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
                                    resource: Any?,
                                    model: Any?,
                                    target: Target<Any?>?,
                                    dataSource: DataSource?,
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
        } catch (e: MalformedURLException) {
            ACRA.errorReporter.handleSilentException(e)
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount(): Int {
        return files.size
    }

    fun clear() {
        files.clear()
        notifyDataSetChanged()
    }

    fun addList(folders: ArrayList<T>?) {
        files.addAll(folders!!)
        notifyDataSetChanged()
    }
}
