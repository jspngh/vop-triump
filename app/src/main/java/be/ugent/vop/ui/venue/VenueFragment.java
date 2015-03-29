package be.ugent.vop.ui.venue;

import android.app.Activity;
import android.app.Dialog;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
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
import be.ugent.vop.ui.event.NewEventActivity;
import be.ugent.vop.ui.group.GroupActivity;


public class VenueFragment extends Fragment {
    private static final int MIN_PARTICIPANTS = 1;
    private static final int MAX_PARTICIPANTS = 1000;

    private VenueBean venueBean;
    private FoursquareVenue fsVenue;
 //   private TextView noRankingTextView;
    private TextView titleTextView;
    private ImageView venueImageView;
    private Button checkinButton;
    private Button newEventButton;
    private Button listEventsButton;

    private RankingAdapter adapter;
    private List<RankingBean> ranking;
    private ListView rankingListView;
    private Dialog EventsList;
    private Context context;

    protected SwipeRefreshLayout mSwipeRefreshLayout;

    private String fsVenueId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_venue, container, false);

        context = getActivity();

        titleTextView = (TextView)rootView.findViewById(R.id.textViewVenueName);
//        noRankingTextView = (TextView) rootView.findViewById(R.id.textViewNoRanking);
        rankingListView = (ListView) rootView.findViewById(R.id.listViewRanking);
        checkinButton = (Button)rootView.findViewById(R.id.buttonCheckin);
        venueImageView = (ImageView) rootView.findViewById(R.id.imageView);
        newEventButton = (Button) rootView.findViewById(R.id.buttonNewEvent);
        listEventsButton = (Button) rootView.findViewById(R.id.buttonListEvents);

        EventsList = new Dialog(context);
        EventsList.setContentView(R.layout.layout_event_list);
        EventsList.setTitle("Events for this venue");
        EventsList.setCancelable(true);
        ListView list = (ListView) EventsList.findViewById(R.id.event_list);
        ArrayList<String> sample_events = new ArrayList<>();
        sample_events.add("sample 1");
        sample_events.add("sample 2");
        sample_events.add("sample 3");
        sample_events.add("sample 4");
        sample_events.add("sample 5");
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(context,android.R.layout.simple_list_item_1, sample_events);
        list.setAdapter(adapter);

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


        newEventButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            /*    EventFragment fragment = new EventFragment();
                getActivity().getFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, fragment)
                        .commit();*/

                Intent intent = new Intent(getActivity(),NewEventActivity.class);
                intent.putExtra(VenueActivity.VENUE_ID, fsVenueId);

                getActivity().startActivity(intent);
            }
        });

        listEventsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EventsList.show();
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
            RankingLoader loader = new RankingLoader(context, fsVenueId,MIN_PARTICIPANTS,MAX_PARTICIPANTS);
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
            CheckInLoader loader = new CheckInLoader(context, fsVenueId,MIN_PARTICIPANTS,MAX_PARTICIPANTS);
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
                        .placeholder(R.drawable.fantastic_background)
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
