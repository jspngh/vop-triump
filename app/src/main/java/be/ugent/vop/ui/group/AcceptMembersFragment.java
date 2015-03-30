package be.ugent.vop.ui.group;

import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Context;
import android.content.Loader;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import be.ugent.vop.R;
import be.ugent.vop.backend.loaders.PendingUsersLoader;
import be.ugent.vop.backend.myApi.model.UserBean;

public class AcceptMembersFragment extends Fragment implements LoaderManager.LoaderCallbacks<ArrayList<UserBean>> {
    private long groupId;
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private LinearLayoutManager mLayoutManager;
    private Context context;
    public AcceptMembersFragment() {
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
        View rootView = inflater.inflate(R.layout.fragment_accept_members, container, false);
        context = getActivity();

        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.members_list);

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
    public Loader<ArrayList<UserBean>> onCreateLoader(int id, Bundle args) {
        return new PendingUsersLoader(context, groupId);
    }

    @Override
    public void onLoadFinished(Loader<ArrayList<UserBean>> loader, ArrayList<UserBean> data) {
        mAdapter = new PendingMembersAdapter(context, data, groupId);
        mRecyclerView.setAdapter(mAdapter);
    }

    @Override
    public void onLoaderReset(Loader<ArrayList<UserBean>> loader) {
        mRecyclerView.setAdapter(null);
    }
}
