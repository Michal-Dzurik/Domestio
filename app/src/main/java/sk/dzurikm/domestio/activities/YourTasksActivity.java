package sk.dzurikm.domestio.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import java.util.List;

import sk.dzurikm.domestio.R;
import sk.dzurikm.domestio.adapters.HomeActivityRoomAdapter;
import sk.dzurikm.domestio.adapters.HomeActivityTaskAdapter;
import sk.dzurikm.domestio.models.Room;
import sk.dzurikm.domestio.models.Task;

public class YourTasksActivity extends AppCompatActivity {

    List<Task> taskData;
    HomeActivityTaskAdapter adapter;
    RecyclerView recyclerView;
    ImageButton backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_your_tasks);

        Bundle extras = getIntent().getExtras();
        taskData = (List<Task>) extras.get("tasks");

        recyclerView = findViewById(R.id.tasksRecycler);
        backButton = findViewById(R.id.backButton);

        adapter = new HomeActivityTaskAdapter(YourTasksActivity.this, taskData);
        recyclerView.setAdapter(adapter);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}