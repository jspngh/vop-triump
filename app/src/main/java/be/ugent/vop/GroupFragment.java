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
        import be.ugent.vop.backend.myApi.model.AuthTokenResponse;

        import java.io.IOException;
public class GroupFragment   extends Fragment {
    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private static final String ARG_SECTION_NUMBER = "section_number";


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
        Long fsUserID = prefs.getLong(getString(R.string.userid), 0);
        String fsToken = prefs.getString(getString(R.string.foursquaretoken), "N.A.");
        EndpointsAsyncTask api_service = new EndpointsAsyncTask();
        try {
            AuthTokenResponse token = new AuthTokenResponse();
            token = api_service.myApiService.getAuthToken(fsUserID, fsToken).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((MainActivity) activity).onSectionAttached(
                getArguments().getInt(ARG_SECTION_NUMBER));
    }

}

