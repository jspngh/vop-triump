package be.ugent.vop.ui.venue;

import android.app.Activity;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Loader;
import android.graphics.drawable.NinePatchDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;

import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.h6ah4i.android.widget.advrecyclerview.animator.GeneralItemAnimator;
import com.h6ah4i.android.widget.advrecyclerview.animator.RefactoredDefaultItemAnimator;
import com.h6ah4i.android.widget.advrecyclerview.decoration.ItemShadowDecorator;
import com.h6ah4i.android.widget.advrecyclerview.decoration.SimpleListDividerDecorator;
import com.h6ah4i.android.widget.advrecyclerview.expandable.RecyclerViewExpandableItemManager;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.RecyclerViewSwipeManager;
import com.h6ah4i.android.widget.advrecyclerview.utils.WrapperAdapterUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import be.ugent.vop.R;
import be.ugent.vop.backend.loaders.VenueEventsLoader;
import be.ugent.vop.backend.myApi.model.EventBean;
import be.ugent.vop.backend.myApi.model.GroupBean;
import be.ugent.vop.foursquare.FoursquareVenue;
import be.ugent.vop.ui.event.AbstractDataProvider;
import be.ugent.vop.ui.event.ConcreteChildData;
import be.ugent.vop.ui.event.ConcreteGroupData;
import be.ugent.vop.ui.event.MyExpandableItemAdapter;


public class VenueEventFragment extends Fragment implements VenueActivity.VenueActivityCallback {
    private static final String TAG = "VenueEventFragment2";
    private static final String SAVED_STATE_EXPANDABLE_ITEM_MANAGER = "RecyclerViewExpandableItemManager";

    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private RecyclerView.Adapter mAdapter;
    private EventDataProvider mProvider;
    private RecyclerView.Adapter mWrappedAdapter;
    private RecyclerViewExpandableItemManager mRecyclerViewExpandableItemManager;
    protected SwipeRefreshLayout mSwipeRefreshLayout;
    private TextView mEmpty;
    private ProgressBar mLoading;
    private VenueActivity mActivity;
    private String fsVenueId;
    public VenueEventFragment() {
        super();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_venue_event, container, false);
        if(getArguments().containsKey(VenueActivity.VENUE_ID))
            fsVenueId = getArguments().getString(VenueActivity.VENUE_ID);
        Log.d(TAG, "venueId:"+fsVenueId);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mLoading = (ProgressBar)rootView.findViewById(R.id.loading);
        mProvider = new EventDataProvider(); // true: example test data
        mSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.fragment_group_swipe_refresh);
        mEmpty = (TextView)rootView.findViewById(R.id.empty);
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);
        final GeneralItemAnimator animator = new RefactoredDefaultItemAnimator();
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setItemAnimator(animator);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final Parcelable eimSavedState = (savedInstanceState != null) ? savedInstanceState.getParcelable(SAVED_STATE_EXPANDABLE_ITEM_MANAGER) : null;
        mRecyclerViewExpandableItemManager = new RecyclerViewExpandableItemManager(eimSavedState);

        //adapter
        final MyExpandableItemAdapter myItemAdapter = new MyExpandableItemAdapter(getDataProvider(), mActivity, true);

        mAdapter = myItemAdapter;

        mWrappedAdapter = mRecyclerViewExpandableItemManager.createWrappedAdapter(myItemAdapter);       // wrap for expanding

        mRecyclerView.setAdapter(mWrappedAdapter);  // requires *wrapped* adapter

        mRecyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                if(mActivity != null)
                    mActivity.onScroll(dx, dy, null);
            }
        });


        // additional decorations
        //noinspection StatementWithEmptyBody
        if (supportsViewElevation()) {
            // Lollipop or later has native drop shadow feature. ItemShadowDecorator is not required.
        } else {
            mRecyclerView.addItemDecoration(new ItemShadowDecorator((NinePatchDrawable) getResources().getDrawable(R.drawable.material_shadow_z1)));
        }
        mRecyclerView.addItemDecoration(new SimpleListDividerDecorator(getResources().getDrawable(R.drawable.list_divider), true));

        mRecyclerViewExpandableItemManager.attachRecyclerView(mRecyclerView);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mProvider.restartLoader();
            }
        });
        mSwipeRefreshLayout.setColorSchemeResources(R.color.theme_primary_dark);
        mEmpty.setVisibility(View.INVISIBLE);
        mRecyclerView.setVisibility(View.INVISIBLE);
        mLoading.setVisibility(View.VISIBLE);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if(activity instanceof VenueActivity)
            mActivity = (VenueActivity) activity;

    }
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // save current state to support screen rotation, etc...
        if (mRecyclerViewExpandableItemManager != null) {
            outState.putParcelable(
                    SAVED_STATE_EXPANDABLE_ITEM_MANAGER,
                    mRecyclerViewExpandableItemManager.getSavedState());
        }
    }

    @Override
    public void onDestroyView() {
        if (mRecyclerViewExpandableItemManager != null) {
            mRecyclerViewExpandableItemManager.release();
            mRecyclerViewExpandableItemManager = null;
        }

        if (mRecyclerView != null) {
            mRecyclerView.setItemAnimator(null);
            mRecyclerView.setAdapter(null);
            mRecyclerView = null;
        }

        if (mWrappedAdapter != null) {
            WrapperAdapterUtils.releaseAll(mWrappedAdapter);
            mWrappedAdapter = null;
        }
        mAdapter = null;
        mLayoutManager = null;

        super.onDestroyView();
    }

    private boolean supportsViewElevation() {
        return (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP);
    }

    public EventDataProvider getDataProvider() {
        return mProvider;
    }


    @Override
    public void setColorPalette(Palette p) {

    }

    class EventDataProvider extends AbstractDataProvider {
        private List<Pair<GroupData, List<ChildData>>> mData;
        private List<EventBean> mEvents;
        // for undo group item
        private Pair<GroupData, List<ChildData>> mLastRemovedGroup;
        private int mLastRemovedGroupPosition = -1;

        // for undo child item
        private ChildData mLastRemovedChild;
        private long mLastRemovedChildParentGroupId = -1;
        private int mLastRemovedChildPosition = -1;

        public EventDataProvider() {
            mData = new LinkedList<>();
            mActivity.getLoaderManager().initLoader(1, null, eventForVenueLoader);
        }

        @Override
        public int getGroupCount() {
            return mData.size();
        }

        public void restartLoader() {
            mActivity.getLoaderManager().restartLoader(1, null, eventForVenueLoader);
        }

        @Override
        public int getChildCount(int groupPosition) {
            return mData.get(groupPosition).second.size();
        }

        @Override
        public GroupData getGroupItem(int groupPosition) {
            if (groupPosition < 0 || groupPosition >= getGroupCount()) {
                throw new IndexOutOfBoundsException("groupPosition = " + groupPosition);
            }

            return mData.get(groupPosition).first;
        }

        @Override
        public ChildData getChildItem(int groupPosition, int childPosition) {
            if (groupPosition < 0 || groupPosition >= getGroupCount()) {
                throw new IndexOutOfBoundsException("groupPosition = " + groupPosition);
            }

            final List<ChildData> children = mData.get(groupPosition).second;

            if (childPosition < 0 || childPosition >= children.size()) {
                throw new IndexOutOfBoundsException("childPosition = " + childPosition);
            }

            return children.get(childPosition);
        }

        @Override
        public void moveGroupItem(int fromGroupPosition, int toGroupPosition) {
            if (fromGroupPosition == toGroupPosition) {
                return;
            }

            final Pair<GroupData, List<ChildData>> item = mData.remove(fromGroupPosition);
            mData.add(toGroupPosition, item);
        }

        @Override
        public void moveChildItem(int fromGroupPosition, int fromChildPosition, int toGroupPosition, int toChildPosition) {
            if ((fromGroupPosition == toGroupPosition) && (fromChildPosition == toChildPosition)) {
                return;
            }

            final Pair<GroupData, List<ChildData>> fromGroup = mData.get(fromGroupPosition);
            final Pair<GroupData, List<ChildData>> toGroup = mData.get(toGroupPosition);

            final ConcreteChildData item = (ConcreteChildData) fromGroup.second.remove(fromChildPosition);

            if (toGroupPosition != fromGroupPosition) {
                // assign a new ID
                final long newId = ((ConcreteGroupData) toGroup.first).generateNewChildId();
                item.setChildId(newId);
            }

            toGroup.second.add(toChildPosition, item);
        }

        @Override
        public void removeGroupItem(int groupPosition) {
            mLastRemovedGroup = mData.remove(groupPosition);
            mLastRemovedGroupPosition = groupPosition;

            mLastRemovedChild = null;
            mLastRemovedChildParentGroupId = -1;
            mLastRemovedChildPosition = -1;
        }

        @Override
        public void removeChildItem(int groupPosition, int childPosition) {
            mLastRemovedChild = mData.get(groupPosition).second.remove(childPosition);
            mLastRemovedChildParentGroupId = mData.get(groupPosition).first.getGroupId();
            mLastRemovedChildPosition = childPosition;

            mLastRemovedGroup = null;
            mLastRemovedGroupPosition = -1;
        }


        @Override
        public long undoLastRemoval() {
            if (mLastRemovedGroup != null) {
                return undoGroupRemoval();
            } else if (mLastRemovedChild != null) {
                return undoChildRemoval();
            } else {
                return RecyclerViewExpandableItemManager.NO_EXPANDABLE_POSITION;
            }
        }

        private long undoGroupRemoval() {
            int insertedPosition;
            if (mLastRemovedGroupPosition >= 0 && mLastRemovedGroupPosition < mData.size()) {
                insertedPosition = mLastRemovedGroupPosition;
            } else {
                insertedPosition = mData.size();
            }

            mData.add(insertedPosition, mLastRemovedGroup);

            mLastRemovedGroup = null;
            mLastRemovedGroupPosition = -1;

            return RecyclerViewExpandableItemManager.getPackedPositionForGroup(insertedPosition);
        }

        private long undoChildRemoval() {
            Pair<GroupData, List<ChildData>> group = null;
            int groupPosition = -1;

            // find the group
            for (int i = 0; i < mData.size(); i++) {
                if (mData.get(i).first.getGroupId() == mLastRemovedChildParentGroupId) {
                    group = mData.get(i);
                    groupPosition = i;
                    break;
                }
            }

            if (group == null) {
                return RecyclerViewExpandableItemManager.NO_EXPANDABLE_POSITION;
            }

            int insertedPosition;
            if (mLastRemovedChildPosition >= 0 && mLastRemovedChildPosition < group.second.size()) {
                insertedPosition = mLastRemovedChildPosition;
            } else {
                insertedPosition = group.second.size();
            }

            group.second.add(insertedPosition, mLastRemovedChild);

            mLastRemovedChildParentGroupId = -1;
            mLastRemovedChildPosition = -1;
            mLastRemovedChild = null;

            return RecyclerViewExpandableItemManager.getPackedPositionForChild(groupPosition, insertedPosition);
        }








        private LoaderManager.LoaderCallbacks<HashMap<EventBean,FoursquareVenue>> eventForVenueLoader
                = new LoaderManager.LoaderCallbacks<HashMap<EventBean,FoursquareVenue>>() {

            @Override
            public void onLoadFinished(Loader<HashMap<EventBean,FoursquareVenue>> loader, HashMap<EventBean, FoursquareVenue> data) {
                Log.d(TAG, "onLoadFinished, VenueEventLoader");
                if (data != null && data.size()>0) {
                    mData = new LinkedList<>();
                    int i = 0;
                    Log.d("RewardFragment", "data found :" + mData.size());
                    Iterator it = data.entrySet().iterator();
                    while (it.hasNext()) {
                        Map.Entry pair = (Map.Entry)it.next();
                        final EventBean bean = (EventBean)pair.getKey();
                        final FoursquareVenue venue = (FoursquareVenue)pair.getValue();
                        final String groupText = bean.getDescription() + "\n" + "@ " + venue.getName();
                        final int groupSwipeReaction = RecyclerViewSwipeManager.REACTION_CAN_SWIPE_LEFT | RecyclerViewSwipeManager.REACTION_CAN_SWIPE_RIGHT;
                        final ConcreteGroupData group = new ConcreteGroupData(i, groupText, groupSwipeReaction);
                        final List<AbstractDataProvider.ChildData> children = new ArrayList<>();

                        String groups = "";
                        if(bean.getVerified()){
                            groups = getActivity().getString(R.string.everyone);
                        }else{
                            for(GroupBean g:bean.getGroups()){
                                groups+= g.getName()+", ";
                            }
                            groups = groups.substring(0,groups.length()-2);
                        }

                        children.add(new ConcreteChildData(1, "Reward: " + bean.getReward(), "At: " + venue.getName() + "\n" + venue.getAddress() + " " + venue.getCity(), "For: " + groups, venue.getId(), RecyclerViewSwipeManager.REACTION_CAN_NOT_SWIPE_BOTH));

                        mData.add(new Pair<AbstractDataProvider.GroupData, List<AbstractDataProvider.ChildData>>(group, children));
                        it.remove();
                        i++;
                    }
                    mEmpty.setVisibility(View.INVISIBLE);
                    mLoading.setVisibility(View.INVISIBLE);
                    mRecyclerView.setVisibility(View.VISIBLE);
                    mAdapter.notifyDataSetChanged();

                } else {
                    mEmpty.setVisibility(View.VISIBLE);
                    mLoading.setVisibility(View.INVISIBLE);
                    mRecyclerView.setVisibility(View.INVISIBLE);
                }
                mSwipeRefreshLayout.setRefreshing(false);
            }


            @Override
            public Loader<HashMap<EventBean,FoursquareVenue>> onCreateLoader(int id, Bundle args) {
                Log.d(TAG, "onCreateLoader");
                Log.d(TAG, "onCreateLoader venueId:"+fsVenueId);
                VenueEventsLoader loader = new VenueEventsLoader(mActivity.getApplicationContext(),fsVenueId);
                return loader;
            }

            @Override
            public void onLoaderReset(Loader<HashMap<EventBean, FoursquareVenue>> loader) {
            }
        };



    }
}
