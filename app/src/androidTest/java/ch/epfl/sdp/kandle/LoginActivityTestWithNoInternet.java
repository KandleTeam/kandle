package ch.epfl.sdp.kandle;

import android.Manifest;
import android.content.res.Resources;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ActivityTestRule;
import androidx.test.rule.GrantPermissionRule;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashMap;

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
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

@RunWith(AndroidJUnit4.class)
public class LoginActivityTestWithNoInternet {

    Resources res = ApplicationProvider.getApplicationContext().getResources();
    User alreadyHasAnAccount;
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
                    MockInternalStorage internalStorage = new MockInternalStorage(false);
                    MockNetwork network = new MockNetwork(false);
                    DependencyManager.setFreshTestDependencies(authentication, db, storage,internalStorage,network);
                }
            };

    @Rule
    public GrantPermissionRule grantLocation = GrantPermissionRule.grant(Manifest.permission.ACCESS_FINE_LOCATION);


    @Test
    public void doNotHaveInternetWhenLoginIn() {
        onView(withId(R.id.email)).perform(typeText("user1@kandle.ch"));
        onView(withId(R.id.email)).perform(closeSoftKeyboard());
        onView(withId(R.id.password)).perform(typeText("123456789"));
        onView(withId(R.id.password)).perform(closeSoftKeyboard());
        onView(withId(R.id.loginBtn)).perform(click());
        onView(withId(com.google.android.material.R.id.snackbar_text))
                .check(matches(withText(R.string.no_connexion)));

    }

}