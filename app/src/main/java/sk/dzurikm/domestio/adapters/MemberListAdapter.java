package sk.dzurikm.domestio.adapters;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import sk.dzurikm.domestio.R;
import sk.dzurikm.domestio.helpers.DataStorage;
import sk.dzurikm.domestio.helpers.Helpers;
import sk.dzurikm.domestio.models.User;
import sk.dzurikm.domestio.views.alerts.Alert;

public class MemberListAdapter extends RecyclerView.Adapter<MemberListAdapter.ViewHolder> {

    private ArrayList<User> data;
    private LayoutInflater layoutInflater;
    private Context context;
    private String adminID;

    // Listeners
    OnMemberRemoveListener onMemberRemoveListener;
    private boolean admin;


    // data is passed into the constructor
    public MemberListAdapter(Context context, ArrayList<User> data,String adminID,boolean admin, OnMemberRemoveListener onMemberRemoveListener) {
        this.layoutInflater = LayoutInflater.from(context);
        this.data = data;
        this.context = context;
        this.adminID = adminID;
        this.onMemberRemoveListener = onMemberRemoveListener;
        this.admin = admin;
    }

    // inflates the row layout from xml when needed
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        view = layoutInflater.inflate(R.layout.member_list_item, parent, false);

        return new ViewHolder(view);
    }

    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        User member = data.get(position);

        holder.getMemberName().setText(member.getName());
        holder.getMemberEmail().setText(member.getEmail());

        if (admin){
            if (!member.getId().equals(adminID)){
                System.out.println(member.getId() + " --  " + adminID);
                holder.getDeleteMemberButton().setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (DataStorage.connected){
                            Alert alert = new Alert(context);
                            alert.setTitle(context.getString(R.string.remove) + " " +  member.getName());
                            alert.setDescription(context.getString(R.string.do_you_want_to_remove_member));
                            alert.setPositiveButtonText(context.getString(R.string.yes));
                            alert.setNegativeButtonText(context.getString(R.string.no));
                            alert.setNegativeButtonOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    alert.dismiss();
                                }
                            });

                            alert.setPositiveButtonOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    onMemberRemoveListener.onMemberRemove(member.getId());
                                    alert.dismiss();
                                }
                            });

                            alert.show();
                        }

                        else Helpers.Toast.noInternet(context);
                    }
                });
            }
            else {
                holder.getAdminBadge().setVisibility(View.VISIBLE);
                holder.getDeleteMemberButton().setVisibility(View.GONE);
            }
        }
        else {
            if (member.getId().equals(adminID)) holder.getAdminBadge().setVisibility(View.VISIBLE);
            holder.getDeleteMemberButton().setVisibility(View.GONE);
        }



    }


    // total number of rows
    @Override
    public int getItemCount() {
        return data.size();
    }


    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder{
        private TextView memberName,memberEmail,adminBadge;
        private ImageButton deleteMemberButton;

        ViewHolder(View view) {
            super(view);

            memberName = view.findViewById(R.id.memberName);
            memberEmail = view.findViewById(R.id.memberEmail);
            deleteMemberButton = view.findViewById(R.id.removeMemberButton);
            adminBadge = view.findViewById(R.id.adminBadge);

        }

        public TextView getMemberName() {
            return memberName;
        }

        public TextView getMemberEmail() {
            return memberEmail;
        }

        public ImageButton getDeleteMemberButton() {
            return deleteMemberButton;
        }

        public TextView getAdminBadge() {
            return adminBadge;
        }
    }

    // convenience method for getting data at click position
    User getItem(int id) {
        return data.get(id);
    }

    public interface OnMemberRemoveListener{
        public default void onMemberRemove(String uid){

        }
    }

}