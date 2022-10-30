package sk.dzurikm.domestio.activities;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;

import sk.dzurikm.domestio.R;
import sk.dzurikm.domestio.adapters.HomeActivityRoomAdapter;
import sk.dzurikm.domestio.adapters.HomeActivityTaskAdapter;
import sk.dzurikm.domestio.helpers.Constants;
import sk.dzurikm.domestio.helpers.DatabaseHelper;
import sk.dzurikm.domestio.helpers.Helpers;
import sk.dzurikm.domestio.models.Room;
import sk.dzurikm.domestio.models.Task;
import sk.dzurikm.domestio.models.User;
import sk.dzurikm.domestio.services.NotificationService;
import sk.dzurikm.domestio.views.dialogs.AddRoomDialog;
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

        // Login info
        Log.i("Firebase user logged in UID",FirebaseAuth.getInstance().getCurrentUser().getUid());

        // Datasets init
        roomData = new ArrayList<>();
        taskData = new ArrayList<Task>();
        usersData = new ArrayList<User>();

        // Helpers init
        snapHelper = new PagerSnapHelper();
        databaseHelper = new DatabaseHelper();

        // Loading data from database and setting them to datasets
        loadData();

        // Dialogs init
        MenuDialog menuDialog = new MenuDialog(HomeActivity.this, HomeActivity.this.getSupportFragmentManager(), new AddRoomDialog.OnRoomCreatedListener() {
            @Override
            public void onRoomCreate(Room room) {
                // update room

                System.out.println(room.toString());
                roomData.add(room);
                roomAdapter.notifyDataSetChanged();
            }
        });

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

                hideLoading();
            }
        });
        databaseHelper.getData(Constants.Firebase.DATA_FOR_USER);
    }


    private void hideLoading(){
        if (roomData.isEmpty()) noRoomsText.setVisibility(View.VISIBLE);
        if (taskData.isEmpty()) noTasksText.setVisibility(View.VISIBLE);
        // Creating adapters needed
        roomAdapter = new HomeActivityRoomAdapter(HomeActivity.this, roomData, new HomeActivityRoomAdapter.OnRoomLeaveListener() {
            @Override
            public void onRoomLeave(Room room) {
                cleanAfterLeftRoom(room);
            }
        });
        taskAdapter = new HomeActivityTaskAdapter(HomeActivity.this,taskData);

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
                    cleanAfterLeftRoom(room);
                }else onRoomChanged(room);

                break;
        }
    }

    private void onRoomChanged(Room room){
        for (int i = 0; i < roomData.size(); i++) {
            if (roomData.get(i).getId().equals(room.getId())) {
                roomData.set(i, room);
                break;
            }
        }

        for (int i = 0; i < taskData.size(); i++) {
            if (taskData.get(i).getRoomId().equals(room.getId())) {
                taskData.get(i).setRoom(room.getTitle());
                taskData.get(i).setColor(room.getColor());
                break;
            }
        }

        roomAdapter.notifyDataSetChanged();
        taskAdapter.notifyDataSetChanged();

    }

    private void cleanAfterLeftRoom(Room room){
        for (int i = 0; i < roomData.size(); i++) {
            if (roomData.get(i).getId().equals(room.getId())) {
                roomData.remove(i);

                roomAdapter.notifyDataSetChanged();
                removeAllTasksWithRoomId(room.getId());

                if (taskData.isEmpty()) noTasksText.setVisibility(View.VISIBLE);
                if (roomData.isEmpty()) noRoomsText.setVisibility(View.VISIBLE);
                break;
            }
        }
    }

    private void removeAllTasksWithRoomId(String id){
        for (int i = 0; i < taskData.size(); i++) {
            if (taskData.get(i).getRoomId().equals(id)){
                taskData.remove(i);
            }
        }

        taskAdapter.notifyDataSetChanged();
    }


}