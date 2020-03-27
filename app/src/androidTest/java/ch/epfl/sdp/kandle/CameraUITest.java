package ch.epfl.sdp.kandle;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import android.content.Context;
import android.content.Intent;

import androidx.camera.core.Preview;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.IdlingRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ActivityTestRule;
import androidx.test.rule.GrantPermissionRule;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class CameraUITest {
    private static final String BASIC_SAMPLE_PACKAGE = "androidx.camera.integration.core";
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
    @Rule
    public GrantPermissionRule mAudioPermissionRule =
            GrantPermissionRule.grant(android.Manifest.permission.RECORD_AUDIO);

    @Before
    public void setUp() {
        mActivityRule.launchActivity(mIntent);
    }
    @After
    public void tearDown() {
        //pressBackAndReturnHome();
        mActivityRule.finishActivity();
    }
    @Test
    public void testPreviewButton() {
        IdlingRegistry.getInstance().register(mActivityRule.getActivity().getViewIdlingResource());
        Preview preview = mActivityRule.getActivity().getPreview();
        // Click to disable the preview use case.
        if (preview != null) {
            // Check preview started.
            onView(withId(R.id.textureView)).check(matches(isDisplayed()));
            // Click toggle.
            onView(withId(R.id.PreviewToggle)).perform(click());
        }
        // It is null(disable) and do click to enable preview use case.
        if (preview == null) {
            onView(withId(R.id.PreviewToggle)).perform(click());
            // Check preview started.
            onView(withId(R.id.textureView)).check(matches(isDisplayed()));
        }
        IdlingRegistry.getInstance().unregister(
                mActivityRule.getActivity().getViewIdlingResource());
    }
    /*private void pressBackAndReturnHome() {
        mDevice.pressBack();
        // Returns to Home to restart next test.
        mDevice.pressHome();
        mDevice.waitForIdle(3000);
    }*/
}