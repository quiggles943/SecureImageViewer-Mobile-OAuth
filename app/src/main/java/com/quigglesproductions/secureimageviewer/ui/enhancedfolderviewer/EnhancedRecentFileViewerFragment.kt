package com.quigglesproductions.secureimageviewer.ui.enhancedfolderviewer

import android.os.Bundle
import android.view.ContextMenu
import android.view.View
import android.widget.AdapterView
import androidx.navigation.NavDirections
import androidx.navigation.Navigation
import com.quigglesproductions.secureimageviewer.paging.repository.RecentFilesRepository
import com.quigglesproductions.secureimageviewer.room.databases.unified.entity.UnifiedRecentsFolder
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class EnhancedRecentFileViewerFragment: BaseFolderViewerFragment() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        folderViewModel.folder.value = UnifiedRecentsFolder(FolderOrigin.ONLINE)
        folderViewModel.folderFilesRepository = RecentFilesRepository(requestService,downloadFileDatabase)
        setUiState()

        setFileClickListener(object: FolderFilesListOnClickListener {
            override fun onClick(position: Int) {
                folderViewModel.selectedFile.value = adapter.peek(position)
                folderViewModel.files.value = adapter.snapshot().items
                val action: NavDirections =
                    EnhancedRecentFileViewerFragmentDirections.actionEnhancedRecentFileViewerFragmentToNavEnhancedFileViewFragment(position)
                Navigation.findNavController(getRootView()).navigate(action)
            }

            override fun onCreateContextMenu(
                menu: ContextMenu?,
                v: View?,
                menuInfo: ContextMenu.ContextMenuInfo?
            ) {
                menu!!.setHeaderTitle("Options")
                val cmi = menuInfo as AdapterView.AdapterContextMenuInfo
                menu.add(CONTEXTMENU_INFO, cmi.position, 0, "Info")
                /*if (folderListViewModel.selectedFolder.value!!.folderOrigin == FolderOrigin.LOCAL) menu.add(
                    CONTEXTMENU_SET_THUMBNAIL,
                    cmi.position,
                    0,
                    "Set as Thumbnail"
                )*/
            }
        })
    }
}