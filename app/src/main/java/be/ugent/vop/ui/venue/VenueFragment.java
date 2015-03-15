package be.ugent.vop.ui.venue;

import android.app.Activity;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import be.ugent.vop.Event;
import be.ugent.vop.EventBroker;
import be.ugent.vop.R;
import be.ugent.vop.backend.myApi.model.RankingBean;
import be.ugent.vop.backend.myApi.model.VenueBean;
import be.ugent.vop.foursquare.FoursquareVenue;
import be.ugent.vop.backend.loaders.CheckInLoader;
import be.ugent.vop.backend.loaders.RankingLoader;
import be.ugent.vop.ui.group.GroupActivity;


public class VenueFragment extends Fragment {

    private FoursquareVenue venue;
    private TextView noRankingTextView;
    private TextView titleTextView;
    private ImageView venueImageView;
    private Button checkinButton;
    private RankingAdapter adapter;
    private ArrayList<RankingBean> ranking;
    private ListView rankingListView;
    private Context context;

    private long venueId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_venue, container, false);

        context = getActivity();

        titleTextView = (TextView)rootView.findViewById(R.id.textViewTitle);
        noRankingTextView = (TextView) rootView.findViewById(R.id.textViewNoRanking);
        rankingListView = (ListView) rootView.findViewById(R.id.listViewRanking);
        checkinButton = (Button)rootView.findViewById(R.id.buttonCheckin);
        venueImageView = (ImageView) rootView.findViewById(R.id.imageView);

        if(getArguments().containsKey(VenueActivity.VENUE_ID))
            venueId = getArguments().getLong(VenueActivity.VENUE_ID);

        getLoaderManager().initLoader(0, null, mRankingLoaderListener);

        checkinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getLoaderManager().initLoader(1, null, mCheckInLoaderListener);
                EventBroker.get().addEvent(new Event("checkin"));

            }
        });

        return rootView;
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        titleTextView.setText(venue.getName());

        String photoUrl;
        if(venue.getPhotos().size()>0){
            photoUrl = venue.getPhotos().get(0).getPrefix()+"500x500"+venue.getPhotos().get(0).getSuffix();
        }else {
            photoUrl = "http://iahip.org/wp-content/plugins/jigoshop/assets/images/placeholder.png";
         }
/*        Ion.with(venueImageView)
                .placeholder(R.drawable.ic_launcher)
                .error(R.drawable.ic_drawer_logout)
                .load(photoUrl);*/

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    /***********
     Loaders
     ***********/

    private LoaderManager.LoaderCallbacks<VenueBean> mRankingLoaderListener
            = new LoaderManager.LoaderCallbacks<VenueBean>() {
        @Override
        public void onLoadFinished(Loader<VenueBean> loader, VenueBean venue) {
            Log.d("VenueFragment", "onLoadFinished");

            if(venue.getRanking()!=null){
            noRankingTextView.setVisibility(View.GONE);
            rankingListView.setVisibility(View.VISIBLE);
            ranking = new ArrayList<>();
            for(RankingBean r:venue.getRanking()) ranking.add(r);

            adapter = new RankingAdapter(context, ranking);

            rankingListView.setAdapter(adapter);

            rankingListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view,
                                        int position, long id) {
                    Intent intent = new Intent(getActivity(), GroupActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putLong("groupId", ranking.get(position).getGroupBean().getGroupId());
                    intent.putExtras(bundle);
                    startActivity(intent);
                }
            });
        }else{
                noRankingTextView.setText(R.string.no_ranking);
            }
        }

        @Override
        public Loader<VenueBean> onCreateLoader(int id, Bundle args) {
            Log.d("venueFragment", "onCreateLoader");
            RankingLoader loader = new RankingLoader(context, venueId);
            return loader;
        }

        @Override
        public void onLoaderReset(Loader<VenueBean> loader) {
            rankingListView.setAdapter(null);
        }


    };

    private LoaderManager.LoaderCallbacks<VenueBean> mCheckInLoaderListener
            = new LoaderManager.LoaderCallbacks<VenueBean>() {

        @Override
        public void onLoadFinished(Loader<VenueBean> loader, VenueBean venue) {
            Log.d("VenueFragment", "onLoadFinished");

        }

        @Override
        public Loader<VenueBean> onCreateLoader(int id, Bundle args) {
            Log.d("venueFragment", "onCreateLoader");
            SharedPreferences settings = context.getSharedPreferences(getString(R.string.sharedprefs),Context.MODE_PRIVATE);
            long groupId = settings.getLong(getString(R.string.group_id), 0);
            if(groupId != 0) {
                CheckInLoader loader = new CheckInLoader(context, venueId, groupId);
                return loader;
            }
            Log.d("Invalid LogIn", "Fail !");
            return null;
        }

        @Override
        public void onLoaderReset(Loader<VenueBean> loader) {
            //rankingListView.setAdapter(null);
        }
    };


}
