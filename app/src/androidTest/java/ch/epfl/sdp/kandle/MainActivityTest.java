package ch.epfl.sdp.kandle;

import android.view.Gravity;

import androidx.test.espresso.contrib.DrawerActions;
import androidx.test.espresso.contrib.NavigationViewActions;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ActivityTestRule;
import androidx.test.rule.GrantPermissionRule;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.contrib.DrawerMatchers.isClosed;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.junit.Assert.assertTrue;


@RunWith(AndroidJUnit4.class)
public class MainActivityTest {
    @Rule
    public final ActivityTestRule<MainActivity> mainActivityRule =
            new ActivityTestRule<>(MainActivity.class);

    @Rule
    public GrantPermissionRule permissionRule = GrantPermissionRule.grant(android.Manifest.permission.ACCESS_FINE_LOCATION);

    @Before
    public void checkClosedDrawer() {
        onView(withId(R.id.drawer_layout)).check(matches(isClosed(Gravity.LEFT))).perform(DrawerActions.open());
    }

    @Test
    public void openMenuAndNavigateToAboutUs() throws InterruptedException {

        onView(withId(R.id.navigation_view)).perform(NavigationViewActions.navigateTo(R.id.about));
        onView(withId(R.id.toolbar)).check(matches(hasDescendant(withText("About us"))));


    }

    @Test
    public void openMenuAndLogout() throws InterruptedException {

        onView(withId(R.id.navigation_view)).perform(NavigationViewActions.navigateTo(R.id.logout));

    }


    @Test
    public void openMenuNavigateToSettings() {


        onView(withId(R.id.navigation_view)).perform(NavigationViewActions.navigateTo(R.id.settings));
        onView(withId(R.id.toolbar)).check(matches(hasDescendant(withText("Settings"))));

    }


    @Test
    public void openMenuNavigateToMap() {


        onView(withId(R.id.navigation_view)).perform(NavigationViewActions.navigateTo(R.id.map_support));
        //onView(withId(R.id.toolbar)).check(matches(hasDescendant(withText("Map"))));


    }


}