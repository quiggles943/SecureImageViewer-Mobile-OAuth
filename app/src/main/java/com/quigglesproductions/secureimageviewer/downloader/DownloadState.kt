package com.quigglesproductions.secureimageviewer.downloader

enum class DownloadState(val title:String){
    UNKNOWN("Unknown"),
    READY("Ready"),
    RETRIEVING_DATA("Retrieving Data"),
    DOWNLOADING("Downloading"),
    COMPLETE("Complete")

}