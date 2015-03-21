package be.ugent.vop.ui.group;

import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Context;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.koushikdutta.ion.Ion;

import java.util.ArrayList;

import be.ugent.vop.R;
import be.ugent.vop.backend.myApi.model.GroupBean;
import be.ugent.vop.backend.myApi.model.UserBean;
import be.ugent.vop.backend.loaders.GroupBeanLoader;
import be.ugent.vop.backend.loaders.JoinGroupLoader;

public class GroupFragment extends Fragment {
    private ImageView groupImageView;
    private TextView description;
    private TextView name;
    private TextView type;
    private TextView created;
    private ListView membersView;
    private Button btn;


    private String token;
    private long groupId;
    private Context context = null;

    public GroupFragment() {
        // Required empty public constructor
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = this.getArguments();
        groupId = bundle.getLong("groupId", 0);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_group, container, false);
        context = getActivity();

        groupImageView = (ImageView) rootView.findViewById(R.id.imageView);
        description = (TextView) rootView.findViewById(R.id.description);
        name = (TextView) rootView.findViewById(R.id.name);
        type = (TextView) rootView.findViewById(R.id.type);
        created = (TextView) rootView.findViewById(R.id.created);
        membersView = (ListView) rootView.findViewById(R.id.membersView);

        String photoUrl = "http://www.beeldarchief.ugent.be/fotocollectie/gebouwen/images/prevs/prev64.jpg";
        Ion.with(groupImageView)
            .placeholder(R.drawable.ic_launcher)
            .error(R.drawable.ic_drawer_logout)
            .load(photoUrl);
        btn = (Button) rootView.findViewById(R.id.joinbtn);
            btn.setVisibility(View.VISIBLE);
        btn.setFocusable(false);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getLoaderManager().initLoader(1, null, mJoinGroupLoaderListener);
            }
        });
        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        getLoaderManager().initLoader(0, null, mGroupBeanLoaderListener);
    }

    /***********
       Loaders
     ***********/

    private LoaderManager.LoaderCallbacks<GroupBean> mGroupBeanLoaderListener
            = new LoaderManager.LoaderCallbacks<GroupBean>() {
        @Override
        public void onLoadFinished(Loader<GroupBean> loader, GroupBean response) {
            if (response != null){

                name.setText("Name: " + response.getName());
                description.setText(response.getDescription().toString());
                created.setText("Created on: " + response.getCreated().toString());
                type.setText(response.getType().toString() + " group");
                ArrayList<String> members = new ArrayList<>();
                for (UserBean user : response.getMembers()) {
                    members.add(user.getFirstName() + " " + user.getLastName());
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<String>( getActivity(), android.R.layout.simple_list_item_1, android.R.id.text1, members );
                membersView.setAdapter(adapter);
            }
        }

        @Override
        public Loader<GroupBean> onCreateLoader (int id, Bundle args){
            return new GroupBeanLoader(context, groupId);
        }

        @Override
        public void onLoaderReset(Loader<GroupBean> loader) {
            membersView.setAdapter(null);
        }
    };

    private LoaderManager.LoaderCallbacks<GroupBean> mJoinGroupLoaderListener
            = new LoaderManager.LoaderCallbacks<GroupBean>() {
        @Override
        public void onLoadFinished(Loader<GroupBean> loader, GroupBean response) {
            if (response != null) {
                btn.setVisibility(View.GONE);
                name.setText("Name: " + response.getName());
                description.setText(response.getDescription().toString());
                created.setText("Created on: " + response.getCreated().toString());
                type.setText(response.getType().toString() + " group");
                ArrayList<String> members = new ArrayList<>();
                for (UserBean user : response.getMembers()) {
                    members.add(user.getFirstName() + " " + user.getLastName());
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<String>( getActivity(), android.R.layout.simple_list_item_1, android.R.id.text1, members );
                membersView.setAdapter(adapter);
            }
        }

        @Override
        public Loader<GroupBean> onCreateLoader(int id, Bundle args) {
            return new JoinGroupLoader(context, groupId, token);
        }

        @Override
        public void onLoaderReset(Loader<GroupBean> loader) {
            membersView.setAdapter(null);
        }
    };
}
