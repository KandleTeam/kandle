/**
 * @author Nicolas Jimenez
 */
package ch.epfl.sdp.kandle;

import static androidx.camera.core.ImageCapture.FLASH_MODE_AUTO;
import static androidx.camera.core.ImageCapture.FLASH_MODE_OFF;
import static androidx.camera.core.ImageCapture.FLASH_MODE_ON;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraControl;
import androidx.camera.core.CameraInfo;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.core.SurfaceRequest;
import androidx.camera.core.TorchState;
import androidx.camera.core.UseCase;
import androidx.camera.core.VideoCapture;
import androidx.camera.core.impl.utils.executor.CameraXExecutors;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.concurrent.futures.CallbackToFutureAdapter;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProviders;
import androidx.test.espresso.IdlingResource;
import androidx.test.espresso.idling.CountingIdlingResource;
import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import java.io.File;
import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicLong;

/**
 * An activity with four use cases: (1) view finder, (2) image capture, (3) image analysis, (4)
 * video capture.
 *
 * <p>All four use cases are created with CameraX and tied to the activity's lifecycle. CameraX
 * automatically connects and disconnects the use cases from the camera in response to changes in
 * the activity's lifecycle. Therefore, the use cases function properly when the app is paused and
 * resumed and when the device is rotated. The complex interactions between the camera and these
 * lifecycle events are handled internally by CameraX.
 */
public class CameraXActivity extends AppCompatActivity
        implements ActivityCompat.OnRequestPermissionsResultCallback {
    private static final String TAG = "CameraXActivity";
    private static final int PERMISSIONS_REQUEST_CODE = 42;
    // Possible values for this intent key: "backward" or "forward".
    private static final String INTENT_EXTRA_CAMERA_DIRECTION = "camera_direction";
    static final CameraSelector BACK_SELECTOR =
            new CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build();
    static final CameraSelector FRONT_SELECTOR =
            new CameraSelector.Builder().requireLensFacing(
                    CameraSelector.LENS_FACING_FRONT).build();
    private boolean mPermissionsGranted = false;
    private CallbackToFutureAdapter.Completer<Boolean> mPermissionsCompleter;
    private final AtomicLong mPreviewFrameCount = new AtomicLong(0);
    /** The camera to use */
    CameraSelector mCurrentCameraSelector = BACK_SELECTOR;
    @CameraSelector.LensFacing
    int mCurrentCameraLensFacing = CameraSelector.LENS_FACING_BACK;
    ProcessCameraProvider mCameraProvider;
    // TODO: Move the analysis processing, capture processing to separate threads, so
    // there is smaller impact on the preview.
    private String mCurrentCameraDirection = "BACKWARD";
    private Preview mPreview;
    private ImageCapture mImageCapture;
    private Camera mCamera;
    @ImageCapture.CaptureMode
    private int mCaptureMode = ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY;
    // Synthetic Accessor
    @SuppressWarnings("WeakerAccess")
    TextureView mTextureView;
    @SuppressWarnings("WeakerAccess")
    SurfaceTexture mSurfaceTexture;
    @SuppressWarnings("WeakerAccess")
    private Size mResolution;
    @SuppressWarnings("WeakerAccess")
    ListenableFuture<SurfaceRequest.Result> mSurfaceReleaseFuture;
    @SuppressWarnings("WeakerAccess")
    SurfaceRequest mSurfaceRequest;
    // Espresso testing variables
    private final CountingIdlingResource mViewIdlingResource = new CountingIdlingResource("view");
    private static final int FRAMES_UNTIL_VIEW_IS_READY = 5;
    private final CountingIdlingResource mAnalysisIdlingResource =
            new CountingIdlingResource("analysis");
    private final CountingIdlingResource mImageSavedIdlingResource =
            new CountingIdlingResource("imagesaved");

    /**
     * Retrieve idling resource that waits for view to display frames before proceeding.
     */
    @VisibleForTesting
    public void resetViewIdlingResource() {
        mPreviewFrameCount.set(0);
        // Make the view idling resource non-idle, until required framecount achieved.
        mViewIdlingResource.increment();
    }
    /**
     * Creates a view finder use case.
     *
     * <p>This use case observes a {@link SurfaceTexture}. The texture is connected to a {@link
     * TextureView} to display a camera preview.
     */
    private void createPreview() {
        Button button = this.findViewById(R.id.PreviewToggle);
        button.setBackgroundColor(Color.LTGRAY);
        enablePreview();
        button.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Button buttonView = (Button) view;
                        if (mPreview != null) {
                            // Remove the use case
                            buttonView.setBackgroundColor(Color.RED);
                            mCameraProvider.unbind(mPreview);
                            mPreview = null;
                        } else {
                            // Add the use case
                            buttonView.setBackgroundColor(Color.LTGRAY);
                            enablePreview();
                        }
                    }
                });
        Log.i(TAG, "Got UseCase: " + mPreview);
    }
    void enablePreview() {
        mPreview = new Preview.Builder()
                .setTargetName("Preview")
                .build();
        Log.d(TAG, "enablePreview");
        mPreview.setSurfaceProvider(
                (surfaceRequest) -> {
                    mResolution = surfaceRequest.getResolution();
                    if (mSurfaceRequest != null) {
                        mSurfaceRequest.willNotProvideSurface();
                    }
                    mSurfaceRequest = surfaceRequest;
                    mSurfaceRequest.addRequestCancellationListener(
                            ContextCompat.getMainExecutor(mTextureView.getContext()), () -> {
                                if (mSurfaceRequest != null && mSurfaceRequest == surfaceRequest) {
                                    mSurfaceRequest = null;
                                    mSurfaceReleaseFuture = null;
                                }
                            });
                    tryToProvidePreviewSurface();
                });
        resetViewIdlingResource();
        if (bindToLifecycleSafely(mPreview, R.id.PreviewToggle) == null) {
            mPreview = null;
            return;
        }
    }
    void transformPreview(@NonNull Size resolution) {
        if (resolution.getWidth() == 0 || resolution.getHeight() == 0 || mTextureView.getWidth() == 0 || mTextureView.getHeight() == 0) {
            return;
        }
        Matrix matrix = new Matrix();
        // Compute the preview ui size based on the available width, height, and ui orientation.
        int viewWidth = (mTextureView.getRight() - mTextureView.getLeft());
        int viewHeight = (mTextureView.getBottom() - mTextureView.getTop());
        Size scaled =
                calculatePreviewViewDimens(
                        resolution, viewWidth, viewHeight, getDisplayRotation());
        // Do corresponding rotation to correct the preview direction
        //viewWidth/2 is the center of the view
        matrix.postRotate(-getDisplayRotation(), viewWidth / 2, viewHeight / 2);
        // Compute the scale value for center crop mode
        //If there is a rotation
        float xScale = getDisplayRotation() == 90 || getDisplayRotation() == 270 ? scaled.getWidth() / (float) viewHeight : scaled.getWidth() / (float) viewWidth;
        float yScale = getDisplayRotation() == 90 || getDisplayRotation() == 270 ? scaled.getHeight() / (float) viewWidth : scaled.getHeight() / (float) viewHeight;
        // Only two digits after the decimal point are valid for postScale. Need to get ceiling of
        // two
        // digits floating value to do the scale operation. Otherwise, the result may be scaled not
        // large enough and will have some blank lines on the screen.
        xScale = new BigDecimal(xScale).setScale(2, BigDecimal.ROUND_CEILING).floatValue();
        yScale = new BigDecimal(yScale).setScale(2, BigDecimal.ROUND_CEILING).floatValue();
        // Do corresponding scale to resolve the deformation problem
        matrix.postScale(xScale, yScale, viewWidth / 2, viewHeight / 2);
        mTextureView.setTransform(matrix);
    }
    /** @return One of 0, 90, 180, 270. */
    private int getDisplayRotation() {
        int displayRotation = getWindowManager().getDefaultDisplay().getRotation();
        switch (displayRotation) {
            case Surface.ROTATION_0:
                return 0;
            case Surface.ROTATION_90:
                return 90;
            case Surface.ROTATION_180:
                return 180;
            case Surface.ROTATION_270:
                return 270;
            default:
                throw new UnsupportedOperationException(
                        "Unsupported display rotation: " + displayRotation);
        }
    }
    private Size calculatePreviewViewDimens(
            Size srcSize, int parentWidth, int parentHeight, int displayRotation) {
        int inWidth = srcSize.getWidth();
        int inHeight = srcSize.getHeight();
        if (displayRotation == 0 || displayRotation == 180) {
            // Need to reverse the width and height since we're in landscape orientation.
            inWidth = srcSize.getHeight();
            inHeight = srcSize.getWidth();
        }
        int outWidth = parentWidth;
        int outHeight = parentHeight;
        if (inWidth != 0 && inHeight != 0) {
            float vfRatio = inWidth / (float) inHeight;
            float parentRatio = parentWidth / (float) parentHeight;
            // Match shortest sides together.
            if (vfRatio < parentRatio) {
                outWidth = parentWidth;
                outHeight = Math.round(parentWidth / vfRatio);
            } else {
                outWidth = Math.round(parentHeight * vfRatio);
                outHeight = parentHeight;
            }
        }
        return new Size(outWidth, outHeight);
    }

    /**
     * Creates an image capture use case.
     *
     * <p>This use case takes a picture and saves it to a file, whenever the user clicks a button.
     */
    private void createImageCapture() {
        Button button = this.findViewById(R.id.PhotoToggle);
        button.setBackgroundColor(Color.LTGRAY);
        enableImageCapture();
        button.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Button buttonView = (Button) view;
                        if (mImageCapture != null) {
                            // Remove the use case
                            buttonView.setBackgroundColor(Color.RED);
                            CameraXActivity.this.disableImageCapture();
                        } else {
                            // Add the use case
                            buttonView.setBackgroundColor(Color.LTGRAY);
                            CameraXActivity.this.enableImageCapture();
                        }
                    }
                });
        Log.i(TAG, "Got UseCase: " + mImageCapture);
    }
    void enableImageCapture() {
        mImageCapture = new ImageCapture.Builder()
                .setCaptureMode(mCaptureMode)
                .setTargetName("ImageCapture")
                .build();
        Camera camera = bindToLifecycleSafely(mImageCapture, R.id.PhotoToggle);
        if (camera == null) {
            Button button = this.findViewById(R.id.Picture);
            button.setOnClickListener(null);
            mImageCapture = null;
            return;
        }
        Button button = this.findViewById(R.id.Picture);
        button.setOnClickListener(
                new View.OnClickListener() {
                    long mStartCaptureTime = 0;
                    @Override
                    public void onClick(View view) {
                        mImageSavedIdlingResource.increment();
                        mStartCaptureTime = SystemClock.elapsedRealtime();
                        createDefaultPictureFolderIfNotExist();
                        ContentValues contentValues = new ContentValues();
                        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpg");
                        ImageCapture.OutputFileOptions outputFileOptions =
                                new ImageCapture.OutputFileOptions.Builder(
                                        getContentResolver(),
                                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                                        contentValues).build();
                        mImageCapture.takePicture(outputFileOptions,
                                ContextCompat.getMainExecutor(CameraXActivity.this),
                                new ImageCapture.OnImageSavedCallback() {
                                    @Override
                                    public void onImageSaved(
                                            @NonNull ImageCapture.OutputFileResults
                                                    outputFileResults) {
                                        Log.d(TAG, "Saved image to "
                                                + outputFileResults.getSavedUri());
                                        try {
                                            mImageSavedIdlingResource.decrement();
                                        } catch (IllegalStateException e) {
                                            Log.e(TAG, "Error: unexpected onImageSaved "
                                                    + "callback received. Continuing.");
                                        }
                                        Toast.makeText(CameraXActivity.this, "Picture was taken", Toast.LENGTH_SHORT).show();
                                        /*long duration =
                                                SystemClock.elapsedRealtime() - mStartCaptureTime;
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                Toast.makeText(CameraXActivity.this,
                                                        "Image captured in " + duration + " ms",
                                                        Toast.LENGTH_SHORT).show();
                                            }
                                        });*/
                                    }
                                    @Override
                                    public void onError(@NonNull ImageCaptureException exception) {
                                        Log.e(TAG, "Failed to save image.", exception.getCause());
                                        try {
                                            mImageSavedIdlingResource.decrement();
                                        } catch (IllegalStateException e) {
                                            Log.e(TAG, "Error: unexpected onImageSaved "
                                                    + "callback received. Continuing.");
                                        }
                                    }
                                });
                    }
                });
        refreshFlashButton();
    }
    void disableImageCapture() {
        mCameraProvider.unbind(mImageCapture);
        mImageCapture = null;
        Button button = this.findViewById(R.id.Picture);
        button.setOnClickListener(null);
        refreshFlashButton();
    }
    private void refreshFlashButton() {
        ImageButton flashToggle = findViewById(R.id.flash_toggle);
        if (mImageCapture != null) {
            CameraInfo cameraInfo = getCameraInfo();
            if (cameraInfo != null && cameraInfo.hasFlashUnit()) {
                flashToggle.setVisibility(View.VISIBLE);
                flashToggle.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        @ImageCapture.FlashMode int flashMode = mImageCapture.getFlashMode();
                        if (flashMode == FLASH_MODE_ON) {
                            mImageCapture.setFlashMode(FLASH_MODE_OFF);
                        } else if (flashMode == FLASH_MODE_OFF) {
                            mImageCapture.setFlashMode(FLASH_MODE_AUTO);
                        } else if (flashMode == FLASH_MODE_AUTO) {
                            mImageCapture.setFlashMode(FLASH_MODE_ON);
                        }
                        refreshFlashButtonIcon();
                    }
                });
                refreshFlashButtonIcon();
            } else {
                flashToggle.setVisibility(View.INVISIBLE);
                flashToggle.setOnClickListener(null);
            }
        } else {
            flashToggle.setVisibility(View.GONE);
            flashToggle.setOnClickListener(null);
        }
    }
    private void refreshFlashButtonIcon() {
        ImageButton flashToggle = findViewById(R.id.flash_toggle);
        @ImageCapture.FlashMode int flashMode = mImageCapture.getFlashMode();
        switch (flashMode) {
            case FLASH_MODE_ON:
                flashToggle.setImageResource(R.drawable.ic_flashon);
                break;
            case FLASH_MODE_OFF:
                flashToggle.setImageResource(R.drawable.ic_flashoff);
                break;
            case FLASH_MODE_AUTO:
                flashToggle.setImageResource(R.drawable.ic_flashauto);
                break;
        }
    }
    /** Creates all the use cases. */
    private void createUseCases() {
        createImageCapture();
        createPreview();
    }
    @SuppressWarnings("UnstableApiUsage")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        mTextureView = findViewById(R.id.textureView);
        mTextureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(final SurfaceTexture surfaceTexture,
                                                  final int width, final int height) {
                mSurfaceTexture = surfaceTexture;
                tryToProvidePreviewSurface();
            }
            @Override
            public void onSurfaceTextureSizeChanged(final SurfaceTexture surfaceTexture,
                                                    final int width, final int height) {
            }
            /**
             * If a surface has been provided to the camera (meaning
             * {@link CameraXActivity#mSurfaceRequest} is null), but the camera
             * is still using it (meaning {@link CameraXActivity#mSurfaceReleaseFuture} is
             * not null), a listener must be added to
             * {@link CameraXActivity#mSurfaceReleaseFuture} to ensure the surface
             * is properly released after the camera is done using it.
             *
             * @param surfaceTexture The {@link SurfaceTexture} about to be destroyed.
             * @return false if the camera is not done with the surface, true otherwise.
             */
            @Override
            public boolean onSurfaceTextureDestroyed(final SurfaceTexture surfaceTexture) {
                mSurfaceTexture = null;
                if (mSurfaceRequest == null && mSurfaceReleaseFuture != null) {
                    mSurfaceReleaseFuture.addListener(surfaceTexture::release,
                            ContextCompat.getMainExecutor(mTextureView.getContext()));
                    return false;
                } else {
                    return true;
                }
            }
            @Override
            public void onSurfaceTextureUpdated(final SurfaceTexture surfaceTexture) {
                // Wait until surface texture receives enough updates. This is for testing.
                if (mPreviewFrameCount.getAndIncrement() >= FRAMES_UNTIL_VIEW_IS_READY) {
                    try {
                        if (!mViewIdlingResource.isIdleNow()) {
                            Log.d(TAG, FRAMES_UNTIL_VIEW_IS_READY + " or more counted on preview."
                                    + " Make IdlingResource idle.");
                            mViewIdlingResource.decrement();
                        }
                    } catch (IllegalStateException e) {
                        Log.e(TAG, "Unexpected decrement. Continuing");
                    }
                }
            }
        });
        StrictMode.VmPolicy policy =
                new StrictMode.VmPolicy.Builder().detectAll().penaltyLog().build();
        StrictMode.setVmPolicy(policy);
        // Get params from adb extra string
        Bundle bundle = this.getIntent().getExtras();
        if (bundle != null) {
            String newCameraDirection = bundle.getString(INTENT_EXTRA_CAMERA_DIRECTION);
            if (newCameraDirection != null) {
                mCurrentCameraDirection = newCameraDirection;
            }
        }
        CameraXViewModel viewModel = ViewModelProviders.of(this).get(CameraXViewModel.class);
        viewModel.getCameraProvider().observe(this, provider -> {
            mCameraProvider = provider;
            if (mPermissionsGranted) {
                setupCamera();
            }
        });
        Futures.addCallback(setupPermissions(), new FutureCallback<Boolean>() {
            @Override
            public void onSuccess(@Nullable Boolean permissionsGranted) {
                mPermissionsGranted = Preconditions.checkNotNull(permissionsGranted);
                if (mPermissionsGranted) {
                    if (mCameraProvider != null) {
                        setupCamera();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "Camera permission denied.",
                            Toast.LENGTH_SHORT)
                            .show();
                    finish();
                }
            }
            @Override
            public void onFailure(@NonNull Throwable throwable) {
                Toast.makeText(getApplicationContext(), "Unable to request camera "
                        + "permission.", Toast.LENGTH_SHORT)
                        .show();
                finish();
            }
        }, ContextCompat.getMainExecutor(this));
    }
    @SuppressWarnings("WeakerAccess")
    void tryToProvidePreviewSurface() {
        /*
          Should only continue if:
          - The preview size has been specified.
          - The textureView's surfaceTexture is available (after TextureView
          .SurfaceTextureListener#onSurfaceTextureAvailable is invoked)
          - The surfaceCompleter has been set (after CallbackToFutureAdapter
          .Resolver#attachCompleter is invoked).
         */
        if (mResolution == null || mSurfaceTexture == null || mSurfaceRequest == null) {
            return;
        }
        mSurfaceTexture.setDefaultBufferSize(mResolution.getWidth(), mResolution.getHeight());
        final Surface surface = new Surface(mSurfaceTexture);
        final ListenableFuture<SurfaceRequest.Result> surfaceReleaseFuture =
                CallbackToFutureAdapter.getFuture(completer -> {
                    mSurfaceRequest.provideSurface(surface,
                            CameraXExecutors.directExecutor(), completer::set);
                    return "provideSurface[request=" + mSurfaceRequest + " surface=" + surface
                            + "]";
                });
        mSurfaceReleaseFuture = surfaceReleaseFuture;
        mSurfaceReleaseFuture.addListener(() -> {
            surface.release();
            if (mSurfaceReleaseFuture == surfaceReleaseFuture) {
                mSurfaceReleaseFuture = null;
            }
        }, ContextCompat.getMainExecutor(mTextureView.getContext()));
        mSurfaceRequest = null;
        transformPreview(mResolution);
    }
    void setupCamera() {
        // Only call setupCamera if permissions are granted
        Preconditions.checkState(mPermissionsGranted);
        Log.d(TAG, "Camera direction: " + mCurrentCameraDirection);
        if (mCurrentCameraDirection.equalsIgnoreCase("BACKWARD")) {
            mCurrentCameraSelector = BACK_SELECTOR;
            mCurrentCameraLensFacing = CameraSelector.LENS_FACING_BACK;
        } else if (mCurrentCameraDirection.equalsIgnoreCase("FORWARD")) {
            mCurrentCameraSelector = FRONT_SELECTOR;
            mCurrentCameraLensFacing = CameraSelector.LENS_FACING_FRONT;
        } else {
            throw new RuntimeException("Invalid camera direction: " + mCurrentCameraDirection);
        }
        Log.d(TAG, "Using camera lens facing: " + mCurrentCameraSelector);
        CameraXActivity.this.createUseCases();
        ImageButton directionToggle = findViewById(R.id.direction_toggle);
        directionToggle.setVisibility(View.VISIBLE);
        directionToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCurrentCameraLensFacing == CameraSelector.LENS_FACING_BACK) {
                    mCurrentCameraSelector = FRONT_SELECTOR;
                    mCurrentCameraLensFacing = CameraSelector.LENS_FACING_FRONT;
                } else if (mCurrentCameraLensFacing == CameraSelector.LENS_FACING_FRONT) {
                    mCurrentCameraSelector = BACK_SELECTOR;
                    mCurrentCameraLensFacing = CameraSelector.LENS_FACING_BACK;
                }
                Log.d(TAG, "Change camera direction: " + mCurrentCameraSelector);
                rebindUseCases();
            }
        });
    }
    private void rebindUseCases() {
        // Rebind all use cases.
        mCameraProvider.unbindAll();
        if (mImageCapture != null) {
            enableImageCapture();
        }
        if (mPreview != null) {
            enablePreview();
        }
    }
    private ListenableFuture<Boolean> setupPermissions() {
        return CallbackToFutureAdapter.getFuture(completer -> {
            mPermissionsCompleter = completer;
            if (!allPermissionsGranted()) {
                makePermissionRequest();
            } else {
                mPermissionsCompleter.set(true);
            }
            return "get_permissions";
        });
    }
    private void makePermissionRequest() {
        ActivityCompat.requestPermissions(this, getRequiredPermissions(), PERMISSIONS_REQUEST_CODE);
    }
    /** Returns true if all the necessary permissions have been granted already. */
    private boolean allPermissionsGranted() {
        for (String permission : getRequiredPermissions()) {
            if (ContextCompat.checkSelfPermission(this, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }
    private void createDefaultPictureFolderIfNotExist() {
        File pictureFolder = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        if (!pictureFolder.exists()) {
            pictureFolder.mkdir();
        }
    }
    /** Tries to acquire all the necessary permissions through a dialog. */
    private String[] getRequiredPermissions() {
        PackageInfo info;
        try {
            info =
                    getPackageManager()
                            .getPackageInfo(getPackageName(), PackageManager.GET_PERMISSIONS);
        } catch (NameNotFoundException exception) {
            Log.e(TAG, "Failed to obtain all required permissions.", exception);
            return new String[0];
        }
        String[] finalPermissions = info.requestedPermissions;
        String[] permissions = new String[3];
        int j = 0;
        for(int i = 0; i < finalPermissions.length; i++){
            String s = finalPermissions[i];
            if((s.equals("android.permission.CAMERA") || s.equals("android.permission.RECORD_AUDIO") || s.equals("android.permission.WRITE_EXTERNAL_STORAGE")) && j < 3){
                permissions[j] = s;
                j++;
            }
        }
        if (permissions != null && permissions.length > 0) {
            return permissions;
        } else {
            return new String[0];
        }
    }
    @CallSuper
    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_CODE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "Permissions Granted.");
                    mPermissionsCompleter.set(true);
                } else {
                    Log.d(TAG, "Permissions Denied.");
                    mPermissionsCompleter.set(false);
                }
                return;
            }
            default:
                // No-op
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
    @Nullable
    private Camera bindToLifecycleSafely(UseCase useCase, int buttonViewId) {
        try {
            mCamera = mCameraProvider.bindToLifecycle(this, mCurrentCameraSelector,
                    useCase);
            return mCamera;
        } catch (IllegalArgumentException e) {
            Log.e(TAG, e.getMessage());
            /*Toast.makeText(getApplicationContext(), "Bind too many use cases.", Toast.LENGTH_SHORT)
                    .show();*/
            Button button = this.findViewById(buttonViewId);
            button.setBackgroundColor(Color.RED);
        }
        return null;
    }
    Preview getPreview() {
        return mPreview;
    }
    ImageCapture getImageCapture() {
        return mImageCapture;
    }
    @VisibleForTesting
    @Nullable
    CameraInfo getCameraInfo() {
        return mCamera != null ? mCamera.getCameraInfo() : null;
    }
    @VisibleForTesting
    @Nullable
    CameraControl getCameraControl() {
        return mCamera != null ? mCamera.getCameraControl() : null;
    }
}
