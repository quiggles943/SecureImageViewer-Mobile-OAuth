package com.quigglesproductions.secureimageviewer.ui.enhancedfolderlist

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.ContextMenu
import android.view.ContextMenu.ContextMenuInfo
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavDirections
import androidx.navigation.Navigation.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import com.google.android.material.snackbar.Snackbar
import com.quigglesproductions.secureimageviewer.R
import com.quigglesproductions.secureimageviewer.databinding.FragmentFolderListBinding
import com.quigglesproductions.secureimageviewer.downloader.FolderDownloadWorker
import com.quigglesproductions.secureimageviewer.enums.FileGroupBy
import com.quigglesproductions.secureimageviewer.managers.ApplicationPreferenceManager
import com.quigglesproductions.secureimageviewer.managers.FolderManager
import com.quigglesproductions.secureimageviewer.managers.NotificationManager
import com.quigglesproductions.secureimageviewer.models.enhanced.folder.IDisplayFolder
import com.quigglesproductions.secureimageviewer.observable.IFolderDownloadObserver
import com.quigglesproductions.secureimageviewer.recycler.RecyclerViewSelectionMode
import com.quigglesproductions.secureimageviewer.room.databases.unified.entity.RoomUnifiedFolder
import com.quigglesproductions.secureimageviewer.room.databases.unified.entity.relations.RoomUnifiedEmbeddedFolder
import com.quigglesproductions.secureimageviewer.ui.EnhancedMainMenuActivity
import com.quigglesproductions.secureimageviewer.ui.SecureFragment
import com.quigglesproductions.secureimageviewer.ui.overview.OverviewViewModel
import com.quigglesproductions.secureimageviewer.utils.ObjectUtils
import kotlinx.coroutines.launch
import javax.inject.Inject


class EnhancedFolderListFragment : SecureFragment() {
    private var binding: FragmentFolderListBinding? = null
    private var myMenu: Menu? = null
    private var state: String? = null
    private lateinit var recyclerView: RecyclerView
    private val viewModel by activityViewModels<EnhancedFolderListViewModel>()
    private lateinit var swipeLayout: SwipeRefreshLayout
    @Inject
    lateinit var adapter: FolderListAdapter
    lateinit var root:View

    private lateinit var downloadObserver: IFolderDownloadObserver
    private val args : EnhancedFolderListFragmentArgs by navArgs()
    private val overviewViewModel by activityViewModels<OverviewViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        setHasOptionsMenu(true)
        binding = FragmentFolderListBinding.inflate(inflater, container, false)
        root = binding!!.root
        state = args.state
        viewModel.folderListType.value = FolderListType.valueOf(state!!)

        if(viewModel.folderListType.value == FolderListType.DOWNLOADED) {
            viewModel.fileGrouping.value = ApplicationPreferenceManager.getInstance().getFileGroupBy(FileGroupBy.FOLDERS)
            setTitle("Local " + viewModel.fileGrouping.value!!.displayName)
        }

        recyclerView = binding!!.folderShimmerRecyclerView
        swipeLayout = binding!!.folderListSwipeContainer
        setupRecyclerView()
        setupSwipeRefreshLayout()
        downloadManager.setCallback { folderDownload, exception ->
            if (exception == null) NotificationManager.getInstance().showSnackbar(
                "Folder " + folderDownload.folderName + " downloaded successfully",
                Snackbar.LENGTH_SHORT
            )
        }
        downloadObserver = object: IFolderDownloadObserver {
            override fun folderDownloaded(folder: RoomUnifiedFolder) {
                viewModel.invalidatePagedData()
            }

            override fun folderThumbnailDownloaded(folder: RoomUnifiedFolder) {
                viewModel.invalidatePagedData()
            }

            override fun downloadStatusUpdated(folder: RoomUnifiedFolder, count: Int, total: Int) {

            }

        }
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        collectUiState()
    }

    private fun collectUiState() {
        viewModel.fileGrouping.observe(viewLifecycleOwner){
            viewLifecycleOwner.lifecycleScope.launch {
                viewModel.createPagedSource(it)
                viewModel.pagedFolders!!.collect { files ->
                    adapter.submitData(viewLifecycleOwner.lifecycle, files)
                }
            }
        }
        folderDownloaderMediator.add(downloadObserver)
    }

    private fun setupRecyclerView() {
        val columnCount = resources.getInteger(R.integer.column_count_folderlist)
        val layoutManager = GridLayoutManager(context, columnCount)
        recyclerView.layoutManager = layoutManager
        recyclerView.setHasFixedSize(true)
        recyclerView.setItemViewCacheSize(20)
        adapter = FolderListAdapter(requireContext(),downloadFileDatabase)
        adapter.setFileUpdates(overviewViewModel.fileUpdates.value)
        adapter.setOnSelectionModeChangeListener(object :
            SelectionChangedListener {

            override fun selectionModeChanged(selectionMode: RecyclerViewSelectionMode?) {
                when (selectionMode) {
                    RecyclerViewSelectionMode.SINGLE -> {
                        if (viewModel.folderListType.value == FolderListType.ONLINE) {
                            myMenu!!.findItem(R.id.online_folder_recent_files).setVisible(true)
                            myMenu!!.findItem(R.id.online_folder_download_selection)
                                .setVisible(false)
                            myMenu!!.findItem(R.id.online_folder_download_viewer).setVisible(true)
                            setTitle("Online Viewer")
                        } else {
                            myMenu!!.findItem(R.id.offline_folder_delete).setVisible(false)
                            myMenu!!.findItem(R.id.offline_folder_favourites).setVisible(true)
                            setTitle("Local "+viewModel.fileGrouping.value!!.displayName)
                        }
                        val ta = context!!.theme.obtainStyledAttributes(androidx.appcompat.R.styleable.AppCompatTheme)
                        @SuppressLint("ResourceAsColor") val primaryColor =
                            ta.getColor(androidx.appcompat.R.styleable.AppCompatTheme_colorPrimary, R.color.white)
                        setActionBarColorFromInt(primaryColor)
                    }

                    RecyclerViewSelectionMode.MULTI -> {
                        if (viewModel.folderListType.value == FolderListType.ONLINE) {
                            myMenu!!.findItem(R.id.online_folder_recent_files).setVisible(false)
                            myMenu!!.findItem(R.id.online_folder_download_selection)
                                .setVisible(true)
                            myMenu!!.findItem(R.id.online_folder_download_viewer).setVisible(false)

                        } else {
                            myMenu!!.findItem(R.id.offline_folder_delete).setVisible(true)
                            myMenu!!.findItem(R.id.offline_folder_favourites).setVisible(false)
                        }
                        setActionBarColor(R.color.selected)
                    }

                    else -> {}
                }
            }

            override fun selectionAdded(position: Int) {
                setTitle(adapter.getSelectedCount().toString() + " Selected")
            }

            override fun selectionRemoved(position: Int) {
                setTitle(adapter.getSelectedCount().toString() + " Selected")
            }
        })
        adapter.setOnClickListener(object :
            FolderListOnClickListener {
            override fun onClick(position: Int) {
                if (adapter.isMultiSelect()) {
                    if (adapter.getIsSelected(position)) adapter.removeFromSelected(
                        position
                    ) else adapter.addToSelected(position)
                    if (adapter.getSelectedCount() == 0) adapter.setMultiselect(false)
                } else {
                    val value = adapter.peek(position)

                    if (value != null) {
                        navigateToFolder(value)
                    }
                }
            }

            override fun onLongClick(position: Int) {
                if (adapter.getSelectedCount() == 0) {
                    //vibrator.vibrate(10);
                    adapter.setMultiselect(true)
                    adapter.addToSelected(position)
                } else {
                    if (adapter.getIsSelected(position)) {
                        adapter.removeFromSelected(position)
                    } else {
                        adapter.addToSelected(position)
                    }
                }
            }
        })
        adapter.stateRestorationPolicy =
            RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY
        //recyclerView.addItemDecoration(new RecyclerViewMargin(0,columnCount));
        recyclerView.adapter = adapter
        registerForContextMenu((recyclerView))
    }

    private fun navigateToFolder(selectedFolder: IDisplayFolder){
        if(selectedFolder.isSecure)
            requiresAuroraAuthenticationManager().biometricAuthenticator.requestBiometricAuthentication(
                requiresSecureActivity(),
                R.string.folder_authentication_title,
                R.string.folder_authentication_subtitle)
            { success, _ ->
                if(success)
                    navigate(selectedFolder)
                else
                    NotificationManager.getInstance().showToast("Unable to access folder", Toast.LENGTH_SHORT)
            }
        else
            navigate(selectedFolder)
    }

    private fun navigate(selectedFolder: IDisplayFolder){
        val action =
            EnhancedFolderListFragmentDirections.actionEnhancedFolderListFragmentToEnhancedFolderFileViewerFragment()
        viewModel.selectedFolder.value = selectedFolder
        findNavController(binding!!.root).navigate(action)
    }
    private fun setupSwipeRefreshLayout(){
        swipeLayout.setOnRefreshListener {
            adapter.refresh()
            swipeLayout.isRefreshing = false
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        when (viewModel.folderListType.value){
            FolderListType.ONLINE -> inflater.inflate(R.menu.menu_online_folder, menu)
            FolderListType.DOWNLOADED -> inflater.inflate(
            R.menu.menu_offline_folder,
            menu
        )
            else -> return
        }
        myMenu = menu
    }

    @SuppressLint("NonConstantResourceId")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.online_folder_recent_files ->{
                val action = EnhancedFolderListFragmentDirections.actionEnhancedFolderListFragmentToEnhancedRecentFileViewerFragment()
                findNavController(binding!!.root).navigate(action)
            }

            R.id.online_folder_download_selection -> {
                if(ApplicationPreferenceManager.getInstance().getFileGroupBy(FileGroupBy.FOLDERS) == FileGroupBy.FOLDERS) {
                    val onlineFolders = adapter.getSelectedFolders() as List<RoomUnifiedFolder>
                    for (folder: RoomUnifiedFolder in onlineFolders) {

                        viewLifecycleOwner.lifecycleScope.launch {
                            val downloadFolder = ObjectUtils.createDeepCopy(folder)
                            downloadFolder.isAvailable = false
                            val folderId = downloadFileDatabase.folderDao().insert(downloadFolder)
                            val inputData = Data.Builder().putLong("folderId", folderId).build()
                            val downloadWorkRequest: OneTimeWorkRequest =
                                OneTimeWorkRequestBuilder<FolderDownloadWorker>()
                                    .setInputData(inputData)
                                    .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                                    .build()
                            folderDownloaderMediator.enqueueFolderDownload(
                                folder,
                                downloadWorkRequest
                            )
                        }
                    }
                    NotificationManager.getInstance().showSnackbar(
                        "Downloading " + adapter.getSelectedCount() + " folders",
                        Snackbar.LENGTH_SHORT
                    )
                    adapter.setMultiselect(false)
                }
            }

            R.id.online_folder_download_viewer -> findNavController(binding!!.root).navigate(R.id.action_nav_enhancedFolderListFragment_to_downloadViewerFragment)

            R.id.offline_folder_delete -> deleteSelectedFolders()

            R.id.offline_folder_sort_type -> {
                showSortDialog()
                return false
            }
            R.id.offline_folder_favourites -> navigateToFavourites()

            else -> return false
        }
        return true
    }

    private fun deleteSelectedFolders(){
        if(ApplicationPreferenceManager.getInstance().getFileGroupBy(FileGroupBy.FOLDERS) == FileGroupBy.FOLDERS) {
            val selectedFolders = adapter.getSelectedFolders() as List<RoomUnifiedFolder>
            val folderManager = FolderManager.instance
            lifecycleScope.launch {
                for (folder: RoomUnifiedFolder in selectedFolders) {
                    val databaseFolder: RoomUnifiedEmbeddedFolder =
                        downloadFileDatabase.folderDao().loadFolderById(folder.uid)
                    folderManager.removeLocalFolder(
                        fileDatabase = downloadFileDatabase,
                        folder = databaseFolder
                    )
                    adapter.refresh()
                }
            }
            NotificationManager.getInstance().showSnackbar(
                "" + selectedFolders.size + " Folder(s) deleted",
                Snackbar.LENGTH_SHORT
            )
            adapter.setMultiselect(false)
        }
    }

    private fun updateListViewVisibility(folders: List<IDisplayFolder>) {
        //binding.folderShimmerRecyclerView.hideShimmerAdapter();
        if (folders.size == 0) {
            binding!!.folderShimmerRecyclerView.visibility = View.INVISIBLE
            binding!!.fragmentFolderListText.visibility = View.VISIBLE
        } else {
            //binding.folderShimmerRecyclerView.setVisibility(View.VISIBLE);
            binding!!.fragmentFolderListText.visibility = View.INVISIBLE
        }
    }

    private fun showSortDialog() {
        val items = arrayOf<CharSequence>(
            FileGroupBy.FOLDERS.displayName,
            FileGroupBy.CATEGORIES.displayName,
            FileGroupBy.SUBJECTS.displayName
        )
        val builder = AlertDialog.Builder(
            requireContext()
        )
        builder.setTitle("Group By")
        val currentType =
            ApplicationPreferenceManager.getInstance().getFileGroupBy(FileGroupBy.FOLDERS)
        var checkedItem = -1
        checkedItem = when (currentType) {
            FileGroupBy.FOLDERS -> 0
            FileGroupBy.CATEGORIES -> 1
            FileGroupBy.SUBJECTS -> 2
            else -> 0
        }
        builder.setSingleChoiceItems(items, checkedItem
        ) { dialog, which ->
            val resultString = items[which].toString()
            val result = FileGroupBy.fromDisplayName(resultString)
            ApplicationPreferenceManager.getInstance().setFileGroupBy(result)
            viewModel.fileGrouping.value = result
            setTitle("Local "+result.displayName)
            dialog.dismiss()
        }
        val alert = builder.create()
        alert.show()
    }

    private fun navigateToFavourites() {
        val action: NavDirections = EnhancedFolderListFragmentDirections.actionEnhancedFolderListFragmentToEnhancedFavouritesViewerFragment()
        findNavController(root).navigate(action)
    }

    override fun onCreateContextMenu(menu: ContextMenu, v: View, menuInfo: ContextMenuInfo?) {
        super.onCreateContextMenu(menu, v, menuInfo)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        folderDownloaderMediator.remove(downloadObserver)
        binding = null
    }

    private fun setTitle(title: String) {
        (requireActivity() as EnhancedMainMenuActivity).setActionBarTitle(title)
    }

    private fun setActionBarColorFromInt(@ColorInt color: Int) {
        (requireActivity() as EnhancedMainMenuActivity).overrideActionBarColorFromInt(color)
    }

    private fun setActionBarColor(@ColorRes color: Int) {
        (requireActivity() as EnhancedMainMenuActivity).overrideActionBarColor(color)
    }

}

