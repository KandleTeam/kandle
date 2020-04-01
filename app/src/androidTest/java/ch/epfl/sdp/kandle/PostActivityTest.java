package ch.epfl.sdp.kandle;


import android.app.Activity;
import android.app.Instrumentation;
import android.content.Intent;
import android.net.Uri;

import androidx.test.espresso.intent.rule.IntentsTestRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import androidx.test.rule.GrantPermissionRule;
import ch.epfl.sdp.kandle.dependencies.DependencyManager;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intending;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasAction;
import static androidx.test.espresso.matcher.ViewMatchers.hasErrorText;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withTagValue;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertTrue;


@RunWith(AndroidJUnit4.class)
public class PostActivityTest {


    @Rule
    public IntentsTestRule<PostActivity> intentsRule =
            new IntentsTestRule<PostActivity>(PostActivity.class,true,true
            ){
                @Override
                protected  void beforeActivityLaunched() {
                    DependencyManager.setFreshTestDependencies(true);
                }
            };
    @Rule
    public GrantPermissionRule permissionRule = GrantPermissionRule.grant(android.Manifest.permission.ACCESS_FINE_LOCATION,android.Manifest.permission.ACCESS_FINE_LOCATION);


    @Test
    public void postEmptyGetsErrorMessage(){
        onView(withId(R.id.postText)).perform(typeText("     "));
        onView(withId (R.id.postText)).perform(closeSoftKeyboard());
        onView(withId(R.id.postButton)).perform(click());
        onView(withId(R.id.postText)).check(matches(hasErrorText("Your post is empty...")));
        onView(withId(R.id.postImage)).check(matches(not(withTagValue(is(PostActivity.POST_IMAGE_TAG)))));

    }


    @Test
    public void postButtonLeadsToMainActivityWhenCorrectPost() {

        onView(withId(R.id.postText)).perform(typeText("   Salut Salut  "));
        onView(withId (R.id.postText)).perform(closeSoftKeyboard());

        onView(withId(R.id.postButton)).perform(click());

        assertTrue(intentsRule.getActivity().isFinishing());


    }

    @Test
    public void clickCameraButtonLeavesToPostActivity() {

        onView(withId(R.id.cameraButton)).perform(click());

    }

    @Test
    public void clickGalleryButtonDisplaysImage() {

        Intent resultData = new Intent();
        resultData.setAction(Intent.ACTION_GET_CONTENT);
        Uri imageUri = Uri.parse("android.resource://ch.epfl.sdp.kandle/drawable/ic_launcher_background.xml");
        resultData.setData(imageUri);
        Instrumentation.ActivityResult result = new Instrumentation.ActivityResult(Activity.RESULT_OK, resultData);
        intending(hasAction(Intent.ACTION_GET_CONTENT)).respondWith(result);

        onView(withId(R.id.galleryButton)).perform(click());
        onView(withId(R.id.postImage)).check(matches(withTagValue(is(PostActivity.POST_IMAGE_TAG))));

        onView(withId(R.id.postButton)).perform(click());

    }


}
