package com.peppe.ftpclient.androidftp.FTPFilesExplorer.FTPRemoteExplorer;

import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;

import com.peppe.ftpclient.androidftp.FTPFilesExplorer.FilesAdapter;
import com.peppe.ftpclient.androidftp.FTPFilesExplorer.FilesFragment;
import com.peppe.ftpclient.androidftp.R;

import org.apache.commons.net.ftp.FTPFile;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by Geri on 24/10/2015.
 */
public class RemoteFilesAdapter  extends FilesAdapter<FTPFile> {

    private static final String TAG = "REMOTE_FILES_ADAPTER";

    public RemoteFilesAdapter(FilesFragment fragment) {
        super(fragment);
    }

    @Override
    public ArrayList<Integer> getSelectedIndices() {
        ArrayList<Integer> items =
                new ArrayList<Integer>(selectedItems.size());
        for (int i = 0; i < selectedItems.size(); i++) {
            items.add(selectedItems.keyAt(i));
        }
        return items;
    }

    @Override
    public ArrayList<FTPFile> getSelectedItems() {
        ArrayList<FTPFile> items =
                new ArrayList<>(selectedItems.size());
        for (int i = 0; i < selectedItems.size(); i++) {
            items.add(dataset.get(selectedItems.keyAt(i)));
        }
        return items;
    }

    @Override
    public ArrayList<String> getSelectedNames() {
        ArrayList<String> names =
                new ArrayList<>(selectedItems.size());
        for (int i = 0; i < selectedItems.size(); i++) {
            names.add(dataset.get(selectedItems.keyAt(i)).getName());
        }
        return names;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        FTPFile ftpFile = dataset.get(position);
        holder.getNameTextView().setText(ftpFile.getName());
        String size = convertToStringRepresentation(ftpFile.getSize());
        Calendar ctime = ftpFile.getTimestamp();
        SimpleDateFormat formatter = new SimpleDateFormat("MMM d, yyyy hh:mm a");
        String time = formatter.format(ctime.getTime());
        if(ftpFile.isFile())
            holder.getInfoTextView().setText(size + " - " + time);
        else
            holder.getInfoTextView().setText(time);
        fileOnClickListener listener = new fileOnClickListener(fragment, ftpFile);
        holder.itemView.setOnClickListener(listener);
        holder.itemView.setOnLongClickListener(listener);
        if(ftpFile.isFile()){
            holder.getImageView().setImageResource(android.R.drawable.ic_menu_delete);
        }
        else
            holder.getImageView().setImageResource(android.R.drawable.ic_menu_gallery);
        boolean activate = (isSelected(position) || (fragment.cutFiles != null && fragment.cutFiles.contains(ftpFile.getName())));
        holder.itemView.setActivated(activate);
    }

    public boolean isDirectory(FTPFile dir){
        return dir.isDirectory();
    }

    protected class fileOnClickListener implements View.OnClickListener, View.OnLongClickListener {
        FilesFragment fragment;
        FTPFile file;

        public fileOnClickListener(FilesFragment fragment, FTPFile file) {
            this.file = file;
            this.fragment = fragment;
        }

        @Override
        public boolean onLongClick(View v) {
            Log.d(TAG, (file.isDirectory() ? "Directory ": "File ") + file.getName() + " long clicked.");
            if(!fragment.isPasteMode()) {
                if (isSelecting() && file.isDirectory()) {
                    final View view = v;
                    PopupMenu menu = new PopupMenu(fragment.getActivity(), v);
                    menu.getMenuInflater().inflate(R.menu.folder_popup_menu, menu.getMenu());
                    menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            switch (item.getItemId()) {
                                case R.id.folderPopupOpen:
                                    fragment.openDirecory(file.getName());
                                    return true;
                                case R.id.folderPopupSelect:
                                    fragment.selectItem(view);
                                    return true;
                                default:
                                    return false;
                            }
                        }
                    });
                    menu.show();
                } else {
                    fragment.selectItem(v);
                }
            }
            /* old
            if (isDirectory(file)) {
                Log.d(TAG, "Directory " + file.getName() + " long clicked.");
                DownloadDialog dialog = DownloadDialog.newInstance(file);
                dialog.setTargetFragment(fragment, 1);
                FragmentManager fm = fragment.getActivity().getSupportFragmentManager();
                dialog.show(fm, "Download");
            }
            else {
                Log.d(TAG, "File " + file.getName() + " long clicked.");
                fragment.selectItem(v);
            }
            */
            return true;
        }

        @Override
        public void onClick(View v) {
            if(isSelecting() && !fragment.isPasteMode()) {
                fragment.selectItem(v);
            }
            else {
                if (isDirectory(file)) {
                    if(!(fragment.cutFiles != null && fragment.cutFiles.contains(file.getName()))) {
                        Log.d(TAG, "Directory " + file.getName() + " clicked.");
                        fragment.openDirecory(file.getName());
                    }
                } else {
                    if(!fragment.isPasteMode()) {
                        Log.d(TAG, "File " + file.getName() + " clicked.");
                        DownloadDialog dialog = DownloadDialog.newInstance(file);
                        dialog.setTargetFragment(fragment, 1);
                        FragmentManager fm = fragment.getActivity().getSupportFragmentManager();
                        dialog.show(fm, "Download");
                    }
                }
            }
        }
    }
}
