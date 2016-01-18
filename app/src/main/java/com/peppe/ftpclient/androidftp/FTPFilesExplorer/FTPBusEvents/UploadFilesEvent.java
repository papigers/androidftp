package com.peppe.ftpclient.androidftp.FTPFilesExplorer.FTPBusEvents;

import com.peppe.ftpclient.androidftp.FTPFilesExplorer.FTPLocalExplorer.LocalFilesFragment;

/**
 * Created by Geri on 27/10/2015.
 */
public class UploadFilesEvent {
    public LocalFilesFragment.LFileMap files;

    public UploadFilesEvent(LocalFilesFragment.LFileMap files){
        this.files = files;
    }
}
