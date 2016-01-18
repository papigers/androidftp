package com.peppe.ftpclient.androidftp.FTPFilesExplorer.FTPLocalExplorer;

import android.os.Environment;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.peppe.ftpclient.androidftp.FTPFilesExplorer.FTPBusEvents.UploadFilesEvent;
import com.peppe.ftpclient.androidftp.FTPFilesExplorer.FilesFragment;
import com.peppe.ftpclient.androidftp.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
import java.util.ArrayList;

import de.greenrobot.event.EventBus;

/**
 * Created by Geri on 25/10/2015.
 */
public class LocalFilesFragment extends FilesFragment {
    private static final String TAG = "LOCAL_FILES_FRAG";

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        MenuInflater inflater = mode.getMenuInflater();
        inflater.inflate(R.menu.files_action_mode_menu, menu);
        MenuItem item = menu.findItem(R.id.action_downupload_file);
        item.setIcon(android.R.drawable.stat_sys_upload);
        item.setTitle("Upload");

        menu.findItem(R.id.action_copy_file).setVisible(true);

        return true;
    }

    @Override
    public void pasteMode(boolean enter) {
        //TODO
    }

    @Override
    public void pasteFiles() {
        for (String fileName : cutFiles) {
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
            case R.id.action_delete_file:
                deleteFiles(adapter.getSelectedNames());
                mode.finish();
                return true;
            case R.id.action_rename_file:
                Toast.makeText(getContext(),"Need to Implement", Toast.LENGTH_SHORT).show();
                mode.finish();
                return true;
            case R.id.action_cut_file:
                cutFiles(adapter.getSelectedNames(), false);
                mode.finish();
                return true;
            case R.id.action_copy_file:
                cutFiles(adapter.getSelectedNames(), true);
                mode.finish();
                return true;
            case R.id.action_paste_file:
                pasteFiles();
                mode.finish();
                return true;
        }
        return false;
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

    @Override
    public void changeWorkingDirectory(String path) {
        if(actionMode != null) actionMode.finish();
        dir = path;
        File folder = new File(path);
        File[] files = folder.listFiles();
        filesAdapter.setDataset(files);
        filesAdapter.notifyDataSetChanged();

        filesFiller.setVisibility(View.GONE);
        if (files.length == 0) {
            filesRecycler.setVisibility(View.GONE);
            noFiles.setVisibility(View.VISIBLE);
        } else {
            filesRecycler.setVisibility(View.VISIBLE);
            noFiles.setVisibility(View.GONE);
        }

        ArrayList<String> dirs = getDirs(dir);
        pathAdapter.setDataset(dirs);
        pathAdapter.notifyDataSetChanged();

        pathFiller.setVisibility(View.GONE);
        pathRecycler.setVisibility(View.VISIBLE);
        pathRecycler.scrollToPosition(dirs.size() - 1);
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

    //TODO: finish create
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

    private void refreshDir() {
        File folder = new File(dir);
        File[] files = folder.listFiles();
        filesAdapter.setDataset(files);
        filesAdapter.notifyDataSetChanged();
        filesFiller.setVisibility(View.GONE);
        if (files.length == 0) {
            filesRecycler.setVisibility(View.GONE);
            noFiles.setVisibility(View.VISIBLE);
        } else {
            filesRecycler.setVisibility(View.VISIBLE);
            noFiles.setVisibility(View.GONE);
            filesRecycler.getLayoutManager().scrollToPosition(0);
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
}
