package be.ugent.vop.ui.venue;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import be.ugent.vop.R;
import be.ugent.vop.backend.myApi.model.RankingBean;
import be.ugent.vop.ui.group.GroupActivity;


public class RankingAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context context;
    private int resourceId;
    private int imageWidth;
    private int imageHeight;
    private int backgroundColor;
    private int textColor;
    private boolean headerPlaceholder;

    private static final int TYPE_PLACEHOLDER = 1;
    private static final int TYPE_RANKING_ROW = 2;

    private List<RankingBean> rankings;

    /**
     * Provide a reference to the type of views that you are using (custom ViewHolder)
     */
    public static class RankingViewHolder extends RecyclerView.ViewHolder {

        private final ImageView nrImageView;
        private final TextView nameTextView;
        private final TextView pointsTextView;

        public RankingViewHolder(View v) {
            super(v);
            nrImageView = (ImageView) v.findViewById(R.id.imageViewRanking);
            nameTextView = (TextView) v.findViewById(R.id.textViewName);
            pointsTextView = (TextView) v.findViewById(R.id.textViewPoints);
        }
    }

    public static class PlaceholderViewHolder extends RecyclerView.ViewHolder {

        public PlaceholderViewHolder(View v) {
            super(v);
        }
    }

    public RankingAdapter(Context context, List<RankingBean> items, int backgroundColor, int textColor, int imageWidth, int imageHeight, boolean headerPlaceholder) {
        super();
        this.context = context;
        this.resourceId = R.layout.ranking_list_item;
        this.backgroundColor = backgroundColor;
        this.textColor = textColor;
        this.imageHeight = imageHeight;
        this.imageWidth = imageWidth;
        this.rankings = items;
        this.headerPlaceholder = headerPlaceholder;
    }

    public void setBackgroundColor(int bgColor){
        this.backgroundColor = bgColor;
    }

    public void setTextColor(int textColor){
        this.textColor = textColor;
    }

    public void setRankings(List<RankingBean> rankings){
        this.rankings = rankings;
        notifyItemRangeInserted(0, rankings.size());
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder vh = null;
        View v;

        switch(viewType) {
            case TYPE_PLACEHOLDER:
                v = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.view_header_placeholder, parent, false);
                vh = new PlaceholderViewHolder(v);
                break;
            case TYPE_RANKING_ROW:
                v = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.ranking_list_item, parent, false);
                vh = new RankingViewHolder(v);
                break;
        }

        return vh;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if(!headerPlaceholder || position > 0) {
            int pos = (headerPlaceholder)? (position - 1) : position;
            final RankingBean r = rankings.get(pos);

            RankingViewHolder h = (RankingViewHolder) holder;

            h.nameTextView.setText(r.getGroupBean().getName());
            h.nrImageView.setImageDrawable(getItemDrawable("" + (pos + 1)));
            h.pointsTextView.setText(r.getPoints().toString());

            h.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(context, GroupActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putLong("groupId", r.getGroupBean().getGroupId());
                    intent.putExtras(bundle);
                    context.startActivity(intent);
                }
            });
        }

    }

    @Override
    public int getItemViewType(int position) {
        return (headerPlaceholder && position == 0)? TYPE_PLACEHOLDER : TYPE_RANKING_ROW;
    }

    @Override
    public int getItemCount() {
        int rankingSize = (rankings == null)? 0 : rankings.size();
        return (headerPlaceholder? rankingSize + 1 : rankingSize);
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
        imagePaint.setAntiAlias(true);
        imagePaint.setTextAlign(Paint.Align.CENTER);
        imagePaint.setColor(textColor);

        imagePaint.setTextSize((int)(imageHeight * 0.6));

        shapeDrawable.draw(imageCanvas);

        // Draw the text on top of our image
        imageCanvas.drawText(text, imageWidth / 2, 7*(imageHeight/10) , imagePaint);

        // Combine background and text to a LayerDrawable
        LayerDrawable layerDrawable = new LayerDrawable(
                new Drawable[]{shapeDrawable, new BitmapDrawable(canvasBitmap)});
        return layerDrawable;
    }
}