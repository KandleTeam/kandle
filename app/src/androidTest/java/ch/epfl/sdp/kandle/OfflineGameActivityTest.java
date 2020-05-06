package ch.epfl.sdp.kandle;

import android.content.res.Resources;

import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ActivityTestRule;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashMap;

import ch.epfl.sdp.kandle.Storage.room.LocalDatabase;
import ch.epfl.sdp.kandle.activity.LoginActivity;
import ch.epfl.sdp.kandle.dependencies.DependencyManager;
import ch.epfl.sdp.kandle.dependencies.MockAuthentication;
import ch.epfl.sdp.kandle.dependencies.MockDatabase;
import ch.epfl.sdp.kandle.dependencies.MockInternalStorage;
import ch.epfl.sdp.kandle.dependencies.MockNetwork;
import ch.epfl.sdp.kandle.dependencies.MockStorage;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

@RunWith(AndroidJUnit4.class)
public class OfflineGameActivityTest {

    private LocalDatabase localDatabase;
    private User alreadyHasAnAccount;
    @Rule
    public ActivityTestRule<LoginActivity> intentsRule =
            new ActivityTestRule<LoginActivity>(LoginActivity.class, true, true){
                @Override
                protected void beforeActivityLaunched() {
                    MockNetwork network = new MockNetwork(false);
                }
            };

    @Before
    public void navigateToOfflineGame() {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        onView(withId(R.id.startOfflineGameButton)).perform(click());
    }

    @Test
    public void startGameAndClickOnVirusUpdateScore(){
        onView(withId(R.id.startButton)).perform(click());
        onView(withId(R.id.virusButton)).perform(click());
        onView(withId(R.id.virusButton)).perform(click());
        onView(withId(R.id.score)).check(matches(not(withText(is("0")))));
        onView(withId(R.id.maxScore)).check(matches((withText(is("2")))));
    }


    @Test
    public void startGameAndNoClickOnVirus(){
        onView(withId(R.id.startButton)).perform(click());
        try {
            Thread.sleep(7000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        onView(withId(R.id.score)).check(matches((withText(is("0")))));
    }

}
