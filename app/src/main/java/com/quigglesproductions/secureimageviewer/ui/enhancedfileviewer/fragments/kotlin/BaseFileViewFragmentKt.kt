package com.quigglesproductions.secureimageviewer.ui.enhancedfileviewer.fragments.kotlin

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import com.quigglesproductions.secureimageviewer.models.enhanced.file.IDisplayFile
import com.quigglesproductions.secureimageviewer.ui.SecureFragment
import com.quigglesproductions.secureimageviewer.ui.compoundcontrols.FileViewerNavigator
import com.quigglesproductions.secureimageviewer.ui.enhancedfileviewer.EnhancedFileViewFragment
import com.quigglesproductions.secureimageviewer.ui.enhancedfolderviewer.kotlin.EnhancedFolderViewerViewModelKt

open class BaseFileViewFragmentKt : SecureFragment() {
    private val folderViewModel by activityViewModels<EnhancedFolderViewerViewModelKt>()
    var viewerNavigator: FileViewerNavigator? = null
        private set

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        if (parentFragment is EnhancedFileViewFragment) registerViewerNavigator((parentFragment as EnhancedFileViewFragment?)!!.navigator)
        retainInstance = true
        super.onViewCreated(view, savedInstanceState)
    }

    fun registerViewerNavigator(navigator: FileViewerNavigator?) {
        viewerNavigator = navigator
    }

    val file: IDisplayFile?
        get() {
            val args = arguments
            val sourceType = FileSourceType.getFromKey(
                args!!.getString(ARG_FILE_SOURCE_TYPE)
            )
            val position = args!!.getInt(ARG_FILE_POSITION)
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
