package be.ugent.vop.ui.event;

/**
 * Created by Lars on 09/05/15.
 */
public class ConcreteChildData extends AbstractDataProvider.ChildData {

    private long mId;
    private final String mReward;
    private final String mInfo;
    private final String mGroups;
    private final int mSwipeReaction;
    private boolean mPinnedToSwipeLeft;
    private String mVenueId;

    public ConcreteChildData(long id, String reward, String info, String groups, String venueId, int swipeReaction) {
        mId = id;
        mReward = reward;
        mInfo = info;
        mGroups = groups;
        mVenueId = venueId;
        mSwipeReaction = swipeReaction;
    }

    @Override
    public long getChildId() {
        return mId;
    }

    @Override
    public int getSwipeReactionType() {
        return mSwipeReaction;
    }

    @Override
    public String getReward() {
        return mReward;
    }

    @Override
    public String getInfo() {
        return mInfo;
    }

    @Override
    public String getGroups() {
        return mGroups;
    }

    @Override
    public String getVenueId() {
        return mVenueId;
    }

    @Override
    public void setPinnedToSwipeLeft(boolean pinnedToSwipeLeft) {
        mPinnedToSwipeLeft = pinnedToSwipeLeft;
    }

    @Override
    public boolean isPinnedToSwipeLeft() {
        return mPinnedToSwipeLeft;
    }

    public void setChildId(long id) {
        this.mId = id;
    }
}