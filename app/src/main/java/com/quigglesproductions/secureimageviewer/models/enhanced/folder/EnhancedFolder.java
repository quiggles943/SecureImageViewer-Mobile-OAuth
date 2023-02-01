package com.quigglesproductions.secureimageviewer.models.enhanced.folder;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.google.gson.annotations.SerializedName;
import com.quigglesproductions.secureimageviewer.models.enhanced.datasource.IFileDataSource;
import com.quigglesproductions.secureimageviewer.models.enhanced.datasource.IFolderDataSource;
import com.quigglesproductions.secureimageviewer.models.enhanced.file.EnhancedFile;
import com.quigglesproductions.secureimageviewer.models.folder.FolderModel;

import java.time.LocalDateTime;
import java.util.ArrayList;

public class EnhancedFolder  implements Parcelable {

    @SerializedName("Id")
    public int onlineId;
    @SerializedName("EncodedName")
    public String encodedName;
    @SerializedName("NormalName")
    public String normalName;
    @SerializedName("Uri")
    public String onlineUri;
    @SerializedName("LastAccessTime")
    public LocalDateTime onlineAccessTime;
    @SerializedName("ThumbnailId")
    public int onlineThumbnailId;
    @SerializedName("DefaultArtistId")
    public int defaultOnlineArtistId;
    @SerializedName("DefaultSubjectId")
    public int defaultOnlineSubjectId;

    private Status status;

    private IFolderDataSource dataSource;

    public boolean isDownloading = false;

    public EnhancedFolder(){

    }

    protected EnhancedFolder(Parcel in) {
        onlineId = in.readInt();
        encodedName = in.readString();
        normalName = in.readString();
        onlineUri = in.readString();
        onlineThumbnailId = in.readInt();
        defaultOnlineArtistId = in.readInt();
        defaultOnlineSubjectId = in.readInt();
        isDownloading = in.readByte() != 0;
    }

    public static final Creator<EnhancedFolder> CREATOR = new Creator<EnhancedFolder>() {
        @Override
        public EnhancedFolder createFromParcel(Parcel in) {
            return new EnhancedFolder(in);
        }

        @Override
        public EnhancedFolder[] newArray(int size) {
            return new EnhancedFolder[size];
        }
    };

    public String getName() {
        return normalName;
    }

    public int getOnlineId() {
        return onlineId;
    }




    public Status getStatus() {
        return this.status;
    }
    public void setStatus(Status status){
        this.status = status;
    }
    public boolean getIsDownloading(){
        if(status == Status.DOWNLOADING)
            return true;
        else
            return false;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel parcel, int i) {
        parcel.writeInt(onlineId);
        parcel.writeString(encodedName);
        parcel.writeString(normalName);
        parcel.writeString(onlineUri);
        parcel.writeInt(onlineThumbnailId);
        parcel.writeInt(defaultOnlineArtistId);
        parcel.writeInt(defaultOnlineSubjectId);
        parcel.writeByte((byte) (isDownloading ? 1 : 0));
    }

    public void clearItems() {

    }

    public void setDataSource(IFolderDataSource dataSource){
        this.dataSource = dataSource;
    }

    public IFolderDataSource getDataSource() {
        return dataSource;
    }


    public enum Status{
        DOWNLOADED,
        DOWNLOADING,
        ONLINE_ONLY,
        UNKNOWN
    }
}
