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

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.koushikdutta.ion.Ion;

import java.util.ArrayList;

import be.ugent.vop.R;
import be.ugent.vop.backend.myApi.model.CheckinBean;
import be.ugent.vop.backend.myApi.model.NewMemberInGroup;
import be.ugent.vop.backend.myApi.model.OverviewBean;
import be.ugent.vop.backend.myApi.model.OverviewReward;
import be.ugent.vop.backend.myApi.model.OverviewCheckin;
import be.ugent.vop.foursquare.FoursquareVenue;
import be.ugent.vop.foursquare.Photo;
import be.ugent.vop.ui.group.GroupActivity;
import be.ugent.vop.ui.venue.VenueActivity;
import be.ugent.vop.utils.PrefUtils;

public class OverviewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String TAG = "CustomAdapter";
    private static final int GROUP_CARD_MEMBER = 0;
    private static final int GROUP_CARD_CHECKIN = 1;
    private static final int VENUE_CARD = 2;
    private static final int REWARD_CARD = 3;
    private static final int EVENT_CARD = 4;
    private OverviewBean overview;
    private ArrayList<FoursquareVenue> fsVenues;
    private Context context;
    private boolean displayWelcome;
    private int newMembersLenght;
    private int checkInLenght;
    private int rewardsLenght;

    public OverviewAdapter(OverviewBean overview, ArrayList<FoursquareVenue> fsVenues, Context context, boolean displayWelcome){
        super();
        this.overview = overview;
        this.fsVenues = fsVenues;
        this.context = context;
        this.displayWelcome = displayWelcome;
        if(overview != null){
            newMembersLenght = (overview.getNewMembers() == null ? 0 : overview.getNewMembers().size());
            checkInLenght = (overview.getCheckIns() == null ? 0 : overview.getCheckIns().size());
            rewardsLenght = (overview.getRewards() == null ? 0 : overview.getRewards().size());
        } else {
            newMembersLenght = checkInLenght = rewardsLenght = 0;
        }

    }

    /**
     * Provide a reference to the type of views that you are using (custom ViewHolder)
     */
    public static class VenueViewHolder extends RecyclerView.ViewHolder {
        protected TextView title;
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
            title = (TextView) v.findViewById(R.id.card_title);
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
        protected TextView title;
        protected TextView update_info;
        protected TextView group_name;
        protected ImageView icon;
        protected View view;

        public GroupViewHolder(View v) {
            super(v);
            view = v;
            title = (TextView) v.findViewById(R.id.card_title);
            update_info = (TextView) v.findViewById(R.id.update_info);
            group_name = (TextView) v.findViewById(R.id.group_name);
            icon = (ImageView) v.findViewById(R.id.user_icon);
        }
    }
    public static class RewardViewHolder extends RecyclerView.ViewHolder {
        protected TextView title;
        protected TextView info;
        protected TextView venue;
        protected View view;

        public RewardViewHolder(View v) {
            super(v);
            view = v;
            title = (TextView) v.findViewById(R.id.card_title);
            info = (TextView) v.findViewById(R.id.reward_info);
            venue = (TextView) v.findViewById(R.id.reward_venue);
        }
    }

    public static class EventViewHolder extends RecyclerView.ViewHolder {
        protected TextView title;
        protected TextView info;

        public EventViewHolder(View v) {
            super(v);
            title = (TextView) v.findViewById(R.id.card_title);
            info = (TextView) v.findViewById(R.id.event_info);
        }
    }

    public static class WelcomeViewHolder extends RecyclerView.ViewHolder {
        public WelcomeViewHolder(View v) {
            super(v);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int position) {
        int viewType = -1;
        View v;

        boolean darkTheme = false;
        if(context != null) darkTheme = PrefUtils.getDarkTheme(context);

        if(position == 0) {
            viewType = VENUE_CARD;
        } else {
            if(overview.getNewMembers() != null && overview.getNewMembers().size() > 0){
                viewType = GROUP_CARD_MEMBER;
            } else if(overview.getCheckIns() != null && overview.getCheckIns().size() > 0){
                viewType = GROUP_CARD_CHECKIN;
            } else if(overview.getRewards() != null && overview.getRewards().size() > 0){
                viewType = REWARD_CARD;
            }
        }

        if(displayWelcome) viewType = -1;
        switch(viewType){
            case GROUP_CARD_MEMBER:

                if(darkTheme){
                    v = LayoutInflater.from(viewGroup.getContext())
                            .inflate(R.layout.layout_card_group_dark, viewGroup, false);
                } else {
                    v = LayoutInflater.from(viewGroup.getContext())
                            .inflate(R.layout.layout_card_group, viewGroup, false);
                }
                GroupViewHolder mGroupViewHolder = new GroupViewHolder(v);

                if(overview != null && overview.getNewMembers() != null) {
                    NewMemberInGroup member = overview.getNewMembers().get(0);
                    final long groupId = member.getGroup().getGroupId();

                    mGroupViewHolder.title.setText(member.getMember().getFirstName() + " just joined one of your groups!");
                    Ion.with(mGroupViewHolder.icon)
                            .placeholder(R.drawable.ic_launcher)
                            .error(R.drawable.ic_drawer_logout)
                            .load(member.getMember().getProfilePictureUrl());
                    mGroupViewHolder.group_name.setText("Group in common:" + member.getGroup().getName());
                    mGroupViewHolder.update_info.setText("Joined at " + member.getDate());
                    overview.getNewMembers().remove(0);
                    mGroupViewHolder.view.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Bundle bundle = new Bundle();
                            bundle.putLong("groupId", groupId);
                            Intent intent = new Intent(context, GroupActivity.class);
                            intent.putExtras(bundle);
                            context.startActivity(intent);
                        }
                    });
                }

                return mGroupViewHolder;

            case GROUP_CARD_CHECKIN:

                if(darkTheme){
                    v = LayoutInflater.from(viewGroup.getContext())
                            .inflate(R.layout.layout_card_group_dark, viewGroup, false);
                } else {
                    v = LayoutInflater.from(viewGroup.getContext())
                            .inflate(R.layout.layout_card_group, viewGroup, false);
                }
                GroupViewHolder mGroupViewHolder2 = new GroupViewHolder(v);

                if(overview != null && overview.getCheckIns() != null) {
                    OverviewCheckin checkin = overview.getCheckIns().get(0);
                    final long groupId = checkin.getCheckin().getGroupId();

                    mGroupViewHolder2.title.setText(checkin.getCheckinUser().getFirstName() + " " + checkin.getCheckinUser().getLastName() + " checked in");
                    if(checkin.getVenueName() != null) {
                        mGroupViewHolder2.group_name.setText("at " + checkin.getVenueName());
                    } else {
                        mGroupViewHolder2.group_name.setText("");
                    }
                    mGroupViewHolder2.update_info.setText("for " + checkin.getCheckinGroup().getName());
                    overview.getCheckIns().remove(0);
                    mGroupViewHolder2.view.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Bundle bundle = new Bundle();
                            bundle.putLong("groupId", groupId);
                            Intent intent = new Intent(context, GroupActivity.class);
                            intent.putExtras(bundle);
                            context.startActivity(intent);
                        }
                    });
                }

                return mGroupViewHolder2;

            case VENUE_CARD:

                if(darkTheme){
                    v = LayoutInflater.from(viewGroup.getContext())
                            .inflate(R.layout.layout_card_venue_dark, viewGroup, false);
                } else {
                    v = LayoutInflater.from(viewGroup.getContext())
                            .inflate(R.layout.layout_card_venue, viewGroup, false);
                }

                VenueViewHolder mVenueViewHolder =  new VenueViewHolder(v);

                if(fsVenues != null && fsVenues.size() > 2){
                    mVenueViewHolder.venue_1_name.setText(fsVenues.get(0).getName());
                    mVenueViewHolder.venue_1_info.setText(fsVenues.get(0).getAddress());

                    if(fsVenues.get(0).getPhotos().size() > 0){
                        Photo p = fsVenues.get(0).getPhotos().get(0);
                        String url = p.getPrefix() + "200x200" + p.getSuffix();
                        Ion.with(mVenueViewHolder.icon1)
                                .placeholder(R.drawable.ic_launcher)
                                .error(R.drawable.ic_drawer_logout)
                                .load(url);
                    }

                    mVenueViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(context, VenueActivity.class);
                            intent.putExtra(VenueActivity.VENUE_ID, fsVenues.get(0).getId());

                            context.startActivity(intent);
                        }
                    });

                    mVenueViewHolder.venue_2_name.setText(fsVenues.get(1).getName());
                    mVenueViewHolder.venue_2_info.setText(fsVenues.get(1).getAddress());

                    if(fsVenues.get(1).getPhotos().size() > 0){
                        Photo p = fsVenues.get(1).getPhotos().get(0);
                        String url = p.getPrefix() + "200x200" + p.getSuffix();
                        Ion.with(mVenueViewHolder.icon2)
                                .placeholder(R.drawable.ic_launcher)
                                .error(R.drawable.ic_drawer_logout)
                                .load(url);
                    }
                    mVenueViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(context, VenueActivity.class);
                            intent.putExtra(VenueActivity.VENUE_ID, fsVenues.get(1).getId());

                            context.startActivity(intent);
                        }
                    });

                    mVenueViewHolder.venue_3_name.setText(fsVenues.get(2).getName());

                    mVenueViewHolder.venue_3_info.setText(fsVenues.get(2).getAddress());

                    if(fsVenues.get(2).getPhotos().size() > 0){
                        Photo p = fsVenues.get(2).getPhotos().get(0);
                        String url = p.getPrefix() + "200x200" + p.getSuffix();
                        Ion.with(mVenueViewHolder.icon3)
                                .placeholder(R.drawable.ic_launcher)
                                .error(R.drawable.ic_drawer_logout)
                                .load(url);
                    }

                    mVenueViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(context, VenueActivity.class);
                            intent.putExtra(VenueActivity.VENUE_ID, fsVenues.get(2).getId());

                            context.startActivity(intent);
                        }
                    });
                }

                return mVenueViewHolder;

            case REWARD_CARD:

                if(darkTheme){
                    v = LayoutInflater.from(viewGroup.getContext())
                            .inflate(R.layout.layout_card_reward_dark, viewGroup, false);
                } else {
                    v = LayoutInflater.from(viewGroup.getContext())
                            .inflate(R.layout.layout_card_reward, viewGroup, false);
                }
                RewardViewHolder mRewardViewHolder = new RewardViewHolder(v);

                if(overview != null && overview.getRewards() != null) {
                    OverviewReward reward = overview.getRewards().get(0);

                    mRewardViewHolder.title.setText("Reward received!");
                    mRewardViewHolder.info.setText("Received at " + reward.getDate());
                    mRewardViewHolder.venue.setText(reward.getEvent().getReward());
                    overview.getRewards().remove(0);
                    /*mRewardViewHolder.view.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                        }
                    });*/
                }

                return mRewardViewHolder;

            case EVENT_CARD:

                if(darkTheme){
                    v = LayoutInflater.from(viewGroup.getContext())
                            .inflate(R.layout.layout_card_event_dark, viewGroup, false);
                } else {
                    v = LayoutInflater.from(viewGroup.getContext())
                            .inflate(R.layout.layout_card_event, viewGroup, false);
                }

                return new EventViewHolder(v);

            default:

                if(darkTheme){
                    v = LayoutInflater.from(viewGroup.getContext())
                            .inflate(R.layout.layout_card_welcome_dark, viewGroup, false);
                } else {
                    v = LayoutInflater.from(viewGroup.getContext())
                            .inflate(R.layout.layout_card_welcome, viewGroup, false);
                }
                return new WelcomeViewHolder(v);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, final int position) {
        Log.d(TAG, "Element " + position + " set.");
    }

    @Override
    public int getItemCount() {
        if(displayWelcome) return 1;
        if(overview != null) return 1 + newMembersLenght + checkInLenght + rewardsLenght;
        return 0;
    }
}
