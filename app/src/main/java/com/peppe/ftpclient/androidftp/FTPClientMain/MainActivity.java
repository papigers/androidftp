package com.peppe.ftpclient.androidftp.FTPClientMain;


import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
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

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private final String TAG = "MAINACTIVITY";

    private final int MY_EXTERNAL_STORAGE = 401;

    public boolean isRemoteAlive = false;
    public boolean isLocalAlive = false;
    public RemoteFilesFragment remote;
    public LocalFilesFragment local;

    public Toast commonToast;

    private String savedTitle = null;

    public FloatingActionButton fab;

    private ConnectionsFragment cf;
    private String errorMessage;

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

    }

    @TargetApi(Build.VERSION_CODES.M)
    public boolean requestStoragePermission(String errorMessage) {
        int sdk = Build.VERSION.SDK_INT;
        if(sdk >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                    PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE}, MY_EXTERNAL_STORAGE);
                this.errorMessage = errorMessage;
                return false;
            }
            else
                return true;
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem paste = menu.findItem(R.id.action_paste_file);
        if(getActiveFragment()!=null && paste != null ){
            Log.d(TAG, "paste menu item found");
            if(getActiveFragment().isPasteMode()) {
                Log.d(TAG, "in paste mode");
                savedTitle = getTitle().toString();
                String state = (getActiveFragment().isCopy() ? "Copy" : "Cut");
                setTitle(state + ": "+getActiveFragment().filesAdapter.getCutItemCount()+ " File(s).");

            }
            else if (savedTitle != null)
                setTitle(savedTitle);
            MenuItem home = menu.findItem(android.R.id.home);
            int icon = ((getActiveFragment() != null && getActiveFragment().isPasteMode()) ? R.drawable.ic_cancel : R.drawable.ic_back);

            getSupportActionBar().setHomeAsUpIndicator(icon);
            paste.setVisible(getActiveFragment().isPasteMode());
            return true;
        }
        return super.onPrepareOptionsMenu(menu);
    }

    public void setRemoteFragment(RemoteFilesFragment frag){
        this.remote = frag;
    }

    public void setLocalFragment(LocalFilesFragment frag){
        this.local = frag;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.action_paste_file:
                getActiveFragment().pasteFiles();
                invalidateOptionsMenu();
                return true;
            case android.R.id.home:
                Log.d(TAG, "clicked home");
                if(getActiveFragment().isPasteMode()){
                    getActiveFragment().pasteMode(false);
                    return true;
                }
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        boolean back = false;
        if(remote != null && isRemoteAlive && !isLocalAlive){
            Log.d(TAG, "back pressed on remote");
            back = remote.pressBack();
        }
        else if(local != null && !isRemoteAlive && isLocalAlive){
            back = local.pressBack();
        }
        else{
            super.onBackPressed();
            /*Log.d(TAG, remote == null ? "remote is null in main" : "remote is not null in main");
            Log.d(TAG, isRemoteAlive ? "remote is alive in main" : "remote is not alive in main");
            Log.d(TAG, isLocalAlive ? "local is alive in main" : "local is not alive in main");*/
        }
        if (back) {
            super.onBackPressed();
        }

    }


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

    public FilesFragment getActiveFragment(){
        if(remote!=null && isRemoteAlive && !isLocalAlive)
            return remote;
        else
            return local;
    }

    @Override
    public void onClick(View v) {
        if(isRemoteAlive && !isLocalAlive){
            Toast.makeText(this, "remote!", Toast.LENGTH_SHORT).show();
        }
        else if(isLocalAlive && !isRemoteAlive){
            Toast.makeText(this, "local!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case MY_EXTERNAL_STORAGE:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if(isRemoteAlive)
                        Toast.makeText(this, getString(R.string.try_again), Toast.LENGTH_SHORT).show();
                    if(local != null)
                        local.refreshDir();
                }
                else
                    Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
        }
    }
}
