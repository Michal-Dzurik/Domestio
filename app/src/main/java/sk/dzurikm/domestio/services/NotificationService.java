package sk.dzurikm.domestio.services;

import static com.google.firebase.firestore.DocumentChange.Type.ADDED;
import static com.google.firebase.firestore.DocumentChange.Type.MODIFIED;
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

        Log.i("Notification timestamp", String.valueOf(now.getSeconds()));

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
        Query notificationQuery = db.collection(DOCUMENT_NOTIFICATIONS).whereEqualTo(Notifications.FIELD_RECEIVER_ID,auth.getUid());

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

        notificationQuery.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                // On task change
                if (error != null)
                    Log.w("DB", "ERROR : ", error);

                if (value != null && !value.isEmpty()) {
                    for (DocumentChange doc : value.getDocumentChanges()) {
                        Log.w("DB", doc.getType().name() + " : " + doc.getDocument().getData());
                        DocumentChange.Type type = doc.getType();
                        if (type.equals(ADDED)) {
                            processData(DOCUMENT_NOTIFICATIONS, (HashMap<String, Object>) doc.getDocument().getData(),doc.getDocument().getId(),doc.getType());
                        }


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
                    user.setId(documentID);
                    user.cast(data);
                    usersData.remove(user);
                    break;
            }

            sendChangedData(COLLECTION, data, documentID, type);
        }
        else if (isNew(data)){
                // Notifications it self
                // send notification

                // TODO pridaj notifikacie
                switch (COLLECTION){
                    case DOCUMENT_ROOMS:
                        Room room = new Room();
                        room.cast(documentID,data);

                        if (type == ADDED){
                            roomData.add(room);
                        }
                        else if (type == MODIFIED){
                            Helpers.DataSet.updateRoom(roomData,room.getId(),room);
                        }

                        break;
                    case DOCUMENT_TASKS:
                        Task task = new Task();
                        task.cast(documentID,data);
                        task.setAuthor(Helpers.DataSet.getAuthorName(DataStorage.users,task.getAuthorId()));

                        if (type == ADDED){
                            taskData.add(task);
                        }
                        else if (type == MODIFIED){
                            Helpers.DataSet.updateTask(taskData,task.getId(),task);
                        }



                        break;
                    case DOCUMENT_USERS:
                        User user = new User();
                        user.setId(documentID);
                        user.cast(data);

                        if (type == ADDED){
                            usersData.add(user);
                        }
                        else if (type == MODIFIED){
                            Helpers.DataSet.updateUser(usersData,user.getId(),user);
                        }

                        // no need for notification , we don't wanna notify users when another is registered or something

                        break;
                    case DOCUMENT_NOTIFICATIONS:
                        resolveNotification(documentID,data);
                        break;
                }

                sendChangedData(COLLECTION,data,documentID,type);

        }
        else {
            // Data loaded innitialy
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
                    user.setId(documentID);
                    user.cast(data);

                    usersData.add(user);
                    break;
                case DOCUMENT_NOTIFICATIONS:
                    resolveNotification(documentID,data);

                    break;
            }
        }

    }

    private void resolveNotification(String id,HashMap<String, Object> data){
        Log.i("DATA NOTIFICATION", String.valueOf(data));

        String from,to;
        to = (String) data.get(Notifications.FIELD_RECEIVER_ID);

        if (!to.equals(auth.getUid())) return;

        Long action = (Long) data.get(Notifications.FIELD_ACTION_ID);

        from = (String) data.get(Notifications.FIELD_FROM_ID);
        User userFrom = Helpers.DataSet.getUserById(usersData,from);

        Intent intent;
        PendingIntent pendingIntent;
        intent = new Intent(this, SplashScreenActivity.class);
        pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        String roomId;
        Room room;

        switch (action.intValue()){
            case Notifications.Action.USER_ADDED_TO_NEW_ROOM:
                sendNotification(Constants.NotificationChannels.NEW_JOINED_ROOM,getString(R.string.new_room), getString(R.string.new_room_for_you), pendingIntent);
                break;
            case Notifications.Action.TASK_ASSIGNED:
                sendNotification(Constants.NotificationChannels.NEW_TASKS,userFrom.getName().trim() + " " + getString(R.string.assigned_you_a_new_task), getString(R.string.you_have_new_task) + "." + getString(R.string.go_check_it_out), pendingIntent);
                break;
            case Notifications.Action.TASK_VERIFIED:
                sendNotification(Constants.NotificationChannels.TASK_UPDATES,getString(R.string.task_verified), getString(R.string.user) + " " + userFrom.getName() + " " + getString(R.string.has_verified_task), pendingIntent);
                break;
            case Notifications.Action.TASK_DONE:
                sendNotification(Constants.NotificationChannels.TASK_UPDATES,getString(R.string.task_done), getString(R.string.user) + " " + userFrom.getName() + " " + getString(R.string.has_done_task) + ".", pendingIntent);
                break;
            case Notifications.Action.TASK_REMOVED:
                sendNotification(Constants.NotificationChannels.TASK_UPDATES,getString(R.string.task_removed), getString(R.string.user) + " " + userFrom.getName() + " " + getString(R.string.has_removed_you_task), pendingIntent);
                break;
            case Notifications.Action.USER_LEFT_THE_ROOM:
                roomId = (String) data.get("room_id");
                room = Helpers.DataSet.getRoomById(roomData,roomId);
                sendNotification(Constants.NotificationChannels.ROOM_UPDATES,getString(R.string.user_left_the_room), getString(R.string.user) + " " + userFrom.getName() + " " + getString(R.string.has_left_room) + " " + room.getTitle() + " .", pendingIntent);
                break;
            case Notifications.Action.USER_REMOVED_FROM_ROOM:
                String roomTitle = (String) data.get("room_title");
                sendNotification(Constants.NotificationChannels.ROOM_UPDATES,getString(R.string.room_unavailable), getString(R.string.user) + " " + userFrom.getName() + " " + getString(R.string.removed_you_from_room) + " " + roomTitle + " .", pendingIntent);
                break;


        }

        notificationId++;
        db.collection(DOCUMENT_NOTIFICATIONS).document(id).delete();
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

    public Task getTask(Task task){
        for (int i = 0; i < DataStorage.tasks.size(); i++) {
            if (DataStorage.tasks.get(i).getId().equals(task.getId())) return DataStorage.tasks.get(i);
        }

        return null;
    }
}
