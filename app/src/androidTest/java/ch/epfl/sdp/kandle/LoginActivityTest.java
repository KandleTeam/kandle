package ch.epfl.sdp.kandle;


import android.Manifest;
import android.content.res.Resources;

import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.contrib.DrawerActions;
import androidx.test.espresso.contrib.NavigationViewActions;
import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ActivityTestRule;
import androidx.test.rule.GrantPermissionRule;

import java.util.HashMap;


import ch.epfl.sdp.kandle.storage.room.LocalDatabase;
import ch.epfl.sdp.kandle.activity.LoginActivity;

import ch.epfl.sdp.kandle.activity.MainActivity;
import ch.epfl.sdp.kandle.activity.RegisterActivity;
import ch.epfl.sdp.kandle.dependencies.DependencyManager;
import ch.epfl.sdp.kandle.dependencies.MockAuthentication;
import ch.epfl.sdp.kandle.dependencies.MockDatabase;
import ch.epfl.sdp.kandle.dependencies.MockInternalStorage;
import ch.epfl.sdp.kandle.dependencies.MockNetwork;
import ch.epfl.sdp.kandle.dependencies.MockStorage;

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


@RunWith(AndroidJUnit4.class)


public class LoginActivityTest {

    private Resources res = ApplicationProvider.getApplicationContext().getResources();
    private User alreadyHasAnAccount;
    private MockNetwork network;
    private LocalDatabase localDatabase;
    @Rule
    public ActivityTestRule<LoginActivity> intentsRule =
            new ActivityTestRule<LoginActivity>(LoginActivity.class, true, true){
                @Override
                protected void beforeActivityLaunched() {
                    alreadyHasAnAccount = new User("user1Id", "username", "user1@kandle.ch", "nickname", null);
                    HashMap<String,String> accounts = new HashMap<>();
                    accounts.put(alreadyHasAnAccount.getEmail(), alreadyHasAnAccount.getId());
                    HashMap<String,User> users = new HashMap<>();
                    users.put(alreadyHasAnAccount.getId(),alreadyHasAnAccount);
                    MockDatabase db = new MockDatabase(false, users, null, null);
                    MockAuthentication authentication = new MockAuthentication(false, accounts, "password");
                    MockStorage storage = new MockStorage();
                    MockInternalStorage internalStorage = new MockInternalStorage();
                    network = new MockNetwork(true);
                    localDatabase = Room.inMemoryDatabaseBuilder(Kandle.getContext(), LocalDatabase.class).allowMainThreadQueries().build();
                    DependencyManager.setFreshTestDependencies(authentication, db, storage,internalStorage,network,localDatabase);
                }
            };

    @Rule
    public GrantPermissionRule grantLocation = GrantPermissionRule.grant(Manifest.permission.ACCESS_FINE_LOCATION);



    @After
    public void clearCurrentUserAndLocalDb(){
        LoggedInUser.clear();
        localDatabase.close();
    }
    @Test
    public void authenticationTestWhereUserExists() {
        Intents.init();
        onView(withId(R.id.email)).perform(typeText(alreadyHasAnAccount.getEmail()));
        onView(withId(R.id.email)).perform(closeSoftKeyboard());
        onView(withId(R.id.password)).perform(typeText("123456789"));
        onView(withId(R.id.password)).perform(closeSoftKeyboard());
        onView(withId(R.id.loginBtn)).perform(click());
        intended(hasComponent(MainActivity.class.getName()));
        onView(withId(R.id.drawer_layout)).perform(DrawerActions.open());
        onView(withId(R.id.navigation_view)).perform(NavigationViewActions.navigateTo(R.id.logout));
        Intents.release();
    }

    @Test
    public void emptyEmailTest() {
        onView(withId(R.id.loginBtn)).perform(click());
        onView(withId(R.id.email)).check(matches(hasErrorText(res.getString(R.string.login_email_required))));
    }

    @Test
    public void emptyPasswordTest() {
        onView(withId(R.id.email)).perform(typeText("test@test.com"));
        onView(withId(R.id.email)).perform(closeSoftKeyboard());
        onView(withId(R.id.loginBtn)).perform(click());
        onView(withId(R.id.password)).check(matches(hasErrorText(res.getString(R.string.login_password_required))));
    }

    @Test
    public void wrongCredentialsTest() {
        onView(withId(R.id.email)).perform(typeText("zzzz@test.com"));
        onView(withId(R.id.email)).perform(closeSoftKeyboard());
        onView(withId(R.id.password)).perform(typeText("zzzzzzzzzz"));
        onView(withId(R.id.password)).perform(closeSoftKeyboard());
        onView(withId(R.id.loginBtn)).perform(click());
        //TODO check toast
    }

    @Test
    public void authenticationShouldFail() {
        onView(withId(R.id.email)).perform(typeText("user2@test.com"));
        onView(withId(R.id.email)).perform(closeSoftKeyboard());
        onView(withId(R.id.password)).perform(typeText("123456789"));
        onView(withId(R.id.password)).perform(closeSoftKeyboard());
        onView(withId(R.id.loginBtn)).perform(click());
        onView(withText("An error has occurred : You do not have an account yet")).inRoot(withDecorView(not(is(intentsRule.getActivity().getWindow().getDecorView())))).check(matches(isDisplayed()));


    }

    @Test
    public void doNotHaveAnAccount() {
        Intents.init();
        onView(withId(R.id.signUpLink)).perform(click());
        intended(hasComponent(RegisterActivity.class.getName()));
        Intents.release();

    }



}
