package be.ugent.vop.ui.main;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.koushikdutta.ion.Ion;

import java.util.ArrayList;

import be.ugent.vop.R;
import be.ugent.vop.foursquare.FoursquareVenue;

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
    public static class ViewHolder extends RecyclerView.ViewHolder {

        public ImageView venueImage;
        public TextView venueName;
        public TextView venueInfo;

        public ViewHolder(View v) {
            super(v);
            venueImage = (ImageView) v.findViewById(R.id.venue_image);
            venueName = (TextView) v.findViewById(R.id.venue_name);
            venueInfo = (TextView) v.findViewById(R.id.venue_info);
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

        return new ViewHolder(v);
    }
    // END_INCLUDE(recyclerViewOnCreateViewHolder)

    // BEGIN_INCLUDE(recyclerViewOnBindViewHolder)
    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        FoursquareVenue venue = venues.get(position);

        viewHolder.venueName.setText(venue.getName());
        viewHolder.venueInfo.setText("Restaurant - 5 groups currently here");

        Ion.with(viewHolder.venueImage)
                .placeholder(R.drawable.ic_launcher)
                .error(R.drawable.ic_drawer_logout)
                .load("http://www.ugentmemorie.be/sites/ugent/files/imagecache/Breedte-640px/foto/2010pm047821h7709.jpg");


    }
    // END_INCLUDE(recyclerViewOnBindViewHolder)

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return venues.size();
    }
}
