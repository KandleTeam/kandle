package ch.epfl.sdp.kandle;

import org.junit.Rule;
import org.junit.Test;

import androidx.test.rule.ActivityTestRule;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

public class RegisterActivityTest {

    @Rule
    public final ActivityTestRule<RegisterActivity>mActivityRule =
            new ActivityTestRule<>(RegisterActivity.class);

    @Test
    public void testField(){
        onView(withId(R.id.signInBtn)).perform(click());
        //onView(withId(R.id.fullName)).check(matches(w));
    }

}
