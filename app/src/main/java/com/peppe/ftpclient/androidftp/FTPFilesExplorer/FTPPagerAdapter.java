package com.peppe.ftpclient.androidftp.FTPFilesExplorer;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.peppe.ftpclient.androidftp.FTPClientMain.MainActivity;
import com.peppe.ftpclient.androidftp.FTPFilesExplorer.FTPLocalExplorer.LocalFilesFragment;
import com.peppe.ftpclient.androidftp.FTPFilesExplorer.FTPRemoteExplorer.RemoteFilesFragment;

import org.apache.commons.net.ftp.FTPClient;

/**
 * Created by Geri on 19/10/2015.
 */
public class FTPPagerAdapter extends FragmentPagerAdapter {

    private FTPClient client;
    private MainActivity activity;

    @Override
    public Fragment getItem(int position) {
        switch(position){
            //remote
            case 0:
                RemoteFilesFragment remote = new RemoteFilesFragment();
                remote.setClient(client);
                activity.setRemoteFragment(remote);
                return remote;
            //local
            case 1:
                LocalFilesFragment local = new LocalFilesFragment();
                activity.setLocalFragment(local);
                return local;
            default:
                return new Fragment();
        }
    }

    @Override
    public int getCount() {
        return 2;
    }

    public FTPPagerAdapter(FTPClient client, MainActivity activity , FragmentManager mgr){
        super(mgr);
        this.client = client;
        this.activity = activity;
    }

}
