package ch.epfl.sdp.kandle;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Intent;
import android.net.Uri;
import android.view.Gravity;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.NoMatchingViewException;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.ViewAssertion;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.contrib.DrawerActions;
import androidx.test.espresso.contrib.NavigationViewActions;
import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.espresso.intent.rule.IntentsTestRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ActivityTestRule;

import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import androidx.test.rule.GrantPermissionRule;

import net.bytebuddy.asm.Advice;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;

import ch.epfl.sdp.kandle.activity.PostActivity;
import java.util.List;

import ch.epfl.sdp.kandle.Storage.room.LocalDatabase;
import ch.epfl.sdp.kandle.activity.MainActivity;
import ch.epfl.sdp.kandle.dependencies.DependencyManager;
import ch.epfl.sdp.kandle.dependencies.MockAuthentication;
import ch.epfl.sdp.kandle.dependencies.MockDatabase;
import ch.epfl.sdp.kandle.dependencies.MockInternalStorage;
import ch.epfl.sdp.kandle.dependencies.MockNetwork;
import ch.epfl.sdp.kandle.dependencies.MockStorage;
import ch.epfl.sdp.kandle.exceptions.NoInternetException;
import ch.epfl.sdp.kandle.fragment.YourPostListFragment;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.longClick;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.contrib.DrawerMatchers.isClosed;
import static androidx.test.espresso.intent.Intents.intending;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasAction;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.RootMatchers.withDecorView;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withTagValue;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static junit.framework.TestCase.assertEquals;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.emptyArray;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

@RunWith(AndroidJUnit4.class)
public class YourPostsListTest {
    private User user1, user2;
    public static Post p1;
    public static Post p2;
    private LocalDatabase localDatabase;
    private MockNetwork network;
    @Rule
    public IntentsTestRule<MainActivity> intentsRule =
            new IntentsTestRule<MainActivity>(MainActivity.class, true, true) {
                @Override
                protected void beforeActivityLaunched() {
                    user1 = new User("user1Id", "user1", "user1@kandle.ch", "user1", null);
                    user2 = new User("user2Id", "user2", "user2@kandle.ch", "user2", null);
                    LoggedInUser.init(new User("loggedInUserId", "LoggedInUser", "loggedInUser@kandle.ch", "nickname", "image"));
                    p1 = new Post("Hello", null, new Date(), LoggedInUser.getInstance().getId(), "post1Id");
                    p2 = new Post("There", "image", new Date(), LoggedInUser.getInstance().getId(), "post2Id");
                    ArrayList<String> likers = new ArrayList<>();
                    likers.add(user1.getId());
                    likers.add(user2.getId());
                    p1.setLikers(likers);
                    p2.setLikers(likers);
                    HashMap<String, String> accounts = new HashMap<>();
                    HashMap<String, User> users = new HashMap<>();
                    HashMap<String, MockDatabase.Follow> followMap = new HashMap<>();
                    HashMap<String, Post> posts = new HashMap<>();
                    posts.put(p1.getPostId(), p1);
                    posts.put(p2.getPostId(), p2);
                    users.put(user2.getId(), user2);
                    users.put(user1.getId(), user1);
                    MockDatabase db = new MockDatabase(true, users, followMap, posts);
                    MockAuthentication authentication = new MockAuthentication(true, accounts, "password");
                    MockStorage storage = new MockStorage();
                    MockInternalStorage internalStorage = new MockInternalStorage();
                    network = new MockNetwork(true);
                    localDatabase = Room.inMemoryDatabaseBuilder(Kandle.getContext(), LocalDatabase.class).allowMainThreadQueries().build();
                    localDatabase.postDao().insertPost(p1);
                    localDatabase.userDao().insertUser(LoggedInUser.getInstance());
                    localDatabase.userDao().insertUser(user1);
                    DependencyManager.setFreshTestDependencies(authentication, db, storage, internalStorage, network, localDatabase);

                }
            };

    @Rule
    public GrantPermissionRule permissionRule = GrantPermissionRule.grant(android.Manifest.permission.ACCESS_FINE_LOCATION);


    @After
    public void clearCurrentUserAndLocalDb() {
        LoggedInUser.clear();
        localDatabase.close();
    }

    @Before
    public void loadPostView() {
        onView(withId(R.id.drawer_layout)).check(matches(isClosed(Gravity.LEFT))).perform(DrawerActions.open());
        onView(withId(R.id.navigation_view)).perform(NavigationViewActions.navigateTo(R.id.your_posts));
        onView(withId(R.id.drawer_layout)).perform(DrawerActions.close());
    }


    @Test
    public void canClickOnAlreadyCreatedPostToSeeDescriptionAndRemoveDescription() {
        onView(withId(R.id.rvPosts)).perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));
        onView(withId(R.id.post_content)).perform(click());
        onView(withId(R.id.rvPosts)).perform(RecyclerViewActions.actionOnItemAtPosition(1, click()));
        onView(withId(R.id.post_content)).perform(click());

    }


    @Test
    public void likesThenUnlikesAlreadyCreatedPostsAndDeleteOne() throws InterruptedException {

        //2 posts should be displayed
        onView(withId(R.id.rvPosts)).check(new RecyclerViewItemCountAssertion(2));
        //Like then unlike the oldest (already created in the mockdatabase)
        onView(withId(R.id.rvPosts)).perform(RecyclerViewActions.actionOnItemAtPosition(0, clickChildViewWithId(R.id.likeButton)));
        onView(withId(R.id.rvPosts)).perform(RecyclerViewActions.actionOnItemAtPosition(1, clickChildViewWithId(R.id.likeButton)));
        onView(withId(R.id.rvPosts)).perform(RecyclerViewActions.actionOnItemAtPosition(0, clickChildViewWithId(R.id.likeButton)));
        onView(withId(R.id.rvPosts)).perform(RecyclerViewActions.actionOnItemAtPosition(1, clickChildViewWithId(R.id.likeButton)));
        onView(withId(R.id.rvPosts)).perform(RecyclerViewActions.actionOnItemAtPosition(1, clickChildViewWithId(R.id.deleteButton)));
        Thread.sleep(1000);
        onView(withId(android.R.id.button1)).perform(click());
        onView(withId(R.id.rvPosts)).check(new RecyclerViewItemCountAssertion(1));


    }

    @Test
    public void getYourPostFromTheLocalDbIfYouAreOffline() {


        //2 posts should be displayed
        onView(withId(R.id.rvPosts)).check(new RecyclerViewItemCountAssertion(2));
        network.setIsOnline(false);
        onView(withId(R.id.drawer_layout)).check(matches(isClosed(Gravity.LEFT))).perform(DrawerActions.open());
        onView(withId(R.id.navigation_view)).perform(NavigationViewActions.navigateTo(R.id.your_posts));
        onView(withId(R.id.drawer_layout)).perform(DrawerActions.close());
        //Like then unlike the oldest (already created in the mockdatabase)
        onView(withId(R.id.rvPosts)).check(new RecyclerViewItemCountAssertion(2));


    }

    @Test
    public void getErrorIfNoPostInLocalDbAndOffline() {


        onView(withId(R.id.rvPosts)).check(new RecyclerViewItemCountAssertion(2));
        network.setIsOnline(false);
        localDatabase.clearAllTables();
        onView(withId(R.id.drawer_layout)).check(matches(isClosed(Gravity.LEFT))).perform(DrawerActions.open());
        onView(withId(R.id.navigation_view)).perform(NavigationViewActions.navigateTo(R.id.your_posts));
        onView(withId(R.id.drawer_layout)).perform(DrawerActions.close());

        onView(withId(R.id.rvPosts)).check(new RecyclerViewItemCountAssertion(0));


    }

    @Test
    public void createTwoNewPostsAndRemoveThem() throws InterruptedException {

        // 2 posts should be displayed
        onView(withId(R.id.rvPosts)).check(new RecyclerViewItemCountAssertion(2));


        // Move back to map
        onView(withId(R.id.drawer_layout)).check(matches(isClosed(Gravity.LEFT))).perform(DrawerActions.open());
        onView(withId(R.id.navigation_view)).perform(NavigationViewActions.navigateTo(R.id.map_support));
        onView(withId(R.id.drawer_layout)).perform(DrawerActions.close());

        // Create two new posts
        onView(withId(R.id.newPostButton)).perform(ViewActions.click());
        onView(withId(R.id.postText)).perform(typeText("Post 3"));
        onView(withId(R.id.postText)).perform(closeSoftKeyboard());
        onView(withId(R.id.postButton)).perform(click());

        Thread.sleep(1000);
        onView(withId(R.id.newPostButton)).perform(click());
        onView(withId(R.id.postText)).perform(typeText("Post 4"));
        onView(withId(R.id.postText)).perform(closeSoftKeyboard());
        onView(withId(R.id.postButton)).perform(click());

        loadPostView();
        //4 posts should be displayed
        onView(withId(R.id.rvPosts)).check(new RecyclerViewItemCountAssertion(4));

        onView(withId(R.id.rvPosts)).perform(RecyclerViewActions.actionOnItemAtPosition(0, clickChildViewWithId(R.id.deleteButton)));
        onView(withId(android.R.id.button1)).perform(click());


    }

    @Test
    public void checksOnePostHasAnImageNotTheOther() {

        //2 posts should be displayed
        onView(withId(R.id.rvPosts)).check(new RecyclerViewItemCountAssertion(2));

        onView(withId(R.id.rvPosts)).perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));
        onView(withId(R.id.postImage)).check(matches(withTagValue(is(YourPostListFragment.POST_IMAGE))));
        onView(withId(R.id.post_content)).perform(click());

        onView(withId(R.id.rvPosts)).perform(RecyclerViewActions.actionOnItemAtPosition(1, click()));
        onView(withId(R.id.postImage)).check(matches(not(withTagValue(is(YourPostListFragment.POST_IMAGE)))));
        onView(withId(R.id.post_content)).perform(click());


    }


    @Test
    public void likesListInteractionTestWithNoUserStoredLocally() {

        onView(withId(R.id.rvPosts)).perform(RecyclerViewActions.actionOnItemAtPosition(1, clickChildViewWithId(R.id.flames)));
        onView(withId(R.id.list_user_number)).check(matches(withText("2")));
        Espresso.pressBack();
        onView(withId(R.id.rvPosts)).perform(RecyclerViewActions.actionOnItemAtPosition(1, clickChildViewWithId(R.id.likeButton)));
        onView(withId(R.id.rvPosts)).perform(RecyclerViewActions.actionOnItemAtPosition(1, clickChildViewWithId(R.id.flames)));
        onView(withId(R.id.list_user_number)).check(matches(withText("3")));


    }

    @Test
    public void likesListInteractionTestWithUserStoredLocallyButStillOnlinePhone() {

        onView(withId(R.id.rvPosts)).perform(RecyclerViewActions.actionOnItemAtPosition(0, clickChildViewWithId(R.id.flames)));
        onView(withId(R.id.list_user_number)).check(matches(withText("2")));
        Espresso.pressBack();


    }

    @Test
    public void likesListInteractionTestWithNoUserStoredLocallyButOnlinePhone() {
        localDatabase.userDao().deleteUser(user1);
        onView(withId(R.id.drawer_layout)).check(matches(isClosed(Gravity.LEFT))).perform(DrawerActions.open());
        onView(withId(R.id.navigation_view)).perform(NavigationViewActions.navigateTo(R.id.your_posts));
        onView(withId(R.id.drawer_layout)).perform(DrawerActions.close());
        onView(withId(R.id.rvPosts)).perform(RecyclerViewActions.actionOnItemAtPosition(0, clickChildViewWithId(R.id.flames)));
        onView(withId(R.id.list_user_number)).check(matches(withText("2")));
        //Espresso.pressBack();


    }

    @Test
    public void likesListInteractionTestWithNoUserStoredLocallyButOfflinePhone() {
        localDatabase.userDao().deleteUser(user1);
        network.setIsOnline(false);
        onView(withId(R.id.rvPosts)).perform(RecyclerViewActions.actionOnItemAtPosition(0, clickChildViewWithId(R.id.flames)));
        onView(withText(R.string.no_connexion)).inRoot(withDecorView(not(is(intentsRule.getActivity().getWindow().getDecorView())))).check(matches(isDisplayed()));

    }

    @Test
    public void likesListInteractionTestWithOnylOneUserStoredLocallyButOfflinePhone() {
        network.setIsOnline(false);
        onView(withId(R.id.rvPosts)).perform(RecyclerViewActions.actionOnItemAtPosition(0, clickChildViewWithId(R.id.flames)));
        onView(withText(R.string.incomplete_data)).inRoot(withDecorView(not(is(intentsRule.getActivity().getWindow().getDecorView())))).check(matches(isDisplayed()));

    }

    @Test
    public void likesListFromNotStoredPostLocallyAndOfflineApp() {
        network.setIsOnline(false);
        localDatabase.userDao().deleteUser(user1);
        onView(withId(R.id.drawer_layout)).check(matches(isClosed(Gravity.LEFT))).perform(DrawerActions.open());
        onView(withId(R.id.navigation_view)).perform(NavigationViewActions.navigateTo(R.id.your_posts));
        onView(withId(R.id.drawer_layout)).perform(DrawerActions.close());
        onView(withId(R.id.rvPosts)).perform(RecyclerViewActions.actionOnItemAtPosition(1, clickChildViewWithId(R.id.flames)));
        onView(withText(R.string.no_connexion)).inRoot(withDecorView(not(is(intentsRule.getActivity().getWindow().getDecorView())))).check(matches(isDisplayed()));

    }

    @Test
    public void likesListFromNotStoredPostLocallyAndOnlineApp() {
        network.setIsOnline(false);
        onView(withId(R.id.rvPosts)).perform(RecyclerViewActions.actionOnItemAtPosition(1, clickChildViewWithId(R.id.flames)));

    }





    @Test
    public void EditPostImageTest(){
        onView(withId(R.id.rvPosts)).perform(RecyclerViewActions.actionOnItemAtPosition(1, clickChildViewWithId(R.id.editButton)));

        Intent resultData = new Intent();
        resultData.setAction(Intent.ACTION_GET_CONTENT);
        Uri imageUri = Uri.parse("android.resource://ch.epfl.sdp.kandle/drawable/ic_launcher_background.xml");
        resultData.setData(imageUri);
        Instrumentation.ActivityResult result = new Instrumentation.ActivityResult(Activity.RESULT_OK, resultData);
        intending(hasAction(Intent.ACTION_GET_CONTENT)).respondWith(result);
        onView(withId(R.id.galleryButton)).perform(click());
        onView(withId(R.id.postImage)).check(matches(withTagValue(is(PostActivity.POST_IMAGE_TAG))));
        onView(withId(R.id.postButton)).perform(click());

        loadPostView();

        onView(withId(R.id.rvPosts)).perform(RecyclerViewActions.actionOnItemAtPosition(1, click()));
        onView(withId(R.id.postImage)).check(matches(withTagValue(is(YourPostListFragment.POST_IMAGE))));
        onView(withId(R.id.post_content)).perform(click());
    }

    @Test
    public void EditPostTextTest() throws InterruptedException {
        onView(withId(R.id.rvPosts)).perform(RecyclerViewActions.actionOnItemAtPosition(0, clickChildViewWithId(R.id.editButton)));
        Thread.sleep(1000);
        onView(withId(R.id.postText)).perform(replaceText("   Salut Salut  "));
        onView(withId (R.id.postText)).perform(closeSoftKeyboard());
        onView(withId(R.id.postButton)).perform(click());

        loadPostView();

        onView(withId(R.id.rvPosts)).perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));
        onView(withId(R.id.post_content)).check(matches(withText(is("Salut Salut"))));
        onView(withId(R.id.post_content)).perform(click());

    }



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