package com.peppe.ftpclient.androidftp.FTPFilesExplorer.FTPLocalExplorer;

import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.peppe.ftpclient.androidftp.FTPClientMain.MainActivity;
import com.peppe.ftpclient.androidftp.R;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by Geri on 20/10/2015.
 */
public class UploadProgressDialog extends DialogFragment {
    public static final String FILE = "FILE";
    public static final String COUNT = "COUNT";
    public static final String TAG = "UPLOAD_DIALOG";

    private AsyncTask task;
    private ArrayList<File> files = new ArrayList<>();
    public ProgressBar main;
    public ProgressBar second;
    private TextView mainText;
    private TextView secondText;
    private TextView cancel;
    public boolean cancelled = false;

    public static UploadProgressDialog newInstance(ArrayList<File> files){
        UploadProgressDialog dialog = new UploadProgressDialog();
        Bundle args = new Bundle();
        args.putInt(COUNT, files.size());
        for(int i = 0 ; i<files.size(); i++) {
            args.putSerializable(FILE+i, files.get(i));
        }
        dialog.setArguments(args);
        return dialog;
    }

    public void setTask(AsyncTask task){
        this.task = task;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((MainActivity)getActivity()).isRemoteAlive = false;
        if (getArguments() != null) {
            int count = getArguments().getInt(COUNT);
            for(int i=0 ; i<count; i++)
                files.add((File)getArguments().getSerializable(FILE + i));
        }
        Log.d(TAG, "progress created");
    }


    public void setSecondaryProgress(int secondaryProgress){
        second.setProgress(secondaryProgress);
        secondText.setText(secondaryProgress+"%");
    }

    public void setMainProgress(int progress){
        main.setProgress(progress);
        mainText.setText(progress+"%");
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        ((MainActivity)getActivity()).isRemoteAlive = true;
        Log.d(TAG, "progress dismissed");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.dialog_transfer_progress, container, false);
        main = (ProgressBar)v.findViewById(R.id.transferProgressBar);
        second = (ProgressBar)v.findViewById(R.id.secondaryTransferProgressBar);
        mainText = (TextView)v.findViewById(R.id.transferTextView);
        secondText = (TextView)v.findViewById(R.id.secondaryTransferTextView);
        cancel = (TextView) v.findViewById(R.id.cancelTransfer);
        if(files.size()>1) {
            getDialog().setTitle("Uploading Files...");
            main.setVisibility(View.VISIBLE);
            mainText.setVisibility(View.VISIBLE);
        }
        else{
            getDialog().setTitle("Uploading " + files.get(0).getName() + "...");
            main.setVisibility(View.GONE);
            mainText.setVisibility(View.GONE);
        }
        TextView title = (TextView) getDialog().findViewById(android.R.id.title);
        title.setGravity(Gravity.CENTER);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelled = true;
                if(task != null && !task.isCancelled()) {
                    task.cancel(true);
                    dismiss();
                }
            }
        });
        main.setProgress(0);
        main.setMax(100);
        second.setProgress(0);
        second.setMax(100);
        return v;
    }
}
