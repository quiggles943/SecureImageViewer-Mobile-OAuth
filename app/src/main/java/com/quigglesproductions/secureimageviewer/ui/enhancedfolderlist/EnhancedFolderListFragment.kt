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
import android.widget.AdapterView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.appcompat.app.AlertDialog
import androidx.core.view.MenuProvider
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavDirections
import androidx.navigation.Navigation.findNavController
import androidx.paging.PagingData
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.switchmaterial.SwitchMaterial
import com.quigglesproductions.secureimageviewer.R
import com.quigglesproductions.secureimageviewer.databinding.FragmentFolderListBinding
import com.quigglesproductions.secureimageviewer.downloader.FolderDownloadWorker
import com.quigglesproductions.secureimageviewer.enums.FileGroupBy
import com.quigglesproductions.secureimageviewer.managers.ApplicationPreferenceManager
import com.quigglesproductions.secureimageviewer.managers.FolderManager
import com.quigglesproductions.secureimageviewer.managers.NotificationManager
import com.quigglesproductions.secureimageviewer.managers.ViewerConnectivityManager
import com.quigglesproductions.secureimageviewer.models.enhanced.folder.IDisplayFolder
import com.quigglesproductions.secureimageviewer.observable.IFolderDownloadObserver
import com.quigglesproductions.secureimageviewer.recycler.RecyclerViewSelectionMode
import com.quigglesproductions.secureimageviewer.room.databases.unified.entity.RoomUnifiedFolder
import com.quigglesproductions.secureimageviewer.room.databases.unified.entity.relations.RoomUnifiedEmbeddedFolder
import com.quigglesproductions.secureimageviewer.ui.EnhancedMainMenuActivity
import com.quigglesproductions.secureimageviewer.ui.SecureFragment
import com.quigglesproductions.secureimageviewer.ui.adapter.loadstate.MyLoadStateAdapter
import com.quigglesproductions.secureimageviewer.ui.overview.OverviewViewModel
import com.quigglesproductions.secureimageviewer.utils.ObjectUtils
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject


class EnhancedFolderListFragment : SecureFragment() {
    private var binding: FragmentFolderListBinding? = null
    private var myMenu: Menu? = null
    private lateinit var recyclerView: RecyclerView
    private val viewModel by activityViewModels<EnhancedFolderListViewModel>()
    private lateinit var swipeLayout: SwipeRefreshLayout
    @Inject
    lateinit var adapter: FolderListAdapter
    lateinit var root:View
    private var folderLoadJob: Job? = null

    private lateinit var downloadObserver: IFolderDownloadObserver
    private val overviewViewModel by activityViewModels<OverviewViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        setHasOptionsMenu(true)
        binding = FragmentFolderListBinding.inflate(inflater, container, false)
        root = binding!!.root
        if(getIsOnline()) {
            if(viewModel.folderListType.value != null){

            }
            else {
                viewModel.folderListType.value = FolderListType.ONLINE
            }
        }
        else
            viewModel.folderListType.value = FolderListType.DOWNLOADED

        /*if(viewModel.folderListType.value == FolderListType.DOWNLOADED) {
            viewModel.fileGrouping.value = ApplicationPreferenceManager.getInstance().getFileGroupBy(FileGroupBy.FOLDERS)
            setTitle("Local " + viewModel.fileGrouping.value!!.displayName)
        }*/

        recyclerView = binding!!.folderShimmerRecyclerView
        recyclerView.setHasFixedSize(true)
        recyclerView.itemAnimator = null
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
        setupMenu()
        viewModel.folderListType.observe(viewLifecycleOwner){
            collectUiState()
        }

        return root
    }

    private fun setupMenu(){
        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_unified_folder_list,menu)
                val item = menu.findItem(R.id.unified_file_location_toggle)
                val switch: SwitchMaterial? = item.actionView?.findViewById(R.id.switchMaterial)
                if(switch != null){
                    switch.isChecked = viewModel.folderListType.value == FolderListType.DOWNLOADED
                    if(!getIsOnline())
                        switch.isEnabled = false
                    switch.setOnCheckedChangeListener { _, isChecked ->
                        if(isChecked)
                            viewModel.folderListType.value = FolderListType.DOWNLOADED
                        else
                            viewModel.folderListType.value = FolderListType.ONLINE
                    }
                }
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                when (menuItem.itemId) {
                    R.id.unified_folder_recent_files ->{
                        val action = EnhancedFolderListFragmentDirections.actionEnhancedFolderListFragmentToEnhancedRecentFileViewerFragment()
                        findNavController(binding!!.root).navigate(action)
                    }

                    R.id.online_folder_download_viewer -> findNavController(binding!!.root).navigate(R.id.action_nav_enhancedFolderListFragment_to_downloadViewerFragment)

                    //R.id.offline_folder_delete -> deleteSelectedFolders()

                    R.id.offline_folder_sort_type -> {
                        showSortDialog()
                        return false
                    }
                    R.id.offline_folder_favourites -> navigateToFavourites()

                    else -> return false
                }
                return true
            }
        }, viewLifecycleOwner)
    }

    private fun getIsOnline():Boolean{
        return connectivityManager.isConnected
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        collectUiState()
    }

    private fun collectUiState() {
        viewModel.fileGrouping.observe(viewLifecycleOwner){
            folderLoadJob?.cancel()
            folderLoadJob = viewLifecycleOwner.lifecycleScope.launch {
                viewModel.createPagedSource(it)
                viewModel.pagedFolders!!.cancellable().collect { files ->
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
        adapter.setOnClickListener {position ->
            val value = adapter.peek(position)

            if (value != null) {
                navigateToFolder(value)
            }
        }
        adapter.setOnCreateOptionsMenuListener { menu, v, menuInfo ->
            menu!!.setHeaderTitle("Options")
            val cmi = menuInfo as AdapterView.AdapterContextMenuInfo
            menu.add(CONTEXTMENU_INFO, cmi.position, 0, "Info")
            val selectedFolder = (adapter.peek(cmi.position) as RoomUnifiedFolder)
            if(selectedFolder.isAvailableOffline){
                menu.add(
                    CONTEXTMENU_DELETE_FOLDER,
                    cmi.position,
                    1,
                    "Delete Folder")
            }
            else{
                menu.add(
                    CONTEXTMENU_DOWNLOAD_FOLDER,
                    cmi.position,
                    0,
                    "Download Folder")
            }
            /*if(viewModel.folderListType.value == FolderListType.ONLINE)
                menu.add(
                    CONTEXTMENU_DOWNLOAD_FOLDER,
                    cmi.position,
                    0,
                    "Download Folder")
            else
                menu.add(
                    CONTEXTMENU_DELETE_FOLDER,
                    cmi.position,
                    1,
                    "Delete Folder")*/
        }
        adapter.stateRestorationPolicy =
            RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY
        //recyclerView.addItemDecoration(new RecyclerViewMargin(0,columnCount));
        recyclerView.adapter = adapter.withLoadStateFooter(MyLoadStateAdapter(adapter::retry))
        registerForContextMenu((recyclerView))
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        val selectedFolder = (adapter.peek(item.itemId) as RoomUnifiedFolder)
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
                itemNameText!!.text = selectedFolder.name
                bottomSheetDialog.create()
                bottomSheetDialog.show()
            }

            CONTEXTMENU_DOWNLOAD_FOLDER -> {
                viewLifecycleOwner.lifecycleScope.launch {
                    val downloadFolder = ObjectUtils.createDeepCopy(selectedFolder)
                    downloadFolder.isAvailable = false
                    val folderId = downloadFileDatabase.folderDao().insert(downloadFolder)
                    val inputData = Data.Builder().putLong("folderId", folderId).build()
                    val downloadWorkRequest: OneTimeWorkRequest =
                        OneTimeWorkRequestBuilder<FolderDownloadWorker>()
                            .setInputData(inputData)
                            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                            .build()
                    folderDownloaderMediator.enqueueFolderDownload(
                        selectedFolder,
                        downloadWorkRequest
                    )
                }
            }

            CONTEXTMENU_DELETE_FOLDER -> {
                lifecycleScope.launch {
                    if(selectedFolder.id != null) {
                        val databaseFolder: RoomUnifiedEmbeddedFolder =
                            downloadFileDatabase.folderDao().loadFolderByOnlineId(selectedFolder.onlineId.toLong())
                        runBlocking {
                            folderManager.removeLocalFolder(
                                fileDatabase = downloadFileDatabase,
                                folder = databaseFolder
                            )
                        }
                    }
                    adapter.refresh()
                }
            }
        }
        return true
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
                    NotificationManager.getInstance().showSnackbar("Unable to access folder",Snackbar.LENGTH_SHORT)
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

    /*private fun deleteSelectedFolders(){
        if(ApplicationPreferenceManager.getInstance().getFileGroupBy(FileGroupBy.FOLDERS) == FileGroupBy.FOLDERS) {
            //val selectedFolders = adapter.getSelectedFolders() as List<RoomUnifiedFolder>
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
        }
    }*/

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
    companion object {
        const val CONTEXTMENU_INFO = 128906
        const val CONTEXTMENU_DOWNLOAD_FOLDER = 128907
        const val CONTEXTMENU_DELETE_FOLDER = 128908
    }
}

