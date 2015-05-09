package be.ugent.vop.ui.event;

/**
 * Created by Lars on 09/05/15.
 */
public class ConcreteGroupData extends AbstractDataProvider.GroupData {

    private final long mId;
    private final String mEventTitle;
    private final int mSwipeReaction;
    private boolean mPinnedToSwipeLeft;
    private long mNextChildId;

    public ConcreteGroupData(long id, String title, int swipeReaction) {
        mId = id;
        mEventTitle = title;
        mSwipeReaction = swipeReaction;
        mNextChildId = 0;
    }

    @Override
    public long getGroupId() {
        return mId;
    }

    @Override
    public boolean isSectionHeader() {
        return false;
    }

    @Override
    public int getSwipeReactionType() {
        return mSwipeReaction;
    }

    @Override
    public String getTitle() {
        return mEventTitle;
    }

    @Override
    public void setPinnedToSwipeLeft(boolean pinnedToSwipeLeft) {
        mPinnedToSwipeLeft = pinnedToSwipeLeft;
    }

    @Override
    public boolean isPinnedToSwipeLeft() {
        return mPinnedToSwipeLeft;
    }

    public long generateNewChildId() {
        final long id = mNextChildId;
        mNextChildId += 1;
        return id;
    }
}