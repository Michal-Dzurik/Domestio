package sk.dzurikm.domestio.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
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
import sk.dzurikm.domestio.adapters.RoomSpinnerAdapter;
import sk.dzurikm.domestio.adapters.StringSpinnerAdapter;
import sk.dzurikm.domestio.broadcasts.DataChangedReceiver;
import sk.dzurikm.domestio.helpers.Constants;
import sk.dzurikm.domestio.helpers.DCO;
import sk.dzurikm.domestio.helpers.DataStorage;
import sk.dzurikm.domestio.helpers.DatabaseHelper;
import sk.dzurikm.domestio.helpers.Helpers;
import sk.dzurikm.domestio.models.Room;
import sk.dzurikm.domestio.models.Task;
import sk.dzurikm.domestio.models.User;
import sk.dzurikm.domestio.views.alerts.Alert;
import sk.dzurikm.domestio.views.dialogs.AddRoomMemberDialog;
import sk.dzurikm.domestio.views.dialogs.AddTaskDialog;
import sk.dzurikm.domestio.views.dialogs.ListMembersDialog;
import sk.dzurikm.domestio.views.dialogs.RoomOptionDialog;

public class RoomActivity extends AppCompatActivity {

    // Views
    RecyclerView roomsRecycler;
    TextView roomTitle,roomDescription,roomPeopleCount,roomTaskCount,noTaskText;
    ImageButton backButton,addTaskButton,optionButton,addMemberButton;
    LinearLayout cardBackground;
    Spinner taskSpinner;

    // Helpers
    DatabaseHelper databaseHelper;
    FirebaseAuth auth;
    DCO dco;

    // Datasets
    ArrayList<Task> taskData;
    ArrayList<Task> allRoomTaskData;
    ArrayList<User> usersData;
    Room room;

    // Broadcasts
    DataChangedReceiver dataChangedReceiver;

    // Adapters
    HomeActivityTaskAdapter taskAdapter;

    int tasksFilter = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room);

        // DB
        auth = FirebaseAuth.getInstance();

        room = (Room) getIntent().getExtras().get("room");
        Log.i("CURRENT_ROOM",room.toString());

        // helpers
        databaseHelper = new DatabaseHelper();

        // Broadcasts
        dataChangedReceiver = new DataChangedReceiver(new DataChangedReceiver.DataChangedListener() {
            @Override
            public void onDataChanged(HashMap<String, Object> data, String collection, String documentID, DocumentChange.Type type) {

                switch (collection){
                    case Constants.Firebase.DOCUMENT_ROOMS:
                        Room room = new Room();
                        room.cast(documentID,data);

                        switch (type){
                            case ADDED:
                                dco.addRoom(room);
                                break;
                            case MODIFIED:
                                dco.updateRoomChangeableInfo(taskData,room);
                                break;
                            case REMOVED:

                                break;
                        }

                        break;

                    case Constants.Firebase.DOCUMENT_TASKS:
                        Task task = new Task();
                        task.cast(documentID,data);

                        task.setAuthor(Helpers.DataSet.getAuthorName(usersData,task.getAuthorId()));
                        task.setRoomName(RoomActivity.this.room.getTitle());
                        task.setColor(RoomActivity.this.room.getColor());


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

                    switch (type){
                        case ADDED:
                            dco.addUser(user);
                            break;
                        case MODIFIED:
                            dco.updatedUser(user);
                            break;
                    }

                        break;
                }
            }

        });
        IntentFilter intentSFilter = new IntentFilter("DATA_CHANGED");
        registerReceiver(dataChangedReceiver, intentSFilter);

        databaseHelper.loadTasksForRoom(room, new DatabaseHelper.TasksForRoomLoadedListener() {
            @Override
            public void onTasksLoaded(ArrayList<Task> data) {
                RoomActivity.this.allRoomTaskData = data;
                System.out.println("DATA" + data);
            }
        });


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
        noTaskText = findViewById(R.id.noTasksText);
        taskSpinner = findViewById(R.id.taskSpinner);

        //Spinners
        taskSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position){
                    case Constants.Spinner.Room.ALL_TASKS:
                        tasksFilter = Constants.Spinner.Room.ALL_TASKS;
                        if (RoomActivity.this.allRoomTaskData != null){
                            RoomActivity.this.taskData.clear();
                            RoomActivity.this.taskData.addAll(RoomActivity.this.allRoomTaskData);
                        }
                        if(taskAdapter != null) taskAdapter.notifyDataSetChanged();
                        break;
                    case Constants.Spinner.Room.MY_TASKS:
                        tasksFilter = Constants.Spinner.Room.MY_TASKS;
                        RoomActivity.this.taskData.clear();
                        RoomActivity.this.taskData.addAll(Helpers.DataSet.filterOnlyRelevantTasksForRoom(dco.getTaskData(),room.getId()));
                        if(taskAdapter != null) taskAdapter.notifyDataSetChanged();
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        StringSpinnerAdapter roomAdapter = new StringSpinnerAdapter((Activity) RoomActivity.this, Helpers.getArray(RoomActivity.this,R.array.taskSpinner));
        taskSpinner.setAdapter(roomAdapter);

        // Helpers
        dco = new DCO(new DCO.OnDataChangeListener() {
            @Override
            public void onChange(ArrayList<User> usersData, ArrayList<Room> roomData, ArrayList<Task> taskData) {
                Room newRoom = dco.getRoom(room.getId());
                System.out.println(newRoom);

                if (usersData != null){
                    RoomActivity.this.usersData = usersData;
                }

                if (taskData != null){
                    // Removing finished tasks
                    switch (tasksFilter){
                        case Constants.Spinner.Room.ALL_TASKS:
                            tasksFilter = Constants.Spinner.Room.ALL_TASKS;
                            databaseHelper.loadTasksForRoom(room, new DatabaseHelper.TasksForRoomLoadedListener() {
                                @Override
                                public void onTasksLoaded(ArrayList<Task> data) {
                                    RoomActivity.this.allRoomTaskData = data;
                                    RoomActivity.this.taskData.clear();
                                    RoomActivity.this.taskData.addAll(data);
                                    if(taskAdapter != null) taskAdapter.notifyDataSetChanged();
                                }
                            });
                            break;
                        case Constants.Spinner.Room.MY_TASKS:
                            tasksFilter = Constants.Spinner.Room.MY_TASKS;
                            RoomActivity.this.taskData.clear();
                            RoomActivity.this.taskData.addAll(Helpers.DataSet.filterOnlyRelevantTasksForRoom(dco.getTaskData(),room.getId()));
                            if(taskAdapter != null) taskAdapter.notifyDataSetChanged();
                            break;
                    }

                    if(taskAdapter != null) taskAdapter.notifyDataSetChanged();

                    hideNoTasksText();
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

        // Setting up empty adapter
        taskData = Helpers.DataSet.filterOnlyRelevantTasksForRoom(dco.getTaskData(),room.getId());
        taskAdapter = new HomeActivityTaskAdapter(RoomActivity.this,taskData,new HomeActivityTaskAdapter.OnDoneClickListener() {
            @Override
            public void onDoneClick(Task task) {
                dco.updateTask(task);
            }
        }){
            @Override
            public void refresh(){
                hideNoTasksText();
            }
        };
        roomsRecycler.setAdapter(taskAdapter);
        hideNoTasksText();

        // Setting up values
        dco.updateRoomChangeableInfo(taskData,room);
        roomTaskCount.setText(String.valueOf(room.getTasksCount()));
        roomPeopleCount.setText(String.valueOf(room.getPeopleCount()));
        cardBackground.setBackgroundColor(Color.parseColor(Helpers.Colors.addOpacity(room.getColor(),"AD")));
        if (!room.isAdmin(auth.getUid())){
            optionButton.setImageResource(R.drawable.ic_leave);
            optionButton.setBackgroundResource(R.drawable.leave_button_background);

            addMemberButton.setImageResource(R.drawable.round_group);
        }


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
                    RoomOptionDialog dialog = new RoomOptionDialog(RoomActivity.this, RoomActivity.this.getSupportFragmentManager(), dco.filterUsersForThisRoom(room.getUserIds()), new RoomOptionDialog.RoomRemoveListener() {
                        @Override
                        public void onRoomRemove() {
                            finish();
                        }
                    });
                    dialog.setRoom(room);
                    dialog.setRoomDataChangedListener(new RoomOptionDialog.RoomDataChangedListener() {
                        @Override
                        public void onRoomDataChangedListener(Room room) {
                            dco.updateRoomChangeableInfo(taskData,room);
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
                            dco.leaveRoom(room, auth.getUid(), new OnCompleteListener() {
                                @Override
                                public void onComplete(@NonNull com.google.android.gms.tasks.Task task) {
                                    finish();
                                    alert.dismiss();
                                }
                            });

                        }
                    });

                    alert.show();
                }
            }
        });

        addTaskButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AddTaskDialog dialog = new AddTaskDialog(RoomActivity.this, RoomActivity.this.getSupportFragmentManager(), usersData, new ArrayList<Room>(Collections.singleton(room)), new AddTaskDialog.OnTaskChangeListener() {
                    @Override
                    public void onTaskAdded(Task task) {
                        // Add task id to room and increment count of them
                        // I am adding nothing cause all the task you see are just your and you cant make your own
                    }

                    @Override
                    public void onTaskEdited(Task task) {

                    }
                });
                dialog.show(RoomActivity.this.getSupportFragmentManager(),"Add Task");
            }
        });

        addMemberButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (room.isAdmin(auth.getUid())){
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
                                            dco.updateRoomChangeableInfo(taskData,room);
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
                else{
                    ListMembersDialog listMembersDialog = new ListMembersDialog(RoomActivity.this,RoomActivity.this.getSupportFragmentManager(),dco.filterUsersForThisRoom(room.getUserIds()),room,false);
                    listMembersDialog.show(RoomActivity.this.getSupportFragmentManager(),"List of members");
                }
            }
        });
    }

    private void hideNoTasksText(){
        if (taskData.size() == 0) noTaskText.setVisibility(View.VISIBLE);
        else noTaskText.setVisibility(View.GONE);
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();
    }

    @Override
    public void finish() {
        unregisterReceiver(dataChangedReceiver);
        super.finish();
    }
}