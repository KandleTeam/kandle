package ch.epfl.sdp.kandle;

import android.Manifest;
import android.view.Gravity;
import androidx.test.espresso.contrib.DrawerActions;
import androidx.test.espresso.contrib.NavigationViewActions;
import androidx.test.espresso.intent.Intents;
import androidx.test.rule.ActivityTestRule;
import androidx.test.rule.GrantPermissionRule;
import ch.epfl.sdp.kandle.dependencies.DependencyManager;
import ch.epfl.sdp.kandle.dependencies.MockAuthentication;
import ch.epfl.sdp.kandle.dependencies.MockDatabase;
import ch.epfl.sdp.kandle.dependencies.MockStorage;

import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import java.util.HashMap;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.contrib.DrawerMatchers.isClosed;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withTagValue;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.is;



public class MainActivityTest {

    @Rule
    public GrantPermissionRule permissionRule = GrantPermissionRule.grant(android.Manifest.permission.ACCESS_FINE_LOCATION);

    @Rule
    public ActivityTestRule<MainActivity> intentsRule =
            new ActivityTestRule<MainActivity>(MainActivity.class,true,true
            ){
                @Override
                protected void beforeActivityLaunched() {
                    LoggedInUser.init(new User("loggedInUserId","LoggedInUser","loggedInUser@kandle.ch","nickname","image"));
                    HashMap<String, String> accounts = new HashMap<>();
                    HashMap<String,User> users = new HashMap<>();
                    HashMap<String, MockDatabase.Follow> followMap = new HashMap<>();
                    HashMap<String,Post> posts = new HashMap<>();
                    MockDatabase db = new MockDatabase(true, users, followMap, posts);
                    MockAuthentication authentication = new MockAuthentication(true, accounts, "password");
                    MockStorage storage = new MockStorage();
                    DependencyManager.setFreshTestDependencies(authentication, db, storage);
                }
            };

    @Rule
    public GrantPermissionRule grantLocation = GrantPermissionRule.grant(android.Manifest.permission.ACCESS_FINE_LOCATION);

    @After
    public void clearCurrentUser(){
        LoggedInUser.clear();
    }



    @Test
    public void openMenuAndNavigateToAboutUs()  {
        onView(withId(R.id.drawer_layout)).check(matches(isClosed(Gravity.LEFT))).perform(DrawerActions.open());
        onView(withId(R.id.navigation_view)).perform(NavigationViewActions.navigateTo(R.id.about));
        onView(withId(R.id.toolbar)).check(matches(hasDescendant(withText("About us"))));
    }

    @Test
    public void openMenuAndNavigateToLogout() {

        onView(withId(R.id.drawer_layout)).check(matches(isClosed(Gravity.LEFT)));
        onView(withId(R.id.drawer_layout)).perform(DrawerActions.open());
        onView(withId(R.id.navigation_view)).perform(NavigationViewActions.navigateTo(R.id.logout));

    }

    @Test
    public void openMenuNavigateToSettings() {


        onView(withId(R.id.drawer_layout)).check(matches(isClosed(Gravity.LEFT))).perform(DrawerActions.open());
        onView(withId(R.id.navigation_view)).perform(NavigationViewActions.navigateTo(R.id.settings));
        onView(withId(R.id.toolbar)).check(matches(hasDescendant(withText("Settings"))));

    }

    /*
    @Test
    public void openMenuNavigateToMap() {

        onView(withId(R.id.drawer_layout)).check(matches(isClosed(Gravity.LEFT))).perform(DrawerActions.open());
        onView(withId(R.id.navigation_view)).perform(NavigationViewActions.navigateTo(R.id.map_support));
        //onView(withId(R.id.toolbar)).check(matches(hasDescendant(withText("Map"))));


    }

     */

    @Test
    public void openMenuNavigateToYourPosts() {

        onView(withId(R.id.drawer_layout)).check(matches(isClosed(Gravity.LEFT))).perform(DrawerActions.open());
        onView(withId(R.id.navigation_view)).perform(NavigationViewActions.navigateTo(R.id.your_posts));
        onView(withId(R.id.toolbar)).check(matches(hasDescendant(withText("Your Posts"))));


    }

    @Test
    public void openMenuNavigateToFollow(){
        onView(withId(R.id.drawer_layout)).check(matches(isClosed(Gravity.LEFT))).perform(DrawerActions.open());
        onView(withId(R.id.navigation_view)).perform(NavigationViewActions.navigateTo(R.id.follow));
        onView(withId(R.id.toolbar)).check(matches(hasDescendant(withText("Follow"))));

    }

    @Test
    public void profilePictureIsDisplayed(){
        onView(withId(R.id.drawer_layout)).check(matches(isClosed(Gravity.LEFT))).perform(DrawerActions.open());
        onView(withId(R.id.profilePicInMenu)).check(matches(withTagValue(is(MainActivity.PROFILE_PICTURE_TAG))));
    }

    @Test
    public void nicknameIsDisplayed() {


        onView(withId(R.id.drawer_layout)).check(matches(isClosed(Gravity.LEFT))).perform(DrawerActions.open());
        onView(withId(R.id.nicknameInMenu)).check(matches(withText(LoggedInUser.getInstance().getNickname())));
    }

    @Test
    public void usernameIsDisplayed() {

        onView(withId(R.id.drawer_layout)).check(matches(isClosed(Gravity.LEFT))).perform(DrawerActions.open());
        onView(withId(R.id.usernameInMenu)).check(matches(withText("@" + LoggedInUser.getInstance().getUsername())));
    }

    @Test
    public void navigateToPost(){
        Intents.init();
        onView(withId(R.id.newPostButton)).perform(click());
        intended(hasComponent(PostActivity.class.getName()));
        Intents.release();
    }

}