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
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import be.ugent.vop.R;
import be.ugent.vop.backend.myApi.model.OverviewBean;
import be.ugent.vop.foursquare.FoursquareVenue;

/**
 * Provide views to RecyclerView with data from mDataSet.
 */
public class OverviewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String TAG = "CustomAdapter";
    private static final int GROUP_CARD = 0;
    private static final int VENUE_CARD = 1;
    private static final int EVENT_CARD = 2;
    private OverviewBean overview;
    private ArrayList<FoursquareVenue> fsVenues;

    public OverviewAdapter(OverviewBean overview, ArrayList<FoursquareVenue> fsVenues){
        super();
        this.overview = overview;
        this.fsVenues = fsVenues;
    }

    // BEGIN_INCLUDE(recyclerViewSampleViewHolder)
    /**
     * Provide a reference to the type of views that you are using (custom ViewHolder)
     */
    public static class VenueViewHolder extends RecyclerView.ViewHolder {
        protected TextView venue_1_info;
        protected TextView venue_2_info;
        protected TextView venue_3_info;
        protected TextView venue_1_name;
        protected TextView venue_2_name;
        protected TextView venue_3_name;
        protected ImageView icon1;
        protected ImageView icon2;
        protected ImageView icon3;

        public VenueViewHolder(View v) {
            super(v);
            venue_1_info = (TextView) v.findViewById(R.id.venue_1_info);
            venue_2_info = (TextView) v.findViewById(R.id.venue_2_info);
            venue_3_info = (TextView) v.findViewById(R.id.venue_3_info);
            venue_1_name = (TextView) v.findViewById(R.id.venue_1_name);
            venue_2_name = (TextView) v.findViewById(R.id.venue_2_name);
            venue_3_name = (TextView) v.findViewById(R.id.venue_3_name);
            icon1 = (ImageView) v.findViewById(R.id.icon1);
            icon2 = (ImageView) v.findViewById(R.id.icon2);
            icon3 = (ImageView) v.findViewById(R.id.icon3);
        }
    }
    public static class GroupViewHolder extends RecyclerView.ViewHolder {
        protected TextView group_info;
        protected TextView group_name;
        protected ImageView icon;

        public GroupViewHolder(View v) {
            super(v);
            group_info = (TextView) v.findViewById(R.id.group_info);
            group_name = (TextView) v.findViewById(R.id.group_name);
            icon = (ImageView) v.findViewById(R.id.icon);
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
                    mGroupViewHolder.group_name.setText(overview.getGroup().getName());
                }

                return mGroupViewHolder;

            case VENUE_CARD:
                v = LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.layout_card_venue, viewGroup, false);

                VenueViewHolder mVenueViewHolder =  new VenueViewHolder(v);
                if(fsVenues != null && fsVenues.size() > 2){
                    mVenueViewHolder.venue_1_name.setText(fsVenues.get(0).getName());
                    mVenueViewHolder.venue_2_name.setText(fsVenues.get(1).getName());
                    mVenueViewHolder.venue_3_name.setText(fsVenues.get(2).getName());
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
