package ch.epfl.sdp.kandle;

import android.view.Gravity;
import android.widget.PopupMenu;


import androidx.recyclerview.widget.RecyclerView;
import androidx.test.espresso.contrib.DrawerActions;
import androidx.test.espresso.contrib.NavigationViewActions;
import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ActivityTestRule;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Date;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.contrib.DrawerMatchers.isClosed;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

@RunWith(AndroidJUnit4.class)
public class PostViewerTest {

    @Rule
    public final ActivityTestRule<MainActivity> mainActivityRule =
            new ActivityTestRule<>(MainActivity.class);



    @Test
    public void canClickOnPostAndRemoveIt() throws InterruptedException {
        onView(withId(R.id.drawer_layout)).check(matches(isClosed(Gravity.LEFT))).perform(DrawerActions.open());
        onView(withId(R.id.navigation_view)).perform(NavigationViewActions.navigateTo(R.id.your_posts));
        PostFragment frag = PostFragment.newInstance();
        ArrayList<Post> myposts = frag.getPostList();
        Post p = new Post("Text",34,"this is my post",new Date() );
        frag.putInPostList(p);
        onView(withId(R.id.rvPosts)).perform(RecyclerViewActions.actionOnItemAtPosition(0,click()));
        onView(withId(R.id.post_description)).perform(click());

        frag.removePost(p);
        //onView(withId(R.id.post_content)).perform(click());
    }

    @Test
    public void putNewPostIntoTheRecyclerAndDeleteIt(){
        
        PostFragment frag = PostFragment.newInstance();
        frag.putInPostList(new Post("Text",34,"this is my post",new Date() ));
        frag.removePostAtIndex(0);
    }


}
