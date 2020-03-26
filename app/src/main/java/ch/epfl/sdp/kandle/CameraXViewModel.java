package ch.epfl.sdp.kandle;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
/** View model providing access to the camera */
public class CameraXViewModel extends AndroidViewModel {
    private MutableLiveData<ProcessCameraProvider> mProcessCameraProviderLiveData;

    public CameraXViewModel(@NonNull Application application) {
        super(application);
    }
    /**
     * Returns a {@link LiveData} containing CameraX's {@link ProcessCameraProvider} once it has
     * been initialized.
     */
    LiveData<ProcessCameraProvider> getCameraProvider() {
        if (mProcessCameraProviderLiveData == null) {
            mProcessCameraProviderLiveData = new MutableLiveData<>();
            ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                    ProcessCameraProvider.getInstance(getApplication());
            cameraProviderFuture.addListener(() -> {
                try {
                    ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                    mProcessCameraProviderLiveData.setValue(cameraProvider);
                } catch (ExecutionException e) {
                    if (!(e.getCause() instanceof CancellationException)) {
                        throw new IllegalStateException("Error occurred while initializing "
                                + "CameraX:", e.getCause());
                    }
                } catch (InterruptedException e) {
                    throw new IllegalStateException("Unable to use CameraX", e);
                }
            }, ContextCompat.getMainExecutor(getApplication()));
        }
        return mProcessCameraProviderLiveData;
    }
}