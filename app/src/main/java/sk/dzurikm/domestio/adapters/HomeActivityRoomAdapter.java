package sk.dzurikm.domestio.adapters;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

import sk.dzurikm.domestio.R;
import sk.dzurikm.domestio.activities.RoomActivity;
import sk.dzurikm.domestio.helpers.Constants;
import sk.dzurikm.domestio.helpers.DatabaseHelper;
import sk.dzurikm.domestio.helpers.Helpers;
import sk.dzurikm.domestio.models.Room;
import sk.dzurikm.domestio.views.alerts.Alert;

public class HomeActivityRoomAdapter extends RecyclerView.Adapter<HomeActivityRoomAdapter.ViewHolder> {

    private List<Room> data;
    private LayoutInflater layoutInflater;
    private ItemClickListener mClickListener;
    private Context context;

    // Firebase
    FirebaseAuth auth;

    // Listeners
    OnRoomLeaveListener onRoomLeaveListener;

    // data is passed into the constructor
    public HomeActivityRoomAdapter(Context context, List<Room> data,OnRoomLeaveListener onRoomLeaveListener) {
        this.context = context;
        this.layoutInflater = LayoutInflater.from(context);
        this.data = data;
        this.onRoomLeaveListener = onRoomLeaveListener;

        auth = FirebaseAuth.getInstance();
    }

    // inflates the row layout from xml when needed
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = layoutInflater.inflate(R.layout.room_slider_layout, parent, false);
        return new ViewHolder(view);
    }

    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Room currentRoom = data.get(position);

        String title,description,color;
        int peopleCount,tasksCount;

        title = Helpers.stringValueOrDefault(currentRoom.getTitle(),"Title not provided");
        description = Helpers.stringValueOrDefault(currentRoom.getDescription(),"Description not provided");

        peopleCount = Helpers.positiveValueOrDefault(currentRoom.getPeopleCount(),0);
        tasksCount = Helpers.positiveValueOrDefault(currentRoom.getTasksCount(),0);
        color = Helpers.stringValueOrDefault(currentRoom.getColor(),"#bada55");

        holder.getCardBackground()
                .setBackgroundColor(Color.parseColor(Helpers.Colors.addOpacity(color,"AD")));

        holder.getTitle()
                .setText(title);
        holder.getDescription()
                .setText(description);

        holder.getPeopleCount()
                .setText(String.valueOf(peopleCount));
        holder.getTasksCount()
                .setText(String.valueOf(tasksCount));

        if (!currentRoom.isAdmin(auth.getUid())){
            holder.getLeaveButton().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // You wanna leave ? Than leave
                    Alert alert = new Alert(context);
                    alert.setTitle(context.getString(R.string.leave_room) + " " + currentRoom.getTitle());
                    alert.setDescription(context.getString(R.string.do_you_really_wnat_t_leave));
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
                            alert.dismiss();
                            DatabaseHelper helper = new DatabaseHelper();
                            helper.leaveRoom(currentRoom, auth.getUid(), new OnCompleteListener() {
                                @Override
                                public void onComplete(@NonNull Task task) {
                                    if (task.isSuccessful()){
                                        onRoomLeaveListener.onRoomLeave(currentRoom);

                                    }
                                }
                            });
                        }
                    });

                    alert.show();
                }
            });
        }
        else {
            holder.getLeaveIcon().setVisibility(View.GONE);
            holder.getAdminBadge().setVisibility(View.VISIBLE);
        }

        holder.getCardBackground().setClickable(true);
        holder.getCardBackground().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // starting activity Room with result code for changing data
                Intent i = new Intent(context, RoomActivity.class);
                i.putExtra("room",currentRoom);


                ((Activity) context).startActivityForResult(i, Constants.Result.ROOM_CHANGED);
            }
        });
    }



    // total number of rows
    @Override
    public int getItemCount() {
        return data.size();
    }


    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder{
       private LinearLayout cardBackground,leaveButton;
       private TextView title,description,peopleCount,tasksCount,adminBadge;
       private ImageView leaveIcon;


        ViewHolder(View view) {
            super(view);
            cardBackground = view.findViewById(R.id.cardBackground);
            leaveButton = view.findViewById(R.id.leaveButton);

            title = view.findViewById(R.id.title);
            description = view.findViewById(R.id.description);
            peopleCount = view.findViewById(R.id.peopleNumber);
            tasksCount = view.findViewById(R.id.tasksNumber);

            leaveIcon = view.findViewById(R.id.leaveButtonIcon);
            adminBadge = view.findViewById(R.id.adminBadge);

        }

        public LinearLayout getCardBackground() {
            return cardBackground;
        }

        public LinearLayout getLeaveButton() {
            return leaveButton;
        }

        public TextView getTitle() {
            return title;
        }

        public TextView getDescription() {
            return description;
        }

        public TextView getPeopleCount() {
            return peopleCount;
        }

        public TextView getTasksCount() {
            return tasksCount;
        }

        public ImageView getLeaveIcon() {
            return leaveIcon;
        }

        public TextView getAdminBadge() {
            return adminBadge;
        }
    }

    // convenience method for getting data at click position
    Room getItem(int id) {
        return data.get(id);
    }

    // allows clicks events to be caught
    void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }

    public interface OnRoomLeaveListener{
        public void onRoomLeave(Room room);
    }

}