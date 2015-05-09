package be.ugent.vop.ui.profile;

import android.app.Activity;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Context;
import android.content.Loader;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import be.ugent.vop.R;
import be.ugent.vop.achievements.AchievementDialog;
import be.ugent.vop.achievements.AchievementListAdapter;
import be.ugent.vop.backend.loaders.UserInfoLoader;
import be.ugent.vop.backend.myApi.model.UserBean;
import be.ugent.vop.ui.widget.CircularImageView;

public class ProfileFragment extends Fragment implements LoaderManager.LoaderCallbacks<UserBean> {
    public final static String USER_ID = "userId";
    private Context mContext;

    private ImageView mProfilePic;
    private TextView mFirstname;
    private TextView mLastname;
    private TextView mEmail;
    private TextView mJoinDate;
    private RecyclerView mAchievementList;
    private RecyclerView.Adapter mAdapter;
    private GridLayoutManager mLayoutManager;

    private UserBean mUserInfo;
    private String mUserId;
    private ImageButton[] achievementImageButton;

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
            throw new ClassCastException(activity.toString() + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        mUserId = this.getArguments().getString(USER_ID);
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_profile, container, false);
        mContext = getActivity();

        mProfilePic = (CircularImageView) rootView.findViewById(R.id.profilePic);
        mFirstname = (TextView) rootView.findViewById(R.id.firstname);
        mLastname = (TextView) rootView.findViewById(R.id.lastname);
        mEmail = (TextView) rootView.findViewById(R.id.email);
        mJoinDate = (TextView) rootView.findViewById(R.id.date_joined);
        mAchievementList = (RecyclerView) rootView.findViewById(R.id.achievement_list);
        mLayoutManager = new GridLayoutManager(mContext, 2, LinearLayoutManager.VERTICAL, false);
        mAchievementList.setLayoutManager(mLayoutManager);
        mAdapter = new AchievementListAdapter(mContext, null);
        mAchievementList.setAdapter(mAdapter);

        return rootView;
    }

    @Override
    public void onStart(){
        super.onStart();
        Bundle args = new Bundle();
        args.putString(USER_ID, mUserId);
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
        this.mUserInfo = userInfo;
        if(userInfo != null){
            if(userInfo.getProfilePictureUrl() != null) {
                Picasso.with(mContext)
                        .load(userInfo.getProfilePictureUrl())
                        .placeholder(R.drawable.fantastic_background)
                        .error(R.drawable.fantastic_background)
                        .into(mProfilePic);
            }
            mFirstname.setText(userInfo.getFirstName());
            mLastname.setText(userInfo.getLastName());
            mEmail.setText(userInfo.getEmail());
            mJoinDate.setText(userInfo.getJoined().toString());
            mAdapter = new AchievementListAdapter(mContext, (ArrayList<Boolean>)userInfo.getAchievementsActivated());
            mAchievementList.setAdapter(mAdapter);
        }
    }
    @Override
    public void onLoaderReset(Loader<UserBean> userInfoLoader) {
    }
}
