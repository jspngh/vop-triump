package be.ugent.vop;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

/**
 * Created by vincent on 19/03/15.
 */
public class NetworkController extends BroadcastReceiver {
    private static NetworkController instance;
    private boolean connection;

    private NetworkController(Context context){
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        context.registerReceiver(this, filter);

        ConnectivityManager connMgr = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        connection = (networkInfo != null && networkInfo.isConnected());
    }

    public static NetworkController get(Context context){
        if(instance==null){
            instance = new NetworkController((context));
        }
        return instance;
    }

    public static NetworkController get(){
        return instance;
    }

    public boolean isNetworkOnline() {
        return connection;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        ConnectivityManager conn = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = conn.getActiveNetworkInfo();
        connection = (networkInfo != null && networkInfo.isConnected());
        if(!connection){
            Toast.makeText(context, R.string.lost_connection, Toast.LENGTH_LONG).show();
        }

    }
}