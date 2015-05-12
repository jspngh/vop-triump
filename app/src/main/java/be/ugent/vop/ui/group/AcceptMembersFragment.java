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
import android.widget.ProgressBar;
import android.widget.TextView;

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
    private TextView mEmpty;
    private ProgressBar mLoading;
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
        mEmpty = (TextView)rootView.findViewById(R.id.empty);
        mLoading = (ProgressBar)rootView.findViewById(R.id.loading);
        mEmpty.setVisibility(View.INVISIBLE);
        mRecyclerView.setVisibility(View.VISIBLE);
        mLoading.setVisibility(View.VISIBLE);
        mEmpty.setText("All requests have been handled.");
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
        if(mAdapter.getItemCount()==0){
            mEmpty.setVisibility(View.VISIBLE);
            mLoading.setVisibility(View.INVISIBLE);
        }else{
            mEmpty.setVisibility(View.INVISIBLE);
            mLoading.setVisibility(View.INVISIBLE);
        }
        mRecyclerView.setVisibility(View.VISIBLE);
        mRecyclerView.setAdapter(mAdapter);
    }

    @Override
    public void onLoaderReset(Loader<ArrayList<UserBean>> loader) {
        mRecyclerView.setAdapter(null);
    }
}
