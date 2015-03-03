package be.ugent.vop.ui.group;

import android.app.LoaderManager;
import android.content.Context;
import android.content.Loader;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.koushikdutta.ion.Ion;

import be.ugent.vop.R;
import be.ugent.vop.backend.myApi.model.GroupBean;
import be.ugent.vop.loaders.GroupBeanLoader;

public class GroupFragment extends Fragment implements LoaderManager.LoaderCallbacks<GroupBean> {
    private ImageView groupImageView;
    private TextView id;
    private TextView name;
    private TextView admin;
    private TextView created;


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
        return new GroupBeanLoader(context, groupId);
    }

    @Override
    public void onLoaderReset(Loader<GroupBean> loader) {
    }
}
