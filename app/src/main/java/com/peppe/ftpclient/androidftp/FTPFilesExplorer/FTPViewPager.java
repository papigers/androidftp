package com.peppe.ftpclient.androidftp.FTPFilesExplorer;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.peppe.ftpclient.androidftp.FTPClientMain.FTPConnection;
import com.peppe.ftpclient.androidftp.FTPClientMain.MainActivity;
import com.peppe.ftpclient.androidftp.R;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;

import de.greenrobot.event.EventBus;

//TODO: add MyFTPClient "bridge"
public class FTPViewPager extends Fragment {
    private static final String CONNECT = "connect";
    private static final String TAG = "FTP_PAGER";

    private FTPConnection connection;
    private FTPPagerAdapter adapter;
    private FTPClient client;
    private View v;

    private EventBus bus = EventBus.getDefault();

    private Menu mMenu;

    public static FTPViewPager newInstance(FTPConnection connection) {
        FTPViewPager fragment = new FTPViewPager();
        Bundle args = new Bundle();
        args.putSerializable(CONNECT, connection);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        if (getArguments() != null) {
            this.connection = (FTPConnection)getArguments().getSerializable(CONNECT);
            //do something when on start
            if(connection == null){

            }
            else{
                if(client==null)
                    client = new FTPClient();
                new FTPConnectTask().execute(connection);
            }
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        v = inflater.inflate(R.layout.fragment_ftpview_pager, container, false);

        return v;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        MainActivity activity =(MainActivity)getActivity();
        switch (item.getItemId()) {
            case android.R.id.home:
                FragmentManager fm = getActivity().getSupportFragmentManager();
                if (fm.getBackStackEntryCount() > 0) {
                    fm.popBackStack();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private class FTPConnectTask extends AsyncTask<FTPConnection,Void,FTPClient> {

        private final String TAG = "CONNECT_TASK";

        @Override
        protected void onPostExecute(FTPClient c) {
            super.onPostExecute(c);
            if (c != null) {
                if(c.isConnected()) {
                    ViewPager pager = (ViewPager) FTPViewPager.this.v.findViewById(R.id.ftpViewPager);
                    pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                        @Override
                        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                        }

                        @Override
                        public void onPageSelected(int position) {
                            MainActivity activity = ((MainActivity) getActivity());
                            if (position == 0) {
                                //activity.setTitle("Remote");
                                Log.d(FTPViewPager.TAG, "remote alive 1");
                                activity.isRemoteAlive = true;
                                activity.isLocalAlive = false;
                            } else {
                                //activity.setTitle("Local");
                                Log.d(FTPViewPager.TAG, "local alive 1");
                                activity.isRemoteAlive = false;
                                activity.isLocalAlive = true;
                            }
                        }

                        @Override
                        public void onPageScrollStateChanged(int state) {
                        }
                    });
                    pager.setVisibility(View.VISIBLE);
                    ProgressBar progress = (ProgressBar) FTPViewPager.this.v.findViewById(R.id.loadConnectionProgressBar);
                    progress.setVisibility(View.GONE);
                    adapter = new FTPPagerAdapter(c, (MainActivity) getActivity(), getChildFragmentManager());
                    pager.setAdapter(adapter);

                }
                else{
                    Toast t = ((MainActivity)getActivity()).commonToast;
                    t.setText("Authentication failed...");
                    t.show();
                    getActivity().onBackPressed();
                }
            }
            else if(c==null){
                Toast t = ((MainActivity)getActivity()).commonToast;
                t.setText("Failed to connect...");
                t.show();
                getActivity().onBackPressed();
            }
        }

        @Override
        protected FTPClient doInBackground(FTPConnection... params) {
            FTPConnection connection = params[0];
            boolean suc = false;
            try {
                FTPClient client = new FTPClient();
                client.connect(connection.getHost(), connection.getPort());

                if (FTPReply.isPositiveCompletion(client.getReplyCode())) {
                    Log.d(TAG, "connected!");
                    boolean status = client.login(connection.getUser(), connection.getPass());
                    client.setFileType(FTP.BINARY_FILE_TYPE);
                    client.enterLocalPassiveMode();
                    suc = status;
                }
                if (suc) {
                    Log.d(TAG, "connected and authenticated!");
                    return client;
                }
                else {
                    Log.d(TAG, "failed to authenticate!");
                    client.disconnect();
                    return client;
                }

            }
            catch (Exception e) {
                Log.e(TAG, e.toString());
                Log.d(TAG, "Error: could not connect to host " + connection.getHost());
            }

            return null;
        }

    }

}
