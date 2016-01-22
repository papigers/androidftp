package com.peppe.ftpclient.androidftp.FTPFilesExplorer;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.TextView;
import android.widget.Toast;

import com.peppe.ftpclient.androidftp.FTPClientMain.MainActivity;
import com.peppe.ftpclient.androidftp.R;

import org.apache.commons.net.ftp.FTPClient;
import org.solovyev.android.views.llm.LinearLayoutManager;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by Geri on 24/10/2015.
 */
public abstract class FilesFragment extends Fragment implements ActionMode.Callback{
    private static final String TAG = "FILES_FRAGMENT";

    protected FTPClient client;

    protected String dir;

    protected TextView noFiles;

    protected View pathFiller;
    protected RecyclerView pathRecycler;
    protected PathAdapter pathAdapter;

    protected View filesFiller;
    protected RecyclerView filesRecycler;
    public FilesAdapter filesAdapter;

    protected String cutSource;
    protected boolean pasteMode;
    protected boolean copy;

    public boolean isPasteMode(){
        return filesAdapter.getCutItemCount()!=0;
    }

    public void cutFiles(boolean copy){
        filesAdapter.cutSelection();
        cutSource = dir;
        this.copy = copy;
        pasteMode(true);
    }

    public String joinPath(String path, String file){
        if(path.equals("/"))
            return path+file;
        else
            return path+"/"+file;
    }

    public abstract void pasteFiles();

    //********************** ITEM SELECTION **********************//

    public ActionMode actionMode;

    public void selectItem(View v){
        if(actionMode == null)
            actionMode = getActivity().startActionMode((MainActivity)getActivity());
        int pos = filesRecycler.getChildAdapterPosition(v);
        Log.d(TAG, "Selected item " + pos);
        filesAdapter.toggleSelection(pos);
        String title = getString(R.string.selected_count, filesAdapter.getSelectedItemCount());
        actionMode.setTitle(title);
        Log.d(TAG, "Selected count " + filesAdapter.getSelectedItemCount());
        actionMode.getMenu().findItem(R.id.action_rename_file).setVisible(filesAdapter.getSelectedItemCount() == 1);
        if(!filesAdapter.isSelecting() && !isPasteMode()) actionMode.finish();
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
    public abstract boolean onActionItemClicked(ActionMode mode, MenuItem item);

    public abstract void showMenuItems(boolean show);

    public void pasteMode(boolean enter) {
        pasteMode = enter;
        Log.d(TAG, (enter ? "Entering" : "Exiting") + " Paste Mode");
        if(enter){
            actionMode.setTitle("Cut: "+filesAdapter.getCutItemCount()+" File(s)");
            //actionMode.getMenu().findItem(android.R.id.closeButton).setIcon(R.drawable.ic_x);
            Log.d(TAG, "cutFiles on enter: "+filesAdapter.getCutItemCount());
            showMenuItems(false);
        }
        else{
            //getActivity().getActionBar().setHomeAsUpIndicator(R.drawable.ic_back);
            actionMode.finish();
        }
        filesAdapter.notifyDataSetChanged();
    }


    @Override
    public void onDestroyActionMode(ActionMode mode) {
        this.actionMode = null;
        filesAdapter.clearSelections();
        filesAdapter.clearCuts();
    }




//********************** END ITEM SELECTION **********************//

    protected ArrayList<String> getDirs(String dir){
        ArrayList<String> dirs = new ArrayList<>();
        while(dir.length()>1) {
            int i = dir.lastIndexOf('/');
            String add = dir.substring(i+1);
            dirs.add(0, add);
            dir = dir.substring(0, i);
        }
        dirs.add(0, '/' + "");
        return dirs;
    }

    public void openLocalFile(String path){
        File file = new File(path);
        Intent openFile = new Intent(Intent.ACTION_VIEW);
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        String mimeType = mime.getMimeTypeFromExtension(fileExt(file.getName()));
        openFile.setDataAndType(Uri.fromFile(file), mimeType);
        openFile.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            getContext().startActivity(openFile);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(getActivity(), "No handler for this type of file.", Toast.LENGTH_LONG).show();
        }
    }

    protected String fileExt(String name){
        if(name.lastIndexOf('.') < 0)
            return null;
        else
            return name.substring(name.lastIndexOf('.')+1).toLowerCase();
    }

    public boolean pressBack(){
        //on root
        //Log.d(TAG, "Pressed back on "+dir+" "+dir.length());
        if(dir.length()==1) {
            if(isPasteMode()) {
                pasteMode(false);
                return false;
            }
            if(client != null && client.isConnected())
                new FTPDisconnectTask().execute();
            return true;
        }
        else{
            openDirecory(null);
            return false;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_files, container, false);

        noFiles = (TextView)v.findViewById(R.id.noFilesTextView);

        initialPathRecycler(v);
        initialFilesRecycler(v);

        loadFiles();

        return v;
    }

    public void initialPathRecycler(View v){
        pathAdapter = new PathAdapter(this);

        pathFiller = v.findViewById(R.id.pathBlank);
        pathRecycler = (RecyclerView) v.findViewById(R.id.pathRecycler);
        LinearLayoutManager pathLayoutManager = new org.solovyev.android.views.llm.LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        pathRecycler.setLayoutManager(pathLayoutManager);
        pathRecycler.setAdapter(pathAdapter);
    }

    public abstract void loadFiles();

    public abstract void initialFilesRecycler(View v);

    public abstract void changeWorkingDirectory(String path);

    public abstract void renameFile(String file, String newName);

    public abstract void deleteFiles(ArrayList<String> files);

    public abstract void openDirecory(String directory);

    public abstract void createDirectory(String name);

    private class FTPDisconnectTask extends AsyncTask<Void,Void,Void> {

        private final String TAG = "DISCONNECT_TASK";

        @Override
        protected Void doInBackground(Void... params) {
            try {
                if(client!=null) {
                    client.logout();
                    client.disconnect();
                }
                Log.d(TAG, "Disconnected");

            }
            catch (Exception e) {
                Log.e(TAG, e.toString());
                Log.d(TAG, "Error: could not disconnect");
            }

            return null;
        }
    }

    public abstract class FileMap<E> {
        protected ArrayList<E> files;
        protected ArrayList<String> dests;
        protected ArrayList<String> froms;

        public FileMap(ArrayList<E> files, ArrayList<String> dest, ArrayList<String> froms){
            this.files  = files;
            this.dests = dest;
            this.froms = froms;
        }

        public FileMap(){
            this.files = new ArrayList<>();
            this.dests = new ArrayList<>();
            this.froms = new ArrayList<>();
        }

        public FileMap(ArrayList<E> files, String dest, String from){
            this.files = files;
            this.dests = new ArrayList<>();
            this.froms = new ArrayList<>();
            for(int i = 0; i < files.size(); i++){
                this.dests.add(dest);
                this.froms.add(from);
            }
        }

        public void add(E file, String dest, String from){
            files.add(file);
            dests.add(dest);
            froms.add(from);
        }

        public void addAll(FileMap map) {
            this.files.addAll(map.files);
            this.dests.addAll(map.dests);
            this.froms.addAll(map.froms);
        }

        public ArrayList<E> getFiles(){
            return files;
        }

        public ArrayList<String> getDest(){
            return dests;
        }

        public ArrayList<String> getFrom(){
            return froms;
        }

        public int size(){
            return files.size();
        }



        public abstract FileMap<E> subset(boolean filesFilter);
    }



}
