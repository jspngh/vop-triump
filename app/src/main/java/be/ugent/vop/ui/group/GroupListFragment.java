package be.ugent.vop.ui.group;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListAdapter;

import java.util.ArrayList;
import java.util.List;

import be.ugent.vop.R;
import be.ugent.vop.backend.myApi.model.AllGroupsBean;
import be.ugent.vop.backend.myApi.model.GroupBean;
import be.ugent.vop.loaders.AllGroupsLoader;
import be.ugent.vop.ui.list.CustomArrayAdapter;
import be.ugent.vop.ui.list.ExpandableListItem;
import be.ugent.vop.ui.list.ExpandingListView;
import be.ugent.vop.ui.main.MainActivity;


public class GroupListFragment extends Fragment implements LoaderManager.LoaderCallbacks<AllGroupsBean> {
    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private static final String ARG_SECTION_NUMBER = "section_number";
    private static Activity activity;
    private ListAdapter arrayAdapter;
    private final int CELL_DEFAULT_HEIGHT = 88;
    private int NUM_OF_CELLS = 30;

    private ExpandingListView mListView;

    private MainActivity mainActivity = null;
    public GroupListFragment()
    {

    }

    public static GroupListFragment newInstance(int sectionNumber) {
        GroupListFragment fragment = new GroupListFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_group_list, container, false);
        activity = getActivity();
        getLoaderManager().initLoader(0, null, this);
        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
       // ((MainActivity) activity).onSectionAttached(getArguments().getInt(ARG_SECTION_NUMBER));
    }

    @Override
    public Loader<AllGroupsBean> onCreateLoader(int id, Bundle bundle) {
        AllGroupsLoader loader = new AllGroupsLoader(getActivity());
        return loader;
    }

    @Override
    public void onLoadFinished(Loader<AllGroupsBean> objectLoader, AllGroupsBean allGroupsBean) {


        List<GroupBean> Groups = allGroupsBean.getGroups();
        Long[] allgroupids = new Long[Groups.size()];

        ExpandableListItem[] values = new ExpandableListItem[Groups.size()];
         for(int i = 0; i < Groups.size(); i++){
            values[i] = new ExpandableListItem(Groups.get(i).getName(), "ass", R.drawable.ic_launcher, CELL_DEFAULT_HEIGHT,
                "tits");
            allgroupids[i] = Groups.get(i).getGroupId();
        }

        List<ExpandableListItem> mData = new ArrayList<ExpandableListItem>();

        for (int i = 0; i < NUM_OF_CELLS; i++) {
            ExpandableListItem obj = values[i % values.length];
            mData.add(new ExpandableListItem(obj.getTitle(),obj.getInfo(), obj.getImgResource(),
                    obj.getCollapsedHeight(), obj.getText()));
        }

        CustomArrayAdapter adapter = new CustomArrayAdapter(activity, R.layout.group_list_item, mData, allgroupids);

        mListView = (ExpandingListView)activity.findViewById(R.id.group_list_view);
        mListView.setAdapter(adapter);
        mListView.setDivider(null);

    }

    @Override
    public void onLoaderReset(Loader<AllGroupsBean> objectLoader) {

    }
}

