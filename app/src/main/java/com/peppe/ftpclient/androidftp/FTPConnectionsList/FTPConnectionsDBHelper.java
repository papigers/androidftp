package com.peppe.ftpclient.androidftp.FTPConnectionsList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.peppe.ftpclient.androidftp.FTPClientMain.FTPConnection;

/**
 * Created by Geri on 13/10/2015.
 */
public class FTPConnectionsDBHelper  {
    public static final String KEY_ROWID = "_id";
    public static final String KEY_NAME = "_name";
    public static final String KEY_HOST = "_host";
    public static final String KEY_USER = "_user";
    public static final String KEY_PASS = "_pass";
    public static final String KEY_PORT = "_port";
    public static final String KEY_PROTOCOL = "_protocol";

    public static final String TAG = "ConnectionsDBHelper";
    private DatabaseHelper DbHelper;
    private SQLiteDatabase Db;

    public static final String DATABASE_NAME = "FTPClient";
    public static final String SQLITE_TABLE = "Connections";

    private Context context;

    public static final String DATABASE_CREATE =
            "CREATE TABLE if not exists " + SQLITE_TABLE + " (" +
                    KEY_ROWID + " integer PRIMARY KEY autoincrement," +
                    KEY_NAME + "," +
                    KEY_HOST + "," +
                    KEY_USER + "," +
                    KEY_PASS + "," +
                    KEY_PORT + "," +
                    KEY_PROTOCOL + ");";

    private static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, 1);
        }


        @Override
        public void onCreate(SQLiteDatabase db) {
            Log.w(TAG, DATABASE_CREATE);
            db.execSQL(DATABASE_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS " + SQLITE_TABLE);
            onCreate(db);
        }
    }

    public FTPConnectionsDBHelper(Context ctx) {
        this.context = ctx;
    }

    public FTPConnectionsDBHelper open() throws SQLException {
        DbHelper = new DatabaseHelper(context);
        Db = DbHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        if (DbHelper != null) {
            DbHelper.close();
        }
    }

    public SQLiteDatabase getWritableDatabase(){
        return DbHelper.getWritableDatabase();
    }

    public SQLiteDatabase getReadableDatabase(){
        return DbHelper.getReadableDatabase();
    }

    public long insertFTPConnection(FTPConnection insert){
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_NAME, insert.getName());
        initialValues.put(KEY_HOST, insert.getHost());
        initialValues.put(KEY_USER, insert.getUser());
        initialValues.put(KEY_PASS, insert.getPass());
        initialValues.put(KEY_PORT, Integer.toString(insert.getPort()));
        initialValues.put(KEY_PROTOCOL, insert.getStringProtocol());

        return Db.insert(SQLITE_TABLE, null, initialValues);
    }

    public long deleteFTPConnection(int del){
        long deleted = 0;
        try {
            deleted =Db.delete(SQLITE_TABLE, "_id = " + del, null);
        }
        catch (Exception e){
            Log.e(TAG, e.toString());
        }
        return deleted;
    }

    public int updateFTPConnection(int update, ContentValues cv){
        int updated =Db.update(SQLITE_TABLE,cv, "_id = "+ update, null);
        return updated;
    }

    public Cursor fetchAllData(){
        Cursor cur = Db.query(SQLITE_TABLE, new String[] {KEY_ROWID,
                        KEY_NAME, KEY_HOST, KEY_USER, KEY_PASS, KEY_PORT, KEY_PROTOCOL},
                null, null, null, null, null);

        if (cur != null) {
            cur.moveToFirst();
        }
        return cur;
    }
}
