package be.ugent.vop.ui.group;

import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.gc.materialdesign.views.ButtonFlat;

import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;

import be.ugent.vop.BaseActivity;
import be.ugent.vop.R;
import be.ugent.vop.backend.BackendAPI;
import be.ugent.vop.backend.loaders.GroupBeanLoader;
import be.ugent.vop.backend.loaders.PendingUsersLoader;
import be.ugent.vop.backend.myApi.model.GroupBean;
import be.ugent.vop.backend.myApi.model.UserBean;
import be.ugent.vop.ui.profile.ProfileActivity;
import be.ugent.vop.ui.profile.ProfileFragment;
import be.ugent.vop.utils.PrefUtils;

public class ManageMembersFragment extends Fragment implements LoaderManager.LoaderCallbacks<GroupBean> {
    private long groupId;
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private LinearLayoutManager mLayoutManager;
    private Context context;
    public ManageMembersFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        groupId = this.getArguments().getLong(GroupFragment.GROUP_ID, 0);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_manage_members, container, false);
        context = getActivity();

        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.member_list);

        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(null);

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public void onLoadFinished(Loader<GroupBean> loader, GroupBean response) {
        ArrayList<UserBean> members = new ArrayList<>();
        if (response != null && response.getMembers() != null) {
            for (UserBean user : response.getMembers()) {
                members.add(user);
            }
        }
        mAdapter = new ManageMemberAdapter(context, members, groupId);
        mRecyclerView.setAdapter(mAdapter);
    }

    @Override
    public Loader<GroupBean> onCreateLoader (int id, Bundle args){
        return new GroupBeanLoader(context, groupId);
    }

    @Override
    public void onLoaderReset(Loader<GroupBean> loader) {
        mRecyclerView.setAdapter(null);
    }
}

class ManageMemberAdapter extends RecyclerView.Adapter<ManageMemberAdapter.ViewHolder> {
    private Context context;
    private ArrayList<UserBean> members;
    private long groupId;

    public ManageMemberAdapter(Context context, ArrayList<UserBean> members, long groupId){
        super();
        this.members = members;
        this.context = context;
        this.groupId = groupId;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView profilePic;
        public TextView member_name;
        public ButtonFlat remove_button;
        public ViewHolder(View v) {
            super(v);
            profilePic = (ImageView) v.findViewById(R.id.profilePic);
            member_name = (TextView) v.findViewById(R.id.member_name);
            remove_button = (ButtonFlat) v.findViewById(R.id.button_remove);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public ManageMemberAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.member_manage_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final int pos = position;
        if(members != null){
            holder.member_name.setText(members.get(position).getFirstName());
            if(members.get(position).getProfilePictureUrl() != null) {
                Picasso.with(context)
                        .load(members.get(position).getProfilePictureUrl())
                        .fit().centerCrop()
                        .placeholder(R.drawable.profile_default)
                        .error(R.drawable.ic_drawer_user)
                        .into(holder.profilePic);
            }
            final String userId = members.get(position).getUserId();
            holder.remove_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new Thread( new Runnable() {
                        @Override
                        public void run() {
                            try {
                                BackendAPI.get(context).removeUserFromGroup(userId, groupId);
                            }catch (IOException e){
                                e.printStackTrace();
                            }
                        }

                    }).start();
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        if(members != null)return members.size();
        return 0;
    }
}
