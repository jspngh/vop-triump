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

import com.gc.materialdesign.views.ButtonFlat;

import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import be.ugent.vop.R;
import be.ugent.vop.backend.myApi.model.NewMemberInGroup;
import be.ugent.vop.backend.myApi.model.OverviewBean;
import be.ugent.vop.backend.myApi.model.OverviewReward;
import be.ugent.vop.backend.myApi.model.OverviewCheckin;
import be.ugent.vop.foursquare.FoursquareVenue;
import be.ugent.vop.foursquare.Photo;
import be.ugent.vop.ui.group.GroupActivity;
import be.ugent.vop.ui.group.GroupListActivity;
import be.ugent.vop.ui.reward.RewardsActivity;
import be.ugent.vop.ui.venue.VenueActivity;
import be.ugent.vop.utils.PrefUtils;

public class OverviewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String TAG = "CustomAdapter";
    private static final int GROUP_CARD_MEMBER = 0;
    private static final int GROUP_CARD_CHECKIN = 1;
    private static final int VENUE_CARD = 2;
    private static final int REWARD_CARD = 3;
    private static final int EVENT_CARD = 4;
    private static final int WELCOME_CARD = 5;
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
        protected TextView description;
        protected ButtonFlat claimButton;
        protected View view;

        public RewardViewHolder(View v) {
            super(v);
            view = v;
            title = (TextView) v.findViewById(R.id.card_title);
            info = (TextView) v.findViewById(R.id.reward_info);
            description = (TextView) v.findViewById(R.id.reward_description);
            claimButton = (ButtonFlat) v.findViewById(R.id.button_claim);
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
        protected ButtonFlat okButton;
        public WelcomeViewHolder(View v) {
            super(v);
            okButton = (ButtonFlat) v.findViewById(R.id.button_ok);
        }
    }

    public static class LoadingViewHolder extends RecyclerView.ViewHolder {
        public LoadingViewHolder(View v) {
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

        if (position == 0) {
            viewType = VENUE_CARD;
        } else {
            Date mostRecent = null;
            Date compare;
            if (overview.getNewMembers() != null && overview.getNewMembers().size() > 0) {
                mostRecent = new Date(overview.getNewMembers().get(0).getDate().getValue());
                viewType = GROUP_CARD_MEMBER;
            }
            if (overview.getCheckIns() != null && overview.getCheckIns().size() > 0) {
                compare = new Date(overview.getCheckIns().get(0).getDate().getValue());
                if (mostRecent != null && mostRecent.compareTo(compare) < 0) {
                    mostRecent = compare;
                    viewType = GROUP_CARD_CHECKIN;
                } else if(mostRecent == null) {
                    mostRecent = compare;
                    viewType = GROUP_CARD_CHECKIN;
                }
            }
            if (overview.getRewards() != null && overview.getRewards().size() > 0) {
                compare = new Date(overview.getRewards().get(0).getDate().getValue());
                if (mostRecent != null && mostRecent.compareTo(compare) < 0) {
                    viewType = REWARD_CARD;
                } else if(mostRecent == null) {
                    viewType = REWARD_CARD;
                }
            }
        }

        if(displayWelcome) viewType = WELCOME_CARD;
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
                    final long groupId = member.getGroupId();
                    Picasso.with(context)

                            .load(member.getMemberIconUrl())
                            .fit().centerCrop()
                            .placeholder(R.drawable.profile_default)
                            .error(R.drawable.ic_drawer_user)
                            .into(mGroupViewHolder.icon);
                    mGroupViewHolder.title.setText(member.getMemberName() + " joined one of your groups");
                    mGroupViewHolder.group_name.setText("Joined " + member.getGroupName());
                    SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm dd/MM/yyyy");
                    mGroupViewHolder.update_info.setText("at " + dateFormat.format(new Date(member.getDate().getValue())));

                    overview.getNewMembers().remove(0);
                    //newMembersLenght--;

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
                    final long groupId = checkin.getGroupId();
                    Picasso.with(context)
                            .load(checkin.getMemberIconUrl())
                            .fit().centerCrop()
                            .placeholder(R.drawable.profile_default)
                            .error(R.drawable.ic_drawer_user)
                            .into(mGroupViewHolder2.icon);
                    mGroupViewHolder2.title.setText(checkin.getMemberName() + " is at " + checkin.getVenueName());
                    if(checkin.getVenueName() != null) {
                        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
                        mGroupViewHolder2.group_name.setText("on " + dateFormat.format(new Date(checkin.getDate().getValue())));
                    } else {
                        mGroupViewHolder2.group_name.setText("");
                    }
                    mGroupViewHolder2.update_info.setText("for " + checkin.getGroupName());

                    overview.getCheckIns().remove(0);
                    //checkInLenght--;

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
                    //venue 1
                    mVenueViewHolder.venue_1_name.setText(fsVenues.get(0).getName());
                    mVenueViewHolder.venue_1_info.setText(fsVenues.get(0).getAddress());

                    if(fsVenues.get(0).getPhotos().size() > 0){
                        Photo p = fsVenues.get(0).getPhotos().get(0);
                        String url = p.getPrefix() + "200x200" + p.getSuffix();
                        Picasso.with(context)
                                .load(url)
                                .fit().centerCrop()
                                .placeholder(R.drawable.ic_launcher)
                                .error(R.drawable.ic_launcher)
                                .into(mVenueViewHolder.icon1);
                    }

                    View.OnClickListener listener = new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(context, VenueActivity.class);
                            intent.putExtra(VenueActivity.VENUE_ID, fsVenues.get(0).getId());

                            context.startActivity(intent);
                        }
                    };
                    mVenueViewHolder.venue_1_name.setOnClickListener(listener);
                    mVenueViewHolder.venue_1_info.setOnClickListener(listener);
                    mVenueViewHolder.icon1.setOnClickListener(listener);

                    //venue 2
                    mVenueViewHolder.venue_2_name.setText(fsVenues.get(1).getName());
                    mVenueViewHolder.venue_2_info.setText(fsVenues.get(1).getAddress());

                    if(fsVenues.get(1).getPhotos().size() > 0){
                        Photo p = fsVenues.get(1).getPhotos().get(0);
                        String url = p.getPrefix() + "200x200" + p.getSuffix();
                        Picasso.with(context)
                                .load(url)
                                .fit().centerCrop()
                                .placeholder(R.drawable.ic_launcher)
                                .error(R.drawable.ic_launcher)
                                .into(mVenueViewHolder.icon2);
                    }
                    listener = new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(context, VenueActivity.class);
                            intent.putExtra(VenueActivity.VENUE_ID, fsVenues.get(1).getId());

                            context.startActivity(intent);
                        }
                    };
                    mVenueViewHolder.venue_2_name.setOnClickListener(listener);
                    mVenueViewHolder.venue_2_info.setOnClickListener(listener);
                    mVenueViewHolder.icon2.setOnClickListener(listener);

                    //venue 3
                    mVenueViewHolder.venue_3_name.setText(fsVenues.get(2).getName());
                    mVenueViewHolder.venue_3_info.setText(fsVenues.get(2).getAddress());

                    if(fsVenues.get(2).getPhotos().size() > 0){
                        Photo p = fsVenues.get(2).getPhotos().get(0);
                        String url = p.getPrefix() + "200x200" + p.getSuffix();
                        Picasso.with(context)
                                .load(url)
                                .fit().centerCrop()
                                .placeholder(R.drawable.ic_launcher)
                                .error(R.drawable.ic_launcher)
                                .into(mVenueViewHolder.icon3);
                    }
                    listener = new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(context, VenueActivity.class);
                            intent.putExtra(VenueActivity.VENUE_ID, fsVenues.get(2).getId());

                            context.startActivity(intent);
                        }
                    };
                    mVenueViewHolder.venue_3_name.setOnClickListener(listener);
                    mVenueViewHolder.venue_3_info.setOnClickListener(listener);
                    mVenueViewHolder.icon3.setOnClickListener(listener);
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

                    mRewardViewHolder.title.setText("You won " + reward.getEventReward());
                    SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm dd/MM/yyyy");
                    if(reward.getVenueName() != null) {
                        mRewardViewHolder.info.setText("Received at " + reward.getVenueName() + " on " + dateFormat.format(new Date(reward.getDate().getValue())));
                    } else {
                        mRewardViewHolder.info.setText("Received at " + dateFormat.format(new Date(reward.getDate().getValue())));
                    }
                    mRewardViewHolder.description.setText("for event: " + reward.getEventDescription());
                    overview.getRewards().remove(0);
                    //rewardsLenght--;
                    mRewardViewHolder.claimButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(context, RewardsActivity.class);
                            context.startActivity(intent);
                        }
                    });
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

            case WELCOME_CARD:
                if(darkTheme){
                    v = LayoutInflater.from(viewGroup.getContext())
                            .inflate(R.layout.layout_card_welcome_dark, viewGroup, false);
                } else {
                    v = LayoutInflater.from(viewGroup.getContext())
                            .inflate(R.layout.layout_card_welcome, viewGroup, false);
                }
                WelcomeViewHolder mWelcomeViewHolder = new WelcomeViewHolder(v);
                mWelcomeViewHolder.okButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(context, GroupListActivity.class);
                        context.startActivity(intent);
                    }
                });
                return mWelcomeViewHolder;

            default:

                if(darkTheme){
                    v = LayoutInflater.from(viewGroup.getContext())
                            .inflate(R.layout.layout_card_loading_dark, viewGroup, false);
                } else {
                    v = LayoutInflater.from(viewGroup.getContext())
                            .inflate(R.layout.layout_card_loading, viewGroup, false);
                }
                return new LoadingViewHolder(v);
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
