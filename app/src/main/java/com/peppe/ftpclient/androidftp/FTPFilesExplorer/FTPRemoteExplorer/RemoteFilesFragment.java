package com.peppe.ftpclient.androidftp.FTPFilesExplorer.FTPRemoteExplorer;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.NotificationCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.ActionMode;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.peppe.ftpclient.androidftp.FTPClientMain.MainActivity;
import com.peppe.ftpclient.androidftp.FTPFilesExplorer.FTPBusEvents.PasteFilesEvent;
import com.peppe.ftpclient.androidftp.FTPFilesExplorer.FTPBusEvents.UploadFilesEvent;
import com.peppe.ftpclient.androidftp.FTPFilesExplorer.FTPLocalExplorer.LocalFilesFragment;
import com.peppe.ftpclient.androidftp.FTPFilesExplorer.FTPLocalExplorer.UploadProgressDialog;
import com.peppe.ftpclient.androidftp.FTPFilesExplorer.FilesFragment;
import com.peppe.ftpclient.androidftp.R;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPFileFilter;
import org.apache.commons.net.io.CopyStreamAdapter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import de.greenrobot.event.EventBus;


public class RemoteFilesFragment extends FilesFragment {

    private static final String TAG = "REMOTE_FRAGMENT";

    private Menu mMenu;

    private EventBus bus = EventBus.getDefault();

    //private FTPConnection ftpConnection;


    /*public static RemoteFilesFragment newInstance(FTPConnection ftpConnection) {
        RemoteFilesFragment fragment = new RemoteFilesFragment();
        Bundle args = new Bundle();
        args.putSerializable(CONNECT, ftpConnection);
        fragment.setArguments(args);
        return fragment;
    }*/

    public RemoteFilesFragment() {
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.remote_files_menu, menu);
        mMenu = menu;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_paste_file:
                Log.d(TAG, "Pressed on paste menu button");
                pasteFiles();
                return true;
            case R.id.action_settings:
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void showMenuItems(boolean show) {
        mMenu.findItem(R.id.remote_files_settings).setVisible(show);
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        RemoteFilesAdapter adapter = (RemoteFilesAdapter)filesAdapter;
        switch(item.getItemId()){
            case R.id.action_downupload_file:
                downloadFiles(false, adapter.getSelectedItems());
                mode.finish();
                return true;
            case R.id.action_delete_file:
                deleteFiles(adapter.getSelectedNames());
                mode.finish();
                return true;
            case R.id.action_rename_file:
                final String name = adapter.getSelectedNames().get(0);
                final DialogFragment renameDialog = new DialogFragment(){
                    @Nullable
                    @Override
                    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
                        View v = inflater.inflate(R.layout.file_rename_dialog, container);

                        getDialog().setTitle(name);
                        ((TextView)getDialog().findViewById(android.R.id.title)).setGravity(Gravity.CENTER);
                        final EditText renameEdit = (EditText)v.findViewById(R.id.renameEditText);
                        renameEdit.setText(name);
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
                        v.findViewById(R.id.renameTextView).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                String newName = ((EditText) getDialog().findViewById(R.id.renameEditText)).getText().toString();
                                renameFile(name, newName);
                                dismiss();
                            }
                        });
                        renameEdit.requestFocus();
                        return v;
                    }
                };

                FragmentManager fm = getActivity().getSupportFragmentManager();
                renameDialog.show(fm, "Rename");
                mode.finish();
                return true;
            case R.id.action_cut_file:
                Log.d(TAG, "Pressed on cut action mode button");
                cutFiles(adapter.getSelectedNames(), false);
                mode.finish();
                return true;
        }
        return false;
    }

    @Override
    public void cutFiles(ArrayList<String> files, boolean copy) {
        super.cutFiles(files, copy);

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        bus.register(this);
    }

    public void onEvent(UploadFilesEvent event) {
        new FTPUplaodTask().execute(event.files);
    }

    public void setClient(FTPClient client) {
        this.client = client;
    }

    @Override
    public void loadFiles() {
        new FTPFilesTask().execute();
    }

    @Override
    public void initialFilesRecycler(View v) {
        filesAdapter = new RemoteFilesAdapter(this);

        filesFiller = v.findViewById(R.id.loadFilesFrame);
        filesRecycler = (RecyclerView) v.findViewById(R.id.filesRecycler);
        org.solovyev.android.views.llm.LinearLayoutManager filesLayoutManager = new org.solovyev.android.views.llm.LinearLayoutManager(getActivity());
        filesRecycler.setLayoutManager(filesLayoutManager);
        filesRecycler.setAdapter(filesAdapter);
    }



    public void pasteFiles(){
        String[] args = new String[cutFiles.size()+1];
        args[0] = cutSource;
        Log.d(TAG, "pasting files from "+cutSource+" to "+dir);
        Log.d(TAG, "size: "+cutFiles.size());
        for(int i = 0; i < cutFiles.size(); i++) {
            Log.d(TAG, "adding "+cutFiles.get(i)+ "to place "+(i+1));
            args[i + 1] = cutFiles.get(i);
        }
        pasteMode(false);
        new FTPCutPasteFile().execute(args);
    }


    public void downloadFiles(boolean open, ArrayList<FTPFile> files) {
        //File fileTo = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File fileTo = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) +
                "/" + getContext().getString(getContext().getApplicationInfo().labelRes));
        if (!fileTo.exists())
            fileTo.mkdir();
        FTPFileMap map = new FTPFileMap(files,  fileTo.getPath(), "");
        FTPFileMap foldersMap = map.subset(false);
        FTPFileMap filesMap = map.subset(true);
        if(foldersMap.size() == 0)
            new FTPDownloadTask(open).execute(map);
        else{
            new FTPDownloadFoldersTask().execute(foldersMap, filesMap);
        }
    }

    public void changeWorkingDirectory(String path) {
        if(actionMode != null) actionMode.finish();
        new FTPChangeDirectoryTask().execute(path);
    }

    //TODO: finish delete
    @Override
    public void deleteFiles(ArrayList<String> files) {
        new FTPDeleteTask().execute(files);
    }

    //TODO: finish create
    @Override
    public void createDirectory(String name) {
        new FTPCreateDir().execute(name);
    }

    public void openDirecory(String directory) {
        if (directory == null) {
            new FTPChangeDirectoryTask().execute((String) null);
        } else {
            String path = dir;
            if(!dir.equals("/"))
                path += "/";
            path += directory;
            Log.d(TAG, path);
            changeWorkingDirectory(path);
        }
    }


    public void renameFile(String file, String newName) {
        new FTPRenameFile().execute(file, newName);
    }

    protected class FTPDownloadFoldersTask extends AsyncTask<FileMap, Void, FileMap>{
        private String TAG = "DOWNLOAD_FOLDER_TASK";

        @Override
        protected void onPostExecute(FileMap map) {
            if(map != null && map.size()!= 0) {
                Log.d(TAG, "Sending files to download task.");
                new FTPDownloadTask(false).execute(map);
            }
            else
                Log.d(TAG, "FileMap " + map == null ? "= null" : "is empty");
        }

        @Override
        protected FileMap doInBackground(FileMap... params) {
            FileMap files = params[1];
            ArrayList<FTPFile> folders = params[0].getFiles();
            ArrayList<String> dests = params[0].getDest();
            for(int i = 0; i < folders.size(); i++){
                FileMap toDownload = downloadFolder(dir, folders.get(i).getName(), dests.get(i));
                if(toDownload!=null)
                    files.addAll(toDownload);
            }
            return files;
        }

        private FileMap downloadFolder(String remotePath, String folder, String localPath){
            try {
                Log.d(TAG, "Downloading folder " + folder + " from " + remotePath + " to " + localPath);
                client.changeWorkingDirectory(folder);
                File to = new File(localPath +"/"+ folder);
                to.mkdir();
                Log.d(TAG, "Created folder "+localPath +"/"+ folder);
                FTPFile[] files = client.listFiles();
                FileMap toDownload = new FTPFileMap();
                String path = remotePath;
                if(path.equals("/")){
                    path += folder;
                }
                else
                    path += "/"+folder;

                for (FTPFile file : files){
                    if (file.isDirectory()) {
                        toDownload.addAll(downloadFolder(path, file.getName(), to.getPath()));
                    }
                    else {
                        Log.d(TAG, "Added file "+file.getName()+" from "+path+"/"+file.getName()+" to " + to.getPath());
                        toDownload.add(file, to.getPath(), path);
                    }
                }
                client.changeToParentDirectory();

                return toDownload;


            } catch (IOException e) {
                Log.e(TAG, e.toString());
                Log.d(TAG, "Could not download folder: "+remotePath+"/"+folder);
            }
            return null;
        }
    }

    protected class FTPCutPasteFile extends AsyncTask<String, Void, Void> {
        private static final String TAG = "PASTE_TASK";

        @Override
        protected void onPostExecute(Void aVoid) {
            Toast.makeText(getContext(), "Refreshing Files...", Toast.LENGTH_SHORT).show();
            new FTPFilesTask().execute();
            cutFiles.clear();
        }

        @Override
        protected Void doInBackground(String... args) {
            Log.d(TAG, "paste task");
            try {
                Log.d(TAG, "entered try");
                int counter = 0;
                Log.d(TAG, ""+ args.length);
                for (int i = 1; i < args.length; i++) {
                    Log.d(TAG, "loop in step "+i);
                    String newSrc = joinPath(args[0], args[i]);
                    String newDst = joinPath(dir, args[i]);
                    boolean status = client.rename(newSrc,newDst);
                    if (status) {
                        counter++;
                        Log.d(TAG, newSrc + " renamed successfuly to " + newDst);
                    }
                    else{
                        Log.d(TAG, newSrc + " failed to rename to " + newDst);
                    }
                }
                if(counter == args.length-1)
                    Toast.makeText(getContext(), counter+ "file(s) were moved successfully.",Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(getContext(), "Failed to move "+(args.length-1-counter)+ "file(s).",Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Log.e(TAG, e.toString());
                Log.d(TAG, "Could not complete the operation!");
            }
            return null;
        }
    }

    protected class FTPRenameFile extends AsyncTask<String, Void, Void> {
        private static final String TAG = "RENAME_TASK";

        @Override
        protected void onPostExecute(Void aVoid) {
            Toast.makeText(getContext(), "Refreshing Files...", Toast.LENGTH_SHORT).show();
            new FTPFilesTask().execute();
        }

        @Override
        protected Void doInBackground(String... params) {
            try {
                boolean status = client.rename(params[0], params[1]);
                if (status) {
                    Log.d(TAG, params[0] + " renamed successfuly to " + params[1]);
                    Toast.makeText(getContext(), params[0] + " renamed successfuly to " + params[1], Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "Failed to rename " + params[0], Toast.LENGTH_SHORT).show();
                }
                /*
                client.makeDirectory("test");
                client.doCommand("SITE","CHMOD 777 test");
                client.deleteFile("test");
                */
            } catch (Exception e) {
                Log.e(TAG, e.toString());
                Log.d(TAG, "Could not rename file: " + params[0] + " to: " + params[1]);
            }
            return null;
        }
    }

    private class FTPChangeDirectoryTask extends AsyncTask<String, Void, FTPFile[]> {

        private final String TAG = "CHANGE_DIR_TASK";

        private String dir;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            filesFiller.setVisibility(View.VISIBLE);
            filesRecycler.setVisibility(View.GONE);

            pathFiller.setVisibility(View.VISIBLE);
            pathRecycler.setVisibility(View.GONE);
            noFiles.setVisibility(View.GONE);
        }

        @Override
        protected void onPostExecute(FTPFile[] result) {
            if (result != null) {
                RemoteFilesFragment.this.dir = dir;
                filesAdapter.setDataset(result);
                filesAdapter.notifyDataSetChanged();

                filesFiller.setVisibility(View.GONE);
                if (result.length == 0) {
                    filesRecycler.setVisibility(View.GONE);
                    noFiles.setVisibility(View.VISIBLE);
                } else {
                    filesRecycler.setVisibility(View.VISIBLE);
                    noFiles.setVisibility(View.GONE);
                    filesRecycler.getLayoutManager().scrollToPosition(0);
                }

                ArrayList<String> dirs = getDirs(dir);
                pathAdapter.setDataset(dirs);
                pathAdapter.notifyDataSetChanged();

                pathFiller.setVisibility(View.GONE);
                pathRecycler.setVisibility(View.VISIBLE);
                pathRecycler.scrollToPosition(dirs.size() - 1);
            } else {
                Log.e(TAG, "files is null");
            }
        }

        @Override
        protected FTPFile[] doInBackground(String... params) {
            Log.d(TAG, "got here");

            if (params[0] != null)
                dir = params[0];
            try {
                if (params[0] == null) {
                    client.changeToParentDirectory();
                    dir = client.printWorkingDirectory();
                } else
                    client.changeWorkingDirectory(dir);
                FTPFile[] ftpFiles = client.listFiles(dir);

                for (FTPFile file : ftpFiles) {
                    String name = file.getName();
                    boolean isFile = file.isFile();

                    if (isFile) {
                        Log.i(TAG, "File : " + name + " " + file.getSize());
                    } else {
                        Log.i(TAG, "Directory : " + name);
                    }
                }

                return ftpFiles;
            } catch (Exception e) {
                Log.e(TAG, e.toString());
            }
            return null;
        }


    }

    private class FTPDownloadTask extends AsyncTask<FileMap, Integer, String[]> {

        private DownloadProgressDialog downloadProgressDialog;
        private boolean open;

        private final String TAG = "DOWNLOAD_TASK";

        public FTPDownloadTask(boolean open) {
            this.open = open;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            if (values[0] == 0)
                downloadProgressDialog.setMainProgress(values[1]);
            else
                downloadProgressDialog.setSecondaryProgress(values[1]);
        }

        @Override
        protected void onPostExecute(String[] info) {
            downloadProgressDialog.dismiss();
            Toast.makeText(getActivity(), "Download Completed", Toast.LENGTH_SHORT).show();
            NotificationManager mNotificationManager = (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.cancel(5);
            if (info != null) {
                //open
                if (open) {
                    openLocalFile(info[0]);
                }
                //change notification
                else {
                    NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getActivity());
                    mBuilder.setContentTitle("Download Completed");
                    mBuilder.setContentText(getString(getContext().getApplicationInfo().labelRes) + ": " + info[1]);
                    mBuilder.setSmallIcon(android.R.drawable.stat_sys_download_done);
                    File file = new File(info[0]);
                    Intent openFile = new Intent(Intent.ACTION_VIEW);
                    MimeTypeMap mime = MimeTypeMap.getSingleton();
                    String mimeType = mime.getMimeTypeFromExtension(fileExt(file.getName()));
                    openFile.setDataAndType(Uri.fromFile(file), mimeType);
                    openFile.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    PendingIntent intent = PendingIntent.getActivity(getContext(), 0, openFile, 0);
                    mBuilder.setContentIntent(intent);
                    mBuilder.setAutoCancel(true);
                    mNotificationManager.notify(5, mBuilder.build());
                }
            } else {
                NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getActivity());
                mBuilder.setContentTitle("Download Completed");
                mBuilder.setContentText(getString(getContext().getApplicationInfo().labelRes));
                mBuilder.setSmallIcon(android.R.drawable.stat_sys_download_done);
                mBuilder.setAutoCancel(true);
                Intent notificationIntent = new Intent(getContext(), MainActivity.class);
                notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                PendingIntent intent = PendingIntent.getActivity(getContext(), 0, notificationIntent, 0);
                mBuilder.setContentIntent(intent);
                mNotificationManager.notify(5, mBuilder.build());
            }

        }

        @Override
        protected void onPreExecute() {

            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getActivity());
            mBuilder.setContentTitle("Downloading Files...");
            mBuilder.setContentText(getString(getContext().getApplicationInfo().labelRes));
            mBuilder.setSmallIcon(android.R.drawable.stat_sys_download);
            Intent notificationIntent = new Intent(getContext(), MainActivity.class);
            notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            PendingIntent intent = PendingIntent.getActivity(getContext(), 0, notificationIntent, 0);
            mBuilder.setContentIntent(intent);
            NotificationManager mNotificationManager = (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.notify(5, mBuilder.build());
        }

        @Override
        protected void onCancelled() {
            Toast.makeText(getActivity(), "Download Cancelled", Toast.LENGTH_SHORT).show();
            NotificationManager mNotificationManager = (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.cancel(5);
        }

        @Override
        protected void onCancelled(String[] s) {
            Toast.makeText(getActivity(), "Download Cancelled", Toast.LENGTH_SHORT).show();
            NotificationManager mNotificationManager = (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.cancel(5);
        }

        @Override
        protected String[] doInBackground(FileMap... params) {
            FileMap filesMap = params[0];
            ArrayList<FTPFile> files = filesMap.getFiles();
            ArrayList<String> dests = filesMap.getDest();
            ArrayList<String> froms = filesMap.getFrom();

            //ArrayList<AFTPFile> files2 = iterateFiles(files.toArray(new FTPFile[files.size()]), "");
            downloadProgressDialog = DownloadProgressDialog.newInstance(files);
            downloadProgressDialog.setTask(FTPDownloadTask.this);
            FragmentManager fm = getActivity().getSupportFragmentManager();
            downloadProgressDialog.show(fm, "Download");

            try {
                client.setBufferSize(1024000);
                for (int i = 0; i < files.size(); i++) {
                    if (!isCancelled()) {
                        FTPFile file = files.get(i);
                        final long size = file.getSize();
                        CopyStreamAdapter streamListener = new CopyStreamAdapter() {
                            @Override
                            public void bytesTransferred(long totalBytesTransferred, int bytesTransferred, long streamSize) {
                                int percent = (int) (totalBytesTransferred * 100 / size);
                                //Log.d(TAG, "percent: " + percent + " bytes: " + totalBytesTransferred + " size: " + size);
                                publishProgress(1, percent);
                                if (downloadProgressDialog.cancelled) {
                                    try {
                                        client.abort();
                                        onCancelled();
                                        Toast.makeText(getActivity(), "Download Cancelled", Toast.LENGTH_SHORT).show();
                                        NotificationManager mNotificationManager = (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
                                        mNotificationManager.cancel(6);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        };
                        client.setCopyStreamListener(streamListener);
                        String newTo = dests.get(i) + "/" + file.getName();
                        String fullPath = froms.get(i);
                        if(fullPath.equals("/"))
                            fullPath += file.getName();
                        else
                            fullPath += "/"+file.getName();
                        fullPath = fullPath.substring(fullPath.indexOf('/')+1);
                        Log.d(TAG, "from: " + fullPath + " to: " + dests.get(i) + " newTo: " + newTo);
                        FileOutputStream fileStream = new FileOutputStream(newTo);
                        boolean status = client.retrieveFile(fullPath, fileStream);
                        if (status)
                            Log.d(TAG, "Downloaded from " + fullPath + " to " + newTo);
                        else
                            Log.d(TAG, "Failed to download from " + fullPath + " to " + newTo);
                        fileStream.close();
                        publishProgress(0, ((int) ((i + 1) * 100) / (files.size())));
                        if (files.size() == 1)
                            return new String[]{newTo, file.getName()};
                    }
                }
            }
            catch (Exception e) {
                Log.e(TAG, e.toString());
                Log.d(TAG, "Error: download failed");
            }

            return null;
        }
    }

    private class FTPDeleteTask extends AsyncTask<ArrayList<String>, Void, Void> {

        private final String TAG = "DELETE_TASK";
        private int n;

        @Override
        protected void onPostExecute(Void v) {
            Toast.makeText(getActivity(), "Deleted "+ n +" File(s).", Toast.LENGTH_SHORT).show();
            new FTPFilesTask().execute();
        }

        private boolean deleteFolder(String path){
            try {
                boolean result = true;
                FTPFile[] files = client.listFiles(path);
                for(FTPFile file: files){
                    if(file.isFile()){
                        result &= client.deleteFile(path+"/"+file.getName());
                    }
                    else{
                        result &= deleteFolder(path+"/"+file.getName());
                        if(result)
                            result &= client.removeDirectory(path+"/"+file.getName());
                    }
                }
                return result;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected Void doInBackground(ArrayList<String>... params) {
            final ArrayList<String> names = params[0];
            n = names.size();

            try {
                FTPFile[] files = client.listFiles(dir, new FTPFileFilter() {
                    @Override
                    public boolean accept(FTPFile file) {
                        return names.contains(file.getName());
                    }
                });
                for(FTPFile file : files){
                    if(file.isFile()){
                        boolean succ = client.deleteFile(file.getName());
                        if(succ) n--;
                    }
                    else{
                        String newPath = dir.equals("/") ? (dir+file.getName()) : (dir+"/"+file.getName());
                        boolean succ = deleteFolder(newPath);
                        if(succ)
                            succ &= client.removeDirectory(newPath);
                        if(succ) n--;
                    }
                }

            } catch (Exception e) {
                Log.e(TAG, e.toString());
                Log.d(TAG, "Error: delete failed");
            }

            return null;
        }

    }

    public class FTPFilesTask extends AsyncTask<Void, Void, FTPFile[]> {

        private final String TAG = "FILES_TASK";

        protected String dir;

        @Override
        protected void onPostExecute(FTPFile[] result) {
            if (result != null) {
                RemoteFilesFragment.this.dir = this.dir;
                filesAdapter.setDataset(result);
                filesAdapter.notifyDataSetChanged();

                filesFiller.setVisibility(View.GONE);
                if (result.length == 0) {
                    filesRecycler.setVisibility(View.GONE);
                    noFiles.setVisibility(View.VISIBLE);
                } else {
                    filesRecycler.setVisibility(View.VISIBLE);
                    noFiles.setVisibility(View.GONE);
                    filesRecycler.getLayoutManager().scrollToPosition(0);
                }

                ArrayList<String> dirs = getDirs(dir);
                pathAdapter.setDataset(dirs);
                pathAdapter.notifyDataSetChanged();

                pathFiller.setVisibility(View.GONE);
                pathRecycler.setVisibility(View.VISIBLE);
                pathRecycler.scrollToPosition(dirs.size() - 1);
            } else {
                Log.e(TAG, "files is null");
            }
        }

        @Override
        protected FTPFile[] doInBackground(Void... params) {
            Log.d(TAG, "got here");
            this.dir = ftpGetCurrentWorkingDirectory();
            try {
                FTPFile[] ftpFiles = client.listFiles(dir);

                for (FTPFile file : ftpFiles) {
                    String name = file.getName();
                    boolean isFile = file.isFile();

                    if (isFile) {
                        Log.i(TAG, "File : " + name + " " + file.getSize());
                    } else {
                        Log.i(TAG, "Directory : " + name);
                    }
                }

                return ftpFiles;
            } catch (Exception e) {
                Log.e(TAG, e.toString());
            }
            return null;
        }

        public String ftpGetCurrentWorkingDirectory() {
            try {
                return client.printWorkingDirectory();
            } catch (Exception e) {
                Log.d(TAG, "Error: could not get current working directory.");
            }

            return null;
        }
    }

    private class FTPCreateDir extends AsyncTask<String, Void, Void> {
        private static final String TAG = "CREATE_DIR_TASK";

        @Override
        protected void onPostExecute(Void aVoid) {
            new FTPFilesTask().execute();
        }

        @Override
        protected Void doInBackground(String... params) {
            try {
                boolean status = client.makeDirectory(params[0]);
                if (status) {
                    Log.d(TAG, params[0] + " created successfuly");
                    Toast.makeText(getContext(), params[0] + " created successfuly", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "Failed to create: " + params[0], Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                Log.e(TAG, e.toString());
                Log.d(TAG, "Could not create directory: " + params[0]);
            }
            return null;
        }
    }

    private class FTPUplaodTask extends AsyncTask<LocalFilesFragment.LFileMap, Integer, String> {

        private UploadProgressDialog uploadProgressDialog;

        private final String TAG = "UPLOAD_TASK";

        @Override
        protected void onProgressUpdate(Integer... values) {
            if (values[0] == 0)
                uploadProgressDialog.setMainProgress(values[1]);
            else
                uploadProgressDialog.setSecondaryProgress(values[1]);
        }

        @Override
        protected void onCancelled() {
            Toast.makeText(getActivity(), "Upload Cancelled", Toast.LENGTH_SHORT).show();
            NotificationManager mNotificationManager = (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.cancel(6);
        }

        @Override
        protected void onCancelled(String s) {
            Toast.makeText(getActivity(), "Upload Cancelled", Toast.LENGTH_SHORT).show();
            NotificationManager mNotificationManager = (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.cancel(6);
        }

        @Override
        protected void onPostExecute(String info) {
            uploadProgressDialog.dismiss();
            Toast.makeText(getActivity(), "Upload Completed", Toast.LENGTH_SHORT).show();
            NotificationManager mNotificationManager = (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.cancel(6);
            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getActivity());
            mBuilder.setContentTitle("Upload Completed");
            if (info != null)
                mBuilder.setContentText(getString(getContext().getApplicationInfo().labelRes) + ": " + info);
            else
                mBuilder.setContentText(getString(getContext().getApplicationInfo().labelRes));
            mBuilder.setSmallIcon(android.R.drawable.stat_sys_upload_done);
            mBuilder.setAutoCancel(true);
            Intent notificationIntent = new Intent(getContext(), MainActivity.class);
            notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            PendingIntent intent = PendingIntent.getActivity(getContext(), 0, notificationIntent, 0);
            mBuilder.setContentIntent(intent);
            mNotificationManager.notify(6, mBuilder.build());
            new FTPFilesTask().execute();
        }

        @Override
        protected void onPreExecute() {
            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getActivity());
            mBuilder.setContentTitle("Uploading Files...");
            mBuilder.setContentText(getString(getContext().getApplicationInfo().labelRes));
            mBuilder.setSmallIcon(android.R.drawable.stat_sys_upload);
            Intent notificationIntent = new Intent(getContext(), MainActivity.class);
            notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            PendingIntent intent = PendingIntent.getActivity(getContext(), 0, notificationIntent, 0);
            mBuilder.setContentIntent(intent);
            NotificationManager mNotificationManager = (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.notify(6, mBuilder.build());
        }

        @Override
        protected String doInBackground(LocalFilesFragment.LFileMap... params) {
            LocalFilesFragment.LFileMap map = params[0];
            LocalFilesFragment.LFileMap fileMap = map.subset(true);
            LocalFilesFragment.LFileMap dirMap = map.subset(false);
            ArrayList<File> files = fileMap.getFiles();
            ArrayList<String> fileDests = fileMap.getDest();
            ArrayList<File> dirs = dirMap.getFiles();
            ArrayList<String> dirDests = dirMap.getDest();
            try {
                for (int i = 0; i < dirs.size(); i++) {
                    File d = dirs.get(i);
                    String currentDir = dir;
                    if(!currentDir.equals("/"))
                        currentDir += "/"+dirDests.get(i);
                    else
                        currentDir += dirDests.get(i);
                    Log.d(TAG, "creating dir "+d.getName()+ " in "+currentDir);
                    client.makeDirectory(currentDir + "/" + d.getName());
                }
            }
            catch (Exception e){
                Log.e(TAG, e.toString());
                Log.d(TAG, "Error: upload failed - directory creation");
            }
            uploadProgressDialog = UploadProgressDialog.newInstance(files);
            uploadProgressDialog.setTask(this);
            FragmentManager fm = getActivity().getSupportFragmentManager();
            uploadProgressDialog.show(fm, "Upload");
            try {
                client.setBufferSize(1024000);
                for (int i = 0; i < files.size(); i++) {
                    if (!isCancelled()) {
                        File file = files.get(i);
                        final long size = file.length();
                        CopyStreamAdapter streamListener = new CopyStreamAdapter() {
                            @Override
                            public void bytesTransferred(long totalBytesTransferred, int bytesTransferred, long streamSize) {
                                int percent = (int) (totalBytesTransferred * 100 / size);
                                Log.d(TAG, "percent: " + percent + " bytes: " + totalBytesTransferred + " size: " + size);
                                publishProgress(1, percent);
                                if (uploadProgressDialog.cancelled)
                                    try {
                                        client.abort();
                                        onCancelled();
                                        Toast.makeText(getActivity(), "Upload Cancelled", Toast.LENGTH_SHORT).show();
                                        NotificationManager mNotificationManager = (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
                                        mNotificationManager.cancel(6);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                            }
                        };
                        client.setCopyStreamListener(streamListener);
                        Log.d(TAG, "from: " + file.getPath() + " to: /" + fileDests.get(i));
                        String path = fileDests.get(i);
                        if(path.equals(""))
                            path+=file.getName();
                        else
                            path+="/"+file.getName();
                        FileInputStream fileStream = new FileInputStream(file.getPath());
                        boolean status = client.storeFile(path, fileStream);
                        if (status)
                            Log.d(TAG, "Uploaded from " + file.getPath() + " to " + fileDests.get(i));
                        else
                            Log.d(TAG, "Failed to upload from " + file.getPath() + " to " + fileDests.get(i));
                        fileStream.close();
                        publishProgress(0, ((int) ((i + 1) * 100) / (files.size())));
                        if (files.size() == 1)
                            return file.getName();
                    }
                    //publishProgress(1, 0);
                }
            } catch (Exception e) {
                Log.e(TAG, e.toString());
                Log.d(TAG, "Error: upload failed");
            }

            return null;
        }

    }

    private class FTPFileMap extends FileMap<FTPFile>{

        public FTPFileMap(ArrayList<FTPFile> files, ArrayList<String> dest, ArrayList<String> froms){
            super(files,dest,froms);
        }

        public FTPFileMap(){
            super();
        }

        public FTPFileMap(ArrayList<FTPFile> files, String dest, String from){
            super(files,dest,from);
        }

        @Override
        public FTPFileMap subset(boolean filesFilter){
            FTPFileMap map = new FTPFileMap();
            for(int i = 0; i< files.size(); i++){
                if((filesFilter && !files.get(i).isDirectory())
                        || (!filesFilter && files.get(i).isDirectory()))
                    map.add(files.get(i), dests.get(i), froms.get(i));
            }
            return map;
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        pasteMode(false);
    }
}
