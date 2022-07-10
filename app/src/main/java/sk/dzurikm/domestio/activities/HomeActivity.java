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
import sk.dzurikm.domestio.adapters.HomeActivityTaskAdapter;
import sk.dzurikm.domestio.models.Room;
import sk.dzurikm.domestio.models.Task;

public class HomeActivity extends AppCompatActivity {
    RecyclerView horizontalRoomSlider,verticalTaskSlider;

    HomeActivityRoomAdapter roomAdapter;
    HomeActivityTaskAdapter taskAdapter;

    List<Room> roomData;
    List<Task> taskData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        horizontalRoomSlider = findViewById(R.id.horizontalRoomSlider);
        verticalTaskSlider = findViewById(R.id.verticalTaskSlider);

        SnapHelper snapHelper = new PagerSnapHelper();

        roomData = new LinkedList<Room>();
        roomData.add(new Room(1L,"Family","Lorem ipsum dolor sit amet",5,10));
        roomData.add(new Room(1L,"Home","Lorem ipsum dolor sit amet",2,4));

        taskData = new LinkedList<Task>();
        taskData.add(new Task("Change car oil","Oil was not changed since winter, so it would be appropriate to do so. It also smells when put in reverse","8:00 PM","Roommate","Home"));
        taskData.add(new Task("Wash the dishes",null,"4:00 AM","Mum","Family"));

        roomAdapter = new HomeActivityRoomAdapter(HomeActivity.this,roomData);
        taskAdapter = new HomeActivityTaskAdapter(HomeActivity.this,taskData);

        horizontalRoomSlider.setAdapter(roomAdapter);
        snapHelper.attachToRecyclerView(horizontalRoomSlider);

        verticalTaskSlider.setAdapter(taskAdapter);
    }
}