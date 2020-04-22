package ch.epfl.sdp.kandle;

import android.Manifest;
import android.view.Gravity;

import androidx.room.Room;
import androidx.test.espresso.contrib.DrawerActions;
import androidx.test.espresso.contrib.NavigationViewActions;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ActivityTestRule;
import androidx.test.rule.GrantPermissionRule;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.contrib.DrawerMatchers.isClosed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;


import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashMap;

import ch.epfl.sdp.kandle.Storage.room.LocalDatabase;
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
    private LocalDatabase localDatabase;

    @Rule
    public GrantPermissionRule grantLocation = GrantPermissionRule.grant(Manifest.permission.ACCESS_FINE_LOCATION);

    @Rule
    public ActivityTestRule<MainActivity> intentsRule =
            new ActivityTestRule<MainActivity>(MainActivity.class,true,true){
                @Override
                protected void beforeActivityLaunched() {
                    user1 = new User("user1Id", "user1", "user1@kandle.ch", null,  null);
                    user2 = new User("user2Id", "user2", "user2@kandle.ch", null,  "image");
                    LoggedInUser.init(new User("loggedInUserId","LoggedInUser","loggedInUser@kandle.ch","nickname","image"));
                    HashMap<String,String> accounts = new HashMap<>();
                    accounts.put(user1.getEmail(),user1.getId());
                    accounts.put(user2.getEmail(),user2.getId());
                    HashMap<String,User> users = new HashMap<>();
                    HashMap<String, MockDatabase.Follow> followMap = new HashMap<>();

                    HashMap<String, Post> posts = new HashMap<>();
                    MockDatabase db = new MockDatabase(true, users, followMap, posts);
                    MockAuthentication authentication = new MockAuthentication(true, accounts, "password");
                    MockStorage storage = new MockStorage();
                    MockInternalStorage internalStorage = new MockInternalStorage();
                    MockNetwork network = new MockNetwork(true);
                    localDatabase = Room.inMemoryDatabaseBuilder(Kandle.getContext(), LocalDatabase.class).allowMainThreadQueries().build();
                    DependencyManager.setFreshTestDependencies(authentication, db, storage,internalStorage,network,localDatabase);
                    DependencyManager.getDatabaseSystem().createUser(user1);
                    DependencyManager.getDatabaseSystem().createUser(user2);
                    DependencyManager.getDatabaseSystem().follow(LoggedInUser.getInstance().getId(),user1.getId());
                    DependencyManager.getDatabaseSystem().follow(user1.getId(),LoggedInUser.getInstance().getId());
                }
            };




    @After
    public void clearCurrentUserAndCloseLocalDb(){
        LoggedInUser.clear();
        localDatabase.close();
    }


    @Before
    public void loadFragment(){
        onView(withId(R.id.drawer_layout)).check(matches(isClosed(Gravity.LEFT))).perform(DrawerActions.open());
        onView(withId(R.id.navigation_view)).perform(NavigationViewActions.navigateTo(R.id.light));
        onView(withId(R.id.drawer_layout)).perform(DrawerActions.close());
    }

    @Test
    public void allAchievementsNotDone(){
        onView(withId(R.id.is_following)).check(matches(withText("NOT DONE"))  ) ;
        onView(withId(R.id.is_posts)).check(matches(withText("NOT DONE")));
        onView(withId(R.id.is_followers)).check(matches(withText("NOT DONE")));
    }

}