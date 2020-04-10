package ch.epfl.sdp.kandle;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Intent;
import android.net.Uri;

import android.provider.MediaStore;
import androidx.test.espresso.intent.rule.IntentsTestRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.GrantPermissionRule;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashMap;

import ch.epfl.sdp.kandle.activity.PostActivity;
import ch.epfl.sdp.kandle.dependencies.DependencyManager;
import ch.epfl.sdp.kandle.dependencies.MockAuthentication;
import ch.epfl.sdp.kandle.dependencies.MockDatabase;
import ch.epfl.sdp.kandle.dependencies.MockInternalStorage;
import ch.epfl.sdp.kandle.dependencies.MockNetwork;
import ch.epfl.sdp.kandle.dependencies.MockStorage;
import ch.epfl.sdp.kandle.dependencies.Post;

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
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;


@RunWith(AndroidJUnit4.class)
public class PostActivityTest {


    @Rule
    public IntentsTestRule<PostActivity> intentsRule =
            new IntentsTestRule<PostActivity>(PostActivity.class,true,true){
                @Override
                protected void beforeActivityLaunched() {
                    LoggedInUser.init(new User("loggedInUserId","LoggedInUser","loggedInUser@kandle.ch","nickname","image"));
                    HashMap<String, String> accounts = new HashMap<>();
                    HashMap<String,User> users = new HashMap<>();
                    HashMap<String, MockDatabase.Follow> followMap = new HashMap<>();
                    HashMap<String, Post> posts = new HashMap<>();
                    MockDatabase db = new MockDatabase(true, users, followMap, posts);
                    MockAuthentication authentication = new MockAuthentication(true, accounts, "password");
                    MockStorage storage = new MockStorage();
                    MockInternalStorage internalStorage = new MockInternalStorage();
                    MockNetwork network = new MockNetwork(true);
                    DependencyManager.setFreshTestDependencies(authentication, db, storage,internalStorage,network);
                }
            };
    @Rule
    public GrantPermissionRule permissionRule = GrantPermissionRule.grant(android.Manifest.permission.ACCESS_FINE_LOCATION);

    @Rule
    public GrantPermissionRule mCameraPermissionRule =
            GrantPermissionRule.grant(android.Manifest.permission.CAMERA);
    @Rule
    public GrantPermissionRule mStoragePermissionRule =
            GrantPermissionRule.grant(android.Manifest.permission.WRITE_EXTERNAL_STORAGE);


    @After
    public void clearCurrentUser(){
        LoggedInUser.clear();
    }

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

        Intent resultData = new Intent();
        resultData.setAction(Intent.ACTION_GET_CONTENT);
        Uri imageUri = Uri.parse("android.resource://ch.epfl.sdp.kandle/drawable/ic_launcher_background.xml");
        resultData.setData(imageUri);
        Instrumentation.ActivityResult result = new Instrumentation.ActivityResult(Activity.RESULT_OK, resultData);
        intending(hasAction(MediaStore.ACTION_IMAGE_CAPTURE)).respondWith(result);

        onView(withId(R.id.cameraButton)).perform(click());
        onView(withId(R.id.postImage)).check(matches(withTagValue(is(PostActivity.POST_IMAGE_TAG))));


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
