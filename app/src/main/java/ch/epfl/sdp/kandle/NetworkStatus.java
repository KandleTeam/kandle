package ch.epfl.sdp.kandle;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;


public class NetworkStatus {
    private static ConnexionStatus connexionStatus;
    public NetworkStatus() {

    }

    public static boolean isConnected() {
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


    public static ConnexionStatus getUpdatedConnexionStatus(){
       return connexionStatus;
    }
}
