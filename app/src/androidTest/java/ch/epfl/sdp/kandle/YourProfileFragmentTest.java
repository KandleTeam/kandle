package ch.epfl.sdp.kandle;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Intent;
import android.net.Uri;
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
import androidx.test.espresso.intent.rule.IntentsTestRule;
import androidx.test.espresso.matcher.BoundedMatcher;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.rule.ActivityTestRule;
import ch.epfl.sdp.kandle.dependencies.DependencyManager;
import ch.epfl.sdp.kandle.fragment.ProfileFragment;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.clearText;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.contrib.DrawerMatchers.isClosed;
import static androidx.test.espresso.intent.Intents.intending;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasAction;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withTagValue;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.is;

public class YourProfileFragmentTest {

    @Rule
    public IntentsTestRule<MainActivity> intentsRule =
            new IntentsTestRule<MainActivity>(MainActivity.class,true,true
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
        onView(withId(R.id.profileEditPictureButton)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)));
    }

    @Test
    public void listOfFollowers(){
        onView(withId(R.id.profileNumberOfFollowers)).perform(click());
        onView(withId(R.id.list_user_recycler_view)).check(matches(atPosition(0, hasDescendant(withText("@user1")))));
    }

    @Test
    public void listOfFollowing(){
        onView(withId(R.id.profileNumberOfFollowing)).perform(click());
        onView(withId(R.id.list_user_recycler_view)).check(matches(atPosition(0, hasDescendant(withText("@user1")))));
        onView(withId(R.id.list_user_recycler_view)).perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));
        onView(withId(R.id.profileUsername)).check(matches(withText("@user1")));
        onView(withId(R.id.profileNumberOfFollowers)).perform(click());
    }

    @Test
    public void editProfilePicture(){
        Intent resultData = new Intent();
        resultData.setAction(Intent.ACTION_GET_CONTENT);
        Uri imageUri = Uri.parse("android.resource://ch.epfl.sdp.kandle/drawable/ic_launcher_background.xml");
        resultData.setData(imageUri);
        Instrumentation.ActivityResult result = new Instrumentation.ActivityResult(Activity.RESULT_OK, resultData);
        intending(hasAction(Intent.ACTION_GET_CONTENT)).respondWith(result);

        onView(withId(R.id.profileEditPictureButton)).perform(click());
        onView(withId(R.id.profilePicture)).check(matches(withTagValue(is(ProfileFragment.PROFILE_PICTURE_AFTER))));
    }

    @Test
    public void editNickname(){
        onView(withId(R.id.profileEditNameButton)).perform(click());
        onView(withId(R.id.edit_view)).perform(clearText());
        onView(withId(R.id.edit_view)).perform(typeText("New Nickname"));
        onView(withId(R.id.profileValidateNameButton)).perform(click());

        onView(withId(R.id.edit_view)).check(matches(withText("New Nickname")));
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
