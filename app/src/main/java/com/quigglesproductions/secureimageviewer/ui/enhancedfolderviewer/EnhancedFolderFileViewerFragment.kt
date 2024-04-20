package com.quigglesproductions.secureimageviewer.ui.enhancedfolderviewer

import android.os.Bundle
import android.view.ContextMenu
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.TextView
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavDirections
import androidx.navigation.Navigation
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.quigglesproductions.secureimageviewer.R
import com.quigglesproductions.secureimageviewer.managers.ApplicationPreferenceManager
import com.quigglesproductions.secureimageviewer.paging.repository.FolderFilesMediatorRepository
import com.quigglesproductions.secureimageviewer.ui.adapter.filelist.EnhancedFolderFilesListOnClickListener
import com.quigglesproductions.secureimageviewer.ui.adapter.itemmodel.folderfileviewer.FolderFileViewerModel
import com.quigglesproductions.secureimageviewer.ui.enhancedfolderlist.EnhancedFolderListViewModel

class EnhancedFolderFileViewerFragment: BaseFolderViewerFragment() {
    //private val viewModel by hiltNavGraphViewModels<EnhancedFolderFileViewerViewModel>(R.id.main_navigation)
    private val folderListViewModel by activityViewModels<EnhancedFolderListViewModel>()
    //private lateinit var root: View

    override fun onContextItemSelected(item: MenuItem): Boolean {
        val selectedFile = (adapter.peek(item.itemId) as FolderFileViewerModel.FileModel).file
        when (item.groupId) {
            CONTEXTMENU_INFO -> {
                val bottomSheetDialog = BottomSheetDialog(requireContext())
                bottomSheetDialog.setContentView(R.layout.bottomdialog_fileinfo)
                bottomSheetDialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
                val itemNameText = bottomSheetDialog.findViewById<TextView>(R.id.item_name)
                val folderNameText = bottomSheetDialog.findViewById<TextView>(R.id.folder_name)
                val artistNameText = bottomSheetDialog.findViewById<TextView>(R.id.artist_name)
                val catagoriesText = bottomSheetDialog.findViewById<TextView>(R.id.catagories)
                val subjectsText = bottomSheetDialog.findViewById<TextView>(R.id.subjects)
                selectedFile!!.dataSource.getFileMetadata(requiresRequestManager()) { metadata, exception -> //selectedFile.metadata = metadata;
                    itemNameText!!.text = selectedFile.name
                    folderNameText!!.text = if(selectedFile.file.cachedFolderName != null) selectedFile.file.cachedFolderName else folderListViewModel.selectedFolder.value!!.name
                    artistNameText!!.text = selectedFile.artistName
                    catagoriesText!!.text = selectedFile.catagoryListString
                    subjectsText!!.text = selectedFile.subjectListString
                    bottomSheetDialog.create()
                    bottomSheetDialog.show()
                }
            }

            CONTEXTMENU_SET_THUMBNAIL -> {
                /*val file = enhancedAdapter!![item.itemId]
                if (file is FileWithMetadata) {
                    backgroundThreadPoster.post {
                        modularFileDatabase.folderDao().setThumbnail(
                            selectedFolder as RoomEmbeddedFolder?,
                            file as RoomEmbeddedFile
                        )
                    }
                }*/
            }

            CONTEXTMENU_UPLOAD -> {}
        }
        return true
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        folderViewModel.folder.value = folderListViewModel.selectedFolder.value
        folderViewModel.folderListType.value = folderListViewModel.folderListType.value
        setTitle(folderListViewModel.selectedFolder.value!!.name)
        folderViewModel.fileSortType.value = ApplicationPreferenceManager.getInstance().folderSortType
        folderViewModel.folderFilesRepository = FolderFilesMediatorRepository(requestService,cachingDatabase,downloadFileDatabase)
        (folderViewModel.folderFilesRepository as FolderFilesMediatorRepository).setFolder(folderViewModel.folder.value!!)
        setUiState()

        setFileClickListener(object: EnhancedFolderFilesListOnClickListener {
            override fun onClick(position: Int) {
                folderViewModel.selectedFile.value = (adapter.peek(position) as FolderFileViewerModel.FileModel).file
                folderViewModel.files.value = adapter.snapshot().items.filter{it is FolderFileViewerModel.FileModel}.map { (it as FolderFileViewerModel.FileModel).file }
                val action: NavDirections =
                    EnhancedFolderFileViewerFragmentDirections.actionEnhancedFolderFileViewerFragmentToEnhancedFileViewFragment(
                        position
                    )
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
                if (folderListViewModel.selectedFolder.value!!.folderOrigin == FolderOrigin.LOCAL) menu.add(
                    CONTEXTMENU_SET_THUMBNAIL,
                    cmi.position,
                    0,
                    "Set as Thumbnail"
                )
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



    /*private fun showSortDialog() {
        val items = arrayOf<CharSequence>("Name A-Z", "Name Z-A", "Newest First", "Oldest First")
        val builder = AlertDialog.Builder(
            requireContext()
        )
        builder.setTitle("Sort by")
        val currentType: FileSortType = ApplicationPreferenceManager.getInstance().folderSortType
        val checkedItem: Int = when (currentType) {
            FileSortType.NAME_ASC -> 0
            FileSortType.NAME_DESC -> 1
            FileSortType.NEWEST_FIRST -> 2
            FileSortType.OLDEST_FIRST -> 3
        }
        builder.setSingleChoiceItems(items, checkedItem) { dialog, which ->
            val result = items[which].toString()
            var newSortType = FileSortType.NAME_ASC
            when (result) {
                "Name A-Z" -> newSortType = FileSortType.NAME_ASC
                "Name Z-A" -> newSortType = FileSortType.NAME_DESC
                "Newest First" -> newSortType = FileSortType.NEWEST_FIRST
                "Oldest First" -> newSortType = FileSortType.OLDEST_FIRST
            }
            ApplicationPreferenceManager.getInstance().folderSortType = newSortType
            folderViewModel.fileSortType.value =newSortType

            //viewModel.fileSortType.update { newSortType }
            dialog.dismiss()
        }
        val alert = builder.create()
        //display dialog box
        alert.show()
    }*/
}