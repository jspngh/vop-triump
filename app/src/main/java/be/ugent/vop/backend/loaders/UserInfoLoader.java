package be.ugent.vop.backend.loaders;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.util.Log;

import java.io.IOException;

import be.ugent.vop.backend.BackendAPI;
import be.ugent.vop.backend.myApi.model.UserBean;
import be.ugent.vop.foursquare.FoursquareAPI;

/**
 * Created by jonas on 24-3-2015.
 */
public class UserInfoLoader extends AsyncTaskLoader<UserBean> {
    private final String TAG = "GroupsForUserLoader";


    UserBean mUserBean;
    private Context context;
    private String userId;

    public UserInfoLoader(Context context, String userId) {
        super(context);
        this.context = context.getApplicationContext();
        this.userId = userId;
    }

    @Override public UserBean loadInBackground() {
        UserBean result = null;
        if(userId != null && !userId.equals("N.A.")) {
            try {
                result = BackendAPI.get(context).getUserInfoForId(userId);
            } catch (IOException e) {
                Log.d(TAG, e.getMessage());
            }
            if (result != null && result.getProfilePictureUrl() == null) {
                result.setProfilePictureUrl(FoursquareAPI.get(context).getUserProfilePicture(userId));
            }
        }
        return result;
    }

    @Override public void deliverResult(UserBean result) {
        if (isReset()) {
            if (result != null) {
                onReleaseResources(result);
            }
        }
        UserBean oldAllGroupsBean = result;
        mUserBean = result;

        if (isStarted()) {
            super.deliverResult(result);
        }

        if (oldAllGroupsBean != null) {
            onReleaseResources(oldAllGroupsBean);
        }
    }

    @Override protected void onStartLoading() {
        if (mUserBean != null) {
            deliverResult(mUserBean);
        }

        if (takeContentChanged() || mUserBean == null) {
            forceLoad();
        }
    }

    @Override protected void onStopLoading() {
        // Attempt to cancel the current load task if possible.
        cancelLoad();
    }

    @Override public void onCanceled(UserBean userBeam) {
        super.onCanceled(userBeam);
        onReleaseResources(userBeam);
    }

    @Override protected void onReset() {
        super.onReset();

        onStopLoading();
        if (mUserBean != null) {
            onReleaseResources(mUserBean);
            mUserBean = null;
        }
    }

    protected void onReleaseResources(UserBean userBean) {
    }
}
