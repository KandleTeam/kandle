package ch.epfl.sdp.kandle.idlingressource;

import android.view.View;
/** Idling resource which waits for a view to be shown. */
public class WaitForViewToShow extends ViewIdlingResource {
    public WaitForViewToShow(int viewId) {
        super(viewId);
    }
    @Override
    protected boolean isViewIdle(View view) {
        return view.isShown();
    }
}
