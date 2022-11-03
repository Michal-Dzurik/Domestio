package sk.dzurikm.domestio.adapters;


import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.transition.ChangeBounds;
import android.transition.TransitionManager;
import android.transition.TransitionSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.constraintlayout.motion.widget.MotionLayout;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import sk.dzurikm.domestio.R;
import sk.dzurikm.domestio.helpers.Helpers;
import sk.dzurikm.domestio.models.Task;

public class HomeActivityTaskAdapter extends RecyclerView.Adapter<HomeActivityTaskAdapter.ViewHolder> {

    private List<Task> data;
    private LayoutInflater layoutInflater;
    private boolean empty;
    private Context context;

    // data is passed into the constructor
    public HomeActivityTaskAdapter(Context context, List<Task> data) {
        empty = false;
        this.layoutInflater = LayoutInflater.from(context);
        this.data = data;
        this.context = context;
    }

    // inflates the row layout from xml when needed
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        if (data.get(0).getId() == null){
            empty = true;
            view = layoutInflater.inflate(R.layout.task_slider_empty_layout, parent, false);
        }
        else {
            view = layoutInflater.inflate(R.layout.task_slider_layout, parent, false);
        }

        return new ViewHolder(view);
    }

    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Task currentTask = data.get(position);

        if (!empty){
            String heading,description,owner,time,room,color;

            heading = Helpers.stringValueOrDefault(currentTask.getHeading(),"Heading not provided");
            description = Helpers.stringValueOrDefault(currentTask.getDescription(),"");
            owner = Helpers.stringValueOrDefault(currentTask.getAuthor(),"No owner");
            time = Helpers.stringValueOrDefault(currentTask.getTime(),"-");
            room = Helpers.stringValueOrDefault(currentTask.getRoomName(),"No room");
            color = Helpers.stringValueOrDefault(currentTask.getColor(),"#bada55");

            holder.getHeading()
                    .setText(heading);

            if (description.equals("")){
                holder.getDescription().setVisibility(View.GONE);
            }
            else {
                holder.getDescription()
                        .setText(description);
            }

            holder.getCardBackground()
                    .setBackgroundColor(Color.parseColor(Helpers.Colors.addOpacity(color,"AD")));

            holder.getOwner()
                    .setText(owner);
            holder.getTime()
                    .setText(time);
            holder.getRoom()
                    .setText(room);

            TextView doneButton = holder.getDoneButton();

            final boolean[] collapsed = {false};

            holder.getCardBackground().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    MotionLayout motionLayout = holder.getMotionLayout();

                    if (!collapsed[0]){
                        motionLayout.transitionToEnd();

                        collapsed[0] = true;
                    }
                    else {
                        motionLayout.transitionToStart();

                        collapsed[0] = false;
                    }


                }
            });

            if (currentTask.getDone()){
                doneButton.setPaintFlags(doneButton.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            }
        }



    }


    // total number of rows
    @Override
    public int getItemCount() {
        return data.size();
    }


    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder{
       private LinearLayout cardBackground;
       private CardView additionalInfo;
       private TextView heading,description,time,owner,room,doneButton;
       private MotionLayout motionLayout;


        ViewHolder(View view) {
            super(view);
            cardBackground = view.findViewById(R.id.cardBackground);

            heading = view.findViewById(R.id.heading);
            description = view.findViewById(R.id.description);
            time = view.findViewById(R.id.time);
            owner = view.findViewById(R.id.owner);
            room = view.findViewById(R.id.roomName);
            doneButton = view.findViewById(R.id.doneButton);
            additionalInfo = view.findViewById(R.id.additionalInfo);
            motionLayout = view.findViewById(R.id.motionLayout);

        }

        public LinearLayout getCardBackground() {
            return cardBackground;
        }

        public TextView getHeading() {
            return heading;
        }

        public TextView getDescription() {
            return description;
        }

        public TextView getTime() {
            return time;
        }

        public TextView getOwner() {
            return owner;
        }

        public TextView getRoom() {
            return room;
        }

        public TextView getDoneButton() {
            return doneButton;
        }

        public CardView getAdditionalInfo() {
            return additionalInfo;
        }

        public MotionLayout getMotionLayout() {
            return motionLayout;
        }
    }

    // convenience method for getting data at click position
    Task getItem(int id) {
        return data.get(id);
    }

}