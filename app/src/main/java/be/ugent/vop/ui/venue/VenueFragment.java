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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.koushikdutta.ion.Ion;

import java.util.ArrayList;
import java.util.List;

import be.ugent.vop.Event;
import be.ugent.vop.EventBroker;
import be.ugent.vop.R;
import be.ugent.vop.backend.loaders.CheckInLoader;
import be.ugent.vop.backend.loaders.RankingLoader;
import be.ugent.vop.backend.loaders.VenueInfoLoader;
import be.ugent.vop.backend.myApi.model.RankingBean;
import be.ugent.vop.backend.myApi.model.VenueBean;
import be.ugent.vop.foursquare.FoursquareVenue;
import be.ugent.vop.ui.group.GroupActivity;


public class VenueFragment extends Fragment {

    private VenueBean venueBean;
    private FoursquareVenue fsVenue;
    private TextView noRankingTextView;
    private TextView titleTextView;
    private ImageView venueImageView;
    private Button checkinButton;
    private RankingAdapter adapter;
    private ArrayList<RankingBean> ranking;
    private ListView rankingListView;
    private Context context;
    private Spinner groupTypeSpinner;
    private Spinner groupSizeSpinner;

    private long venueId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_venue, container, false);

        context = getActivity();

        titleTextView = (TextView)rootView.findViewById(R.id.textViewVenueName);
        noRankingTextView = (TextView) rootView.findViewById(R.id.textViewNoRanking);
        rankingListView = (ListView) rootView.findViewById(R.id.listViewRanking);
        checkinButton = (Button)rootView.findViewById(R.id.buttonCheckin);
        venueImageView = (ImageView) rootView.findViewById(R.id.imageView);

        /**
         * Populate the spinners
         * groupType
         */
        groupTypeSpinner = (Spinner) rootView.findViewById(R.id.spinnerGroupType);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapterType = ArrayAdapter.createFromResource(context,
                R.array.groupType_spinner_options, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapterType.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        groupTypeSpinner.setAdapter(adapterType);

        /**
         * groupSize
         */
        groupSizeSpinner = (Spinner) rootView.findViewById(R.id.spinnerGroupSize);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapterSize = ArrayAdapter.createFromResource(context,
                R.array.groupSize_spinner_options, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapterSize.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        groupSizeSpinner.setAdapter(adapterSize);

        if(getArguments().containsKey(VenueActivity.VENUE_ID))
            venueId = getArguments().getLong(VenueActivity.VENUE_ID);

        Log.d("VenueFragment", "venueId :" + venueId);
        //loader for venueInfo (to foursquare)
        getLoaderManager().initLoader(2, null, mVenueInfoLoaderListener);
        //loader for ranking (to backend)
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


    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    /***********
     Loaders
     ***********/

    /**
     *
     * Loader 1: Ranking
     *
     */

    private LoaderManager.LoaderCallbacks<List<RankingBean>> mRankingLoaderListener
            = new LoaderManager.LoaderCallbacks<List<RankingBean>>() {
        @Override
        public void onLoadFinished(Loader<List<RankingBean>> loader, List<RankingBean> rankings) {
            Log.d("VenueFragment", "onLoadFinished rankingloader");

            if(rankings!=null){
                rankingListView.setVisibility(View.VISIBLE);

                noRankingTextView.setVisibility(View.INVISIBLE);
                adapter = new RankingAdapter(context, rankings);

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
        public Loader<List<RankingBean>> onCreateLoader(int id, Bundle args) {
            Log.d("venueFragment", "onCreateLoader");
            RankingLoader loader = new RankingLoader(context, venueId);
            return loader;
        }

        @Override
        public void onLoaderReset(Loader<List<RankingBean>> loader) {
            rankingListView.setAdapter(null);
        }


    };


    /**
     *
     * Loader 2: Refresh ranking after checkin
     *
     */

    private LoaderManager.LoaderCallbacks<List<RankingBean>> mCheckInLoaderListener
            = new LoaderManager.LoaderCallbacks<List<RankingBean>>() {

        @Override
        public void onLoadFinished(Loader<List<RankingBean>> loader, List<RankingBean> rankings) {
            Log.d("VenueFragment", "onLoadFinished after checkin loader");

            if(rankings!=null){
                adapter = new RankingAdapter(context, rankings);
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

                adapter.notifyDataSetChanged();
            }else{
                noRankingTextView.setText(R.string.no_ranking);
            }

        }

        @Override
        public Loader<List<RankingBean>> onCreateLoader(int id, Bundle args) {
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
        public void onLoaderReset(Loader<List<RankingBean>> loader) {
            //rankingListView.setAdapter(null);
        }


    };

    /**
     *
     * Loader 3: VenueInfo
     *
     */

    private LoaderManager.LoaderCallbacks<FoursquareVenue> mVenueInfoLoaderListener
            = new LoaderManager.LoaderCallbacks<FoursquareVenue>() {
        @Override
        public void onLoadFinished(Loader<FoursquareVenue> loader, FoursquareVenue venue) {
            Log.d("VenueFragment", "onLoadFinished of venueInfoLoader");
            if(venue!=null) {
                titleTextView.setText(venue.getName());
                //placeholder image
                String photoUrl;
                if(venue.getPhotos().size()>0) {
                    photoUrl = venue.getPhotos().get(0).getPrefix() + "500x500" + venue.getPhotos().get(0).getSuffix();
                }
                else photoUrl =
                        "http://iahip.org/wp-content/plugins/jigoshop/assets/images/placeholder.png";
                Ion.with(venueImageView)
                        .placeholder(R.drawable.ic_launcher)
                        .error(R.drawable.ic_drawer_logout)
                        .load(photoUrl);
            }
        }

        @Override
        public Loader<FoursquareVenue> onCreateLoader(int id, Bundle args) {
            Log.d("venueFragment", "onCreateLoader");
            VenueInfoLoader loader = new VenueInfoLoader(context, venueId);
            return loader;
        }

        @Override
        public void onLoaderReset(Loader<FoursquareVenue> loader) {}

    };



}
