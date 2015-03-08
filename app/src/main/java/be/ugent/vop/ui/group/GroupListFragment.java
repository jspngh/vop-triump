package be.ugent.vop.ui.group;

import android.app.Activity;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Loader;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
    private final int CELL_DEFAULT_HEIGHT = 150;
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
        return new AllGroupsLoader(getActivity());
    }

    @Override
    public void onLoadFinished(Loader<AllGroupsBean> objectLoader, AllGroupsBean allGroupsBean) {


        List<GroupBean> Groups = allGroupsBean.getGroups();
        NUM_OF_CELLS = Groups.size();
        Long[] allgroupids = new Long[NUM_OF_CELLS];

        String text = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Praesent vitae enim dapibus, tincidunt mi ut, dignissim dolor. " +
                "Donec tristique eget lorem sagittis lacinia. Fusce iaculis lobortis tellus, dictum sodales nisl dapibus eu. " +
                "Aenean vitae convallis ipsum, non sodales tellus. Pellentesque ex nunc, suscipit quis vestibulum in, cursus ac risus. " +
                "Mauris vel massa luctus, porta odio sed, facilisis mi. Nunc tincidunt posuere imperdiet. Sed nec massa sit amet quam sollicitudin semper a at ligula. " +
                "Integer eleifend sed ipsum luctus commodo. Quisque vel ante sit amet tellus venenatis fermentum vel sed ante. Nam tempus purus vitae urna ultrices consequat. " +
                "Suspendisse accumsan felis quis pretium mollis.";

        ExpandableListItem[] values = new ExpandableListItem[NUM_OF_CELLS];
         for(int i = 0; i < NUM_OF_CELLS; i++){
            values[i] = new ExpandableListItem(Groups.get(i).getName(), "ass", R.drawable.ic_launcher, CELL_DEFAULT_HEIGHT,
                text);
            allgroupids[i] = Groups.get(i).getGroupId();
        }

        List<ExpandableListItem> mData = new ArrayList<>();

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

