package sk.dzurikm.domestio.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import sk.dzurikm.domestio.R;
import sk.dzurikm.domestio.adapters.HomeActivityTaskAdapter;
import sk.dzurikm.domestio.helpers.DCO;
import sk.dzurikm.domestio.helpers.DataStorage;
import sk.dzurikm.domestio.helpers.DatabaseHelper;
import sk.dzurikm.domestio.models.Room;
import sk.dzurikm.domestio.models.Task;
import sk.dzurikm.domestio.models.User;

public class YourTasksActivity extends AppCompatActivity {

    // Views
    RecyclerView recyclerView;
    ImageButton backButton;
    LinearLayout noTasksText;

    // Datasets
    List<Task> taskData;

    // Adapters
    HomeActivityTaskAdapter adapter;

    // Database
    DatabaseHelper databaseHelper;

    // DCO
    DCO dco;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_your_tasks);

        // Views
        recyclerView = findViewById(R.id.tasksRecycler);
        backButton = findViewById(R.id.backButton);
        noTasksText = findViewById(R.id.noTasksText);

        // Database
        databaseHelper = new DatabaseHelper();

        // DCO
        dco = new DCO(new DCO.OnDataChangeListener() {
            @Override
            public void onChange(ArrayList<User> usersData, ArrayList<Room> roomData, ArrayList<Task> taskData) {
                if (roomData != null) {
                    DataStorage.rooms = roomData;
                }
            }
        });

        databaseHelper.loadTasksCreatedByMe(new DatabaseHelper.TasksForRoomLoadedListener() {
            @Override
            public void onTasksLoaded(ArrayList<Task> data) {
                taskData = data;

                // Setting up adapters
                refreshNoDataTexts();
                adapter = new HomeActivityTaskAdapter(YourTasksActivity.this, taskData,null,getSupportFragmentManager()){
                    @Override
                    public void refresh(){
                        adapter.notifyDataSetChanged();
                        refreshNoDataTexts();
                    }
                };
                recyclerView.setAdapter(adapter);
            }
        });




        // Setting up listeners
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }

    public void taskChanged(Task task){
        for (int i = 0; i < taskData.size(); i++) {
            if (taskData.get(i).getId().equals(task.getId())){
                taskData.set(i,task);
                adapter.notifyItemChanged(i);
                refreshNoDataTexts();
                break;
            }
        }
    }


    public void taskRemoved(Task task){
        for (int i = 0; i < taskData.size(); i++) {
            if (taskData.get(i).getId().equals(task.getId())){
                taskData.remove(i);
                adapter.notifyItemRemoved(i);
                removeIdFromTaskIdList(task);
                refreshNoDataTexts();
                break;
            }
        }
    }

    public void removeIdFromTaskIdList(Task task){
        if (DataStorage.rooms == null) return;
        for (int i = 0; i < DataStorage.rooms.size(); i++) {
            if (DataStorage.rooms.get(i).getId().equals(task.getRoomId())){
                DataStorage.rooms.get(i).removeTaskId(task.getId());
            }
        }
    }

    public void refreshNoDataTexts(){
        if (taskData.isEmpty()) noTasksText.setVisibility(View.VISIBLE);
    }
}