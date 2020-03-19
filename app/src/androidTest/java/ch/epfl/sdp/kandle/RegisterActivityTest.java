package ch.epfl.sdp.kandle;

import org.junit.Rule;
import org.junit.Test;

import androidx.test.espresso.contrib.DrawerActions;
import androidx.test.espresso.contrib.NavigationViewActions;
import androidx.test.espresso.intent.Intents;
import androidx.test.rule.ActivityTestRule;
import ch.epfl.sdp.kandle.DependencyInjection.Authentication;
import ch.epfl.sdp.kandle.DependencyInjection.Database;
import ch.epfl.sdp.kandle.DependencyInjection.MockAuthentication;
import ch.epfl.sdp.kandle.DependencyInjection.MockDatabase;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.matcher.RootMatchers.withDecorView;
import static androidx.test.espresso.matcher.ViewMatchers.hasErrorText;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

public class RegisterActivityTest {

    @Rule
    public ActivityTestRule<RegisterActivity> intentsRule =
            new ActivityTestRule<RegisterActivity>(RegisterActivity.class,true,true
            ){
                @Override
                protected  void beforeActivityLaunched() {
                    Authentication.setAuthenticationSystem(new MockAuthentication(false));
                    Database.setDatabaseSystem(new MockDatabase());
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
    public void accountCreationShouldFail(){

        onView(withId (R.id.fullName)).perform(typeText ("Mock User"));
        onView(withId (R.id.fullName)).perform(closeSoftKeyboard());

        onView(withId (R.id.email)).perform(typeText ("user1@test.com"));
        onView(withId (R.id.email)).perform(closeSoftKeyboard());

        onView(withId (R.id.password)).perform(typeText ("12345678"));
        onView(withId (R.id.password)).perform(closeSoftKeyboard());

        onView(withId (R.id.passwordConfirm)).perform(typeText ("12345678"));
        onView(withId (R.id.passwordConfirm)).perform(closeSoftKeyboard());

        onView(withId(R.id.loginBtn)).perform(click());
        onView(withText("An error has occurred : You already have an account")).inRoot(withDecorView(not(is( intentsRule.getActivity().getWindow().getDecorView())))).check(matches(isDisplayed()));
    }


    @Test
    public void accountCreation() {

        Intents.init();

        onView(withId (R.id.fullName)).perform(typeText ("zzdrian Freeman"));
        onView(withId (R.id.fullName)).perform(closeSoftKeyboard());

        onView(withId (R.id.email)).perform(typeText ("zzrian@test.com"));
        onView(withId (R.id.email)).perform(closeSoftKeyboard());

        onView(withId (R.id.password)).perform(typeText ("12345678"));
        onView(withId (R.id.password)).perform(closeSoftKeyboard());

        onView(withId (R.id.passwordConfirm)).perform(typeText ("12345678"));
        onView(withId (R.id.passwordConfirm)).perform(closeSoftKeyboard());

        onView(withId(R.id.loginBtn)).perform(click());


       // onView(withId (R.id.email)).perform(closeSoftKeyboard());
        //onView(withId(R.id.loginBtn)).perform(click());


        intended(hasComponent(CustomAccountActivity.class.getName()));
        onView(withId(R.id.startButton)).perform(click());
        onView(withId(R.id.drawer_layout)).perform(DrawerActions.open());
        onView(withId(R.id.navigation_view)).perform(NavigationViewActions.navigateTo(R.id.logout));


        Intents.release();

    }


    @Test
    public void alreadyHaveAnAccount() throws InterruptedException {

        onView(withId(R.id.signInLink)).perform(click());
        //intended(hasComponent(LoginActivity.class.getName()));
    }


}
