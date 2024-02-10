package com.quigglesproductions.secureimageviewer.dagger.hilt.mapper

import com.quigglesproductions.secureimageviewer.datasource.file.RetrofitFileDataSource
import com.quigglesproductions.secureimageviewer.models.modular.file.ModularOnlineFile
import com.skydoves.retrofit.adapters.paging.PagingMapper

class ModularOnlineFileMapper : PagingMapper<List<ModularOnlineFile>,ModularOnlineFile> {
    override fun map(value: List<ModularOnlineFile>): List<ModularOnlineFile> {
        return value;
    }
}