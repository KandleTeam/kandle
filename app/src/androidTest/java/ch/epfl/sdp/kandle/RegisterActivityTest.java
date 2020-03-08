package ch.epfl.sdp.kandle;

import org.junit.Rule;
import org.junit.Test;

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
    public final ActivityTestRule<RegisterActivity> mActivityRule =
            new ActivityTestRule<>(RegisterActivity.class);



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

        onView(withId (R.id.fullName)).perform(typeText ("Test Register"));
        onView(withId (R.id.fullName)).perform(closeSoftKeyboard());

        onView(withId (R.id.email)).perform(typeText ("new"));
        onView(withId (R.id.email)).perform(closeSoftKeyboard());

        onView(withId (R.id.password)).perform(typeText ("12345678"));
        onView(withId (R.id.password)).perform(closeSoftKeyboard());

        onView(withId (R.id.passwordConfirm)).perform(typeText ("12345678"));
        onView(withId (R.id.passwordConfirm)).perform(closeSoftKeyboard());

        /*
        onView(withId(R.id.signInBtn)).perform(click());
        Thread.sleep(500);


        onView(withId (R.id.email)).perform(typeText ("TestRegister" + LocalDateTime.now().toString().replace( " ", "")
                .replace("." , "").replace(":", "") + "@test.ch"));
        onView(withId (R.id.email)).perform(closeSoftKeyboard());
        onView(withId(R.id.signInBtn)).perform(click());

        Thread.sleep(1000);
        intended(hasComponent(MainActivity.class.getName()));
        */
    }


    @Test
    public void alreadyHaveAnAccount() throws InterruptedException {

        onView(withId(R.id.signInLink)).perform(click());
        //intended(hasComponent(LoginActivity.class.getName()));
    }


}
