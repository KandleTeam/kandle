package ch.epfl.sdp.kandle;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.InstrumentationRegistry.getTargetContext;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assume.assumeTrue;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.util.Log;

import androidx.camera.core.CameraSelector;
import androidx.lifecycle.Lifecycle;
import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.IdlingRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.GrantPermissionRule;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObject;
import androidx.test.uiautomator.UiObjectNotFoundException;
import androidx.test.uiautomator.UiSelector;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

// Test application lifecycle when using CameraX.
@RunWith(AndroidJUnit4.class)
@LargeTest
public class LifeCycleCameraTest {
    private static final int HOME_TIMEOUT_MS = 3000;
    private final UiDevice mDevice =
            UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
    private final String mLauncherPackageName = mDevice.getLauncherPackageName();
    @Rule
    public GrantPermissionRule mCameraPermissionRule =
            GrantPermissionRule.grant(android.Manifest.permission.CAMERA);
    @Rule
    public GrantPermissionRule mStoragePermissionRule =
            GrantPermissionRule.grant(android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
    /*@Rule
    public GrantPermissionRule mAudioPermissionRule =
            GrantPermissionRule.grant(android.Manifest.permission.RECORD_AUDIO);*/
    @Before
    public void setup() {
        allowPermission();
        assertThat(mLauncherPackageName, notNullValue());
        allowPermission();
    }
    @After
    public void tearDown() {
        mDevice.pressHome();
        mDevice.waitForIdle(HOME_TIMEOUT_MS);
    }
    // Check if Preview screen is updated or not, after Destroy-Create lifecycle.
    @Test
    public void checkPreviewUpdatedAfterDestroyRecreate() {
        allowPermission();
        // Launch activity.
        try (ActivityScenario<CameraXActivity> activityScenario =
                     ActivityScenario.launch(CameraXActivity.class)) {
            allowPermission();
            // Check for view idle, then destroy it.
            checkForViewIdle(activityScenario);
            // Launch new activity and check for view idle.
            checkForViewIdle(activityScenario.recreate());
        }
    }
    // Check if Preview screen is updated or not, after Stop-Resume lifecycle.
    @Test
    public void checkPreviewUpdatedAfterStopResume() {
        // Launch activity.
        allowPermission();
        try (ActivityScenario<CameraXActivity> activityScenario =
                     ActivityScenario.launch(CameraXActivity.class)) {
            allowPermission();
            // Check view gets to idle.
            checkForViewIdle(activityScenario);
            // Go through pause/resume then check again for view to get frames then idle.
            activityScenario.moveToState(Lifecycle.State.CREATED).onActivity(activity -> {
                activity.resetViewIdlingResource();
            });
            checkForViewIdle(activityScenario.moveToState(Lifecycle.State.RESUMED));
            // Go through pause/resume then check again for view to get frames then idle, the
            // second pass is used to protect against previous observed issues.
            activityScenario.moveToState(Lifecycle.State.CREATED).onActivity(activity -> {
                activity.resetViewIdlingResource();
            });
            checkForViewIdle(activityScenario.moveToState(Lifecycle.State.RESUMED));
        }
    }
    // Check if Preview screen is updated or not, after toggling camera, then a Destroy-Create
    // lifecycle.
    @Test
    public void checkPreviewUpdatedAfterToggleCameraAndStopResume() {
        allowPermission();
        // check have front camera
        try (ActivityScenario<CameraXActivity> activityScenario =
                     ActivityScenario.launch(CameraXActivity.class)) {
            allowPermission();
            try {
                activityScenario.onActivity(activity -> {
                    IdlingRegistry.getInstance().register(activity.getViewIdlingResource());
                });
                onView(withId(R.id.textureView)).check(matches(isDisplayed()));
                // Switch camera.
                onView(withId(R.id.direction_toggle)).perform(click());
                // Go through pause/resume then check again for view to get frames then idle.
                activityScenario.moveToState(Lifecycle.State.CREATED);
                activityScenario.onActivity(activity -> {
                    activity.resetViewIdlingResource();
                });
                activityScenario.moveToState(Lifecycle.State.RESUMED);
                onView(withId(R.id.textureView)).check(matches(isDisplayed()));
            } finally {
                activityScenario.onActivity(activity -> {
                    IdlingRegistry.getInstance().unregister(activity.getViewIdlingResource());
                });
            }
        }
    }
    // Check if Preview screen is updated or not, after rotate device, and Stop-Resume lifecycle.
    @Test
    public void checkPreviewUpdatedAfterRotateDeviceAndStopResume() {
        // Launch activity.
        allowPermission();
        try (ActivityScenario<CameraXActivity> activityScenario =
                     checkForViewIdle(ActivityScenario.launch(CameraXActivity.class))) {
            // Check view gets to idle.
            allowPermission();
            checkForViewIdle(activityScenario);
            // Rotate to Landscape and the activity will be recreated.
            activityScenario.onActivity(activity -> {
                activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            });
            // Get idling from the re-created activity.
            activityScenario.onActivity(activity -> {
                activity.resetViewIdlingResource();
            });
            checkForViewIdle(activityScenario);
            activityScenario.moveToState(Lifecycle.State.CREATED);
            activityScenario.onActivity(activity -> {
                activity.resetViewIdlingResource();
            });
            activityScenario.moveToState(Lifecycle.State.RESUMED);
            checkForViewIdle(activityScenario);
        }
    }
    private ActivityScenario<CameraXActivity>
    checkForViewIdle(ActivityScenario<CameraXActivity> activityScenario) {
        try {
            activityScenario.onActivity(activity -> {
                IdlingRegistry.getInstance().register(activity.getViewIdlingResource());
            });
            // Check the activity launched and Preview displays frames.
            onView(withId(R.id.textureView)).check(matches(isDisplayed()));
        } finally {
            // Always release the idling resource, in case of timeout exceptions.
            activityScenario.onActivity(activity -> {
                IdlingRegistry.getInstance().unregister(activity.getViewIdlingResource());
            });
        }
        return activityScenario;
    }


    private void allowPermission(){
        if (Build.VERSION.SDK_INT >= 23) {
            UiObject allowPermissions = mDevice.findObject(new UiSelector().text("allow"));
            if (allowPermissions.exists()) {
                try {
                    allowPermissions.click();
                } catch (UiObjectNotFoundException e) {
                    System.out.println("There is no permissions dialog to interact with ");
                }
            }
        }
    }
}
