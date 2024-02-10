package com.quigglesproductions.secureimageviewer.paging.source

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.paging.ListenableFuturePagingSource
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.google.common.base.Function
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import com.quigglesproductions.secureimageviewer.SortType
import com.quigglesproductions.secureimageviewer.models.enhanced.file.IDisplayFile
import com.quigglesproductions.secureimageviewer.models.modular.file.ModularOnlineFile
import com.quigglesproductions.secureimageviewer.models.modular.folder.ModularOnlineFolder
import com.quigglesproductions.secureimageviewer.retrofit.ModularRequestService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.runBlocking
import retrofit2.Callback
import retrofit2.Response
import retrofit2.awaitResponse
import java.io.IOException
import java.util.concurrent.Executor
import java.util.stream.Collectors
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class OnlineFolderFilePagingSource (
    private var requestService: ModularRequestService,
    var folderId : Int
) : PagingSource<Int, ModularOnlineFile>() {

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    override suspend fun load(
        params: LoadParams<Int>
    ): LoadResult<Int, ModularOnlineFile> {
        val nextPageNumber = params.key ?: 1
        return try{
            val response = requestService.doGetFolderPaginatedFiles(folderId,nextPageNumber,params.loadSize,true,SortType.NAME_ASC.name)!!.awaitResponse()
            //var response = requestService.doGetFolderPaginatedFiles(folderId,nextPageNumber,true,SortType.NAME_ASC.name)!!.execute()
            //val response = backend.searchUsers(query, nextPageNumber)
            val responseBody : List<ModularOnlineFile> = response!!.body() as List<ModularOnlineFile>
            if(responseBody.size > 0) {
                return LoadResult.Page(
                    data = responseBody,
                    prevKey = null, // Only paging forward.
                    nextKey = nextPageNumber + 1,
                )
            }
            else{
                return LoadResult.Page(
                    data = responseBody,
                    prevKey = null, // Only paging forward.
                    nextKey = null
                )
            }
        }
        catch (exception : Exception){
            return LoadResult.Error(exception)
        }
        /*try {
            // Start refresh at page 1 if undefined.

            var response = requestService.doGetFolderPaginatedFiles(folderId,nextPageNumber,true,SortType.NAME_ASC.name)!!.execute()
            //val response = backend.searchUsers(query, nextPageNumber)
            val responseBody : List<ModularOnlineFile> = response!!.body() as List<ModularOnlineFile>
            if (response != null) {
                return LoadResult.Page(
                    data = responseBody,
                    prevKey = null, // Only paging forward.
                    nextKey = nextPageNumber+1
                )
            }
            else
                return LoadResult.Error(Exception())
        } catch (e: Exception) {
            // Handle errors in this block and return LoadResult.Error for
            // expected errors (such as a network failure).
            return LoadResult.Error(e)
        }*/
    }

    override fun getRefreshKey(state: PagingState<Int, ModularOnlineFile>): Int? {
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
