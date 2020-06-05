package ch.epfl.sdp.kandle.fragment;

import android.view.Gravity;
import android.view.View;

import androidx.room.Room;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.contrib.DrawerActions;
import androidx.test.espresso.contrib.NavigationViewActions;
import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.rule.ActivityTestRule;
import androidx.test.rule.GrantPermissionRule;

import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.HashMap;
import java.util.LinkedList;

import ch.epfl.sdp.kandle.Kandle;
import ch.epfl.sdp.kandle.R;
import ch.epfl.sdp.kandle.activity.MainActivity;
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
import static androidx.test.espresso.action.ViewActions.clearText;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.contrib.DrawerMatchers.isClosed;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static ch.epfl.sdp.kandle.fragment.ProfileFragmentTest.atPosition;
import static ch.epfl.sdp.kandle.dependencies.DependencyManager.getDatabaseSystem;

public class SearchFragmentTest {

    @Rule
    public GrantPermissionRule permissionRule = GrantPermissionRule.grant(android.Manifest.permission.ACCESS_FINE_LOCATION);

    private User user1, user2, user3, user4;
    private LocalDatabase localDatabase;

    @Rule
    public ActivityTestRule<MainActivity> intentsRule =
            new ActivityTestRule<MainActivity>(MainActivity.class, true, true) {
                @Override
                protected void beforeActivityLaunched() {
                    user1 = new User("user1Id", "user1", "user1@kandle.ch", null, null);
                    user2 = new User("user2Id", "user2", "user2@kandle.ch", null, "image");
                    user3 = new User("user3Id", "user3", "user3@kandle.ch", null, "image");
                    user4 = new User("user4Id", "user4", "user4@kandle.ch", null, "image");
                    LoggedInUser.init(new User("loggedInUserId", "LoggedInUser", "loggedInUser@kandle.ch", "nickname", "image"));
                    HashMap<String, String> accounts = new HashMap<>();
                    accounts.put(user1.getEmail(), user1.getId());
                    accounts.put(user2.getEmail(), user2.getId());
                    accounts.put(user3.getEmail(), user3.getId());
                    accounts.put(user4.getEmail(), user4.getId());
                    HashMap<String, User> users = new HashMap<>();
                    HashMap<String, MockDatabase.Follow> followMap = new HashMap<>();
                    followMap.put(user1.getId(), new MockDatabase.Follow(new LinkedList<>(), new LinkedList<>()));
                    followMap.put(user2.getId(), new MockDatabase.Follow(new LinkedList<>(), new LinkedList<>()));
                    followMap.put(user3.getId(), new MockDatabase.Follow(new LinkedList<>(), new LinkedList<>()));
                    followMap.put(user4.getId(), new MockDatabase.Follow(new LinkedList<>(), new LinkedList<>()));
                    HashMap<String, Post> posts = new HashMap<>();
                    MockDatabase db = new MockDatabase(true, users, followMap, posts);
                    MockAuthentication authentication = new MockAuthentication(true, accounts, "password");
                    MockImageStorage storage = new MockImageStorage();
                    MockInternalStorage internalStorage = new MockInternalStorage(new HashMap<>());
                    MockNetwork network = new MockNetwork(true);
                    localDatabase = Room.inMemoryDatabaseBuilder(Kandle.getContext(), LocalDatabase.class).allowMainThreadQueries().build();
                    DependencyManager.setFreshTestDependencies(authentication, db, storage, internalStorage, network, localDatabase);
                    getDatabaseSystem().createUser(user1);
                    getDatabaseSystem().createUser(user2);
                    getDatabaseSystem().createUser(user3);
                    getDatabaseSystem().createUser(user4);
                    getDatabaseSystem().follow(LoggedInUser.getInstance().getId(), user1.getId());
                    getDatabaseSystem().follow(user1.getId(), LoggedInUser.getInstance().getId());
                }
            };

    private static ViewAction clickChildViewWithId(final int id) {
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

    @After
    public void clearCurrentUserAndLocalDb() {
        LoggedInUser.clear();
        localDatabase.close();
    }

    @Before
    public void loadFragment() {
        onView(ViewMatchers.withId(R.id.drawer_layout)).check(matches(isClosed(Gravity.LEFT))).perform(DrawerActions.open());
        onView(withId(R.id.navigation_view)).perform(NavigationViewActions.navigateTo(R.id.follow));
    }

    @Test
    public void followThenUnfollow() {

        onView(withId(R.id.search_bar)).perform(typeText("us"));
        onView(withId(R.id.search_bar)).perform(closeSoftKeyboard());

        onView(withId(R.id.search_bar)).perform(clearText());
        onView(withId(R.id.search_bar)).perform(closeSoftKeyboard());

        onView(withId(R.id.search_bar)).perform(typeText("us"));
        onView(withId(R.id.search_bar)).perform(closeSoftKeyboard());

        onView(withId(R.id.recycler_view)).perform(RecyclerViewActions.actionOnItemAtPosition(0, clickChildViewWithId(R.id.btn_follow)));
        onView(withId(R.id.recycler_view)).perform(RecyclerViewActions.actionOnItemAtPosition(0, clickChildViewWithId(R.id.btn_follow)));
        onView(withId(R.id.recycler_view)).perform(RecyclerViewActions.actionOnItemAtPosition(0, clickChildViewWithId(R.id.btn_follow)));

        onView(withId(R.id.recycler_view)).perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));

        onView(withId(R.id.profileNumberOfFollowing)).check(matches(withText("1")));
        onView(withId(R.id.profileNumberOfFollowers)).check(matches(withText("0")));

        onView(withId(R.id.profileFollowButton)).perform(click());
        onView(withId(R.id.profileNumberOfFollowers)).check(matches(withText("1")));
        onView(withId(R.id.profileFollowButton)).perform(click());
        onView(withId(R.id.profileNumberOfFollowers)).check(matches(withText("0")));
        onView(withId(R.id.profileFollowButton)).perform(click());
    }

    @Test
    public void clickOnUserProfile() {
        onView(withId(R.id.search_bar)).perform(typeText("us"));
        onView(withId(R.id.search_bar)).perform(closeSoftKeyboard());
        onView(withId(R.id.recycler_view)).perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));
        onView(withId(R.id.profileUsername)).check(matches(withText("@" + user1.getUsername())));
    }

    @Test
    public void userWithNoProfilePic() {
        onView(withId(R.id.search_bar)).perform(typeText(user1.getUsername()));
        onView(withId(R.id.search_bar)).perform(closeSoftKeyboard());
    }

    @Test
    public void checkFollowingPropositions() {
        getDatabaseSystem().follow(user1.getId(), user2.getId());
        getDatabaseSystem().follow(user1.getId(), user3.getId());
        loadFragment();
        onView(withId(R.id.recycler_view)).check(matches(atPosition(0, hasDescendant(withText("@" + user2.getUsername())))));
        onView(withId(R.id.recycler_view)).check(matches(atPosition(1, hasDescendant(withText("@" + user3.getUsername())))));
    }

    @Test
    public void writeAndDeleteShowsList() {
        getDatabaseSystem().follow(user1.getId(), user2.getId());
        getDatabaseSystem().follow(user1.getId(), user3.getId());
        onView(withId(R.id.search_bar)).perform(typeText("us"));
        onView(withId(R.id.search_bar)).perform(closeSoftKeyboard());
        onView(withId(R.id.search_bar)).perform(clearText());
        onView(withId(R.id.search_bar)).perform(closeSoftKeyboard());
        onView(withId(R.id.recycler_view)).check(matches(atPosition(0, hasDescendant(withText("@" + user2.getUsername())))));
        onView(withId(R.id.recycler_view)).check(matches(atPosition(1, hasDescendant(withText("@" + user3.getUsername())))));
    }

    @Test
    public void writeFollowDeleteTextShowsList() {
        getDatabaseSystem().follow(user1.getId(), user2.getId());
        getDatabaseSystem().follow(user1.getId(), user3.getId());
        getDatabaseSystem().follow(user2.getId(), user4.getId());
        onView(withId(R.id.search_bar)).perform(typeText("user2"));
        onView(withId(R.id.search_bar)).perform(closeSoftKeyboard());
        onView(withId(R.id.recycler_view)).perform(RecyclerViewActions.actionOnItemAtPosition(0, clickChildViewWithId(R.id.btn_follow)));
        onView(withId(R.id.search_bar)).perform(clearText());
        onView(withId(R.id.search_bar)).perform(closeSoftKeyboard());
        onView(withId(R.id.recycler_view)).check(matches(atPosition(0, hasDescendant(withText("@" + user3.getUsername())))));
        onView(withId(R.id.recycler_view)).check(matches(atPosition(1, hasDescendant(withText("@" + user4.getUsername())))));
    }
}
