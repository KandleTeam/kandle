package ch.epfl.sdp.kandle;

import android.app.Application;

import ch.epfl.sdp.kandle.network.ConnexionStatus;

public class Kandle extends Application {

    private static Kandle mContext;
    private static ConnexionStatus connexionStatus;

    public void onCreate() {
        super.onCreate();
        mContext = this;
    }

    public static Kandle getContext() {
        return mContext;
    }

}
