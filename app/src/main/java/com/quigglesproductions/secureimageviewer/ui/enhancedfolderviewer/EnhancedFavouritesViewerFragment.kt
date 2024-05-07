package com.quigglesproductions.secureimageviewer.ui.enhancedfolderviewer

import android.os.Bundle
import android.view.ContextMenu
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import androidx.navigation.NavDirections
import androidx.navigation.Navigation
import com.quigglesproductions.secureimageviewer.R
import com.quigglesproductions.secureimageviewer.paging.repository.FavouriteFilesRepository
import com.quigglesproductions.secureimageviewer.room.databases.unified.entity.UnifiedFavouritesFolder
import com.quigglesproductions.secureimageviewer.ui.adapter.filelist.EnhancedFolderFilesListOnClickListener
import com.quigglesproductions.secureimageviewer.ui.adapter.itemmodel.folderfileviewer.FolderFileViewerModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class EnhancedFavouritesViewerFragment: BaseFolderViewerFragment() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        folderViewModel.folder.value = UnifiedFavouritesFolder(FolderOrigin.LOCAL)
        folderViewModel.folderFilesRepository = FavouriteFilesRepository(requestService,downloadFileDatabase)
        setUiState()

        setFileClickListener(object: EnhancedFolderFilesListOnClickListener {
            override fun onClick(position: Int) {
                folderViewModel.selectedFile.value = (adapter.peek(position) as FolderFileViewerModel.FileModel).file
                folderViewModel.files.value = adapter.snapshot().items.filter{it is FolderFileViewerModel.FileModel}.map { (it as FolderFileViewerModel.FileModel).file }
                val action: NavDirections =
                    EnhancedFavouritesViewerFragmentDirections.actionEnhancedFavouritesViewerFragmentToNavEnhancedFileViewFragment(position)
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

    @Deprecated("Deprecated in Java")
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_folderview_fragment, menu)
    }

    @Deprecated("Deprecated in Java")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.folderview_fragment_sort -> showSortDialog()
            else -> return false
        }
        return true
    }
}