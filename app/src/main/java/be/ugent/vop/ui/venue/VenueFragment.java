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

import com.koushikdutta.ion.Ion;

import java.util.ArrayList;
import java.util.List;

import be.ugent.vop.R;
import be.ugent.vop.backend.myApi.model.RankingBean;
import be.ugent.vop.backend.myApi.model.VenueBean;
import be.ugent.vop.foursquare.FoursquareVenue;
import be.ugent.vop.loaders.CheckInLoader;
import be.ugent.vop.loaders.RankingLoader;
import be.ugent.vop.ui.group.Group;
import be.ugent.vop.ui.group.GroupActivity;


public class VenueFragment extends Fragment {

    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private FoursquareVenue venue;
    private TextView titleTextView;
    private ImageView venueImageView;
    private Button checkinButton;
    private RankingAdapter adapter;
    private ArrayList<Group> topGroups;
    private ListView rankingListView;
    private Context context;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_venue, container, false);

        context = getActivity();

        titleTextView = (TextView)rootView.findViewById(R.id.textViewTitle);
        rankingListView = (ListView) rootView.findViewById(R.id.listViewRanking);
        checkinButton = (Button)rootView.findViewById(R.id.buttonCheckin);
        venueImageView = (ImageView) rootView.findViewById(R.id.imageView);

        venue = (FoursquareVenue) getArguments().getParcelable("venue");

        getLoaderManager().initLoader(0, null, mRankingLoaderListener);

        checkinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getLoaderManager().initLoader(1, null, mCheckInLoaderListener);
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
            photoUrl = "http://www.beeldarchief.ugent.be/fotocollectie/gebouwen/images/prevs/prev64.jpg";
         }
        Ion.with(venueImageView)
                .placeholder(R.drawable.ic_launcher)
                .error(R.drawable.ic_drawer_logout)
                .load(photoUrl);

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
            topGroups = new ArrayList<>();
            List<RankingBean> ranking = venue.getRanking();
            if(ranking!=null) {
                for(RankingBean rank: ranking){
                    topGroups.add(new Group(
                            rank.getGroupBean().getGroupId(),
                            rank.getGroupBean().getName(),
                            rank.getPoints()
                    ));
                }

            }else {
                topGroups.add(new Group(
                        1234567890,
                        "VTK - GENT" ,
                        380
                ));
                topGroups.add(new Group(
                        67890,
                        "VLAK" ,
                        210
                ));
                topGroups.add(new Group(
                        1230,
                        "Moeder Barry" ,
                        40
                ));
            }


            adapter = new RankingAdapter(context, R.layout.ranking_list_item, topGroups);

            rankingListView.setAdapter(adapter);

            rankingListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view,
                                        int position, long id) {

                    Intent intent = new Intent(getActivity(), GroupActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putLong("groupId", topGroups.get(position).getId());
                    intent.putExtras(bundle);
                    startActivity(intent);
                }
            });
        }

        @Override
        public Loader<VenueBean> onCreateLoader(int id, Bundle args) {
            Log.d("venueFragment", "onCreateLoader");
            RankingLoader loader = new RankingLoader(context, venue.getId());
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
            topGroups = new ArrayList<>();
            List<RankingBean> ranking = venue.getRanking();
            if(ranking!=null) {
                for(RankingBean rank: ranking){
                    topGroups.add(new Group(
                            rank.getGroupBean().getGroupId(),
                            rank.getGroupBean().getName(),
                            rank.getPoints()
                    ));
                }

            }else {
                topGroups.add(new Group(
                        1234567890,
                        "VTK - GENT" ,
                        380
                ));
                topGroups.add(new Group(
                        67890,
                        "VLAK" ,
                        210
                ));
                topGroups.add(new Group(
                        1230,
                        "Moeder Barry" ,
                        40
                ));
            }


            adapter = new RankingAdapter(context, R.layout.ranking_list_item, topGroups);

            rankingListView.setAdapter(adapter);

            rankingListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view,
                                        int position, long id) {

                    Intent intent = new Intent(getActivity(), GroupActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putLong("groupId", topGroups.get(position).getId());
                    intent.putExtras(bundle);
                    startActivity(intent);
                }
            });
        }

        @Override
        public Loader<VenueBean> onCreateLoader(int id, Bundle args) {
            Log.d("venueFragment", "onCreateLoader");
            SharedPreferences settings = context.getSharedPreferences(getString(R.string.sharedprefs),Context.MODE_PRIVATE);
            long groupId = settings.getLong(getString(R.string.group_id), 0);
            if(groupId != 0) {
                CheckInLoader loader = new CheckInLoader(context, venue.getId(), groupId);
                return loader;
            }
            Log.d("Invalid LogIn", "Fail !");
            return null;
        }

        @Override
        public void onLoaderReset(Loader<VenueBean> loader) {
            rankingListView.setAdapter(null);
        }
    };


}
