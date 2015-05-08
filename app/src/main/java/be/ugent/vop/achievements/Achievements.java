package be.ugent.vop.achievements;

import be.ugent.vop.R;

/**
 * Created by jonas on 8-5-2015.
 */
public class Achievements {
    private static int nrAchievements = 6;
    private static String[] mAchievementTitles = {"God Slayer", "Lone Wolf", "Checkin spree", "Medal I", "Medal II", "Trophy of Triumph"};
    private static String[] mAchievementDescriptions = {"God Slayer", "Lone Wolf", "Checkin spree", "Medal I", "Medal II", "Trophy of Triumph"};
    private static int[] mAchievementImages = {R.drawable.ach1_drawable, R.drawable.ach2_drawable, R.drawable.ach3_drawable, R.drawable.ach4_drawable, R.drawable.ach5_drawable, R.drawable.ach6_drawable};

    public static int getNrAchievements() {
        return nrAchievements;
    }

    public static int[] getAchievementImages() {
        return mAchievementImages;
    }

    public static String[] getAchievementTitles() {
        return mAchievementTitles;
    }

    public static String[] getmAchievementDescriptions() {
        return mAchievementDescriptions;
    }
}
