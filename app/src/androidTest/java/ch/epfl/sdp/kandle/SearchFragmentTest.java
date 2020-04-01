package ch.epfl.sdp.kandle;

import android.view.Gravity;
import android.view.View;
import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.contrib.DrawerActions;
import androidx.test.espresso.contrib.NavigationViewActions;
import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.rule.ActivityTestRule;
import java.util.HashMap;
import java.util.LinkedList;
import ch.epfl.sdp.kandle.dependencies.DependencyManager;
import ch.epfl.sdp.kandle.dependencies.Follow;
import static androidx.test.espresso.action.ViewActions.clearText;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.contrib.DrawerMatchers.isClosed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

public class SearchFragmentTest {


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
                    HashMap<String,Follow> followMap = new HashMap<>();
                    followMap.put(user1.getId(),new Follow(new LinkedList<>(),new LinkedList<>()));
                    followMap.put(user2.getId(),new Follow(new LinkedList<>(),new LinkedList<>()));
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
        onView(withId(R.id.navigation_view)).perform(NavigationViewActions.navigateTo(R.id.follow));
    }

    @Test
    public void followThenUnfollow() {

        onView(withId(R.id.search_bar)).perform(typeText("us"));
        onView(withId (R.id.search_bar)).perform(closeSoftKeyboard());

        onView(withId(R.id.search_bar)).perform(clearText());
        onView(withId (R.id.search_bar)).perform(closeSoftKeyboard());

        onView(withId(R.id.search_bar)).perform(typeText("us"));
        onView(withId (R.id.search_bar)).perform(closeSoftKeyboard());

        onView(withId(R.id.recycler_view)).perform(RecyclerViewActions.actionOnItemAtPosition(0, clickChildViewWithId(R.id.btn_follow)));
        onView(withId(R.id.recycler_view)).perform(RecyclerViewActions.actionOnItemAtPosition(0, clickChildViewWithId(R.id.btn_follow)));
        onView(withId(R.id.recycler_view)).perform(RecyclerViewActions.actionOnItemAtPosition(0, clickChildViewWithId(R.id.btn_follow)));

        onView(withId(R.id.recycler_view)).perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));

        onView(withId(R.id.profileNumberOfFollowing)).check(matches( withText("1")));
        onView(withId(R.id.profileNumberOfFollowers)).check(matches( withText("0")));

        onView(withId(R.id.profileFollowButton)).perform(click());
        onView(withId(R.id.profileNumberOfFollowers)).check(matches( withText("1")));
        onView(withId(R.id.profileFollowButton)).perform(click());
        onView(withId(R.id.profileNumberOfFollowers)).check(matches( withText("0")));
        onView(withId(R.id.profileFollowButton)).perform(click());
    }

    @Test
    public void clickOnUserProfile(){
        onView(withId(R.id.search_bar)).perform(typeText("us"));
        onView(withId (R.id.search_bar)).perform(closeSoftKeyboard());
        onView(withId(R.id.recycler_view)).perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));
        onView(withId(R.id.profileUsername)).check(matches(withText("@" + user1.getUsername())));
    }

    @Test
    public void userWithNoProfilePic() {
        onView(withId(R.id.search_bar)).perform(typeText(user1.getUsername()));
        onView(withId (R.id.search_bar)).perform(closeSoftKeyboard());
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
}
