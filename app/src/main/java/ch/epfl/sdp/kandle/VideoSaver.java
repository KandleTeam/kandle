package ch.epfl.sdp.kandle;

import android.util.Log;
import androidx.annotation.GuardedBy;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.core.VideoCapture;
import androidx.camera.core.VideoCapture.OnVideoSavedCallback;
import androidx.camera.core.VideoCapture.VideoCaptureError;
import java.io.File;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
/**
 * Basic functionality required for interfacing the {@link VideoCapture}.
 */
public class VideoSaver implements OnVideoSavedCallback {
    private static final String TAG = "VideoSaver";
    private final Format mFormatter = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", Locale.ENGLISH);
    private final Object mLock = new Object();
    private File mRootDirectory;
    @GuardedBy("mLock")
    private boolean mIsSaving = false;
    @Override
    public void onVideoSaved(@NonNull File file) {
        Log.d(TAG, "Saved file: " + file.getPath());
        synchronized (mLock) {
            mIsSaving = false;
        }
    }
    @Override
    public void onError(@VideoCaptureError int videoCaptureError, @NonNull String message,
                        @Nullable Throwable cause) {
        Log.e(TAG, "Error: " + videoCaptureError + ", " + message);
        if (cause != null) {
            Log.e(TAG, "Error cause: " + cause.getCause());
        }
        synchronized (mLock) {
            mIsSaving = false;
        }
    }
    /** Returns a new {@link File} where to save a video. */
    public File getNewVideoFile() {
        Date date = Calendar.getInstance().getTime();
        File file = new File(mRootDirectory + "/" + mFormatter.format(date) + ".mp4");
        return file;
    }
    /** Sets the directory for saving files. */
    public void setRootDirectory(File rootDirectory) {
        mRootDirectory = rootDirectory;
    }
    public boolean isSaving() {
        synchronized (mLock) {
            return mIsSaving;
        }
    }
    /** Sets saving state after video startRecording */
    public void setSaving() {
        synchronized (mLock) {
            mIsSaving = true;
        }
    }
}