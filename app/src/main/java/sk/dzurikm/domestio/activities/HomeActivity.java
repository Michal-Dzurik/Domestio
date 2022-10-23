package sk.dzurikm.domestio.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;

import android.content.Intent;
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
import sk.dzurikm.domestio.views.dialogs.MenuDialog;

public class HomeActivity extends AppCompatActivity {

    // Views
    RecyclerView horizontalRoomSlider,verticalTaskSlider;
    View loading;
    TextView userName;
    ImageButton menuButton, profileButton;
    TextView seeAllTasksButton,seeAllRoomsButton;

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
        seeAllRoomsButton = findViewById(R.id.seeAllRoomsButton);
        seeAllTasksButton = findViewById(R.id.seeAllTasksButton);
        loading = findViewById(R.id.loading);


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
        MenuDialog menuDialog = new MenuDialog(HomeActivity.this,HomeActivity.this.getSupportFragmentManager());

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

        seeAllRoomsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent yourRoomsActivityIntent = new Intent(HomeActivity.this,YourRoomsActivity.class );
                yourRoomsActivityIntent.putExtra(Constants.Firebase.Bundle.ROOMS, roomData);

                startActivity(yourRoomsActivityIntent);
            }
        });

        seeAllTasksButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent yourTasksActivityIntent = new Intent(HomeActivity.this,YourTasksActivity.class );
                yourTasksActivityIntent.putExtra(Constants.Firebase.Bundle.TASKS, taskData);

                startActivity(yourTasksActivityIntent);
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
        // Creating adapters needed
        roomAdapter = new HomeActivityRoomAdapter(HomeActivity.this,roomData);
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
}