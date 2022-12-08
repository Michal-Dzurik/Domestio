package sk.dzurikm.domestio.views.dialogs;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;

import sk.dzurikm.domestio.R;
import sk.dzurikm.domestio.activities.AboutMeActivity;
import sk.dzurikm.domestio.activities.HomeActivity;
import sk.dzurikm.domestio.activities.SettingsActivity;
import sk.dzurikm.domestio.activities.YourTasksActivity;
import sk.dzurikm.domestio.models.Room;
import sk.dzurikm.domestio.models.Task;
import sk.dzurikm.domestio.models.User;

public class MenuDialog extends BottomSheetDialogFragment {

    // Views
    private View rootView;
    private View addRoomButton,addTaskButton,aboutButton,settingsButton,tasksYouCreatedButton;

    // Needed variables
    private Context context;
    private FragmentManager fragmentManager;

    ArrayList<Room> roomData;
    ArrayList<User> usersData;

    // Dialogs
    private AddRoomDialog addRoomDialog;
    private AddTaskDialog addTaskDialog;

    // Listeners
    private AddRoomDialog.OnRoomCreatedListener onRoomCreatedListener;
    private AddTaskDialog.OnTaskChangeListener onTaskAddedListener;

    public MenuDialog(Context context, FragmentManager fragmentManager, ArrayList<Room> roomData,
                      ArrayList<User> usersData, AddRoomDialog.OnRoomCreatedListener onRoomCreatedListener, AddTaskDialog.OnTaskChangeListener onTaskAddedListener) {
        this.context = context;
        this.fragmentManager = fragmentManager;
        this.onRoomCreatedListener = onRoomCreatedListener;
        this.usersData = usersData;
        this.roomData = roomData;
        this.onTaskAddedListener = onTaskAddedListener;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @SuppressLint("RestrictedApi")
    @Override
    public void setupDialog(Dialog dialog, int style) {
        super.setupDialog(dialog,style);
        rootView = View.inflate(getContext(), R.layout.menu_dialog_layput, null);
        dialog.setContentView(rootView);

        addRoomButton = rootView.findViewById(R.id.CreateRoomButton);
        addTaskButton = rootView.findViewById(R.id.createTaskButton);
        aboutButton = rootView.findViewById(R.id.aboutButton);
        settingsButton = rootView.findViewById(R.id.settingsButton);
        tasksYouCreatedButton = rootView.findViewById(R.id.tasksYouCreatedButton);

        addRoomDialog = new AddRoomDialog(context, fragmentManager, new AddRoomDialog.OnRoomCreatedListener() {
            @Override
            public void onRoomCreate(Room room) {
                dialog.dismiss();
                onRoomCreatedListener.onRoomCreate(room);
            }
        });
        addTaskDialog = new AddTaskDialog(context, fragmentManager, usersData, roomData, new AddTaskDialog.OnTaskChangeListener() {
            @Override
            public void onTaskAdded(Task task) {
                onTaskAddedListener.onTaskAdded(task);
            }

            @Override
            public void onTaskEdited(Task task) {

            }
        });


        ((View) rootView.getParent()).setBackgroundColor(Color.TRANSPARENT);

        addRoomButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MenuDialog.this.dismiss();
                addRoomDialog.show(fragmentManager,"AddRoom");
            }
        });

        addTaskButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MenuDialog.this.dismiss();
                addTaskDialog.show(fragmentManager,"AddTask");
            }
        });

        aboutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MenuDialog.this.dismiss();
                Intent i = new Intent(getContext(), AboutMeActivity.class);
                startActivity(i);
            }
        });

        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*MenuDialog.this.dismiss();
                Intent i = new Intent(getContext(), SettingsActivity.class);
                startActivity(i);*/

                MenuDialog.this.dismiss();
                SettingsDialog settingsDialog = new SettingsDialog(context,fragmentManager);
                settingsDialog.show(fragmentManager,"SettingDialog");
            }
        });

        tasksYouCreatedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Show list of tasks created by me
                MenuDialog.this.dismiss();
                Intent i = new Intent(context,YourTasksActivity.class);
                startActivity(i);
            }
        });
    }



}
