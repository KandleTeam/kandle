package ch.epfl.sdp.kandle.dependencies;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;

import ch.epfl.sdp.kandle.ConnexionStatus;
import ch.epfl.sdp.kandle.Kandle;
import ch.epfl.sdp.kandle.NetworkState;


public class UserNetworkStatus implements NetworkState {
    private ConnexionStatus connexionStatus;
    private static final UserNetworkStatus INSTANCE = new UserNetworkStatus();
    private UserNetworkStatus() {

    }

    public static UserNetworkStatus getInstance() {
        return INSTANCE;
    }

    public boolean isConnected() {
        Boolean connected;
        ConnectivityManager cm = (ConnectivityManager) Kandle.getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        //For Api 29
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            NetworkCapabilities capabilities = cm.getNetworkCapabilities(cm.getActiveNetwork());
            connected = capabilities != null && (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) || capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) || capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET));
            //For lower Api versions
        } else {
            NetworkInfo info = cm.getActiveNetworkInfo();
            connected = info != null && info.isConnectedOrConnecting();
        }

        if(connected){
            connexionStatus = ConnexionStatus.ONLINE;
        }else{
            connexionStatus = ConnexionStatus.OFFLINE;
        }
        return connected;
    }

}
