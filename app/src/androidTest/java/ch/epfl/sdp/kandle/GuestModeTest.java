package ch.epfl.sdp.kandle;

import android.Manifest;
import android.content.res.Resources;
import android.view.Gravity;

import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.contrib.DrawerActions;
import androidx.test.espresso.contrib.NavigationViewActions;
import androidx.test.espresso.intent.Intents;
import androidx.test.rule.ActivityTestRule;
import androidx.test.rule.GrantPermissionRule;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.HashMap;

import ch.epfl.sdp.kandle.Storage.room.LocalDatabase;
import ch.epfl.sdp.kandle.activity.LoginActivity;
import ch.epfl.sdp.kandle.activity.MainActivity;
import ch.epfl.sdp.kandle.dependencies.DependencyManager;
import ch.epfl.sdp.kandle.dependencies.MockAuthentication;
import ch.epfl.sdp.kandle.dependencies.MockDatabase;
import ch.epfl.sdp.kandle.dependencies.MockInternalStorage;
import ch.epfl.sdp.kandle.dependencies.MockNetwork;
import ch.epfl.sdp.kandle.dependencies.MockStorage;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.contrib.DrawerMatchers.isClosed;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.matcher.ViewMatchers.*;
import static androidx.test.espresso.intent.Intents.*;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

public class GuestModeTest {

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

    @Before
    public void enterGuestMode() {
        Intents.init();
        onView(withId(R.id.guestModeLink)).perform(click());
        intended(hasComponent(MainActivity.class.getName()));
        Intents.release();
    }

    @After
    public void clearCurrentUserAndLocalDb(){
        LoggedInUser.clear();
        localDatabase.close();
    }

    @Test
    public void guestDrawerContainsReducedNavigation() {
        onView(withId(R.id.drawer_layout)).check(matches(isClosed(Gravity.LEFT))).perform(DrawerActions.open());

        onView(withId(R.id.your_posts)).check(doesNotExist());
        onView(withId(R.id.following_posts)).check(doesNotExist());
        onView(withId(R.id.follow)).check(doesNotExist());
        onView(withId(R.id.achievements)).check(doesNotExist());

        onView(withId(R.id.drawer_layout)).perform(DrawerActions.close());
    }

    @Test
    public void guestCanAccessMapAndCannotPost() {
        onView(withId(R.id.drawer_layout)).check(matches(isClosed(Gravity.LEFT))).perform(DrawerActions.open());
        onView(withId(R.id.navigation_view)).perform(NavigationViewActions.navigateTo(R.id.map_support));
        onView(withId(R.id.newPostButton)).check(matches(not(isDisplayed())));
    }

    @Test
    public void guestCanSeeProfileButNotModify() {
        onView(withId(R.id.drawer_layout)).check(matches(isClosed(Gravity.LEFT))).perform(DrawerActions.open());
        //TODO Manage to make espresso click on profilePicInMenu (throws "error performing single click")
        //onView(withId(R.id.profilePicInMenu)).perform();
    }

    @Test
    public void guestCanAccessSettingsButNotUserSpecificOnes() {
        onView(withId(R.id.drawer_layout)).check(matches(isClosed(Gravity.LEFT))).perform(DrawerActions.open());
        onView(withId(R.id.navigation_view)).perform(NavigationViewActions.navigateTo(R.id.settings));
        onView(withId(R.id.modifyPassword)).check(matches(not(isDisplayed())));
    }

}
