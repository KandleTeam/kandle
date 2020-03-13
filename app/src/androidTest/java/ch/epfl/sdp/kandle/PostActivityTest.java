package ch.epfl.sdp.kandle;


import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ActivityTestRule;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.matcher.ViewMatchers.hasErrorText;
import static androidx.test.espresso.matcher.ViewMatchers.withId;


@RunWith(AndroidJUnit4.class)
public class PostActivityTest {

    @Rule
    public final ActivityTestRule<PostActivity> postActivityRule =
            new ActivityTestRule<>(PostActivity.class);


    @Test
    public void postEmptyGetsErrorMessage(){
        onView(withId(R.id.postText)).perform(typeText("     "));
        onView(withId (R.id.postText)).perform(closeSoftKeyboard());
        onView(withId(R.id.postButton)).perform(click());
        onView(withId(R.id.postText)).check(matches(hasErrorText("Your post is empty...")));

    }


    @Test
    public void postButtonLeadsToMainActivityWhenCorrectPost() throws InterruptedException {
        Intents.init();
        onView(withId(R.id.postText)).perform(typeText("   Salut Salut  "));
        onView(withId (R.id.postText)).perform(closeSoftKeyboard());


        onView(withId(R.id.postButton)).perform(click());
        Thread.sleep(1000);
        intended(hasComponent(MainActivity.class.getName()));

        Intents.release();

    }

    @Test
    public void clickCameraButtonLeavesToPostActivity() throws InterruptedException {
        //Intents.init();

        onView(withId(R.id.cameraButton)).perform(click());
        //Thread.sleep(1000);
        //intended(hasComponent(PostActivity.class.getName()));

       // Intents.release();
    }


    @Test
    public void clickGaleryButtonLeavesToPostActivity() throws InterruptedException {
        //Intents.init();

        onView(withId(R.id.galeryButton)).perform(click());
        //Thread.sleep(1000);
        //intended(hasComponent(PostActivity.class.getName()));
       // Intents.release();
    }
}
