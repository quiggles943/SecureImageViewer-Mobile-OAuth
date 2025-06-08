package com.quigglesproductions.secureimageviewer.ui.preferences

import android.content.Context
import android.os.Bundle
import android.view.MenuItem
import android.widget.TextView
import androidx.preference.Preference
import com.google.android.material.snackbar.Snackbar
import com.quigglesproductions.secureimageviewer.R
import com.quigglesproductions.secureimageviewer.managers.FolderManager
import com.quigglesproductions.secureimageviewer.managers.NotificationManager
import com.quigglesproductions.secureimageviewer.base.activity.SecureActivity
import com.quigglesproductions.secureimageviewer.ui.SecurePreferenceFragmentCompat
import kotlinx.coroutines.runBlocking
import java.io.File

class StorageSettingsActivity : SecureActivity() {
    private var context: Context? = null
    var fileCountString: TextView? = null
    var folderCountString: TextView? = null
    var storageUsedString: TextView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        context = this

        setContentView(R.layout.activity_settings_storage)
        fileCountString = findViewById(R.id.storage_file_count)
        folderCountString = findViewById(R.id.storage_folder_count)
        storageUsedString = findViewById(R.id.storage_size_used)
        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(
                    R.id.settings_storage,
                    SettingsFragment(object : SettingsFragment.StorageInformationUpdatedCallback {
                        override fun informationUpdated() {
                            getStorageInfo()
                        }
                    },folderManager)
                )
                .commit()
        }
        val actionBar = supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)
        getStorageInfo()
    }

    fun getStorageInfo(): Unit
        {
            backgroundThreadPoster.post {
                val storageUsedByte = getFolderSize(context!!.filesDir)
                val storageUsedMb = storageUsedByte / 1024 / 1024
                val folderCount =
                    downloadFileDatabase.folderDao().folders.size.toLong()
                val fileCount = downloadFileDatabase.fileDao().files.size.toLong()
                uiThreadPoster.post {
                    fileCountString!!.text = fileCount.toString()
                    folderCountString!!.text = folderCount.toString()
                    storageUsedString!!.text = storageUsedMb.toString() + "Mb"
                }
            }
        }

    class SettingsFragment(var callback: StorageInformationUpdatedCallback?, private val folderManager: FolderManager) :
        SecurePreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.storage_preferences, rootKey)
            val resetPreference = preferenceManager.findPreference<Preference>("file_reset")
            resetPreference!!.onPreferenceClickListener =
                Preference.OnPreferenceClickListener {
                    Thread {
                        runBlocking {
                            folderManager.removeAllFolders(downloadFileDatabase)
                        }
                        downloadFileDatabase.clearAllTables()
                        recordDatabase.clearAllTables()
                        //getRecordDatabase().downloadRecordDao().archiveAll();
                        NotificationManager.getInstance()
                            .showSnackbar("All folders removed", Snackbar.LENGTH_SHORT)
                        if (callback != null) callback!!.informationUpdated()
                    }.start()
                    //DatabaseHandler.getInstance().clearFiles();
                    true
                }
        }

        interface StorageInformationUpdatedCallback {
            fun informationUpdated()
        }
    }

    private fun getFolderSize(file: File?): Long {
        if (file == null || !file.exists()) return 0
        var size: Long = 0
        if (file.isDirectory) {
            val files = file.listFiles()
            if (files == null || files.size == 0) return size
            for (f in files) size += getFolderSize(f)
        } else size += file.length()
        return size
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                //NavUtils.navigateUpFromSameTask(this);
                return true
            }

            else -> return super.onOptionsItemSelected(item)
        }
    }
}