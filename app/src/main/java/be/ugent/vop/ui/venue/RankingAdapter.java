package be.ugent.vop.ui.venue;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import be.ugent.vop.R;
import be.ugent.vop.backend.myApi.model.RankingBean;

/**
 * Created by vincent on 03/03/15.
 */

public class RankingAdapter extends ArrayAdapter<RankingBean> {
    private int resourceId;
    private int imageWidth;
    private int imageHeight;
    private int backgroundColor;

    public RankingAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
    }

    public RankingAdapter(Context context, List<RankingBean> items, int backgroundColor, int imageWidth, int imageHeight) {
        super(context, R.layout.ranking_list_item, items);
        this.resourceId = R.layout.ranking_list_item;
        this.backgroundColor = backgroundColor;
        this.imageHeight = imageHeight;
        this.imageWidth = imageWidth;

        Log.d("RANKINGADAPTER", ""+imageHeight);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View v = convertView;

        if (v == null) {
             v = LayoutInflater.from(getContext()).inflate(R.layout.ranking_list_item, parent, false);
        }

        RankingBean r = getItem(position);


        if (r != null) {
            ImageView nrImageView = (ImageView) v.findViewById(R.id.imageViewRanking);
            TextView nameTextView = (TextView) v.findViewById(R.id.textViewName);
            TextView pointsTextView = (TextView) v.findViewById(R.id.textViewPoints);

            if (nameTextView != null) {
                nameTextView.setText(r.getGroupBean().getName());
            }
            if(nrImageView!=null){
                nrImageView.setImageDrawable(getItemDrawable("" + (position + 1)));
            }

            if (pointsTextView != null) {

                pointsTextView.setText("" + r.getPoints());
            }
        }
        return v;
    }

    public Drawable getItemDrawable(String text){
        OvalShape ovalShape = new OvalShape();
        ShapeDrawable shapeDrawable = new ShapeDrawable(ovalShape);
        shapeDrawable.getPaint().setColor(backgroundColor);

        Bitmap canvasBitmap = Bitmap.createBitmap(imageWidth, imageHeight,
                Bitmap.Config.ARGB_8888);
        // Create a canvas, that will draw on to canvasBitmap.
        Canvas imageCanvas = new Canvas(canvasBitmap);

        // Set up the paint for use with our Canvas
        Paint imagePaint = new Paint(Paint.LINEAR_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG);
        imagePaint.setTextAlign(Paint.Align.CENTER);
        imagePaint.setColor(Color.WHITE);

        imagePaint.setTextSize((int)(imageHeight * 0.6));

        shapeDrawable.draw(imageCanvas);

        // Draw the text on top of our image
        imageCanvas.drawText(text, imageWidth / 2, 7*(imageHeight/10) , imagePaint);

        // Combine background and text to a LayerDrawable
        LayerDrawable layerDrawable = new LayerDrawable(
                new Drawable[]{shapeDrawable, new BitmapDrawable(canvasBitmap)});
        return layerDrawable;
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