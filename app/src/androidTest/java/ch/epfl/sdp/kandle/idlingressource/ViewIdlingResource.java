package ch.epfl.sdp.kandle.idlingressource;

import android.app.Activity;
import android.view.View;
import androidx.annotation.IdRes;
import androidx.test.espresso.IdlingResource;
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry;
import androidx.test.runner.lifecycle.Stage;
import java.util.Collection;
/** Idling resource which wait for view with given resource id. */
public abstract class ViewIdlingResource implements IdlingResource {
    @IdRes
    private final int mViewId;
    private ResourceCallback mResourceCallback;
    protected ViewIdlingResource(@IdRes int viewId) {
        mViewId = viewId;
    }
    protected abstract boolean isViewIdle(View view);
    @Override
    public String getName() {
        return "ViewIdlingResource";
    }
    @Override
    public boolean isIdleNow() {
        Collection<Activity> resumedActivities =
                ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(Stage.RESUMED);
        for (Activity activity : resumedActivities) {
            View view = activity.findViewById(mViewId);
            if (view != null && isViewIdle(view)) {
                if (mResourceCallback != null) {
                    mResourceCallback.onTransitionToIdle();
                }
                return true;
            }
        }
        return false;
    }
    @Override
    public void registerIdleTransitionCallback(ResourceCallback callback) {
        mResourceCallback = callback;
    }
}