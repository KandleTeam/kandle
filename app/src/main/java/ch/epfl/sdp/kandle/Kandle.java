package ch.epfl.sdp.kandle;

import android.app.Application;
import android.content.Context;

public class Kandle extends Application {
    private static Kandle mContext;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
    }

    public static Kandle getContext() {
        return mContext;
    }
}
