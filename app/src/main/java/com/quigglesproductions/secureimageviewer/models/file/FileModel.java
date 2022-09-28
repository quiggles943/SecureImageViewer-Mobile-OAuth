package com.quigglesproductions.secureimageviewer.models.file;

import android.graphics.Bitmap;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.annotations.SerializedName;
import com.quigglesproductions.secureimageviewer.models.ArtistModel;
import com.quigglesproductions.secureimageviewer.models.CatagoryModel;
import com.quigglesproductions.secureimageviewer.models.ItemBaseModel;
import com.quigglesproductions.secureimageviewer.models.SubjectModel;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ArrayList;

public class FileModel implements ItemBaseModel {
    private int ID;
    @SerializedName("FILE_ID")
    public int onlineId;
    @SerializedName("FILE_BASE64_NAME")
    public String encodedName;
    @SerializedName("FILE_REAL_NAME")
    public String normalName;
    @SerializedName("FILE_SIZE")
    public int fileSize;
    @SerializedName("FILE_FOLDER_ID")
    public int onlineFolderId;
    @SerializedName("FILE_WIDTH")
    public int fileWidth;
    @SerializedName("FILE_HEIGHT")
    public int fileHeight;
    @SerializedName("FILE_ARTIST_ID")
    public int onlineArtistId;
    @SerializedName("FILE_EXTENSION")
    public String fileExtension;
    @SerializedName("FILE_IS_ENCRYPTED")
    public boolean isEncrypted;
    @SerializedName("FILE_CONTENT_TYPE")
    public String contentType;
    public ArtistModel Artist;
    public ArrayList<SubjectModel> Subjects;
    public ArrayList<CatagoryModel> Catagories;

    public Date downloadTime;
    public int folderId;
    String filePath;
    File imageFile;
    String thumbnailPath;
    File thumbnailFile;
    Bitmap image;
    Bitmap thumbnailImage;
    String folderName;;
    boolean isUploaded;

    public FileModel(String name, String base64Name){
        normalName = name;
        encodedName = base64Name;
        isUploaded = false;
        isEncrypted = false;
    }

    public FileModel(int itemId,int onlineId, String name, String base64Name, int artistId, int folderId,int onlineFolderId) {
        ID = itemId;
        this.onlineId = onlineId;
        normalName = name;
        encodedName = base64Name;
        onlineArtistId = artistId;
        this.onlineFolderId = onlineFolderId;
        this.folderId = folderId;
        isUploaded = true;
    }
    public FileModel(int itemId,int onlineId, String name, String base64Name, int artistId, int folderId,int onlineFolderId,int width, int height, File imageFile, File thumbnailFile, Date downloadTime) {
        ID = itemId;
        this.onlineId = onlineId;
        normalName = name;
        encodedName = base64Name;
        onlineArtistId = artistId;
        this.onlineFolderId = onlineFolderId;
        fileWidth = width;
        fileHeight = height;
        this.folderId = folderId;
        this.filePath = imageFile.getPath();
        this.imageFile = imageFile;
        this.thumbnailPath = thumbnailFile.getPath();
        this.thumbnailFile = thumbnailFile;
        this.downloadTime = downloadTime;
        isUploaded = true;
    }

    public String getName(){
        return normalName;
    }

    public String getFileType(){
        return contentType;
    }

    public int getId() {
        return this.ID;
    }

    public int getFolderId(){
        return this.folderId;
    }
    public int getOnlineFolderId() {
        return onlineFolderId;
    }

    public File getImageFile(){
        //return this.imageFile;
        return new File(filePath);
    }

    public File getThumbnailFile(){
        //return this.thumbnailFile;
        return new File(thumbnailPath);
    }

    public void setImageFile(File imageFile){
        this.imageFile = imageFile;
        this.filePath = imageFile.getPath();
    }

    public void setThumbnailFile(File thumbnailFile){
        this.thumbnailFile = thumbnailFile;
        this.thumbnailPath = thumbnailFile.getPath();
    }

    public boolean hasThumbnail(){
        if(this.thumbnailFile == null)
            return false;
        else
            return true;
    }

    public void setImage(Bitmap bitmap){
        this.image = bitmap;
    }

    public boolean imageSet(){
        if(this.image == null)
            return false;
        else
            return true;
    }

    public Bitmap getImage(){return this.image; }

    public void setThumbnailImage(Bitmap bitmap)
    {
        this.thumbnailImage = bitmap;
    }

    public Bitmap getThumbnailImage() {
        return thumbnailImage;
    }

    public boolean thumbnailSet(){
        if(this.thumbnailImage == null)
            return false;
        else
            return true;
    }

    public String getFolderName() {
        return folderName;
    }

    public void setFolderName(String name) {
        folderName = name;
    }

    public String getArtistName(){
        if(Artist == null)
            return "";
        else
            return Artist.name;
    }

    public String getCatagoryListString(){
        String catagoryString = "";
        if(Catagories != null) {
            for (int i = 0; i < Catagories.size(); i++) {
                catagoryString = catagoryString + Catagories.get(i).name + ", ";
            }
            if (catagoryString.length() > 0) {
                catagoryString = catagoryString.substring(0, catagoryString.length() - 2);
            }
        }
        return catagoryString;
    }

    public String getSubjectListString() {
        String subjectString = "";
        if(Subjects != null) {
            for (int i = 0; i < Subjects.size(); i++) {
                subjectString = subjectString + Subjects.get(i).name + ", ";
            }
            if (subjectString.length() > 0) {
                subjectString = subjectString.substring(0, subjectString.length() - 2);
            }
        }
        return subjectString;
    }

    public void setId(int newRowId) {
        ID = newRowId;
    }

    public int getOnlineId() {
        return this.onlineId;
    }

    public void setFolderId(int folderId) {
        this.folderId = folderId;
    }

    public Date getDownloadTime() {
        if(downloadTime != null)
        return downloadTime;
        else
            return new Date();
    }

    public void setDownloadTime(Date downloadDate) {
        downloadTime = downloadDate;
    }

    public void setIsUploaded(boolean isUploaded) {
        this.isUploaded = isUploaded;
    }

    public boolean getIsUploaded() {
        return this.isUploaded;
    }

    public void setOnlineId(int fileId) {
        this.onlineId = fileId;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }
    public ArrayList<SubjectModel> getSubjects(){
        return this.Subjects;
    }
    public ArrayList<CatagoryModel> getCatagories(){
        return this.Catagories;
    }


    public String getJson() throws JSONException {
        Gson gson = new Gson();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss");
        String date = format.format(new Date());
        JSONObject object = new JSONObject();
        object.put("FILE_BASE64_NAME",encodedName);
        object.put("FILE_REAL_NAME",normalName);
        object.put("FILE_SIZE",fileSize);
        object.put("FILE_FOLDER_ID",onlineFolderId);
        object.put("FILE_WIDTH",fileWidth);
        object.put("FILE_HEIGHT",fileHeight);
        object.put("FILE_ARTIST_ID",onlineArtistId);
        object.put("FILE_EXTENSION",fileExtension);
        object.put("FILE_IS_ENCRYPTED",isEncrypted);
        object.put("FILE_CONTENT_TYPE",contentType);
        object.put("FILE_IMPORT_TIME",date);
        object.put("Catagories",gson.toJsonTree(getCatagories()));
        object.put("Subjects",gson.toJsonTree(getSubjects()));
        return object.toString();
    }

    public JSONObject getJsonObject() throws JSONException {
        Gson gson = new Gson();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss");
        String date = format.format(new Date());
        JSONObject object = new JSONObject();
        object.put("FILE_BASE64_NAME",encodedName);
        object.put("FILE_REAL_NAME",normalName);
        object.put("FILE_SIZE",fileSize);
        object.put("FILE_FOLDER_ID",onlineFolderId);
        object.put("FILE_WIDTH",fileWidth);
        object.put("FILE_HEIGHT",fileHeight);
        object.put("FILE_ARTIST_ID",onlineArtistId);
        object.put("FILE_EXTENSION",fileExtension);
        object.put("FILE_IS_ENCRYPTED",isEncrypted);
        object.put("FILE_CONTENT_TYPE",contentType);
        object.put("FILE_IMPORT_TIME",date);
        object.put("Catagories",gson.toJsonTree(getCatagories()));
        object.put("Subjects",gson.toJsonTree(getSubjects()));
        return object;
    }
}
