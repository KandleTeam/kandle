package ch.epfl.sdp.kandle;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ActivityTestRule;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.swipeLeft;
import static androidx.test.espresso.matcher.ViewMatchers.withId;


@RunWith(AndroidJUnit4.class)
public class MenuActivityTest {
    @Rule
    public final ActivityTestRule<MenuActivity> menuActivity=
            new ActivityTestRule<>(MenuActivity.class);
    @Test
    public void testCanSelectSettings() {
        onView(withId(R.id.mainButton)).perform(click());
        onView(withId(R.id.toolbar)).perform(swipeLeft());
        // onView(withId(R.id.greetingMessage)).check(matches(withText("Hello from my unit test!")));
    }
}
