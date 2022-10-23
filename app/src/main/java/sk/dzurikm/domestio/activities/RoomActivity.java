package sk.dzurikm.domestio.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

import sk.dzurikm.domestio.R;
import sk.dzurikm.domestio.adapters.HomeActivityTaskAdapter;
import sk.dzurikm.domestio.helpers.Constants;
import sk.dzurikm.domestio.helpers.DatabaseHelper;
import sk.dzurikm.domestio.helpers.Helpers;
import sk.dzurikm.domestio.models.Room;
import sk.dzurikm.domestio.models.Task;
import sk.dzurikm.domestio.models.User;
import sk.dzurikm.domestio.views.dialogs.AddTaskDialog;
import sk.dzurikm.domestio.views.dialogs.RoomOptionDialog;

public class RoomActivity extends AppCompatActivity {

    // Views
    RecyclerView roomsRecycler;
    TextView roomTitle,roomDescription,roomPeopleCount,roomTaskCount;
    ImageButton backButton,addTaskButton,optionButton;
    LinearLayout cardBackground;


    // Helpers
    DatabaseHelper databaseHelper;

    // Datasets
    ArrayList<Task> taskData;
    ArrayList<User> usersData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room);

        Room room = (Room) getIntent().getExtras().get("room");
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

        // Setting up values
        roomTitle.setText(room.getTitle());
        roomDescription.setText(room.getDescription());
        roomTaskCount.setText(String.valueOf(room.getTasksCount()));
        roomPeopleCount.setText(String.valueOf(room.getPeopleCount()));
        cardBackground.setBackgroundColor(Color.parseColor(Helpers.Colors.addOpacity(room.getColor(),"AD")));

        // helpers
        databaseHelper = new DatabaseHelper();
        databaseHelper.setRoom(room);
        databaseHelper.setOnDataLoadedListener(new DatabaseHelper.OnDataLoadedListener() {
            @Override
            public void onDataLoaded(ArrayList<Room> roomData, ArrayList<Task> taskData, ArrayList<User> userData) {
                RoomActivity.this.taskData = taskData;
                RoomActivity.this.usersData = userData;

                roomsRecycler.setAdapter(new HomeActivityTaskAdapter(RoomActivity.this,taskData));
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
                RoomOptionDialog dialog = new RoomOptionDialog(RoomActivity.this,RoomActivity.this.getSupportFragmentManager());
                dialog.setRoom(room);
                dialog.show(RoomActivity.this.getSupportFragmentManager(),"Room Option Dialog");
            }
        });

        addTaskButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AddTaskDialog dialog = new AddTaskDialog(RoomActivity.this,RoomActivity.this.getSupportFragmentManager());
                dialog.show(RoomActivity.this.getSupportFragmentManager(),"Add Task");
            }
        });
    }
}