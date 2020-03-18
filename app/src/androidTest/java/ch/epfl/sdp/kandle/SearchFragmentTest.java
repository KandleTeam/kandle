package ch.epfl.sdp.kandle;

import android.net.Uri;
import android.os.Parcel;
import android.view.Gravity;
import android.view.View;

import com.google.android.gms.internal.firebase_auth.zzff;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.FirebaseUserMetadata;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.auth.zzy;
import com.google.firebase.auth.zzz;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.contrib.DrawerActions;
import androidx.test.espresso.contrib.NavigationViewActions;
import androidx.test.espresso.contrib.RecyclerViewActions;

import androidx.test.espresso.intent.Intents;
import androidx.test.rule.ActivityTestRule;
import ch.epfl.sdp.kandle.DependencyInjection.Authentication;
import ch.epfl.sdp.kandle.DependencyInjection.Database;
import ch.epfl.sdp.kandle.DependencyInjection.MockAuthentication;
import ch.epfl.sdp.kandle.Fragment.SearchFragment;

import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.mockito.Mockito.*;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.contrib.DrawerMatchers.isClosed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

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
                    Authentication.setAuthenticationSystem(new MockAuthentication(true));
                    Database.setDatabaseSystem(new MockDatabase());
                }
            };


    @Before
    public void loadFragment(){
        onView(withId(R.id.drawer_layout)).check(matches(isClosed(Gravity.LEFT))).perform(DrawerActions.open());
        onView(withId(R.id.navigation_view)).perform(NavigationViewActions.navigateTo(R.id.follow));
    }


    @Test
    public void followThenUnfollow(){

        onView(withId(R.id.search_bar)).perform(typeText("us"));
        onView(withId (R.id.search_bar)).perform(closeSoftKeyboard());

        onView(withId(R.id.recycler_view)).perform(RecyclerViewActions.actionOnItemAtPosition(0, clickChildViewWithId(R.id.btn_follow)));
        onView(withId(R.id.recycler_view)).perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));

        onView(withId(R.id.profileNumberOfFollowing)).check(matches( withText("1")));
        onView(withId(R.id.profileNumberOfFollowers)).check(matches( withText("0")));

        onView(withId(R.id.profileFollowButton)).perform(click());
        onView(withId(R.id.profileNumberOfFollowers)).check(matches( withText("1")));
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
