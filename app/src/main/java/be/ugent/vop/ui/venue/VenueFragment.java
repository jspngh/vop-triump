package be.ugent.vop.ui.venue;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.LoaderManager;
import android.content.Intent;
import android.content.Loader;
import android.location.Location;
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

import android.app.Fragment;

import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.koushikdutta.ion.Ion;

import java.util.ArrayList;
import java.util.List;

import be.ugent.vop.R;
import be.ugent.vop.backend.myApi.model.RankingBean;
import be.ugent.vop.backend.myApi.model.VenueBean;
import be.ugent.vop.foursquare.FoursquareVenue;
import be.ugent.vop.loaders.RankingLoader;
import be.ugent.vop.loaders.VenueLoader;
import be.ugent.vop.ui.group.Group;
import be.ugent.vop.ui.group.GroupActivity;


public class VenueFragment extends Fragment implements LoaderManager.LoaderCallbacks<VenueBean>{

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




    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_venue, container, false);
        titleTextView = (TextView)rootView.findViewById(R.id.textViewTitle);
        rankingListView = (ListView) rootView.findViewById(R.id.listViewRanking);
        checkinButton = (Button)rootView.findViewById(R.id.buttonCheckin);
        venueImageView = (ImageView) rootView.findViewById(R.id.imageView);

        venue = (FoursquareVenue) getArguments().getParcelable("venue");

        getLoaderManager().initLoader(0, null, this);

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



    /**
     *
     * LoaderManager callback
     *
     */


    @Override
    public Loader<VenueBean> onCreateLoader(int id, Bundle bundle) {
        Log.d("venueFragment", "onCreateLoader");
        RankingLoader loader = new RankingLoader(this.getActivity(), venue.getId());
        return loader;
    }

    @Override
    public void onLoadFinished(Loader<VenueBean> loader, VenueBean venue) {
        Log.d("VenueFragment", "onLoadFinished");
        topGroups = new ArrayList<>();
        List<RankingBean> ranking = venue.getRanking();
        if(ranking!=null) {
            RankingBean position = ranking.get(0);
            if(position!=null){
                topGroups.add(new Group(
                        position.getGroupBean().getGroupId(),
                        position.getGroupBean().getName(),
                        position.getPoints()
                ));
            }
            position = ranking.get(1);
            if(position!=null){
                topGroups.add(new Group(
                        position.getGroupBean().getGroupId(),
                        position.getGroupBean().getName(),
                        position.getPoints()
                ));
            }
            position = ranking.get(2);
            if(position!=null){
                topGroups.add(new Group(
                        position.getGroupBean().getGroupId(),
                        position.getGroupBean().getName(),
                        position.getPoints()
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


        adapter = new RankingAdapter(this.getActivity(),R.layout.ranking_list_item, topGroups);

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
    public void onLoaderReset(Loader<VenueBean> venue) {
        rankingListView.setAdapter(null);
    }



}
