package ch.epfl.sdp.kandle;

import android.app.Application;


public class Kandle extends Application {

    private static Kandle mContext;


    public void onCreate() {
        super.onCreate();
        mContext = this;
    }

    public static Kandle getContext() {
        return mContext;
    }

}
