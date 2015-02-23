package be.ugent.vop;

/**
 * Created by Lars on 21/02/15.
 */

        import android.app.Activity;
        import android.content.Context;
        import android.content.SharedPreferences;
        import android.os.Bundle;
        import android.support.v4.util.Pair;
        import android.view.LayoutInflater;
        import android.view.View;
        import android.view.ViewGroup;
        import android.support.v4.app.Fragment;
        import be.ugent.vop.backend.myApi.MyApi;
        import be.ugent.vop.backend.myApi.model.AllGroupsBean;
        import be.ugent.vop.backend.myApi.model.AuthTokenResponse;
        import be.ugent.vop.backend.myApi.model.GroupBean;

        import android.util.Log;
        import android.widget.ArrayAdapter;
        import android.widget.ListView;
        import android.widget.Toast;

        import java.io.IOException;
        import java.util.List;


public class GroupFragment   extends Fragment {
    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private static final String ARG_SECTION_NUMBER = "section_number";
    private ListView groupslistView;
    private ArrayAdapter arrayAdapter;
    private String[] groupArray;
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
        SharedPreferences prefs = this.getActivity().getSharedPreferences(getString(R.string.sharedprefs), Context.MODE_PRIVATE);
        String backendToken = prefs.getString(getString(R.string.backendtoken), "N.A.");
        groupslistView = (ListView) rootView.findViewById(R.id.groups_list);


        String[] getAllGroups = {"getAllGroups", backendToken};
        try {
            AllGroupsBean AllGroups = (AllGroupsBean)new EndpointsAsyncTask(this.getActivity()).execute(getAllGroups).get();
            List<GroupBean> Groups = AllGroups.getGroups();
            Log.d("","Number of groups: " + Groups.size());
            groupArray = new String[Groups.size()];
            for(int i = 0; i < Groups.size(); i++){
                groupArray[i] = Groups.get(i).getName();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        arrayAdapter = new ArrayAdapter(this.getActivity(), android.R.layout.simple_list_item_1, groupArray);
        groupslistView.setAdapter(arrayAdapter);

        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((MainActivity) activity).onSectionAttached(
                getArguments().getInt(ARG_SECTION_NUMBER));
    }

}

