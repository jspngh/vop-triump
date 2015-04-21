package be.ugent.vop.backend.loaders;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.location.Location;

import java.io.IOException;
import java.util.ArrayList;

import be.ugent.vop.backend.BackendAPI;
import be.ugent.vop.backend.myApi.model.OverviewBean;
import be.ugent.vop.backend.myApi.model.OverviewCheckin;
import be.ugent.vop.backend.myApi.model.VenueBean;
import be.ugent.vop.foursquare.FoursquareAPI;
import be.ugent.vop.foursquare.FoursquareVenue;
import be.ugent.vop.ui.main.OverviewAdapter;

public class OverviewLoader  extends AsyncTaskLoader<OverviewAdapter> {
    private final String TAG = "OverviewLoader";


    OverviewAdapter mAdapter;
    private Context context;
    private Location mLastLocation;
    private ArrayList<FoursquareVenue> fsVenues;
    private Object syncToken;

    public OverviewLoader(Context context, Location lastLocation) {
        super(context);
        this.context = context;
        this.mLastLocation = lastLocation;
    }

    @Override public OverviewAdapter loadInBackground() {
        OverviewBean result = null;

        fsVenues = FoursquareAPI.get(context).getNearbyVenues(mLastLocation);
        ArrayList<String> venues = new ArrayList<>();
        for(FoursquareVenue v : fsVenues){
            venues.add(v.getId());
        }

        OverviewBean overview = null;
        try {
            overview = BackendAPI.get(context).getOverview(venues);
        } catch (IOException e) {
            e.printStackTrace();
        }
        ArrayList<FoursquareVenue> venuesInOverview = new ArrayList<>();
        if(overview != null && overview.getVenues() != null) {
            for (VenueBean venue : overview.getVenues()) {
                for(FoursquareVenue fsVenue : fsVenues) {
                    if (venue.getVenueId().equals(fsVenue.getId())) {
                        venuesInOverview.add(fsVenue);
                    }
                }
            }
        }
        if(venuesInOverview.size() < 3){
            for(int i = 0; i < fsVenues.size() && venuesInOverview.size() < 3; i++){
                if(!venuesInOverview.contains(fsVenues.get(i)))
                    venuesInOverview.add(fsVenues.get(i));
            }
        }
        fsVenues = venuesInOverview;

        if(overview != null && overview.getCheckIns() != null) {
            for (OverviewCheckin checkin : overview.getCheckIns()) {
                FoursquareVenue venue = FoursquareAPI.get(context).getVenueInfo(checkin.getCheckin().getVenueId());
                if(venue != null) checkin.setVenueName(venue.getName());
            }
        }
        return new OverviewAdapter(overview, fsVenues, context, false);
    }

    @Override public void deliverResult(OverviewAdapter result) {
        if (isReset()) {
            if (result != null) {
                onReleaseResources(result);
            }
        }
        OverviewAdapter oldAllGroupsBean = result;
        mAdapter = result;

        if (isStarted()) {
            super.deliverResult(result);
        }

        if (oldAllGroupsBean != null) {
            onReleaseResources(oldAllGroupsBean);
        }
    }

    @Override protected void onStartLoading() {
        if (mAdapter != null) {
            deliverResult(mAdapter);
        }

        if (takeContentChanged() || mAdapter == null) {
            forceLoad();
        }
    }

    @Override protected void onStopLoading() {
        // Attempt to cancel the current load task if possible.
        cancelLoad();
    }

    @Override public void onCanceled(OverviewAdapter userBeam) {
        super.onCanceled(userBeam);
        onReleaseResources(userBeam);
    }

    @Override protected void onReset() {
        super.onReset();

        onStopLoading();
        if (mAdapter != null) {
            onReleaseResources(mAdapter);
            mAdapter = null;
        }
    }

    protected void onReleaseResources(OverviewAdapter userBean) {
    }
}
