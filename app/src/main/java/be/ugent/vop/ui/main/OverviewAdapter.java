/*
* Copyright (C) 2014 The Android Open Source Project
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package be.ugent.vop.ui.main;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import be.ugent.vop.R;
import be.ugent.vop.backend.myApi.model.OverviewBean;

/**
 * Provide views to RecyclerView with data from mDataSet.
 */
public class OverviewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String TAG = "CustomAdapter";
    private static final int GROUP_CARD = 0;
    private static final int VENUE_CARD = 1;
    private static final int EVENT_CARD = 2;
    private OverviewBean overview;

    public OverviewAdapter(OverviewBean overview){
        super();
        this.overview = overview;
    }

    // BEGIN_INCLUDE(recyclerViewSampleViewHolder)
    /**
     * Provide a reference to the type of views that you are using (custom ViewHolder)
     */
    public static class VenueViewHolder extends RecyclerView.ViewHolder {
        protected TextView info;
        public VenueViewHolder(View v) {
            super(v);
            info = (TextView) v.findViewById(R.id.venue_info);
        }
    }
    public static class GroupViewHolder extends RecyclerView.ViewHolder {
        protected TextView info;
        public GroupViewHolder(View v) {
            super(v);
            info = (TextView) v.findViewById(R.id.group_info);
        }
    }
    public static class EventViewHolder extends RecyclerView.ViewHolder {
        protected TextView info;
        public EventViewHolder(View v) {
            super(v);
            info = (TextView) v.findViewById(R.id.event_info);
        }
    }
    public static class WelcomeViewHolder extends RecyclerView.ViewHolder {
        public WelcomeViewHolder(View v) {
            super(v);
        }
    }
    // END_INCLUDE(recyclerViewSampleViewHolder)

    // BEGIN_INCLUDE(recyclerViewOnCreateViewHolder)
    // Create new views (invoked by the layout manager)
    @Override
    public int getItemViewType(int position) {

        return position;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view.
        View v;
        switch(viewType){
            case GROUP_CARD:
                v = LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.layout_card_group, viewGroup, false);
                GroupViewHolder mGroupViewHolder = new GroupViewHolder(v);
                if(overview != null && overview.getGroup() != null) {
                    mGroupViewHolder.info.setText(overview.getGroup().getName());
                }

                return mGroupViewHolder;

            case VENUE_CARD:
                v = LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.layout_card_venue, viewGroup, false);

                VenueViewHolder mVenueViewHolder =  new VenueViewHolder(v);
                if(overview.getVenues() == null) Log.d("Overview", "venues == null");
                if(overview != null && overview.getVenues() != null && overview.getVenues().get(0) != null) {
                    mVenueViewHolder.info.setText(overview.getVenues().get(0).getName());
                }

                return mVenueViewHolder;

            case EVENT_CARD:
                v = LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.layout_card_event, viewGroup, false);

                EventViewHolder mEventViewHolder =  new EventViewHolder(v);

                return mEventViewHolder;

            default:
                v = LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.layout_card_welcome, viewGroup, false);
                return new WelcomeViewHolder(v);
        }
    }
    // END_INCLUDE(recyclerViewOnCreateViewHolder)

    // BEGIN_INCLUDE(recyclerViewOnBindViewHolder)
    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, final int position) {
        Log.d(TAG, "Element " + position + " set.");
    }
    // END_INCLUDE(recyclerViewOnBindViewHolder)

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return 4;
    }
}
