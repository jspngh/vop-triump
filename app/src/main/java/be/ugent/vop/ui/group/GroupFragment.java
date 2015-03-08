package be.ugent.vop.ui.group;

import android.app.LoaderManager;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import com.koushikdutta.ion.Ion;

import be.ugent.vop.R;
import be.ugent.vop.backend.myApi.model.GroupBean;
import be.ugent.vop.loaders.GroupBeanLoader;
import be.ugent.vop.loaders.JoinGroupLoader;
import be.ugent.vop.ui.login.FirstLaunchFragment;

public class GroupFragment extends Fragment implements LoaderManager.LoaderCallbacks<GroupBean> {
    private ImageView groupImageView;
    private TextView id;
    private TextView name;
    private TextView admin;
    private TextView created;
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
        groupImageView = (ImageView) rootView.findViewById(R.id.imageView);
        id = (TextView) rootView.findViewById(R.id.groupId);
        name = (TextView) rootView.findViewById(R.id.name);
        admin = (TextView) rootView.findViewById(R.id.admin);
        created = (TextView) rootView.findViewById(R.id.created);
        id.setText(""+groupId);
        String photoUrl = "http://www.beeldarchief.ugent.be/fotocollectie/gebouwen/images/prevs/prev64.jpg";
        Ion.with(groupImageView)
            .placeholder(R.drawable.ic_launcher)
            .error(R.drawable.ic_drawer_logout)
            .load(photoUrl);
        SharedPreferences settings = context.getSharedPreferences(getString(R.string.sharedprefs),Context.MODE_PRIVATE);
        btn = (Button) rootView.findViewById(R.id.joinbtn);
         token = settings.getString(getString(R.string.foursquaretoken), "N.A.");
        if(settings.getLong(getString(R.string.group_id), 0)!=0) {
            btn.setVisibility(View.GONE);
        }else{
            btn.setVisibility(View.VISIBLE);
        }
        btn.setFocusable(false);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //getLoaderManager().restartLoader(1, null, null );
            }
        });
        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        context = getActivity();
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public void onLoadFinished(Loader<GroupBean> loader, GroupBean response) {
        if (response != null){
            name.setText(response.getName());
            admin.setText(response.getAdminId().toString());
            created.setText(response.getCreated().toString());
        }
    }

    @Override
    public Loader<GroupBean> onCreateLoader (int id, Bundle args){
        //if(id==0) {
            return new GroupBeanLoader(context, groupId);
        //}else if(id==1){
        //    return new JoinGroupLoader(context, groupId,token);
        //}
        //return null;
    }

    @Override
    public void onLoaderReset(Loader<GroupBean> loader) {
    }
}
