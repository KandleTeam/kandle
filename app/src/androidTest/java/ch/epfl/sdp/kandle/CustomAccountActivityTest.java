package ch.epfl.sdp.kandle;

import android.app.Activity;
import android.app.Instrumentation.ActivityResult;
import android.content.Intent;
import android.net.Uri;

import androidx.test.espresso.intent.rule.IntentsTestRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
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
import static org.junit.Assert.assertTrue;


import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import ch.epfl.sdp.kandle.DependencyInjection.Database;
import ch.epfl.sdp.kandle.DependencyInjection.MockDatabase;
import ch.epfl.sdp.kandle.DependencyInjection.MockStorage;
import ch.epfl.sdp.kandle.DependencyInjection.Storage;

@RunWith(AndroidJUnit4.class)
public class CustomAccountActivityTest {

    @Rule
    public IntentsTestRule<CustomAccountActivity> intentsRule =
            new IntentsTestRule<CustomAccountActivity>(CustomAccountActivity.class,true,true
            ){
                @Override
                protected  void beforeActivityLaunched() {
                    Storage.setStorageSystem(new MockStorage());
                    Database.setDatabaseSystem(new MockDatabase());
                }

            };

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

        Database.getDatabaseSystem().getProfilePicture().addOnCompleteListener(task -> {
            String uri = task.getResult();
            assertThat(uri, is(not(equalTo(null))));
        });
    }

    @Test
    public void leaveActivity() {
        onView(withId(R.id.startButton)).perform(click());
        intended(hasComponent(MainActivity.class.getName()));
    }
}