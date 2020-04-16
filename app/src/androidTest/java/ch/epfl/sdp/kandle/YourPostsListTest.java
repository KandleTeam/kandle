package ch.epfl.sdp.kandle;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Intent;
import android.net.Uri;
import android.view.Gravity;
import android.view.View;
import androidx.recyclerview.widget.RecyclerView;
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
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import androidx.test.rule.GrantPermissionRule;
import java.util.Date;
import java.util.HashMap;

import ch.epfl.sdp.kandle.activity.PostActivity;
import ch.epfl.sdp.kandle.dependencies.DependencyManager;
import ch.epfl.sdp.kandle.dependencies.MockAuthentication;
import ch.epfl.sdp.kandle.dependencies.MockDatabase;
import ch.epfl.sdp.kandle.dependencies.MockInternalStorage;
import ch.epfl.sdp.kandle.dependencies.MockNetwork;
import ch.epfl.sdp.kandle.dependencies.MockStorage;
import ch.epfl.sdp.kandle.dependencies.Post;
import ch.epfl.sdp.kandle.fragment.YourPostListFragment;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.contrib.DrawerMatchers.isClosed;
import static androidx.test.espresso.intent.Intents.intending;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasAction;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withTagValue;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static junit.framework.TestCase.assertEquals;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

@RunWith(AndroidJUnit4.class)
public class YourPostsListTest {

    public static Post p1;
    public static Post p2;

    @Rule
    public IntentsTestRule<MainActivity> intentsRule =
            new IntentsTestRule<MainActivity>(MainActivity.class, true, true){
                @Override
                protected void beforeActivityLaunched() {
                    LoggedInUser.init(new User("loggedInUserId","LoggedInUser","loggedInUser@kandle.ch","nickname","image"));
                    p1 =  new Post("Hello", null, new Date(), LoggedInUser.getInstance().getId(), "post1Id");
                    p2 = new Post("There", "image", new Date(), LoggedInUser.getInstance().getId(), "post2Id");
                    LoggedInUser.getInstance().addPostId(p1.getPostId());
                    LoggedInUser.getInstance().addPostId(p2.getPostId());
                    HashMap<String, String> accounts = new HashMap<>();
                    HashMap<String,User> users = new HashMap<>();
                    HashMap<String, MockDatabase.Follow> followMap = new HashMap<>();
                    HashMap<String,Post> posts = new HashMap<>();
                    posts.put(p1.getPostId(),p1);
                    posts.put(p2.getPostId(),p2);
                    MockDatabase db = new MockDatabase(true, users, followMap, posts);
                    MockAuthentication authentication = new MockAuthentication(true, accounts, "password");
                    MockStorage storage = new MockStorage();
                    MockInternalStorage internalStorage = new MockInternalStorage();
                    MockNetwork network = new MockNetwork(true);
                    DependencyManager.setFreshTestDependencies(authentication, db, storage,internalStorage,network);

                }

            };

    @Rule
    public GrantPermissionRule permissionRule = GrantPermissionRule.grant(android.Manifest.permission.ACCESS_FINE_LOCATION);

    @After
    public void clearCurrentUser(){
        LoggedInUser.clear();
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
    public void likesThenUnlikesAlreadyCreatedPosts(){

        //2 posts should be displayed
        onView(withId(R.id.rvPosts)).check(new RecyclerViewItemCountAssertion(2));
        //Like then unlike the oldest (already created in the mockdatabase)
        //TODO When we like here the like counter is 2 and not 1 therefor we need to check and fix the issue
        onView(withId(R.id.rvPosts)).perform(RecyclerViewActions.actionOnItemAtPosition(0, clickChildViewWithId(R.id.likeButton)));
        onView(withId(R.id.rvPosts)).perform(RecyclerViewActions.actionOnItemAtPosition(1, clickChildViewWithId(R.id.likeButton)));
        onView(withId(R.id.rvPosts)).perform(RecyclerViewActions.actionOnItemAtPosition(0, clickChildViewWithId(R.id.likeButton)));
        onView(withId(R.id.rvPosts)).perform(RecyclerViewActions.actionOnItemAtPosition(1, clickChildViewWithId(R.id.likeButton)));

        //Remove the the oldest post
        //onView(withId(R.id.rvPosts)).perform(RecyclerViewActions.actionOnItemAtPosition(1, clickChildViewWithId(R.id.deleteButton)));
        //onView(withId(android.R.id.button1)).perform(click());

        //only 1 post should be displayed
        //onView(withId(R.id.rvPosts)).check(new RecyclerViewItemCountAssertion(1));

    }

    @Test
    public void createTwoNewPostsAndRemoveThem() {

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

        onView(withId(R.id.newPostButton)).perform(click());
        onView(withId(R.id.postText)).perform(typeText("Post 4"));
        onView(withId(R.id.postText)).perform(closeSoftKeyboard());
        onView(withId(R.id.postButton)).perform(click());

        loadPostView();
        //4 posts should be displayed
        onView(withId(R.id.rvPosts)).check(new RecyclerViewItemCountAssertion(4));

        onView(withId(R.id.rvPosts)).perform(RecyclerViewActions.actionOnItemAtPosition(0,clickChildViewWithId(R.id.deleteButton)));
        onView(withId(android.R.id.button1)).perform(click());
        onView(withId(R.id.drawer_layout)).check(matches(isClosed(Gravity.LEFT))).perform(DrawerActions.open());
        onView(withId(R.id.navigation_view)).perform(NavigationViewActions.navigateTo(R.id.about));

    }

    @Test
    public void ChecksOnePostHasAnImageNotTheOther() throws Throwable {

        //2 posts should be displayed
        onView(withId(R.id.rvPosts)).check(new RecyclerViewItemCountAssertion(2));

        onView(withId(R.id.rvPosts)).perform(RecyclerViewActions.actionOnItemAtPosition(1,click()));
        onView(withId(R.id.postImage)).check(matches(withTagValue(is(YourPostListFragment.POST_IMAGE))));
        onView(withId(R.id.post_content)).perform(click());

        onView(withId(R.id.rvPosts)).perform(RecyclerViewActions.actionOnItemAtPosition(0,click()));
        onView(withId(R.id.postImage)).check(matches(not(withTagValue(is(YourPostListFragment.POST_IMAGE)))));
        onView(withId(R.id.post_content)).perform(click());
    }


    @Test
    public void LikesListInteractionTest(){

        onView(withId(R.id.rvPosts)).perform(RecyclerViewActions.actionOnItemAtPosition(1, clickChildViewWithId(R.id.flames)));
        onView(withId(R.id.list_user_number)).check(matches(withText("0")));
        Espresso.pressBack();
        onView(withId(R.id.rvPosts)).perform(RecyclerViewActions.actionOnItemAtPosition(1, clickChildViewWithId(R.id.likeButton)));
        onView(withId(R.id.rvPosts)).perform(RecyclerViewActions.actionOnItemAtPosition(1, clickChildViewWithId(R.id.flames)));
        onView(withId(R.id.list_user_number)).check(matches(withText("1")));
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
    public void EditPostTextTest(){
        onView(withId(R.id.rvPosts)).perform(RecyclerViewActions.actionOnItemAtPosition(0, clickChildViewWithId(R.id.editButton)));

        onView(withId(R.id.postText)).perform(replaceText("   Salut Salut  "));
        onView(withId (R.id.postText)).perform(closeSoftKeyboard());
        onView(withId(R.id.postButton)).perform(click());

        loadPostView();

        onView(withId(R.id.rvPosts)).perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));
        onView(withId(R.id.post_content)).check(matches(withText(is("Salut Salut"))));
        onView(withId(R.id.post_content)).perform(click());

    }

    @Test
    public void EditPostWithoutImageTest(){

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