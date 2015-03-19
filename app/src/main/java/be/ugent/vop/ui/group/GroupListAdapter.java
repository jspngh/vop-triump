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

import java.security.acl.Group;
import java.util.ArrayList;

import be.ugent.vop.R;
import be.ugent.vop.backend.myApi.model.GroupBean;
import be.ugent.vop.backend.myApi.model.VenueBean;
import be.ugent.vop.foursquare.FoursquareVenue;

/**
 * Created by siebe on 02/03/15.
 */
public class GroupListAdapter extends RecyclerView.Adapter<GroupListAdapter.ViewHolder> {
    private static final String TAG = "GroupListAdapter";
    private ArrayList<GroupBean> groups;
    private Context context;

    public void setGroups(ArrayList<GroupBean> groups) {
        this.groups = groups;
    }
    public void setContext(Context context) {
        this.context = context;
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
    // END_INCLUDE(recyclerViewSampleViewHolder)

    // BEGIN_INCLUDE(recyclerViewOnCreateViewHolder)
    // Create new views (invoked by the layout manager)
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view.
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.group_list_item, viewGroup, false);

        ViewHolder vh = new ViewHolder(v, new ViewHolder.IMyViewHolderClicks() {
            @Override
            public void onItemClick(View caller, int position) {
                Long groupId = groups.get(position).getGroupId();
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
        GroupBean group = groups.get(position);

        viewHolder.groupName.setText(group.getName());
        viewHolder.groupInfo.setText(group.getDescription());

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
        return groups.size();
    }
}