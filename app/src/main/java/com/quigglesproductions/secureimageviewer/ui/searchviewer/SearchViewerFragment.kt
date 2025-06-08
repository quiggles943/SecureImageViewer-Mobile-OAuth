package com.quigglesproductions.secureimageviewer.ui.searchviewer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.view.MenuProvider
import androidx.fragment.app.viewModels
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.switchmaterial.SwitchMaterial
import com.quigglesproductions.secureimageviewer.R
import com.quigglesproductions.secureimageviewer.databinding.FragmentSearchViewBinding
import com.quigglesproductions.secureimageviewer.models.modular.ModularSearchItem
import com.quigglesproductions.secureimageviewer.room.databases.unified.entity.RoomUnifiedSearchItem
import com.quigglesproductions.secureimageviewer.room.databases.unified.entity.relations.RoomUnifiedEmbeddedFile
import com.quigglesproductions.secureimageviewer.ui.EnhancedMainMenuActivity
import com.quigglesproductions.secureimageviewer.base.fragment.SecureFragment
import com.quigglesproductions.secureimageviewer.ui.adapter.filelist.EnhancedFileListRecyclerAdapter
import com.quigglesproductions.secureimageviewer.ui.enhancedfolderlist.FolderListType
import com.quigglesproductions.secureimageviewer.ui.enhancedfolderviewer.FolderViewerViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import retrofit2.awaitResponse
import javax.inject.Inject


@AndroidEntryPoint
open class SearchViewerFragment : SecureFragment() {
    private lateinit var binding: FragmentSearchViewBinding
    private val viewModel by viewModels<SearchViewerViewModel>()
    private lateinit var root: View
    private val folderViewModel by hiltNavGraphViewModels<FolderViewerViewModel>(R.id.main_navigation)

    @Inject
    lateinit var adapter: SearchItemListAdapter

    private lateinit var fileAdapter: EnhancedFileListRecyclerAdapter<RoomUnifiedEmbeddedFile>

    private lateinit var autoCompleteAdapter: SearchItemAutoCompleteAdapter
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSearchViewBinding.inflate(inflater, container, false)
        root = binding.root
        val columnCount = resources.getInteger(R.integer.column_count_filelist)
        val layoutManager = GridLayoutManager(context, columnCount)

        if(connectivityManager.isConnected) {
            if(viewModel.folderListType.value == null){
                viewModel.folderListType.value = FolderListType.ONLINE
            }
        }
        else
            viewModel.folderListType.value = FolderListType.DOWNLOADED

        binding.fileRecyclerview.layoutManager = layoutManager
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        setupMenu()
        setupObservers()
        getSearchTerms()
    }

    private fun initView() {
        val layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        binding.selectedSearchitemRecyclerview.layoutManager = layoutManager
        binding.selectedSearchitemRecyclerview.setAdapter(adapter)

        autoCompleteAdapter = SearchItemAutoCompleteAdapter(requireContext(), android.R.layout.simple_list_item_1)
        adapter.setOnRemoveTagClickListener { position ->
            adapter.removeItem(position)
        }
        binding.autoCompleteTextView.setAdapter(autoCompleteAdapter)
        binding.autoCompleteTextView.setOnItemClickListener { parent, view, position, id ->
            val item = parent.getItemAtPosition(position) as RoomUnifiedSearchItem
            adapter.addItem(item)
            binding.autoCompleteTextView.text = null
        }

        fileAdapter = EnhancedFileListRecyclerAdapter(requireContext())
        binding.fileRecyclerview.adapter = fileAdapter
        
        fileAdapter.setOnClickListener { position ->
            folderViewModel.selectedFile.value = fileAdapter.getItem(position)
            folderViewModel.files.value = fileAdapter.getFiles()
            val navigation = SearchViewerFragmentDirections.actionNavSearchFragmentToNavEnhancedFileViewFragment(position)
            Navigation.findNavController(getRootView()).navigate(navigation)

        }

        binding.searchBtn.setOnClickListener {
            binding.autoCompleteTextView.isEnabled = false
            binding.searchBtn.isEnabled = false
            binding.autoCompleteTextView.text = null
            viewLifecycleOwner.lifecycleScope.launch {
                when (viewModel.folderListType.value) {
                    FolderListType.DOWNLOADED -> searchDevice()
                    FolderListType.ONLINE -> searchOnline()
                    null -> throw IllegalStateException("Device online state not valid")
                }
                binding.autoCompleteTextView.isEnabled = true
                binding.searchBtn.isEnabled = true
            }

        }


    }

    private fun setupObservers(){
        viewModel.possibleSearchItems.observe(viewLifecycleOwner){searchItems ->
            autoCompleteAdapter.setSearchItems(searchItems)
        }
        viewModel.searchedTerms.observe(viewLifecycleOwner){
            adapter.updateItems(it)
        }

        viewModel.searchedFiles.observe(viewLifecycleOwner){searchedFiles ->
            fileAdapter.setFiles(searchedFiles.toMutableList())
        }
        viewModel.folderListType.observe(viewLifecycleOwner){listType ->
            viewLifecycleOwner.lifecycleScope.launch {
                adapter.clearItems()

                binding.autoCompleteTextView.isEnabled = false
                binding.searchBtn.isEnabled = false
                binding.autoCompleteTextView.text = null
                when (viewModel.folderListType.value) {
                    FolderListType.DOWNLOADED -> getSearchTermsDevice()
                    FolderListType.ONLINE -> getSearchTermsOnline()
                    null -> throw IllegalStateException("Device online state not valid")
                }
                binding.autoCompleteTextView.isEnabled = true
                binding.searchBtn.isEnabled = true
            }
        }
    }

    private fun getSearchTerms(){
        viewLifecycleOwner.lifecycleScope.launch {
            when (viewModel.folderListType.value) {
                FolderListType.DOWNLOADED -> getSearchTermsDevice()
                FolderListType.ONLINE -> getSearchTermsOnline()
                null -> throw IllegalStateException("Device online state not valid")
            }
        }

    }

    private suspend fun getSearchTermsDevice(){
        viewModel.possibleSearchItems.value = downloadFileDatabase.fileDao().getSearchItems()
    }

    private suspend fun getSearchTermsOnline(){

        val response = requestService.doGetSearchTerms().awaitResponse()
        if(response.isSuccessful) {
            val searchItemList: ArrayList<RoomUnifiedSearchItem> = ArrayList()
            for(searchTerm in response.body()!!){
                val deviceSearchTerm: RoomUnifiedSearchItem = RoomUnifiedSearchItem.getFromOnlineSearchTerm(searchTerm)
                searchItemList.add(deviceSearchTerm)
            }
            viewModel.possibleSearchItems.value = searchItemList
        }

    }

    private fun setupMenu(){
        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_search,menu)
                val item = menu.findItem(R.id.unified_file_location_toggle)
                val switch: SwitchMaterial? = item.actionView?.findViewById(R.id.switchMaterial)
                if(switch != null){
                    switch.isChecked = viewModel.folderListType.value == FolderListType.DOWNLOADED
                    if(!connectivityManager.isConnected)
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
                    R.id.offline_folder_sort_type -> {
                        //showSortDialog()
                        return false
                    }

                    else -> return false
                }
                //return true
            }
        }, viewLifecycleOwner)
    }

    private suspend fun searchOnline(){

        val deviceSearchItems = adapter.getItems()
        val onlineSearchItems = ArrayList<ModularSearchItem>()
        for(deviceSearchItem in deviceSearchItems)
            onlineSearchItems.add(ModularSearchItem.fromDeviceSearchItem(deviceSearchItem))
        val response = requestService.doGetSearchFiles(onlineSearchItems).awaitResponse()
        if(response.isSuccessful)
        {
            val files = response.body()
            val deviceFiles = ArrayList<RoomUnifiedEmbeddedFile>()
            if (files != null) {
                for(file in files){
                    val deviceFile = RoomUnifiedEmbeddedFile.Creator().loadFromOnlineFile(file).build()
                    deviceFiles.add(deviceFile)
                }
                viewModel.searchedFiles.value = deviceFiles
            }
        }

    }

    private suspend fun searchDevice(){
        //TODO search for matching files on device
    }

    /**
     * Updates the UI using the data in the viewmodel
     */
    /*fun setUiState(){
        viewLifecycleOwner.lifecycleScope.launch {
            folderViewModel.generatePagedSource(folderViewModel.folder.value!!.fileGroupingType)
            folderViewModel.getPagedDataSource()?.collect{ files ->
                adapter.submitData(files)
            }

        }
    }*/

    /*fun showSortDialog() {
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
