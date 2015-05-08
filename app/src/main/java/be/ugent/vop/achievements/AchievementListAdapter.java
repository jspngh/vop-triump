package be.ugent.vop.achievements;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;

import be.ugent.vop.R;

/**
 * Created by jonas on 8-5-2015.
 */
public class AchievementListAdapter extends RecyclerView.Adapter<AchievementListAdapter.ViewHolder> {
    private Context context;
    private ArrayList<Boolean> mActivated;

    public AchievementListAdapter(Context context, ArrayList<Boolean> activated){
        super();
        if(activated != null && activated.size() == Achievements.getNrAchievements()){
            this.mActivated = activated;
        } else{
            this.mActivated = null;
        }
        this.context = context;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public ImageButton image;
        public TextView text;
        public ViewHolder(View v) {
            super(v);
            text = (TextView) v.findViewById(R.id.achievementText);
            image = (ImageButton) v.findViewById(R.id.achievementImage);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public AchievementListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.achievement, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final int pos = position;
        holder.image.setImageResource(Achievements.getAchievementImages()[position]);
        holder.text.setText(Achievements.getAchievementTitles()[position]);

        if(mActivated != null){
            holder.image.setActivated(mActivated.get(position));
        } else{
            holder.image.setActivated(false);
        }

        holder.image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle args = new Bundle();
                args.putInt(AchievementDialog.ACHIEVEMENT_NR, pos);
                AchievementDialog achievementDialog = new AchievementDialog();
                achievementDialog.setArguments(args);
                achievementDialog.show(((Activity)context).getFragmentManager(), null);
            }
        });
    }

    @Override
    public int getItemCount() {
        return Achievements.getNrAchievements();
    }
}