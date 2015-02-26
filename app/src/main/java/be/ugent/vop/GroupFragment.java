package be.ugent.vop;

import android.app.Activity;
import android.os.Bundle;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.List;

import be.ugent.vop.backend.myApi.model.AllGroupsBean;
import be.ugent.vop.backend.myApi.model.GroupBean;
import be.ugent.vop.loaders.AllGroupsLoader;


public class GroupFragment extends Fragment implements LoaderManager.LoaderCallbacks<AllGroupsBean> {
    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private static final String ARG_SECTION_NUMBER = "section_number";
    private ListView groupslistView;
    private ArrayAdapter arrayAdapter;
    private String[] groupArray;

    private MainActivity mainActivity = null;
    public GroupFragment()
    {

    }

    public static GroupFragment newInstance(int sectionNumber) {
        GroupFragment fragment = new GroupFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_group, container, false);
        groupslistView = (ListView) rootView.findViewById(R.id.groups_list);

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
        Log.d("","Number of groups: " + Groups.size());
        groupArray = new String[Groups.size()];
        for(int i = 0; i < Groups.size(); i++){
            groupArray[i] = Groups.get(i).getName();
        }

        arrayAdapter = new ArrayAdapter(this.getActivity(), android.R.layout.simple_list_item_1, groupArray);
        groupslistView.setAdapter(arrayAdapter);
    }

    @Override
    public void onLoaderReset(Loader<AllGroupsBean> objectLoader) {

    }
}

