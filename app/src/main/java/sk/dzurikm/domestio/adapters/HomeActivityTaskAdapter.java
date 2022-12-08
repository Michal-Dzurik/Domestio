package sk.dzurikm.domestio.adapters;


import static android.content.Context.MODE_PRIVATE;
import static sk.dzurikm.domestio.helpers.Constants.Settings.COLLAPSED_STATE;

import android.content.Context;
import android.content.SharedPreferences;
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
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

import sk.dzurikm.domestio.R;
import sk.dzurikm.domestio.helpers.Constants;
import sk.dzurikm.domestio.helpers.DatabaseHelper;
import sk.dzurikm.domestio.helpers.Helpers;
import sk.dzurikm.domestio.models.Task;
import sk.dzurikm.domestio.views.alerts.Alert;
import sk.dzurikm.domestio.views.dialogs.TasksOptionDialog;

public class HomeActivityTaskAdapter extends RecyclerView.Adapter<HomeActivityTaskAdapter.ViewHolder> {

    private List<Task> data;
    private LayoutInflater layoutInflater;
    private boolean empty;
    private Context context;
    private FirebaseAuth auth;
    private FragmentManager fragmentManager;
    private OnDoneClickListener doneClickListener;

    boolean collapsed;

    private DatabaseHelper databaseHelper;

    // Shared Preferences
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor sharedPreferencesEditor;

    // data is passed into the constructor
    public HomeActivityTaskAdapter(Context context, List<Task> data,OnDoneClickListener doneClickListener) {
        empty = false;
        this.layoutInflater = LayoutInflater.from(context);
        this.data = data;
        this.context = context;
        this.auth = FirebaseAuth.getInstance();
        this.doneClickListener = doneClickListener;

        init();
    }

    public HomeActivityTaskAdapter(Context context, List<Task> data,OnDoneClickListener doneClickListener, FragmentManager manager) {
        empty = false;
        this.layoutInflater = LayoutInflater.from(context);
        this.data = data;
        this.context = context;
        this.auth = FirebaseAuth.getInstance();
        this.doneClickListener = doneClickListener;
        fragmentManager= manager;

        init();
    }

    private void init(){
        databaseHelper = new DatabaseHelper();

        // SharedPreferences
        sharedPreferences = context.getSharedPreferences(Constants.SHARED_PREFERENCES_KEY,MODE_PRIVATE);
        sharedPreferencesEditor = sharedPreferences.edit();

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

    TextView doneButton;

    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Task currentTask = data.get(position);

        String collapseState = sharedPreferences.getString(COLLAPSED_STATE,String.valueOf(Constants.Settings.CollapsingState.COLLAPSED));

        collapsed = !collapseState.equals(String.valueOf(Constants.Settings.CollapsingState.EXPANDED));
        System.out.println(collapseState + "   -  " + String.valueOf(Constants.Settings.CollapsingState.EXPANDED));

        if (!empty){
            String heading,description,owner,time,room,color;

            heading = Helpers.limitLetters(Helpers.stringValueOrDefault(currentTask.getHeading(),"Heading not provided"), Constants.TextPrint.Task.HEADING_NAME_MAX_CHAR);
            description = Helpers.stringValueOrDefault(currentTask.getDescription(),"");
            owner = Helpers.limitLetters(Helpers.stringValueOrDefault(currentTask.getAuthor(),"No owner"),Constants.TextPrint.Task.USER_NAME_MAX_CHAR);
            time = Helpers.stringValueOrDefault(currentTask.getTime(),"-");
            room = Helpers.limitLetters(Helpers.stringValueOrDefault(currentTask.getRoomName(),"No room"),Constants.TextPrint.Task.ROOM_NAME_MAX_CHAR);
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

            doneButton = holder.getDoneButton();


            MotionLayout motionLayout = holder.getMotionLayout();

            holder.getCardBackground().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    MotionLayout motionLayout = holder.getMotionLayout();

                    if (!collapsed){
                        motionLayout.transitionToStart();

                        collapsed = true;
                    }
                    else {
                        motionLayout.transitionToEnd();

                        collapsed = false;
                    }


                }
            });

            holder.getCardBackground().setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (currentTask.getAuthorId().equals(auth.getCurrentUser().getUid())){
                        System.out.println(currentTask);
                        TasksOptionDialog tasksOptionDialog = new TasksOptionDialog(context,fragmentManager,currentTask);
                        tasksOptionDialog.show(fragmentManager,"TaskOptionDialog");
                    }

                    return true;
                }
            });

            doneButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (auth.getUid().equals(currentTask.getReceiverId())){
                        currentTask.setDone(!currentTask.getDone());
                        notifyDataSetChanged();

                        //System.out.println(currentTask.getDone() + "  -  " + !currentTask.getDone());

                        if (doneClickListener != null) {
                            databaseHelper.updateTaskDone(currentTask);
                            doneClickListener.onDoneClick(currentTask);
                        }
                    }
                }
            });

            if (currentTask.getDone()){
                doneButton.setPaintFlags(doneButton.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                doneButton.setTextColor(context.getResources().getColor(R.color.white_transparent));
            }
            else {
                doneButton.setPaintFlags(0);
                doneButton.setTextColor(Color.WHITE);
            }

            if (!collapsed){
                motionLayout.transitionToStart();

                collapsed = true;
            }
            else {
                motionLayout.transitionToEnd();

                collapsed = false;
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

    public interface OnDoneClickListener{
        public void onDoneClick(Task task);
    }

}