package ch.epfl.sdp.kandle;


import android.location.Location;

import androidx.room.Room;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.rule.ActivityTestRule;
import androidx.test.rule.GrantPermissionRule;
import androidx.test.uiautomator.By;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.Until;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import ch.epfl.sdp.kandle.storage.room.LocalDatabase;
import ch.epfl.sdp.kandle.activity.MainActivity;
import ch.epfl.sdp.kandle.dependencies.DependencyManager;
import ch.epfl.sdp.kandle.dependencies.MockAuthentication;
import ch.epfl.sdp.kandle.dependencies.MockDatabase;
import ch.epfl.sdp.kandle.dependencies.MockInternalStorage;
import ch.epfl.sdp.kandle.dependencies.MockNetwork;
import ch.epfl.sdp.kandle.dependencies.MockStorage;
import ch.epfl.sdp.kandle.fragment.MapViewFragment;
import ch.epfl.sdp.kandle.fragment.PostFragment;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withAlpha;
import static androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withTagValue;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

public class MapViewFragmentTest {

    @Rule
    public GrantPermissionRule permissionRule = GrantPermissionRule.grant(android.Manifest.permission.ACCESS_FINE_LOCATION);
    private UiDevice uiDevice;
    private LocalDatabase localDatabase;
    @Rule
    public ActivityTestRule<MainActivity> intentsRule =
            new ActivityTestRule<MainActivity>(MainActivity.class, true, true
            ) {
                @Override
                protected void beforeActivityLaunched() {
                    User user1 = new User("user1Id", "user1", "user1@kandle.ch", "user1nickname", null);
                    LoggedInUser.init(new User("loggedInUserId", "LoggedInUser", "loggedInUser@kandle.ch", "nickname", "image"));
                    HashMap<String, String> accounts = new HashMap<>();
                    accounts.put(user1.getEmail(), user1.getId());
                    HashMap<String, User> users = new HashMap<>();
                    HashMap<String, MockDatabase.Follow> followMap = new HashMap<>();
                    HashMap<String, Post> posts = new HashMap<>();
                    Post closePost = new Post("closePostDesciption", null, new Date(), LoggedInUser.getInstance().getId(), "closePostId");
                    closePost.setLatitude(0.00015);
                    closePost.setLongitude(0.00015);
                    Post farPost = new Post("farPostDesciption", "image", new Date(), "user1Id", "farPostId");

                    farPost.setLatitude(0.0015);
                    farPost.setLongitude(0.0015);
                    Calendar cal = Calendar.getInstance();
                    cal.set(2099, 12, 31, 12, 59);
                    Post event = new Post("eventDescription", "image", cal.getTime(), "user1Id", "eventId");
                    event.setType(Post.EVENT);
                    event.setLatitude(0.0015);
                    event.setLongitude(0.0015);
                    LoggedInUser.getInstance().addPostId(closePost.getPostId());
                    user1.addPostId(farPost.getPostId());
                    user1.addPostId(event.getPostId());
                    posts.put(closePost.getPostId(), closePost);
                    posts.put(farPost.getPostId(), farPost);
                    posts.put(event.getPostId(), event);
                    MockDatabase db = new MockDatabase(true, users, followMap, posts);
                    MockAuthentication authentication = new MockAuthentication(true, accounts, "password");
                    MockStorage storage = new MockStorage();
                    MockInternalStorage internalStorage = new MockInternalStorage();
                    MockNetwork network = new MockNetwork(true);
                    localDatabase = Room.inMemoryDatabaseBuilder(Kandle.getContext(), LocalDatabase.class).allowMainThreadQueries().build();
                    DependencyManager.setFreshTestDependencies(authentication, db, storage, internalStorage, network, localDatabase);
                    DependencyManager.getDatabaseSystem().createUser(user1);
                }
            };

    @Before
    public void initUiDevice() {
        uiDevice = UiDevice.getInstance(getInstrumentation());
    }

    @After
    public void clearCurrentUser() {
        LoggedInUser.clear();
    }

    @Test
    public void clickOnClosePost() throws Throwable {
        uiDevice.wait(Until.hasObject(By.desc("MAP READY")), 2000);
        Thread.sleep(2000);
        ((MapViewFragment) intentsRule.getActivity().getCurrentFragment()).goToPostFragment("closePostId", new Location("mock"));

        onView(withId(R.id.postFragmentProfilePicture)).check(matches(withTagValue(is(PostFragment.PROFILE_PICTURE_IMAGE))));
        onView(withId(R.id.postFragmentPostImage)).check(matches(not(withTagValue(is(PostFragment.POST_IMAGE)))));

        onView(withId(R.id.postFragmentFollowButton)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)));

        onView(withId(R.id.postFragmentNumberOfLikes)).check(matches(withText("0")));
        onView(withId(R.id.postFragmentLikeButton)).perform(click());
        onView(withId(R.id.postFragmentNumberOfLikes)).check(matches(withText("1")));
        onView(withId(R.id.postFragmentLikeButton)).perform(click());
        onView(withId(R.id.postFragmentNumberOfLikes)).check(matches(withText("0")));
    }

    @Test
    public void clickOnFarPost() throws Throwable {
        uiDevice.wait(Until.hasObject(By.desc("MAP READY")), 2000);
        Thread.sleep(2000);
        ((MapViewFragment) intentsRule.getActivity().getCurrentFragment()).goToPostFragment("farPostId", new Location("mock"));

        onView(withId(R.id.postFragmentPostImage)).check(matches(withTagValue(is(PostFragment.POST_IMAGE))));
        onView(withId(R.id.postFragmentProfilePicture)).check(matches(not(withTagValue(is(PostFragment.PROFILE_PICTURE_IMAGE)))));

        onView(withId(R.id.postFragmentFollowButton)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)));
        onView(withId(R.id.postFragmentFollowButton)).perform(click());
        onView(withId(R.id.postFragmentFollowButton)).perform(click());

        onView(withId(R.id.postFragmentLikeButton)).check(matches(withAlpha((float) 0.5)));
        onView(withId(R.id.postFragmentNumberOfLikes)).check(matches(withText("0")));
        onView(withId(R.id.postFragmentLikeButton)).perform(click());
        onView(withId(R.id.postFragmentNumberOfLikes)).check(matches(withText("0")));

    }

    @Test
    public void clickOnEvent() throws Throwable {
        uiDevice.wait(Until.hasObject(By.desc("MAP READY")), 2000);
        Thread.sleep(2000);
        ((MapViewFragment) intentsRule.getActivity().getCurrentFragment()).goToPostFragment("eventId", new Location("mock"));
        onView(withId(R.id.postFragmentNumberOfLikes)).check(matches(withText("0")));
        onView(withId(R.id.postFragmentLikeButton)).perform(click());
        onView(withId(R.id.postFragmentNumberOfLikes)).check(matches(withText("1")));
    }


}
