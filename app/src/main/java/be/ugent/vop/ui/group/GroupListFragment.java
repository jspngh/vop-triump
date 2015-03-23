package be.ugent.vop.ui.group;

import android.app.Activity;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Intent;
import android.content.Loader;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import be.ugent.vop.R;
import be.ugent.vop.backend.loaders.AllGroupsLoader;
import be.ugent.vop.backend.loaders.GroupsForUserLoader;
import be.ugent.vop.backend.myApi.model.AllGroupsBean;
import be.ugent.vop.backend.myApi.model.GroupBean;
import be.ugent.vop.backend.myApi.model.GroupsBean;


public class GroupListFragment extends Fragment implements LoaderManager.LoaderCallbacks<GroupsBean>, android.widget.SearchView.OnQueryTextListener {
    private static final String TAG = "CheckinFragment";

    protected GroupListFragment mFragment;
    protected RecyclerView mRecyclerView;
    protected GroupListAdapter mAdapter;
    protected SwipeRefreshLayout mSwipeRefreshLayout;
    protected RecyclerView.LayoutManager mLayoutManager;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_group_list, container, false);
        rootView.setTag(TAG);
        setHasOptionsMenu(true);
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.group_list);

        mSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.fragment_group_swipe_refresh);

        // LinearLayoutManager is used here, this will layout the elements in a similar fashion
        // to the way ListView would layout elements. The RecyclerView.LayoutManager defines how
        // elements are laid out.
        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new GroupListAdapter();
        // Set CustomAdapter as the adapter for RecyclerView.
        mRecyclerView.setAdapter(null);
        // END_INCLUDE(initializeRecyclerView);
        getLoaderManager().initLoader(1, null, this);
        mFragment=this;
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getLoaderManager().restartLoader(1,null,mFragment);
            }
            });
        mSwipeRefreshLayout.setColorSchemeResources(R.color.theme_primary_dark);
        mRecyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            int state;


            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                state = newState;
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy){
                //if(state == RecyclerView.SCROLL_STATE_DRAGGING){
                Log.d(TAG, "Vertical scroll: " + dy);
                mRecyclerView.animate().translationY(dy);
                //}
            }

        });

        return rootView;
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public Loader<GroupsBean> onCreateLoader(int id, Bundle bundle) {
        Log.d(TAG, "onCreateLoader");
        return new GroupsForUserLoader(getActivity());

    }

    @Override
    /*
    Note:
    Adjusted Loader to only load the groups where the user is a member
     */
    public void onLoadFinished(Loader<GroupsBean> objectLoader, GroupsBean allGroupsBean) {
        Log.d(TAG, "onLoadFinished");
        /**************************************
         Resultaat kan null zijn
         Rekening mee houden!
         **************************************/
        if(allGroupsBean.getGroups() != null) {
            mAdapter.setGroups((ArrayList<GroupBean>) allGroupsBean.getGroups());
            Log.d(TAG, "amount of groups : " + allGroupsBean.getGroups().size());
            mAdapter.setContext(getActivity());
            mRecyclerView.setAdapter(mAdapter);
        }

        mRecyclerView.setVisibility(View.VISIBLE);
        mSwipeRefreshLayout.setRefreshing(false);


    }

    @Override
    public void onLoaderReset(Loader<GroupsBean> objectLoader) {
        mRecyclerView.setAdapter(null);
    }

    @Override
    public boolean onQueryTextSubmit(String s) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String s) {
        return false;
    }
}

