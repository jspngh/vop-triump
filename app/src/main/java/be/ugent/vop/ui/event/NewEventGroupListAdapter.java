package be.ugent.vop.ui.event;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import be.ugent.vop.R;
import be.ugent.vop.backend.myApi.model.GroupBean;
import be.ugent.vop.backend.myApi.model.RankingBean;



/**
 * Created by vincent on 25/03/15.
 */

public class NewEventGroupListAdapter extends ArrayAdapter<GroupBean> {
    private int resourceId;
    private NewEventFragment parent;

    public NewEventGroupListAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);

    }

    public NewEventGroupListAdapter(Context context, List<GroupBean> items) {
        super(context, R.layout.group_new_event_list_item, items);
        this.resourceId = R.layout.group_new_event_list_item;
    }

    @Override
    public View getView(int position, View convertView, final ViewGroup parent) {

        View v = convertView;

        if (v == null) {
            v = LayoutInflater.from(getContext()).inflate(R.layout.group_new_event_list_item, parent, false);
        }

        GroupBean r = getItem(position);

        CheckedTextView ctw = (CheckedTextView) v.findViewById(R.id.checkedTextViewGroup);

        if(r!=null){
            ctw.setText(r.getName());


        ctw.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v)
            {
                ((CheckedTextView) v).toggle();
            }
        });
        }
        return v;
    }


}