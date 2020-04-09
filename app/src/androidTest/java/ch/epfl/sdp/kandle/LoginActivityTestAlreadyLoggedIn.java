package ch.epfl.sdp.kandle;

import android.view.Gravity;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import androidx.test.rule.ActivityTestRule;
import androidx.test.rule.GrantPermissionRule;

import java.util.HashMap;
import ch.epfl.sdp.kandle.dependencies.DependencyManager;
import ch.epfl.sdp.kandle.dependencies.MockAuthentication;
import ch.epfl.sdp.kandle.dependencies.MockDatabase;
import ch.epfl.sdp.kandle.dependencies.MockInternalStorage;
import ch.epfl.sdp.kandle.dependencies.MockNetwork;
import ch.epfl.sdp.kandle.dependencies.MockStorage;
import ch.epfl.sdp.kandle.dependencies.Post;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.contrib.DrawerMatchers.isClosed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

public class LoginActivityTestAlreadyLoggedIn {

    @Rule
    public GrantPermissionRule permissionRule = GrantPermissionRule.grant(android.Manifest.permission.ACCESS_FINE_LOCATION);

    @Rule
    public ActivityTestRule<LoginActivity> intentsRule =
            new ActivityTestRule<LoginActivity>(LoginActivity.class,true,true
            ){
                @Override
                protected void beforeActivityLaunched() {
                    LoggedInUser.init(new User("loggedInUserId","LoggedInUser","loggedInUser@kandle.ch","nickname","image"));

                    HashMap<String, String> accounts = new HashMap<>();
                    accounts.put(LoggedInUser.getInstance().getEmail(), LoggedInUser.getInstance().getId());
                    HashMap<String,User> users = new HashMap<>();
                    users.put(LoggedInUser.getInstance().getId(),LoggedInUser.getInstance());
                    HashMap<String, MockDatabase.Follow> followMap = new HashMap<>();
                    HashMap<String, Post> posts = new HashMap<>();
                    MockDatabase db = new MockDatabase(false, users, followMap, posts);
                    MockAuthentication authentication = new MockAuthentication(true, accounts, "password");
                    MockStorage storage = new MockStorage();
                    MockInternalStorage internalStorage = new MockInternalStorage(true);
                    MockNetwork network = new MockNetwork(false);
                    DependencyManager.setFreshTestDependencies(authentication, db, storage,internalStorage,network);
                    LoggedInUser.clear();
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


}
