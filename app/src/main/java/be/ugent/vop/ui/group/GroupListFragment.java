package be.ugent.vop.ui.group;

import android.app.Activity;
import android.app.Fragment;
import android.app.LoaderManager;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;

import be.ugent.vop.R;
import be.ugent.vop.backend.loaders.AllGroupsLoader;
import be.ugent.vop.backend.loaders.GroupsForUserLoader;
import be.ugent.vop.backend.myApi.model.GroupBean;
import be.ugent.vop.backend.myApi.model.GroupsBean;


public class GroupListFragment extends Fragment implements android.widget.SearchView.OnQueryTextListener {
    private static final String TAG = "GroupListFragment";

    protected GroupListFragment mFragment;
    protected RecyclerView mRecyclerView;
    protected GroupListAdapter mAdapter;
    protected SwipeRefreshLayout mSwipeRefreshLayout;
    protected RecyclerView.LayoutManager mLayoutManager;
    protected boolean allUsers;
    private TextView mEmpty;
    private ProgressBar mLoading;

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.groups_menu, menu);
        // Associate searchable configuration with the SearchView
        MenuItem searchItem = menu.findItem(R.id.action_search);

        SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);

        SearchView searchView = null;
        if (searchItem != null) {
            searchView = (SearchView) searchItem.getActionView();
        }
        if (searchView != null) {
            searchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));

            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String s) {
                    Log.d(TAG, "Searching: " + s);
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String s) {
                    if (TextUtils.isEmpty(s)) {
                        ((GroupListAdapter) mRecyclerView.getAdapter()).clearFilter();
                    } else {
                        ((GroupListAdapter) mRecyclerView.getAdapter()).setFilter(s.toString());
                    }
                    return true;
                }
            });
        }

        super.onCreateOptionsMenu(menu, inflater);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.action_add:
                Intent intent = new Intent(this.getActivity(), NewGroupActivity.class);
                this.getActivity().startActivity(intent);
                //this.getActivity().finish();
                Log.d("GroupListAdapter", "Adding a new group ");
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public boolean onQueryTextSubmit(String s) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String s) {
        return false;
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
        mFragment = this;
        allUsers = mFragment.getArguments().getBoolean("allGroups", true);
        if(allUsers){
            getLoaderManager().initLoader(1, null, mAllGroupsLoader);
        }else {
            getLoaderManager().initLoader(1, null, mGroupsForUserLoader);
        }
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if(allUsers){
                    getLoaderManager().restartLoader(1, null, mAllGroupsLoader);
                }else {
                    getLoaderManager().restartLoader(1, null, mGroupsForUserLoader);
                }
            }
        });
        mSwipeRefreshLayout.setColorSchemeResources(R.color.theme_primary_dark);
        mEmpty = (TextView)rootView.findViewById(R.id.empty);
        mLoading = (ProgressBar)rootView.findViewById(R.id.loading);
        mEmpty.setVisibility(View.INVISIBLE);
        mRecyclerView.setVisibility(View.VISIBLE);
        mLoading.setVisibility(View.VISIBLE);
        if(allUsers){
            mEmpty.setText("There are currently no groups. \n Be the first to create a group!");
        }else {
            mEmpty.setText("You are not a member of a group. \n Join a group in the Groups section!");
        }
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

    private LoaderManager.LoaderCallbacks<GroupsBean> mGroupsForUserLoader
            = new LoaderManager.LoaderCallbacks<GroupsBean>() {
        @Override
        public Loader<GroupsBean> onCreateLoader(int id, Bundle bundle) {
            Log.d(TAG, "onCreateLoader");
            return new GroupsForUserLoader(getActivity());

        }

        /*
        Note:
        Adjusted Loader to only load the groups where the user is a member
         */
        @Override
        public void onLoadFinished(Loader<GroupsBean> objectLoader, GroupsBean allGroupsBean) {
            Log.d(TAG, "onLoadFinished Groups For User");
            /**************************************
             Resultaat kan null zijn
             Rekening mee houden!
             **************************************/
            if (allGroupsBean != null && allGroupsBean.getGroups() != null) {
                mAdapter.setGroups((ArrayList<GroupBean>) allGroupsBean.getGroups());
                Log.d(TAG, "amount of groups : " + allGroupsBean.getGroups().size());
                mAdapter.setContext(getActivity());
                mRecyclerView.setAdapter(mAdapter);
                mEmpty.setVisibility(View.INVISIBLE);
                mLoading.setVisibility(View.INVISIBLE);
            }else {
                mEmpty.setVisibility(View.VISIBLE);
                mLoading.setVisibility(View.INVISIBLE);
            }
            mRecyclerView.setVisibility(View.VISIBLE);
            mSwipeRefreshLayout.setRefreshing(false);
        }

        @Override
        public void onLoaderReset(Loader<GroupsBean> objectLoader) {
            mRecyclerView.setAdapter(null);
        }
    };

    private LoaderManager.LoaderCallbacks<GroupsBean> mAllGroupsLoader
            = new LoaderManager.LoaderCallbacks<GroupsBean>() {
        @Override
        public Loader<GroupsBean> onCreateLoader(int id, Bundle bundle) {
            Log.d(TAG, "onCreateLoader");
            return new AllGroupsLoader(getActivity());

        }

        /*
        Note:
        Adjusted Loader to only load the groups where the user is a member
         */
        @Override
        public void onLoadFinished(Loader<GroupsBean> objectLoader, GroupsBean allGroupsBean) {
            Log.d(TAG, "onLoadFinished All Groups");
            /**************************************
             Resultaat kan null zijn
             Rekening mee houden!
             **************************************/
            if (allGroupsBean != null && allGroupsBean.getGroups() != null) {
                mAdapter.setGroups((ArrayList<GroupBean>) allGroupsBean.getGroups());
                Log.d(TAG, "amount of groups : " + allGroupsBean.getGroups().size());
                mAdapter.setContext(getActivity());
                mRecyclerView.setAdapter(mAdapter);
                mEmpty.setVisibility(View.INVISIBLE);
                mLoading.setVisibility(View.INVISIBLE);
            }else{
                mEmpty.setVisibility(View.VISIBLE);
                mLoading.setVisibility(View.INVISIBLE);
            }
            mRecyclerView.setVisibility(View.VISIBLE);
            mSwipeRefreshLayout.setRefreshing(false);
        }

        @Override
        public void onLoaderReset(Loader<GroupsBean> objectLoader) {
            mRecyclerView.setAdapter(null);
        }
    };
}