package ch.epfl.sdp.kandle.network;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;

import ch.epfl.sdp.kandle.Kandle;


public class UserNetworkStatus implements NetworkState {

    private static final UserNetworkStatus INSTANCE = new UserNetworkStatus();

    private UserNetworkStatus() {

    }

    public static UserNetworkStatus getInstance() {
        return INSTANCE;
    }

    public boolean isConnected() {
        boolean connected;
        ConnectivityManager cm = (ConnectivityManager) Kandle.getContext().getSystemService(Context.CONNECTIVITY_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) { //For Api 29

            NetworkCapabilities capabilities = cm.getNetworkCapabilities(cm.getActiveNetwork());

            connected = capabilities != null &&
                    (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                            || capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
                            || capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR));


        } else { //For lower Api versions
            NetworkInfo info = cm.getActiveNetworkInfo();
            connected = info != null && info.isConnectedOrConnecting();
        }

        return connected;
    }

}
