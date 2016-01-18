package com.peppe.ftpclient.androidftp.FTPConnectionsList;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.peppe.ftpclient.androidftp.FTPClientMain.FTPConnection;
import com.peppe.ftpclient.androidftp.FTPClientMain.MainActivity;
import com.peppe.ftpclient.androidftp.R;

/**
 * Created by Geri on 14/10/2015.
 */
public class ConnectionCursorAdapter extends SimpleCursorAdapter {
    protected ListView listView;
    protected FTPConnectionsDBHelper dbHelper;
    protected MainActivity mActivity;
    public static final String TAG = "CONNECTION_ADAPTER";

    protected static class RowViewHolder {
        public TextView userTextView;
        public TextView nameTextView;
        public TextView hostTextView;
        public TextView atTextView;
        public ImageView edit;
        public ImageView delete;

        public RowViewHolder(View view){
            nameTextView = (TextView) view.findViewById(R.id.connectionNameTextView);
            userTextView = (TextView) view.findViewById(R.id.connectionUserTextView);
            hostTextView = (TextView) view.findViewById(R.id.connectionHostTextView);
            atTextView = (TextView) view.findViewById(R.id.connectionAtTextView);
            edit = (ImageView) view.findViewById(R.id.connectionEditImageButton);
            delete = (ImageView) view.findViewById(R.id.connectionDeleteImageButton);
        }
    }
    /**
     * Standard constructor.
     *
     * @param context The context where the ListView associated with this
     *                SimpleListItemFactory is running
     * @param layout  resource identifier of a layout file that defines the views
     *                for this list item. The layout file should include at least
     *                those named views defined in "to"
     * @param c       The database cursor.  Can be null if the cursor is not available yet.
     * @param from    A list of column names representing the data to bind to the UI.  Can be null
     *                if the cursor is not available yet.
     * @param to      The views that should display column in the "from" parameter.
     *                These should all be TextViews. The first N views in this list
     *                are given the values of the first N columns in the from
     *                parameter.  Can be null if the cursor is not available yet.
     * @param flags   Flags used to determine the behavior of the adapter,
     *                as per {@link CursorAdapter#CursorAdapter(Context, Cursor, int)}.
     */
    public ConnectionCursorAdapter(FTPConnectionsDBHelper dbHelper, ListView listView,
                                   Context context, int layout, Cursor c, String[] from, int[] to, int flags) {
        super(context, layout, c, from, to, flags);
        this.listView = listView;
        this.dbHelper = dbHelper;
        this.mActivity = (MainActivity)context;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        super.bindView(view, context, cursor);
        RowViewHolder holder = new RowViewHolder(view);

        if(cursor.getString(cursor.getColumnIndexOrThrow(FTPConnectionsDBHelper.KEY_NAME)).equals("")){
            holder.nameTextView.setText(holder.hostTextView.getText());
            holder.atTextView.setVisibility(View.GONE);
            holder.hostTextView.setVisibility(View.GONE);
        }
        else{
            holder.atTextView.setVisibility(View.VISIBLE);
            holder.hostTextView.setVisibility(View.VISIBLE);
        }

        holder.edit.setOnClickListener(onEditButtonClickListener);

        holder.delete.setOnClickListener(onDeleteButtonClickListener);

        view.setTag(holder);
    }

    /**
     * Inflates view(s) from the specified XML file.
     *
     * @param context
     * @param cursor
     * @param parent
     * @see CursorAdapter#newView(Context,
     * Cursor, ViewGroup)
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = View.inflate(context, R.layout.ftpconnection_item, null);
        return view;
    }

    private OnClickListener onEditButtonClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            final int position = listView.getPositionForView((View) v.getParent());
            Log.v(TAG, "edit button clicked, row "+position);

            Cursor cursor = getCursor();
            cursor.moveToPosition(position);
            int id = cursor.getInt(cursor.getColumnIndexOrThrow(FTPConnectionsDBHelper.KEY_ROWID));
            String name = cursor.getString(cursor.getColumnIndexOrThrow(FTPConnectionsDBHelper.KEY_NAME));
            String host = cursor.getString(cursor.getColumnIndexOrThrow(FTPConnectionsDBHelper.KEY_HOST));
            String user = cursor.getString(cursor.getColumnIndexOrThrow(FTPConnectionsDBHelper.KEY_USER));
            String pass = cursor.getString(cursor.getColumnIndexOrThrow(FTPConnectionsDBHelper.KEY_PASS));
            int port = cursor.getInt(cursor.getColumnIndexOrThrow(FTPConnectionsDBHelper.KEY_PORT));
            String protocol = cursor.getString(cursor.getColumnIndexOrThrow(FTPConnectionsDBHelper.KEY_PROTOCOL));

            FTPConnection connection = new FTPConnection(id,name, host, user, pass, port, protocol);

            mActivity.startEditConnection(connection);

            /*ContentValues cv = new ContentValues();
            cv.put(FTPConnectionsDBHelper.KEY_NAME, "TEST_EDIT");
            dbHelper.updateFTPConnection(id, cv);
            changeCursor(dbHelper.fetchAllData());
            notifyDataSetChanged();*/
        }
    };

    private OnClickListener onDeleteButtonClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            final int position = listView.getPositionForView((View) v.getParent());
            Log.v(TAG, "delete button clicked, row " + position);
            Cursor cursor = getCursor();
            cursor.moveToPosition(position);
            int id = cursor.getInt(cursor.getColumnIndexOrThrow(FTPConnectionsDBHelper.KEY_ROWID));
            /*
            String name = cursor.getString(cursor.getColumnIndexOrThrow(FTPConnectionsDBHelper.KEY_NAME));
            String host = cursor.getString(cursor.getColumnIndexOrThrow(FTPConnectionsDBHelper.KEY_HOST));
            String user = cursor.getString(cursor.getColumnIndexOrThrow(FTPConnectionsDBHelper.KEY_USER));
            String pass = cursor.getString(cursor.getColumnIndexOrThrow(FTPConnectionsDBHelper.KEY_PASS));
            int port = cursor.getInt(cursor.getColumnIndexOrThrow(FTPConnectionsDBHelper.KEY_PORT));
            String protocol = cursor.getString(cursor.getColumnIndexOrThrow(FTPConnectionsDBHelper.KEY_PROTOCOL));

            FTPConnection connection = new FTPConnection(name, host, user, pass, port, protocol);
            */

            dbHelper.deleteFTPConnection(id);
            changeCursor(dbHelper.fetchAllData());
            notifyDataSetChanged();
        }
    };
}
