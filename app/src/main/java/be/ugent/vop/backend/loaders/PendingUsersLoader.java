package be.ugent.vop.backend.loaders;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import be.ugent.vop.backend.BackendAPI;
import be.ugent.vop.backend.myApi.model.UserBean;

/**
 * Created by jonas on 30-3-2015.
 */
public class PendingUsersLoader extends AsyncTaskLoader<ArrayList<UserBean>> {
    private final String TAG = "PendingUsersLoader";

    private ArrayList<UserBean> mPendingUsers;
    private Context context;
    private long groupId;

    public PendingUsersLoader(Context context, long groupId) {
        super(context);
        this.context = context;
        this.groupId = groupId;
    }

    @Override public ArrayList<UserBean> loadInBackground() {
        ArrayList<UserBean> pendingUsers = new ArrayList<>();
        Log.d(TAG, "Loading Pending users");
        if(groupId != 0) {
            try {
                List<UserBean> result = BackendAPI.get(context).getPendingRequests(groupId);
                if(result != null) {
                    for (UserBean user : result) {
                        pendingUsers.add(user);
                        Log.d(TAG, "Adding pending user");
                    }
                }
            } catch (IOException e) {
                Log.d(TAG, e.getMessage());
            }
        }
        return pendingUsers;
    }

    @Override public void deliverResult(ArrayList<UserBean> result) {
        if (isReset()) {
            if (result != null) {
                onReleaseResources(result);
            }
        }
        ArrayList<UserBean> oldAllGroupsBean = result;
        mPendingUsers = result;
        Log.d(TAG, "Delivering result");

        if (isStarted()) {
            super.deliverResult(result);
        }

        if (oldAllGroupsBean != null) {
            onReleaseResources(oldAllGroupsBean);
        }
    }

    @Override protected void onStartLoading() {
        if (mPendingUsers != null) {
            deliverResult(mPendingUsers);
        }

        if (takeContentChanged() || mPendingUsers == null) {
            forceLoad();
        }
    }

    @Override protected void onStopLoading() {
        // Attempt to cancel the current load task if possible.
        cancelLoad();
    }

    @Override public void onCanceled( ArrayList<UserBean> users) {
        super.onCanceled(users);
        onReleaseResources(users);
    }

    @Override protected void onReset() {
        super.onReset();

        onStopLoading();
        if (mPendingUsers != null) {
            onReleaseResources(mPendingUsers);
            mPendingUsers = null;
        }
    }

    protected void onReleaseResources(ArrayList<UserBean> result) {
    }
}
