package com.quigglesproductions.secureimageviewer.ui.enhancedfolderviewer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.activityViewModels
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.quigglesproductions.secureimageviewer.R
import com.quigglesproductions.secureimageviewer.databinding.FragmentFolderViewBinding
import com.quigglesproductions.secureimageviewer.managers.ApplicationPreferenceManager
import com.quigglesproductions.secureimageviewer.room.enums.FileSortType
import com.quigglesproductions.secureimageviewer.ui.EnhancedMainMenuActivity
import com.quigglesproductions.secureimageviewer.ui.SecureFragment
import com.quigglesproductions.secureimageviewer.ui.adapter.filelist.EnhancedFolderFilesListAdapter
import com.quigglesproductions.secureimageviewer.ui.adapter.filelist.EnhancedFolderFilesListOnClickListener
import com.quigglesproductions.secureimageviewer.ui.adapter.itemmodel.folderfileviewer.FolderFileViewerModel
import com.quigglesproductions.secureimageviewer.ui.enhancedfolderlist.EnhancedFolderListViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
open class BaseFolderViewerFragment : SecureFragment() {
    //GridView gridview;
    private lateinit var binding: FragmentFolderViewBinding
    //lateinit var selectedFolder: RoomUnifiedFolder
    //private val viewModel by hiltNavGraphViewModels<EnhancedFolderFileViewerViewModel>(R.id.main_navigation)
    val folderViewModel by hiltNavGraphViewModels<FolderViewerViewModel>(R.id.main_navigation)
    private val folderListViewModel by activityViewModels<EnhancedFolderListViewModel>()
    private lateinit var root: View
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout

    @Inject
    lateinit var adapter: EnhancedFolderFilesListAdapter
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        setHasOptionsMenu(true)
        binding = FragmentFolderViewBinding.inflate(inflater, container, false)
        root = binding.root
        swipeRefreshLayout = binding.folderViewerSwipeContainer
        val columnCount = resources.getInteger(R.integer.column_count_filelist)
        val layoutManager = GridLayoutManager(context, columnCount)
        layoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return when (adapter.getItemViewType(position)) {
                    R.layout.filegrid_header -> columnCount
                    R.layout.filegrid_layout_constrained -> 1
                    else -> 1
                }
            }
        }
        binding.fileRecyclerview.layoutManager = layoutManager
        folderViewModel.folder.value = folderListViewModel.selectedFolder.value
        //viewModel.folderListType.value = folderListViewModel.folderListType.value

        folderViewModel.folderListType.value = folderListViewModel.folderListType.value
        return root
    }

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
        folderViewModel.fileSortType.value = ApplicationPreferenceManager.getInstance().folderSortType
        initView()
    }

    private fun initView() {
        binding.fileRecyclerview.adapter = adapter
        swipeRefreshLayout.setOnRefreshListener {
            adapter.refresh()
            swipeRefreshLayout.isRefreshing = false
        }
        folderViewModel.fileSortType.observe(viewLifecycleOwner){
            setUiState()
        }
    }

    /**
     * Sets the listener used every time an item is clicked
     */
    fun setFileClickListener(listener: EnhancedFolderFilesListOnClickListener){
        adapter.setOnClickListener(listener)
    }

    /**
     * Updates the UI using the data in the viewmodel
     */
    fun setUiState(){
        viewLifecycleOwner.lifecycleScope.launch {
            folderViewModel.generatePagedSource(folderViewModel.folder.value!!.fileGroupingType)
            folderViewModel.getPagedDataSource()?.collect{ files ->
                adapter.submitData(files)
            }

        }
    }

    fun showSortDialog() {
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
    }

    fun getRootView():View{
        return root
    }

    fun setTitle(title: String) {
        (requireActivity() as EnhancedMainMenuActivity).setActionBarTitle(title)
        //((EnhancedMainMenuActivity)requireActivity()).overrideActionBarTitle(title);
    }

    companion object {
        const val CONTEXTMENU_INFO = 0
        const val CONTEXTMENU_SET_THUMBNAIL = 1
        const val CONTEXTMENU_UPLOAD = 2
    }
}
