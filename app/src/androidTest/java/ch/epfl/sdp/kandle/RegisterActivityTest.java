package ch.epfl.sdp.kandle;

import org.junit.Rule;
import org.junit.Test;

import androidx.test.espresso.intent.rule.IntentsTestRule;
import androidx.test.rule.ActivityTestRule;

import ch.epfl.sdp.kandle.db.DatabaseManager;
import ch.epfl.sdp.kandle.db.MockDatabase;

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
    public IntentsTestRule intentsRule =
            new IntentsTestRule<RegisterActivity>(RegisterActivity.class,true,true
            ){
                @Override
                protected  void beforeActivityLaunched() {
                    super.beforeActivityLaunched();
                    //Authentication.setAuthenticationSystem(new MockAuthentication());
                    DatabaseManager.setDatabaseSystem(new MockDatabase());
                }
            };



    @Test
    public void errorsInForm (){


        onView(withId (R.id.signUpBtn)).perform(click());
        onView(withId(R.id.fullName)).check(matches( hasErrorText("Your full name is required !")));

        onView(withId (R.id.fullName)).perform(typeText ("test"));
        onView(withId (R.id.fullName)).perform(closeSoftKeyboard());

        onView(withId (R.id.signUpBtn)).perform(click());
        onView(withId(R.id.email)).check(matches( hasErrorText("Your email is required !")));

        onView(withId (R.id.email)).perform(typeText ("test@test.com" ));
        onView(withId (R.id.email)).perform(closeSoftKeyboard());

        onView(withId (R.id.password)).perform(typeText ("123" ));
        onView(withId (R.id.password)).perform(closeSoftKeyboard());

        onView(withId (R.id.signUpBtn)).perform(click());
        onView(withId(R.id.password)).check(matches( hasErrorText("Please choose a password of more than 8 characters !")));

        onView(withId (R.id.password)).perform(typeText ("12345678" ));
        onView(withId (R.id.password)).perform(closeSoftKeyboard());
        onView(withId (R.id.passwordConfirm)).perform(typeText ("123" ));
        onView(withId (R.id.passwordConfirm)).perform(closeSoftKeyboard());

        onView(withId (R.id.signUpBtn)).perform(click());
        onView(withId(R.id.passwordConfirm)).check(matches( hasErrorText("Your passwords do not match !")));

    }




    @Test
    public void accountCreation() throws InterruptedException {

        onView(withId (R.id.fullName)).perform(typeText ("Register Test"));
        onView(withId (R.id.fullName)).perform(closeSoftKeyboard());

        onView(withId (R.id.email)).perform(typeText ("registerTestEmail845@test.ch"));
        onView(withId (R.id.email)).perform(closeSoftKeyboard());

        onView(withId (R.id.password)).perform(typeText ("testPassword"));
        onView(withId (R.id.password)).perform(closeSoftKeyboard());

        onView(withId (R.id.passwordConfirm)).perform(typeText ("testPassword"));
        onView(withId (R.id.passwordConfirm)).perform(closeSoftKeyboard());

        onView(withId(R.id.signUpBtn)).perform(click());

        //intended(hasComponent(MainActivity.class.getName()));


    }


    @Test
    public void alreadyHaveAnAccount() throws InterruptedException {

        onView(withId(R.id.signInLink)).perform(click());
        //intended(hasComponent(LoginActivity.class.getName()));
    }


}
