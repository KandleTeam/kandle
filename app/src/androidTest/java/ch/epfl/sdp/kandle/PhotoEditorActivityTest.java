package ch.epfl.sdp.kandle;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ActivityTestRule;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import ch.epfl.sdp.kandle.activity.PhotoEditorActivity;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.matcher.ViewMatchers.withId;


@RunWith(AndroidJUnit4.class)
public class PhotoEditorActivityTest {

    @Rule
    public ActivityTestRule<PhotoEditorActivity> intentsRule =
            new ActivityTestRule<PhotoEditorActivity>(PhotoEditorActivity.class, true, true) {
                @Override
                protected void beforeActivityLaunched() {
                }
            };



    @Test
    public void brushAndColor()  {
        onView(withId(R.id.brushButton)).perform(click());
        onView(withId(R.id.okColorButton)).perform(click());
    }

    @Test
    public void textAndColor() {
        onView(withId(R.id.textButton)).perform(click());
        onView(withId(R.id.okColorButton)).perform(click());
        onView(withId(R.id.add_text_edit_text)).perform(typeText("test"));
        onView(withId(R.id.add_text_edit_text)).perform(closeSoftKeyboard());
        onView(withId(R.id.add_text_done_tv)).perform(click());
    }

    @Test
    public void eraser() {
        onView(withId(R.id.eraserButton)).perform(click());
    }

    @Test
    public void undoAndRedo() {
        onView(withId(R.id.undoButton)).perform(click());
        onView(withId(R.id.redoButton)).perform(click());
    }

    @Test
    public void finish() {
        onView(withId(R.id.finishButton)).perform(click());
    }

}
