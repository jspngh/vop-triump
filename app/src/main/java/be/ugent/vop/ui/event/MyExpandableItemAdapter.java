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

package be.ugent.vop.ui.event;

import android.content.Context;
import android.content.Intent;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import be.ugent.vop.R;
import be.ugent.vop.ui.venue.VenueActivity;

import com.gc.materialdesign.views.ButtonFlat;
import com.h6ah4i.android.widget.advrecyclerview.expandable.RecyclerViewExpandableItemManager;
import com.h6ah4i.android.widget.advrecyclerview.utils.AbstractExpandableItemAdapter;
import com.h6ah4i.android.widget.advrecyclerview.utils.AbstractExpandableItemViewHolder;

public class MyExpandableItemAdapter extends AbstractExpandableItemAdapter<MyExpandableItemAdapter.MyGroupViewHolder, MyExpandableItemAdapter.MyChildViewHolder> {
    private static final String TAG = "MyExpandableItemAdapter";

    private AbstractDataProvider mProvider;
    private Context mContext;
    private boolean mHeaderPlaceholder;

    public static class MyGroupViewHolder extends AbstractExpandableItemViewHolder {
        public ViewGroup mContainer;
        public TextView mTitle;
        public View mDragHandle;
        public View cardLayout;

        public MyGroupViewHolder(View v, boolean bind) {
            super(v);
            if(bind){
                mContainer = (ViewGroup) v.findViewById(R.id.container);
                mDragHandle = v.findViewById(R.id.drag_handle);
                mTitle = (TextView) v.findViewById(R.id.event_title);
                cardLayout = v.findViewById(R.id.card_layout);
                // hide the drag handle
                mDragHandle.setVisibility(View.GONE);
            }
        }
    }

    public static class MyChildViewHolder extends AbstractExpandableItemViewHolder {
        public TextView groups;
        public TextView reward;
        public TextView info;
        public ButtonFlat toEvent;

        public MyChildViewHolder(View v) {
            super(v);
            groups = (TextView) v.findViewById(R.id.event_groups);
            reward = (TextView) v.findViewById(R.id.event_reward);
            info = (TextView) v.findViewById(R.id.event_info);
            toEvent = (ButtonFlat) v.findViewById(R.id.button_to_event);
        }
    }

    public MyExpandableItemAdapter(AbstractDataProvider dataProvider, Context context, boolean headerPlaceholder) {
        mProvider = dataProvider;
        mContext = context;
        mHeaderPlaceholder = headerPlaceholder;
        // ExpandableItemAdapter requires stable ID, and also
        // have to implement the getGroupItemId()/getChildItemId() methods appropriately.
        setHasStableIds(true);
    }

    @Override
    public int getGroupCount() {
        return mHeaderPlaceholder? mProvider.getGroupCount() + 1 : mProvider.getGroupCount();
    }

    @Override
    public int getChildCount(int groupPosition) {
        if(mHeaderPlaceholder && groupPosition == 0)
            return 0;
        else if(mHeaderPlaceholder)
            return mProvider.getChildCount(groupPosition - 1);
        else
            return mProvider.getChildCount(groupPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        if(mHeaderPlaceholder && groupPosition == 0)
            return -1;
        else if(mHeaderPlaceholder)
            return mProvider.getGroupItem(groupPosition - 1).getGroupId();
        else
            return mProvider.getGroupItem(groupPosition).getGroupId();
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        if(mHeaderPlaceholder)
            return mProvider.getChildItem(groupPosition - 1, childPosition).getChildId();
        else
            return mProvider.getChildItem(groupPosition, childPosition).getChildId();
    }

    @Override
    public int getGroupItemViewType(int groupPosition) {
        if(mHeaderPlaceholder && groupPosition == 0)
            return 1;
        else
            return 0;
    }

    @Override
    public int getChildItemViewType(int groupPosition, int childPosition) {
        return 0;
    }

    @Override
    public MyGroupViewHolder onCreateGroupViewHolder(ViewGroup parent, int viewType) {
        final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if(viewType == 0){
            final View v = inflater.inflate(R.layout.list_group_item, parent, false);
            return new MyGroupViewHolder(v, true);
        }else{
            final View v = inflater.inflate(R.layout.view_header_placeholder, parent, false);
            return new MyGroupViewHolder(v, false);
        }
    }

    @Override
    public MyChildViewHolder onCreateChildViewHolder(ViewGroup parent, int viewType) {
        final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        final View v = inflater.inflate(R.layout.list_item, parent, false);
        return new MyChildViewHolder(v);
    }

    @Override
    public void onBindGroupViewHolder(MyGroupViewHolder holder, int groupPosition, int viewType) {
        if(viewType == 0){
            // child item

            int groupPos = mHeaderPlaceholder? groupPosition - 1 : groupPosition;

            final AbstractDataProvider.GroupData item = mProvider.getGroupItem(groupPos);

            // set text
            holder.mTitle.setText(item.getTitle());

            // mark as clickable
            holder.itemView.setClickable(true);

            final int expandState = holder.getExpandStateFlags();

            if ((expandState & RecyclerViewExpandableItemManager.STATE_FLAG_IS_UPDATED) != 0) {
                int bgResId;

                if ((expandState & RecyclerViewExpandableItemManager.STATE_FLAG_IS_EXPANDED) != 0) {
                    bgResId = R.drawable.bg_group_item_expanded_state;
                } else {
                    bgResId = R.drawable.bg_group_item_normal_state;
                }
                holder.cardLayout.setBackgroundResource(bgResId);
            }
        }
    }

    @Override
    public void onBindChildViewHolder(MyChildViewHolder holder, int groupPosition, int childPosition, int viewType) {
        // group item

        int groupPos = mHeaderPlaceholder? groupPosition - 1 : groupPosition;
        final AbstractDataProvider.ChildData itwem = mProvider.getChildItem(groupPos, childPosition);

        // set text
        holder.reward.setText(item.getReward());
        holder.info.setText(item.getInfo());
        holder.groups.setText(item.getGroups());
        holder.toEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, VenueActivity.class);
                intent.putExtra(VenueActivity.VENUE_ID, item.getVenueId());
                mContext.startActivity(intent);
            }
        });
    }

    @Override
    public boolean onCheckCanExpandOrCollapseGroup(MyGroupViewHolder holder, int groupPosition, int x, int y, boolean expand) {
        if(mHeaderPlaceholder && groupPosition == 0)
            return false;

        int groupPos = mHeaderPlaceholder? groupPosition - 1 : groupPosition;

        // check the item is *not* pinned
        if (mProvider.getGroupItem(groupPos).isPinnedToSwipeLeft()) {
            // return false to raise View.OnClickListener#onClick() event
            return false;
        }

        // check is enabled
        if (!(holder.itemView.isEnabled() && holder.itemView.isClickable())) {
            return false;
        }

        final View containerView = holder.mContainer;
        final View dragHandleView = holder.mDragHandle;

        final int offsetX = containerView.getLeft() + (int) (ViewCompat.getTranslationX(containerView) + 0.5f);
        final int offsetY = containerView.getTop() + (int) (ViewCompat.getTranslationY(containerView) + 0.5f);

        return !hitTest(dragHandleView, x - offsetX, y - offsetY);
    }

    public static boolean hitTest(View v, int x, int y) {
        final int tx = (int) (ViewCompat.getTranslationX(v) + 0.5f);
        final int ty = (int) (ViewCompat.getTranslationY(v) + 0.5f);
        final int left = v.getLeft() + tx;
        final int right = v.getRight() + tx;
        final int top = v.getTop() + ty;
        final int bottom = v.getBottom() + ty;

        return (x >= left) && (x <= right) && (y >= top) && (y <= bottom);
    }
}
