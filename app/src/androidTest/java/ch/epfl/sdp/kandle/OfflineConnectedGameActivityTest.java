package ch.epfl.sdp.kandle;

import android.view.Gravity;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashMap;

import androidx.room.Room;
import androidx.test.espresso.contrib.DrawerActions;
import androidx.test.espresso.contrib.NavigationViewActions;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ActivityTestRule;
import ch.epfl.sdp.kandle.activity.MainActivity;
import ch.epfl.sdp.kandle.activity.OfflineGameActivity;
import ch.epfl.sdp.kandle.dependencies.DependencyManager;
import ch.epfl.sdp.kandle.dependencies.MockAuthentication;
import ch.epfl.sdp.kandle.dependencies.MockDatabase;
import ch.epfl.sdp.kandle.dependencies.MockImageStorage;
import ch.epfl.sdp.kandle.dependencies.MockInternalStorage;
import ch.epfl.sdp.kandle.dependencies.MockNetwork;
import ch.epfl.sdp.kandle.entities.post.Post;
import ch.epfl.sdp.kandle.entities.user.LoggedInUser;
import ch.epfl.sdp.kandle.entities.user.User;
import ch.epfl.sdp.kandle.storage.room.LocalDatabase;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.contrib.DrawerMatchers.isClosed;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.is;

@RunWith(AndroidJUnit4.class)
public class OfflineConnectedGameActivityTest {
    private LocalDatabase localDatabase;
    private MockNetwork network;
    @Rule
    public ActivityTestRule<MainActivity> intentsRule =
            new ActivityTestRule<MainActivity>(MainActivity.class, true, true) {
                @Override
                protected void beforeActivityLaunched() {
                    LoggedInUser.init(new User("loggedInUserId", "LoggedInUser", "loggedInUser@kandle.ch", "nickname", "image"));
                    HashMap<String, String> accounts = new HashMap<>();
                    HashMap<String, User> users = new HashMap<>();
                    HashMap<String, MockDatabase.Follow> followMap = new HashMap<>();
                    HashMap<String, Post> posts = new HashMap<>();
                    users.put(LoggedInUser.getInstance().getId(), LoggedInUser.getInstance());
                    MockDatabase db = new MockDatabase(true, users, followMap, posts);
                    MockAuthentication authentication = new MockAuthentication(true, accounts, "password");
                    MockImageStorage storage = new MockImageStorage();
                    MockInternalStorage internalStorage = new MockInternalStorage(true, new HashMap<>());
                    network = new MockNetwork(false);
                    localDatabase = Room.inMemoryDatabaseBuilder(Kandle.getContext(), LocalDatabase.class).allowMainThreadQueries().build();
                    localDatabase.userDao().insertUser(LoggedInUser.getInstance());
                    DependencyManager.setFreshTestDependencies(authentication, db, storage, internalStorage, network, localDatabase);
                }
            };

    @Before
    public void navigateToOfflineGame() {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        onView(withId(R.id.drawer_layout)).check(matches(isClosed(Gravity.LEFT))).perform(DrawerActions.open());
        onView(withId(R.id.navigation_view)).perform(NavigationViewActions.navigateTo(R.id.map_support));
        onView(withId(R.id.toolbar)).check(matches(hasDescendant(withText("Map"))));
        onView(withId(R.id.startOfflineGameConnectedButton)).perform(click());
    }


    @Test
    public void userPlayAndSetsRecordThenCached() {
        onView(withId(R.id.startButton)).perform(click());
        for (int i = 0; i < OfflineGameActivity.MAX_NB_VIRUS; i++) {
            onView(withId(R.id.virusButton)).perform(click());
        }
        onView(withId(R.id.maxScore)).check(matches((withText(is(Integer.toString(OfflineGameActivity.MAX_NB_VIRUS))))));
        onView(withId(R.id.backButton)).perform(click());
        network.setIsOnline(true);
        network.setPreviouslyOnline(false);
        onView(withId(R.id.drawer_layout)).check(matches(isClosed(Gravity.LEFT))).perform(DrawerActions.open());
        onView(withId(R.id.navigation_view)).perform(NavigationViewActions.navigateTo(R.id.your_posts));
        onView(withId(R.id.drawer_layout)).perform(DrawerActions.close());
        network.setIsOnline(false);
        network.setPreviouslyOnline(true);
        navigateToOfflineGame();
        onView(withId(R.id.maxScore)).check(matches((withText(is(Integer.toString(OfflineGameActivity.MAX_NB_VIRUS))))));
    }

    @After
    public void clearCurrentUser() {
        LoggedInUser.clear();
        localDatabase.close();
    }

}
