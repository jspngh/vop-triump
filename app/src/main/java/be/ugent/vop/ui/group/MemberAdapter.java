package be.ugent.vop.ui.group;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import be.ugent.vop.R;

/**
 * Created by jonas on 8-3-2015.
 */
public class MemberAdapter extends ArrayAdapter<String> {
    private final Context context;
    private final ArrayList<String> members;

    public MemberAdapter(Context context, ArrayList<String> members) {
        super(context, R.layout.member_list_item, members);
        this.context = context;
        this.members = members;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.member_list_item, parent, false);
        TextView name = (TextView) rowView.findViewById(R.id.name);
        TextView info = (TextView) rowView.findViewById(R.id.info);
        ImageView imageView = (ImageView) rowView.findViewById(R.id.icon);
        name.setText(members.get(position));

        return rowView;
    }
}
