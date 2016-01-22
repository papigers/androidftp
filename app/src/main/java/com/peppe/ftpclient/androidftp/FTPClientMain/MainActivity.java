package com.peppe.ftpclient.androidftp.FTPClientMain;


import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ActionMode;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.peppe.ftpclient.androidftp.FTPConnectionsList.ConnectionsFragment;
import com.peppe.ftpclient.androidftp.FTPConnectionsList.EditConnectionFragment;
import com.peppe.ftpclient.androidftp.FTPFilesExplorer.FTPLocalExplorer.LocalFilesFragment;
import com.peppe.ftpclient.androidftp.FTPFilesExplorer.FTPRemoteExplorer.RemoteFilesFragment;
import com.peppe.ftpclient.androidftp.FTPFilesExplorer.FTPViewPager;
import com.peppe.ftpclient.androidftp.FTPFilesExplorer.FilesFragment;
import com.peppe.ftpclient.androidftp.R;

import org.apache.commons.net.ftp.FTPClient;

public class MainActivity extends AppCompatActivity implements ActionMode.Callback{

    private final String TAG = "MAINACTIVITY";
    private final String TEST_HOST = "speedtest.tele2.net";
    private final String TEST_USER =  "anonymous";
    private final String TEST_PASS =  "";
    private FTPClient client = null;

    public boolean isRemoteAlive = false;
    public boolean isLocalAlive = false;
    public RemoteFilesFragment remote;
    public LocalFilesFragment local;

    public Toast commonToast;

    public ActionMode actionMode;

    public FloatingActionButton fab;

    private ConnectionsFragment cf;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        commonToast = Toast.makeText(this, "", Toast.LENGTH_SHORT);

        Log.d(TAG, "before replace");
        getSupportFragmentManager().addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
            @Override
            public void onBackStackChanged() {
                int stackHeight = getSupportFragmentManager().getBackStackEntryCount();
                if (stackHeight > 0) { // if we have something on the stack (doesn't include the current shown fragment)
                    getSupportActionBar().setHomeButtonEnabled(true);
                    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                } else {
                    getSupportActionBar().setDisplayHomeAsUpEnabled(false);
                    getSupportActionBar().setHomeButtonEnabled(false);
                }
            }

        });
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        cf = new ConnectionsFragment();
        ft.replace(R.id.main_placeholder, cf, "CONNECTIONS_FRAGMENT");
        ft.commit();
        Log.d(TAG, "after replace");
        fab = (FloatingActionButton) findViewById(R.id.connections_fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startEditConnection(null);
            }
        });

    }


    public void setRemoteFragment(RemoteFilesFragment frag){
        this.remote = frag;
    }

    public void setLocalFragment(LocalFilesFragment frag){
        this.local = frag;
    }

    @Override
    public void onBackPressed() {
        if(remote != null && isRemoteAlive && !isLocalAlive){
            Log.d(TAG, "back pressed on remote");
            boolean back = remote.pressBack();
            if(back)
                super.onBackPressed();
        }
        else if(local != null && !isRemoteAlive && isLocalAlive){
            boolean back = local.pressBack();
            if(back)
                super.onBackPressed();
        }
        else{
            super.onBackPressed();
            /*Log.d(TAG, remote == null ? "remote is null in main" : "remote is not null in main");
            Log.d(TAG, isRemoteAlive ? "remote is alive in main" : "remote is not alive in main");
            Log.d(TAG, isLocalAlive ? "local is alive in main" : "local is not alive in main");*/
        }

    }

    public boolean dispatchKeyEvent(KeyEvent event) {
        return true;

    }

    /*
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        Log.d(TAG,((event.getKeyCode() == KeyEvent.KEYCODE_BACK) ? "back" : "not back") +" "+
                ((event.getAction() == KeyEvent.ACTION_UP) ? "up" : "not up"));
        if(event.getKeyCode() == KeyEvent.KEYCODE_BACK){
            FilesFragment frag = null;
            if(remote != null && isRemoteAlive && !isLocalAlive){
                frag = remote;
            }
            else if(local != null && !isRemoteAlive && isLocalAlive){
                frag = local;
            }
            if(frag !=null && frag.isPasteMode()) {
                Log.d(TAG, "back on paste mode");

                if (!frag.pressBack()) {
                    Log.d(TAG, "back on paste mode overrided");
                    return true;
                }
                return super.dispatchKeyEvent(event);
            }
            else {
                return super.dispatchKeyEvent(event);
            }
        }
        return super.dispatchKeyEvent(event);
    }*/

    protected void onResume(){
        super.onResume();
        /*
        FTPConnection test = new FTPConnection();
        view= ((ListFragment)getFragmentManager().findFragmentById(R.id.fragment)).getListView();
        adapter = new ArrayAdapter<String>(view.getContext(), android.R.layout.simple_list_item_activated_1);
        view.setAdapter(adapter);
        if(client == null)
            client = new FTPClient();
        new FTPConnectTask().execute(test);
        */
    }

    public void connectTo(FTPConnection connection){
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        FTPViewPager pager = FTPViewPager.newInstance(connection);
        ft.replace(R.id.main_placeholder, pager);
        ft.addToBackStack("CONNECTION_PAGER");
        ft.commit();
        isRemoteAlive = true;
        isLocalAlive = false;
    }

    public void startEditConnection(FTPConnection connection){
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        EditConnectionFragment editCF = EditConnectionFragment.newInstance(connection);
        ft.replace(R.id.main_placeholder, editCF);
        ft.addToBackStack("EDIT_CONNECTION");
        ft.commit();

    }

    public void finishEditConnection(FTPConnection old, FTPConnection edited){
        InputMethodManager inputManager = (InputMethodManager)
                getSystemService(Context.INPUT_METHOD_SERVICE);
        View v = getCurrentFocus();
        if(v != null)
        inputManager.hideSoftInputFromWindow(v.getWindowToken(),
                InputMethodManager.HIDE_NOT_ALWAYS);


        FragmentManager fm = getSupportFragmentManager();
        if (fm.getBackStackEntryCount() > 0) {
            fm.popBackStack();
            FragmentTransaction ft = fm.beginTransaction();
            ft.replace(R.id.main_placeholder, cf, "CONNECTIONS_FRAGMENT");
            ft.commit();
            cf.editDatabase(old, edited);
        }
    }


    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        MenuInflater inflater = mode.getMenuInflater();
        inflater.inflate(R.menu.files_action_mode_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        return false;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item){
        return getActiveFragment().onActionItemClicked(mode, item);
    }


    @Override
    public void onDestroyActionMode(ActionMode mode) {
        FilesFragment frag = getActiveFragment();
        frag.actionMode = null;
        frag.filesAdapter.clearSelections();
        frag.filesAdapter.clearCuts();
    }

    public FilesFragment getActiveFragment(){
        if(remote!=null && isRemoteAlive && !isLocalAlive)
            return remote;
        else
            return local;
    }

}
