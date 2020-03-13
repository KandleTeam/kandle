package ch.epfl.sdp.kandle;

import com.google.firebase.FirebaseApp;

import org.junit.Rule;
import org.junit.Test;

import androidx.test.espresso.contrib.DrawerActions;
import androidx.test.espresso.contrib.NavigationViewActions;
import androidx.test.espresso.intent.Intents;
import androidx.test.espresso.intent.rule.IntentsTestRule;
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

public class RegisterActivityTest {

    @Rule
    public ActivityTestRule<RegisterActivity> intentsRule =
            new ActivityTestRule<RegisterActivity>(RegisterActivity.class,true,true
            ){
                @Override
                protected  void beforeActivityLaunched() {
                    FirebaseAuthFactory.setDependency(new FakeFirebaseAuth(FirebaseApp.getInstance()));
                }
            };


    @Test
    public void errorsInForm (){


        onView(withId (R.id.loginBtn)).perform(click());
        onView(withId(R.id.fullName)).check(matches( hasErrorText("Your full name is required !")));

        onView(withId (R.id.fullName)).perform(typeText ("test"));
        onView(withId (R.id.fullName)).perform(closeSoftKeyboard());

        onView(withId (R.id.loginBtn)).perform(click());
        onView(withId(R.id.email)).check(matches( hasErrorText("Your email is required !")));

        onView(withId (R.id.email)).perform(typeText ("test@test.com" ));
        onView(withId (R.id.email)).perform(closeSoftKeyboard());

        onView(withId (R.id.password)).perform(typeText ("123" ));
        onView(withId (R.id.password)).perform(closeSoftKeyboard());

        onView(withId (R.id.loginBtn)).perform(click());
        onView(withId(R.id.password)).check(matches( hasErrorText("Please choose a password of more than 8 characters !")));

        onView(withId (R.id.password)).perform(typeText ("12345678" ));
        onView(withId (R.id.password)).perform(closeSoftKeyboard());
        onView(withId (R.id.passwordConfirm)).perform(typeText ("123" ));
        onView(withId (R.id.passwordConfirm)).perform(closeSoftKeyboard());

        onView(withId (R.id.loginBtn)).perform(click());
        onView(withId(R.id.passwordConfirm)).check(matches( hasErrorText("Your passwords do not match !")));

    }




    @Test
    public void accountCreation() throws InterruptedException {

        //Intents.init();

        onView(withId (R.id.fullName)).perform(typeText ("Test Register"));
        onView(withId (R.id.fullName)).perform(closeSoftKeyboard());

        onView(withId (R.id.email)).perform(typeText ("new@new.com"));
        onView(withId (R.id.email)).perform(closeSoftKeyboard());

        onView(withId (R.id.password)).perform(typeText ("12345678"));
        onView(withId (R.id.password)).perform(closeSoftKeyboard());

        onView(withId (R.id.passwordConfirm)).perform(typeText ("12345678"));
        onView(withId (R.id.passwordConfirm)).perform(closeSoftKeyboard());


        //onView(withId(R.id.loginBtn)).perform(click());
       // Thread.sleep(500);

       // onView(withId (R.id.email)).perform(closeSoftKeyboard());
        //onView(withId(R.id.loginBtn)).perform(click());

       // Thread.sleep(1000);
       // intended(hasComponent(MainActivity.class.getName()));
       // onView(withId(R.id.drawer_layout)).perform(DrawerActions.open());
       // onView(withId(R.id.navigation_view)).perform(NavigationViewActions.navigateTo(R.id.logout));


        //Intents.release();

    }


    @Test
    public void alreadyHaveAnAccount() throws InterruptedException {

        onView(withId(R.id.signInLink)).perform(click());
        //intended(hasComponent(LoginActivity.class.getName()));
    }


}
