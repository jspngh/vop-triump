package be.ugent.vop.ui.group;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.koushikdutta.ion.Ion;

import java.util.ArrayList;

import be.ugent.vop.R;
import be.ugent.vop.backend.myApi.model.UserBean;
import be.ugent.vop.ui.profile.ProfileActivity;
import be.ugent.vop.ui.profile.ProfileFragment;

/**
 * Created by jonas on 28-3-2015.
 */
public class MemberListAdapter extends RecyclerView.Adapter<MemberListAdapter.ViewHolder> {
    private ArrayList<String> mDataset;
    private Context context;
    private ArrayList<UserBean> members;

    public MemberListAdapter(Context context, ArrayList<UserBean> members){
        super();
        this.members = members;
        this.context = context;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView profilePic;
        public TextView member_name;
        public ViewHolder(View v) {
            super(v);
            profilePic = (ImageView) v.findViewById(R.id.profilePic);
            member_name = (TextView) v.findViewById(R.id.member_name);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public MemberListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.member_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final int pos = position;
        if(members != null){
            holder.member_name.setText(members.get(position).getFirstName());
            if(members.get(position).getProfilePictureUrl() != null) {
                Ion.with(holder.profilePic)
                        .placeholder(R.drawable.profile_default)
                        .error(R.drawable.ic_drawer_user)
                        .load(members.get(position).getProfilePictureUrl());
            }
            holder.profilePic.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, ProfileActivity.class);
                    intent.putExtra(ProfileFragment.USER_ID, members.get(pos).getUserId());
                    context.startActivity(intent);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        if(members != null)return members.size();
        return 0;
    }
}