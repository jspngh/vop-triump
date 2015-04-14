/*
 *    Copyright (C) 2015 Haruki Hasegawa
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package be.ugent.vop.ui.reward;

import android.app.LoaderManager;
import android.content.Context;
import android.content.Loader;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.text.NoCopySpan;
import android.util.Log;

import com.h6ah4i.android.widget.advrecyclerview.swipeable.RecyclerViewSwipeManager;

import java.io.IOException;

import be.ugent.vop.BaseActivity;
import be.ugent.vop.R;
import be.ugent.vop.backend.BackendAPI;
import be.ugent.vop.backend.loaders.EventLoader;
import be.ugent.vop.backend.myApi.model.EventBean;
import be.ugent.vop.backend.myApi.model.EventRewardBean;
import be.ugent.vop.backend.myApi.model.GroupBean;
import be.ugent.vop.utils.PrefUtils;

public class RewardsActivity extends BaseActivity implements ItemPinnedMessageDialogFragment.EventListener {
    private static final String FRAGMENT_LIST_VIEW = "list view";
    private static final String FRAGMENT_TAG_ITEM_PINNED_DIALOG = "item pinned dialog";
    private Context context;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        context = this.getApplicationContext();
        boolean darkTheme = PrefUtils.getDarkTheme(this);
        if(darkTheme) setTheme(R.style.AppTheme_Dark);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rewards);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, new RewardListViewFragment(), FRAGMENT_LIST_VIEW)
                    .commit();
        }
    }
    @Override
    protected int getSelfNavDrawerItem() {
        return NAVDRAWER_ITEM_REWARD;
    }

    @Override
    protected void onPause() {
        super.onPause();
    }
    /**
     * This method will be called when a list item is pinned
     *
     * @param position The position of the item within data set
     */
    public void onItemPinned(int position) {
        final DialogFragment dialog = ItemPinnedMessageDialogFragment.newInstance(position);

        getSupportFragmentManager()
                .beginTransaction()
                .add(dialog, FRAGMENT_TAG_ITEM_PINNED_DIALOG)
                .commit();
    }

    public void onItemRemoved(int position) {
    }


    /**
     * This method will be called when a list item is clicked
     *
     * @param position The position of the item within data set
     */
    public void onItemClicked(int position) {
        final Fragment fragment = getSupportFragmentManager().findFragmentByTag(FRAGMENT_LIST_VIEW);
        AbstractDataProvider.Data data = getDataProvider().getItem(position);

        if (data.isPinnedToSwipeLeft()) {
            // unpin if tapped the pinned item
            data.setPinnedToSwipeLeft(false);
            ((RewardListViewFragment) fragment).notifyItemChanged(position);
        }
    }



    // implements ItemPinnedMessageDialogFragment.EventListener
    @Override
    public void onNotifyItemPinnedDialogDismissed(int itemPosition, boolean ok) {
        final Fragment fragment = getSupportFragmentManager().findFragmentByTag(FRAGMENT_LIST_VIEW);
        final EventBean bean = ((RewardListViewFragment.RewardDataProvider.ConcreteData)getDataProvider().getItem(itemPosition)).getBean();
        new Thread( new Runnable() {
            @Override
            public void run() {
                try {
                    for(GroupBean g: bean.getGroups()) {
                        if(g!=null) {
                            BackendAPI.get(context).claimReward(bean.getEventId(), g.getGroupId());
                        }
                    }
                }catch (IOException e){
                    e.printStackTrace();
                }
            }
        }).start();
        getDataProvider().removeItem(itemPosition);
        getDataProvider().restartLoader();
        ((RewardListViewFragment) fragment).notifyItemChanged(itemPosition);

    }

    public RewardListViewFragment.RewardDataProvider getDataProvider() {
        final Fragment fragment = getSupportFragmentManager().findFragmentByTag(FRAGMENT_LIST_VIEW);
        Log.d("RewardsActivity", "size: " + ((RewardListViewFragment) fragment).getDataProvider().getCount());
        return ((RewardListViewFragment) fragment).getDataProvider();
    }

}


