package ch.epfl.sdp.kandle.idlingressource;

import android.view.View;
/** Idling resource which waits for a view to be hidden. */
public class WaitForViewToHide extends ViewIdlingResource {
    public WaitForViewToHide(int viewId) {
        super(viewId);
    }
    @Override
    protected boolean isViewIdle(View view) {
        return !view.isShown();
    }
}
