package ch.epfl.sdp.kandle;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.InstrumentationRegistry.getTargetContext;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assume.assumeTrue;
import android.content.Intent;
import android.os.Build;

import androidx.camera.core.CameraInfo;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.TorchState;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.IdlingRegistry;
import androidx.test.espresso.IdlingResource;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.ActivityTestRule;
import androidx.test.rule.GrantPermissionRule;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObject;
import androidx.test.uiautomator.UiObjectNotFoundException;
import androidx.test.uiautomator.UiSelector;

import junit.framework.AssertionFailedError;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import ch.epfl.sdp.kandle.idlingressource.ElapsedTimeIdlingResource;
import ch.epfl.sdp.kandle.idlingressource.WaitForViewToShow;

/** Test toggle buttons in CoreTestApp. */
@RunWith(AndroidJUnit4.class)
@LargeTest
public final class ToggleButtonUITest {
    private static final int IDLE_TIMEOUT_MS = 1000;
    private static final String BASIC_SAMPLE_PACKAGE = "androidx.camera.integration.core";
    private final Intent mIntent = ApplicationProvider.getApplicationContext().getPackageManager()
            .getLaunchIntentForPackage(BASIC_SAMPLE_PACKAGE);
    @Rule
    public ActivityTestRule<CameraXActivity> mActivityRule =
            new ActivityTestRule<>(CameraXActivity.class, true,
                    false);
    @Rule
    public GrantPermissionRule mCameraPermissionRule =
            GrantPermissionRule.grant(android.Manifest.permission.CAMERA);
    @Rule
    public GrantPermissionRule mStoragePermissionRule =
            GrantPermissionRule.grant(android.Manifest.permission.WRITE_EXTERNAL_STORAGE);

    @Rule
    public GrantPermissionRule mAudioPermissionRule =
            GrantPermissionRule.grant(android.Manifest.permission.RECORD_AUDIO);

    public static void waitFor(IdlingResource idlingResource) {
        IdlingRegistry.getInstance().register(idlingResource);
        Espresso.onIdle();
        IdlingRegistry.getInstance().unregister(idlingResource);
    }
    @Before
    public void setUp() {
        // Clear the device UI before start each test.
        // Launch Activity
        mActivityRule.launchActivity(mIntent);
    }
    @After
    public void tearDown() {
        // Idles Espresso thread and make activity complete each action.
        waitFor(new ElapsedTimeIdlingResource(IDLE_TIMEOUT_MS));
        mActivityRule.finishActivity();
        // Returns to Home to restart next test.
    }
    @Test
    public void testFlashToggleButton() {
        waitFor(new WaitForViewToShow(R.id.constraintLayout));
        assumeTrue(detectButtonVisibility(R.id.flash_toggle));
        ImageCapture useCase = mActivityRule.getActivity().getImageCapture();
        assertNotNull(useCase);
        // There are 3 different states of flash mode: ON, OFF and AUTO.
        // By pressing flash mode toggle button, the flash mode would switch to the next state.
        // The flash mode would loop in following sequence: OFF -> AUTO -> ON -> OFF.
        @ImageCapture.FlashMode int mode1 = useCase.getFlashMode();
        onView(withId(R.id.flash_toggle)).perform(click());
        @ImageCapture.FlashMode int mode2 = useCase.getFlashMode();
        // After the switch, the mode2 should be different from mode1.
        assertNotEquals(mode2, mode1);
        onView(withId(R.id.flash_toggle)).perform(click());
        @ImageCapture.FlashMode int mode3 = useCase.getFlashMode();
        onView(withId(R.id.Picture)).perform(click());
        // The mode3 should be different from first and second time.
        assertNotEquals(mode3, mode2);
        assertNotEquals(mode3, mode1);
    }
    @Test
    public void testSwitchCameraToggleButton() {
        waitFor(new WaitForViewToShow(R.id.direction_toggle));
        boolean isPreviewExist = mActivityRule.getActivity().getPreview() != null;
        boolean isImageCaptureExist = mActivityRule.getActivity().getImageCapture() != null;
        for (int i = 0; i < 2; i++) {
            onView(withId(R.id.direction_toggle)).perform(click());
            waitFor(new ElapsedTimeIdlingResource(2000));
            if (isImageCaptureExist) {
                assertNotNull(mActivityRule.getActivity().getImageCapture());
            }
            if (isPreviewExist) {
                assertNotNull(mActivityRule.getActivity().getPreview());
            }
        }
    }

    private boolean isTorchOn(CameraInfo cameraInfo) {
        return cameraInfo.getTorchState().getValue() == TorchState.ON;
    }
    private boolean detectButtonVisibility(int resource) {
        try {
            onView(withId(resource)).check(matches(isDisplayed()));
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