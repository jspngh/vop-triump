package be.ugent.vop.ui.main;

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
import be.ugent.vop.foursquare.FoursquareVenue;
import be.ugent.vop.foursquare.Photo;

/**
 * Created by siebe on 02/03/15.
 */
public class VenueListAdapter extends RecyclerView.Adapter<VenueListAdapter.ViewHolder> {
    private static final String TAG = "VenueListAdapter";

    private ArrayList<FoursquareVenue> venues;

    public void setVenues(ArrayList<FoursquareVenue> venues) {
        this.venues = venues;
    }

    // BEGIN_INCLUDE(recyclerViewSampleViewHolder)
    /**
     * Provide a reference to the type of views that you are using (custom ViewHolder)
     */
    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public ImageView venueImage;
        public TextView venueName;
        public TextView venueInfo;
        public IMyViewHolderClicks mListener;

        public ViewHolder(View v, IMyViewHolderClicks listener) {
            super(v);
            venueImage = (ImageView) v.findViewById(R.id.venue_image);
            venueName = (TextView) v.findViewById(R.id.venue_name);
            venueInfo = (TextView) v.findViewById(R.id.venue_info);
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
                .inflate(R.layout.venue_list_item, viewGroup, false);

        ViewHolder vh = new ViewHolder(v, new ViewHolder.IMyViewHolderClicks() {
            @Override
            public void onItemClick(View caller, int position) {
                String venueId = venues.get(position).getId();

                // TODO: Start new activity showing details for this venue
                Log.d("VenueListAdapter", "Showing details for venue " + venueId);
            }
        });

        return vh;
    }
    // END_INCLUDE(recyclerViewOnCreateViewHolder)

    // BEGIN_INCLUDE(recyclerViewOnBindViewHolder)
    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        FoursquareVenue venue = venues.get(position);

        viewHolder.venueName.setText(venue.getName());
        viewHolder.venueInfo.setText("Restaurant - 5 groups currently here");

        if(venue.getPhotos().size() > 0){
            Photo p = venue.getPhotos().get(0);
            String url = p.getPrefix() + "original" + p.getSuffix();
            Ion.with(viewHolder.venueImage)
                    .placeholder(R.drawable.ic_launcher)
                    .error(R.drawable.ic_drawer_logout)
                    .load(url);
        }


    }
    // END_INCLUDE(recyclerViewOnBindViewHolder)

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return venues.size();
    }
}
