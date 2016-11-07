package com.peppe.ftpclient.androidftp.FTPFilesExplorer.FTPLocalExplorer;

import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
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
import com.peppe.ftpclient.androidftp.FTPFilesExplorer.FTPBusEvents.UploadFilesEvent;
import com.peppe.ftpclient.androidftp.FTPFilesExplorer.FilesAdapter;
import com.peppe.ftpclient.androidftp.FTPFilesExplorer.FilesFragment;
import com.peppe.ftpclient.androidftp.R;

import org.apache.commons.net.ftp.FTPFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * Created by Geri on 25/10/2015.
 */
public class LocalFilesFragment extends FilesFragment<File> {
    private static final String TAG = "LOCAL_FILES_FRAG";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dir = Environment.getExternalStorageDirectory().getPath();
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        MenuInflater inflater = mode.getMenuInflater();
        inflater.inflate(R.menu.files_action_mode_menu, menu);

        MenuItem upload = menu.findItem(R.id.action_downupload_file);
        upload.setIcon(android.R.drawable.stat_sys_upload);
        upload.setTitle(R.string.action_upload_file);

        MenuItem share = menu.findItem(R.id.action_url_share_file);
        share.setTitle(R.string.action_share_file);

        menu.findItem(R.id.action_copy_file).setVisible(true);

        return true;
    }

    @Override
    public void pasteFiles() {
        for (String fileName : ((LocalFilesAdapter)filesAdapter).getCutNames()) {
            String source = joinPath(cutSource, fileName);
            String dest = joinPath(dir, fileName);
            if(copy) {
                FileChannel inputChannel = null;
                FileChannel outputChannel = null;
                try {
                    inputChannel = new FileInputStream(source).getChannel();
                    outputChannel = new FileOutputStream(dest).getChannel();
                    outputChannel.transferFrom(inputChannel, 0, inputChannel.size());
                } catch (Exception e) {
                } finally {
                    try {
                        inputChannel.close();
                        outputChannel.close();
                    } catch (Exception e) {
                    }
                }
            }
            else{
                File src = new File(source);
                File dst = new File(dest);
                src.renameTo(dst);
            }
        }
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        LocalFilesAdapter adapter = (LocalFilesAdapter)filesAdapter;
        switch(item.getItemId()){
            case R.id.action_downupload_file:
                uploadFiles(adapter.getSelectedItems());
                mode.finish();
                return true;
            case R.id.action_url_share_file:
                shareFile(adapter.getSelectedItems().get(0));
                mode.finish();
                return true;
            case R.id.action_delete_file:
                deleteFiles(adapter.getSelectedNames());
                mode.finish();
                return true;
            case R.id.action_rename_file:
                showRenameDialog();
                mode.finish();
                return true;
            case R.id.action_cut_file:
                cutFiles(false);
                return true;
            case R.id.action_copy_file:
                cutFiles(true);
                return true;
            case R.id.action_paste_file:
                pasteFiles();
                return true;
            case R.id.action_info_file:
                final File file = adapter.getSelectedItems().get(0);
                final DialogFragment infoDialog = new DialogFragment(){
                    @Nullable
                    @Override
                    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
                        View v = inflater.inflate(R.layout.file_info_dialog, container);

                        getDialog().setTitle(R.string.action_info_file);
                        ((TextView)getDialog().findViewById(android.R.id.title)).setGravity(Gravity.CENTER);
                        TextView nameText = (TextView)v.findViewById(R.id.infoNameTextView);
                        nameText.setText(file.getName());
                        TextView sizeText = (TextView)v.findViewById(R.id.infoSizeTextView);
                        sizeText.setText(filesAdapter.convertToStringRepresentation(file.length()));
                        TextView typeText = (TextView)v.findViewById(R.id.infoTypeTextView);
                        if(file.isDirectory()) {
                            typeText.setText("Directory");
                            sizeText.setVisibility(View.GONE);
                        }
                        else
                            typeText.setText(getTypeByName(file.getName()));

                        TextView dirText = (TextView)v.findViewById(R.id.infoDirTextView);
                        dirText.setText(dir);
                        return v;
                    }
                };

                FragmentManager ifm = getActivity().getSupportFragmentManager();
                infoDialog.show(ifm, "Rename");
                mode.finish();
                return true;
        }
        return false;
    }

    public void shareFile(File file){
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        String mimeType = mime.getMimeTypeFromExtension(fileExt(file.getName()));
        shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
        shareIntent.setType(mimeType);
        startActivity(Intent.createChooser(shareIntent, getString(R.string.share_file_title, file.getName())));
    }


    @Override
    public void loadFiles() {
        changeWorkingDirectory(Environment.getExternalStorageDirectory().getPath());
    }

    @Override
    public void initialFilesRecycler(View v) {
        filesAdapter = new LocalFilesAdapter(this);
        filesFiller = v.findViewById(R.id.loadFilesFrame);
        filesRecycler = (RecyclerView) v.findViewById(R.id.filesRecycler);
        org.solovyev.android.views.llm.LinearLayoutManager filesLayoutManager = new org.solovyev.android.views.llm.LinearLayoutManager(getActivity());
        filesRecycler.setLayoutManager(filesLayoutManager);
        filesRecycler.setAdapter(filesAdapter);
    }

    /*
    @Override
    protected void refreshDir() {
        if(!getStoragePermissions(getText(R.string.cant_show_files).toString()))
            showHideFiles(SHOW_NOPERM, -1);
        else {
            File folder = new File(dir);
            File[] files = folder.listFiles();
            filesAdapter.setDataset(files);
            filesAdapter.notifyDataSetChanged();
            filesFiller.setVisibility(View.GONE);
            if (files.length == 0) {
                showHideFiles(SHOW_NOFILES, -1);
            } else {
                showHideFiles(SHOW_FILES, -1);
            }
        }
    }
    */

    @Override
    public void changeWorkingDirectory(String path) {
        if(actionMode != null && !isPasteMode()) actionMode.finish();
        if(!getStoragePermissions(getText(R.string.cant_show_files).toString()))
            showHideFiles(SHOW_NOPERM, -1);
        else {
            ArrayList<String> dirs = getDirs(dir);
            File folder = new File(path);
            Log.d(TAG, path);
            File[] files = folder.listFiles();
            Log.d(TAG, "files: " + (files == null ? "null" : "not null"));
            if(dir!= null && this.dir.equals(path) && filesAdapter.dataset != null){
                filesAdapter.animateTo(Arrays.asList(files));
            }
            else {
                dir = path;
                filesAdapter.setDataset(files);
                filesAdapter.notifyDataSetChanged();
                pathAdapter.setDataset(dirs);
                pathAdapter.notifyDataSetChanged();
            }


            if (files == null || files.length == 0) {
                showHideFiles(SHOW_NOFILES, dirs.size() - 1);
            } else {
                showHideFiles(SHOW_FILES, dirs.size() - 1);
            }
        }
    }

    @Override
    public void renameFile(String file, String newName) {
        File rename = new File(dir + "/" + file);
        rename.renameTo(new File(dir + "/" + newName));
        refreshDir();
    }

    @Override
    public void deleteFiles(ArrayList<String> files) {
        for (String file : files) {
            File delete = new File(dir+"/"+file);
            if(delete.isDirectory()){
                deleteFiles(delete.listFiles());
            }
            Log.d(TAG, "deleting " + delete.getPath());
            delete.delete();
        }
        refreshDir();
    }

    public void deleteFiles(File[] files) {
        for (File delete : files) {
            if(delete.isDirectory()){
                deleteFiles(delete.listFiles());
            }
            Log.d(TAG, "deleting " + delete.getPath());
            delete.delete();
        }
    }

    @Override
    public void openDirecory(String directory) {
        if (directory == null) {
            File current = new File(dir);
            Log.d(TAG, current.getParent());
            changeWorkingDirectory(current.getParent());
        } else {
            String path = this.dir + "/" + directory;
            Log.d(TAG, path);
            changeWorkingDirectory(path);
        }
    }

    //TODO: check create
    @Override
    public void createDirectory(String name) {
        File create = new File(dir +"/"+name);
        if(create.exists()){
            Toast.makeText(getContext(), "File "+name +" already exist.", Toast.LENGTH_SHORT).show();
        }
        else{
            boolean res = create.mkdir();
            if(res) {
                Toast.makeText(getContext(), "Folder " + name + " created.", Toast.LENGTH_SHORT).show();
                refreshDir();
            }
            else
                Toast.makeText(getContext(), "Folder "+name +" couldn't be created.", Toast.LENGTH_SHORT).show();
        }
    }

    protected void uploadFiles(ArrayList<File> files) {
        LFileMap map = new LFileMap();
        for(File file: files){
            map.add(file, "", "");
            if(file.isDirectory())
                map.addAll(createMap(file.listFiles(), file.getName()));
        }
        EventBus bus = EventBus.getDefault();
        bus.post(new UploadFilesEvent(map));
    }

    private LFileMap createMap(File[] files, String dir) {
        LFileMap map = new LFileMap();
        for(File file: files){
            map.add(file, dir, "");
            if(file.isDirectory())
                map.addAll(createMap(file.listFiles(), dir+"/"+file.getName()));
        }
        return map;
    }

    public class LFileMap extends FileMap<File>{

        public LFileMap(ArrayList<File> files, ArrayList<String> dest){
            super(files,dest,null);
        }

        public LFileMap(){
            super();
        }

        public LFileMap(ArrayList<File> files, String dest){
            super(files,dest,null);
        }

        @Override
        public LFileMap subset(boolean filesFilter){
            LFileMap map = new LFileMap();
            for(int i = 0; i< files.size(); i++){
                if((filesFilter && !files.get(i).isDirectory())
                        || (!filesFilter && files.get(i).isDirectory()))
                    map.add(files.get(i), dests.get(i), froms.get(i));
            }
            return map;
        }
    }

    @Override
    protected ArrayList<File> filter(List<File> files, String query) {
        query = query.toLowerCase();

        final ArrayList<File> filteredFileList = new ArrayList<>();
        for (File file : files) {
            final String text = file.getName().toLowerCase();
            if (text.contains(query)) {
                filteredFileList.add(file);
            }
        }
        return filteredFileList;
    }
}
