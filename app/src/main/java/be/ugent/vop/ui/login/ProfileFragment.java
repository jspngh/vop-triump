package be.ugent.vop.ui.login;

import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import be.ugent.vop.R;

public class ProfileFragment extends Fragment {
    public final static String USER_ID = "userId";
    public final static String PROFILE_ACTIVITY = "Go to profile";
    private long userId;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        Bundle bundle = this.getArguments();
        userId = bundle.getLong(USER_ID, 0);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_profile, container, false);
        TextView txt = (TextView) rootView.findViewById(R.id.userId);
        txt.setText(""+ userId);
        return rootView;
    }


}
