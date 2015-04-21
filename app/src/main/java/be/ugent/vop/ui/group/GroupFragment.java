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
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.koushikdutta.ion.Ion;

import java.util.ArrayList;

import be.ugent.vop.BaseActivity;
import be.ugent.vop.R;
import be.ugent.vop.backend.loaders.GroupBeanLoader;
import be.ugent.vop.backend.loaders.JoinGroupLoader;
import be.ugent.vop.backend.myApi.model.GroupBean;
import be.ugent.vop.backend.myApi.model.UserBean;
import be.ugent.vop.utils.PrefUtils;

public class GroupFragment extends Fragment {
    public static final String GROUP_ID = "groupId";
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private GridLayoutManager mLayoutManager;

    private ImageView bannerImage;
    private TextView description;
    private TextView name;
    private TextView type;
    private TextView created;
    private Button btn;

    private OnFragmentInteractionListener mListener;
    private String token;
    private long groupId;
    private Context context = null;

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
        name = (TextView) rootView.findViewById(R.id.group_name);
        type = (TextView) rootView.findViewById(R.id.group_type);
        created = (TextView) rootView.findViewById(R.id.group_created);

        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.members_list);
        mLayoutManager = new GridLayoutManager(context, 1, LinearLayoutManager.HORIZONTAL, false);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new MemberListAdapter(null, null);
        mRecyclerView.setAdapter(mAdapter);

        String photoUrl = "http://www.beeldarchief.ugent.be/fotocollectie/gebouwen/images/prevs/prev64.jpg";
        Ion.with(bannerImage)
            .placeholder(R.drawable.ic_launcher)
            .error(R.drawable.ic_drawer_logout)
            .load(photoUrl);
        btn = (Button) rootView.findViewById(R.id.joinbtn);
            btn.setVisibility(View.VISIBLE);
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
        inflater.inflate(R.menu.group_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_pending_requests:
                mListener.onGroupFragmentInteraction(groupId);
                return true;
            case R.id.action_set_banner:
                Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, 0);
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
        void onGroupFragmentInteraction(long groupId);
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

                name.setText(response.getName());
                description.setText(response.getDescription());
                created.setText("Created on: " + response.getCreated().toString());
                type.setText(response.getType() + " group");
                ArrayList<UserBean> members = new ArrayList<>();
                String userId = PrefUtils.getUserId(getActivity());
                if(response.getMembers() != null) {
                    for (UserBean user : response.getMembers()) {
                        if (user.getUserId().equals(userId)) {
                            btn.setVisibility(View.GONE);
                        }
                        members.add(user);
                    }
                }
                mAdapter = new MemberListAdapter(context, members);
                mRecyclerView.setAdapter(mAdapter);
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
                name.setText(response.getName());
                description.setText(response.getDescription().toString());
                created.setText("Created on: " + response.getCreated().toString());
                type.setText(response.getType().toString() + " group");
                ArrayList<UserBean> members = new ArrayList<>();
                if(response.getMembers() != null) {
                    for (UserBean user : response.getMembers()) {
                        members.add(user);
                    }
                }
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
