package com.peppe.ftpclient.androidftp.FTPClientMain;

import android.util.Log;

import java.io.Serializable;

/**
 * Created by Geri on 11/10/2015.
 */
public class FTPConnection implements Serializable{
    private final String TAG = "CONNECTION";
    private static int counter = 0;
    private int id;
    private String name;
    private String host;
    private String user;

    public void setId(int id) {
        this.id = id;
    }

    private String pass;
    private int port;
    private enum Protocol {
        FTP, FTPS;
        public int toIndex(){
            if(this == FTP)
                return 0;
            else
                return 1;
        }
        public String toString(){
            if(this == FTP)
                return "FTP";
            else
                return "FTPS";
        }
    };
    private Protocol  protocol;
    private final String ANONYMOUS_USER = "anonymous";
    private String dir;
    private String[] files;


    public FTPConnection(){
        id = counter++;
        name = "TEST";
        host = "speedtest.tele2.net";
        user = ANONYMOUS_USER;
        pass = "";
        port = 21;
        protocol = Protocol.FTP;
    }

    public FTPConnection(int id, String name, String host, String user, String pass, int port, String prot){
        this.id = id;
        this.name = name;
        this.host = host;
        this.user =  user;
        this.pass = pass;
        this.port = port;
        this.protocol = (prot.equals("FTP") ? Protocol.FTP : Protocol.FTPS);
    }

    public FTPConnection(String name, String host, String user, String pass, int port, Protocol prot){
        this.id = counter++;
        this.name = name;
        this.host = host;
        this.user =  user;
        this.pass = pass;
        this.port = port;
        this.protocol = prot;
    }
    public FTPConnection(String name, String host, String user, String pass, int port, String prot){
        this.id = counter++;
        this.name = name;
        this.host = host;
        this.user =  user;
        this.pass = pass;
        this.port = port;
        this.protocol = (prot.equals("FTP") ? Protocol.FTP : Protocol.FTPS);
    }

    public FTPConnection(String name, String host, String user, String pass, int port, int prot){
        this.id = counter++;
        this.name = name;
        this.host = host;
        this.user =  user;
        this.pass = pass;
        this.port = port;
        this.protocol = (prot == 0 ? Protocol.FTP : Protocol.FTPS);
    }

    public FTPConnection(FTPConnection connection){
        this.id = connection.id;
        this.name = connection.name;
        this.host = connection.host;
        this.user = connection.user;
        this.pass = connection.pass;
        this.port = connection.port;
        this.protocol = connection.protocol;
    }

    public void editConnection(FTPConnection connection){
        this.id = connection.id;
        this.name = connection.name;
        this.host = connection.host;
        this.user = connection.user;
        this.pass = connection.pass;
        this.port = connection.port;
        this.protocol = connection.protocol;
    }

    public boolean isAnonymous(){
        return user.equals("anonymous");
    }

    public int getId() { return id; }

    public String getName(){
        return name;
    }

    public String getHost(){
        return host;
    }

    public String getUser(){
        return user;
    }

    public String getPass(){
        return pass;
    }

    public int getPort(){
        return port;
    }

    public String getStringProtocol() {
        return protocol.toString();
    }

    public int getIndexProtocol(){
        return protocol.toIndex();
    }

    public String getWorkingDir(){
        return dir;
    }

    public void setWorkingDir(String dir){
        this.dir = dir;
    }

    public String[] getFiles(){
        return files;
    }

    public void setFiles(String[] files){
        if(files == null) Log.d(TAG, "files is null");
        this.files = new String[files.length];
        for(int i = 0; i<files.length; i++){
            this.files[i] = new String(files[i]);
            Log.i(TAG, "File : " + this.files[i]);
        }
    }
}
