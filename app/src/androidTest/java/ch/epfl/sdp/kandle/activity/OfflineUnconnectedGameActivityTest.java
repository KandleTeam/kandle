package ch.epfl.sdp.kandle.activity;

import androidx.room.Room;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ActivityTestRule;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashMap;

import ch.epfl.sdp.kandle.Kandle;
import ch.epfl.sdp.kandle.R;
import ch.epfl.sdp.kandle.activity.LoginActivity;
import ch.epfl.sdp.kandle.activity.OfflineGameActivity;
import ch.epfl.sdp.kandle.dependencies.DependencyManager;
import ch.epfl.sdp.kandle.dependencies.MockAuthentication;
import ch.epfl.sdp.kandle.dependencies.MockDatabase;
import ch.epfl.sdp.kandle.dependencies.MockImageStorage;
import ch.epfl.sdp.kandle.dependencies.MockInternalStorage;
import ch.epfl.sdp.kandle.dependencies.MockNetwork;
import ch.epfl.sdp.kandle.entities.post.Post;
import ch.epfl.sdp.kandle.entities.user.User;
import ch.epfl.sdp.kandle.storage.caching.CachedFirestoreDatabase;
import ch.epfl.sdp.kandle.storage.room.LocalDatabase;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

@RunWith(AndroidJUnit4.class)
public class OfflineUnconnectedGameActivityTest {
    private MockNetwork network;
    private LocalDatabase localDatabase;
    @Rule
    public ActivityTestRule<LoginActivity> intentsRule =
            new ActivityTestRule<LoginActivity>(LoginActivity.class, true, true){
                @Override
                protected void beforeActivityLaunched() {
                    HashMap<String, String> accounts = new HashMap<>();
                    HashMap<String, User> users = new HashMap<>();
                    HashMap<String, MockDatabase.Follow> followMap = new HashMap<>();
                    HashMap<String, Post> posts = new HashMap<>();
                    MockDatabase db = new MockDatabase(false, users, followMap, posts);
                    MockAuthentication authentication = new MockAuthentication(true, accounts, "password");
                    MockImageStorage storage = new MockImageStorage();
                    MockInternalStorage internalStorage = new MockInternalStorage(new HashMap<>());
                    network = new MockNetwork(false);
                    localDatabase = Room.inMemoryDatabaseBuilder(Kandle.getContext(), LocalDatabase.class).allowMainThreadQueries().build();
                    DependencyManager.setFreshTestDependencies(authentication, db, storage, internalStorage, network, localDatabase);
                }
            };

    @Before
    public void navigateToOfflineGame() {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        onView(ViewMatchers.withId(R.id.startOfflineGameButton)).perform(click());
    }

    @Test
    public void startGameAndClickOnVirusUpdateScore(){
        onView(withId(R.id.startButton)).perform(click());
        for(int i=0; i < OfflineGameActivity.MAX_NB_VIRUS; i++){
            onView(withId(R.id.virusButton)).perform(click());
        }
        onView(withId(R.id.score)).check(matches(not(withText(is("0")))));
        onView(withId(R.id.maxScore)).check(matches((withText(is(Integer.toString(OfflineGameActivity.MAX_NB_VIRUS))))));
    }


    @Test
    public void startGameAndNoClickOnVirus(){
        onView(withId(R.id.startButton)).perform(click());
        try {
            Thread.sleep(OfflineGameActivity.APPEARING_TIME * OfflineGameActivity.MAX_NB_VIRUS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        onView(withId(R.id.score)).check(matches((withText(is("0")))));
    }

    @After
    public void clearCurrentUser() {
        localDatabase.close();
    }

}
