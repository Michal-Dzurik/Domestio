package sk.dzurikm.domestio.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;

import android.os.Bundle;

import java.util.LinkedList;
import java.util.List;

import sk.dzurikm.domestio.R;
import sk.dzurikm.domestio.adapters.HomeActivityRoomAdapter;
import sk.dzurikm.domestio.models.Room;

public class HomeActivity extends AppCompatActivity {
    RecyclerView horizontalRoomSlider;

    HomeActivityRoomAdapter roomAdapter;

    List<Room> roomData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        horizontalRoomSlider = findViewById(R.id.horizontalRoomSlider);

        SnapHelper snapHelper = new PagerSnapHelper();

        roomData = new LinkedList<Room>();
        roomData.add(new Room(1L,"Family","Lorem ipsum dolor sit amet",5,10));
        roomData.add(new Room(1L,"Roommate","Lorem ipsum dolor sit amet",2,4));

        roomAdapter = new HomeActivityRoomAdapter(HomeActivity.this,roomData);

        horizontalRoomSlider.setAdapter(roomAdapter);
        snapHelper.attachToRecyclerView(horizontalRoomSlider);
    }
}