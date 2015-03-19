package be.ugent.vop.ui.widget;

import android.content.Context;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AbsListView;

import be.ugent.vop.R;

public class CustomSwipeRefreshLayout extends SwipeRefreshLayout {

    private Context context;
    public CustomSwipeRefreshLayout(Context context) {
        super(context, null);
        this.context = context;
    }

    public CustomSwipeRefreshLayout(Context context, AttributeSet attrs){
        super(context, attrs);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if(checkCoordinateCross(ev, R.id.map)) {
            return false;
        }
        return super.onInterceptTouchEvent(ev);
    }
    private boolean checkCoordinateCross(MotionEvent ev, int resId) {
        View target = findViewById(resId);
        if(target == null) {
            return false;
        }
        if(ev.getX() > target.getX() && ev.getX() < target.getX() + target.getWidth() && ev.getY() > target.getY() && ev.getY() < target.getY() + target.getHeight()) {
            return true;
        }
        return false;
    }

    /*
    We have to override this because we use the SwipeRefreshLayout with a custom child view
     */
    @Override
    public boolean canChildScrollUp() {
        View mTarget = findViewById(R.id.venue_list);
        if (android.os.Build.VERSION.SDK_INT < 14) {
            if (mTarget instanceof AbsListView) {
                final AbsListView absListView = (AbsListView) mTarget;
                return absListView.getChildCount() > 0
                        && (absListView.getFirstVisiblePosition() > 0 || absListView.getChildAt(0)
                        .getTop() < absListView.getPaddingTop());
            } else {
                return mTarget.getScrollY() > 0;
            }
        } else {
            return ViewCompat.canScrollVertically(mTarget, -1);
        }
    }
}
