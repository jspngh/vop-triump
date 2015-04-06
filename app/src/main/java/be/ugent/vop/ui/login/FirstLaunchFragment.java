package be.ugent.vop.ui.login;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import be.ugent.vop.R;
import be.ugent.vop.ui.main.MainActivity;
import be.ugent.vop.utils.PrefUtils;

public class FirstLaunchFragment extends Fragment {

    public FirstLaunchFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_first_launch, container, false);
        Button backButton = (Button) rootView.findViewById(R.id.back);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PrefUtils.setFirstLaunch(getActivity(), false);
                Intent main = new Intent(getActivity(), MainActivity.class);
                startActivity(main);
                getActivity().finish();
            }
        });
        return rootView;
    }
}
