package ch.epfl.sdp.kandle;

import android.view.Gravity;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Rule;
import org.junit.Test;
import androidx.test.rule.ActivityTestRule;
import java.util.HashMap;
import ch.epfl.sdp.kandle.dependencies.DependencyManager;
import ch.epfl.sdp.kandle.dependencies.Follow;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.contrib.DrawerMatchers.isClosed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

public class LoginActivityTestAlreadyLoggedIn {

    @Rule
    public ActivityTestRule<LoginActivity> intentsRule =
            new ActivityTestRule<LoginActivity>(LoginActivity.class,true,true
            ){
                @Override
                protected void beforeActivityLaunched() {
                    LoggedInUser.init(new User("loggedInUserId","LoggedInUser","loggedInUser@kandle.ch","nickname","image"));
                    HashMap<String,String> accounts = new HashMap<>();
                    HashMap<String,User> users = new HashMap<>();
                    users.put(LoggedInUser.getInstance().getId(),LoggedInUser.getInstance());
                    HashMap<String, Follow> followMap = new HashMap<>();
                    HashMap<String, Post> posts = new HashMap<>();
                    DependencyManager.setFreshTestDependencies(true,accounts,users,followMap,posts);
                }
            };

    @After
    public void signout() {
        DependencyManager.getAuthSystem().signOut();
    }

    @Test
    public void checkAutomaticLogIn(){
        onView(withId(R.id.drawer_layout)).check(matches(isClosed(Gravity.LEFT)));
    }

    @AfterClass
    public static void clearLoggedInUserForNextTest(){
        LoggedInUser.clear();
    }
}
