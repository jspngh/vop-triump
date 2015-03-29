package be.ugent.vop.ui.leaderboard;

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

import be.ugent.vop.R;
import be.ugent.vop.backend.loaders.LeaderboardLoader;
import be.ugent.vop.backend.myApi.model.RankingBean;
import be.ugent.vop.ui.group.GroupActivity;

import be.ugent.vop.ui.venue.RankingAdapter;
import be.ugent.vop.utils.RangeSeekBar;


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
    public LeaderboardsFragment(){}
    RangeSeekBar<Integer> seekBar;
    int minMembers = -1;
    int maxMembers = -1;
    private static final int MIN_PARTICIPANTS = 1;
    private static final int MAX_PARTICIPANTS = 1000;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_leaderboard, container, false);
        rankingListView = (ListView) rootView.findViewById(R.id.ranking_list);
        noRankingTextView = (TextView) rootView.findViewById(R.id.noRankingTextView);
        context = getActivity();
        activity = getActivity();

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
            LeaderboardLoader loader = new LeaderboardLoader(context, MIN_PARTICIPANTS, MAX_PARTICIPANTS, "All");
            return loader;
        }

        @Override
        public void onLoadFinished(Loader<List<RankingBean>> loader, List<RankingBean> data) {
            Log.d("LeaderboardsFragment", "onLoadFinished");
                if (data!=null &&data.size() != 0) {
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