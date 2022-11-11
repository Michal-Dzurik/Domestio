package sk.dzurikm.domestio.activities;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;

import android.app.ActivityManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import sk.dzurikm.domestio.R;
import sk.dzurikm.domestio.adapters.HomeActivityRoomAdapter;
import sk.dzurikm.domestio.adapters.HomeActivityTaskAdapter;
import sk.dzurikm.domestio.broadcasts.DataChangedReceiver;
import sk.dzurikm.domestio.helpers.Constants;
import sk.dzurikm.domestio.helpers.DCO;
import sk.dzurikm.domestio.helpers.DatabaseHelper;
import sk.dzurikm.domestio.helpers.Helpers;
import sk.dzurikm.domestio.models.Room;
import sk.dzurikm.domestio.models.Task;
import sk.dzurikm.domestio.models.User;
import sk.dzurikm.domestio.services.NotificationService;
import sk.dzurikm.domestio.views.dialogs.AddRoomDialog;
import sk.dzurikm.domestio.views.dialogs.AddTaskDialog;
import sk.dzurikm.domestio.views.dialogs.MenuDialog;

public class HomeActivity extends AppCompatActivity {

    // Views
    RecyclerView horizontalRoomSlider,verticalTaskSlider;
    View loading;
    TextView userName;
    ImageButton menuButton, profileButton;
    TextView noTasksText,noRoomsText;

    // Datasets
    ArrayList<Room> roomData;
    ArrayList<Task> taskData;
    ArrayList<User> usersData;

    // Adapters
    HomeActivityRoomAdapter roomAdapter;
    HomeActivityTaskAdapter taskAdapter;

    // Helpers
    SnapHelper snapHelper;
    DatabaseHelper databaseHelper;
    DCO dco;

    // Dialogs
    MenuDialog menuDialog;

    // Broadcast receivers
    DataChangedReceiver dataChangedReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        /* Setting user name for greeting */
        userName = findViewById(R.id.userName);
        userName.setText(getIntent().getStringExtra("user_name"));

        // Views with need of adapter
        horizontalRoomSlider = findViewById(R.id.horizontalRoomSlider);
        verticalTaskSlider = findViewById(R.id.verticalTaskSlider);

        // Views
        profileButton = findViewById(R.id.profileButton);
        menuButton = findViewById(R.id.menuButton);
        loading = findViewById(R.id.loading);
        noRoomsText = findViewById(R.id.noRoomsText);
        noTasksText = findViewById(R.id.noTasksText);

        loading.setVisibility(View.VISIBLE);

        // Login info
        Log.i("Firebase user logged in UID",FirebaseAuth.getInstance().getCurrentUser().getUid());

        // Datasets init
        roomData = new ArrayList<Room>();
        taskData = new ArrayList<Task>();
        usersData = new ArrayList<User>();

        // Helpers init
        snapHelper = new PagerSnapHelper();
        databaseHelper = new DatabaseHelper();

        // Loading data from database and setting them to datasets
        loadData();

        // Broadcasts
        dataChangedReceiver = new DataChangedReceiver(new DataChangedReceiver.DataChangedListener() {
            @Override
            public void onDataChanged(HashMap<String, Object> data, String collection,String documentID, DocumentChange.Type type) {
                switch (collection){
                    case Constants.Firebase.DOCUMENT_ROOMS:
                        Room room = new Room();
                        room.cast(documentID,data);

                        switch (type){
                            case ADDED:
                                dco.addRoom(room);
                                break;
                            case MODIFIED:
                                dco.onRoomChanged(room);
                                break;
                            case REMOVED:
                                dco.cleanAfterLeftRoom(room);
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


        // Setting up listeners
        menuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Show Menu dialog
                menuDialog.show(getSupportFragmentManager(),"MenuDialog");
            }
        });

        profileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent profileActivityIntent = new Intent(HomeActivity.this,ProfileActivity.class );

                startActivity(profileActivityIntent);
            }
        });

    }

    private void loadData() {
        databaseHelper.setOnDataLoadedListener(new DatabaseHelper.OnDataLoadedListener() {
            @Override
            public void onDataLoaded(ArrayList<Room> roomData, ArrayList<Task> taskData, ArrayList<User> userData) {
                HomeActivity.this.roomData = roomData;
                HomeActivity.this.taskData = taskData;
                HomeActivity.this.usersData = userData;

                // Dialogs init
                menuDialog = new MenuDialog(HomeActivity.this, HomeActivity.this.getSupportFragmentManager(), roomData, usersData, new AddRoomDialog.OnRoomCreatedListener() {
                    @Override
                    public void onRoomCreate(Room room) {
                        // update room

                        System.out.println(room.toString());
                        dco.addRoom(room);
                    }
                }, new AddTaskDialog.OnTaskAddedListener() {
                    @Override
                    public void onTaskAdded(Task task) {
                        // Add id to room and then notify adapter
                        dco.onTaskAdded(task);
                    }
                });

                hideLoading();
            }
        });
        databaseHelper.getData(Constants.Firebase.DATA_FOR_USER);
    }

    private void hideNoDataMessages(){
        if (roomData.isEmpty()) noRoomsText.setVisibility(View.VISIBLE);
        else noRoomsText.setVisibility(View.GONE);
        if (taskData.isEmpty()) noTasksText.setVisibility(View.VISIBLE);
        else noTasksText.setVisibility(View.GONE);
    }

    private void hideLoading(){
        hideNoDataMessages();
        // Creating adapters needed
        roomAdapter = new HomeActivityRoomAdapter(HomeActivity.this, roomData, new HomeActivityRoomAdapter.OnRoomLeaveListener() {
            @Override
            public void onRoomLeave(Room room) {
                dco.cleanAfterLeftRoom(room);
            }
        });
        taskAdapter = new HomeActivityTaskAdapter(HomeActivity.this,taskData);

        // Setting up DCO
        dco = new DCO(roomData, taskData, usersData, new DCO.OnDataChangeListener() {
            @Override
            public void onChange(ArrayList<User> usersData, ArrayList<Room> roomData, ArrayList<Task> taskData) {

                if (usersData != null) {
                    HomeActivity.this.usersData = usersData;
                }

                if (taskData != null) {
                    HomeActivity.this.taskData = taskData;
                    taskAdapter.notifyDataSetChanged();
                    hideNoDataMessages();
                }

                if (roomData != null) {
                    HomeActivity.this.roomData = roomData;
                    roomAdapter.notifyDataSetChanged();
                    hideNoDataMessages();
                }

            }
        });


        // Settings up the adapters and helpers
        horizontalRoomSlider.setAdapter(roomAdapter);
        snapHelper.attachToRecyclerView(horizontalRoomSlider);
        verticalTaskSlider.setAdapter(taskAdapter);

        // Starting Notification service
        // It listens if something in DB was changed or added and send a notification about it
        startService(new Intent( this, NotificationService.class ) );

        // Delay before showing home activity so there are no visual bugs
        Helpers.Time.delay(new Runnable() {
            @Override
            public void run() {
                loading.startAnimation(AnimationUtils.loadAnimation(HomeActivity.this,R.anim.fade_out));
                loading.getAnimation().setFillAfter(true);
            }
        },500);
    }


    @Override
    protected void onStart() {
        overridePendingTransition(0,0);
        super.onStart();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (resultCode) {
            case Constants.Result.ROOM_CHANGED:
                Room room = (Room) data.getExtras().get(Constants.Firebase.DOCUMENT_ROOMS);
                if (room.hasJustLeft()){
                    // remove room from list and update everything
                    dco.cleanAfterLeftRoom(room);
                }else dco.onRoomChanged(room);

                ArrayList<Task> tasks = (ArrayList<Task>) data.getExtras().get(Constants.Firebase.DOCUMENT_TASKS);
                if (!tasks.isEmpty()){
                    for (int i = 0; i < tasks.size(); i++) {
                        dco.addTask(tasks.get(i));
                    }

                }
                break;
        }
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(dataChangedReceiver);

        super.onDestroy();
    }
}