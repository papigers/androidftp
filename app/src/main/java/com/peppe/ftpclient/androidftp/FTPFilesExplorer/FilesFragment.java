package com.peppe.ftpclient.androidftp.FTPFilesExplorer;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
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
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.peppe.ftpclient.androidftp.FTPClientMain.FTPConnection;
import com.peppe.ftpclient.androidftp.FTPClientMain.MainActivity;
import com.peppe.ftpclient.androidftp.R;

import org.apache.commons.net.ftp.FTPClient;
import org.solovyev.android.views.llm.LinearLayoutManager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Geri on 24/10/2015.
 */
public abstract class FilesFragment<E> extends Fragment implements ActionMode.Callback, SearchView.OnQueryTextListener{
    private static final String TAG = "FILES_FRAGMENT";

    protected FTPClient client;

    protected FTPConnection connection;

    protected String dir;

    protected TextView noFiles;

    protected View pathFiller;
    protected RecyclerView pathRecycler;
    protected PathAdapter pathAdapter;

    protected View filesFiller;
    protected RecyclerView filesRecycler;
    public FilesAdapter<E> filesAdapter;

    protected String cutSource;
    protected boolean pasteMode;

    private ArrayList<E> datasetCopy;

    public boolean isCopy() {
        return copy;
    }

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
            actionMode = getActivity().startActionMode(this);
        int pos = filesRecycler.getChildAdapterPosition(v);
        Log.d(TAG, "Selected item " + pos);
        filesAdapter.toggleSelection(pos);
        String title = getString(R.string.selected_count, filesAdapter.getSelectedItemCount());
        actionMode.setTitle(title);
        Log.d(TAG, "Selected count " + filesAdapter.getSelectedItemCount());
        boolean oneItem = filesAdapter.getSelectedItemCount() == 1;
        actionMode.getMenu().findItem(R.id.action_rename_file).setVisible(oneItem);
        if(oneItem) {
            oneItem &= (((MainActivity) getActivity()).isRemoteAlive) ||(((MainActivity) getActivity()).isLocalAlive && ((File) (filesAdapter.getSelectedItems().get(0))).isFile());
            actionMode.getMenu().findItem(R.id.action_url_share_file).setVisible(oneItem);
        }
        if(!filesAdapter.isSelecting()) actionMode.finish();
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

    public void pasteMode(boolean enter) {
        pasteMode = enter;
        Log.d(TAG, (enter ? "Entering" : "Exiting") + " Paste Mode");
        if(enter){
            //actionMode.getMenu().findItem(android.R.id.closeButton).setIcon(R.drawable.ic_x);
            Log.d(TAG, "cutFiles on enter: " + filesAdapter.getCutItemCount());
        }
        else{
            //getActivity().getActionBar().setHomeAsUpIndicator(R.drawable.ic_back);
            filesAdapter.clearCuts();
            filesAdapter.clearSelections();
        }
        getActivity().invalidateOptionsMenu();
        if(actionMode != null) actionMode.finish();
        filesAdapter.notifyDataSetChanged();
    }


    @Override
    public void onDestroyActionMode(ActionMode mode) {
        this.actionMode = null;
        filesAdapter.clearSelections();
    }




//********************** END ITEM SELECTION **********************//


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.remote_files_menu, menu);

        MenuItem item = menu.findItem(R.id.action_search);
        ((SearchView) MenuItemCompat.getActionView(item)).setOnQueryTextListener(this);
        ((SearchView) MenuItemCompat.getActionView(item)).setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                datasetCopy = new ArrayList<E>(filesAdapter.dataset);
                pathRecycler.setVisibility(View.GONE);
                ((MainActivity)getActivity()).fab.hide();
            }
        });
        ((SearchView) MenuItemCompat.getActionView(item)).setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                filesAdapter.animateTo(datasetCopy);
                pathRecycler.setVisibility(View.VISIBLE);
                ((MainActivity)getActivity()).fab.show();
                return false;
            }
        });

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.sort_files_menu:
                PopupMenu popup = new PopupMenu(getContext(), getActivity().findViewById(R.id.sort_files_menu));
                MenuInflater inflater = popup.getMenuInflater();
                inflater.inflate(R.menu.sort_files_options, popup.getMenu());
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()){
                            case R.id.sort_by_name:
                                filesAdapter.sort(FilesAdapter.FileComparator.BY_NAME);
                                return true;
                            case R.id.sort_by_size:
                                filesAdapter.sort(FilesAdapter.FileComparator.BY_SIZE);
                                return true;
                            case R.id.sort_by_type:
                                filesAdapter.sort(FilesAdapter.FileComparator.BY_TYPE);
                                return true;
                            case R.id.sort_by_time:
                                filesAdapter.sort(FilesAdapter.FileComparator.BY_TIME);
                                return true;
                            default:
                                return false;
                        }
                    }
                });
                popup.show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    protected void ensurePathIsSowhn(){
        if ( pathFiller.getVisibility() == View.GONE)
            pathRecycler.setVisibility(View.VISIBLE);
    }

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

    protected String getTypeByName (String fileName){
        String ext = fileExt(fileName);
        if(ext == null)
            return "File";
        else{
            return ext.toUpperCase()+" File";
        }
    }

    protected void showRenameDialog() {
        final String name = (String)filesAdapter.getSelectedNames().get(0);
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


    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String query) {
        final ArrayList<E> filteredFileList = filter(datasetCopy, query);
        filesAdapter.animateTo(filteredFileList);
        filesRecycler.scrollToPosition(0);
        return true;
    }

    protected abstract ArrayList<E> filter(List<E> files, String query);

    public static final int SHOW_FILLERS = 0;
    public static final int SHOW_FILES = 1;
    public static final int SHOW_NOFILES = 2;
    public static final int SHOW_NOPERM = 3;

    /**
     *
     * @param action the action to be performed: SHOW_FILES, SHOW_NOFILES, SHOW_NOPERM
     */
    protected void showHideFiles(int action, int pathScroll){
        if (action == SHOW_FILLERS){
            filesFiller.setVisibility(View.VISIBLE);
            filesRecycler.setVisibility(View.GONE);

            pathFiller.setVisibility(View.VISIBLE);
            pathRecycler.setVisibility(View.GONE);
            noFiles.setVisibility(View.GONE);
        }
        else if (action > 1) {
            filesRecycler.setVisibility(View.GONE);
            filesFiller.setVisibility(View.GONE);
            noFiles.setVisibility(View.VISIBLE);
            if(action == SHOW_NOFILES) {
                noFiles.setText(getText(R.string.empty_files));
                pathFiller.setVisibility(View.GONE);
                pathRecycler.setVisibility(View.VISIBLE);
                noFiles.setOnClickListener(null);

            }
            else {
                noFiles.setText(getText(R.string.no_storage_perm));
                pathFiller.setVisibility(View.VISIBLE);
                pathRecycler.setVisibility(View.GONE);
                noFiles.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        getStoragePermissions(getString(R.string.cant_show_files));
                    }
                });
                //todo hide fab
            }
        }
        else{
            filesRecycler.setVisibility(View.VISIBLE);
            filesFiller.setVisibility(View.GONE);
            noFiles.setVisibility(View.GONE);

            pathFiller.setVisibility(View.GONE);
            pathRecycler.setVisibility(View.VISIBLE);

            filesRecycler.scrollToPosition(0);
        }

        if(pathScroll != -1)
            pathRecycler.scrollToPosition(pathScroll);
    }

    protected boolean getStoragePermissions(String error){
        MainActivity activity = (MainActivity)getActivity();
        return activity.requestStoragePermission(error);
    }

    public void refreshDir(){
        changeWorkingDirectory(dir);
        filesAdapter.sort(FilesAdapter.FileComparator.BY_NAME);
    }

}
