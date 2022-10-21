package sk.dzurikm.domestio.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import java.util.List;

import sk.dzurikm.domestio.R;
import sk.dzurikm.domestio.adapters.HomeActivityRoomAdapter;
import sk.dzurikm.domestio.models.Room;

public class YourRoomsActivity extends AppCompatActivity {

    List<Room> roomData;
    HomeActivityRoomAdapter adapter;
    RecyclerView recyclerView;
    ImageButton backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_your_rooms);

        Bundle extras = getIntent().getExtras();
        roomData = (List<Room>) extras.get("rooms");

        recyclerView = findViewById(R.id.roomRecycler);
        backButton = findViewById(R.id.backButton);

        adapter = new HomeActivityRoomAdapter(YourRoomsActivity.this,roomData);
        recyclerView.setAdapter(adapter);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}