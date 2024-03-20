package com.quigglesproductions.secureimageviewer.ui.enhancedfileviewer.fragments

import android.os.Bundle
import android.view.View
import androidx.annotation.OptIn
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.media3.common.util.UnstableApi
import com.quigglesproductions.secureimageviewer.R
import com.quigglesproductions.secureimageviewer.room.databases.unified.entity.relations.RoomUnifiedEmbeddedFile
import com.quigglesproductions.secureimageviewer.ui.SecureFragment
import com.quigglesproductions.secureimageviewer.ui.compoundcontrols.FileViewerNavigator
import com.quigglesproductions.secureimageviewer.ui.enhancedfileviewer.EnhancedFileViewFragment
import com.quigglesproductions.secureimageviewer.ui.enhancedfolderviewer.FolderViewerViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
open class BaseFileViewFragmentKt : SecureFragment() {
    private val folderViewModel by hiltNavGraphViewModels<FolderViewerViewModel>(R.id.main_navigation)
    @UnstableApi
    var viewerNavigator: FileViewerNavigator? = null
        private set

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        if (parentFragment is EnhancedFileViewFragment) registerViewerNavigator((parentFragment as EnhancedFileViewFragment?)!!.navigator)
        super.onViewCreated(view, savedInstanceState)
    }

    @OptIn(UnstableApi::class)
    fun registerViewerNavigator(navigator: FileViewerNavigator?) {
        viewerNavigator = navigator
    }

    fun setFileFavourite(state:Boolean){
        val fileViewFragment = parentFragment as EnhancedFileViewFragment
        fileViewFragment.updateFileFavourite(file,state)
    }

    val file: RoomUnifiedEmbeddedFile
        get() {
            val args = arguments
            val sourceType = FileSourceType.getFromKey(
                args!!.getString(ARG_FILE_SOURCE_TYPE)
            )
            val position = args.getInt(ARG_FILE_POSITION)
            /*when (sourceType) {

                FileSourceType.ONLINE -> {
                    file = gson.fromJson(args.getString(ARG_FILE), ModularOnlineFile::class.java)
                    file.setDataSource(
                        RetrofitFileDataSource(
                            file,
                            requiresAuroraAuthenticationManager()
                        )
                    )
                }

                FileSourceType.DATABASE -> file = gson.fromJson(
                    args.getString(ARG_FILE), EnhancedDatabaseFile::class.java
                )

                FileSourceType.ROOM -> file = gson.fromJson(
                    args.getString(ARG_FILE), FileWithMetadata::class.java
                )

                FileSourceType.MODULAR -> file = gson.fromJson(
                    args.getString(ARG_FILE), RoomEmbeddedFile::class.java
                )

                FileSourceType.PAGING -> file = gson.fromJson(
                    args.getString(ARG_FILE),
                    com.quigglesproductions.secureimageviewer.room.databases.paging.file.entity.relations.RoomEmbeddedFile::class.java
                )

                else -> file = null
            }*/
            return folderViewModel.files.value!![position]
        }

    enum class FileSourceType {
        UNKNOWN,
        DATABASE,
        ROOM,
        MODULAR,
        PAGING,
        ONLINE;

        companion object {
            fun getFromKey(key: String?): FileSourceType {
                val result = UNKNOWN
                if (key != null) {
                    for (type in entries) {
                        if (type.toString().contentEquals(key.uppercase())) return type
                    }
                }
                return UNKNOWN
            }
        }
    }

    companion object {
        const val ARG_FILE_ID = "fileid"
        const val ARG_FILE = "file"
        const val ARG_FILE_SOURCE_TYPE = "sourceType"
        const val ARG_FILE_POSITION = "position"
    }
}
