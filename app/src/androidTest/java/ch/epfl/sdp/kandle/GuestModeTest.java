package ch.epfl.sdp.kandle;

import android.Manifest;
import android.content.res.Resources;
import android.location.Location;
import android.view.Gravity;

import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.contrib.DrawerActions;
import androidx.test.espresso.contrib.NavigationViewActions;
import androidx.test.espresso.intent.Intents;
import androidx.test.rule.ActivityTestRule;
import androidx.test.rule.GrantPermissionRule;
import androidx.test.uiautomator.By;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.Until;

import org.junit.After;
import org.junit.Rule;
import org.junit.Test;

import java.util.Date;
import java.util.HashMap;

import ch.epfl.sdp.kandle.storage.room.LocalDatabase;
import ch.epfl.sdp.kandle.activity.LoginActivity;
import ch.epfl.sdp.kandle.activity.MainActivity;
import ch.epfl.sdp.kandle.dependencies.DependencyManager;
import ch.epfl.sdp.kandle.dependencies.MockAuthentication;
import ch.epfl.sdp.kandle.dependencies.MockDatabase;
import ch.epfl.sdp.kandle.dependencies.MockInternalStorage;
import ch.epfl.sdp.kandle.dependencies.MockNetwork;
import ch.epfl.sdp.kandle.dependencies.MockImageStorage;
import ch.epfl.sdp.kandle.fragment.MapViewFragment;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.contrib.DrawerMatchers.isClosed;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.matcher.ViewMatchers.isClickable;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

public class GuestModeTest {

    @Rule
    public GrantPermissionRule grantLocation = GrantPermissionRule.grant(Manifest.permission.ACCESS_FINE_LOCATION);
    private Resources res = ApplicationProvider.getApplicationContext().getResources();
    private LocalDatabase localDatabase;

    @Rule
    public ActivityTestRule<MainActivity> intentsRule =
            new ActivityTestRule<MainActivity>(MainActivity.class, true, true) {
                @Override
                protected void beforeActivityLaunched() {

                    LoggedInUser.initGuestMode();

                    User user1 = new User("user1Id", "user1", "user1@kandle.ch", "user1nickname", "image");
                    HashMap<String, String> accounts = new HashMap<>();
                    accounts.put(user1.getEmail(), user1.getId());

                    HashMap<String, User> users = new HashMap<>();
                    HashMap<String, MockDatabase.Follow> followMap = new HashMap<>();
                    HashMap<String, Post> posts = new HashMap<>();

                    Post closePost = new Post("closePostDescription", null, new Date(), user1.getId(), "closePostId");
                    closePost.setLatitude(0.00015);
                    closePost.setLongitude(0.00015);

                    Post farPost = new Post("farPostDescription", "image", new Date(), user1.getId(), "farPostId");
                    farPost.setLatitude(0.0015);
                    farPost.setLongitude(0.0015);

                    user1.addPostId(closePost.getPostId());
                    user1.addPostId(farPost.getPostId());
                    posts.put(closePost.getPostId(), closePost);
                    posts.put(farPost.getPostId(), farPost);

                    MockDatabase db = new MockDatabase(true, users, followMap, posts);
                    MockAuthentication authentication = new MockAuthentication(false, accounts, null);
                    MockImageStorage storage = new MockImageStorage();
                    MockInternalStorage internalStorage = new MockInternalStorage(new HashMap<>());
                    MockNetwork network = new MockNetwork(true);
                    localDatabase = Room.inMemoryDatabaseBuilder(Kandle.getContext(), LocalDatabase.class).allowMainThreadQueries().build();
                    DependencyManager.setFreshTestDependencies(authentication, db, storage, internalStorage, network, localDatabase);
                    DependencyManager.getDatabaseSystem().createUser(user1);

                }
            };



    @After
    public void clearCurrentUserAndLocalDb() {
        LoggedInUser.clear();
        localDatabase.close();
    }

    @Test
    public void canLogoutAndEnterGuestMode() {
        Intents.init();
        onView(withId(R.id.drawer_layout)).check(matches(isClosed(Gravity.LEFT))).perform(DrawerActions.open());
        onView(withId(R.id.navigation_view)).perform(NavigationViewActions.navigateTo(R.id.logout));
        intended(hasComponent(LoginActivity.class.getName()));
        onView(withId(R.id.guestModeLink)).perform(click());
        intended(hasComponent(MainActivity.class.getName()));
        Intents.release();
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
    public void guestCanSeeNearbyPostsButNotLikeOrFollow() {
        UiDevice uiDevice = UiDevice.getInstance(getInstrumentation());
        uiDevice.wait(Until.hasObject(By.desc("MAP READY")), 2000);

        MapViewFragment map = ((MapViewFragment) intentsRule.getActivity().getCurrentFragment());
        map.goToPostFragment("closePostId", new Location("mock"));

        onView(withId(R.id.postFragmentLikeButton)).check(matches(not(isClickable())));
        onView(withId(R.id.postFragmentFollowButton)).check(matches(not(isDisplayed())));

        Espresso.pressBack();

        map.goToPostFragment("farPostId", new Location("mock"));

        onView(withId(R.id.postFragmentLikeButton)).check(matches(not(isClickable())));
        onView(withId(R.id.postFragmentFollowButton)).check(matches(not(isDisplayed())));

    }

    @Test
    public void guestCanSeeProfileButNotModify() {
        onView(withId(R.id.drawer_layout)).check(matches(isClosed(Gravity.LEFT))).perform(DrawerActions.open());
        // TODO Manage to make espresso click on profilePicInMenu (throws "error performing single click")
        // onView(withId(R.id.profilePicInMenu)).perform();
    }

    @Test
    public void guestCanAccessSettingsButNotUserSpecificOnes() {
        onView(withId(R.id.drawer_layout)).check(matches(isClosed(Gravity.LEFT))).perform(DrawerActions.open());
        onView(withId(R.id.navigation_view)).perform(NavigationViewActions.navigateTo(R.id.settings));
        onView(withId(R.id.otherSettings)).check(matches(isDisplayed()));
        onView(withId(R.id.modifyPassword)).check(matches(not(isDisplayed())));
    }

}
