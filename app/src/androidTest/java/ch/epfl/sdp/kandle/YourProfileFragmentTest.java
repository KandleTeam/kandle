package ch.epfl.sdp.kandle;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.nfc.Tag;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.contrib.DrawerActions;
import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.espresso.intent.rule.IntentsTestRule;
import androidx.test.espresso.matcher.BoundedMatcher;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.rule.GrantPermissionRule;


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
import ch.epfl.sdp.kandle.fragment.ProfileFragment;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.clearText;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.contrib.DrawerMatchers.isClosed;
import static androidx.test.espresso.intent.Intents.intending;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasAction;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withTagValue;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static ch.epfl.sdp.kandle.dependencies.DependencyManager.getDatabaseSystem;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

public class YourProfileFragmentTest {

    public static User user1;
    public static User user2;
    public static User user3;
    public static User user4;
    public static User user5;
    public static User user6;
    public static Post p1;
    public static Post p2;
    public static Post p3;
    public static Post p4;
    public static Post p5;
    private LocalDatabase localDatabase;
    @Rule
    public GrantPermissionRule permissionRule = GrantPermissionRule.grant(android.Manifest.permission.ACCESS_FINE_LOCATION);
    @Rule
    public IntentsTestRule<MainActivity> intentsRule =
            new IntentsTestRule<MainActivity>(MainActivity.class,true,true
            ){
                @Override
                protected void beforeActivityLaunched() {
                    LoggedInUser.init(new User("loggedInUserId","LoggedInUser","loggedInUser@kandle.ch","nickname","image"));
                    user1 = new User("user1Id","user1","user1@kandle.ch","user1",null);
                    user2 = new User("user2Id","user2","user2@kandle.ch","user2",null);
                    user3 = new User("user3Id", "user3", "user3@kandle.ch", null,  null);
                    user4 = new User("user4Id", "user4", "user4@kandle.ch", null,  null);
                    user5 = new User("user5Id", "user5", "user5@kandle.ch", null,  null);
                    user6 = new User("user6Id", "user6", "user6@kandle.ch", null,  null);
                    p1 =  new Post("Hello", null, new Date(), "loggedInUserId", "post1Id");
                    p2 = new Post("There", "null", new Date(), "loggedInUserId", "post2Id");
                    p3 =  new Post("My", null, new Date(), "loggedInUserId", "post3Id");
                    p4 =  new Post("You", null, new Date(), "loggedInUserId", "post4Id");
                    p5 =  new Post("Are", null, new Date(), "loggedInUserId", "post5Id");
                    HashMap<String, String> accounts = new HashMap<>();
                    HashMap<String,User> users = new HashMap<>();
                    accounts.put(user1.getEmail(), user1.getId());
                    accounts.put(user2.getEmail(), user2.getId());
                    accounts.put(user3.getEmail(), user3.getId());
                    accounts.put(user4.getEmail(), user4.getId());
                    accounts.put(user5.getEmail(), user5.getId());
                    accounts.put(user6.getEmail(), user6.getId());
                    HashMap<String, MockDatabase.Follow> followMap = new HashMap<>();
                    HashMap<String, Post> posts = new HashMap<>();
                    posts.put(p1.getPostId(),p1);
                    posts.put(p2.getPostId(),p2);
                    posts.put(p3.getPostId(),p3);
                    posts.put(p4.getPostId(),p4);
                    followMap.put(LoggedInUser.getInstance().getId(),new MockDatabase.Follow(new LinkedList<>(),new LinkedList<>()));
                    MockDatabase db = new MockDatabase(true, users, followMap, posts);
                    MockAuthentication authentication = new MockAuthentication(true, accounts, "password");
                    MockStorage storage = new MockStorage();
                    MockInternalStorage internalStorage = new MockInternalStorage(true);
                    MockNetwork network = new MockNetwork(true);
                    localDatabase = Room.inMemoryDatabaseBuilder(Kandle.getContext(), LocalDatabase.class).allowMainThreadQueries().build();
                    DependencyManager.setFreshTestDependencies(authentication, db, storage,internalStorage,network,localDatabase);
                    getDatabaseSystem().createUser(user1);
                    getDatabaseSystem().createUser(user2);
                    getDatabaseSystem().createUser(user3);
                    getDatabaseSystem().createUser(user4);
                    getDatabaseSystem().createUser(user5);
                    getDatabaseSystem().createUser(user6);
                    getDatabaseSystem().follow(user1.getId(),LoggedInUser.getInstance().getId());
                    getDatabaseSystem().follow(LoggedInUser.getInstance().getId(),user1.getId());
                    getDatabaseSystem().follow(user2.getId(),LoggedInUser.getInstance().getId());
                }
            };

    @Before
    public void loadFragment(){
        onView(withId(R.id.drawer_layout)).check(matches(isClosed(Gravity.LEFT))).perform(DrawerActions.open());
        onView(withId(R.id.profilePicInMenu)).perform(click());
    }

    @After
    public void clearCurrentUserAndLocalDb(){
        LoggedInUser.clear();
        localDatabase.close();
    }

    @Test
    public void testImageViewIsEmpty(){
        onView(withId(R.id.badgePicture)).check(matches(withTagValue(equalTo(R.drawable.ic_icons2_medal_64))));
    }

    @Test
    public void testImageViewForSuccesses(){
        DependencyManager.getDatabaseSystem().addPost(p5);
        onView(withId(R.id.badgePicture)).check(matches(withTagValue(equalTo(R.drawable.ic_icons2_medal_64))));
        DependencyManager.getDatabaseSystem().follow(user3.getId(),LoggedInUser.getInstance().getId());
        DependencyManager.getDatabaseSystem().follow(LoggedInUser.getInstance().getId(), user2.getId());
        DependencyManager.getDatabaseSystem().follow(LoggedInUser.getInstance().getId(), user3.getId());
        loadFragment();
        onView(withId(R.id.badgePicture)).check(matches(withTagValue(equalTo(R.drawable.ic_icons1_medal_64))));
        DependencyManager.getDatabaseSystem().follow(user4.getId(),LoggedInUser.getInstance().getId());
        DependencyManager.getDatabaseSystem().follow(user5.getId(),LoggedInUser.getInstance().getId());
        DependencyManager.getDatabaseSystem().follow(user6.getId(),LoggedInUser.getInstance().getId());
        DependencyManager.getDatabaseSystem().follow(LoggedInUser.getInstance().getId(), user4.getId());
        DependencyManager.getDatabaseSystem().follow(LoggedInUser.getInstance().getId(), user5.getId());
        DependencyManager.getDatabaseSystem().follow(LoggedInUser.getInstance().getId(), user6.getId());
        loadFragment();
        onView(withId(R.id.badgePicture)).check(matches(withTagValue(equalTo(R.drawable.icons8_medal_64_1))));
    }

    @Test
    public void editButtonIsVisible(){
        onView(withId(R.id.profileEditPictureButton)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)));
    }

    @Test
    public void listOfFollowers(){
        onView(withId(R.id.profileNumberOfFollowers)).perform(click());
        onView(withId(R.id.list_user_recycler_view)).check(matches(atPosition(0, hasDescendant(withText("@" + user1.getUsername())))));
    }

    @Test
    public void listOfFollowing(){
        onView(withId(R.id.profileNumberOfFollowing)).perform(click());
        onView(withId(R.id.list_user_recycler_view)).check(matches(atPosition(0, hasDescendant(withText("@" + user1.getUsername())))));
        onView(withId(R.id.list_user_recycler_view)).perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));
        onView(withId(R.id.profileUsername)).check(matches(withText("@user1")));
        onView(withId(R.id.profileNumberOfFollowers)).perform(click());
    }

    @Test
    public void editProfilePicture(){
        Intent resultData = new Intent();
        resultData.setAction(Intent.ACTION_GET_CONTENT);
        Uri imageUri = Uri.parse("android.resource://ch.epfl.sdp.kandle/drawable/ic_launcher_background.xml");
        resultData.setData(imageUri);
        Instrumentation.ActivityResult result = new Instrumentation.ActivityResult(Activity.RESULT_OK, resultData);
        intending(hasAction(Intent.ACTION_GET_CONTENT)).respondWith(result);

        onView(withId(R.id.profileEditPictureButton)).perform(click());
        onView(withId(R.id.profilePicture)).check(matches(withTagValue(is(ProfileFragment.PROFILE_PICTURE_AFTER))));

        onView(withId(R.id.profileValidatePictureButton)).perform(click());
    }

    @Test
    public void editNickname(){
        onView(withId(R.id.profileEditNameButton)).perform(click());

        onView(withId(R.id.edit_view)).perform(clearText());
        onView(withId(R.id.edit_view)).perform(typeText("New Nickname"));
        onView(withId (R.id.edit_view)).perform(closeSoftKeyboard());
        onView(withId(R.id.profileValidateNameButton)).perform(click());
        onView(withId(R.id.text_view)).check(matches(withText("New Nickname")));
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

    public static Matcher<View> atPosition(final int position, @NonNull final Matcher<View> itemMatcher) {

        return new BoundedMatcher<View, RecyclerView>(RecyclerView.class) {
            @Override
            public void describeTo(Description description) {
                description.appendText("has item at position " + position + ": ");
                itemMatcher.describeTo(description);
            }

            @Override
            protected boolean matchesSafely(final RecyclerView view) {
                RecyclerView.ViewHolder viewHolder = view.findViewHolderForAdapterPosition(position);
                if (viewHolder == null) {
                    // has no item on such position
                    return false;
                }
                return itemMatcher.matches(viewHolder.itemView);
            }
        };
    }

}
