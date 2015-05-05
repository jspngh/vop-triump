package be.ugent.vop.ui.profile;


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
    public static final int ACH_1 = 1;  // doelman
    public static final int ACH_2 = 2;  // kasteel
    public static final int ACH_3 = 3;  // scavenger
    public static final int ACH_4 = 4;  // medaille 1
    public static final int ACH_5 = 5;  // medaille 2
    public static final int ACH_6 = 6;  // beker


    private static final String TAG = "AchievementDialog";
    private ImageView achievementIV;
    private TextView achievementTitleTV;
    private TextView  achievementDescrTV;



    private int achievementNr = -1;


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View rootView = inflater.inflate(R.layout.dialog_achievement, null);

        // zet afbeelding goed
        achievementIV = (ImageView) rootView.findViewById(R.id.imageViewAchievement);
        achievementTitleTV = (TextView) rootView.findViewById(R.id.textViewTitleAchievemnt);
        achievementDescrTV = (TextView) rootView.findViewById(R.id.textViewDescriptionAchievement);

        achievementNr = getArguments().getInt(ACHIEVEMENT_NR);
        initContent();

        builder.setView(rootView);

        return builder.create();
    }

    private void initContent() {
        switch(achievementNr){
            case ACH_1:
                achievementIV.setImageResource(R.drawable.achievement1);
                achievementTitleTV.setText(getActivity().getString(R.string.ach1_title));
                achievementDescrTV.setText(getActivity().getString(R.string.ach1_description));
                break;
            case ACH_2:
                achievementIV.setImageResource(R.drawable.achievement2);
                achievementTitleTV.setText(getActivity().getString(R.string.ach2_title));
                achievementDescrTV.setText(getActivity().getString(R.string.ach2_description));
                break;
            case ACH_3:
                achievementIV.setImageResource(R.drawable.achievement3);
                achievementTitleTV.setText(getActivity().getString(R.string.ach3_title));
                achievementDescrTV.setText(getActivity().getString(R.string.ach3_description));
                break;
            case ACH_4:
                achievementIV.setImageResource(R.drawable.achievement4);
                achievementTitleTV.setText(getActivity().getString(R.string.ach4_title));
                achievementDescrTV.setText(getActivity().getString(R.string.ach4_description));
                break;
            case ACH_5:
                achievementIV.setImageResource(R.drawable.achievement5);
                achievementTitleTV.setText(getActivity().getString(R.string.ach5_title));
                achievementDescrTV.setText(getActivity().getString(R.string.ach5_description));
                break;
            case ACH_6:
                achievementIV.setImageResource(R.drawable.achievement6);
                achievementTitleTV.setText(getActivity().getString(R.string.ach6_title));
                achievementDescrTV.setText(getActivity().getString(R.string.ach6_description));
                break;

        }
    }

}
