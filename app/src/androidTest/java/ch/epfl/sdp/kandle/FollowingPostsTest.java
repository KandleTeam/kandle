package ch.epfl.sdp.kandle;

import android.view.Gravity;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;
import androidx.test.espresso.NoMatchingViewException;
import androidx.test.espresso.ViewAssertion;
import androidx.test.espresso.contrib.DrawerActions;
import androidx.test.espresso.contrib.NavigationViewActions;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ActivityTestRule;
import androidx.test.rule.GrantPermissionRule;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;

import ch.epfl.sdp.kandle.activity.MainActivity;
import ch.epfl.sdp.kandle.dependencies.DependencyManager;
import ch.epfl.sdp.kandle.dependencies.MockAuthentication;
import ch.epfl.sdp.kandle.dependencies.MockDatabase;
import ch.epfl.sdp.kandle.dependencies.MockImageStorage;
import ch.epfl.sdp.kandle.dependencies.MockInternalStorage;
import ch.epfl.sdp.kandle.dependencies.MockNetwork;
import ch.epfl.sdp.kandle.entities.post.Post;
import ch.epfl.sdp.kandle.entities.post.PostAdapter;
import ch.epfl.sdp.kandle.entities.user.LoggedInUser;
import ch.epfl.sdp.kandle.entities.user.User;
import ch.epfl.sdp.kandle.storage.room.LocalDatabase;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.contrib.DrawerMatchers.isClosed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withTagValue;
import static junit.framework.TestCase.assertEquals;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

@RunWith(AndroidJUnit4.class)
public class FollowingPostsTest {

    public static Post p1;
    public static Post p2;
    public User u1;
    public User u2;
    @Rule
    public GrantPermissionRule permissionRule = GrantPermissionRule.grant(android.Manifest.permission.ACCESS_FINE_LOCATION);
    private MockDatabase db;
    private LocalDatabase localDatabase;
    @Rule
    public ActivityTestRule<MainActivity> intentsRule =
            new ActivityTestRule<MainActivity>(MainActivity.class, true, true) {
                @Override
                protected void beforeActivityLaunched() {
                    LoggedInUser.init(new User("loggedInUserId", "LoggedInUser", "loggedInUser@kandle.ch", "nickname", "image"));
                    u1 = new User("u1Id", "u1", "u2@kandle.ch", "u1", "image1");
                    u2 = new User("u2Id", "u2", "u2@kandle.ch", "u2", "image2");
                    p1 = new Post("Hello", null, new Date(), "u1Id", "post1Id");
                    p2 = new Post("There", "image1", new Date(), "u2Id", "post2Id");
                    u1.addPostId(p1.getPostId());
                    u2.addPostId(p2.getPostId());
                    HashMap<String, String> accounts = new HashMap<>();
                    accounts.put(u1.getEmail(), u1.getId());
                    accounts.put(u2.getEmail(), u2.getId());
                    HashMap<String, User> users = new HashMap<>();
                    HashMap<String, MockDatabase.Follow> followMap = new HashMap<>();
                    followMap.put(u1.getId(), new MockDatabase.Follow(new LinkedList<>(), new LinkedList<>()));
                    followMap.put(u2.getId(), new MockDatabase.Follow(new LinkedList<>(), new LinkedList<>()));
                    HashMap<String, Post> posts = new HashMap<>();
                    posts.put(p1.getPostId(), p1);
                    posts.put(p2.getPostId(), p2);
                    db = new MockDatabase(true, users, followMap, posts);
                    MockAuthentication authentication = new MockAuthentication(true, accounts, "password");
                    MockImageStorage storage = new MockImageStorage();
                    MockInternalStorage internalStorage = new MockInternalStorage(new HashMap<>());
                    MockNetwork network = new MockNetwork(true);
                    localDatabase = Room.inMemoryDatabaseBuilder(Kandle.getContext(), LocalDatabase.class).allowMainThreadQueries().build();
                    DependencyManager.setFreshTestDependencies(authentication, db, storage, internalStorage, network, localDatabase);
                    DependencyManager.getDatabaseSystem().createUser(u1);
                    DependencyManager.getDatabaseSystem().createUser(u2);
                    DependencyManager.getDatabaseSystem().follow(LoggedInUser.getInstance().getId(), u1.getId());
                    DependencyManager.getDatabaseSystem().follow(LoggedInUser.getInstance().getId(), u2.getId());
                }

            };

    @After
    public void clearCurrentUser() {
        LoggedInUser.clear();
        localDatabase.close();
    }

    @Before
    public void loadPostView() {
        onView(withId(R.id.drawer_layout)).check(matches(isClosed(Gravity.LEFT))).perform(DrawerActions.open());
        onView(withId(R.id.navigation_view)).perform(NavigationViewActions.navigateTo(R.id.following_posts));
        onView(withId(R.id.drawer_layout)).perform(DrawerActions.close());
    }

    @Test
    public void ChecksOnePostHasAnImageNotTheOther() {

        //2 posts should be displayed
        onView(withId(R.id.flPosts)).check(new FollowingPostsTest.RecyclerViewItemCountAssertion(2));

        onView(new RecyclerViewMatcher(R.id.flPosts)
                .atPositionOnView(1, R.id.postImageInPost))
                .check(matches(withTagValue(is(PostAdapter.POST_IMAGE))));

        onView(new RecyclerViewMatcher(R.id.flPosts)
                .atPositionOnView(0, R.id.postImageInPost))
                .check(matches(not(withTagValue(is(PostAdapter.POST_IMAGE)))));
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
    /*@Test
    public void ChecksOnePostHasAnImageNotTheOther() throws Throwable {

        //2 posts should be displayed
        onView(withId(R.id.rvPosts)).check(new YourPostsListTest.RecyclerViewItemCountAssertion(2));

        onView(withId(R.id.rvPosts)).perform(RecyclerViewActions.actionOnItemAtPosition(1,click()));
        onView(withId(R.id.postImage)).check(matches(withTagValue(is(YourPostListFragment.POST_IMAGE))));
        onView(withId(R.id.post_content)).perform(click());

        onView(withId(R.id.rvPosts)).perform(RecyclerViewActions.actionOnItemAtPosition(0,click()));
        onView(withId(R.id.postImage)).check(matches(not(withTagValue(is(YourPostListFragment.POST_IMAGE)))));
        onView(withId(R.id.post_content)).perform(click());
    }
*/
}
