package be.ugent.vop.ui.group;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.util.Pair;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import be.ugent.vop.R;
import be.ugent.vop.ui.login.LoginActivity;
import be.ugent.vop.ui.login.ProfileFragment;

/**
 * Created by jonas on 8-3-2015.
 */
public class MemberAdapter extends ArrayAdapter<Pair<String, Long>> {
    private final Context context;
    private final ArrayList<Pair<String, Long>> members;

    public MemberAdapter(Context context, ArrayList<Pair<String, Long>> members) {
        super(context, R.layout.member_list_item, members);
        this.context = context;
        this.members = members;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final Long userId = members.get(position).second;
        View rowView = inflater.inflate(R.layout.member_list_item, parent, false);
        rowView.setClickable(true);
        rowView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent profileIntent = new Intent(context, LoginActivity.class);
                profileIntent.putExtra(context.getString(R.string.profile), ProfileFragment.PROFILE_ACTIVITY);
                profileIntent.putExtra(ProfileFragment.USER_ID, userId);
                context.startActivity(profileIntent);
            }
        });

        TextView name = (TextView) rowView.findViewById(R.id.name);
        TextView info = (TextView) rowView.findViewById(R.id.info);
        ImageView imageView = (ImageView) rowView.findViewById(R.id.icon);
        name.setText(members.get(position).first);

        return rowView;
    }
}
