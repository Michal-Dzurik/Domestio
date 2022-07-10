package sk.dzurikm.domestio.adapters;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import sk.dzurikm.domestio.R;
import sk.dzurikm.domestio.helpers.Helpers;
import sk.dzurikm.domestio.models.Room;

public class HomeActivityRoomAdapter extends RecyclerView.Adapter<HomeActivityRoomAdapter.ViewHolder> {

    private List<Room> data;
    private LayoutInflater layoutInflater;
    private ItemClickListener mClickListener;

    // data is passed into the constructor
    public HomeActivityRoomAdapter(Context context, List<Room> data) {
        this.layoutInflater = LayoutInflater.from(context);
        this.data = data;
    }

    // inflates the row layout from xml when needed
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = layoutInflater.inflate(R.layout.room_slider_layout, parent, false);
        return new ViewHolder(view);
    }

    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Room currentRoom = data.get(position);

        String title,description;
        int peopleCount,tasksCount;

        title = Helpers.stringValueOrDefault(currentRoom.getTitle(),"Title not provided");
        description = Helpers.stringValueOrDefault(currentRoom.getDescription(),"Description not provided");

        peopleCount = Helpers.positiveValueOrDefault(currentRoom.getPeopleCount(),0);
        tasksCount = Helpers.positiveValueOrDefault(currentRoom.getTasksCount(),0);

        holder.getTitle()
                .setText(title);
        holder.getDescription()
                .setText(description);

        holder.getPeopleCount()
                .setText(String.valueOf(peopleCount));
        holder.getTasksCount()
                .setText(String.valueOf(tasksCount));
    }



    // total number of rows
    @Override
    public int getItemCount() {
        return data.size();
    }


    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder{
       private LinearLayout cardBackground,leaveButton;
       private TextView title,description,peopleCount,tasksCount;


        ViewHolder(View view) {
            super(view);
            cardBackground = view.findViewById(R.id.cardBackground);
            leaveButton = view.findViewById(R.id.leaveButton);

            title = view.findViewById(R.id.title);
            description = view.findViewById(R.id.description);
            peopleCount = view.findViewById(R.id.peopleNumber);
            tasksCount = view.findViewById(R.id.tasksNumber);

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
}