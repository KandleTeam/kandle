package ch.epfl.sdp.kandle;

import android.view.Gravity;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;
import androidx.test.espresso.NoMatchingViewException;
import androidx.test.espresso.ViewAssertion;
import androidx.test.espresso.contrib.DrawerActions;
import androidx.test.espresso.contrib.NavigationViewActions;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.rule.ActivityTestRule;
import androidx.test.rule.GrantPermissionRule;

import org.junit.After;
import org.junit.Rule;
import org.junit.Test;

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
import ch.epfl.sdp.kandle.entities.user.LoggedInUser;
import ch.epfl.sdp.kandle.entities.user.User;
import ch.epfl.sdp.kandle.storage.caching.CachedFirestoreDatabase;
import ch.epfl.sdp.kandle.storage.room.LocalDatabase;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.contrib.DrawerMatchers.isClosed;
import static androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static ch.epfl.sdp.kandle.entities.post.Post.CLOSE_FOLLOWER;
import static ch.epfl.sdp.kandle.dependencies.DependencyManager.getDatabaseSystem;
import static ch.epfl.sdp.kandle.dependencies.DependencyManager.setFreshTestDependencies;
import static junit.framework.TestCase.assertEquals;

public class CloseFollowerFragmentTest {
    private Post p1;
    private Post p2;
    private Post p3;
    private Post p4;
    private Post p5;
    private User u1;
    private User u2;
    private User u3;
    private MockDatabase db;
    private LocalDatabase localDatabase;
    private LinkedList<String> user12;
    private LinkedList<String> loggedUserList;

    @Rule
    public ActivityTestRule<MainActivity> intentsRule =
            new ActivityTestRule<MainActivity>(MainActivity.class, true, true) {
                @Override
                protected void beforeActivityLaunched() {
                    LoggedInUser.init(new User("loggedInUserId","LoggedInUser","loggedInUser@kandle.ch","nickname","image"));
                    u1 = new User("u1Id","u1","u2@kandle.ch","u1","image1");
                    u2 = new User("u2Id","u2","u2@kandle.ch","u2","image2");
                    u3 = new User("u3Id","u3","u3@kandle.ch","u3",null);
                    p1 =  new Post("Hello", null, new Date(), "loggedInUserId", "post1Id");
                    p2 = new Post("There", "image1", new Date(), "loggedInUserId", "post2Id", CLOSE_FOLLOWER);
                    p3 =  new Post("Hello", null, new Date(), "u1Id", "post3Id");
                    p4 = new Post("There", "image1", new Date(), "u2Id", "post4Id", CLOSE_FOLLOWER);
                    p5 = new Post("Ther", "image3", new Date(), "u3Id", "post5Id", CLOSE_FOLLOWER);
                    LoggedInUser.getInstance().addPostId(p1.getPostId());
                    LoggedInUser.getInstance().addPostId(p2.getPostId());
                    u1.addPostId(p3.getPostId());
                    u2.addPostId(p4.getPostId());
                    HashMap<String, String> accounts = new HashMap<>();
                    accounts.put(u1.getEmail(), u1.getId());
                    accounts.put(u2.getEmail(), u2.getId());
                    accounts.put(LoggedInUser.getInstance().getEmail(), LoggedInUser.getInstance().getId());
                    accounts.put(u3.getEmail(), u3.getId());
                    HashMap<String,User> users = new HashMap<>();
                    HashMap<String, MockDatabase.Follow> followMap = new HashMap<>();
                    followMap.put(u1.getId(),new MockDatabase.Follow(new LinkedList<>(),new LinkedList<>()));
                    followMap.put(u2.getId(),new MockDatabase.Follow(new LinkedList<>(),new LinkedList<>(), new LinkedList<>()));
                    followMap.put(LoggedInUser.getInstance().getId(), new MockDatabase.Follow(new LinkedList<>(), new LinkedList<>(), new LinkedList<>()));
                    HashMap<String,Post> posts = new HashMap<>();
                    posts.put(p1.getPostId(),p1);
                    posts.put(p2.getPostId(),p2);
                    posts.put(p3.getPostId(), p3);
                    posts.put(p4.getPostId(), p4);
                    posts.put(p5.getPostId(), p5);
                    db = new MockDatabase(true, users, followMap, posts);
                    MockAuthentication authentication = new MockAuthentication(true, accounts, "password");
                    MockImageStorage storage = new MockImageStorage();
                    MockInternalStorage internalStorage = new MockInternalStorage(new HashMap<>());
                    MockNetwork network = new MockNetwork(true);
                    localDatabase = Room.inMemoryDatabaseBuilder(Kandle.getContext(), LocalDatabase.class).allowMainThreadQueries().build();
                    DependencyManager.setFreshTestDependencies(authentication, db, storage, internalStorage, network, localDatabase,  CachedFirestoreDatabase.getInstance());
                    getDatabaseSystem().createUser(u1);
                    getDatabaseSystem().createUser(u2);
                    getDatabaseSystem().createUser(u3);
                    getDatabaseSystem().follow(LoggedInUser.getInstance().getId(),u1.getId());
                    getDatabaseSystem().follow(LoggedInUser.getInstance().getId(), u2.getId());
                    getDatabaseSystem().follow(LoggedInUser.getInstance().getId(), u3.getId());
                    getDatabaseSystem().setCloseFollower(LoggedInUser.getInstance().getId(), u2.getId());
                }

            };
    @Rule
    public GrantPermissionRule permissionRule = GrantPermissionRule.grant(android.Manifest.permission.ACCESS_FINE_LOCATION);


    @After
    public void clearCurrentUser(){
        LoggedInUser.clear();
    }

    public void loadView(int view) {
        onView(withId(R.id.drawer_layout)).check(matches(isClosed(Gravity.LEFT))).perform(DrawerActions.open());
        onView(withId(R.id.navigation_view)).perform(NavigationViewActions.navigateTo(view));
        onView(withId(R.id.drawer_layout)).perform(DrawerActions.close());
    }

    @Test
    public void CheckPostP2IsCloseFollowersAndP1Not(){
        loadView(R.id.your_posts);
        onView(withId(R.id.rvPosts)).check(new RecyclerViewItemCountAssertion(2));
        onView(new RecyclerViewMatcher(R.id.rvPosts)
                .atPositionOnView(0, R.id.isPostForCloseFollowers)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)));
        onView(new RecyclerViewMatcher(R.id.rvPosts)
                .atPositionOnView(1, R.id.isPostForCloseFollowers)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)));
    }

    @Test
    public void CheckPostCloseFollowersOnOtherPosts(){
        loadView(R.id.following_posts);
        onView(withId(R.id.flPosts)).check(new RecyclerViewItemCountAssertion(2));

        //Like then unlike the oldest (already created in the mockdatabase)
        onView(new RecyclerViewMatcher(R.id.flPosts)
                .atPositionOnView(1, R.id.isPostForCloseFollowers)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)));
        onView(new RecyclerViewMatcher(R.id.flPosts)
                .atPositionOnView(0, R.id.isPostForCloseFollowers)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)));
        getDatabaseSystem().unsetCloseFollower(LoggedInUser.getInstance().getId(), u2.getId());
        loadView(R.id.following_posts);
        onView(withId(R.id.flPosts)).check(new RecyclerViewItemCountAssertion(1));

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
