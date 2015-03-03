package be.ugent.vop.ui.venue;

import android.app.Activity;
import android.content.Intent;
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

import com.koushikdutta.ion.Ion;

import java.util.ArrayList;

import be.ugent.vop.R;
import be.ugent.vop.foursquare.FoursquareVenue;
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




    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_venue, container, false);
        titleTextView = (TextView)rootView.findViewById(R.id.textViewTitle);
        rankingListView = (ListView) rootView.findViewById(R.id.listViewRanking);
        checkinButton = (Button)rootView.findViewById(R.id.buttonCheckin);
        venueImageView = (ImageView) rootView.findViewById(R.id.imageView);

        venue = (FoursquareVenue) getArguments().getParcelable("venue");


        return rootView;
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        topGroups = new ArrayList<>();
        topGroups.add(new Group(12020,"VTK - Gent",181));
        topGroups.add(new Group(12345,"VLAK",160));
        topGroups.add(new Group(3422,"Moeder Barry",13));

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

        adapter = new RankingAdapter(this.getActivity(),R.layout.ranking_list_item,topGroups);

        rankingListView.setAdapter(adapter);

        rankingListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                Log.d("Hello!", "sqdqsd");
           Intent intent = new Intent(getActivity(), GroupActivity.class);
           Bundle bundle = new Bundle();
           bundle.putLong("groupId", topGroups.get(position).getId());
           intent.putExtras(bundle);
           startActivity(intent);

            }
        });

    }



    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }





}
