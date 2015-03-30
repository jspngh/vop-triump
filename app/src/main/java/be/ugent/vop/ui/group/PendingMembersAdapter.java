package be.ugent.vop.ui.group;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.koushikdutta.ion.Ion;

import java.io.IOException;
import java.util.ArrayList;

import be.ugent.vop.R;
import be.ugent.vop.backend.BackendAPI;
import be.ugent.vop.backend.myApi.model.UserBean;
import be.ugent.vop.ui.profile.ProfileActivity;
import be.ugent.vop.ui.profile.ProfileFragment;

/**
 * Created by jonas on 30-3-2015.
 */
public class PendingMembersAdapter extends RecyclerView.Adapter<PendingMembersAdapter.ViewHolder> {

    private Context context;
    private ArrayList<UserBean> pendingUsers;
    private long groupId;

    public PendingMembersAdapter(Context context, ArrayList<UserBean> pendingUsers, long groupId){
        super();
        this.pendingUsers = pendingUsers;
        this.context = context;
        this.groupId = groupId;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView profilePic;
        public TextView user_name;
        public Button acceptBtn;
        public Button declineBtn;
        public ViewHolder(View v) {
            super(v);
            profilePic = (ImageView) v.findViewById(R.id.profilePic);
            user_name = (TextView) v.findViewById(R.id.user_name);
            acceptBtn = (Button) v.findViewById(R.id.btnAccept);
            declineBtn = (Button) v.findViewById(R.id.btnDecline);
        }
    }

    public void removeAt(int position) {
        pendingUsers.remove(position);
        notifyItemRemoved(position);
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public PendingMembersAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.pending_user_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final int pos = position;
        final String userId = pendingUsers.get(position).getUserId();
        if(pendingUsers != null){
            holder.user_name.setText(pendingUsers.get(position).getFirstName() + " " + pendingUsers.get(position).getLastName());
            if(pendingUsers.get(position).getProfilePictureUrl() != null) {
                Ion.with(holder.profilePic)
                        .placeholder(R.drawable.profile_default)
                        .error(R.drawable.ic_drawer_user)
                        .load(pendingUsers.get(position).getProfilePictureUrl());
            }
            holder.user_name.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, ProfileActivity.class);
                    intent.putExtra(ProfileFragment.USER_ID, pendingUsers.get(pos).getUserId());
                    context.startActivity(intent);
                }
            });
            holder.profilePic.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, ProfileActivity.class);
                    intent.putExtra(ProfileFragment.USER_ID, pendingUsers.get(pos).getUserId());
                    context.startActivity(intent);
                }
            });
            holder.acceptBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new Thread( new Runnable() {
                        @Override
                        public void run() {
                            try {
                                BackendAPI.get(context).acceptUserInGroup(userId, groupId);
                            }catch (IOException e){
                                e.printStackTrace();
                            }
                        }

                    }).start();
                    removeAt(pos);
                }
            });
            holder.declineBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new Thread( new Runnable() {
                        @Override
                        public void run() {
                            try {
                                BackendAPI.get(context).denyUserInGroup(userId, groupId);
                            }catch (IOException e){
                                e.printStackTrace();
                            }
                        }
                    }).start();
                    removeAt(pos);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        if(pendingUsers != null)return pendingUsers.size();
        return 0;
    }
}