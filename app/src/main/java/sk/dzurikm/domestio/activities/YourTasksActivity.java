package sk.dzurikm.domestio.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.firebase.firestore.DocumentChange;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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

public class YourTasksActivity extends AppCompatActivity {

    // Views
    RecyclerView recyclerView;
    ImageButton backButton;
    TextView noTasksText;

    // Datasets
    List<Task> taskData;

    // Adapters
    HomeActivityTaskAdapter adapter;

    // Database
    DatabaseHelper databaseHelper;


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

        databaseHelper.loadTasksCreatedByMe(new DatabaseHelper.TasksForRoomLoadedListener() {
            @Override
            public void onTasksLoaded(ArrayList<Task> data) {
                taskData = data;

                System.out.println(data);

                // Setting up adapters
                if (taskData.isEmpty()) noTasksText.setVisibility(View.VISIBLE);
                adapter = new HomeActivityTaskAdapter(YourTasksActivity.this, taskData,getSupportFragmentManager());
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
                adapter.notifyDataSetChanged();
                if (taskData.isEmpty()) noTasksText.setVisibility(View.VISIBLE);
                break;
            }
        }
    }

    public void taskRemoved(Task task){
        for (int i = 0; i < taskData.size(); i++) {
            if (taskData.get(i).getId().equals(task.getId())){
                taskData.remove(i);
                adapter.notifyDataSetChanged();
                if (taskData.isEmpty()) noTasksText.setVisibility(View.VISIBLE);
                break;
            }
        }
    }
}