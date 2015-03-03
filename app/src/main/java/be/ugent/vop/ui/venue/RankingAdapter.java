package be.ugent.vop.ui.venue;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import be.ugent.vop.ui.group.Group;
import be.ugent.vop.R;

/**
 * Created by vincent on 03/03/15.
 */

public class RankingAdapter extends ArrayAdapter<Group> {
    private int resourceId;

    public RankingAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
    }

    public RankingAdapter(Context context, int resource, ArrayList<Group> items) {
        super(context, resource, items);
        this.resourceId = resource;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View v = convertView;

        if (v == null) {
            Log.d("Adapter","Here");

            v = LayoutInflater.from(getContext()).inflate(resourceId, parent, false);


        }

        Group p = getItem(position);
        Log.d("Adapter", p.getName());

        if (p != null) {

            TextView nameTextView = (TextView) v.findViewById(R.id.textViewName);
            TextView nrTextView = (TextView) v.findViewById(R.id.textViewNr);
            TextView pointsTextView = (TextView) v.findViewById(R.id.textViewPoints);

            if (nameTextView != null) {
                nameTextView.setText(p.getName());
            }
            if (nrTextView != null) {

                nrTextView.setText("" + position);
            }
            if (pointsTextView != null) {

                pointsTextView.setText(""+p.getPoints());
            }
        }

        return v;

    }
}