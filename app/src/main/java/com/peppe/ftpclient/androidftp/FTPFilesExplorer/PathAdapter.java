package com.peppe.ftpclient.androidftp.FTPFilesExplorer;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.peppe.ftpclient.androidftp.R;

import java.util.ArrayList;

/**
 * Created by Geri on 19/10/2015.
 */
public class PathAdapter extends RecyclerView.Adapter<PathAdapter.ViewHolder> {
    private static final String TAG = "PATH_ADAPTER";
    protected ArrayList<String> dataset;
    protected FilesFragment fragment;

    public PathAdapter(FilesFragment fragment) {
        this.fragment = fragment;
    }


    public void setDataset(ArrayList<String> dataset) {
        if (dataset == null)
            Log.d(TAG, "path dataset null");
        this.dataset = dataset;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.path_recycler_item, parent, false);

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.getTextView().setText(dataset.get(position) + "  > ");
        String path = "";
        for (int i = 1; i <= position; i++) {
            path += dataset.get(i);
            if (dataset.get(i).charAt(dataset.get(i).length() - 1) != '/' && i != position)
                path += "/";
        }
        path = "/" + path;
        holder.setOnClickListener(new changeDirOnClickListener(path, fragment));
    }

    @Override
    public int getItemCount() {
        if (dataset == null)
            return 0;
        return dataset.size();
    }

    protected static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView textView;
        public final String TAG = "PATH_VH";
        private View v;

        public ViewHolder(View v) {
            super(v);
            // Define click listener for the ViewHolder's View.
            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG, "Element " + getPosition() + " clicked.");
                }
            });
            this.v = v;
            textView = (TextView) v.findViewById(R.id.pathTextView);
        }

        public void setOnClickListener(View.OnClickListener listener) {
            v.setOnClickListener(listener);
        }

        public TextView getTextView() {
            return textView;
        }
    }

    protected class changeDirOnClickListener implements View.OnClickListener {
        String path;
        FilesFragment fragment;

        public changeDirOnClickListener(String path, FilesFragment fragment) {
            this.path = path;
            this.fragment = fragment;
        }

        @Override
        public void onClick(View v) {
            Log.d(TAG, "Directory Path " + path + " clicked.");
            fragment.changeWorkingDirectory(path);
        }
    }
}
