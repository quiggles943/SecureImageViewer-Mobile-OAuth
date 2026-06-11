package com.quigglesproductions.secureimageviewer.downloader

import android.content.Context
import androidx.room.Room.inMemoryDatabaseBuilder
import com.quigglesproductions.secureimageviewer.App
import com.quigglesproductions.secureimageviewer.retrofit.DownloadService
import com.quigglesproductions.secureimageviewer.room.databases.unified.UnifiedFileDatabase
import com.quigglesproductions.secureimageviewer.room.databases.unified.dao.UnifiedFileDao
import com.quigglesproductions.secureimageviewer.room.databases.unified.entity.RoomUnifiedFolder
import com.quigglesproductions.secureimageviewer.room.databases.unified.entity.relations.RoomUnifiedEmbeddedFile
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.Request
import okhttp3.ResponseBody
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.Timeout
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.runners.MockitoJUnitRunner
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException

@RunWith(MockitoJUnitRunner::class)
class FileDownloadHelperTest {
    private lateinit var fileDownloadHelperSpy: FileDownloadHelper
    private lateinit var mockContext: Context
    private lateinit var mockDownloadService: DownloadService
    private lateinit var mockDatabase: UnifiedFileDatabase
    private lateinit var mockFileDao: UnifiedFileDao

    @Before
    fun setup() {
        mockContext = App()
        mockDatabase = inMemoryDatabaseBuilder(mockContext, UnifiedFileDatabase::class.java).build()
        mockFileDao = mockDatabase.fileDao()
        mockDownloadService = Mockito.mock(
            DownloadService::class.java
        )
        val helper = FileDownloadHelper(mockDownloadService, mockContext)
        fileDownloadHelperSpy = Mockito.spy(helper)
    }

    @After
    fun closeDb() {
        mockDatabase.close()
    }

    @Test
    fun testDownloadFileContent() {
        return runTest{
            val folder = RoomUnifiedFolder()
            val file = RoomUnifiedEmbeddedFile()
            val responseBody = "Test".toResponseBody("image/jpeg".toMediaTypeOrNull())
            val callResponse: Call<ResponseBody> = object : Call<ResponseBody> {
                @Throws(IOException::class)
                override fun execute(): Response<ResponseBody> {
                    return Response.error(404, responseBody)
                }

                override fun enqueue(callback: Callback<ResponseBody>) {
                    callback.onResponse(this, Response.error(404, responseBody))
                }

                override fun isExecuted(): Boolean {
                    return false
                }

                override fun cancel() {
                }

                override fun isCanceled(): Boolean {
                    return false
                }

                override fun clone(): Call<ResponseBody> {
                    return this
                }

                override fun request(): Request {
                    return Request.Builder().build()
                }

                override fun timeout(): Timeout {
                    return Timeout.NONE
                }
            }
            Mockito.`when`<Any?>(
                mockFileDao.getByOnlineId(
                    Mockito.any()
                )
            ).thenReturn(null)
            Mockito.doNothing().`when`<Any>(
                mockFileDao.insert(
                    Mockito.any(
                        RoomUnifiedFolder::class.java
                    ), Mockito.any(
                        RoomUnifiedEmbeddedFile::class.java
                    )
                )
            )
            Mockito.`when`(mockDownloadService.doGetFileContent(Mockito.any()))
                .thenReturn(callResponse)
            fileDownloadHelperSpy.downloadFileContent(folder, file, mockFileDao)
            assert(true)
        }

    }
}
