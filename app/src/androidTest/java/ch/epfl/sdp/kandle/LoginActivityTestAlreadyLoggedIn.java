package ch.epfl.sdp.kandle;

import android.view.Gravity;

import org.junit.Rule;
import org.junit.Test;

import androidx.test.rule.ActivityTestRule;

import ch.epfl.sdp.kandle.dependencies.DependencyManager;

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
                protected  void beforeActivityLaunched() {
                    DependencyManager.setFreshTestDependencies(true);
                }
            };

    @Test
    public void checkAutomaticLogIn(){
        onView(withId(R.id.drawer_layout)).check(matches(isClosed(Gravity.LEFT)));
    }
}
