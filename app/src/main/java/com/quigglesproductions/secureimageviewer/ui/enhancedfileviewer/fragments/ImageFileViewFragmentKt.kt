package com.quigglesproductions.secureimageviewer.ui.enhancedfileviewer.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.signature.ObjectKey
import com.quigglesproductions.secureimageviewer.R
import com.quigglesproductions.secureimageviewer.datasource.file.IFileDataSource.DataSourceCallback
import com.quigglesproductions.secureimageviewer.models.enhanced.file.IDisplayFile
import com.quigglesproductions.secureimageviewer.ui.enhancedfileviewer.EnhancedFileCollectionAdapterKt.ZoomLevelChangeCallback
import com.quigglesproductions.secureimageviewer.ui.enhancedfileviewer.NewTouchImageView
import com.quigglesproductions.secureimageviewer.ui.enhancedfileviewer.EnhancedFileViewFragmentKt

class ImageFileViewFragmentKt : BaseFileViewFragmentKt() {
    var fileName: TextView? = null
    var topLayout: LinearLayout? = null
    var imagePagerControls: LinearLayout? = null
    var zoomLevelChangeCallback: ZoomLevelChangeCallback? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.activity_imageviewer, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val parentFragment = parentFragment as EnhancedFileViewFragmentKt?
        zoomLevelChangeCallback = parentFragment!!.zoomCallback
        val file = file
        view.setOnClickListener { }
        loadImage(view, file)
        super.onViewCreated(view, savedInstanceState)
    }

    @OptIn(UnstableApi::class)
    private fun loadImage(itemView: View, item: IDisplayFile?) {
        val imageView = itemView.findViewById<View>(R.id.imageViewer) as NewTouchImageView
        imageView.setOnClickListener(View.OnClickListener {
            if (viewerNavigator == null) return@OnClickListener
            if (viewerNavigator!!.isFullyVisible) viewerNavigator!!.hide() else viewerNavigator!!.show()
        })
        imageView.maxZoom = 3.2.toFloat()
        imageView.setOnTouchListener { v, event ->
            if (zoomLevelChangeCallback != null) {
                if (imageView.currentZoom == imageView.minZoom) zoomLevelChangeCallback!!.zoomLevelChanged(
                    false
                ) else zoomLevelChangeCallback!!.zoomLevelChanged(true)
            }
            false
        }
        /*imageView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(zoomLevelChangeCallback != null) {
                    if (imageView.getCurrentZoom() == imageView.getMinZoom())
                        zoomLevelChangeCallback.zoomLevelChanged(false);
                    else
                        zoomLevelChangeCallback.zoomLevelChanged(true);
                }
                return false;
            }
        });*/try {
            val dataSource = item!!.dataSource
            val fileType = item.fileType
            val decodeFormat: DecodeFormat
            decodeFormat =
                if (fileType.hasTransparency()) DecodeFormat.PREFER_ARGB_8888 else DecodeFormat.PREFER_RGB_565
            if (dataSource == null) return
            dataSource.getFullFileDataSource(context, object : DataSourceCallback {
                override fun FileDataSourceRetrieved(dataSource: Any, exception: Exception) {}
                override fun FileThumbnailDataSourceRetrieved(
                    dataSource: Any?,
                    exception: Exception?
                ) {
                }

                override fun FileRetrievalDataSourceRetrieved(
                    fileDataSource: Any?,
                    fileThumbnailDataSource: Any?,
                    exception: Exception?
                ) {
                    Glide.with(context!!).addDefaultRequestListener(object : RequestListener<Any?> {
                        override fun onLoadFailed(
                            e: GlideException?,
                            model: Any?,
                            target: Target<Any?>,
                            isFirstResource: Boolean
                        ): Boolean {
                            Log.e("Image Load Fail", e!!.message!!)
                            e.logRootCauses("Image Load Fail")
                            return false
                        }

                        override fun onResourceReady(
                            resource: Any,
                            model: Any,
                            target: Target<Any?>?,
                            dataSource: DataSource,
                            isFirstResource: Boolean
                        ): Boolean {
                            return false
                        }
                    }).load(fileDataSource)
                        .format(decodeFormat)
                        .thumbnail(
                            Glide.with(context!!)
                                .load(fileThumbnailDataSource)
                                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                                .signature(ObjectKey(item.metadata.creationTime)) //.signature(new ObjectKey(item.getMetadata().getCreationTime()))
                                .dontTransform()
                        )
                        .dontTransform()
                        .format(decodeFormat)
                        .into(imageView)
                }
            })
        } catch (exc: Exception) {
            Log.e("Error", exc.message!!)
        }

        //imageView.setImageBitmap(BitmapFactory.decodeFile(item.getImageFile().getAbsolutePath()));
        //imageView.setImageBitmap(BitmapFactory.decodeFile(itemFolder.getItemAtPosition(position).getImageFile().getAbsolutePath()));
        //Glide.with(context).load(itemFolder.getItemAtPosition(position).getImageFile()).asBitmap().into(imageView);

        // Adding the View
    } /*private void setupControls(View view){
        View.OnClickListener prevFileClick = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO Update view to show previous file
            }
        };
        View.OnClickListener nextFileClick = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO Update view to show next file
            }
        };
        fileName = view.findViewById(R.id.file_name);
        topLayout = view.findViewById(R.id.topLinearLayout);
        //bottomLayout = view.findViewById(R.id.imageviewer_pager_layout);
        //fileViewerNavigatorImage = view.findViewById(R.id.fileviewer_navigator);
        imagePagerControls = view.findViewById(R.id.image_pager_controls);
        //setupImageNavigatorControls();


        ImageButton backButton = topLayout.findViewById(R.id.backButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });
        //imageCountText = findViewById(R.id.imagecount);
        //imageTotalText = findViewById(R.id.imagetotal);

    }*/
}
