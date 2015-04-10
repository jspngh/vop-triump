package be.ugent.vop.ui.login;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.ViewTarget;

import be.ugent.vop.R;
import be.ugent.vop.ui.group.GroupListActivity;
import be.ugent.vop.ui.main.MainActivity;
import be.ugent.vop.utils.PrefUtils;

public class TutorialFragment extends Fragment {
    private final static String EXTRA_POSITION = "fragmentPosition";
    private int position;

    public static TutorialFragment newInstance(int position)
    {
        TutorialFragment f = new TutorialFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(EXTRA_POSITION, position);
        f.setArguments(bundle);
        return f;
    }


    public TutorialFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        position = getArguments().getInt(EXTRA_POSITION, 1);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView;
        switch (position) {
            case 1:
                rootView = inflater.inflate(R.layout.fragment_tutorial_2, container, false);
                Button searchButton = (Button) rootView.findViewById(R.id.search);
                searchButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent group = new Intent(getActivity(), GroupListActivity.class);
                        startActivity(group);
                    }
                });
                break;
            case 2:
                rootView = inflater.inflate(R.layout.fragment_tutorial_3, container, false);
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
                break;
            default:
                rootView = inflater.inflate(R.layout.fragment_tutorial_1, container, false);
                break;
        }
        return rootView;
    }
}
