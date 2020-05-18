package ch.epfl.sdp.kandle.network;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;

import ch.epfl.sdp.kandle.Kandle;
<<<<<<< Updated upstream
=======
import ch.epfl.sdp.kandle.LoggedInUser;
import ch.epfl.sdp.kandle.dependencies.Database;
import ch.epfl.sdp.kandle.dependencies.DependencyManager;
import ch.epfl.sdp.kandle.storage.caching.CachedFirestoreDatabase;
import ch.epfl.sdp.kandle.storage.firebase.FirestoreDatabase;
>>>>>>> Stashed changes


public class UserNetworkStatus implements NetworkState {

    private static final UserNetworkStatus INSTANCE = new UserNetworkStatus();
<<<<<<< Updated upstream


=======
    private Database database = new CachedFirestoreDatabase();
>>>>>>> Stashed changes
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

<<<<<<< Updated upstream


        return connected;
    }



=======
        System.out.println("Previously"+previouslyConnected);
        System.out.println("Right now " + connected);

        if(!previouslyConnected && connected){
            System.out.println("Passing in the condition that we had false and true");
            onNetworkChange();

        }

        if(previouslyConnected != connected) {
            previouslyConnected = connected;
        }
        return connected;
    }

    private void onNetworkChange(){
        DependencyManager.getDatabaseSystem().updateHighScore(LoggedInUser.getInstance().getHighScore());
        System.out.println("yo");
    }
>>>>>>> Stashed changes

}
