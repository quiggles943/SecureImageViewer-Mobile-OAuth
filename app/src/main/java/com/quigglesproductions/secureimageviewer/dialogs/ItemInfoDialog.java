package com.quigglesproductions.secureimageviewer.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;

import com.quigglesproductions.secureimageviewer.R;
import com.quigglesproductions.secureimageviewer.models.FileModel;


public class ItemInfoDialog extends DialogFragment {
    private Toolbar toolbar;
    private FileModel file;
    TextView fileName,folderName,artist,catagories,subjects;
    public ItemInfoDialog(FileModel fileModel){
        this.file = fileModel;

    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.AppTheme_FullScreenDialog);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.dialog_item_info, container, false);
        toolbar = view.findViewById(R.id.toolbar);
        //toolbar = view.findViewById(R.id.toolbar);
        fileName = view.findViewById(R.id.item_name);
        folderName = view.findViewById(R.id.folder_name);
        artist = view.findViewById(R.id.artist_name);
        catagories = view.findViewById(R.id.catagories);
        subjects = view.findViewById(R.id.subjects);
        fileName.setText(file.getName());
        folderName.setText(file.getFolderName());
        artist.setText(file.getArtistName());
        catagories.setText(file.getCatagoryListString());
        subjects.setText(file.getSubjectListString());
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        toolbar.setNavigationOnClickListener(v -> dismiss());
        toolbar.setTitle("File Info");
        toolbar.inflateMenu(R.menu.info_dialog_menu);
        toolbar.setOnMenuItemClickListener(item -> {
            dismiss();
            return true;
        });
    }
    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.MATCH_PARENT;
            dialog.getWindow().setLayout(width, height);
        }
    }

    public static String TAG = "ItemInfoDialog";
}
