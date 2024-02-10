package com.quigglesproductions.secureimageviewer.ui.enhancedfileviewer.kotlin

import android.os.Bundle
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.WindowManager
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.OptIn
import androidx.core.view.ActionProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.media3.common.util.UnstableApi
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.quigglesproductions.secureimageviewer.R
import com.quigglesproductions.secureimageviewer.managers.FolderManager
import com.quigglesproductions.secureimageviewer.models.enhanced.file.IDisplayFile
import com.quigglesproductions.secureimageviewer.ui.EnhancedMainMenuActivity
import com.quigglesproductions.secureimageviewer.ui.IFileViewer
import com.quigglesproductions.secureimageviewer.ui.SecureActivity
import com.quigglesproductions.secureimageviewer.ui.compoundcontrols.FileViewerNavigator
import com.quigglesproductions.secureimageviewer.ui.enhancedfolderviewer.kotlin.EnhancedFolderViewerViewModelKt
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class EnhancedFileViewFragmentKt() : Fragment(), IFileViewer {
    private var startPos = 0
    private lateinit var viewPager: ViewPager2
    lateinit var topLayout: LinearLayout
    private lateinit var backButton: ImageButton
    lateinit var fileName: TextView
    lateinit var fileNavigator: FileViewerNavigator
    var selectedFile: IDisplayFile? = null
    private var currentPagerSlopMultiplier = 0
    private var hasStartPosition: Boolean = false
    private val viewModel: EnhancedFileViewerViewModelKt by viewModels()
    private val folderViewModel by activityViewModels<EnhancedFolderViewerViewModelKt>()
    private lateinit var collectionAdapter: EnhancedFileCollectionAdapterKt<IDisplayFile>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if ((requireActivity() as EnhancedMainMenuActivity).supportActionBar != null) {
            (requireActivity() as EnhancedMainMenuActivity).supportActionBar!!.hide()
        } else {
            (requireActivity() as EnhancedMainMenuActivity).registerActionBarSetListener { (requireActivity() as EnhancedMainMenuActivity).supportActionBar!!.hide() }
        }
        val wrapper = ContextThemeWrapper(requireContext(), R.style.FileViewerTheme)
        val themedInflater = inflater.cloneInContext(wrapper)
        (activity as EnhancedMainMenuActivity?)!!.hideStatusBar()
        return themedInflater.inflate(R.layout.fragment_file_pager, container, false)
    }

    @OptIn(UnstableApi::class)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewPager = view.findViewById(R.id.fragment_view_pager)
        if (!hasStartPosition)
            startPos = EnhancedFileViewFragmentKtArgs.fromBundle(requireArguments()).startPosition
        setupNavigationControls(view)
        selectedFile = folderViewModel.selectedFile.value
        collectionAdapter = EnhancedFileCollectionAdapterKt<IDisplayFile>(this)
        collectionAdapter.setFileNavigator(fileNavigator)
        val selectedFolder = FolderManager.getInstance().currentFolder
        collectionAdapter.addFiles(folderViewModel.files.value)
        viewPager.adapter = collectionAdapter
        super.onViewCreated(view, savedInstanceState)
        hideSystemBars()
        fileNavigator!!.setFileTotal(collectionAdapter.itemCount)
        viewPager.registerOnPageChangeCallback(object : OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                //selectedFile = collectionAdapter!!.getItem(position)
                fileName!!.text = selectedFile!!.getName()
                fileNavigator!!.setFilePosition(position + 1)
                topLayout!!.invalidate()
            }
        })
        viewPager.setCurrentItem(collectionAdapter.getPosition(selectedFile), false)
        fileName!!.text = selectedFile!!.getName()
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
                val recyclerViewField = viewPager!!.javaClass.getDeclaredField("mRecyclerView")
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
        fileName = topLayout.findViewById(R.id.file_name)
        fileNavigator = view.findViewById(R.id.fileviewer_navigator)
        backButton.setOnClickListener(View.OnClickListener { requireActivity().onBackPressed() })
        fileNavigator.setNextButtonOnClickListener(View.OnClickListener {
            viewPager!!.setCurrentItem(
                viewPager!!.currentItem + 1,
                true
            )
        })
        fileNavigator.setPreviousButtonOnClickListener(View.OnClickListener {
            viewPager!!.setCurrentItem(
                viewPager!!.currentItem - 1,
                true
            )
        })
        fileNavigator.setVisibilityChangedListener(ActionProvider.VisibilityListener { isVisible ->
            if (isVisible) topLayout.setVisibility(
                View.VISIBLE
            ) else topLayout.setVisibility(View.INVISIBLE)
        })
    }

    private fun hideSystemBars() {
        val decorView = requireActivity().window.decorView
        val windowInsetsController = decorView.windowInsetsController ?: return
        // Configure the behavior of the hidden system bars
        windowInsetsController.systemBarsBehavior =
            WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        requireActivity().window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)

        //viewModel.getSystemBarsHidden().setValue(true);
    }

    private fun showSystemBars() {
        val decorView = requireActivity().window.decorView
        val windowInsetsController = decorView.windowInsetsController ?: return
        // Configure the behavior of the hidden system bars
        windowInsetsController.systemBarsBehavior = WindowInsetsController.BEHAVIOR_DEFAULT
        windowInsetsController.show(WindowInsets.Type.systemBars())
        //viewModel.getSystemBarsHidden().setValue(false);
        requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
    }

    override fun getNavigator(): FileViewerNavigator {
        return fileNavigator!!
    }
    override fun onDestroyView() {
        super.onDestroyView()
        showSystemBars()
        if ((requireActivity() as SecureActivity).supportActionBar != null) {
            (requireActivity() as SecureActivity).supportActionBar!!.show()
        }
    }

    val zoomCallback: EnhancedFileCollectionAdapterKt.ZoomLevelChangeCallback
        get() = collectionAdapter!!.zoomLevelCallback

}
