package be.ugent.vop;

import android.app.Fragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import be.ugent.vop.foursquare.FoursquareAPI;
import be.ugent.vop.ui.main.MainActivity;

public class FirstLaunchFragment extends Fragment {

    public FirstLaunchFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_first_launch, container, false);
        Button backButton = (Button) rootView.findViewById(R.id.back);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FoursquareAPI.get(getActivity()).checkIn("4b7e4c44f964a520b8e82fe3");
                //SharedPreferences settings = getActivity().getSharedPreferences(getString(R.string.sharedprefs), 0);
                //SharedPreferences.Editor editor = settings.edit();
                //editor.putBoolean(getString(R.string.first_launch), false);
                //editor.apply();
                Intent main = new Intent(getActivity(), MainActivity.class);
                startActivity(main);
                getActivity().finish();
            }
        });
        return rootView;
    }
}
