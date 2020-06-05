package ch.epfl.sdp.kandle.activity;

import android.content.res.Resources;

import org.junit.After;
import org.junit.Rule;
import org.junit.Test;

import java.util.HashMap;

import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.contrib.DrawerActions;
import androidx.test.espresso.contrib.NavigationViewActions;
import androidx.test.espresso.intent.rule.IntentsTestRule;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.rule.GrantPermissionRule;
import ch.epfl.sdp.kandle.Kandle;
import ch.epfl.sdp.kandle.R;
import ch.epfl.sdp.kandle.dependencies.DependencyManager;
import ch.epfl.sdp.kandle.dependencies.MockAuthentication;
import ch.epfl.sdp.kandle.dependencies.MockDatabase;
import ch.epfl.sdp.kandle.dependencies.MockImageStorage;
import ch.epfl.sdp.kandle.dependencies.MockInternalStorage;
import ch.epfl.sdp.kandle.dependencies.MockNetwork;
import ch.epfl.sdp.kandle.entities.post.Post;
import ch.epfl.sdp.kandle.entities.user.LoggedInUser;
import ch.epfl.sdp.kandle.entities.user.User;
import ch.epfl.sdp.kandle.storage.room.LocalDatabase;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.matcher.RootMatchers.withDecorView;
import static androidx.test.espresso.matcher.ViewMatchers.hasErrorText;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

public class RegisterActivityTest {
    @Rule
    public GrantPermissionRule permissionRule = GrantPermissionRule.grant(android.Manifest.permission.ACCESS_FINE_LOCATION);
    private Resources res = ApplicationProvider.getApplicationContext().getResources();
    private LocalDatabase localDatabase;
    private User userWithSameEmail;
    private User userWithSameUsername;
    private MockNetwork network;
    @Rule
    public IntentsTestRule<RegisterActivity> intentsRule =
            new IntentsTestRule<RegisterActivity>(RegisterActivity.class, true, true
            ) {
                @Override
                protected void beforeActivityLaunched() {
                    userWithSameEmail = new User("userIdE", "randomusername", "sameEmail@kandle.ch", "nickname", null);
                    userWithSameUsername = new User("userIdU", "sameusername", "randomEmail@kandle.ch", "nickname", null);
                    HashMap<String, String> accounts = new HashMap<>();
                    accounts.put(userWithSameEmail.getEmail(), userWithSameEmail.getId());
                    accounts.put(userWithSameUsername.getEmail(), userWithSameUsername.getId());
                    HashMap<String, User> users = new HashMap<>();
                    users.put(userWithSameUsername.getId(), userWithSameUsername);
                    users.put(userWithSameEmail.getId(), userWithSameEmail);
                    HashMap<String, MockDatabase.Follow> followMap = new HashMap<>();
                    HashMap<String, Post> posts = new HashMap<>();
                    MockDatabase db = new MockDatabase(false, users, followMap, posts);
                    MockAuthentication authentication = new MockAuthentication(false, accounts, "password");
                    MockImageStorage storage = new MockImageStorage();
                    MockInternalStorage internalStorage = new MockInternalStorage(new HashMap<>());
                    network = new MockNetwork(true);
                    localDatabase = Room.inMemoryDatabaseBuilder(Kandle.getContext(), LocalDatabase.class).allowMainThreadQueries().build();
                    DependencyManager.setFreshTestDependencies(authentication, db, storage, internalStorage, network, localDatabase);
                }
            };

    @After
    public void clearCurrentUserAndLocalDb() {
        LoggedInUser.clear();
        localDatabase.close();
    }

    @Test
    public void errorsInForm() {

        onView(ViewMatchers.withId(R.id.loginBtn)).perform(click());
        onView(withId(R.id.username)).check(matches(hasErrorText(res.getString(R.string.registerUsernameRequired))));

        onView(withId(R.id.username)).perform(typeText("test"));
        onView(withId(R.id.username)).perform(closeSoftKeyboard());

        onView(withId(R.id.loginBtn)).perform(click());
        onView(withId(R.id.email)).check(matches(hasErrorText(res.getString(R.string.registerEmailRequired))));

        onView(withId(R.id.email)).perform(typeText("test@test.com"));
        onView(withId(R.id.email)).perform(closeSoftKeyboard());

        onView(withId(R.id.password)).perform(typeText("123"));
        onView(withId(R.id.password)).perform(closeSoftKeyboard());

        onView(withId(R.id.loginBtn)).perform(click());
        onView(withId(R.id.password)).check(matches(hasErrorText(res.getString(R.string.registerPasswordLengthError))));

        onView(withId(R.id.password)).perform(typeText("12345678"));
        onView(withId(R.id.password)).perform(closeSoftKeyboard());
        onView(withId(R.id.passwordConfirm)).perform(typeText("123"));
        onView(withId(R.id.passwordConfirm)).perform(closeSoftKeyboard());

        onView(withId(R.id.loginBtn)).perform(click());
        onView(withId(R.id.passwordConfirm)).check(matches(hasErrorText(res.getString(R.string.registerPasswordsMatchingError))));
    }

    @Test
    public void accountCreationShouldFailBecauseThereIsAlreadyOneWithSameEmail() {

        onView(withId(R.id.username)).perform(typeText("uniqueUsername"));
        onView(withId(R.id.username)).perform(closeSoftKeyboard());

        onView(withId(R.id.email)).perform(typeText(userWithSameEmail.getEmail()));
        onView(withId(R.id.email)).perform(closeSoftKeyboard());

        onView(withId(R.id.password)).perform(typeText("12345678"));
        onView(withId(R.id.password)).perform(closeSoftKeyboard());

        onView(withId(R.id.passwordConfirm)).perform(typeText("12345678"));
        onView(withId(R.id.passwordConfirm)).perform(closeSoftKeyboard());

        onView(withId(R.id.loginBtn)).perform(click());
        onView(withText("An error has occurred : You already have an account")).inRoot(withDecorView(not(is(intentsRule.getActivity().getWindow().getDecorView())))).check(matches(isDisplayed()));
    }

    @Test
    public void accountCreationShouldFailBecauseThereIsAlreadyOneWithSameUsername() {

        onView(withId(R.id.username)).perform(typeText(userWithSameUsername.getUsername()));
        onView(withId(R.id.username)).perform(closeSoftKeyboard());

        onView(withId(R.id.email)).perform(typeText("uniqueEmail@kandle.ch"));
        onView(withId(R.id.email)).perform(closeSoftKeyboard());

        onView(withId(R.id.password)).perform(typeText("12345678"));
        onView(withId(R.id.password)).perform(closeSoftKeyboard());

        onView(withId(R.id.passwordConfirm)).perform(typeText("12345678"));
        onView(withId(R.id.passwordConfirm)).perform(closeSoftKeyboard());

        onView(withId(R.id.loginBtn)).perform(click());
        onView(withId(R.id.username)).check(matches(hasErrorText(res.getString(R.string.registerUsernameAlreadyUsed))));
    }

    @Test
    public void accountCreation() {


        onView(withId(R.id.username)).perform(typeText("newUserId"));
        onView(withId(R.id.username)).perform(closeSoftKeyboard());


        onView(withId(R.id.email)).perform(typeText("newedfgfdsgdfgdf@kandle.ch"));
        onView(withId(R.id.email)).perform(closeSoftKeyboard());

        onView(withId(R.id.password)).perform(typeText("12345678"));
        onView(withId(R.id.password)).perform(closeSoftKeyboard());

        onView(withId(R.id.passwordConfirm)).perform(typeText("12345678"));
        onView(withId(R.id.passwordConfirm)).perform(closeSoftKeyboard());

        onView(withId(R.id.loginBtn)).perform(click());
        intended(hasComponent(CustomAccountActivity.class.getName()));
        onView(withId(R.id.startButton)).perform(click());
        onView(withId(R.id.drawer_layout)).perform(DrawerActions.open());
        onView(withId(R.id.navigation_view)).perform(NavigationViewActions.navigateTo(R.id.logout));


    }


    @Test
    public void wantToLoginInsteadToRegister() {
        onView(withId(R.id.signInLink)).perform(click());
        intended(hasComponent(LoginActivity.class.getName()));
    }

    @Test
    public void doNotHaveInternetWhenLoginIn() {
        network.setIsOnline(false);
        onView(withId(R.id.username)).perform(typeText("newUserId"));
        onView(withId(R.id.username)).perform(closeSoftKeyboard());
        onView(withId(R.id.email)).perform(typeText("newedfgfdsgdfgdf@kandle.ch"));
        onView(withId(R.id.email)).perform(closeSoftKeyboard());
        onView(withId(R.id.password)).perform(typeText("12345678"));
        onView(withId(R.id.password)).perform(closeSoftKeyboard());
        onView(withId(R.id.passwordConfirm)).perform(typeText("12345678"));
        onView(withId(R.id.passwordConfirm)).perform(closeSoftKeyboard());
        onView(withId(R.id.loginBtn)).perform(click());
        onView(withId(com.google.android.material.R.id.snackbar_text))
                .check(matches(withText(R.string.noConnexion)));

    }


}
