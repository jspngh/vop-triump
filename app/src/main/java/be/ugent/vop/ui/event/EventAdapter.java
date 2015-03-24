package be.ugent.vop.ui.event;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;


import be.ugent.vop.backend.myApi.model.EventBean;
import be.ugent.vop.backend.myApi.model.RankingBean;
import be.ugent.vop.R;

/**
 * Created by vincent on 03/03/15.
 */

public class EventAdapter extends ArrayAdapter<EventBean> {
    private int resourceId;

    public EventAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
    }

    public EventAdapter(Context context, List<EventBean> items) {
        super(context, R.layout.event_list_item, items);
        this.resourceId = R.layout.event_list_item;

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View v = convertView;

        if (v == null) {
            v = LayoutInflater.from(getContext()).inflate(R.layout.event_list_item, parent, false);
        }

        EventBean r = getItem(position);


        if (r != null) {
            TextView nameTextView = (TextView) v.findViewById(R.id.name);
            TextView infoTextView = (TextView) v.findViewById(R.id.info);

            if (nameTextView != null) {
                nameTextView.setText(r.getDescription());
            }

            if (infoTextView != null) {

                infoTextView.setText("Reward: " + r.getReward()+ " / at " + r.getVenue());
            }
        }
        return v;
    }


    public BitmapDrawable writeOnDrawable(int drawableId, String text){

        Bitmap bm = BitmapFactory.decodeResource(getContext().getResources(), drawableId).copy(Bitmap.Config.ARGB_8888, true);

        Paint paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.BLACK);
        paint.setTextSize(100);

        Canvas canvas = new Canvas(bm);
        canvas.drawText(text, 3*bm.getWidth()/8, 7*bm.getHeight()/10, paint);

        return new BitmapDrawable(bm);
    }
}