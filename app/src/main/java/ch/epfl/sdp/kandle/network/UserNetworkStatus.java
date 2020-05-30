package ch.epfl.sdp.kandle.network;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;

import ch.epfl.sdp.kandle.Kandle;
import ch.epfl.sdp.kandle.entities.user.LoggedInUser;
import ch.epfl.sdp.kandle.dependencies.DependencyManager;

public class UserNetworkStatus implements NetworkState {

    private static boolean previouslyConnected = true;
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

        if(!previouslyConnected && connected){
            onNetworkChange();
        }

        if(previouslyConnected != connected) {
            previouslyConnected = connected;
        }
        return connected;
    }

    private void onNetworkChange(){
        DependencyManager.getDatabaseSystem().updateHighScore(LoggedInUser.getInstance().getHighScore());
    }

}