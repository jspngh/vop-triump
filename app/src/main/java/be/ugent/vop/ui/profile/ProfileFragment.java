package be.ugent.vop.ui.profile;

import android.app.Activity;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.DialogInterface;
import android.content.Loader;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.koushikdutta.ion.Ion;

import be.ugent.vop.R;
import be.ugent.vop.backend.loaders.UserInfoLoader;
import be.ugent.vop.backend.myApi.model.UserBean;
import be.ugent.vop.ui.widget.CircularImageView;

public class ProfileFragment extends Fragment implements LoaderManager.LoaderCallbacks<UserBean> {
    public final static String USER_ID = "userId";
    public final static String PROFILE_ACTIVITY = "Go to profile";
    private final static int NR_ACHIEVEMENTS = 6;

    private final static boolean[] achievements = new boolean[]{
            false, true,
            true, true,
            false, false
    };

    private ImageView profilePic;
    private TextView firstname;
    private TextView lastname;
    private TextView email;
    private TextView date_joined;;
    private UserBean userInfo;
    private String userId;
    private ImageButton[] achievementImageButton;

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

        achievementImageButton = new ImageButton[NR_ACHIEVEMENTS];

        achievementImageButton[0] = (ImageButton) rootView.findViewById(R.id.imageButton1);
        achievementImageButton[1] = (ImageButton) rootView.findViewById(R.id.imageButton2);
        achievementImageButton[2] = (ImageButton) rootView.findViewById(R.id.imageButton3);
        achievementImageButton[3] = (ImageButton) rootView.findViewById(R.id.imageButton4);
        achievementImageButton[4] = (ImageButton) rootView.findViewById(R.id.imageButton5);
        achievementImageButton[5] = (ImageButton) rootView.findViewById(R.id.imageButton6);

        initAchievementsButton();

        return rootView;
    }

    private void initAchievementsButton() {
        for(int i=0;i<NR_ACHIEVEMENTS;i++) {
            achievementImageButton[i].setActivated(achievements[i]);
            achievementImageButton[i].setOnClickListener(new AchievementClickListener(i+1));
        }
    }

    @Override
    public void onStart(){
        super.onStart();
        Bundle args = new Bundle();
        args.putString(USER_ID, userId);
        getLoaderManager().initLoader(0, args, this);
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
        }
    }
    @Override
    public void onLoaderReset(Loader<UserBean> userInfoLoader) {
    }


    /**
     * Inner private class
     * Achievement clickListener
     */

    class AchievementClickListener implements View.OnClickListener{
        private int achievementNr;

        public AchievementClickListener(int achievementNr){
            this.achievementNr = achievementNr;
        }
        @Override
        public void onClick(View v) {
            Bundle args = new Bundle();
            args.putInt(AchievementDialog.ACHIEVEMENT_NR, achievementNr);
            AchievementDialog achievementDialog = new AchievementDialog();
            achievementDialog.setArguments(args);
            achievementDialog.show(getFragmentManager(), null);
        }
    }

}
