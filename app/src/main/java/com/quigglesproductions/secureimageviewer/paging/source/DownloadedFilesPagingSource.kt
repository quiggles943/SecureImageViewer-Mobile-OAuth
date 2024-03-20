package com.quigglesproductions.secureimageviewer.paging.source

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.quigglesproductions.secureimageviewer.room.databases.unified.UnifiedFileDatabase
import com.quigglesproductions.secureimageviewer.room.databases.unified.entity.relations.RoomUnifiedEmbeddedFile
import com.quigglesproductions.secureimageviewer.room.enums.FileSortType

class DownloadedFilesPagingSource(
    private var database: UnifiedFileDatabase,
    private val folderId: Long,
    val sortType: FileSortType
) : PagingSource<Int, RoomUnifiedEmbeddedFile>() {
    val fileDao = database.fileDao()
    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    override suspend fun load(
        params: LoadParams<Int>
    ): LoadResult<Int, RoomUnifiedEmbeddedFile> {
        val pageNumber = params.key ?: 0
        val offset = pageNumber * params.loadSize
        return try{
            val files = fileDao.getFiles(folderId,offset,params.loadSize,sortType)
            val nextPageNumber: Int?
            if(files.isEmpty() || files.size < params.loadSize)
                nextPageNumber = null
            else
                nextPageNumber = pageNumber+1
            LoadResult.Page(
                data = files,
                prevKey = null,
                nextKey = nextPageNumber
            )
        }
        catch (exception : Exception){
            return LoadResult.Error(exception)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, RoomUnifiedEmbeddedFile>): Int? {
        // Try to find the page key of the closest page to anchorPosition from
        // either the prevKey or the nextKey; you need to handle nullability
        // here.
        //  * prevKey == null -> anchorPage is the first page.
        //  * nextKey == null -> anchorPage is the last page.
        //  * both prevKey and nextKey are null -> anchorPage is the
        //    initial page, so return null.
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }


}
