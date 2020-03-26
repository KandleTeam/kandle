package ch.epfl.sdp.kandle;

import android.view.Gravity;
import android.view.View;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.contrib.DrawerActions;
import androidx.test.espresso.contrib.NavigationViewActions;
import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.espresso.matcher.BoundedMatcher;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.rule.ActivityTestRule;
import ch.epfl.sdp.kandle.dependencies.DependencyManager;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.contrib.DrawerMatchers.isClosed;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

public class YourProfileFragmentTest {

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
        //onView(withId(R.id.navigation_view)).perform(NavigationViewActions.navigateTo(R.id.follow));
        onView(withId(R.id.profilePicInMenu)).perform(click());
    }

    @Test
    public void editButtonIsVisible(){
        onView(withId(R.id.profileEditButton)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)));
    }

    @Test
    public void listOfFollowers(){
        onView(withId(R.id.profileNumberOfFollowers)).perform(click());
        onView(withId(R.id.list_user_recycler_view)).check(matches(atPosition(0, hasDescendant(withText("@user3")))));
    }

    @Test
    public void listOfFollowing(){
        onView(withId(R.id.profileNumberOfFollowing)).perform(click());
        onView(withId(R.id.list_user_recycler_view)).check(matches(atPosition(0, hasDescendant(withText("@user2")))));
        onView(withId(R.id.list_user_recycler_view)).perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));
        onView(withId(R.id.profileUsername)).check(matches(withText("@user2")));
        onView(withId(R.id.profileNumberOfFollowers)).perform(click());
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

    public static Matcher<View> atPosition(final int position, @NonNull final Matcher<View> itemMatcher) {

        return new BoundedMatcher<View, RecyclerView>(RecyclerView.class) {
            @Override
            public void describeTo(Description description) {
                description.appendText("has item at position " + position + ": ");
                itemMatcher.describeTo(description);
            }

            @Override
            protected boolean matchesSafely(final RecyclerView view) {
                RecyclerView.ViewHolder viewHolder = view.findViewHolderForAdapterPosition(position);
                if (viewHolder == null) {
                    // has no item on such position
                    return false;
                }
                return itemMatcher.matches(viewHolder.itemView);
            }
        };
    }


}
