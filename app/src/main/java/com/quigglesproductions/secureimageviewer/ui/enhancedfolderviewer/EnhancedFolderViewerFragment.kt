package com.quigglesproductions.secureimageviewer.ui.enhancedfolderviewer

import android.os.Bundle
import android.view.ContextMenu
import android.view.ContextMenu.ContextMenuInfo
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView.AdapterContextMenuInfo
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavDirections
import androidx.navigation.Navigation.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.quigglesproductions.secureimageviewer.R
import com.quigglesproductions.secureimageviewer.databinding.FragmentFolderViewBinding
import com.quigglesproductions.secureimageviewer.managers.ApplicationPreferenceManager
import com.quigglesproductions.secureimageviewer.managers.FolderManager
import com.quigglesproductions.secureimageviewer.models.enhanced.file.IDisplayFile
import com.quigglesproductions.secureimageviewer.models.enhanced.folder.IDisplayFolder
import com.quigglesproductions.secureimageviewer.room.databases.unified.entity.RoomUnifiedFolder
import com.quigglesproductions.secureimageviewer.room.enums.FileSortType
import com.quigglesproductions.secureimageviewer.ui.EnhancedMainMenuActivity
import com.quigglesproductions.secureimageviewer.ui.SecureFragment
import com.quigglesproductions.secureimageviewer.ui.enhancedfolderlist.EnhancedFolderListViewModel
import com.techyourchance.threadposter.BackgroundThreadPoster
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class EnhancedFolderViewerFragment : SecureFragment() {
    //GridView gridview;
    private lateinit var binding: FragmentFolderViewBinding
    lateinit var selectedFolder: IDisplayFolder
    private lateinit var startingSortType: FileSortType
    //private val viewModel: EnhancedFolderViewerViewModelKt by viewModels()
    private val viewModel by activityViewModels<EnhancedFolderViewerViewModelKt>()
    private val folderListViewModel by activityViewModels<EnhancedFolderListViewModel>()
    private val backgroundThreadPoster = BackgroundThreadPoster()
    private lateinit var root: View
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout

    @Inject
    lateinit var adapter: FolderFilesListAdapter
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
        binding.fileRecyclerview.layoutManager = layoutManager
        //binding!!.fileRecyclerview.showShimmerAdapter()
        selectedFolder = FolderManager.instance.currentFolder!!
        viewModel.folder.value = selectedFolder
        viewModel.folderListType.value = folderListViewModel.folderListType.value
        //restoreInstanceState(savedInstanceState)

        //registerForContextMenu(recyclerView!!)
        viewModel.fileSortType.value = when (selectedFolder.folderOrigin) {
            FolderOrigin.ONLINE -> ApplicationPreferenceManager.getInstance().onlineFolderSortType
            FolderOrigin.LOCAL, FolderOrigin.ROOM -> ApplicationPreferenceManager.getInstance().offlineFolderSortType
            else -> FileSortType.NAME_ASC
        }
        return root
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        val selectedFile = adapter.peek(item.itemId)
        when (item.groupId) {
            CONTEXTMENU_INFO -> {
                //new ItemInfoDialog(adapter.getItem(item.getItemId())).show(getSupportFragmentManager(),ItemInfoDialog.TAG);
                val bottomSheetDialog = BottomSheetDialog(requireContext())
                bottomSheetDialog.setContentView(R.layout.bottomdialog_fileinfo)
                val itemNameText = bottomSheetDialog.findViewById<TextView>(R.id.item_name)
                val folderNameText = bottomSheetDialog.findViewById<TextView>(R.id.folder_name)
                val artistNameText = bottomSheetDialog.findViewById<TextView>(R.id.artist_name)
                val catagoriesText = bottomSheetDialog.findViewById<TextView>(R.id.catagories)
                val subjectsText = bottomSheetDialog.findViewById<TextView>(R.id.subjects)
                selectedFile!!.dataSource.getFileMetadata(requiresRequestManager()) { metadata, exception -> //selectedFile.metadata = metadata;
                    itemNameText!!.text = selectedFile.name
                    folderNameText!!.text = selectedFolder.name
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
        viewModel.fileAdapter = adapter
        setTitle(selectedFolder!!.name)
        //restoreInstanceState(savedInstanceState)
        //getFolderFiles(context, viewModel)
        initView()
        collectUiState()
    }

    private fun initView() {
        //adapter = FolderFilesListAdapter(context)
        binding.fileRecyclerview.adapter = adapter
        adapter.setOnClickListener(object : FolderFilesListOnClickListener {
            override fun onClick(position: Int) {
                viewModel.selectedFile.value = adapter.peek(position)
                viewModel.files.value = adapter.snapshot().items
                val action: NavDirections =
                    EnhancedFolderViewerFragmentDirections.actionEnhancedFolderViewerFragmentToEnhancedFileViewFragment(
                        position
                    )
                findNavController(root).navigate(action)
            }

            override fun onCreateContextMenu(
                menu: ContextMenu?,
                v: View?,
                menuInfo: ContextMenuInfo?
            ) {
                menu!!.setHeaderTitle("Options")
                val cmi = menuInfo as AdapterContextMenuInfo
                //EnhancedFile selectedFile = enhancedAdapter.get(position);
                val selectedFile: IDisplayFile? = adapter.peek(cmi.position)
                menu!!.add(CONTEXTMENU_INFO, cmi.position, 0, "Info")
                if (selectedFolder.folderOrigin == FolderOrigin.LOCAL) menu.add(
                    CONTEXTMENU_SET_THUMBNAIL,
                    cmi.position,
                    0,
                    "Set as Thumbnail"
                )
            }
        })
        swipeRefreshLayout.setOnRefreshListener {
            adapter.refresh()
            swipeRefreshLayout.isRefreshing = false
        }
    }

    private fun collectUiState() {
        //backgroundThreadPoster.post {
            viewLifecycleOwner.lifecycleScope.launch {
                viewModel.getFiles(viewModel.folder.value!! as RoomUnifiedFolder).collect { files ->
                    adapter.submitData(viewLifecycleOwner.lifecycle,files)
                }
            }
        //}

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_folderview_fragment, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.folderview_fragment_sort -> showSortDialog()
            else -> return false
        }
        return true
    }

    private fun showSortDialog() {
        val items = arrayOf<CharSequence>("Name A-Z", "Name Z-A", "Newest First", "Oldest First")
        val builder = AlertDialog.Builder(
            requireContext()
        )
        builder.setTitle("Sort by")
        val currentType: FileSortType
        currentType = when (selectedFolder!!.folderOrigin) {
            FolderOrigin.ONLINE -> ApplicationPreferenceManager.getInstance().onlineFolderSortType
            FolderOrigin.LOCAL -> ApplicationPreferenceManager.getInstance().offlineFolderSortType
            else -> FileSortType.NAME_ASC
        }
        var checkedItem = -1
        checkedItem = when (currentType) {
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
            //enhancedAdapter!!.sort(newSortType)
            if (selectedFolder.folderOrigin == FolderOrigin.LOCAL)
                ApplicationPreferenceManager.getInstance().offlineFolderSortType = newSortType
            else
                ApplicationPreferenceManager.getInstance().onlineFolderSortType = newSortType
            viewModel.fileSortType.update { newSortType }
            dialog.dismiss()
        }
        val alert = builder.create()
        //display dialog box
        alert.show()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
    }

    private fun setTitle(title: String) {
        (requireActivity() as EnhancedMainMenuActivity).setActionBarTitle(title)
        //((EnhancedMainMenuActivity)requireActivity()).overrideActionBarTitle(title);
    }

    companion object {
        private const val CONTEXTMENU_INFO = 0
        private const val CONTEXTMENU_SET_THUMBNAIL = 1
        private const val CONTEXTMENU_UPLOAD = 2
        private const val LIST_UPDATE_TRIGGER_THRESHOLD = 75
    }
}
