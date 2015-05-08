package be.ugent.vop.achievements;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.media.Image;
import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import be.ugent.vop.R;

/**
 * Created by vincent on 05/05/15.
 */
public class AchievementDialog extends DialogFragment {
    public static final String ACHIEVEMENT_NR = "NrAchievement";

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View rootView = inflater.inflate(R.layout.dialog_achievement, null);

        // zet afbeelding goed
        ImageView achievementIV = (ImageView) rootView.findViewById(R.id.imageViewAchievement);
        TextView achievementTitleTV = (TextView) rootView.findViewById(R.id.textViewTitleAchievemnt);
        TextView achievementDescrTV = (TextView) rootView.findViewById(R.id.textViewDescriptionAchievement);

        int position = getArguments().getInt(ACHIEVEMENT_NR);

        achievementIV.setImageResource(Achievements.getAchievementImages()[position]);
        achievementIV.setActivated(true);
        achievementTitleTV.setText(Achievements.getAchievementTitles()[position]);
        achievementDescrTV.setText(Achievements.getmAchievementDescriptions()[position]);

        builder.setView(rootView);

        return builder.create();
    }
}
