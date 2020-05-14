package ch.epfl.sdp.kandle.network;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;

import ch.epfl.sdp.kandle.Kandle;
import ch.epfl.sdp.kandle.dependencies.Database;
import ch.epfl.sdp.kandle.dependencies.DependencyManager;
import ch.epfl.sdp.kandle.storage.caching.CachedFirestoreDatabase;
import ch.epfl.sdp.kandle.storage.firebase.FirestoreDatabase;


public class UserNetworkStatus implements NetworkState {

    private static boolean previouslyConnected = true;
    private static final UserNetworkStatus INSTANCE = new UserNetworkStatus();
    //private Database database = new CachedFirestoreDatabase();
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

        System.out.println(previouslyConnected);
        System.out.println(connected);

        if(!previouslyConnected && connected){
            System.out.println("Passing in the condition that we had false and true");
            onNetworkChange();
        }

        previouslyConnected = connected;
        return connected;
    }

    private void onNetworkChange(){
        //database.updateHighScore(DependencyManager.getAuthSystem().getCurrentUser().getHighScore());
        System.out.println("yo");
    }

}
