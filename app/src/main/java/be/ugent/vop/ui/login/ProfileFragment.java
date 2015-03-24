package be.ugent.vop.ui.login;

import android.app.Activity;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Loader;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import com.koushikdutta.ion.Ion;

import be.ugent.vop.R;
import be.ugent.vop.backend.loaders.UserInfoLoader;
import be.ugent.vop.backend.myApi.model.UserBean;

public class ProfileFragment extends Fragment implements LoaderManager.LoaderCallbacks<UserBean> {
    public final static String USER_ID = "userId";
    public final static String PROFILE_ACTIVITY = "Go to profile";
    private ImageView profilePic;
    private Button btnLogout;
    private UserBean userInfo;

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
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_profile, container, false);
        profilePic = (ImageView) rootView.findViewById(R.id.profilePic);
        btnLogout = (Button) rootView.findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onProfileFragmentInteraction();
                }
            }
        });
        return rootView;
    }

    @Override
    public void onStart(){
        super.onStart();
        getLoaderManager().initLoader(0, null, this);
    }

    public interface OnFragmentInteractionListener {
        public void onProfileFragmentInteraction();
    }

    @Override
    public Loader<UserBean> onCreateLoader(int i, Bundle bundle) {
        return new UserInfoLoader(getActivity());
    }

    @Override
    public void onLoadFinished(Loader<UserBean> userInfoLoader, final UserBean userInfo) {
        this.userInfo = userInfo;
        if(userInfo != null && userInfo.getProfilePictureUrl() != null){
            Ion.with(profilePic)
                    .placeholder(R.drawable.fantastic_background)
                    .error(R.drawable.ic_drawer_logout)
                    .load(userInfo.getProfilePictureUrl());
        }
    }
    @Override
    public void onLoaderReset(Loader<UserBean> userInfoLoader) {
    }

}
