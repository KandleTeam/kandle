package ch.epfl.sdp.kandle;


import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;


import androidx.test.espresso.contrib.DrawerActions;
import androidx.test.espresso.contrib.NavigationViewActions;
import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ActivityTestRule;
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


public class LoginActivityTest {


    @Rule
    public final ActivityTestRule<LoginActivity> mainActivityRule =
            new ActivityTestRule<>(LoginActivity.class);


    @Test
    public void emptyEmailTest (){

        onView(withId (R.id.signUpBtn)).perform(click());
        onView(withId(R.id.email)).check(matches( hasErrorText("Your email is required !")));

   }

    @Test
    public void emptyPasswordTest (){

        onView(withId (R.id.email)).perform(typeText ("test@test.com"));
        onView(withId (R.id.email)).perform(closeSoftKeyboard());

        onView(withId (R.id.signUpBtn)).perform(click());
        onView(withId(R.id.password)).check(matches(hasErrorText("Please enter a password")));
    }

    @Test
    public void wrongCredentialsTest () throws InterruptedException {

        onView(withId (R.id.email)).perform(typeText ("zzzz@test.com"));
        onView(withId (R.id.email)).perform(closeSoftKeyboard());

        onView(withId (R.id.password)).perform(typeText ("zzzzzzzzzz"));
        onView(withId (R.id.password)).perform(closeSoftKeyboard());

        onView(withId (R.id.signUpBtn)).perform(click());

        Thread.sleep(2000);
        onView(withId(R.id.email)).check(matches(hasErrorText("Wrong Credentials")));
        onView(withId(R.id.password)).check(matches(hasErrorText("Wrong Credentials")));
    }

    @Test
    public void authenticationTest() throws InterruptedException {

        Intents.init();


        onView(withId (R.id.email)).perform(typeText ("anas.ibrahim@epfl.ch"));
        onView(withId (R.id.email)).perform(closeSoftKeyboard());
        Thread.sleep(500);

        onView(withId (R.id.password)).perform(typeText ("12345678"));
        onView(withId (R.id.password)).perform(closeSoftKeyboard());
        Thread.sleep(500);

        onView(withId (R.id.signUpBtn)).perform(click());

        Thread.sleep(2000);
        intended(hasComponent(MainActivity.class.getName()));
        onView(withId(R.id.drawer_layout)).perform(DrawerActions.open());
        Thread.sleep(1000);
        onView(withId(R.id.navigation_view)).perform(NavigationViewActions.navigateTo(R.id.logout));

        Thread.sleep(1000);

        Intents.release();

    }

    @Test
    public void alreadyHaveAnAccount() throws InterruptedException {
        Intents.init();

        onView(withId(R.id.signUpLink)).perform(click());
        Thread.sleep(1000);
        intended(hasComponent(RegisterActivity.class.getName()));

        Intents.release();
    }

}
