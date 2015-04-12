package be.ugent.vop.ui.reward;


import android.app.Activity;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Context;
import android.content.Loader;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import be.ugent.vop.R;
import be.ugent.vop.backend.myApi.model.EventRewardBean;

/**
 * Created by vincent on 10/04/15.
 */
public class RewardsFragment extends Fragment {

    private Context context;
    private ListView rewardsListView;



    public RewardsFragment(){}


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_rewards, container, false);
        rewardsListView = (ListView) rootView.findViewById(R.id.event_list);

        return rootView;
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    private LoaderManager.LoaderCallbacks<EventRewardBean> mEventLoaderListener
            = new LoaderManager.LoaderCallbacks<EventRewardBean>() {
        @Override
        public Loader<EventRewardBean> onCreateLoader(int id, Bundle args) {
                return null;
        }

        @Override
        public void onLoadFinished(Loader<EventRewardBean> loader, EventRewardBean data) {

        }


        @Override
        public void onLoaderReset(Loader<EventRewardBean> loader) {

        }
    };
}
