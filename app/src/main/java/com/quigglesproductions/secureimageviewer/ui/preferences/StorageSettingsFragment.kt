package com.quigglesproductions.secureimageviewer.ui.preferences

import android.os.Bundle
import androidx.preference.Preference
import com.google.android.material.snackbar.Snackbar
import com.quigglesproductions.secureimageviewer.R
import com.quigglesproductions.secureimageviewer.managers.NotificationManager
import com.quigglesproductions.secureimageviewer.ui.SecurePreferenceFragmentCompat
import com.techyourchance.threadposter.BackgroundThreadPoster
import com.techyourchance.threadposter.UiThreadPoster
import kotlinx.coroutines.runBlocking
import java.io.File

class StorageSettingsFragment : SecurePreferenceFragmentCompat() {

    private lateinit var filesOnDevicePreference: Preference
    private lateinit var foldersOnDevicePreference: Preference
    private lateinit var deviceStorageUsedPreference: Preference

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.storage_preferences, rootKey)

        val fileResetPreference = preferenceManager.findPreference<Preference>("file_reset")!!
        fileResetPreference.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            secureActivity.backgroundThreadPoster.post {
                runBlocking {
                    secureActivity.folderManager.removeAllFolders(downloadFileDatabase)
                }
                downloadFileDatabase.clearAllTables()
                recordDatabase.clearAllTables()
                //getRecordDatabase().downloadRecordDao().archiveAll();
                NotificationManager.getInstance()
                    .showSnackbar("All folders removed", Snackbar.LENGTH_SHORT)
                secureActivity.uiThreadPoster.post{
                    getStorageInfo()
                }


            }
            true
        }

        filesOnDevicePreference = preferenceManager.findPreference("files_on_device")!!
        filesOnDevicePreference.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            true
        }
        foldersOnDevicePreference = preferenceManager.findPreference("folders_on_device")!!
        foldersOnDevicePreference.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            true
        }
        deviceStorageUsedPreference = preferenceManager.findPreference("device_storage_used")!!
        deviceStorageUsedPreference.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            true
        }
        getStorageInfo()
    }

    private fun getStorageInfo(): Unit
    {
        secureActivity.backgroundThreadPoster.post {
            val storageUsedByte = getFolderSize(requireContext().filesDir)
            val storageUsedMb = storageUsedByte / 1024 / 1024
            val folderCount =
                downloadFileDatabase.folderDao().folders.size.toLong()
            val fileCount = downloadFileDatabase.fileDao().files.size.toLong()
            secureActivity.uiThreadPoster.post {
                filesOnDevicePreference.summary = fileCount.toString()
                foldersOnDevicePreference.summary = folderCount.toString()
                deviceStorageUsedPreference.summary = storageUsedMb.toString()+"Mb"
            }
        }
    }

    private fun getFolderSize(file: File?): Long {
        if (file == null || !file.exists()) return 0
        var size: Long = 0
        if (file.isDirectory) {
            val files = file.listFiles()
            if (files == null || files.isEmpty()) return size
            for (f in files) size += getFolderSize(f)
        } else size += file.length()
        return size
    }
}