package be.ugent.vop;

import android.app.Activity;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import be.ugent.vop.backend.loaders.LeaderboardLoader;
import be.ugent.vop.backend.myApi.model.RankingBean;
import be.ugent.vop.ui.group.GroupActivity;

import be.ugent.vop.ui.venue.RankingAdapter;


public class LeaderboardsFragment extends Fragment{

    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private Context context;
    private static Activity activity;
    private ListView rankingListView;
    private TextView noRankingTextView;
    private RankingAdapter adapter;
    private ArrayList<RankingBean> ranking;
    private Spinner groupTypeSpinner;
    private Spinner groupSizeSpinner;
    public LeaderboardsFragment(){}


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_leaderboard, container, false);
        rankingListView = (ListView) rootView.findViewById(R.id.ranking_list);
        noRankingTextView = (TextView) rootView.findViewById(R.id.noRankingTextView);
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
        getLoaderManager().initLoader(0, null, mLeaderboardLoaderListener);
        groupSizeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                getLoaderManager().restartLoader(0,null,mLeaderboardLoaderListener);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }

        });

        groupTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                getLoaderManager().restartLoader(0, null, mLeaderboardLoaderListener);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }

        });

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
            LeaderboardLoader loader = new LeaderboardLoader(context,groupSizeSpinner.getSelectedItem().toString(),groupTypeSpinner.getSelectedItem().toString());
            return loader;
        }

        @Override
        public void onLoadFinished(Loader<List<RankingBean>> loader, List<RankingBean> data) {
            Log.d("LeaderboardsFragment", "onLoadFinished");

            if (data.size() != 0) {
                noRankingTextView.setVisibility(View.GONE);
                rankingListView.setVisibility(View.VISIBLE);
                Log.d("LeaderboardsFragment", "size of data " + data.size());
                ranking = new ArrayList<RankingBean>();
                for (RankingBean r : data) ranking.add(r);
                adapter = new RankingAdapter(context, ranking);
                rankingListView.setAdapter(adapter);

                rankingListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view,
                                            int position, long id) {
                        Intent intent = new Intent(getActivity(), GroupActivity.class);
                        Bundle bundle = new Bundle();
                        bundle.putLong("groupId", ranking.get(position).getGroupBean().getGroupId());
                        intent.putExtras(bundle);
                        startActivity(intent);
                    }
                });
            } else {
                noRankingTextView.setText(R.string.no_ranking);
            }
        }


        @Override
        public void onLoaderReset(Loader<List<RankingBean>> loader) {

        }
    };
}