/*
 *    Copyright (C) 2015 Haruki Hasegawa
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package be.ugent.vop.ui.reward;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.Context;
import android.content.Loader;
import android.graphics.drawable.NinePatchDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import be.ugent.vop.R;
import be.ugent.vop.backend.loaders.EventLoader;
import be.ugent.vop.backend.myApi.model.EventBean;
import be.ugent.vop.backend.myApi.model.EventRewardBean;

import com.h6ah4i.android.widget.advrecyclerview.animator.GeneralItemAnimator;
import com.h6ah4i.android.widget.advrecyclerview.animator.SwipeDismissItemAnimator;
import com.h6ah4i.android.widget.advrecyclerview.decoration.ItemShadowDecorator;
import com.h6ah4i.android.widget.advrecyclerview.decoration.SimpleListDividerDecorator;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.RecyclerViewSwipeManager;
import com.h6ah4i.android.widget.advrecyclerview.touchguard.RecyclerViewTouchActionGuardManager;
import com.h6ah4i.android.widget.advrecyclerview.utils.WrapperAdapterUtils;

import java.util.LinkedList;
import java.util.List;

public class RewardListViewFragment extends Fragment {
    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private RewardDataProvider mProvider;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.Adapter mWrappedAdapter;
    private RecyclerViewSwipeManager mRecyclerViewSwipeManager;
    private RecyclerViewTouchActionGuardManager mRecyclerViewTouchActionGuardManager;
    protected SwipeRefreshLayout mSwipeRefreshLayout;
    private Activity activity;
    public RewardListViewFragment() {
        super();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_rewards, container, false);
        activity=this.getActivity();
        mProvider = new RewardDataProvider(); // true: example test data
        mSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.fragment_group_swipe_refresh);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //noinspection ConstantConditions
        mRecyclerView = (RecyclerView) getView().findViewById(R.id.recycler_view);
        mLayoutManager = new LinearLayoutManager(getActivity());
        // touch guard manager  (this class is required to suppress scrolling while swipe-dismiss animation is running)
        mRecyclerViewTouchActionGuardManager = new RecyclerViewTouchActionGuardManager();
        mRecyclerViewTouchActionGuardManager.setInterceptVerticalScrollingWhileAnimationRunning(true);
        mRecyclerViewTouchActionGuardManager.setEnabled(true);

        // swipe manager
        mRecyclerViewSwipeManager = new RecyclerViewSwipeManager();

        //adapter
        final MySwipeableItemAdapter myItemAdapter = new MySwipeableItemAdapter(getDataProvider());
        myItemAdapter.setEventListener(new MySwipeableItemAdapter.EventListener() {
            @Override
            public void onItemRemoved(int position) {
                ((RewardsActivity) getActivity()).onItemRemoved(position);
            }

            @Override
            public void onItemPinned(int position) {
                ((RewardsActivity) getActivity()).onItemPinned(position);
            }

            @Override
            public void onItemViewClicked(View v, boolean pinned) {
                onItemViewClick(v, pinned);
            }
        });

        mAdapter = myItemAdapter;

        mWrappedAdapter = mRecyclerViewSwipeManager.createWrappedAdapter(myItemAdapter);      // wrap for swiping

        final GeneralItemAnimator animator = new SwipeDismissItemAnimator();

        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mWrappedAdapter);  // requires *wrapped* adapter
        mRecyclerView.setItemAnimator(animator);

        // additional decorations
        //noinspection StatementWithEmptyBody
        if (supportsViewElevation()) {
            // Lollipop or later has native drop shadow feature. ItemShadowDecorator is not required.
        } else {
            mRecyclerView.addItemDecoration(new ItemShadowDecorator((NinePatchDrawable) getResources().getDrawable(R.drawable.material_shadow_z1)));
        }
        mRecyclerView.addItemDecoration(new SimpleListDividerDecorator(getResources().getDrawable(R.drawable.list_divider), true));

        // NOTE:
        // The initialization order is very important! This order determines the priority of touch event handling.
        //
        // priority: TouchActionGuard > Swipe > DragAndDrop
        mRecyclerViewTouchActionGuardManager.attachRecyclerView(mRecyclerView);
        mRecyclerViewSwipeManager.attachRecyclerView(mRecyclerView);

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mProvider.restartLoader();
            }
        });
        mSwipeRefreshLayout.setColorSchemeResources(R.color.theme_primary_dark);
    }

    @Override
    public void onDestroyView() {
        if (mRecyclerViewSwipeManager != null) {
            mRecyclerViewSwipeManager.release();
            mRecyclerViewSwipeManager = null;
        }

        if (mRecyclerViewTouchActionGuardManager != null) {
            mRecyclerViewTouchActionGuardManager.release();
            mRecyclerViewTouchActionGuardManager = null;
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

    private void onItemViewClick(View v, boolean pinned) {
        int position = mRecyclerView.getChildPosition(v);
        if (position != RecyclerView.NO_POSITION) {
            ((RewardsActivity) getActivity()).onItemClicked(position);
        }
    }

    private boolean supportsViewElevation() {
        return (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP);
    }

    public RewardDataProvider getDataProvider() {
        return mProvider;
    }

    public void notifyItemChanged(int position) {
        mAdapter.notifyItemChanged(position);
    }

    public void notifyItemInserted(int position) {
        mAdapter.notifyItemInserted(position);
        mRecyclerView.scrollToPosition(position);
    }

    class RewardDataProvider extends AbstractDataProvider {
        private List<ConcreteData> mData;
        private List<EventBean> mRewards;
        private ConcreteData mLastRemovedData;
        private int mLastRemovedPosition = -1;

        public RewardDataProvider() {
            mData = new LinkedList<>();
            activity.getLoaderManager().initLoader(1, null, mEventLoaderListener);
        }

        @Override
        public int getCount() {
            return mData.size();
        }

        public void restartLoader(){
            activity.getLoaderManager().restartLoader(1, null, mEventLoaderListener);
        }

        @Override
        public Data getItem(int index) {
            if (index < 0 || index >= getCount()) {
                throw new IndexOutOfBoundsException("index = " + index);
            }

            return mData.get(index);
        }

        @Override
        public int undoLastRemoval() {
            if (mLastRemovedData != null) {
                int insertedPosition;
                if (mLastRemovedPosition >= 0 && mLastRemovedPosition < mData.size()) {
                    insertedPosition = mLastRemovedPosition;
                } else {
                    insertedPosition = mData.size();
                }

                mData.add(insertedPosition, mLastRemovedData);

                mLastRemovedData = null;
                mLastRemovedPosition = -1;

                return insertedPosition;
            } else {
                return -1;
            }
        }

        @Override
        public void moveItem(int fromPosition, int toPosition) {
            if (fromPosition == toPosition) {
                return;
            }

            final ConcreteData item = mData.remove(fromPosition);

            mData.add(toPosition, item);
            mLastRemovedPosition = -1;
        }

        @Override
        public void removeItem(int position) {
            //noinspection UnnecessaryLocalVariable
            final ConcreteData removedItem = mData.remove(position);

            mLastRemovedData = removedItem;
            mLastRemovedPosition = position;
        }

        public final class ConcreteData extends Data {

            private final long mId;
            private final String mText;
            private final int mViewType;
            private final int mSwipeReaction;
            private boolean mPinnedToSwipeLeft;
            private final EventBean mBean;

            ConcreteData(long id, int viewType, EventBean bean, int swipeReaction) {
                mId = id;
                mViewType = viewType;
                mBean = bean;
                mText = makeText(id, bean);
                mSwipeReaction = swipeReaction;
            }

            private String makeText(long id, EventBean bean) {
                final StringBuilder sb = new StringBuilder();

                sb.append(bean.getReward());
                sb.append(" - ");
                sb.append(bean.getDescription());

                return sb.toString();
            }

            @Override
            public boolean isSectionHeader() {
                return false;
            }

            @Override
            public int getViewType() {
                return mViewType;
            }

            public EventBean getBean() {
                return mBean;
            }

            @Override
            public long getId() {
                return mId;
            }

            @Override
            public String toString() {
                return mText;
            }

            @Override
            public int getSwipeReactionType() {
                return mSwipeReaction;
            }

            @Override
            public String getText() {
                return mText;
            }

            @Override
            public boolean isPinnedToSwipeLeft() {
                return mPinnedToSwipeLeft;
            }

            @Override
            public void setPinnedToSwipeLeft(boolean pinedToSwipeLeft) {
                mPinnedToSwipeLeft = pinedToSwipeLeft;
            }
        }



        private LoaderManager.LoaderCallbacks<EventRewardBean> mEventLoaderListener
                = new LoaderManager.LoaderCallbacks<EventRewardBean>() {
            @Override
            public Loader<EventRewardBean> onCreateLoader(int id, Bundle args) {
                Log.d("RewardFragment", "onCreateLoader");
                EventLoader loader = new EventLoader(activity.getApplicationContext());
                return loader;
            }

            @Override
            public void onLoadFinished(Loader<EventRewardBean> loader, EventRewardBean data) {
                Log.d("RewardFragment", "onLoadFinished");
                if (data != null) {
                    mRewards = data.getRewards();
                    if(mRewards!=null){
                    Log.d("RewardFragment", "size " + mRewards.size() );
                        mData = new LinkedList<>();
                    for (int j = 0; j < mRewards.size(); j++) {
                        final long id = mData.size();
                        final int viewType = 0;
                        final EventBean bean = mRewards.get(j);
                        final int swipeReaction = RecyclerViewSwipeManager.REACTION_CAN_SWIPE_LEFT;
                        mData.add(new ConcreteData(id, viewType, bean, swipeReaction));
                        Log.d("RewardFragment", "added data ");

                    }}

                    mAdapter.notifyDataSetChanged();

                }
                mSwipeRefreshLayout.setRefreshing(false);

            }


            @Override
            public void onLoaderReset(Loader<EventRewardBean> loader) {

            }
        };
    }



}
