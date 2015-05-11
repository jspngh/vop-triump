package be.ugent.vop.ui.leaderboard;

import android.app.Activity;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Context;
import android.content.Loader;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import be.ugent.vop.R;
import be.ugent.vop.backend.loaders.LeaderboardLoader;
import be.ugent.vop.backend.myApi.model.RankingBean;
import be.ugent.vop.ui.venue.RankingAdapter;
import be.ugent.vop.ui.venue.SizeSelectorDialog;
import be.ugent.vop.utils.RangeSeekBar;


public class LeaderboardsFragment extends Fragment {

    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private Context context;
    private static Activity activity;
    private RecyclerView rankingView;
    private TextView noRankingTextView;
    private RankingAdapter adapter;
    private ArrayList<RankingBean> ranking;
    private LinearLayoutManager mLayoutManager;
    private Spinner groupTypeSpinner;
    private Spinner groupSizeSpinner;
    private View mSpinners;
    private String currentGroupType = "All";
    private String currentGroupSize = "All";
    protected SwipeRefreshLayout mSwipeRefreshLayout;
    public LeaderboardsFragment(){}

    private int customMin;
    private int customMax;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_leaderboard, container, false);
        rankingView = (RecyclerView) rootView.findViewById(R.id.ranking_list);
        noRankingTextView = (TextView) rootView.findViewById(R.id.noRankingTextView);
        mSpinners = rootView.findViewById(R.id.spinners);

        mLayoutManager = new LinearLayoutManager(getActivity());
        rankingView.setLayoutManager(mLayoutManager);
        rankingView.setItemAnimator(new DefaultItemAnimator());

        context = getActivity();
        activity = getActivity();

        groupTypeSpinner = (Spinner) rootView.findViewById(R.id.spinnerGroupType);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapterType = ArrayAdapter.createFromResource(context,
                R.array.groupType_spinner_options, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapterType.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        groupTypeSpinner.setAdapter(adapterType);

        /**
         * groupSize
         */
        groupSizeSpinner = (Spinner) rootView.findViewById(R.id.spinnerGroupSize);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapterSize = ArrayAdapter.createFromResource(context,
                R.array.groupSize_spinner_options, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapterSize.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        groupSizeSpinner.setAdapter(adapterSize);
        groupSizeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                String selected = (String) parentView.getSelectedItem();
                Log.d("VenueFragment", "selected group size: "+selected);

                if(parentView.getSelectedItemPosition() == 4){
                    currentGroupSize = selected;
                    SizeSelectorDialog dialog = new SizeSelectorDialog();
                    dialog.setListener(mSizeSelectorListener);
                    dialog.show(getFragmentManager(), "sizeselector");
                }else if(!currentGroupSize.equals(selected)) {
                    currentGroupSize = selected;
                    getLoaderManager().restartLoader(0, null, mLeaderboardLoaderListener);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }

        });

        groupTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                String selected = (String) parentView.getSelectedItem();
                Log.d("VenueFragment", "selected group size: "+selected);
                if(!currentGroupType.equals(selected)){
                    currentGroupType = selected;
                    getLoaderManager().restartLoader(0,null, mLeaderboardLoaderListener);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }

        });
        mSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.fragment_venue_swipe_refresh);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getLoaderManager().restartLoader(1,null,mLeaderboardLoaderListener);
            }
        });
        mSwipeRefreshLayout.setColorSchemeResources(R.color.theme_primary_dark);
        getLoaderManager().initLoader(1, null, mLeaderboardLoaderListener);

        return rootView;
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    private LoaderManager.LoaderCallbacks<List<RankingBean>> mLeaderboardLoaderListener
            = new LoaderManager.LoaderCallbacks<List<RankingBean>>() {
        @Override
        public Loader<List<RankingBean>> onCreateLoader(int id, Bundle args) {
            Log.d("LeaderboardsFragment", "onCreateLoader");

            int groupTypePos = groupTypeSpinner.getSelectedItemPosition();
            String groupType = getResources().getStringArray(R.array.groupType_options)[groupTypePos];

            int groupSize = groupSizeSpinner.getSelectedItemPosition();

            int minSize = 1, maxSize = 1;

            switch(groupSize){
                case 0: // All
                    maxSize = Integer.MAX_VALUE;
                    break;
                case 1: // Small
                    maxSize = 10;
                    break;
                case 2: // Medium
                    minSize = 11;
                    maxSize = 50;
                    break;
                case 3: // Large
                    minSize = 51;
                    maxSize = Integer.MAX_VALUE;
                    break;
                case 4: // Custom
                    minSize = customMin;
                    maxSize = customMax;
                    break;
            }
            LeaderboardLoader loader = new LeaderboardLoader(context, minSize, maxSize, groupType);
            return loader;
        }

        @Override
        public void onLoadFinished(Loader<List<RankingBean>> loader, List<RankingBean> data) {
            Log.d("LeaderboardsFragment", "onLoadFinished");
                if (data!=null &&data.size() != 0) {
                    noRankingTextView.setVisibility(View.GONE);
                    rankingView.setVisibility(View.VISIBLE);
                    Log.d("LeaderboardsFragment", "size of data " + data.size());
                    ranking = new ArrayList<>();
                    for (RankingBean r : data) ranking.add(r);
                    int ht = 200;
                    int wt = 200;

                    float ht_px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, ht, getResources().getDisplayMetrics());
                    float wt_px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, wt, getResources().getDisplayMetrics());

                    //    noRankingTextView.setVisibility(View.INVISIBLE);
                    adapter = new RankingAdapter(getActivity(), ranking, Color.BLACK, Color.WHITE, (int)wt_px, (int)ht_px, false);
                    rankingView.setAdapter(adapter);
                } else {
                    noRankingTextView.setVisibility(View.VISIBLE);
                    rankingView.setVisibility(View.GONE);
                    noRankingTextView.setText(R.string.no_ranking);
                }
            mSwipeRefreshLayout.setRefreshing(false);

        }


        @Override
        public void onLoaderReset(Loader<List<RankingBean>> loader) {

        }
    };

    private SizeSelectorDialog.SizeSelectorListener mSizeSelectorListener = new SizeSelectorDialog.SizeSelectorListener() {
        @Override
        public void setNewSizes(int min, int max) {
            customMin = min;
            customMax = max;
            getLoaderManager().restartLoader(0, null, mLeaderboardLoaderListener);
        }
    };
}