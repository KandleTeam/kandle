package ch.epfl.sdp.kandle;

import android.view.Gravity;
import android.view.View;

import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.contrib.DrawerActions;
import androidx.test.espresso.contrib.NavigationViewActions;
import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.rule.ActivityTestRule;

import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import ch.epfl.sdp.kandle.dependencies.DependencyManager;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.clearText;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.contrib.DrawerMatchers.isClosed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

public class SearchFragmentTest {


    /*@Rule
    public final ActivityTestRule<MainActivity> mainActivityRule =
            new ActivityTestRule<>(MainActivity.class);

     */

    @Rule
    public ActivityTestRule<MainActivity> intentsRule =
            new ActivityTestRule<MainActivity>(MainActivity.class,true,true
            ){
                @Override
                protected  void beforeActivityLaunched() {
                    DependencyManager.setFreshTestDependencies(true);
                }
            };


    @Before
    public void loadFragment(){
        onView(withId(R.id.drawer_layout)).check(matches(isClosed(Gravity.LEFT))).perform(DrawerActions.open());
        onView(withId(R.id.navigation_view)).perform(NavigationViewActions.navigateTo(R.id.follow));
    }


    @Test
    public void followThenUnfollow() {

        onView(withId(R.id.search_bar)).perform(typeText("us"));
        onView(withId (R.id.search_bar)).perform(closeSoftKeyboard());

        onView(withId(R.id.search_bar)).perform(clearText());
        onView(withId (R.id.search_bar)).perform(closeSoftKeyboard());

        onView(withId(R.id.search_bar)).perform(typeText("us"));
        onView(withId (R.id.search_bar)).perform(closeSoftKeyboard());

        onView(withId(R.id.recycler_view)).perform(RecyclerViewActions.actionOnItemAtPosition(0, clickChildViewWithId(R.id.btn_follow)));
        onView(withId(R.id.recycler_view)).perform(RecyclerViewActions.actionOnItemAtPosition(0, clickChildViewWithId(R.id.btn_follow)));
        onView(withId(R.id.recycler_view)).perform(RecyclerViewActions.actionOnItemAtPosition(0, clickChildViewWithId(R.id.btn_follow)));

        onView(withId(R.id.recycler_view)).perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));

        onView(withId(R.id.profileNumberOfFollowing)).check(matches( withText("1")));
        onView(withId(R.id.profileNumberOfFollowers)).check(matches( withText("0")));

        onView(withId(R.id.profileFollowButton)).perform(click());
        onView(withId(R.id.profileNumberOfFollowers)).check(matches( withText("1")));
        onView(withId(R.id.profileFollowButton)).perform(click());
        onView(withId(R.id.profileNumberOfFollowers)).check(matches( withText("0")));
        onView(withId(R.id.profileFollowButton)).perform(click());
    }

    @Test
    public void clickOnUserProfile(){
        onView(withId(R.id.search_bar)).perform(typeText("us"));
        onView(withId (R.id.search_bar)).perform(closeSoftKeyboard());
        onView(withId(R.id.recycler_view)).perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));
        onView(withId(R.id.profileUsername)).check(matches(withText("@user2")));

    }



    @Test
    public void userWithNoProfilePic() {
        onView(withId(R.id.search_bar)).perform(typeText("user3"));
        onView(withId (R.id.search_bar)).perform(closeSoftKeyboard());
    }



    public static ViewAction clickChildViewWithId(final int id) {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return null;
            }

            @Override
            public String getDescription() {
                return "Click on a child view with specified id.";
            }

            @Override
            public void perform(UiController uiController, View view) {
                View v = view.findViewById(id);
                v.performClick();

            }

        };
    }



}
