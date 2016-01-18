package com.peppe.ftpclient.androidftp.FTPFilesExplorer.FTPLocalExplorer;

import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;

import com.peppe.ftpclient.androidftp.FTPFilesExplorer.FilesAdapter;
import com.peppe.ftpclient.androidftp.FTPFilesExplorer.FilesFragment;
import com.peppe.ftpclient.androidftp.R;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

/**
 * Created by Geri on 25/10/2015.
 */
public class LocalFilesAdapter extends FilesAdapter<File> {

    private static final String TAG = "LOCAL_FILES_ADAPTER";

    public LocalFilesAdapter(FilesFragment fragment) {
        super(fragment);
    }

    @Override
    public ArrayList<File> getSelectedItems() {
        ArrayList<File> items =
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
        holder.getNameTextView().setText(dataset.get(position).getName());
        String size = convertToStringRepresentation(dataset.get(position).length());
        SimpleDateFormat formatter = new SimpleDateFormat("MMM d, yyyy hh:mm a");
        String time = formatter.format(dataset.get(position).lastModified());
        if (dataset.get(position).isDirectory())
            holder.getInfoTextView().setText(time);
        else
            holder.getInfoTextView().setText(size + " - " + time);
        fileOnClickListener listener = new fileOnClickListener(fragment, dataset.get(position));
        holder.itemView.setOnLongClickListener(listener);
        holder.itemView.setOnClickListener(listener);
        if(dataset.get(position).isFile()){
            holder.getImageView().setImageResource(android.R.drawable.ic_menu_delete);
        }
        else
            holder.getImageView().setImageResource(android.R.drawable.ic_menu_gallery);
        holder.itemView.setActivated(isSelected(position));
    }

    @Override
    public boolean isDirectory(File dir) {
        return dir.isDirectory();
    }

    protected class fileOnClickListener implements View.OnClickListener, View.OnLongClickListener {
        FilesFragment fragment;
        File file;

        public fileOnClickListener(FilesFragment fragment, File file) {
            this.file = file;
            this.fragment = fragment;
        }

        @Override
        public boolean onLongClick(View v) {
            Log.d(TAG, (file.isDirectory() ? "Directory ": "File ") + file.getName() + " long clicked.");
            if(isSelecting() && file.isDirectory()){
                final View view = v;
                PopupMenu menu = new PopupMenu(fragment.getActivity(), v);
                menu.getMenuInflater().inflate(R.menu.folder_popup_menu, menu.getMenu());
                menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch(item.getItemId()){
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
            }
            else {
                fragment.selectItem(v);
            }
            /*
            if (isDirectory(file)) {
                Log.d(TAG, "Directory " + file.getName() + " long clicked.");
                UploadDialog dialog = UploadDialog.newInstance(file);
                dialog.setTargetFragment(fragment, 1);
                FragmentManager fm = fragment.getActivity().getSupportFragmentManager();
                dialog.show(fm, "Upload");
            }
            else {
                Log.d(TAG, "File " + file.getName() + " long clicked.");
            }
            */
            return true;
        }

        @Override
        public void onClick(View v) {
            if(isSelecting())
                fragment.selectItem(v);
            else {
                if (isDirectory(file)) {
                    Log.d(TAG, "Directory " + file.getName() + " clicked.");
                    fragment.openDirecory(file.getName());
                } else {
                    Log.d(TAG, "File " + file.getName() + " clicked.");
                    UploadDialog dialog = UploadDialog.newInstance(file);
                    dialog.setTargetFragment(fragment, 1);
                    FragmentManager fm = fragment.getActivity().getSupportFragmentManager();
                    dialog.show(fm, "Upload");
                }
            }
        }
    }


}
