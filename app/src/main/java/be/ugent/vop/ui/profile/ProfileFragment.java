package be.ugent.vop.ui.profile;

import android.app.Activity;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Loader;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.amlcurran.showcaseview.ApiUtils;
import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.Target;
import com.github.amlcurran.showcaseview.targets.ViewTarget;
import com.koushikdutta.ion.Ion;

import be.ugent.vop.R;
import be.ugent.vop.backend.loaders.UserInfoLoader;
import be.ugent.vop.backend.myApi.model.UserBean;
import be.ugent.vop.ui.widget.CircularImageView;
import be.ugent.vop.utils.PrefUtils;

public class ProfileFragment extends Fragment implements LoaderManager.LoaderCallbacks<UserBean>, View.OnClickListener {
    public final static String USER_ID = "userId";
    public final static String PROFILE_ACTIVITY = "Go to profile";
    private ImageView profilePic;
    private TextView firstname;
    private TextView lastname;
    private TextView email;
    private TextView date_joined;;
    private UserBean userInfo;
    private String userId;

    private ShowcaseView showcaseView;
    private final ApiUtils apiUtils = new ApiUtils();
    private int counter = 0;

    private OnFragmentInteractionListener mListener;

    public ProfileFragment() {
        // Required empty public constructor
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
    public void onCreate(Bundle savedInstanceState){
        userId = this.getArguments().getString(USER_ID);
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_profile, container, false);
        profilePic = (CircularImageView) rootView.findViewById(R.id.profilePic);
        firstname = (TextView) rootView.findViewById(R.id.firstname);
        lastname = (TextView) rootView.findViewById(R.id.lastname);
        email = (TextView) rootView.findViewById(R.id.email);
        date_joined = (TextView) rootView.findViewById(R.id.date_joined);

        showcaseView = new ShowcaseView.Builder(getActivity())
                .setTarget(new ViewTarget(rootView.findViewById(R.id.lastname)))
                .setOnClickListener(this)
                .setContentTitle("Test title")
                .setContentText("Test text")
                .setStyle(R.style.ShowcaseTheme)
                .build();

        return rootView;
    }

    @Override
    public void onStart(){
        super.onStart();
        Bundle args = new Bundle();
        args.putString(USER_ID, userId);
        getLoaderManager().initLoader(0, args, this);
    }

    private void setAlpha(float alpha, View... views) {
        if (apiUtils.isCompatWithHoneycomb()) {
            for (View view : views) {
                view.setAlpha(alpha);
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (counter){
            case 0:
                showcaseView.setTarget(Target.NONE);
                showcaseView.setContentTitle("Check it out");
                showcaseView.setContentText("You don't always need a target to showcase");
                showcaseView.setButtonText("close");
                setAlpha(0.4f, firstname, lastname, email);
                break;
            default:
                showcaseView.hide();
                setAlpha(1.0f, firstname, lastname, email);
                break;
        }
        counter++;
    }

    public interface OnFragmentInteractionListener {
        public void onProfileFragmentInteraction();
    }

    @Override
    public Loader<UserBean> onCreateLoader(int i, Bundle bundle) {
        return new UserInfoLoader(getActivity(), bundle.getString(USER_ID, "N.A."));
    }

    @Override
    public void onLoadFinished(Loader<UserBean> userInfoLoader, final UserBean userInfo) {
        this.userInfo = userInfo;
        if(userInfo != null){
            if(userInfo.getProfilePictureUrl() != null) {
                Ion.with(profilePic)
                        .placeholder(R.drawable.fantastic_background)
                        .error(R.drawable.ic_drawer_logout)
                        .load(userInfo.getProfilePictureUrl());
            }
            firstname.setText(userInfo.getFirstName());
            lastname.setText(userInfo.getLastName());
            email.setText(userInfo.getEmail());
            date_joined.setText(userInfo.getJoined().toString());
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
            firstname.setText(sharedPref.getString(PrefUtils.DISPLAY_NAME, ""));
        }
    }
    @Override
    public void onLoaderReset(Loader<UserBean> userInfoLoader) {
    }

}
