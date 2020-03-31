package ch.epfl.sdp.kandle.ImagePicker;

import android.app.Activity;
import android.app.ProgressDialog;
import android.net.Uri;

import com.google.android.gms.tasks.Task;

import ch.epfl.sdp.kandle.dependencies.DependencyManager;
import ch.epfl.sdp.kandle.dependencies.Storage;


public class PostImagePicker extends ImagePicker {


    public PostImagePicker(Activity activity) {
        super(activity);
    }

    @Override
    protected void uploadImage() {

    }

}
