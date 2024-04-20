package com.quigglesproductions.secureimageviewer.ui.enhancedfileviewer

import android.content.Context
import android.os.Bundle
import android.view.ContextThemeWrapper
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.ToggleButton
import androidx.annotation.OptIn
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ActionProvider
import androidx.fragment.app.activityViewModels
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.util.UnstableApi
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.quigglesproductions.secureimageviewer.R
import com.quigglesproductions.secureimageviewer.managers.VideoPlaybackManager
import com.quigglesproductions.secureimageviewer.models.enhanced.file.IDisplayFile
import com.quigglesproductions.secureimageviewer.room.databases.unified.entity.relations.RoomUnifiedEmbeddedFile
import com.quigglesproductions.secureimageviewer.ui.IFileViewer
import com.quigglesproductions.secureimageviewer.ui.SecureFragment
import com.quigglesproductions.secureimageviewer.ui.compoundcontrols.FileViewerNavigator
import com.quigglesproductions.secureimageviewer.ui.enhancedfolderlist.FolderListType
import com.quigglesproductions.secureimageviewer.ui.enhancedfolderviewer.FolderViewerViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class EnhancedFileViewFragment : SecureFragment(), IFileViewer {
    private var startPos = 0
    private lateinit var viewPager: ViewPager2
    lateinit var topLayout: ConstraintLayout
    private lateinit var backButton: ImageButton
    private lateinit var optionsButton: ImageButton
    private lateinit var favouriteButton: ToggleButton
    lateinit var fileName: TextView
    @UnstableApi
    lateinit var fileNavigator: FileViewerNavigator
    private var currentPagerSlopMultiplier = 0
    private var hasStartPosition: Boolean = false
    private val viewModel: EnhancedFileViewerViewModel by activityViewModels<EnhancedFileViewerViewModel>()
    private val folderViewModel by hiltNavGraphViewModels<FolderViewerViewModel>(R.id.main_navigation)
    private lateinit var collectionAdapter: EnhancedFileCollectionAdapterKt<IDisplayFile>

    @Inject
    lateinit var videoPlaybackManager: VideoPlaybackManager

    private val args: EnhancedFileViewFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val wrapper = ContextThemeWrapper(requireContext(), R.style.FileViewerTheme)
        val themedInflater = inflater.cloneInContext(wrapper)

        return themedInflater.inflate(R.layout.fragment_file_pager, container, false)
    }

    @OptIn(UnstableApi::class)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewPager = view.findViewById(R.id.fragment_view_pager)
        if (!hasStartPosition)
            startPos = args.startPosition
        setupNavigationControls(view)
        collectionAdapter = EnhancedFileCollectionAdapterKt<IDisplayFile>(this,videoPlaybackManager,viewModel)
        collectionAdapter.setFileNavigator(fileNavigator)
        collectionAdapter.addFiles(folderViewModel.files.value)
        viewPager.adapter = collectionAdapter
        super.onViewCreated(view, savedInstanceState)
        //hideSystemBars()
        fileNavigator.setFileTotal(collectionAdapter.itemCount)
        viewPager.registerOnPageChangeCallback(object : OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                //selectedFile = collectionAdapter!!.getItem(position)
                val selectedFile = folderViewModel.files.value?.get(position)
                fileName.text = selectedFile?.name
                folderViewModel.selectedFile.value = selectedFile
                //fileName!!.text = selectedFile!!.getName()
                fileNavigator.setFilePosition(position + 1)
                topLayout.invalidate()
                favouriteButton.isChecked = selectedFile!!.file.isFavourite
            }
        })
        viewPager.setCurrentItem(collectionAdapter.getPosition(folderViewModel.selectedFile.value), false)
        fileName.text = folderViewModel.selectedFile.value!!.getName()
        viewPager.setNestedScrollingEnabled(true)
        collectionAdapter.setFileZoomLevelCallback(EnhancedFileCollectionAdapterKt.ZoomLevelChangeCallback { isZoomed ->
            if (isZoomed) setViewPagerSlop(11) else setViewPagerSlop(1)
            return@ZoomLevelChangeCallback
        })
    }

    private fun setViewPagerSlop(multiplier: Int) {
        val initialValue = 42
        try {
            if (currentPagerSlopMultiplier != multiplier) {
                val recyclerViewField = viewPager.javaClass.getDeclaredField("mRecyclerView")
                recyclerViewField.isAccessible = true
                val recyclerView = recyclerViewField[viewPager] as RecyclerView
                val field = RecyclerView::class.java.getDeclaredField("mTouchSlop")
                field.isAccessible = true
                field[recyclerView] = initialValue * multiplier
            }
        } catch (e: NoSuchFieldException) {
            e.printStackTrace()
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        } finally {
            currentPagerSlopMultiplier = multiplier
        }
    }

    @OptIn(UnstableApi::class)
    private fun setupNavigationControls(view: View) {
        topLayout = view.findViewById(R.id.topLinearLayout)
        backButton = topLayout.findViewById(R.id.backButton)
        optionsButton = topLayout.findViewById(R.id.optionsButton)
        favouriteButton = topLayout.findViewById(R.id.favouriteButton)
        fileName = topLayout.findViewById(R.id.file_title)
        fileNavigator = view.findViewById(R.id.fileviewer_navigator)
        backButton.setOnClickListener { requireActivity().onBackPressed() }
        fileNavigator.setNextButtonOnClickListener {
            viewPager.setCurrentItem(
                viewPager.currentItem + 1,
                true
            )
        }
        optionsButton.setOnClickListener(View.OnClickListener {
            val popup = PopupMenu(requireContext(),optionsButton,Gravity.END)
            popup.menuInflater.inflate(R.menu.menu_file_view,popup.menu)
            popup.setOnMenuItemClickListener {
                when(it.itemId){
                    R.id.action_info_view -> showFileInfoDialog()
                }
                return@setOnMenuItemClickListener true
            }
            popup.show()
        })
        favouriteButton.setOnCheckedChangeListener { buttonView, isChecked ->
            if(isChecked != folderViewModel.selectedFile.value!!.file.isFavourite) {
                folderViewModel.selectedFile.value!!.file.isFavourite = isChecked
                when (folderViewModel.folderListType.value) {
                    FolderListType.ONLINE -> {}
                    FolderListType.DOWNLOADED -> {
                        lifecycleScope.launch {
                            downloadFileDatabase.fileDao().update(folderViewModel.selectedFile.value!!.file)
                        }
                    }

                    else -> {}
                }
            }
        }
        fileNavigator.setPreviousButtonOnClickListener(View.OnClickListener {
            viewPager.setCurrentItem(
                viewPager.currentItem - 1,
                true
            )
        })
        fileNavigator.setVisibilityChangedListener(ActionProvider.VisibilityListener { isVisible ->
            if (isVisible) topLayout.visibility = View.VISIBLE else topLayout.visibility = View.INVISIBLE
        })
    }

    private fun showFileInfoDialog(){
        val bottomSheetDialog = BottomSheetDialog(requireContext())
        bottomSheetDialog.setContentView(R.layout.bottomdialog_fileinfo)
        bottomSheetDialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        val itemNameText = bottomSheetDialog.findViewById<TextView>(R.id.item_name)
        val folderNameText = bottomSheetDialog.findViewById<TextView>(R.id.folder_name)
        val artistNameText = bottomSheetDialog.findViewById<TextView>(R.id.artist_name)
        val catagoriesText = bottomSheetDialog.findViewById<TextView>(R.id.catagories)
        val subjectsText = bottomSheetDialog.findViewById<TextView>(R.id.subjects)
        folderViewModel.selectedFile.value!!.dataSource.getFileMetadata(requiresRequestManager()) { metadata, exception -> //selectedFile.metadata = metadata;
            itemNameText!!.text = folderViewModel.selectedFile.value!!.name
            folderNameText!!.text = if(folderViewModel.selectedFile.value!!.file.cachedFolderName != null) folderViewModel.selectedFile.value!!.file.cachedFolderName else folderViewModel.folder.value?.name
            artistNameText!!.text = folderViewModel.selectedFile.value!!.artistName
            catagoriesText!!.text = folderViewModel.selectedFile.value!!.catagoryListString
            subjectsText!!.text = folderViewModel.selectedFile.value!!.subjectListString
            bottomSheetDialog.create()
            bottomSheetDialog.show()
        }
    }

    @OptIn(UnstableApi::class)
    override fun getNavigator(): FileViewerNavigator {
        return fileNavigator
    }
    override fun onDestroyView() {
        super.onDestroyView()

    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
    }

    override fun onDetach() {
        super.onDetach()
        requiresSecureActivity().showSystemUI()
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        requiresSecureActivity().hideSystemUI()
    }

    fun updateFileFavourite(file: RoomUnifiedEmbeddedFile, state: Boolean) {
        file.file.isFavourite = state
        lifecycleScope.launch {
            downloadFileDatabase.fileDao().update(file = file.file)
        }
    }

    val zoomCallback: EnhancedFileCollectionAdapterKt.ZoomLevelChangeCallback
        get() = collectionAdapter.zoomLevelCallback

}
