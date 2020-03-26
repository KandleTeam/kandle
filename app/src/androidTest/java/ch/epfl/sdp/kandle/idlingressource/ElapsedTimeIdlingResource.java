package ch.epfl.sdp.kandle.idlingressource;

import androidx.test.espresso.IdlingResource;
/** Idling resource which will block until the timeout occurs. */
public class ElapsedTimeIdlingResource implements IdlingResource {
    private long mStartTime;
    private long mWaitTime;
    private IdlingResource.ResourceCallback mResourceCallback;
    public ElapsedTimeIdlingResource(long waitTime) {
        mStartTime = System.currentTimeMillis();
        mWaitTime = waitTime;
    }
    @Override
    public String getName() {
        return "ElapsedTimeIdlingResource:" + mWaitTime;
    }
    @Override
    public boolean isIdleNow() {
        if ((System.currentTimeMillis() - mStartTime) >= mWaitTime) {
            mResourceCallback.onTransitionToIdle();
            return true;
        }
        return false;
    }
    @Override
    public void registerIdleTransitionCallback(ResourceCallback resourceCallback) {
        mResourceCallback = resourceCallback;
    }
}
