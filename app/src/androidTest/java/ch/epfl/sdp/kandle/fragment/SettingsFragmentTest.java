package ch.epfl.sdp.kandle.fragment;

import android.view.Gravity;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.HashMap;

import androidx.room.Room;
import androidx.test.espresso.contrib.DrawerActions;
import androidx.test.espresso.contrib.NavigationViewActions;
import androidx.test.espresso.intent.Intents;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.rule.ActivityTestRule;
import androidx.test.rule.GrantPermissionRule;
import ch.epfl.sdp.kandle.Kandle;
import ch.epfl.sdp.kandle.R;
import ch.epfl.sdp.kandle.activity.LoginActivity;
import ch.epfl.sdp.kandle.activity.MainActivity;
import ch.epfl.sdp.kandle.dependencies.DependencyManager;
import ch.epfl.sdp.kandle.dependencies.MockAuthentication;
import ch.epfl.sdp.kandle.dependencies.MockDatabase;
import ch.epfl.sdp.kandle.dependencies.MockImageStorage;
import ch.epfl.sdp.kandle.dependencies.MockInternalStorage;
import ch.epfl.sdp.kandle.dependencies.MockNetwork;
import ch.epfl.sdp.kandle.entities.post.Post;
import ch.epfl.sdp.kandle.entities.user.User;
import ch.epfl.sdp.kandle.storage.room.LocalDatabase;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.contrib.DrawerMatchers.isClosed;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.matcher.RootMatchers.withDecorView;
import static androidx.test.espresso.matcher.ViewMatchers.hasErrorText;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static ch.epfl.sdp.kandle.entities.user.LoggedInUser.clear;
import static ch.epfl.sdp.kandle.entities.user.LoggedInUser.init;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

public class SettingsFragmentTest {

    @Rule
    public GrantPermissionRule permissionRule = GrantPermissionRule.grant(android.Manifest.permission.ACCESS_FINE_LOCATION);
    private LocalDatabase localDatabase;
    @Rule
    public ActivityTestRule<MainActivity> intentsRule =
            new ActivityTestRule<MainActivity>(MainActivity.class, true, true
            ) {
                @Override
                protected void beforeActivityLaunched() {
                    init(new User("loggedInUserId", "LoggedInUser", "loggedInUser@kandle.ch", "nickname", "image"));
                    HashMap<String, String> accounts = new HashMap<>();
                    HashMap<String, User> users = new HashMap<>();
                    HashMap<String, MockDatabase.Follow> followMap = new HashMap<>();
                    HashMap<String, Post> posts = new HashMap<>();
                    MockDatabase db = new MockDatabase(true, users, followMap, posts);
                    MockAuthentication authentication = new MockAuthentication(true, accounts, "password");
                    MockImageStorage storage = new MockImageStorage();
                    MockInternalStorage internalStorage = new MockInternalStorage(new HashMap<>());
                    MockNetwork network = new MockNetwork(true);
                    localDatabase = Room.inMemoryDatabaseBuilder(Kandle.getContext(), LocalDatabase.class).allowMainThreadQueries().build();
                    DependencyManager.setFreshTestDependencies(authentication, db, storage, internalStorage, network, localDatabase);


                }
            };


    @After
    public void clearCurrentUserAndLocalDb() {
        clear();
        localDatabase.close();
    }

    @Before
    public void loadFragment() throws InterruptedException {
        onView(ViewMatchers.withId(R.id.drawer_layout)).check(matches(isClosed(Gravity.LEFT))).perform(DrawerActions.open());
        onView(withId(R.id.navigation_view)).perform(NavigationViewActions.navigateTo(R.id.settings));
        Thread.sleep(1000);
    }


    @Test
    public void wrongOldPasswordDisplaysError() {
        onView(withId(R.id.modifyPassword)).perform(click());
        onView(withId(R.id.oldPassword)).perform(typeText("passworf"));
        onView(withId(R.id.oldPassword)).perform(closeSoftKeyboard());
        onView(withId(R.id.validatePasswordButton)).perform(click());
        onView(withId(R.id.oldPassword)).check(matches(hasErrorText("Unable to authenticate, please check that your password is correct")));
    }

    @Test
    public void invalidNewPasswordDisplaysError() {
        onView(withId(R.id.modifyPassword)).perform(click());
        onView(withId(R.id.oldPassword)).perform(typeText("password"));
        onView(withId(R.id.newPassword)).perform(closeSoftKeyboard());
        onView(withId(R.id.newPassword)).perform(typeText("passwor"));
        onView(withId(R.id.newPassword)).perform(closeSoftKeyboard());
        onView(withId(R.id.validatePasswordButton)).perform(click());
        onView(withId(R.id.newPassword)).check(matches(hasErrorText("Please choose a password of more than 8 characters !")));
    }

    @Test
    public void notMatchingPasswordsDisplaysError() {
        onView(withId(R.id.modifyPassword)).perform(click());
        onView(withId(R.id.oldPassword)).perform(typeText("password"));
        onView(withId(R.id.newPassword)).perform(closeSoftKeyboard());
        onView(withId(R.id.newPassword)).perform(typeText("HoldTheDoor"));
        onView(withId(R.id.newPassword)).perform(closeSoftKeyboard());
        onView(withId(R.id.newPasswordConfirm)).perform(typeText("PasAuDehors"));
        onView(withId(R.id.newPasswordConfirm)).perform(closeSoftKeyboard());
        onView(withId(R.id.validatePasswordButton)).perform(click());
        onView(withId(R.id.newPasswordConfirm)).check(matches(hasErrorText("Your passwords do not match !")));
    }

    @Test
    public void correctPasswordInputDisplaysToast() {
        onView(withId(R.id.modifyPassword)).perform(click());
        onView(withId(R.id.oldPassword)).perform(typeText("password"));
        onView(withId(R.id.newPassword)).perform(closeSoftKeyboard());
        onView(withId(R.id.newPassword)).perform(typeText("newpassword"));
        onView(withId(R.id.newPassword)).perform(closeSoftKeyboard());
        onView(withId(R.id.newPasswordConfirm)).perform(typeText("newpassword"));
        onView(withId(R.id.newPasswordConfirm)).perform(closeSoftKeyboard());
        onView(withId(R.id.validatePasswordButton)).perform(click());
        onView(withText("Your password has been successfully updated")).inRoot(withDecorView(not(is(intentsRule.getActivity().getWindow().getDecorView())))).check(matches(isDisplayed()));
    }

    @Test
    public void viewsCanBeExpanded() {
        onView(withId(R.id.otherSettings)).perform(click());
        onView(withId(R.id.otherSettings)).perform(click());
    }

    @Test
    public void accountDeletion() {
        Intents.init();
        onView(withId(R.id.deleteAccountButton)).perform((click()));
        intended(hasComponent(LoginActivity.class.getName()));
        Intents.release();

    }

    @Test
    public void clearCache() {
        onView(withId(R.id.clearCacheButton)).perform((click()));


    }


}
