package sk.dzurikm.domestio.adapters;


import static android.content.Context.MODE_PRIVATE;
import static sk.dzurikm.domestio.helpers.Constants.Settings.COLLAPSED_STATE;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.transition.ChangeBounds;
import android.transition.TransitionManager;
import android.transition.TransitionSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.motion.widget.MotionLayout;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
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
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Task currentTask = data.get(position);

        String collapseState = sharedPreferences.getString(COLLAPSED_STATE,String.valueOf(Constants.Settings.CollapsingState.COLLAPSED));

        collapsed = !collapseState.equals(String.valueOf(Constants.Settings.CollapsingState.EXPANDED));

        if (!empty){
            String heading,description,owner,time,room,color;

            heading = Helpers.limitLetters(Helpers.stringValueOrDefault(currentTask.getHeading(),"Heading not provided"), Constants.TextPrint.Task.HEADING_NAME_MAX_CHAR);
            description = Helpers.stringValueOrDefault(currentTask.getDescription(),"");
            owner = Helpers.limitLetters(Helpers.stringValueOrDefault(currentTask.getAuthor(),"No owner"),Constants.TextPrint.Task.USER_NAME_MAX_CHAR);
            time = Helpers.stringValueOrDefault(currentTask.getTime(),"-");
            room = Helpers.limitLetters(Helpers.stringValueOrDefault(currentTask.getRoomName(),"No room"),Constants.TextPrint.Task.ROOM_NAME_MAX_CHAR);
            color = Helpers.stringValueOrDefault(currentTask.getColor(),"#bada55");

            Date date = new Date();
            if (currentTask.getTimestamp() < ((long) (new Timestamp(date.getTime()).getTime() / 1000))){
                holder.getMotionLayout().setAlpha(0.5F);
            }

            Log.i("HomeActivityTaskAdapter","Current task - " + currentTask);

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

            if (auth.getUid().equals(currentTask.getAuthorId())){
                // You are the author of the task
                if (currentTask.getDone()){
                    doneButton.setText(context.getString(R.string.verify));
                    doneButton.setTextColor(context.getResources().getColor(R.color.white));
                }
                else {
                    doneButton.setText(context.getString(R.string.undone));
                    doneButton.setPaintFlags(doneButton.getPaintFlags());
                    doneButton.setTextColor(context.getResources().getColor(R.color.white_transparent));
                }

                if (currentTask.getVerified()){
                    decorateLineThrough(doneButton);
                }
                else {
                    removeDecoration(doneButton);
                }
            }
            else if (auth.getUid().equals(currentTask.getReceiverId())){
                // You are the receiver of the task
                if (currentTask.getDone()){
                    decorateLineThrough(doneButton);
                }
                else {
                    removeDecoration(doneButton);
                }
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
                    Log.i("HomeActivityTaskAdapter","Long pressed task - " + currentTask);
                    TasksOptionDialog tasksOptionDialog = new TasksOptionDialog(context,fragmentManager,currentTask);
                    tasksOptionDialog.show(fragmentManager,"TaskOptionDialog");
                }

                return true;
            }
        });

        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (auth.getUid().equals(currentTask.getAuthorId())){
                    // You are the author of the task

                    if (currentTask.getDone()){
                        Alert alert = new Alert(context);
                        alert.setTitle(context.getString(R.string.set_task_as_verified));
                        alert.setDescription(context.getString(R.string.sure_want_to_set_verified));
                        alert.setNegativeButtonOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                alert.dismiss();
                            }
                        });
                        alert.setPositiveButtonOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                currentTask.setVerified(true);
                                data.remove(currentTask);
                                databaseHelper.updateTaskVerified(currentTask);
                                // TODO pridat kredity pre užívateľa

                                refresh();

                                final Handler handler = new Handler();
                                handler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {

                                        databaseHelper.removeUnrelatedTask(currentTask);
                                    }
                                }, 500);



                                alert.dismiss();
                            }
                        });
                        alert.setPositiveButtonText(context.getString(R.string.yes));
                        alert.setNegativeButtonText(context.getString(R.string.no));

                        alert.show();

                    }
                    else Toast.makeText(context, context.getString(R.string.task_undone_by_receiver),Toast.LENGTH_SHORT).show();


                    if (doneClickListener != null) {
                        doneClickListener.onDoneClick(currentTask);
                    }
                }
                else if (auth.getUid().equals(currentTask.getReceiverId())){
                    // You are the receiver of the task
                    currentTask.setDone(!currentTask.getDone());
                    notifyItemChanged(holder.getAdapterPosition());

                    //System.out.println(currentTask.getDone() + "  -  " + !currentTask.getDone());

                    if (doneClickListener != null) {
                        databaseHelper.updateTaskDone(currentTask);
                        doneClickListener.onDoneClick(currentTask);
                    }
                }
            }
        });

    }

    private void decorateLineThrough(TextView text){
        text.setPaintFlags(text.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        text.setTextColor(context.getResources().getColor(R.color.white_transparent));
    }

    private void removeDecoration(TextView text){
        text.setPaintFlags(0);
        text.setTextColor(Color.WHITE);
    }

    public void refresh(){

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