package com.quigglesproductions.secureimageviewer.ui.enhancedfolderviewer.kotlin

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
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavDirections
import androidx.navigation.Navigation.findNavController
import androidx.paging.LoadState
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.reflect.TypeToken
import com.quigglesproductions.secureimageviewer.R
import com.quigglesproductions.secureimageviewer.SortType
import com.quigglesproductions.secureimageviewer.databinding.FragmentFolderViewBinding
import com.quigglesproductions.secureimageviewer.managers.ApplicationPreferenceManager
import com.quigglesproductions.secureimageviewer.managers.FolderManager
import com.quigglesproductions.secureimageviewer.models.enhanced.file.EnhancedDatabaseFile
import com.quigglesproductions.secureimageviewer.models.enhanced.file.EnhancedOnlineFile
import com.quigglesproductions.secureimageviewer.models.enhanced.file.IDisplayFile
import com.quigglesproductions.secureimageviewer.models.enhanced.folder.IDisplayFolder
import com.quigglesproductions.secureimageviewer.room.databases.file.relations.FileWithMetadata
import com.quigglesproductions.secureimageviewer.ui.EnhancedMainMenuActivity
import com.quigglesproductions.secureimageviewer.ui.SecureFragment
import com.quigglesproductions.secureimageviewer.ui.enhancedfolderviewer.EnhancedFileGridRecyclerAdapter.EnhancedRecyclerViewOnClickListener
import com.quigglesproductions.secureimageviewer.ui.enhancedfolderviewer.EnhancedFolderViewerFragment
import com.quigglesproductions.secureimageviewer.ui.enhancedfolderviewer.EnhancedFolderViewerFragmentDirections
import com.quigglesproductions.secureimageviewer.ui.enhancedfolderviewer.FolderOrigin
import com.techyourchance.threadposter.BackgroundThreadPoster
import com.techyourchance.threadposter.UiThreadPoster
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.lang.reflect.Type
import javax.inject.Inject

@AndroidEntryPoint
class EnhancedFolderViewerFragmentKt : SecureFragment() {
    //GridView gridview;
    private lateinit var binding: FragmentFolderViewBinding
    lateinit var selectedFolder: IDisplayFolder
    private var scrollBottomReached = false
    var startingSortType: SortType? = null
    //private val viewModel: EnhancedFolderViewerViewModelKt by viewModels()
    private val viewModel by activityViewModels<EnhancedFolderViewerViewModelKt>()
    //var recyclerView: ShimmerRecyclerViewX? = null
    private val backgroundThreadPoster = BackgroundThreadPoster()
    private val uiThreadPoster = UiThreadPoster()
    private lateinit var root: View

    @Inject
    lateinit var adapter: FolderFilesListAdapter
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        //postponeEnterTransition();
        setHasOptionsMenu(true)
        binding = FragmentFolderViewBinding.inflate(inflater, container, false)
        root = binding!!.root
        /*lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED){

            }
        }*/
        /*viewModel = ViewModelProvider(this).get(
            EnhancedFolderViewerViewModel::class.java
        )*/
        //gridview = binding.fileGridview;
        //recyclerView = binding!!.fileShimmerRecyclerView
        val columnCount = resources.getInteger(R.integer.column_count_filelist)
        val layoutManager = GridLayoutManager(context, columnCount)
        binding!!.fileRecyclerview.layoutManager = layoutManager
        //binding!!.fileRecyclerview.showShimmerAdapter()
        selectedFolder = FolderManager.getInstance().currentFolder
        viewModel!!.folder.value = selectedFolder
        //restoreInstanceState(savedInstanceState)

        //registerForContextMenu(recyclerView!!)
        startingSortType = when (selectedFolder!!.getFolderOrigin()) {
            FolderOrigin.ONLINE -> ApplicationPreferenceManager.getInstance().onlineFolderSortType
            FolderOrigin.LOCAL, FolderOrigin.ROOM -> ApplicationPreferenceManager.getInstance().offlineFolderSortType
            else -> SortType.NAME_ASC
        }
        return root
    }

    private fun restoreInstanceState(savedInstanceState: Bundle?) {
        //if(savedInstanceState != null) {
        val fileListJson = viewModel!!.savedStateHandle.get<String>("FileList")
        //String fileListJson = savedInstanceState.getString("FileList");
        val listType: Type?
        if (fileListJson != null) {
            listType = when (selectedFolder!!.folderOrigin) {
                FolderOrigin.ONLINE -> object :
                    TypeToken<ArrayList<EnhancedOnlineFile?>?>() {}.type

                FolderOrigin.LOCAL -> object :
                    TypeToken<ArrayList<EnhancedDatabaseFile?>?>() {}.type

                FolderOrigin.ROOM -> object :
                    TypeToken<ArrayList<FileWithMetadata?>?>() {}.type

                else -> null
            }
            val files = gson.fromJson<ArrayList<IDisplayFile>>(fileListJson, listType)
            //recyclerView!!.hideShimmerAdapter()
        }
        //}
    }

    private fun saveInstanceState() {
        /*if (enhancedAdapter!!.itemCount > 0) viewModel!!.state.set(
            "FileList", gson.toJson(
                enhancedAdapter!!.files
            )
        )*/
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
                    folderNameText!!.text = selectedFolder!!.name
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
        binding!!.fileRecyclerview.adapter = adapter


        /*gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                NavDirections action = EnhancedFolderViewerFragmentDirections.actionEnhancedFolderViewerFragmentToEnhancedFileViewFragment(position);
                Navigation.findNavController(view).navigate(action);
                //Intent intent = new Intent(getContext(), EnhancedFileViewActivity.class);
                //intent.putExtra("position",position);
                //startActivity(intent);
            }
        });*/adapter.setOnClickListener(object : FolderFilesListOnClickListener {
            override fun onClick(position: Int) {
                viewModel.selectedFile.value = adapter.peek(position)
                viewModel.files.value = adapter.snapshot().items
                saveInstanceState()
                val action: NavDirections =
                    EnhancedFolderViewerFragmentKtDirections.actionEnhancedFolderViewerFragmentKtToEnhancedFileViewFragment(
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
    }

    private fun collectUiState() {
        backgroundThreadPoster.post {
            viewLifecycleOwner.lifecycleScope.launch {
                viewModel!!.getFiles(viewModel!!.folder.value!!.onlineId.toInt()).collectLatest { files ->
                    adapter?.submitData(files)
                }
            }
        }

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
        val currentType: SortType
        currentType = when (selectedFolder!!.folderOrigin) {
            FolderOrigin.ONLINE -> ApplicationPreferenceManager.getInstance().onlineFolderSortType
            FolderOrigin.LOCAL -> ApplicationPreferenceManager.getInstance().offlineFolderSortType
            else -> SortType.NAME_ASC
        }
        var checkedItem = -1
        checkedItem = when (currentType) {
            SortType.NAME_ASC -> 0
            SortType.NAME_DESC -> 1
            SortType.NEWEST_FIRST -> 2
            SortType.OLDEST_FIRST -> 3
        }
        builder.setSingleChoiceItems(items, checkedItem) { dialog, which ->
            val result = items[which].toString()
            var newSortType = SortType.NAME_ASC
            when (result) {
                "Name A-Z" -> newSortType = SortType.NAME_ASC
                "Name Z-A" -> newSortType = SortType.NAME_DESC
                "Newest First" -> newSortType = SortType.NEWEST_FIRST
                "Oldest First" -> newSortType = SortType.OLDEST_FIRST
            }
            //enhancedAdapter!!.sort(newSortType)
            if (selectedFolder!!.folderOrigin == FolderOrigin.LOCAL) ApplicationPreferenceManager.getInstance().offlineFolderSortType =
                newSortType else ApplicationPreferenceManager.getInstance().onlineFolderSortType =
                newSortType
            selectedFolder!!.sortFiles(newSortType)
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
