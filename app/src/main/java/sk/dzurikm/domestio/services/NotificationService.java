package sk.dzurikm.domestio.services;

import static com.google.firebase.firestore.DocumentChange.Type.REMOVED;
import static sk.dzurikm.domestio.helpers.Constants.Firebase.*;
import static sk.dzurikm.domestio.helpers.Constants.Firebase.Room.FIELD_MODIFIED_AT;
import static sk.dzurikm.domestio.helpers.Constants.Firebase.Room.FIELD_USER_IDS;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import sk.dzurikm.domestio.R;
import sk.dzurikm.domestio.activities.SplashScreenActivity;
import sk.dzurikm.domestio.helpers.Constants;
import sk.dzurikm.domestio.helpers.DataStorage;
import sk.dzurikm.domestio.helpers.Helpers;
import sk.dzurikm.domestio.models.Room;
import sk.dzurikm.domestio.models.Task;
import sk.dzurikm.domestio.models.User;

public class NotificationService extends Service {

    // Database
    FirebaseFirestore db;
    FirebaseAuth auth;
    Timestamp now ;

    Context context;

    ArrayList<Room> roomData;
    ArrayList<Task> taskData;
    ArrayList<User> usersData;


    int notificationId = 1;

    @Override
    public void onCreate() {
        // Database
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        startListeners();
        super.onCreate();

        context = getApplicationContext();
        now = new Timestamp(new Date());

        roomData = new ArrayList<>();
        usersData = new ArrayList<>();
        taskData = new ArrayList<>();

        if (Helpers.Network.isNetworkAvailable(context))
            DataStorage.connected = true;
        else
            DataStorage.connected = false;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public void startListeners(){
        // Declaring documents
        Timestamp timestamp = new Timestamp(new Date());
        Query taskQuery = db.collection(DOCUMENT_TASKS).whereEqualTo(Constants.Firebase.Task.FIELD_RECEIVER_ID,auth.getCurrentUser().getUid());
        Query roomQuery = db.collection(DOCUMENT_ROOMS).whereArrayContains(FIELD_USER_IDS,auth.getCurrentUser().getUid());
        Query userQuery = db.collection(DOCUMENT_USERS);

        taskQuery.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                // On task change
                if (error != null)
                    Log.w("DB", "ERROR : ", error);

                if (value != null && !value.isEmpty()) {
                    for (DocumentChange doc : value.getDocumentChanges()) {
                        Log.w("DB", doc.getType().name() + " : " + doc.getDocument().getData());
                        processData(DOCUMENT_TASKS, (HashMap<String, Object>) doc.getDocument().getData(),doc.getDocument().getId(),doc.getType());

                    }
                }
            }
        });

        roomQuery.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                // On room change
                if (error != null)
                    Log.w("DB", "ERROR : ", error);

                if (value != null && !value.isEmpty()) {
                    for (DocumentChange doc : value.getDocumentChanges()) {
                        Log.w("DB", doc.getType().name() + " : " + doc.getDocument().getData());
                        processData(DOCUMENT_ROOMS, (HashMap<String, Object>) doc.getDocument().getData(),doc.getDocument().getId(),doc.getType());
                    }
                }
            }
        });

        userQuery.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                // On user change
                if (error != null)
                    Log.w("DB", "ERROR : ", error);

                if (value != null && !value.isEmpty()) {
                    for (DocumentChange doc : value.getDocumentChanges()) {
                        Log.w("DB", doc.getType().name() + " : " + doc.getDocument().getData());
                        processData(DOCUMENT_USERS, (HashMap<String, Object>) doc.getDocument().getData(),doc.getDocument().getId(),doc.getType());
                    }
                }
            }
        });


    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void sendChangedData(String COLLECTION, HashMap<String,Object> data,String documentID, DocumentChange.Type type){
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction("DATA_CHANGED");
        broadcastIntent.putExtra("type",type);
        broadcastIntent.putExtra("data", data);
        broadcastIntent.putExtra("documentID", documentID);
        broadcastIntent.putExtra("collection",COLLECTION);

        sendBroadcast(broadcastIntent);
    }

    private boolean isNew(Map<String,Object> data){
        Timestamp ts = (Timestamp) data.get(FIELD_MODIFIED_AT);
        if (ts == null) return false;

        if (ts.getSeconds() >= now.getSeconds()){
            return true;
        }

        return false;
    }

    private void processData(String COLLECTION, HashMap<String,Object> data,String documentID, DocumentChange.Type type){
        if ( type == REMOVED) {
            sendChangedData(COLLECTION, data, documentID, type);

            switch (COLLECTION) {
                case DOCUMENT_ROOMS:
                    Room room = new Room();
                    room.cast(documentID, data);
                    roomData.remove(room);
                    break;
                case DOCUMENT_TASKS:
                    Task task = new Task();
                    task.cast(documentID,data);
                    taskData.remove(task);
                    break;
                case DOCUMENT_USERS:
                    User user = new User();
                    user.cast(data);
                    usersData.remove(user);
                    break;
            }
        }

        else if (isNew(data)){
                // Notifications it self
                // send notification
                Intent intent;
                PendingIntent pendingIntent;
                sendChangedData(COLLECTION,data,documentID,type);

                switch (COLLECTION){
                    case DOCUMENT_ROOMS:
                        Room room = new Room();
                        room.cast(documentID,data);

                        intent = new Intent(this, SplashScreenActivity.class);
                        pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);

                        if (type == REMOVED) {

                            break;
                        }


                        if (isRoomNew(room) ) {
                            roomData.add(room);
                            // If i haven't created room then notify me
                            if(!room.getAdminId().equals(auth.getCurrentUser().getUid())) {
                                sendNotification(Constants.NotificationChannels.NEW_JOINED_ROOM,getString(R.string.new_room), getString(R.string.your_are_member_of_room) + " " + room.getTitle() + ". " + getString(R.string.go_check_it_out), pendingIntent);
                                notificationId++;
                            }
                        }
                        else {


                            // If i haven't changed room then notify me
                            if(!room.getAdminId().equals(auth.getCurrentUser().getUid())) {
                                sendNotification(Constants.NotificationChannels.ROOM_UPDATES,room.getTitle(), getString(R.string.something_new_in_room) + ". " + getString(R.string.go_check_it_out), pendingIntent);
                                notificationId++;
                            }
                        }
                        break;
                    case DOCUMENT_TASKS:
                        Task task = new Task();
                        task.cast(documentID,data);

                        intent = new Intent(this, SplashScreenActivity.class);
                        pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);

                        task.setAuthor(Helpers.DataSet.getAuthorName(DataStorage.users,task.getAuthorId()));

                        if (isTaskNew(task) ) {
                            taskData.add(task);

                            // If i haven't created task then notify me
                            if(!task.getAuthorId().equals(auth.getCurrentUser().getUid())) {
                                sendNotification(Constants.NotificationChannels.NEW_TASKS,task.getAuthor() + " " + getString(R.string.assigned_you_a_new_task), getString(R.string.you_have_new_task) + " " + task.getHeading() + ". " + getString(R.string.go_check_it_out), pendingIntent);
                                notificationId++;
                            }
                        }
                        else {
                            if (type == REMOVED) break;

                            // If i haven't changed task then notify me
                            if(!task.getAuthorId().equals(auth.getCurrentUser().getUid())) {
                                sendNotification(Constants.NotificationChannels.TASK_UPDATES,getString(R.string.task_modified), getString(R.string.something_new_in_task) + " " + task.getHeading() + ". " + getString(R.string.go_check_it_out), pendingIntent);
                                notificationId++;
                            }
                        }
                        break;
                    case DOCUMENT_USERS:
                        User user = new User();
                        user.cast(data);

                        // no need for notification , we don't wanna notify users when another is registered or something

                        break;
                }

        }
        else {
            // Data distribution to app
            switch (COLLECTION){
                case DOCUMENT_ROOMS:
                    Room room = new Room();
                    room.cast(documentID,data);

                    roomData.add(room);

                    break;
                case DOCUMENT_TASKS:
                    Task task = new Task();
                    task.cast(documentID,data);

                    taskData.add(task);
                    break;
                case DOCUMENT_USERS:
                    User user = new User();
                    user.cast(data);

                    usersData.add(user);
                    break;
            }
        }

    }

    private void sendNotification(String channel,String text, String content, PendingIntent pendingIntent){
        Helpers.Notifications.show(getApplicationContext(),notificationId,Helpers.Notifications.createBasicNotification(getApplicationContext(), channel,text,content,pendingIntent));
    }


    public boolean isRoomNew(Room room){
        for (int i = 0; i < roomData.size(); i++) {
            if (roomData.get(i).getId().equals(room.getId())) return false;
        }

        return true;
    }

    public boolean isTaskNew(Task task){
        for (int i = 0; i < taskData.size(); i++) {
            if (taskData.get(i).getId().equals(task.getId())) return false;
        }

        return true;
    }
}
