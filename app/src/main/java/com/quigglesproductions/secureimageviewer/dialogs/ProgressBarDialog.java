package com.quigglesproductions.secureimageviewer.dialogs;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.quigglesproductions.secureimageviewer.R;

public class ProgressBarDialog extends AlertDialog {
    public ProgressBar progressBar;
    private TextView folderName,topProgress, alertTitle, alertSubTitle;
    int currentFileCount;
    public ProgressBarDialog(Context context){
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_progress);
        progressBar = findViewById(R.id.singleProgressBar);
        progressBar.setIndeterminate(false);
        folderName = findViewById(R.id.single_progress_folder_name);
        topProgress = findViewById(R.id.single_progress_folder_count);
        alertTitle = findViewById(R.id.single_progress_title);
        alertSubTitle = findViewById(R.id.single_progress_subtitle);
    }
    public void setTopProgressBarTitle(String title){
        folderName.setText(title);
    }
    public void updateTopProgressBar(int value,int total){
        progressBar.setMax(total);
        progressBar.setProgress(value);
        topProgress.setText(value+"/"+total);
    }
    @Override
    public void setTitle(CharSequence title) {
        super.setTitle(title);
        alertTitle = findViewById(R.id.single_progress_title);
        alertTitle.setText(title);
    }
    public void setSubTitle(CharSequence subTitle){
        alertSubTitle.setText(subTitle);
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
    }
}
