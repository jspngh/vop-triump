package be.ugent.vop.ui.group;

import android.app.Activity;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.gc.materialdesign.views.ButtonRectangle;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Random;

import be.ugent.vop.BaseActivity;
import be.ugent.vop.R;
import be.ugent.vop.backend.loaders.GroupBeanLoader;
import be.ugent.vop.backend.loaders.JoinGroupLoader;
import be.ugent.vop.backend.myApi.model.GroupBean;
import be.ugent.vop.backend.myApi.model.UserBean;
import be.ugent.vop.utils.PrefUtils;

public class GroupFragment extends Fragment {
    public static final String GROUP_ID = "groupId";
    private static final String TAG = "GroupFragment";
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private GridLayoutManager mLayoutManager;

    private ImageView bannerImage;
    private TextView description;
    private TextView type;
  //  private TextView created;
    private ButtonRectangle btn;

    private Menu mMenu;

    private OnFragmentInteractionListener mListener;
    private String token;
    private long groupId;
    private ArrayList<UserBean> memberList;
    private Context context = null;

    private boolean mIsAdmin = false;

    public GroupFragment() {
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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = this.getArguments();
        groupId = bundle.getLong(GROUP_ID, 0);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_group, container, false);
        context = getActivity();

        setHasOptionsMenu(true);

        bannerImage = (ImageView) rootView.findViewById(R.id.bannerImage);
        description = (TextView) rootView.findViewById(R.id.group_description);
        type = (TextView) rootView.findViewById(R.id.group_type);
      //  created = (TextView) rootView.findViewById(R.id.group_created);

        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.members_list);
        mLayoutManager = new GridLayoutManager(context, 1, LinearLayoutManager.HORIZONTAL, false);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new MemberListAdapter(null, null);
        mRecyclerView.setAdapter(mAdapter);
        String photoUrl;

        int a = new Random().nextInt(5);
        switch(a){
            case 0:
                photoUrl = "http://images5.fanpop.com/image/photos/25600000/Phineas-and-Ferb-in-Abbey-Road-the-beatles-25671083-254-198.jpg";
                break;
            case 1:
                photoUrl = "http://i1.kym-cdn.com/photos/images/original/000/612/359/9cd.jpg";
                break;
            case 2:
                photoUrl = "http://uncleartmusic.com/wp-content/uploads/2015/04/abbey-road-beatles.jpg";
                break;
            case 3:
                photoUrl = "http://media.topito.com/wp-content/uploads/2011/12/abbey_road_parodie012.jpg";
                break;
            case 4:
                photoUrl = "http://fc00.deviantart.net/fs70/f/2013/128/8/7/abbey_road_by_niels827-d64iq0f.png";
                break;
            case 5:
                photoUrl = "http://www.buro247.com/images/ralph-abbey.jpg";
                break;
            default:
                photoUrl = "http://i0.kym-cdn.com/entries/icons/original/000/004/400/the-simpsons-abbey-road.jpg";
                break;

        }
        Picasso.with(context)
                .load(photoUrl)
                .placeholder(R.drawable.ic_launcher)
                .error(R.drawable.ic_drawer_logout)
                .into(bannerImage);

        btn = (ButtonRectangle) rootView.findViewById(R.id.joinbtn);

        btn.setVisibility(View.GONE);
        btn.setFocusable(false);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getLoaderManager().initLoader(1, null, mJoinGroupLoaderListener);
            }
        });
        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        getLoaderManager().initLoader(0, null, mGroupBeanLoaderListener);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        this.mMenu = menu;
        inflater.inflate(R.menu.group_menu, menu);

        mMenu.findItem(R.id.action_pending_requests).setVisible(mIsAdmin);
        mMenu.findItem(R.id.action_set_banner).setVisible(mIsAdmin);
        mMenu.findItem(R.id.action_manage_members).setVisible(mIsAdmin);

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_pending_requests:
                mListener.onGroupFragmentInteraction(0, groupId);
                return true;
            case R.id.action_set_banner:
                Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, 0);
                return true;
            case R.id.action_manage_members:
                mListener.onGroupFragmentInteraction(1, groupId);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            Uri selectedImage = data.getData();
            String[] filePathColumn = {MediaStore.Images.Media.DATA};

            Cursor cursor = context.getContentResolver().query(
                    selectedImage, filePathColumn, null, null, null);
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String filePath = cursor.getString(columnIndex);
            cursor.close();

            //now we need to SAVE this BITMAP aswell
            Bitmap image = BitmapFactory.decodeFile(filePath);
            bannerImage.setImageBitmap(image);
        }
    }

    public interface OnFragmentInteractionListener {
        void onGroupFragmentInteraction(int action, long groupId);
    }


    /***********
       Loaders
     ***********/

    private LoaderManager.LoaderCallbacks<GroupBean> mGroupBeanLoaderListener
            = new LoaderManager.LoaderCallbacks<GroupBean>() {
        @Override
        public void onLoadFinished(Loader<GroupBean> loader, GroupBean response) {
            if (response != null){
                ((BaseActivity) getActivity()).setTitle(response.getName());

                description.setText(response.getDescription());
            //    created.setText("Created on: " + response.getCreated().toString());
                type.setText(response.getType());
                ArrayList<UserBean> members = new ArrayList<>();
                String userId = PrefUtils.getUserId(getActivity());
                if(response.getMembers() != null) {
                    for (UserBean user : response.getMembers()) {
                        if (!user.getUserId().equals(userId)) {
                            btn.setVisibility(View.VISIBLE);
                        }
                        members.add(user);
                    }
                }
                memberList = members;
                mAdapter = new MemberListAdapter(context, members);
                mRecyclerView.setAdapter(mAdapter);

                Log.d(TAG, "Group admin id: " + response.getAdminId());

                if(PrefUtils.getUserId(getActivity()).equals(response.getAdminId())) {
                    Log.d(TAG, "User is admin of group");
                    mIsAdmin = true;
                    getActivity().invalidateOptionsMenu();
                }
            }
        }

        @Override
        public Loader<GroupBean> onCreateLoader (int id, Bundle args){
            return new GroupBeanLoader(context, groupId);
        }

        @Override
        public void onLoaderReset(Loader<GroupBean> loader) {
            mRecyclerView.setAdapter(null);
        }
    };

    private LoaderManager.LoaderCallbacks<GroupBean> mJoinGroupLoaderListener
            = new LoaderManager.LoaderCallbacks<GroupBean>() {
        @Override
        public void onLoadFinished(Loader<GroupBean> loader, GroupBean response) {
            if (response != null) {
                btn.setVisibility(View.GONE);
                description.setText(response.getDescription());
               // created.setText("Created on: " + response.getCreated().toString());
                type.setText(response.getType() + " group");
                ArrayList<UserBean> members = new ArrayList<>();
                if(response.getMembers() != null) {
                    for (UserBean user : response.getMembers()) {
                        members.add(user);
                    }
                }
                memberList = members;
                mAdapter = new MemberListAdapter(context, members);
                mRecyclerView.setAdapter(mAdapter);
            }
        }

        @Override
        public Loader<GroupBean> onCreateLoader(int id, Bundle args) {
            return new JoinGroupLoader(context, groupId);
        }

        @Override
        public void onLoaderReset(Loader<GroupBean> loader) {
            mRecyclerView.setAdapter(null);
        }
    };
}
