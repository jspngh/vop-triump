package be.ugent.vop.ui.venue;

import android.app.Activity;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.koushikdutta.ion.Ion;

import java.util.ArrayList;
import java.util.List;

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
 //   private TextView noRankingTextView;
    private TextView titleTextView;
    private ImageView venueImageView;
    private Button checkinButton;
    private RankingAdapter adapter;
    private List<RankingBean> ranking;
    private ListView rankingListView;
    private Context context;
    private Spinner groupTypeSpinner;
    private Spinner groupSizeSpinner;

    private String currentGroupType = "All";
    private String currentGroupSize = "All";

    protected SwipeRefreshLayout mSwipeRefreshLayout;

    private String fsVenueId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_venue, container, false);

        context = getActivity();

        titleTextView = (TextView)rootView.findViewById(R.id.textViewVenueName);
    //    noRankingTextView = (TextView) rootView.findViewById(R.id.textViewNoRanking);
        rankingListView = (ListView) rootView.findViewById(R.id.listViewRanking);
        checkinButton = (Button)rootView.findViewById(R.id.buttonCheckin);
        venueImageView = (ImageView) rootView.findViewById(R.id.imageView);


        /**
         *
         * Listview refresh
         */
        mSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.fragment_venue_swipe_refresh);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getLoaderManager().restartLoader(1,null,mRankingLoaderListener);
            }
        });
        mSwipeRefreshLayout.setColorSchemeResources(R.color.theme_primary_dark);


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
            fsVenueId = getArguments().getString(VenueActivity.VENUE_ID);

        Log.d("VenueFragment", "venueId :" + fsVenueId);

        /**
         *
         * Initialize loaders
         */
        //loader for venueInfo (to foursquare)
        getLoaderManager().initLoader(2, null, mVenueInfoLoaderListener);
        //loader for ranking (to backend)
        getLoaderManager().initLoader(0, null, mRankingLoaderListener);
        //loader for loading updated ranking after checkin

        checkinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(getLoaderManager().getLoader(1)==null)
                getLoaderManager().initLoader(1, null, mCheckInLoaderListener);
                else
                getLoaderManager().restartLoader(1, null, mCheckInLoaderListener);
            }
        });

        groupSizeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                String selected = (String) parentView.getSelectedItem();
                Log.d("VenueFragment", "selected group size: "+selected);
                if(!currentGroupSize.equals(selected)) {
                    currentGroupSize = selected;
                    RankingLoader.setGroupSize(selected);
                    CheckInLoader.setGroupSize(selected);
                    getLoaderManager().restartLoader(0, null, mRankingLoaderListener);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }

        });

        groupTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                String selected = (String) parentView.getSelectedItem();
                Log.d("VenueFragment", "selected group size: "+selected);
                if(!currentGroupType.equals(selected)){
                    currentGroupType = selected;
                    RankingLoader.setGroupType(selected);
                    CheckInLoader.setGroupType(selected);
                    getLoaderManager().restartLoader(0,null,mRankingLoaderListener);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
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
            ranking=rankings;
            if(rankings!=null){
                rankingListView.setVisibility(View.VISIBLE);
                for(RankingBean r:rankings){
                    Log.d("VenueFragment",r.getGroupBean().getName()+ " | "+r.getPoints());
                }
            //    noRankingTextView.setVisibility(View.INVISIBLE);
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
                        context.startActivity(intent);
                    }
                });
            }else{
                rankingListView.setVisibility(View.INVISIBLE);
             //   noRankingTextView.setText(R.string.no_ranking);
            }

            mSwipeRefreshLayout.setRefreshing(false);
        }

        @Override
        public Loader<List<RankingBean>> onCreateLoader(int id, Bundle args) {
            Log.d("venueFragment", "onCreateLoader");
            RankingLoader loader = new RankingLoader(context, fsVenueId,groupSizeSpinner.getSelectedItem().toString(),groupTypeSpinner.getSelectedItem().toString());
            return loader;
        }

        @Override
        public void onLoaderReset(Loader<List<RankingBean>> loader) {
            //rankingListView.setAdapter(null);
        }


    };


    /**
     *
     * Loader 2: Checkin & Refresh ranking after checkin
     *
     */

    private LoaderManager.LoaderCallbacks<List<RankingBean>> mCheckInLoaderListener
            = new LoaderManager.LoaderCallbacks<List<RankingBean>>() {

        @Override
        public void onLoadFinished(Loader<List<RankingBean>> loader, List<RankingBean> rankings) {
            Log.d("VenueFragment", "onLoadFinished after checkin loader");
            ranking=rankings;
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
                        context.startActivity(intent);
                    }
                });

                adapter.notifyDataSetChanged();
                if( rankingListView.getVisibility()==View.INVISIBLE){
                    rankingListView.setVisibility(View.VISIBLE);
                    Toast t = Toast.makeText(getActivity(),"Congrats! You just scored the first points at this venue.",Toast.LENGTH_SHORT);
                    t.show();
                }
            }else{
             //   noRankingTextView.setText(R.string.no_ranking);
            }

        }

        @Override
        public Loader<List<RankingBean>> onCreateLoader(int id, Bundle args) {
            Log.d("venueFragment", "onCreateLoader");
            CheckInLoader loader = new CheckInLoader(context, fsVenueId,groupSizeSpinner.getSelectedItem().toString(),groupTypeSpinner.getSelectedItem().toString());
            return loader;
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
            VenueInfoLoader loader = new VenueInfoLoader(context, fsVenueId);
            return loader;
        }

        @Override
        public void onLoaderReset(Loader<FoursquareVenue> loader) {}

    };



}
