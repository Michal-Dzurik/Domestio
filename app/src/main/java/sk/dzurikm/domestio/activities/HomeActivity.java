package sk.dzurikm.domestio.activities;

import static sk.dzurikm.domestio.helpers.Constants.Result.PROFILE_ACTIVITY;

import androidx.annotation.ColorInt;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;

import java.util.ArrayList;
import java.util.HashMap;

import sk.dzurikm.domestio.R;
import sk.dzurikm.domestio.adapters.HomeActivityRoomAdapter;
import sk.dzurikm.domestio.adapters.HomeActivityTaskAdapter;
import sk.dzurikm.domestio.helpers.broadcasts.DataChangedReceiver;
import sk.dzurikm.domestio.helpers.broadcasts.NetworkChangeReceiver;
import sk.dzurikm.domestio.helpers.Constants;
import sk.dzurikm.domestio.helpers.DCO;
import sk.dzurikm.domestio.helpers.DataStorage;
import sk.dzurikm.domestio.helpers.DatabaseHelper;
import sk.dzurikm.domestio.helpers.Helpers;
import sk.dzurikm.domestio.models.Room;
import sk.dzurikm.domestio.models.Task;
import sk.dzurikm.domestio.models.User;
import sk.dzurikm.domestio.services.NotificationService;
import sk.dzurikm.domestio.views.alerts.Alert;
import sk.dzurikm.domestio.views.dialogs.AddRoomDialog;
import sk.dzurikm.domestio.views.dialogs.AddTaskDialog;
import sk.dzurikm.domestio.views.dialogs.MenuDialog;

public class HomeActivity extends AppCompatActivity {

    // Views
    RecyclerView horizontalRoomSlider,verticalTaskSlider;
    View loading,offlineBar;
    TextView userName;
    ImageButton menuButton, profileButton;
    LinearLayout noTasksText,noRoomsText;

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
    NetworkChangeReceiver networkChangeReceiver;
    IntentFilter dataChangeBroadcastFilter;

    Window window;

    FirebaseAuth auth;

    SharedPreferences sharedPreferences;
    SharedPreferences.Editor sharedPreferencesEditor;

    Alert appPermissionsAlert;
    PowerManager pm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Shared preferences
        sharedPreferences = getSharedPreferences(Constants.SHARED_PREFERENCES_KEY,MODE_PRIVATE);

        /* Setting user name for greeting */
        userName = findViewById(R.id.userName);
        userName.setText(sharedPreferences.getString("user-name","Anonymous"));

        // Views with need of adapter
        horizontalRoomSlider = findViewById(R.id.horizontalRoomSlider);
        verticalTaskSlider = findViewById(R.id.verticalTaskSlider);

        // Views
        profileButton = findViewById(R.id.profileButton);
        menuButton = findViewById(R.id.menuButton);
        loading = findViewById(R.id.loading);
        noRoomsText = findViewById(R.id.noRoomsText);
        noTasksText = findViewById(R.id.noTasksText);
        offlineBar = findViewById(R.id.offlineBar);

        loading.setVisibility(View.VISIBLE);

        pm = (PowerManager) getSystemService(Context.POWER_SERVICE);

        // Set up appPermissionsAlert for app permission
        appPermissionsAlert = new Alert(HomeActivity.this);
        appPermissionsAlert.setTitle(getString(R.string.app_permission));
        appPermissionsAlert.setDescription(getString(R.string.app_permission_descirption));
        appPermissionsAlert.setPositiveButtonText(getString(R.string.enable));
        appPermissionsAlert.setNegativeButtonText("");
        appPermissionsAlert.setCanceledOnTouchOutside(false);
        appPermissionsAlert.setCancelable(false);
        appPermissionsAlert.setPositiveButtonOnClickListener(view -> {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                intent.setData(Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, 1002);
            }
        });
        appPermissionsAlert.disableNegativeButton(true);
        appPermissionsAlert.setNegativeButtonOnClickListener(view -> {

        });

        checkForBatteryPermissions();

        // Login info
        auth = FirebaseAuth.getInstance();
        Log.i("Firebase user logged in UID",auth.getUid());

        // Datasets init
        roomData = new ArrayList<Room>();
        taskData = new ArrayList<Task>();
        usersData = new ArrayList<User>();

        // Helpers init
        snapHelper = new PagerSnapHelper();
        databaseHelper = new DatabaseHelper();

        window = HomeActivity.this.getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

        // Loading data from database and setting them to datasets
        loadData();

        // Broadcasts
        dataChangedReceiver = new DataChangedReceiver(new DataChangedReceiver.DataChangedListener() {
            @Override
            public void onDataChanged(HashMap<String, Object> data, String collection,String documentID, DocumentChange.Type type) {
                Log.i("On Change", "On Change event triggered");
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

                        HashMap<String,String> dat = Helpers.DataSet.getRoomInfo(roomData,task.getRoomId(), new String[]{Constants.Firebase.Room.FIELD_TITLE, Constants.Firebase.Room.FIELD_COLOR});
                        task.setAuthor(Helpers.DataSet.getAuthorName(usersData,task.getAuthorId()));
                        task.setRoomName(dat.get(Constants.Firebase.Room.FIELD_TITLE));
                        task.setColor(dat.get(Constants.Firebase.Room.FIELD_COLOR));

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
                        user.setId(documentID);
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
        dataChangeBroadcastFilter = new IntentFilter("DATA_CHANGED");
        registerReceiver(dataChangedReceiver, dataChangeBroadcastFilter);

        networkChangeReceiver = new NetworkChangeReceiver(new NetworkChangeReceiver.NetworkStatusChangeListener() {
            @SuppressLint("ResourceType")
            @Override
            public void onChange() {
                changeConnectionStatus();
            }
        });
        registerReceiver(networkChangeReceiver,new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"));


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

                unregisterBroadcasts();
                HomeActivity.this.startActivityForResult(profileActivityIntent,PROFILE_ACTIVITY);
            }
        });

    }

    @SuppressLint("ResourceType")
    private void changeConnectionStatus(){
        if (DataStorage.connected){
            // Connected
            offlineBar.setVisibility(View.GONE);
            TypedValue typedValue = new TypedValue();
            Resources.Theme theme = HomeActivity.this.getTheme();
            theme.resolveAttribute(R.attr.BackgroundColor, typedValue, true);
            @ColorInt int color = typedValue.resourceId;
            System.out.println(color);
            window.setStatusBarColor(ContextCompat.getColor(HomeActivity.this, color));
        }
        else{
            // Disconnected
            offlineBar.setVisibility(View.VISIBLE);
            window.setStatusBarColor(ContextCompat.getColor(HomeActivity.this,R.color.red));

        }
    }

    private void loadData() {
        databaseHelper.setOnDataLoadedListener(new DatabaseHelper.OnDataLoadedListener() {
            @Override
            public void onDataLoaded(ArrayList<Room> roomData, ArrayList<Task> taskData, ArrayList<User> userData) {
                Log.i("Data loaded","\nRoom - " + roomData + "\nTasks - " + taskData + "\nUsers - " + userData);
                if (HomeActivity.this.roomData != null) {
                    HomeActivity.this.roomData.clear();
                    HomeActivity.this.roomData.addAll(roomData);
                }
                else HomeActivity.this.roomData = roomData;


                if (HomeActivity.this.taskData != null) {
                    HomeActivity.this.taskData.clear();
                    HomeActivity.this.taskData.addAll(Helpers.DataSet.filterOnlyRelevantTasks(taskData));
                }
                else HomeActivity.this.taskData = taskData;


                if (HomeActivity.this.usersData != null) {
                    HomeActivity.this.usersData.clear();
                    HomeActivity.this.usersData.addAll(userData);
                }
                else HomeActivity.this.usersData = userData;

                DataStorage.rooms = roomData;
                DataStorage.tasks = taskData;
                DataStorage.users = userData;


                // Dialogs init
                menuDialog = new MenuDialog(HomeActivity.this, HomeActivity.this.getSupportFragmentManager(), roomData, usersData, new AddRoomDialog.OnRoomCreatedListener() {
                    @Override
                    public void onRoomCreate(Room room) {
                        // update room
                        Log.i("Room Created","Now");
                        dco.addRoom(room);
                    }
                }, new AddTaskDialog.OnTaskChangeListener() {
                    @Override
                    public void onTaskAdded(Task task) {
                        // Add id to room and then notify adapter
                        dco.onTaskAdded(task);
                    }

                    @Override
                    public void onTaskEdited(Task task) {

                    }
                });

                hideLoading();
            }
        });
        databaseHelper.getData(Constants.Firebase.DATA_FOR_USER,null);
    }

    private void refreshNoDataTexts(){
        // Room no data message
        if (roomData != null && roomData.isEmpty()) noRoomsText.setVisibility(View.VISIBLE);
        else noRoomsText.setVisibility(View.GONE);

        // Task no data message

        if (taskData != null && taskData.isEmpty()) {
            noTasksText.setVisibility(View.VISIBLE);
        }
        else noTasksText.setVisibility(View.GONE);
    }

    private void hideLoading(){
        refreshNoDataTexts();
        // Creating adapters needed
        roomAdapter = new HomeActivityRoomAdapter(HomeActivity.this, roomData, new HomeActivityRoomAdapter.OnRoomLeaveListener() {
            @Override
            public void onRoomLeave(Room room) {
                dco.cleanAfterLeftRoom(room);
            }
        });
        taskAdapter = new HomeActivityTaskAdapter(HomeActivity.this, taskData, new HomeActivityTaskAdapter.OnDoneClickListener() {
            @Override
            public void onDoneClick(Task task) {
                dco.updateTask(task);
            }
        }){
            @Override
            public void refresh(){
                refreshNoDataTexts();
            }
        };

        // Setting up DCO

        dco = new DCO(roomData, taskData, usersData, new DCO.OnDataChangeListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onChange(ArrayList<User> usersData, ArrayList<Room> roomData, ArrayList<Task> taskData) {
                Log.i("DCO On Change","DCO On Change triggered");
                if (usersData != null) {

                    if (HomeActivity.this.usersData != null) {
                        HomeActivity.this.usersData.clear();
                        HomeActivity.this.usersData.addAll(usersData);
                    }
                    else HomeActivity.this.usersData = (usersData);

                    Log.i("DCO Update Users", String.valueOf(usersData));
                }

                if (taskData != null) {
                    Log.i("DCO On Change new list", String.valueOf(taskData));
                    ArrayList<Task> newList = Helpers.DataSet.filterOnlyRelevantTasks(taskData);

                    if (HomeActivity.this.taskData != null) {
                        HomeActivity.this.taskData.clear();
                        HomeActivity.this.taskData.addAll(newList);
                    }
                    else HomeActivity.this.taskData = (newList);

                    HomeActivity.this.taskAdapter.refresh();
                }

                if (roomData != null) {
                    Log.i("Room Data Changed", String.valueOf(roomData));
                    if (HomeActivity.this.roomData != null) {
                        HomeActivity.this.roomData.clear();
                        HomeActivity.this.roomData.addAll(roomData);
                    }
                    else HomeActivity.this.roomData = (roomData);

                    HomeActivity.this.roomAdapter.notifyDataSetChanged();
                    refreshNoDataTexts();
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
    protected void onResume() {
        if (HomeActivity.this.usersData != null && DataStorage.users != null) {
            HomeActivity.this.usersData.clear();
            HomeActivity.this.usersData.addAll(DataStorage.users);
        }
        else HomeActivity.this.usersData = (DataStorage.users);

        if (HomeActivity.this.usersData != null && DataStorage.rooms != null) {
            HomeActivity.this.roomData.clear();
            HomeActivity.this.roomData.addAll(DataStorage.rooms);
        }
        else HomeActivity.this.roomData = (DataStorage.rooms);

        if (DataStorage.tasks != null){
            taskData = Helpers.DataSet.filterOnlyRelevantTasks(DataStorage.tasks);
        }

        if (roomAdapter != null) roomAdapter.notifyDataSetChanged();
        if (taskAdapter != null) taskAdapter.notifyDataSetChanged();

        refreshNoDataTexts();

        registerReceiver(dataChangedReceiver, dataChangeBroadcastFilter);

        changeConnectionStatus();

        checkForBatteryPermissions();

        super.onResume();
    }

    public void preferencesHasBeenChanged(){
        taskAdapter.notifyDataSetChanged();
    }

    public void checkForBatteryPermissions(){
        String packageName = getPackageName();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if (!pm.isIgnoringBatteryOptimizations(packageName)) {
                if (!appPermissionsAlert.isShowing()) appPermissionsAlert.show();
                // it is not enabled. Ask the user to do so from the settings.
            }
            else{
                if (appPermissionsAlert.isShowing()) appPermissionsAlert.dismiss();
            }
        }

    }



    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStart() {
        overridePendingTransition(0,0);
        super.onStart();

    }

    @Override
    protected void onDestroy() {

        super.onDestroy();
    }

    public void unregisterBroadcasts(){
        unregisterReceiver(dataChangedReceiver);
    }
}