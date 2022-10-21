package sk.dzurikm.domestio.activities;

import androidx.annotation.NonNull;
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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

import sk.dzurikm.domestio.R;
import sk.dzurikm.domestio.adapters.HomeActivityRoomAdapter;
import sk.dzurikm.domestio.adapters.HomeActivityTaskAdapter;
import sk.dzurikm.domestio.helpers.Helpers;
import sk.dzurikm.domestio.helpers.MultipleDone;
import sk.dzurikm.domestio.helpers.listeners.OnDoneListener;
import sk.dzurikm.domestio.models.Room;
import sk.dzurikm.domestio.models.Task;
import sk.dzurikm.domestio.models.User;
import sk.dzurikm.domestio.services.NotificationService;
import sk.dzurikm.domestio.views.dialogs.MenuDialog;

public class HomeActivity extends AppCompatActivity {
    private final String DOCUMENT_ROOMS = "Rooms";
    private final String DOCUMENT_TASKS = "Tasks";
    private final String DOCUMENT_USERS = "Users";

    RecyclerView horizontalRoomSlider,verticalTaskSlider;
    View loading;
    TextView userName;
    ImageButton menuButton, profileButton;
    TextView seeAllTasksButton,seeAllRoomsButton;

    ArrayList<String> roomsIDs,userRelatedUserIds;

    HomeActivityRoomAdapter roomAdapter;
    HomeActivityTaskAdapter taskAdapter;

    ArrayList<Room> roomData;
    ArrayList<Task> taskData;
    ArrayList<User> usersData;

    SnapHelper snapHelper;
    MultipleDone multipleDone;

    FirebaseFirestore db;
    FirebaseUser user;
    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        userName = findViewById(R.id.userName);
        userName.setText(getIntent().getStringExtra("user_name"));

        horizontalRoomSlider = findViewById(R.id.horizontalRoomSlider);
        verticalTaskSlider = findViewById(R.id.verticalTaskSlider);

        profileButton = findViewById(R.id.profileButton);
        menuButton = findViewById(R.id.menuButton);
        seeAllRoomsButton = findViewById(R.id.seeAllRoomsButton);
        seeAllTasksButton = findViewById(R.id.seeAllTasksButton);

        loading = findViewById(R.id.loading);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();


        Log.i("USER_ID",user.getUid());


        snapHelper = new PagerSnapHelper();
        multipleDone = new MultipleDone();
        multipleDone.setOnDoneListener(new OnDoneListener() {
            @Override
            public void OnDone() {
                hideLoading();
            }
        });

        roomData = new ArrayList<>();
        roomsIDs = new ArrayList();
        userRelatedUserIds = new ArrayList();

        taskData = new ArrayList<Task>();

        usersData = new ArrayList<User>();

        loadData();

        MenuDialog menuDialog = new MenuDialog(HomeActivity.this,HomeActivity.this.getSupportFragmentManager());

        // Initializing listeners
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
                yourRoomsActivityIntent.putExtra("rooms", roomData);

                startActivity(yourRoomsActivityIntent);
            }
        });

        seeAllTasksButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent yourTasksActivityIntent = new Intent(HomeActivity.this,YourTasksActivity.class );
                yourTasksActivityIntent.putExtra("tasks", taskData);

                startActivity(yourTasksActivityIntent);
            }
        });

    }

    private void loadData() {
        loadRooms();
    }


    private void hideLoading(){
        roomAdapter = new HomeActivityRoomAdapter(HomeActivity.this,roomData);
        taskAdapter = new HomeActivityTaskAdapter(HomeActivity.this,taskData);

        horizontalRoomSlider.setAdapter(roomAdapter);
        snapHelper.attachToRecyclerView(horizontalRoomSlider);
        verticalTaskSlider.setAdapter(taskAdapter);

        startService(new Intent( this, NotificationService.class ) );

        Helpers.Time.delay(new Runnable() {
            @Override
            public void run() {
                loading.startAnimation(AnimationUtils.loadAnimation(HomeActivity.this,R.anim.fade_out));
                loading.getAnimation().setFillAfter(true);
            }
        },1000);
    }

    private void loadRooms(){
        Query roomQuery = db.collection(DOCUMENT_ROOMS)
                .whereArrayContains("user_ids", user.getUid());

        roomQuery.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull com.google.android.gms.tasks.Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {

                            for (QueryDocumentSnapshot document : task.getResult()) {
                                roomData.clear();
                                Map<String, Object> data = document.getData();

                                // Room storing
                                Room room = new Room();
                                room.cast(document.getId(),data);

                                roomData.add(room);

                                // Tasks
                                String id = document.getId();
                                roomsIDs.add(id);

                                // Users
                                ArrayList<String> userIds = data.get("user_ids") == null ? new ArrayList<String>() : (ArrayList) data.get("user_ids");
                                Helpers.List.addUnique(userRelatedUserIds,userIds);


                            }

                            loadUsers();
                            System.out.println(String.valueOf(Arrays.toString(taskData.toArray())));

                        } else {
                            Log.w("DB_RESULT", "Error getting documents.", task.getException());
                        }
                    }

                });

        /*roomQuery.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null)
                    Log.w("DB", "ERROR : ", error);

                if (value != null && !value.isEmpty()) {
                    for (DocumentChange doc : value.getDocumentChanges()) {
                        switch (doc.getType()){
                            case ADDED:
                                Log.w("DB", "ADDED : " + doc.getDocument().getData());
                                break;
                            case REMOVED:
                                Log.w("DB", "REMOVED : " + doc.getDocument().getData());
                                break;
                            case MODIFIED:
                                Log.w("DB", "MODIFIED : " + doc.getDocument().getData());
                                break;
                        }
                    }
                }
            }
        });*/

    }

    private void loadUsers(){

        db.collection(DOCUMENT_USERS).get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull com.google.android.gms.tasks.Task<QuerySnapshot> response) {
                        if (response.isSuccessful()) {
                            usersData.clear();
                            for (QueryDocumentSnapshot document : response.getResult()) {

                                Map<String, Object> data = document.getData();

                                // Task storing
                                User user = new User();
                                user.cast(document.getId(),data);

                                usersData.add(user);
                                Log.i("DB_RESULT_USER", String.valueOf(user));

                            }

                            Log.i("DB_RESULT", String.valueOf(Arrays.toString(usersData.toArray())));
                            loadTasks();

                        } else {
                            Log.w("DB_RESULT", "Error getting documents.", response.getException());
                        }
                    }
                });

        /*taskQuery.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null)
                    Log.w("DB", "ERROR : ", error);

                if (value != null && !value.isEmpty()) {
                    for (DocumentChange doc : value.getDocumentChanges()) {
                        switch (doc.getType()){
                            case ADDED:
                                Log.w("DB", "ADDED : " + doc.getDocument().getData());
                                break;
                            case REMOVED:
                                Log.w("DB", "REMOVED : " + doc.getDocument().getData());
                                break;
                            case MODIFIED:
                                Log.w("DB", "MODIFIED : " + doc.getDocument().getData());
                                break;
                        }
                    }
                }
            }
        });*/
    }

    private void loadTasks(){
        Query taskQuery = db.collection(DOCUMENT_TASKS)
                .whereIn("room_id", roomsIDs)
                .whereEqualTo("receiving_user_id",user.getUid());

        System.out.println(roomsIDs);

        taskQuery.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull com.google.android.gms.tasks.Task<QuerySnapshot> response) {
                        if (response.isSuccessful()) {
                            taskData.clear();
                            for (QueryDocumentSnapshot document : response.getResult()) {

                                Map<String, Object> data = document.getData();

                                // Task storing
                                Task task = new Task();
                                task.cast(document.getId(),data);
                                task.setOwner(getUsersName(task.getOwnerId()));
                                task.setRoom(getRoomsTitle(task.getRoomId()));
                                task.setColor(getRoomsColor(task.getOwnerId()));

                                taskData.add(task);
                                Log.i("DB_RESULT_TASKS", task.toString());

                            }

                            System.out.println(String.valueOf(Arrays.toString(taskData.toArray())));
                            hideLoading();


                        } else {
                            Log.w("DB_RESULT", "Error getting documents.", response.getException());
                        }
                    }
                });

        /*taskQuery.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null)
                    Log.w("DB", "ERROR : ", error);

                if (value != null && !value.isEmpty()) {
                    for (DocumentChange doc : value.getDocumentChanges()) {
                        switch (doc.getType()){
                            case ADDED:
                                Log.w("DB", "ADDED : " + doc.getDocument().getData());
                                break;
                            case REMOVED:
                                Log.w("DB", "REMOVED : " + doc.getDocument().getData());
                                break;
                            case MODIFIED:
                                Log.w("DB", "MODIFIED : " + doc.getDocument().getData());
                                break;
                        }
                    }
                }
            }
        });*/
    }

    private String getUsersName(String id){
        for (int i = 0; i < usersData.size(); i++) {
            User user = usersData.get(i);
            if (user.getId().equals(id)) return user.getName();
        }

        return "";
    }

    private String getRoomsTitle(String id){
        for (int i = 0; i < roomData.size(); i++) {
            Room room = roomData.get(i);
            if (room.getId().equals(id)) return room.getTitle();
        }

        return "";
    }

    private String getRoomsColor(String id){
        for (int i = 0; i < roomData.size(); i++) {
            Room room = roomData.get(i);
            if (room.getId().equals(id)) return room.getColor();
        }

        return "";
    }

    @Override
    protected void onStart() {
        overridePendingTransition(0,0);
        super.onStart();

    }
}