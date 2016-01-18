package com.peppe.ftpclient.androidftp.FTPFilesExplorer.FTPRemoteExplorer;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.peppe.ftpclient.androidftp.FTPClientMain.MainActivity;
import com.peppe.ftpclient.androidftp.R;

import org.apache.commons.net.ftp.FTPFile;

import java.util.ArrayList;

/**
 * Created by Geri on 20/10/2015.
 */
public class DownloadDialog extends DialogFragment {
    public static final String FILE = "FILE";
    public static final String TAG = "DOWNLOAD_DIALOG";

    private FTPFile file;

    public static DownloadDialog newInstance(FTPFile file){
        DownloadDialog dialog = new DownloadDialog();
        Bundle args = new Bundle();
        args.putSerializable(FILE, file);
        dialog.setArguments(args);
        return dialog;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((MainActivity)getActivity()).isRemoteAlive = false;
        Log.d(TAG, "dialog created");
        if (getArguments() != null) {
            this.file = (FTPFile)getArguments().getSerializable(FILE);
        }
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        ((MainActivity)getActivity()).isRemoteAlive = true;
        Log.d(TAG, "dialog dismissed");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.file_dialog, container, false);
        getDialog().setTitle(file.getName());
        TextView name = (TextView) getDialog().findViewById(android.R.id.title);
        name.setGravity(Gravity.CENTER);
        final RemoteFilesFragment fragment = (RemoteFilesFragment)getTargetFragment();

        final TextView down = (TextView)v.findViewById(R.id.dialogFileDownUpView);
        down.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getActivity(), "Download " + file.getName(), Toast.LENGTH_LONG).show();
                dismiss();
                ArrayList<FTPFile> files = new ArrayList<>();
                files.add(file);
                fragment.downloadFiles(false, files);
            }
        });

        final TextView open = (TextView)v.findViewById(R.id.dialogFileOpenView);
        open.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (file.isDirectory()) {
                    dismiss();
                    fragment.openDirecory(file.getName());
                } else {
                    Toast.makeText(getActivity(), "Open " + file.getName(), Toast.LENGTH_LONG).show();
                    dismiss();
                    ArrayList<FTPFile> files = new ArrayList<>();
                    files.add(file);
                    fragment.downloadFiles(true, files);
                }
            }
        });

        /*
        final TextView rename = (TextView)v.findViewById(R.id.dialogDownloadRenameView);
        final EditText renameEdit = (EditText)v.findViewById(R.id.renameEditText);
        final TextView renameOK = (TextView)v.findViewById(R.id.renameOkTextView);
        final TextView renameCancel = (TextView)v.findViewById(R.id.renameCancelTextView);
        final TextView delete = (TextView)v.findViewById(R.id.dialogDownloadDeleteView);

        renameEdit.setText(file.getName());


        rename.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                down.setVisibility(View.GONE);
                open.setVisibility(View.GONE);
                rename.setVisibility(View.GONE);
                delete.setVisibility(View.GONE);
                renameEdit.setVisibility(View.VISIBLE);
                renameOK.setVisibility(View.VISIBLE);
                renameCancel.setVisibility(View.VISIBLE);
                renameEdit.requestFocus();
            }
        });

        renameEdit.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    String text = renameEdit.getText().toString();
                    if (!text.isEmpty()) {
                        int index = text.lastIndexOf('.');
                        if (index == -1)
                            renameEdit.selectAll();
                        else
                            renameEdit.setSelection(0, index);
                        InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                        inputMethodManager.toggleSoftInputFromWindow(renameEdit.getApplicationWindowToken(), InputMethodManager.SHOW_FORCED, 0);
                    }
                }
            }
        });
        renameOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "called to rename " + file.getName() + " to " + renameEdit.getText().toString());
                fragment.renameFile(file.getName(), renameEdit.getText().toString());
                dismiss();
            }
        });
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                down.setVisibility(View.GONE);
                open.setVisibility(View.GONE);
                rename.setVisibility(View.GONE);
                delete.setVisibility(View.GONE);
                renameOK.setVisibility(View.VISIBLE);
                renameCancel.setVisibility(View.VISIBLE);
                getDialog().setTitle(getString(R.string.delete_confirm) + " " + file.getName() + "?");
                renameOK.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ArrayList<String> files = new ArrayList<String>();
                        files.add(file.getName());
                        fragment.deleteFiles(files);
                        dismiss();
                    }
                });
                renameOK.setText(R.string.yes);
                renameCancel.setText(R.string.no);
            }
        });
        renameCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        */
        return v;
    }
}
