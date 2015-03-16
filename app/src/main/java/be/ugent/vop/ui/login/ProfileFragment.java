package be.ugent.vop.ui.login;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import be.ugent.vop.R;

public class ProfileFragment extends Fragment {
    public final static String USER_ID = "userId";
    public final static String PROFILE_ACTIVITY = "Go to profile";
    private String userId;
    private ImageView profilePic;
    private Button btnLogout;

    private OnFragmentInteractionListener mListener;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        Bundle bundle = this.getArguments();
        userId = bundle.getString(USER_ID);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_profile, container, false);
        profilePic = (ImageView) rootView.findViewById(R.id.profilePic);
        TextView txt = (TextView) rootView.findViewById(R.id.userId);
        btnLogout = (Button) rootView.findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onProfileFragmentInteraction();
                }
            }
        });
        txt.setText(""+ userId);
        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }
    @Override
    public void onStart(){
        super.onStart();
/*        Ion.with(profilePic)
                .placeholder(R.drawable.ic_launcher)
                .error(R.drawable.ic_drawer_logout)
                .load("http://fc06.deviantart.net/fs70/f/2012/115/2/0/diablo_3__demon_hunter_by_go_maxpower-d4xiwg1.jpg");*/
    }

    public interface OnFragmentInteractionListener {
        public void onProfileFragmentInteraction();
    }


}
