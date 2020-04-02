package ch.epfl.sdp.kandle;

import android.app.Activity;
import android.app.Instrumentation.ActivityResult;
import android.content.Intent;
import android.net.Uri;
import androidx.test.espresso.intent.rule.IntentsTestRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
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
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import java.util.HashMap;
import ch.epfl.sdp.kandle.dependencies.DependencyManager;
import ch.epfl.sdp.kandle.dependencies.Follow;

@RunWith(AndroidJUnit4.class)
public class CustomAccountActivityTest {

    @Rule
    public IntentsTestRule<CustomAccountActivity> intentsRule =
            new IntentsTestRule<CustomAccountActivity>(CustomAccountActivity.class, true, true){
                @Override
                protected void beforeActivityLaunched() {
                    LoggedInUser.init(new User("loggedInUserId","LoggedInUser","loggedInUser@kandle.ch",null,null));
                    HashMap<String,String> accounts = new HashMap<>();
                    HashMap<String,User> users = new HashMap<>();
                    HashMap<String, Follow> followMap = new HashMap<>();
                    HashMap<String, Post> posts = new HashMap<>();
                    DependencyManager.setFreshTestDependencies(true,accounts,users,followMap,posts);
                }
            };


    @After
    public void signout() {
        DependencyManager.getAuthSystem().signOut();
    }


    @Test
    public void enterUsername() throws InterruptedException {
        onView(withId (R.id.nickname)).perform(typeText ("User 1"));
        onView(withId (R.id.nickname)).perform(closeSoftKeyboard());
        onView(withId(R.id.startButton)).perform(click());

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
        DependencyManager.getDatabaseSystem().getProfilePicture().addOnCompleteListener(task -> {
            String uri = task.getResult();
            assertThat(uri, is(not(equalTo(null))));
        });
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