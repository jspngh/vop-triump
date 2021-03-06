package be.ugent.vop.ui.group;

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


import com.squareup.picasso.Picasso;

import java.security.acl.Group;
import java.util.ArrayList;
import java.util.Random;

import be.ugent.vop.R;
import be.ugent.vop.backend.myApi.model.GroupBean;
import be.ugent.vop.backend.myApi.model.VenueBean;
import be.ugent.vop.foursquare.FoursquareVenue;
import be.ugent.vop.utils.PrefUtils;

/**
 * Created by siebe on 02/03/15.
 */
public class GroupListAdapter extends RecyclerView.Adapter<GroupListAdapter.ViewHolder> {
    private static final String TAG = "GroupListAdapter";
    private ArrayList<GroupBean> groups;
    private ArrayList<GroupBean> visibleGroups;
    private Context context;

    public void setGroups(ArrayList<GroupBean> groups) {
        this.groups = groups;
        this.visibleGroups=groups;
    }
    public void setContext(Context context) {
        this.context = context;
    }

    public void clearFilter(){
        visibleGroups=groups;
        notifyDataSetChanged();
    }


    public void setFilter(String filter) {
        visibleGroups=new ArrayList<>();
        for (GroupBean item: groups) {
            if (item.getName().toLowerCase().contains(filter))
                visibleGroups.add(item);
        }
        notifyDataSetChanged();
    }

    // BEGIN_INCLUDE(recyclerViewSampleViewHolder)
    /**
     * Provide a reference to the type of views that you are using (custom ViewHolder)
     */
    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public ImageView groupImage;
        public TextView groupName;
        public TextView groupInfo;
        public IMyViewHolderClicks mListener;

        public ViewHolder(View v, IMyViewHolderClicks listener) {
            super(v);
            groupImage = (ImageView) v.findViewById(R.id.group_image);
            groupName = (TextView) v.findViewById(R.id.group_name);
            groupInfo = (TextView) v.findViewById(R.id.group_info);
            mListener = listener;

            v.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            mListener.onItemClick(view, getPosition());
        }

        public static interface IMyViewHolderClicks {
            public void onItemClick(View caller, int position);
        }

    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View v;
        if(context != null && PrefUtils.getDarkTheme(context)){
            v = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.group_list_item_dark, viewGroup, false);
        } else {
            v = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.group_list_item, viewGroup, false);
        }

        ViewHolder vh = new ViewHolder(v, new ViewHolder.IMyViewHolderClicks() {
            @Override
            public void onItemClick(View caller, int position) {
                Long groupId = visibleGroups.get(position).getGroupId();
                Bundle bundle = new Bundle();
                bundle.putLong("groupId", groupId);
                Intent intent = new Intent(context, GroupActivity.class);
                intent.putExtras(bundle);
                context.startActivity(intent);
                //this.getActivity().finish();

                // TODO: Start new fragment showing details for this venue
                Log.d("GroupListAdapter", "Showing details for group " + groupId);
            }
        });

        return vh;
    }
    // END_INCLUDE(recyclerViewOnCreateViewHolder)

    // BEGIN_INCLUDE(recyclerViewOnBindViewHolder)
    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        GroupBean group = visibleGroups.get(position);

        viewHolder.groupName.setText(group.getName());
        viewHolder.groupInfo.setText(group.getDescription());
        String photoUrl;

        int a = new Random().nextInt(5);
        switch(a){
            case 0:
                photoUrl = "http://images5.fanpop.com/image/photos/25600000/Phineas-and-Ferb-in-Abbey-Road-the-beatles-25671083-254-198.jpg";
                break;
            case 1:
                photoUrl = "http://i1.kym-cdn.com/photos/images/original/000/612/359/9cd.jpg";
                break;
            case 2:
                photoUrl = "http://uncleartmusic.com/wp-content/uploads/2015/04/abbey-road-beatles.jpg";
                break;
            case 3:
                photoUrl = "http://media.topito.com/wp-content/uploads/2011/12/abbey_road_parodie012.jpg";
                break;
            case 4:
                photoUrl = "http://fc00.deviantart.net/fs70/f/2013/128/8/7/abbey_road_by_niels827-d64iq0f.png";
                break;
            case 5:
                photoUrl = "http://www.buro247.com/images/ralph-abbey.jpg";
                break;
            default:
                photoUrl = "http://i0.kym-cdn.com/entries/icons/original/000/004/400/the-simpsons-abbey-road.jpg";
                break;

        }
        Picasso.with(context)
                .load(photoUrl)
                .fit().centerCrop()
                .placeholder(R.drawable.team)
                .error(R.drawable.team)
                .into(viewHolder.groupImage);
        // No photos yet...
        /*
        if(venue.getPhotos().size() > 0){
            Photo p = venue.getPhotos().get(0);
            String url = p.getPrefix() + "original" + p.getSuffix();
            Ion.with(viewHolder.venueImage)
                    .placeholder(R.drawable.ic_launcher)
                    .error(R.drawable.ic_drawer_logout)
                    .load(url);
        }*/


    }
    // END_INCLUDE(recyclerViewOnBindViewHolder)

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return visibleGroups.size();
    }
}
