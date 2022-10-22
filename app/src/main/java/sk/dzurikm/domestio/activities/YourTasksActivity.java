package sk.dzurikm.domestio.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import java.util.List;

import sk.dzurikm.domestio.R;
import sk.dzurikm.domestio.adapters.HomeActivityTaskAdapter;
import sk.dzurikm.domestio.helpers.Constants;
import sk.dzurikm.domestio.models.Task;

public class YourTasksActivity extends AppCompatActivity {

    // Views
    RecyclerView recyclerView;
    ImageButton backButton;

    // Datasets
    List<Task> taskData;

    // Adapters
    HomeActivityTaskAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_your_tasks);

        // Views
        recyclerView = findViewById(R.id.tasksRecycler);
        backButton = findViewById(R.id.backButton);

        // Getting some data from extra bundle
        Bundle extras = getIntent().getExtras();
        taskData = (List<Task>) extras.get(Constants.Firebase.Bundle.TASKS);

        // Setting up adapters
        adapter = new HomeActivityTaskAdapter(YourTasksActivity.this, taskData);
        recyclerView.setAdapter(adapter);


        // Setting up listeners
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}