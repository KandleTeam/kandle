package ch.epfl.sdp.kandle;

import android.view.Gravity;

import androidx.test.espresso.contrib.DrawerActions;
import androidx.test.espresso.contrib.NavigationViewActions;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ActivityTestRule;

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
import java.util.LinkedList;

import ch.epfl.sdp.kandle.dependencies.DependencyManager;
import ch.epfl.sdp.kandle.dependencies.MockDatabase;

@RunWith(AndroidJUnit4.class)
public class AchievementTest {
    public  User user1;
    public  User user2;

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
                    followMap.put(user1.getId(),new MockDatabase.Follow(new LinkedList<>(),new LinkedList<>()));
                    followMap.put(user2.getId(),new MockDatabase.Follow(new LinkedList<>(),new LinkedList<>()));
                    HashMap<String,Post> posts = new HashMap<>();
                    DependencyManager.setFreshTestDependencies(true,accounts,users,followMap,posts);
                    DependencyManager.getDatabaseSystem().createUser(user1);
                    DependencyManager.getDatabaseSystem().createUser(user2);
                    DependencyManager.getDatabaseSystem().follow(LoggedInUser.getInstance().getId(),user1.getId());
                    DependencyManager.getDatabaseSystem().follow(user1.getId(),LoggedInUser.getInstance().getId());
                }
            };

    @After
    public void clearCurrentUser(){
        LoggedInUser.clear();
    }
    @Before
    public void loadFragment(){
        onView(withId(R.id.drawer_layout)).check(matches(isClosed(Gravity.LEFT))).perform(DrawerActions.open());
        onView(withId(R.id.navigation_view)).perform(NavigationViewActions.navigateTo(R.id.light));
    }

    @Test
    public void allAchivementsNotDone(){
        onView(withId(R.id.is_following)).check(matches(withText("NOT DONE")));
        onView(withId(R.id.is_posts)).check(matches(withText("NOT DONE")));
        onView(withId(R.id.is_followers)).check(matches(withText("NOT DONE")));
    }

}