package com.quigglesproductions.secureimageviewer.glide

import android.content.Context
import com.bumptech.glide.Glide
import com.bumptech.glide.GlideBuilder
import com.bumptech.glide.Registry
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.module.AppGlideModule
import com.bumptech.glide.request.RequestOptions
import com.quigglesproductions.secureimageviewer.dagger.hilt.annotations.RequestServiceClient
import dagger.hilt.EntryPoint
import dagger.hilt.EntryPoints
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import java.io.InputStream

@GlideModule
class CustomGlideModule : AppGlideModule() {
    override fun applyOptions(context: Context, builder: GlideBuilder) {
        builder.setDefaultRequestOptions(
            RequestOptions()
                .format(DecodeFormat.PREFER_RGB_565)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
        )
    }

    override fun registerComponents(context: Context, glide: Glide, registry: Registry) {
        super.registerComponents(context, glide, registry)
        val entryPoint = EntryPoints.get(
            context,
            GlideModuleEntryPoint::class.java
        )
        val client = entryPoint.provideOkHttpClient()
        val factory = OkHttpUrlLoader.Factory(client)
        registry.replace(GlideUrl::class.java,InputStream::class.java, factory)
    }

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    internal interface GlideModuleEntryPoint {
        @RequestServiceClient
        fun provideOkHttpClient(): OkHttpClient
    }
}
