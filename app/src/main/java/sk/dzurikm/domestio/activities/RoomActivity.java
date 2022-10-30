package sk.dzurikm.domestio.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;

import sk.dzurikm.domestio.R;
import sk.dzurikm.domestio.adapters.HomeActivityTaskAdapter;
import sk.dzurikm.domestio.helpers.Constants;
import sk.dzurikm.domestio.helpers.DatabaseHelper;
import sk.dzurikm.domestio.helpers.Helpers;
import sk.dzurikm.domestio.models.Room;
import sk.dzurikm.domestio.models.Task;
import sk.dzurikm.domestio.models.User;
import sk.dzurikm.domestio.views.alerts.Alert;
import sk.dzurikm.domestio.views.dialogs.AddRoomMemberDialog;
import sk.dzurikm.domestio.views.dialogs.AddTaskDialog;
import sk.dzurikm.domestio.views.dialogs.RoomOptionDialog;

public class RoomActivity extends AppCompatActivity {

    // Views
    RecyclerView roomsRecycler;
    TextView roomTitle,roomDescription,roomPeopleCount,roomTaskCount;
    ImageButton backButton,addTaskButton,optionButton;
    LinearLayout cardBackground;
    Button addMemberButton;

    // Helpers
    DatabaseHelper databaseHelper;
    FirebaseAuth auth;

    // Datasets
    ArrayList<Task> taskData;
    ArrayList<User> usersData;
    Room room;

    // Adapters
    HomeActivityTaskAdapter taskAdapter;

    // Variables needed
    private boolean left = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room);

        // DB
        auth = FirebaseAuth.getInstance();

        room = (Room) getIntent().getExtras().get("room");
        Log.i("CURRENT_ROOM",room.toString());

        // Views
        roomsRecycler = findViewById(R.id.tasksRecycler);
        roomTitle = findViewById(R.id.roomTitle);
        roomDescription = findViewById(R.id.roomDescription);
        roomPeopleCount = findViewById(R.id.roomPeopleNumber);
        roomTaskCount = findViewById(R.id.roomTasksNumber);
        backButton = findViewById(R.id.backButton);
        cardBackground = findViewById(R.id.cardBackground);
        addTaskButton = findViewById(R.id.addTaskButton);
        optionButton = findViewById(R.id.optionButton);
        addMemberButton = findViewById(R.id.addMemberButton);

        // Setting up empty adapter
        ArrayList<Task> emptyData = new ArrayList<Task>();
        emptyData.add(new Task());
        roomsRecycler.setAdapter(new HomeActivityTaskAdapter(RoomActivity.this,emptyData));

        // Setting up values
        updateRoomChangeableInfo(room);
        roomTaskCount.setText(String.valueOf(room.getTasksCount()));
        roomPeopleCount.setText(String.valueOf(room.getPeopleCount()));
        cardBackground.setBackgroundColor(Color.parseColor(Helpers.Colors.addOpacity(room.getColor(),"AD")));
        if (room.isAdmin(auth.getUid())){
            addMemberButton.setVisibility(View.VISIBLE);
        }
        else {
            // option button transforms to leave button
            optionButton.setImageResource(R.drawable.ic_leave);
            optionButton.setBackgroundResource(R.drawable.leave_button_background);
        }

        // helpers
        databaseHelper = new DatabaseHelper();
        databaseHelper.setRoom(room);
        databaseHelper.setOnDataLoadedListener(new DatabaseHelper.OnDataLoadedListener() {
            @Override
            public void onDataLoaded(ArrayList<Room> roomData, ArrayList<Task> taskData, ArrayList<User> userData) {
                RoomActivity.this.taskData = taskData;
                RoomActivity.this.usersData = userData;

                taskAdapter = new HomeActivityTaskAdapter(RoomActivity.this,taskData);
                roomsRecycler.setAdapter(taskAdapter);
            }
        });
        databaseHelper.getData(Constants.Firebase.DATA_FOR_ROOM);

        // Setting up listeners
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        optionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (room.isAdmin(auth.getUid())){
                    RoomOptionDialog dialog = new RoomOptionDialog(RoomActivity.this,RoomActivity.this.getSupportFragmentManager(),filterUsersForThisRoom());
                    dialog.setRoom(room);
                    dialog.setRoomDataChangedListener(new RoomOptionDialog.RoomDataChangedListener() {
                        @Override
                        public void onRoomDataChangedListener(Room room) {
                            updateRoomChangeableInfo(room);
                            updateTaskRoomInfo(room);

                            taskAdapter.notifyDataSetChanged();
                        }
                    });

                    dialog.show(RoomActivity.this.getSupportFragmentManager(),"Room Option Dialog");
                }
                else {
                    // You wanna leave ? Than leave
                    Alert alert = new Alert(RoomActivity.this);
                    alert.setTitle(getString(R.string.leave_room) + " " + room.getTitle());
                    alert.setDescription(getString(R.string.do_you_really_wnat_t_leave));
                    alert.setPositiveButtonText(getString(R.string.yes));
                    alert.setNegativeButtonText(getString(R.string.no));
                    alert.setNegativeButtonOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            alert.dismiss();
                        }
                    });

                    alert.setPositiveButtonOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            databaseHelper.leaveRoom(room, auth.getUid(), new OnCompleteListener() {
                                @Override
                                public void onComplete(@NonNull com.google.android.gms.tasks.Task task) {
                                    if (task.isSuccessful()){
                                        left = true;
                                        finish();
                                    }
                                    else {}
                                }
                            });
                            alert.dismiss();
                        }
                    });

                    alert.show();
                }
            }
        });

        addTaskButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AddTaskDialog dialog = new AddTaskDialog(RoomActivity.this,RoomActivity.this.getSupportFragmentManager());
                dialog.show(RoomActivity.this.getSupportFragmentManager(),"Add Task");
            }
        });

        addMemberButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AddRoomMemberDialog dialog = new AddRoomMemberDialog(RoomActivity.this, RoomActivity.this.getSupportFragmentManager());
                dialog.setOnEmailValidListener(new AddRoomMemberDialog.OnEmailValidListener() {
                    @Override
                    public void onEmailValid(String email) {
                        Log.i("EMAIL",email);

                        String id = getUserIdByEmail(email);

                        if (id != null){
                            databaseHelper.addMemberInRoom(room.getId(), id, new OnCompleteListener() {
                                @Override
                                public void onComplete(@NonNull com.google.android.gms.tasks.Task task) {
                                    if (task.isSuccessful()){
                                        dialog.dismiss();
                                        Toast.makeText(RoomActivity.this, RoomActivity.this.getString(R.string.user_is_now_member_of_this_room),Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }
                        else Toast.makeText(RoomActivity.this, RoomActivity.this.getString(R.string.user_doesnt_exists),Toast.LENGTH_SHORT).show();
                    }
                });
                dialog.show(RoomActivity.this.getSupportFragmentManager(),"Add Member");
            }
        });
    }

    private void updateRoomChangeableInfo(Room room){
        this.room = room;

        roomTitle.setText(room.getTitle());
        roomDescription.setText(room.getDescription());
        cardBackground.setBackgroundColor(Color.parseColor(Helpers.Colors.addOpacity(room.getColor(),"AD")));

        if (taskAdapter != null) {
            for (int i = 0; i < taskData.size(); i++) {
                taskData.get(i).setColor(room.getColor());
            }
            taskAdapter.notifyDataSetChanged();
        }
    }

    private void updateTaskRoomInfo(Room room){
        for (int i = 0; i < taskData.size(); i++) {
            taskData.get(i).setRoom(room.getTitle());
        }
    }

    private String getUserIdByEmail(String email){
        email = email.trim();
        for (int i = 0; i < usersData.size(); i++) {
            if (usersData.get(i).getEmail().equals(email)) return usersData.get(i).getId();
        }

        return null;
    }

    private ArrayList<User> filterUsersForThisRoom(){
        ArrayList<User> filtered = new ArrayList<>();

        for (int i = 0; i < usersData.size(); i++) {
            if (room.getUserIds().contains(usersData.get(i).getId())) filtered.add(usersData.get(i));
        }

        return filtered;
    }

    @Override
    public void finish() {
        room.setJustLeft(left);

        // Setting up room so activity can update its information's
        Intent intent = getIntent();
        intent.putExtra(Constants.Firebase.DOCUMENT_ROOMS,room);

        setResult(Constants.Result.ROOM_CHANGED,intent);
        super.finish();
    }
}