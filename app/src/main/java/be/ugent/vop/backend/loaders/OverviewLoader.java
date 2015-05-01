package be.ugent.vop.backend.loaders;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.location.Location;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import be.ugent.vop.backend.BackendAPI;
import be.ugent.vop.backend.myApi.model.NewMemberInGroup;
import be.ugent.vop.backend.myApi.model.OverviewBean;
import be.ugent.vop.backend.myApi.model.OverviewCheckin;
import be.ugent.vop.backend.myApi.model.OverviewReward;
import be.ugent.vop.backend.myApi.model.VenueBean;
import be.ugent.vop.database.contentproviders.CheckInContentProvider;
import be.ugent.vop.database.contentproviders.NewMemberContentProvider;
import be.ugent.vop.database.contentproviders.RewardContentProvider;
import be.ugent.vop.foursquare.FoursquareAPI;
import be.ugent.vop.foursquare.FoursquareVenue;
import be.ugent.vop.ui.main.OverviewAdapter;

public class OverviewLoader  extends AsyncTaskLoader<OverviewAdapter> {
    private final String TAG = "OverviewLoader";


    OverviewAdapter mAdapter;
    private Context context;
    private Location mLastLocation;
    private ArrayList<FoursquareVenue> fsVenues;
    private NewMemberContentProvider memberDatabase;
    private CheckInContentProvider checkinDatabase;
    private RewardContentProvider rewardDatabase;
    private boolean getNewOverview;

    public OverviewLoader(Context context, Location lastLocation, boolean getNewOverview) {
        super(context);
        this.context = context;
        this.memberDatabase = new NewMemberContentProvider(context);
        memberDatabase.open();
        this.checkinDatabase = new CheckInContentProvider(context);
        checkinDatabase.open();
        this.rewardDatabase = new RewardContentProvider(context);
        rewardDatabase.open();
        this.mLastLocation = lastLocation;
        this.getNewOverview = getNewOverview;
    }

    @Override public OverviewAdapter loadInBackground() {
        fsVenues = FoursquareAPI.get(context).getNearbyVenues(mLastLocation);
        ArrayList<String> venues = new ArrayList<>();
        for(FoursquareVenue v : fsVenues){
            venues.add(v.getId());
        }

        OverviewBean overview = null;

        if(getNewOverview) {
            try {
                overview = BackendAPI.get(context).getOverview(venues);
                checkinDatabase.deleteAllCheckIns();
                memberDatabase.deleteAllMembers();
                rewardDatabase.deleteAllRewards();
                if (overview.getNewMembers() != null) {
                    for (NewMemberInGroup member : overview.getNewMembers()) {
                        memberDatabase.createMember(member);
                    }
                }
                if (overview.getRewards() != null) {
                    for (OverviewReward reward : overview.getRewards()) {
                        FoursquareVenue venue = FoursquareAPI.get(context).getVenueInfo(reward.getVenueId());
                        if (venue != null) reward.setVenueName(venue.getName());
                        rewardDatabase.createReward(reward);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            ArrayList<FoursquareVenue> venuesInOverview = new ArrayList<>();
            if (overview != null && overview.getVenues() != null) {
                for (VenueBean venue : overview.getVenues()) {
                    for (FoursquareVenue fsVenue : fsVenues) {
                        if (venue.getVenueId().equals(fsVenue.getId())) {
                            venuesInOverview.add(fsVenue);
                        }
                    }
                }
            }
            if (venuesInOverview.size() < 3) {
                for (int i = 0; i < fsVenues.size() && venuesInOverview.size() < 3; i++) {
                    if (!venuesInOverview.contains(fsVenues.get(i)))
                        venuesInOverview.add(fsVenues.get(i));
                }
            }
            fsVenues = venuesInOverview;

            if (overview != null && overview.getCheckIns() != null) {
                for (OverviewCheckin checkin : overview.getCheckIns()) {
                    FoursquareVenue venue = FoursquareAPI.get(context).getVenueInfo(checkin.getVenueId());
                    if (venue != null) checkin.setVenueName(venue.getName());
                }
                for (OverviewCheckin checkin : overview.getCheckIns()) {
                    checkinDatabase.createCheckIn(checkin);
                }
            }
        } else {
            overview = new OverviewBean();
            List<NewMemberInGroup> members = memberDatabase.getAllNewMembers();
            List<OverviewCheckin> checkins = checkinDatabase.getAllCheckIns();
            List<OverviewReward> rewards = rewardDatabase.getAllRewards();
            overview.setNewMembers(members);
            overview.setCheckIns(checkins);
            overview.setRewards(rewards);
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
