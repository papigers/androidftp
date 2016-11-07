package com.peppe.ftpclient.androidftp.FTPFilesExplorer;

import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.peppe.ftpclient.androidftp.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * Created by Geri on 19/10/2015.
 */
public abstract class FilesAdapter<E> extends RecyclerView.Adapter<FilesAdapter.ViewHolder>{
    private static final String TAG = "FILES_ADAPTER";
    protected FilesFragment fragment;
    public ArrayList<E> dataset;

    //********************** ITEM SELECTION **********************//

    protected SparseBooleanArray selectedItems;
    protected ArrayList<String> cutItems;

    public void cutSelection(){
        cutItems = getSelectedNames();
        selectedItems.clear();
        notifyDataSetChanged();
    }

    public boolean toggleSelection(int pos) {
        boolean ans = false;
        if (selectedItems.get(pos, false)) {
            selectedItems.delete(pos);
        }
        else {
            selectedItems.put(pos, true);
            ans = true;
        }
        notifyItemChanged(pos);
        return ans;
    }

    public boolean isSelected(int pos){
        return selectedItems.get(pos,false);
    }

    public boolean isCut(String name){
        return fragment.dir.equals(fragment.cutSource) && cutItems.contains(name);
    }

    public void clearSelections() {
        selectedItems.clear();
        notifyDataSetChanged();
    }

    public void clearCuts() {
        cutItems.clear();
        notifyDataSetChanged();
    }


    public int getSelectedItemCount() {
        return selectedItems.size();
    }

    public int getCutItemCount() {
        return cutItems.size();
    }

    public boolean isSelecting() { return getSelectedItemCount() != 0; }

    public ArrayList<Integer> getSelectedIndices() {
        ArrayList<Integer> items =
                new ArrayList<Integer>(selectedItems.size());
        for (int i = 0; i < selectedItems.size(); i++) {
            items.add(selectedItems.keyAt(i));
        }
        return items;
    }

    public abstract ArrayList<E> getSelectedItems();

    public ArrayList<String> getCutNames(){
        return cutItems;
    }

    public abstract ArrayList<String> getSelectedNames();

    //********************** END ITEM SELECTION **********************//

    public FilesAdapter(FilesFragment fragment) {
        this.fragment = fragment;
        this.selectedItems = new SparseBooleanArray();
        this.cutItems = new ArrayList<>();
    }

    protected abstract void sort(int mode);

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.files_recycler_item, parent, false);

        return new ViewHolder(v);
    }

    public String convertToStringRepresentation(long value) {
        final long K = 1024;
        final long M = K * K;
        final long G = M * K;
        final long T = G * K;
        final long[] dividers = new long[]{T, G, M, K, 1};
        final String[] units = new String[]{"TB", "GB", "MB", "KB", "B"};
        String result = "0 B";
        for (int i = 0; i < dividers.length; i++) {
            final long divider = dividers[i];
            if (value >= divider) {
                result = format(value, divider, units[i]);
                break;
            }
        }
        //Log.d(TAG, "value: "+value+", result: "+result);
        return result;
    }

    private String format(final long value,
                          final long divider,
                          final String unit) {
        final double result =
                divider > 1 ? (double) value / (double) divider : (double) value;
        return String.format("%.1f %s", result, unit);
    }

    @Override
    public abstract void onBindViewHolder(ViewHolder holder, int position);

    public void setDataset(E[] dataset) {
        if (dataset == null) {
            Log.d(TAG, "file dataset null");
            this.dataset = new ArrayList<>();
        }
        else {
            /*
            ArrayList<E> dirs = new ArrayList<>();
            ArrayList<E> files = new ArrayList<>();
            for (E dir : dataset) {
                if (isDirectory(dir))
                    dirs.add(dir);
                else
                    files.add(dir);
            }
            */
            if (this.dataset == null)
                this.dataset = new ArrayList<>();
            animateTo(Arrays.asList(dataset));
            sort(FileComparator.BY_NAME);

            /*
            int i = 0;
            for (i = 0; i < dirs.size(); i++)
                this.dataset.add(dirs.get(i));
            for (i = 0; i < files.size(); i++)
                this.dataset.add(files.get(i));
                */
        }

    }

    public abstract boolean isDirectory(E dir);

    @Override
    public int getItemCount() {
        if (dataset == null)
            return 0;
        return dataset.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView nameTextView;
        private final TextView infoTextView;
        private final ImageView imageView;
        public final String TAG = "FILES_VH";
        private View v;

        public ViewHolder(View v) {
            super(v);
            this.v = v;
            // Define click listener for the ViewHolder's View.
            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG, "Element " + getPosition() + " clicked.");
                    //here i try to open the dialog fragment.

                }
            });
            nameTextView = (TextView) v.findViewById(R.id.fileNameTextView);
            infoTextView = (TextView) v.findViewById(R.id.fileInfoTextView);
            imageView = (ImageView) v.findViewById(R.id.fileImageView);
        }

        public void setOnClickListener(View.OnClickListener listener) {
            v.setOnClickListener(listener);
        }

        public TextView getNameTextView() {
            return nameTextView;
        }

        public ImageView getImageView() {
            return imageView;
        }

        public TextView getInfoTextView() {
            return infoTextView;
        }
    }

    protected abstract class FileComparator implements Comparator<E>{
        public static final int BY_NAME = 101;
        public static final int BY_SIZE = 102;
        public static final int BY_TYPE = 103;
        public static final int BY_TIME = 104;

        protected int mode;

        public FileComparator(int mode){
            this.mode = mode;
        }

        protected String getName(String fullName){
            if(fullName.indexOf('.')<0)
                return fullName;
            else
                return fullName.substring(0, fullName.indexOf('.'));
        }

        protected String getExt(String fullName){
            if(fullName.indexOf('.')<0)
                return "";
            else
                return fullName.substring(fullName.indexOf('.')+1);
        }
    }

    public E removeItem(int position) {
        final E file = dataset.remove(position);
        notifyItemRemoved(position);
        return file;
    }

    public void addItem(int position, E file) {
        dataset.add(position, file);
        notifyItemInserted(position);
    }

    public void moveItem(int fromPosition, int toPosition) {
        final E file = dataset.remove(fromPosition);
        dataset.add(toPosition, file);
        notifyItemMoved(fromPosition, toPosition);
    }

    private void applyAndAnimateRemovals(List<E> newData) {
        for (int i = dataset.size() - 1; i >= 0; i--) {
            final E file = dataset.get(i);
            if (!newData.contains(file)) {
                removeItem(i);
            }
        }
    }

    private void applyAndAnimateAdditions(List<E> newData) {
        for (int i = 0; i < newData.size(); i++) {
            final E file = newData.get(i);
            if (!dataset.contains(file)) {
                addItem(i, file);
            }
        }
        /*
        for (int i = newData.size() - 1; i >= 0; i--) {
            final E file = newData.get(i);
            if (!dataset.contains(file)) {
                addItem(i, file);
            }
        }
        */
    }

    private void applyAndAnimateMovedItems(List<E> newData) {
        for (int toPosition = newData.size() - 1; toPosition >= 0; toPosition--) {
            final E file = newData.get(toPosition);
            final int fromPosition = dataset.indexOf(file);
            if (fromPosition >= 0 && fromPosition != toPosition) {
                moveItem(fromPosition, toPosition);
            }
        }
    }

    public void animateTo(List<E> newData) {
        applyAndAnimateRemovals(newData);
        applyAndAnimateAdditions(newData);
        applyAndAnimateMovedItems(newData);
    }


}
