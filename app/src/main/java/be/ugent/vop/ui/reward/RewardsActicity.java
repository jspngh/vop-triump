package be.ugent.vop.ui.reward;


import android.app.Activity;
import android.os.Bundle;

import be.ugent.vop.BaseActivity;
import be.ugent.vop.R;
import be.ugent.vop.feedback.Feedback;

/**
 * Created by vincent on 10/04/15.
 */
public class RewardsActicity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rewards);

        RewardsFragment fragment = new RewardsFragment();


        this.getFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

    @Override
    protected int getSelfNavDrawerItem() {
        return NAVDRAWER_ITEM_REWARD;
    }


}
