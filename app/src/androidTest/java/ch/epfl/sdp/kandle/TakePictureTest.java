package ch.epfl.sdp.kandle;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;
import android.content.Context;
import android.content.Intent;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.Preview;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.IdlingRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.ActivityTestRule;
import androidx.test.rule.GrantPermissionRule;
import androidx.test.uiautomator.UiDevice;

import junit.framework.AssertionFailedError;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class TakePictureTest {
    private static final String BASIC_SAMPLE_PACKAGE = "androidx.camera.integration.core";
    private final UiDevice mDevice =
            UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
    private final Context mContext = ApplicationProvider.getApplicationContext();
    private final Intent mIntent = mContext.getPackageManager()
            .getLaunchIntentForPackage(BASIC_SAMPLE_PACKAGE);
    @Rule
    public ActivityTestRule<CameraXActivity> mActivityRule =
            new ActivityTestRule<>(CameraXActivity.class, true, false);
    @Rule
    public GrantPermissionRule mCameraPermissionRule =
            GrantPermissionRule.grant(android.Manifest.permission.CAMERA);
    @Rule
    public GrantPermissionRule mStoragePermissionRule =
            GrantPermissionRule.grant(android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
    @Before
    public void setUp() {
        /*assumeTrue(CameraUtil.deviceHasCamera());
        CoreAppTestUtil.assumeCompatibleDevice();*/
        // Clear the device UI before start each test.
        //clearDeviceUI(InstrumentationRegistry.getInstrumentation());
        // Launch Activity
        mActivityRule.launchActivity(mIntent);
    }
    @After
    public void tearDown() {
        pressBackAndReturnHome();
        mActivityRule.finishActivity();
    }
    @Test
    public void testPictureButton() {
        assertTrue(checkPreviewReady());
        ImageCapture imageCapture = mActivityRule.getActivity().getImageCapture();
        assertNotNull(imageCapture);
        onView(withId(R.id.Picture)).perform(click());
    }


    private void pressBackAndReturnHome() {
        mDevice.pressBack();
        // Returns to Home to restart next test.
        mDevice.pressHome();
        mDevice.waitForIdle(3000);
    }

    private boolean checkPreviewReady() {
        try {
            onView(withId(R.id.textureView));
            // View is in hierarchy
            return true;
        } catch (AssertionFailedError e) {
            // View is not in hierarchy
            return false;
        } catch (Exception e) {
            // View is not in hierarchy
            return false;
        }

    }
}