package sk.dzurikm.domestio.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.List;

import sk.dzurikm.domestio.R;
import sk.dzurikm.domestio.adapters.HomeActivityRoomAdapter;
import sk.dzurikm.domestio.helpers.Constants;
import sk.dzurikm.domestio.models.Room;

public class YourRoomsActivity extends AppCompatActivity {

    // Views
    RecyclerView recyclerView;
    ImageButton backButton;
    TextView noRoomsText;

    // Datasets
    List<Room> roomData;

    // Adapters
    HomeActivityRoomAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_your_rooms);

        // Views
        recyclerView = findViewById(R.id.roomRecycler);
        backButton = findViewById(R.id.backButton);
        noRoomsText = findViewById(R.id.noRoomsText);

        // Getting some data from extra bundle
        Bundle extras = getIntent().getExtras();
        roomData = (List<Room>) extras.get(Constants.Firebase.Bundle.ROOMS);

        // Setting up adapters
        if (roomData.isEmpty()) noRoomsText.setVisibility(View.VISIBLE);
        adapter = new HomeActivityRoomAdapter(YourRoomsActivity.this, roomData, new HomeActivityRoomAdapter.OnRoomLeaveListener() {
            @Override
            public void onRoomLeave(Room room) {
                // TODO need to program if need to use
            }
        });
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