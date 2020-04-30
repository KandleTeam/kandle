package ch.epfl.sdp.kandle;

import android.Manifest;
import android.app.Activity;
import android.app.Instrumentation.ActivityResult;
import android.content.Intent;
import android.net.Uri;

import androidx.room.Room;
import androidx.test.espresso.intent.rule.IntentsTestRule;
import androidx.test.rule.GrantPermissionRule;

import org.junit.After;
import org.junit.Rule;
import org.junit.Test;

import java.util.HashMap;

import ch.epfl.sdp.kandle.storage.room.LocalDatabase;
import ch.epfl.sdp.kandle.activity.CustomAccountActivity;
import ch.epfl.sdp.kandle.activity.MainActivity;
import ch.epfl.sdp.kandle.dependencies.DependencyManager;
import ch.epfl.sdp.kandle.dependencies.MockDatabase;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.Intents.intending;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasAction;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.matcher.ViewMatchers.assertThat;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withTagValue;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsEqual.equalTo;

import ch.epfl.sdp.kandle.dependencies.MockAuthentication;
import ch.epfl.sdp.kandle.dependencies.MockInternalStorage;
import ch.epfl.sdp.kandle.dependencies.MockNetwork;
import ch.epfl.sdp.kandle.dependencies.MockStorage;


public class CustomAccountActivityTest {

    private LocalDatabase localDatabase;
    @Rule
    public IntentsTestRule<CustomAccountActivity> intentsRule =
            new IntentsTestRule<CustomAccountActivity>(CustomAccountActivity.class, true, true) {
                @Override
                protected void beforeActivityLaunched() {
                    LoggedInUser.init(new User("loggedInUserId","LoggedInUser","loggedInUser@kandle.ch",null,null));
                    HashMap<String, String> accounts = new HashMap<>();
                    HashMap<String,User> users = new HashMap<>();
                    HashMap<String, MockDatabase.Follow> followMap = new HashMap<>();
                    HashMap<String, Post> posts = new HashMap<>();
                    MockDatabase db = new MockDatabase(true, users, followMap, posts);
                    MockAuthentication authentication = new MockAuthentication(true, accounts, "password");
                    MockStorage storage = new MockStorage();
                    MockInternalStorage internalStorage = new MockInternalStorage(true);
                    MockNetwork network = new MockNetwork(true);
                    localDatabase = Room.inMemoryDatabaseBuilder(Kandle.getContext(), LocalDatabase.class).allowMainThreadQueries().build();
                    DependencyManager.setFreshTestDependencies(authentication, db, storage,internalStorage,network,localDatabase);

                }
            };

    @Rule

    public GrantPermissionRule grantLocation = GrantPermissionRule.grant(Manifest.permission.ACCESS_FINE_LOCATION);

    @After
    public void signoutAndCloseLocalDb() {
        DependencyManager.getAuthSystem().signOut();
        localDatabase.close();
    }



    @Test
    public void enterUsername() {
        onView(withId(R.id.nickname)).perform(typeText("User 1"));
        onView(withId(R.id.nickname)).perform(closeSoftKeyboard());
        onView(withId(R.id.startButton)).perform(click());
        String nickname = DependencyManager.getAuthSystem().getCurrentUser().getNickname();
        assertThat(nickname, is(equalTo("User 1")));

    }

    @Test
    public void selectProfilePicture() {

        Intent resultData = new Intent();
        resultData.setAction(Intent.ACTION_GET_CONTENT);
        Uri imageUri = Uri.parse("android.resource://ch.epfl.sdp.kandle/drawable/ic_launcher_background.xml");
        resultData.setData(imageUri);
        ActivityResult result = new ActivityResult(Activity.RESULT_OK, resultData);
        intending(hasAction(Intent.ACTION_GET_CONTENT)).respondWith(result);
        onView(withId(R.id.button)).perform(click());
        onView(withId(R.id.profilePic)).check(matches(withTagValue(is(CustomAccountActivity.PROFILE_PICTURE_TAG))));
        onView(withId(R.id.startButton)).perform(click());

        String uri = DependencyManager.getAuthSystem().getCurrentUser().getImageURL();
        assertThat(uri, is(not(equalTo(null))));

    }

    @Test
    public void badResultDoesNotChangePicture() {
        Intent resultData = new Intent();
        resultData.setAction(Intent.ACTION_GET_CONTENT);
        Uri imageUri = Uri.parse("android.resource://ch.epfl.sdp.kandle/drawable/ic_launcher_background.xml");
        resultData.setData(imageUri);
        ActivityResult result = new ActivityResult(Activity.RESULT_CANCELED, resultData);
        intending(hasAction(Intent.ACTION_GET_CONTENT)).respondWith(result);
        onView(withId(R.id.button)).perform(click());
        onView(withId(R.id.profilePic)).check(matches(not((withTagValue(is(CustomAccountActivity.PROFILE_PICTURE_TAG))))));
    }

    @Test
    public void nullDataDoesNotChangePicture() {
        ActivityResult result = new ActivityResult(Activity.RESULT_OK, null);
        intending(hasAction(Intent.ACTION_GET_CONTENT)).respondWith(result);
        onView(withId(R.id.button)).perform(click());
        onView(withId(R.id.profilePic)).check(matches(not((withTagValue(is(CustomAccountActivity.PROFILE_PICTURE_TAG))))));
    }

    @Test
    public void nullUriDoesNotChangePicture() {
        Intent resultData = new Intent();
        resultData.setAction(Intent.ACTION_GET_CONTENT);
        ActivityResult result = new ActivityResult(Activity.RESULT_OK, resultData);
        intending(hasAction(Intent.ACTION_GET_CONTENT)).respondWith(result);
        onView(withId(R.id.button)).perform(click());
        onView(withId(R.id.profilePic)).check(matches(not((withTagValue(is(CustomAccountActivity.PROFILE_PICTURE_TAG))))));
    }

    @Test
    public void leaveActivity() {
        onView(withId(R.id.startButton)).perform(click());
        intended(hasComponent(MainActivity.class.getName()));
    }
}