package sk.dzurikm.domestio.activities;

import static sk.dzurikm.domestio.helpers.Constants.Url.*;

import androidx.appcompat.app.AppCompatActivity;
import androidx.browser.customtabs.CustomTabsIntent;

import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.google.firebase.firestore.DocumentChange;

import java.util.ArrayList;
import java.util.HashMap;

import sk.dzurikm.domestio.R;
import sk.dzurikm.domestio.helpers.broadcasts.DataChangedReceiver;
import sk.dzurikm.domestio.helpers.broadcasts.NetworkChangeReceiver;
import sk.dzurikm.domestio.helpers.Constants;
import sk.dzurikm.domestio.helpers.DCO;
import sk.dzurikm.domestio.helpers.DataStorage;
import sk.dzurikm.domestio.helpers.Helpers;
import sk.dzurikm.domestio.models.Room;
import sk.dzurikm.domestio.models.Task;
import sk.dzurikm.domestio.models.User;

public class AboutMeActivity extends AppCompatActivity {
    // Views
    private ImageButton facebook,linkedin,github,backButton;
    private LinearLayout supportMe;

    // DCO
    DCO dco;

    // Broadcasts
    DataChangedReceiver dataChangedReceiver;
    NetworkChangeReceiver networkChangeReceiver;
    IntentFilter dataChangeBroadcastFilter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_me);

        // Views
        facebook = findViewById(R.id.facebook);
        linkedin = findViewById(R.id.linkedin);
        github = findViewById(R.id.github);
        backButton = findViewById(R.id.backButton);
        supportMe = findViewById(R.id.supportMeButton);

        CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
        CustomTabsIntent customTabsIntent = builder.build();

        // Helpers
        dco = new DCO(new DCO.OnDataChangeListener() {
            @Override
            public void onChange(ArrayList<User> usersData, ArrayList<Room> roomData, ArrayList<Task> taskData) {
                // Don't need to change anything in this activity so it is blank
            }
        });

        // Broadcasts
        dataChangedReceiver = new DataChangedReceiver(new DataChangedReceiver.DataChangedListener() {
            @Override
            public void onDataChanged(HashMap<String, Object> data, String collection, String documentID, DocumentChange.Type type) {

                switch (collection){
                    case Constants.Firebase.DOCUMENT_ROOMS:
                        Room room = new Room();
                        room.cast(documentID,data);

                        switch (type){
                            case ADDED:
                                dco.addRoom(room);
                                break;
                            case MODIFIED:
                                dco.updateRoomChangeableInfo(room);
                                break;
                            case REMOVED:

                                break;
                        }

                        break;

                    case Constants.Firebase.DOCUMENT_TASKS:
                        Task task = new Task();
                        task.cast(documentID,data);

                        task.setAuthor(Helpers.DataSet.getAuthorName(DataStorage.users,task.getAuthorId()));

                        HashMap<String,String> dat = Helpers.DataSet.getRoomInfo(DataStorage.rooms,task.getRoomId(), new String[]{Constants.Firebase.Room.FIELD_TITLE, Constants.Firebase.Room.FIELD_COLOR});
                        task.setRoomName(dat.get(Constants.Firebase.Room.FIELD_TITLE));
                        task.setColor(dat.get(Constants.Firebase.Room.FIELD_COLOR));


                        switch (type){
                            case ADDED:
                                dco.addTask(task);
                                break;
                            case MODIFIED:
                                dco.updateTask(task);
                                break;
                            case REMOVED:
                                dco.removeTask(task);
                                break;
                        }

                        break;

                    case Constants.Firebase.DOCUMENT_USERS:
                        User user = new User();
                        user.setId(documentID);
                        user.cast(data);

                        switch (type){
                            case ADDED:
                                dco.addUser(user);
                                break;
                            case MODIFIED:
                                dco.updatedUser(user);
                                break;
                        }

                        break;
                }
            }

        });
        IntentFilter intentSFilter = new IntentFilter("DATA_CHANGED");
        registerReceiver(dataChangedReceiver, intentSFilter);

        // Setting up listeners
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        facebook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                customTabsIntent.launchUrl(AboutMeActivity.this, Uri.parse(FACEBOOK));
            }
        });

        linkedin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                customTabsIntent.launchUrl(AboutMeActivity.this, Uri.parse(LINKED_ID));
            }
        });

        github.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                customTabsIntent.launchUrl(AboutMeActivity.this, Uri.parse(GIT_HUB));
            }
        });

        supportMe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                customTabsIntent.launchUrl(AboutMeActivity.this, Uri.parse(BUY_ME_A_COFFEE));
            }
        });
    }

    @Override
    public void finish() {
        unregisterReceiver(dataChangedReceiver);
        super.finish();
    }
}