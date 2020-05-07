package ch.epfl.sdp.kandle;

import android.Manifest;
import android.view.Gravity;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;
import androidx.test.espresso.NoMatchingViewException;
import androidx.test.espresso.ViewAssertion;
import androidx.test.espresso.contrib.DrawerActions;
import androidx.test.espresso.contrib.NavigationViewActions;
import androidx.test.rule.ActivityTestRule;
import androidx.test.rule.GrantPermissionRule;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.contrib.DrawerMatchers.isClosed;
import static androidx.test.espresso.contrib.RecyclerViewActions.scrollToPosition;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static ch.epfl.sdp.kandle.YourProfileFragmentTest.atPosition;
import static ch.epfl.sdp.kandle.dependencies.DependencyManager.getDatabaseSystem;
import static junit.framework.TestCase.assertEquals;


import org.junit.After;
import org.junit.Rule;
import org.junit.Test;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;

import ch.epfl.sdp.kandle.storage.room.LocalDatabase;
import ch.epfl.sdp.kandle.activity.MainActivity;
import ch.epfl.sdp.kandle.dependencies.DependencyManager;
import ch.epfl.sdp.kandle.dependencies.MockAuthentication;
import ch.epfl.sdp.kandle.dependencies.MockDatabase;
import ch.epfl.sdp.kandle.dependencies.MockInternalStorage;
import ch.epfl.sdp.kandle.dependencies.MockNetwork;
import ch.epfl.sdp.kandle.dependencies.MockStorage;


public class AchievementTest {
    private  User user1;
    private  User user2;
    private User user3;
    private Post p1;
    private Post p2;
    private Post p3;
    private Post p4;
    private Post p5;
    MockDatabase db;

    private LocalDatabase localDatabase;

    @Rule
    public GrantPermissionRule permissionRule = GrantPermissionRule.grant(android.Manifest.permission.ACCESS_FINE_LOCATION);

    public GrantPermissionRule grantLocation = GrantPermissionRule.grant(Manifest.permission.ACCESS_FINE_LOCATION);

    @Rule
    public ActivityTestRule<MainActivity> intentsRule =
            new ActivityTestRule<MainActivity>(MainActivity.class,true,true){
                @Override
                protected void beforeActivityLaunched() {
                    LoggedInUser.init(new User("loggedInUserId","LoggedInUser","loggedInUser@kandle.ch","nickname","image"));
                    user1 = new User("user1Id", "user1", "user1@kandle.ch", null,  null);
                    user2 = new User("user2Id", "user2", "user2@kandle.ch", null,  null);
                    user3 = new User("user3Id", "user3", "user3@kandle.ch", null,  null);
                    p1 =  new Post("Hello", null, new Date(), "loggedInUserId", "post1Id");
                    p2 = new Post("There", "null", new Date(), "loggedInUserId", "post2Id");
                    p3 =  new Post("My", null, new Date(), "loggedInUserId", "post3Id");
                    p4 =  new Post("You", null, new Date(), "loggedInUserId", "post4Id");
                    p5 =  new Post("Are", null, new Date(), "loggedInUserId", "post5Id");
                    LoggedInUser.getInstance().addPostId(p1.getPostId());
                    LoggedInUser.getInstance().addPostId(p2.getPostId());
                    LoggedInUser.getInstance().addPostId(p3.getPostId());
                    LoggedInUser.getInstance().addPostId(p4.getPostId());
                    HashMap<String,String> accounts = new HashMap<>();
                    accounts.put(user1.getEmail(),user1.getId());
                    accounts.put(user2.getEmail(),user2.getId());
                    accounts.put(user3.getEmail(),user3.getId());
                    HashMap<String,User> users = new HashMap<>();
                    HashMap<String, MockDatabase.Follow> followMap = new HashMap<>();
                    followMap.put(user1.getId(),new MockDatabase.Follow(new LinkedList<>(),new LinkedList<>()));
                    followMap.put(user2.getId(),new MockDatabase.Follow(new LinkedList<>(),new LinkedList<>()));
                    followMap.put(user3.getId(),new MockDatabase.Follow(new LinkedList<>(),new LinkedList<>()));
                    HashMap<String, Post> posts = new HashMap<>();
                    posts.put(p1.getPostId(),p1);
                    posts.put(p2.getPostId(),p2);
                    posts.put(p3.getPostId(),p3);
                    posts.put(p4.getPostId(),p4);
                     db = new MockDatabase(true, users, followMap, posts);
                    MockAuthentication authentication = new MockAuthentication(true, accounts, "password");
                    MockStorage storage = new MockStorage();
                    MockInternalStorage internalStorage = new MockInternalStorage();
                    MockNetwork network = new MockNetwork(true);
                    localDatabase = Room.inMemoryDatabaseBuilder(Kandle.getContext(), LocalDatabase.class).allowMainThreadQueries().build();
                    DependencyManager.setFreshTestDependencies(authentication, db, storage,internalStorage,network,localDatabase);
                    getDatabaseSystem().createUser(user1);
                    getDatabaseSystem().createUser(user2);
                    getDatabaseSystem().createUser(user3);
                    getDatabaseSystem().follow(user1.getId(), LoggedInUser.getInstance().getId());
                    getDatabaseSystem().follow(user2.getId(),LoggedInUser.getInstance().getId());
                    getDatabaseSystem().follow(LoggedInUser.getInstance().getId(), user1.getId());
                    getDatabaseSystem().follow(LoggedInUser.getInstance().getId(), user2.getId());
                    getDatabaseSystem().likePost(user1.getId(), p1.getPostId());
                    getDatabaseSystem().likePost(user2.getId(), p1.getPostId());
                    getDatabaseSystem().likePost(user1.getId(), p2.getPostId());
                    getDatabaseSystem().likePost(user2.getId(), p2.getPostId());
                }
            };




    @After
    public void clearCurrentUser(){
        LoggedInUser.clear();
        localDatabase.close();
    }


    @Test
    public void allTypesOfAchievementsNotDone(){
        setFragment();
        onView(withId(R.id.flAchievements)).check(new AchievementTest.RecyclerViewItemCountAssertion(11));
        //onView(withId(R.id.flPosts)).perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));
        onView(withId(R.id.flAchievements)).check(matches(atPosition(0, hasDescendant(withText("Still Not Completed !")))));
        onView(withId(R.id.flAchievements)).check(matches(atPosition(3, hasDescendant(withText("Still Not Completed !")))));
        onView(withId(R.id.flAchievements)).perform(scrollToPosition(5)).check(matches(atPosition(5, hasDescendant(withText("Still Not Completed !")))));
        onView(withId(R.id.flAchievements)).perform(scrollToPosition(7)).check(matches(atPosition(7, hasDescendant(withText("Still Not Completed !")))));
        onView(withId(R.id.flAchievements)).perform(scrollToPosition(10)).check(matches(atPosition(10, hasDescendant(withText("Still Not Completed !")))));
    }

    @Test
    public void AchievementFollowingWorks() {
        getDatabaseSystem().follow(LoggedInUser.getInstance().getId(), user3.getId());
        setFragment();
        onView(withId(R.id.flAchievements)).check(matches(atPosition(3, hasDescendant(withText("Achievement Completed !")))));
    }

    @Test
    public void AchievementFollowersWorks() {
        getDatabaseSystem().follow(user3.getId(), LoggedInUser.getInstance().getId());
        setFragment();
        onView(withId(R.id.flAchievements)).perform(scrollToPosition(5)).check(matches(atPosition(5, hasDescendant(withText("Achievement Completed !")))));
    }

    @Test
    public void AchievementNumberPostsWorks() {
        getDatabaseSystem().addPost(p5);
        setFragment();
        onView(withId(R.id.flAchievements)).check(matches(atPosition(0, hasDescendant(withText("Achievement Completed !")))));
    }

    @Test
    public void AchievementLikesInPostWorks() {
        getDatabaseSystem().likePost(user3.getId(), p1.getPostId());
        setFragment();
        onView(withId(R.id.flAchievements)).perform(scrollToPosition(7)).check(matches(atPosition(7, hasDescendant(withText("Achievement Completed !")))));
    }

    @Test
    public void AchievementLikesAllPostWorks() {
        getDatabaseSystem().likePost(user3.getId(), p2.getPostId());
        setFragment();
        onView(withId(R.id.flAchievements)).perform(scrollToPosition(9)).check(matches(atPosition(9, hasDescendant(withText("Achievement Completed !")))));
    }

    private void setFragment(){
        onView(withId(R.id.drawer_layout)).check(matches(isClosed(Gravity.LEFT))).perform(DrawerActions.open());
        onView(withId(R.id.navigation_view)).perform(NavigationViewActions.navigateTo(R.id.achievements));
        onView(withId(R.id.drawer_layout)).perform(DrawerActions.close());
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