package be.ugent.vop.ui.group;

import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import com.koushikdutta.ion.Ion;

import org.w3c.dom.Text;

import be.ugent.vop.R;

public class GroupFragment extends Fragment {
    private ImageView venueImageView;

    private long groupId;
    public GroupFragment() {
        // Required empty public constructor
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = this.getArguments();
        groupId = bundle.getLong("groupId", -1);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_group, container, false);
        venueImageView = (ImageView) rootView.findViewById(R.id.imageView);
        TextView id = (TextView) rootView.findViewById(R.id.groupId);
        id.setText(""+groupId);
        String photoUrl = "http://www.beeldarchief.ugent.be/fotocollectie/gebouwen/images/prevs/prev64.jpg";
        Ion.with(venueImageView)
            .placeholder(R.drawable.ic_launcher)
            .error(R.drawable.ic_drawer_logout)
            .load(photoUrl);
        return rootView;
    }
}
