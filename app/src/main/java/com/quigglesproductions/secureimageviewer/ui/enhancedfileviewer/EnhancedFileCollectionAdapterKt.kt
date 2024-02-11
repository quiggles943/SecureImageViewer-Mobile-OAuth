package com.quigglesproductions.secureimageviewer.ui.enhancedfileviewer

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.adapter.FragmentViewHolder
import com.quigglesproductions.secureimageviewer.models.enhanced.file.IDisplayFile
import com.quigglesproductions.secureimageviewer.ui.compoundcontrols.FileViewerNavigator
import com.quigglesproductions.secureimageviewer.ui.enhancedfileviewer.fragments.BaseFileViewFragmentKt
import com.quigglesproductions.secureimageviewer.ui.enhancedfileviewer.fragments.ImageFileViewFragmentKt
import com.quigglesproductions.secureimageviewer.ui.enhancedfileviewer.fragments.VideoFileViewFragmentKt

class EnhancedFileCollectionAdapterKt<T : IDisplayFile?>(fragment: Fragment) :
    FragmentStateAdapter(fragment) {
    private val files = ArrayList<T>()
    private var zoomCallback: ZoomLevelChangeCallback? = null
    private var navigatorControls: FileViewerNavigator? = null

    override fun createFragment(position: Int): Fragment {
        val file = files[position]
        var fragment: Fragment = when (file!!.fileTypeString) {
            "IMAGE" -> ImageFileViewFragmentKt()
            "VIDEO" -> VideoFileViewFragmentKt()
            else -> ImageFileViewFragmentKt()
        }
        val args = Bundle()
        args.putInt(BaseFileViewFragmentKt.ARG_FILE_POSITION,position)
        args.putInt(BaseFileViewFragmentKt.ARG_FILE_ID, file.onlineId)
        fragment.arguments = args
        return fragment
    }

    override fun getItemCount(): Int {
        return files.size
    }

    fun addFiles(fileModels: List<T>?) {
        files.clear()
        files.addAll(fileModels!!)
        notifyDataSetChanged()
    }

    fun getItem(position: Int): T {
        return files[position]
    }

    fun setFileZoomLevelCallback(callback: ZoomLevelChangeCallback) {
        zoomCallback = callback
    }

    fun setFileNavigator(fileNavigator: FileViewerNavigator?) {
        navigatorControls = fileNavigator
    }

    val zoomLevelCallback: ZoomLevelChangeCallback
        get() = object : ZoomLevelChangeCallback {
            override fun zoomLevelChanged(isZoomed: Boolean) {
                zoomCallback!!.zoomLevelChanged(isZoomed)
            }
        }

    fun getPosition(selectedFile: IDisplayFile?): Int {
        return files.indexOf(selectedFile)
    }

    fun interface ZoomLevelChangeCallback {
        fun zoomLevelChanged(isZoomed: Boolean)
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
    }

    override fun onBindViewHolder(holder: FragmentViewHolder, position: Int, payloads: List<Any>) {
        super.onBindViewHolder(holder, position, payloads)
    }
}
