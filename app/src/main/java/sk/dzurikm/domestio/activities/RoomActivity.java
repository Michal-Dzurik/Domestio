package sk.dzurikm.domestio.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.IntentFilter;
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
import com.google.firebase.firestore.DocumentChange;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import sk.dzurikm.domestio.R;
import sk.dzurikm.domestio.adapters.HomeActivityTaskAdapter;
import sk.dzurikm.domestio.broadcasts.DataChangedReceiver;
import sk.dzurikm.domestio.helpers.Constants;
import sk.dzurikm.domestio.helpers.DCO;
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
    DCO dco;

    // Datasets
    ArrayList<Task> taskData;
    ArrayList<User> usersData;
    Room room;
    ArrayList<Task> taskDataAdded;

    // Broadcasts
    DataChangedReceiver dataChangedReceiver;


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

        // Broadcasts
        dataChangedReceiver = new DataChangedReceiver(new DataChangedReceiver.DataChangedListener() {
            @Override
            public void onDataChanged(HashMap<String, Object> data, String collection, String documentID, DocumentChange.Type type) {

                switch (collection){
                    case Constants.Firebase.DOCUMENT_ROOMS:
                        Room room = new Room();
                        room.cast(documentID,data);

                        System.out.println(type.name());

                        switch (type){
                            case ADDED:
                                dco.addRoom(room);
                                break;
                            case MODIFIED:
                                dco.updateRoomChangeableInfo(room);
                                break;
                            case REMOVED:

                                break;
                        }

                        break;

                    case Constants.Firebase.DOCUMENT_TASKS:
                        Task task = new Task();
                        task.cast(documentID,data);

                        System.out.println(type);
                        switch (type){
                            case ADDED:
                                dco.addTask(task);
                                break;
                            case MODIFIED:
                                dco.updateTask(task);
                                break;
                            case REMOVED:
                                dco.removeTask(task);
                                break;
                        }

                        break;

                    case Constants.Firebase.DOCUMENT_USERS:
                        User user = new User();
                        user.cast(data);

                        break;
                }
            }

        });
        IntentFilter intentSFilter = new IntentFilter("DATA_CHANGED");
        registerReceiver(dataChangedReceiver, intentSFilter);

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
        taskDataAdded = new ArrayList<>();
        taskData = new ArrayList<>();
        usersData = new ArrayList<>();

        // Helpers
        dco = new DCO(room, taskData, usersData, new DCO.OnDataChangeListener() {
            @Override
            public void onChange(ArrayList<User> usersData, ArrayList<Room> roomData, ArrayList<Task> taskData) {
                Room newRoom = dco.getRoom();
                System.out.println(newRoom);

                if (usersData != null){
                    RoomActivity.this.usersData = usersData;
                }

                if (taskData != null){
                    RoomActivity.this.taskData = taskData;
                    if(taskAdapter != null) taskAdapter.notifyDataSetChanged();
                }

                if (newRoom != null){
                    RoomActivity.this.room = newRoom;
                    roomTitle.setText(newRoom.getTitle());
                    roomDescription.setText(newRoom.getDescription());
                    cardBackground.setBackgroundColor(Color.parseColor(Helpers.Colors.addOpacity(newRoom.getColor(),"AD")));
                    roomTaskCount.setText(String.valueOf(newRoom.getTasksCount()));
                    roomPeopleCount.setText(String.valueOf(newRoom.getPeopleCount()));

                }
            }
        });

        // Setting up values
        dco.updateRoomChangeableInfo(room);
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

                dco.setTaskData(taskData);
                dco.setUsersData(userData);
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
                    RoomOptionDialog dialog = new RoomOptionDialog(RoomActivity.this,RoomActivity.this.getSupportFragmentManager(),dco.filterUsersForThisRoom());
                    dialog.setRoom(room);
                    dialog.setRoomDataChangedListener(new RoomOptionDialog.RoomDataChangedListener() {
                        @Override
                        public void onRoomDataChangedListener(Room room) {
                            dco.updateRoomChangeableInfo(room);
                            dco.updateTaskRoomInfo(room);

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
                AddTaskDialog dialog = new AddTaskDialog(RoomActivity.this, RoomActivity.this.getSupportFragmentManager(), usersData, new ArrayList<Room>(Collections.singleton(room)), new AddTaskDialog.OnTaskAddedListener() {
                    @Override
                    public void onTaskAdded(Task task) {
                        // Add task id to room and increment count of them
                        dco.addTask(task);
                        taskDataAdded.add(task);
                    }
                });
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

                        String id = dco.getUserIdByEmail(email);

                        if (id != null){
                            databaseHelper.addMemberInRoom(room.getId(), id, new OnCompleteListener() {
                                @Override
                                public void onComplete(@NonNull com.google.android.gms.tasks.Task task) {
                                    if (task.isSuccessful()){
                                        dialog.dismiss();
                                        room.addUserId(id);
                                        dco.updateRoomChangeableInfo(room);
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

    @Override
    public void finish() {
        room.setJustLeft(left);

        // Setting up room so activity can update its information's
        Intent intent = getIntent();
        intent.putExtra(Constants.Firebase.DOCUMENT_ROOMS,room);
        intent.putExtra(Constants.Firebase.DOCUMENT_TASKS,taskDataAdded);

        setResult(Constants.Result.ROOM_CHANGED,intent);
        super.finish();
    }
}