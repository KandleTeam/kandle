package ch.epfl.sdp.kandle;

import android.view.Gravity;
import android.view.View;

import org.junit.After;
import org.junit.Rule;
import org.junit.Test;

import java.util.HashMap;
import java.util.LinkedList;

import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;
import androidx.test.espresso.NoMatchingViewException;
import androidx.test.espresso.ViewAssertion;
import androidx.test.espresso.contrib.DrawerActions;
import androidx.test.espresso.contrib.NavigationViewActions;
import androidx.test.rule.ActivityTestRule;
import androidx.test.rule.GrantPermissionRule;
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
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.contrib.DrawerMatchers.isClosed;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static ch.epfl.sdp.kandle.ProfileFragmentTest.atPosition;
import static ch.epfl.sdp.kandle.dependencies.DependencyManager.getDatabaseSystem;
import static ch.epfl.sdp.kandle.fragment.PopularUserFragment.USERS_ARRAY_SIZE;
import static junit.framework.TestCase.assertEquals;

public class PopularKandlersTest {

    @Rule
    public GrantPermissionRule permissionRule = GrantPermissionRule.grant(android.Manifest.permission.ACCESS_FINE_LOCATION);
    MockDatabase db;
    private User user1;
    private User user2;
    private User user3;
    private User user4;
    private User user5;
    private User user6;
    private LocalDatabase localDatabase;
    @Rule
    public ActivityTestRule<MainActivity> intentsRule =
            new ActivityTestRule<MainActivity>(MainActivity.class, true, true) {
                @Override
                protected void beforeActivityLaunched() {
                    LoggedInUser.init(new User("loggedInUserId", "LoggedInUser", "loggedInUser@kandle.ch", "nickname", "image"));
                    user1 = new User("user1Id", "user1", "user1@kandle.ch", "user1", null);
                    user2 = new User("user2Id", "user2", "user2@kandle.ch", "user2", null);
                    user3 = new User("user3Id", "user3", "user3@kandle.ch", "user3", null);
                    user4 = new User("user4Id", "user4", "user4@kandle.ch", "user4", null);
                    user5 = new User("user5Id", "user5", "user5@kandle.ch", "user5", null);
                    user6 = new User("user6Id", "user6", "user6@kandle.ch", "user6", null);
                    HashMap<String, String> accounts = new HashMap<>();
                    accounts.put(user1.getEmail(), user1.getId());
                    accounts.put(user2.getEmail(), user2.getId());
                    accounts.put(user3.getEmail(), user3.getId());
                    accounts.put(user4.getEmail(), user4.getId());
                    accounts.put(user5.getEmail(), user5.getId());
                    accounts.put(user6.getEmail(), user6.getId());
                    HashMap<String, User> users = new HashMap<>();
                    HashMap<String, MockDatabase.Follow> followMap = new HashMap<>();
                    followMap.put(user1.getId(), new MockDatabase.Follow(new LinkedList<>(), new LinkedList<>()));
                    followMap.put(user2.getId(), new MockDatabase.Follow(new LinkedList<>(), new LinkedList<>()));
                    followMap.put(user3.getId(), new MockDatabase.Follow(new LinkedList<>(), new LinkedList<>()));
                    followMap.put(user4.getId(), new MockDatabase.Follow(new LinkedList<>(), new LinkedList<>()));
                    followMap.put(user5.getId(), new MockDatabase.Follow(new LinkedList<>(), new LinkedList<>()));
                    followMap.put(user6.getId(), new MockDatabase.Follow(new LinkedList<>(), new LinkedList<>()));
                    HashMap<String, Post> posts = new HashMap<>();
                    db = new MockDatabase(true, users, followMap, posts);
                    MockAuthentication authentication = new MockAuthentication(true, accounts, "password");
                    MockImageStorage storage = new MockImageStorage();
                    MockInternalStorage internalStorage = new MockInternalStorage(new HashMap<>());
                    MockNetwork network = new MockNetwork(true);
                    localDatabase = Room.inMemoryDatabaseBuilder(Kandle.getContext(), LocalDatabase.class).allowMainThreadQueries().build();
                    DependencyManager.setFreshTestDependencies(authentication, db, storage, internalStorage, network, localDatabase);
                    getDatabaseSystem().createUser(user1);
                    getDatabaseSystem().createUser(user2);
                    getDatabaseSystem().createUser(user3);
                    getDatabaseSystem().follow(LoggedInUser.getInstance().getId(), user1.getId());
                    getDatabaseSystem().follow(user2.getId(), user1.getId());
                    getDatabaseSystem().follow(user3.getId(), user1.getId());
                    getDatabaseSystem().follow(user1.getId(), user2.getId());
                    getDatabaseSystem().follow(user3.getId(), user2.getId());
                    getDatabaseSystem().follow(user2.getId(), user3.getId());
                }
            };

    private void setFragment() {
        onView(withId(R.id.drawer_layout)).check(matches(isClosed(Gravity.LEFT))).perform(DrawerActions.open());
        onView(withId(R.id.navigation_view)).perform(NavigationViewActions.navigateTo(R.id.popularKandlers));
        onView(withId(R.id.drawer_layout)).perform(DrawerActions.close());
    }

    @After
    public void clearCurrentUser() {
        LoggedInUser.clear();
        localDatabase.close();
    }

    @Test
    public void checkIfLessUserThanPopularCapacity() {
        setFragment();
        onView(withId(R.id.list_user_recycler_view)).check(new RecyclerViewItemCountAssertion(4));
        onView(withId(R.id.list_user_recycler_view)).check(matches(atPosition(0, hasDescendant(withText("@" + user1.getUsername())))));
        onView(withId(R.id.list_user_recycler_view)).check(matches(atPosition(1, hasDescendant(withText("@" + user2.getUsername())))));
        onView(withId(R.id.list_user_recycler_view)).check(matches(atPosition(2, hasDescendant(withText("@" + user3.getUsername())))));
        onView(withId(R.id.list_user_recycler_view)).check(matches(atPosition(3, hasDescendant(withText("@" + LoggedInUser.getInstance().getUsername())))));
    }

    @Test
    public void checkIfUserHaveRightOrder() {
        getDatabaseSystem().createUser(user4);
        getDatabaseSystem().createUser(user5);
        getDatabaseSystem().createUser(user6);
        getDatabaseSystem().follow(user1.getId(), LoggedInUser.getInstance().getId());
        getDatabaseSystem().follow(user2.getId(), LoggedInUser.getInstance().getId());
        getDatabaseSystem().follow(user1.getId(), user3.getId());
        getDatabaseSystem().follow(user4.getId(), user1.getId());
        getDatabaseSystem().follow(user5.getId(), user1.getId());
        getDatabaseSystem().follow(user6.getId(), user1.getId());
        getDatabaseSystem().follow(user4.getId(), user2.getId());
        getDatabaseSystem().follow(user5.getId(), user2.getId());
        getDatabaseSystem().follow(user4.getId(), user3.getId());
        getDatabaseSystem().follow(user1.getId(), user4.getId());
        setFragment();
        onView(withId(R.id.list_user_recycler_view)).check(new RecyclerViewItemCountAssertion(USERS_ARRAY_SIZE));
        onView(withId(R.id.list_user_recycler_view)).check(matches(atPosition(0, hasDescendant(withText("@" + user1.getUsername())))));
        onView(withId(R.id.list_user_recycler_view)).check(matches(atPosition(1, hasDescendant(withText("@" + user2.getUsername())))));
        onView(withId(R.id.list_user_recycler_view)).check(matches(atPosition(2, hasDescendant(withText("@" + user3.getUsername())))));
        onView(withId(R.id.list_user_recycler_view)).check(matches(atPosition(3, hasDescendant(withText("@" + LoggedInUser.getInstance().getUsername())))));
        onView(withId(R.id.list_user_recycler_view)).check(matches(atPosition(4, hasDescendant(withText("@" + user4.getUsername())))));
    }

    @Test
    public void checkIfFollowButtonWorks() {
        setFragment();
        onView(withId(R.id.list_user_recycler_view)).check(new RecyclerViewItemCountAssertion(4));
        onView(withId(R.id.list_user_recycler_view)).check(matches(atPosition(0, hasDescendant(withText(R.string.followBtnAlreadyFollowing)))));
        onView(withId(R.id.list_user_recycler_view)).check(matches(atPosition(1, hasDescendant(withText(R.string.followBtnNotFollowing)))));
        onView(withId(R.id.list_user_recycler_view)).check(matches(atPosition(2, hasDescendant(withText("@" + user3.getUsername())))));
        onView(withId(R.id.list_user_recycler_view)).check(matches(atPosition(3, hasDescendant(withText("@" + LoggedInUser.getInstance().getUsername())))));
    }

    private class RecyclerViewItemCountAssertion implements ViewAssertion {
        private final int expectedCount;

        RecyclerViewItemCountAssertion(int expectedCount) {
            this.expectedCount = expectedCount;
        }

        @Override
        public void check(View view, NoMatchingViewException noViewFoundException) {
            if (noViewFoundException != null) {
                throw noViewFoundException;
            }

            RecyclerView recyclerView = (RecyclerView) view;
            RecyclerView.Adapter adapter = recyclerView.getAdapter();
            assertEquals(adapter.getItemCount(), expectedCount);
        }
    }
}
