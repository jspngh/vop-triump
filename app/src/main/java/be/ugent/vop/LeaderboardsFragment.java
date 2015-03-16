package be.ugent.vop;

import android.app.Activity;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.koushikdutta.ion.Ion;

import java.util.ArrayList;
import java.util.List;

import be.ugent.vop.Event;
import be.ugent.vop.EventBroker;
import be.ugent.vop.R;
import be.ugent.vop.backend.loaders.CheckInLoader;
import be.ugent.vop.backend.loaders.LeaderboardLoader;
import be.ugent.vop.backend.loaders.RankingLoader;
import be.ugent.vop.backend.myApi.model.AllGroupsBean;
import be.ugent.vop.backend.myApi.model.GroupBean;
import be.ugent.vop.backend.myApi.model.RankingBean;
import be.ugent.vop.backend.myApi.model.VenueBean;
import be.ugent.vop.foursquare.FoursquareVenue;
import be.ugent.vop.ui.group.GroupActivity;
import be.ugent.vop.ui.list.CustomArrayAdapter;
import be.ugent.vop.ui.list.ExpandableListItem;
import be.ugent.vop.ui.list.ExpandingListView;
import be.ugent.vop.ui.venue.RankingAdapter;
import be.ugent.vop.ui.venue.VenueActivity;


public class LeaderboardsFragment extends Fragment implements LoaderManager.LoaderCallbacks<List<RankingBean>>{

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


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_leaderboard, container, false);
        rankingListView = (ListView) rootView.findViewById(R.id.ranking_list);
        noRankingTextView = (TextView) rootView.findViewById(R.id.noRankingTextView);
        context = getActivity();
        activity = getActivity();
        getLoaderManager().initLoader(0, null, this);

        return rootView;
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public Loader<List<RankingBean>> onCreateLoader(int id, Bundle args) {
        Log.d("LeaderboardsFragment", "onCreateLoader");
        LeaderboardLoader loader = new LeaderboardLoader(context);
        return loader;
    }

    @Override
    public void onLoadFinished(Loader<List<RankingBean>> loader, List<RankingBean> data) {
        Log.d("LeaderboardsFragment", "onLoadFinished");

        if(data.size()!=0){
            noRankingTextView.setVisibility(View.GONE);
            rankingListView.setVisibility(View.VISIBLE);
            Log.d("LeaderboardsFragment","size of data " + data.size());
            ranking = new ArrayList<RankingBean>();
            for(RankingBean r : data) ranking.add(r);
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
        }else{
            noRankingTextView.setText(R.string.no_ranking);
        }
    }


    @Override
    public void onLoaderReset(Loader<List<RankingBean>> loader) {

    }
}