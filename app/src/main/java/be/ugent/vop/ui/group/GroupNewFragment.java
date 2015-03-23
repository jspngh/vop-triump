package be.ugent.vop.ui.group;

import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Context;
import android.content.Loader;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.koushikdutta.ion.Ion;

import java.util.ArrayList;

import be.ugent.vop.R;
import be.ugent.vop.backend.loaders.CreateGroupLoader;
import be.ugent.vop.backend.myApi.model.GroupBean;
import be.ugent.vop.backend.myApi.model.UserBean;
import be.ugent.vop.backend.loaders.GroupBeanLoader;
import be.ugent.vop.backend.loaders.JoinGroupLoader;

public class GroupNewFragment extends Fragment {
    private EditText name;
    private EditText description;
    private Spinner type;
    private Button btn;
    private TextView result;


    private String token;
    private Context context = null;

    public GroupNewFragment() {
        // Required empty public constructor
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_group_new, container, false);
        context = getActivity();

        description = (EditText) rootView.findViewById(R.id.group_description_edit);
        name = (EditText) rootView.findViewById(R.id.group_name_edit);
        result = (TextView) rootView.findViewById(R.id.result);
        type = (Spinner) rootView.findViewById(R.id.group_type_spinner);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapterType = ArrayAdapter.createFromResource(context,
                R.array.groupType_spinner_options, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapterType.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        type.setAdapter(adapterType);
        btn = (Button) rootView.findViewById(R.id.add_button);
        result.setVisibility(View.GONE);
        btn.setFocusable(false);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getLoaderManager().initLoader(1, null, mCreateGroupLoaderListener);
            }
        });
        return rootView;
    }

    private LoaderManager.LoaderCallbacks<GroupBean> mCreateGroupLoaderListener
            = new LoaderManager.LoaderCallbacks<GroupBean>() {
        @Override
        public void onLoadFinished(Loader<GroupBean> loader, GroupBean response) {
            if (response != null){
                btn.setVisibility(View.GONE);
                result.setVisibility(View.VISIBLE);
                Log.d("GroupNewFragment", "group added successfully");
            }
        }

        @Override
        public Loader<GroupBean> onCreateLoader (int id, Bundle args){
            return new CreateGroupLoader(context, name.getText().toString(), description.getText().toString(), type.getSelectedItem().toString());
        }

        @Override
        public void onLoaderReset(Loader<GroupBean> loader) {

        }
    };


}