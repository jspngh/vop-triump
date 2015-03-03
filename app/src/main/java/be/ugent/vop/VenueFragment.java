package be.ugent.vop;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import android.support.v4.app.Fragment;

import com.google.android.gms.plus.model.people.Person;
import com.koushikdutta.ion.Ion;

import be.ugent.vop.foursquare.FoursquareVenue;



public class VenueFragment extends Fragment {
    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private String venueId;
    private FoursquareVenue venue;
    private ListView venueListView;
    private TextView titleTextView;
    private ImageView venueImageView;
    private Button checkinButton;
    private ArrayAdapter arrayAdapter;
    private String[] rankingArray;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_venue, container, false);
        venueListView = (ListView) rootView.findViewById(R.id.listViewRanking);
        titleTextView = (TextView)rootView.findViewById(R.id.textViewTitle);
        checkinButton = (Button)rootView.findViewById(R.id.buttonCheckin);
        venueImageView = (ImageView) rootView.findViewById(R.id.imageView);

        venue = (FoursquareVenue) getArguments().getParcelable("venue");


        return rootView;
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        rankingArray = new String[3];
        rankingArray[0] = "Moeder barry";
        rankingArray[1] = "CW";
        rankingArray[2] = "YOLO";

        titleTextView.setText(venue.getName());

        String photoUrl;
        if(venue.getPhotos().size()>0){
            photoUrl = venue.getPhotos().get(0).getPrefix()+"original"+venue.getPhotos().get(0).getSuffix();
        }else {
            photoUrl = "http://www.beeldarchief.ugent.be/fotocollectie/gebouwen/images/prevs/prev64.jpg";
         }
        Ion.with(venueImageView)
                .placeholder(R.drawable.ic_launcher)
                .error(R.drawable.ic_drawer_logout)
                .load(photoUrl);

        arrayAdapter = new ArrayAdapter(getActivity(), android.R.layout.simple_list_item_1, rankingArray);
        venueListView.setAdapter(arrayAdapter);

    }



    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }





}
