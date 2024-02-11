package com.quigglesproductions.secureimageviewer.paging.source

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.quigglesproductions.secureimageviewer.SortType
import com.quigglesproductions.secureimageviewer.models.modular.file.ModularOnlineFile
import com.quigglesproductions.secureimageviewer.room.databases.paging.file.PagingFileDatabase
import com.quigglesproductions.secureimageviewer.room.databases.paging.file.entity.RoomPagingFolder
import retrofit2.awaitResponse

class DownloadedFoldersPagingSource (
    private var database: PagingFileDatabase,
    var folderId : Int
) : PagingSource<Int, RoomPagingFolder>() {
    val folderDao = database.folderDao()
    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    override suspend fun load(
        params: LoadParams<Int>
    ): LoadResult<Int, RoomPagingFolder> {
        val nextPageNumber = params.key ?: 1
        return try{
            val folders = folderDao!!.folders
            return LoadResult.Page(
                data = folders,
                prevKey = null,
                nextKey = nextPageNumber+1
            )
        }
        catch (exception : Exception){
            return LoadResult.Error(exception)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, RoomPagingFolder>): Int? {
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
