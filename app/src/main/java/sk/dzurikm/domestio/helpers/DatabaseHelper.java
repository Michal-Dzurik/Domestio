package sk.dzurikm.domestio.helpers;

import static sk.dzurikm.domestio.helpers.Constants.Firebase.DOCUMENT_ROOMS;
import static sk.dzurikm.domestio.helpers.Constants.Firebase.DOCUMENT_TASKS;
import static sk.dzurikm.domestio.helpers.Constants.Firebase.DOCUMENT_USERS;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

import sk.dzurikm.domestio.models.Room;
import sk.dzurikm.domestio.models.Task;
import sk.dzurikm.domestio.models.User;

public class DatabaseHelper {
    FirebaseFirestore db;
    FirebaseUser user;
    FirebaseAuth auth;

    // Additional variables
    ArrayList<String> roomsIDs,userRelatedUserIds;

    // Datasets
    ArrayList<Room> roomData;
    ArrayList<Task> taskData;
    ArrayList<User> usersData;

    Room room;

    // Listeners
    OnDataLoadedListener onDataLoadedListener;

    public DatabaseHelper() {

        roomData = new ArrayList<>();
        roomsIDs = new ArrayList();
        userRelatedUserIds = new ArrayList();
        taskData = new ArrayList<Task>();
        usersData = new ArrayList<User>();

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
    }

    public void loadRooms(int TYPE){
        // DB query

        switch (TYPE){
            case Constants.Firebase.DATA_FOR_USER:
                Query roomQuery = db.collection(DOCUMENT_ROOMS)
                        .whereArrayContains(Constants.Firebase.Room.FIELD_USER_IDS, user.getUid());
                roomQuery.get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull com.google.android.gms.tasks.Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {

                                    for (QueryDocumentSnapshot document : task.getResult()) {
                                        roomData.clear();
                                        Map<String, Object> data = document.getData();

                                        // Creating room and casting db results as data for model
                                        Room room = new Room();
                                        room.cast(document.getId(),data);

                                        // Adding room to the dataset
                                        roomData.add(room);

                                        // Adding room id to arraylist
                                        roomsIDs.add(document.getId());

                                        // Adding User ids to arraylist
                                        ArrayList<String> userIds = data.get(Constants.Firebase.Room.FIELD_USER_IDS) == null ? new ArrayList<String>() : (ArrayList) data.get(Constants.Firebase.Room.FIELD_USER_IDS);
                                        Helpers.List.addUnique(userRelatedUserIds,userIds);


                                    }

                                    loadUsers(TYPE);
                                    Log.i("Firebase result", String.valueOf(Arrays.toString(taskData.toArray())));

                                } else {
                                    Log.w("DB_RESULT", "Error getting documents.", task.getException());
                                }
                            }

                        });
                break;
            case Constants.Firebase.DATA_FOR_ROOM:
                loadUsers(TYPE);
                break;

        }


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

    public void loadUsers(int TYPE){
        db.collection(DOCUMENT_USERS).get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull com.google.android.gms.tasks.Task<QuerySnapshot> response) {
                        if (response.isSuccessful()) {
                            usersData.clear();
                            for (QueryDocumentSnapshot document : response.getResult()) {

                                Map<String, Object> data = document.getData();

                                // Creating task and casting from DB result to model
                                User user = new User();
                                user.cast(document.getId(),data);

                                // Adding user to it's dataset
                                usersData.add(user);
                                Log.i("Firebase result", String.valueOf(user));

                            }

                            Log.i("DB_RESULT", String.valueOf(Arrays.toString(usersData.toArray())));
                            loadTasks(TYPE);

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

    public void loadTasks(int TYPE){
        Query taskQuery;


        if (TYPE == Constants.Firebase.DATA_FOR_USER){
            taskQuery = db.collection(DOCUMENT_TASKS)
                    .whereIn(Constants.Firebase.Task.FIELD_ROOM_ID, roomsIDs);
            taskQuery.whereEqualTo(Constants.Firebase.Task.FIELD_RECEIVER_ID,user.getUid());
        }
        else{
            taskQuery = db.collection(DOCUMENT_TASKS)
                    .whereEqualTo(Constants.Firebase.Task.FIELD_ROOM_ID, room.getId());
        }

        taskQuery.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull com.google.android.gms.tasks.Task<QuerySnapshot> response) {
                        if (response.isSuccessful()) {
                            taskData.clear();
                            for (QueryDocumentSnapshot document : response.getResult()) {

                                Map<String, Object> data = document.getData();

                                // Creating task and casting from db result to model
                                Task task = new Task();
                                task.cast(document.getId(),data);
                                task.setOwner(getUsersName(task.getOwnerId()));
                                task.setRoom(getRoomsTitle(task.getRoomId()));
                                task.setColor(getRoomsColor(task.getOwnerId()));

                                // Adding task to it's dataset
                                taskData.add(task);
                            }

                            onDataLoadedListener.onDataLoaded(roomData,taskData,usersData);

                            Log.i("Firebase result",String.valueOf(Arrays.toString(taskData.toArray())));

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

    /**
     *
     * @param id user UID
     * @return id name of user with  id
     */
    private String getUsersName(String id){
        for (int i = 0; i < usersData.size(); i++) {
            User user = usersData.get(i);
            if (user.getId().equals(id)) return user.getName();
        }

        return "";
    }

    /**
     * @param id user UID
     * @return title of room wit id
     */
    private String getRoomsTitle(String id){
        for (int i = 0; i < roomData.size(); i++) {
            Room room = roomData.get(i);
            if (room.getId().equals(id)) return room.getTitle();
        }

        return "";
    }

    /**
     * @param id room UID
     * @return color of room with id
     */
    private String getRoomsColor(String id){
        for (int i = 0; i < roomData.size(); i++) {
            Room room = roomData.get(i);
            if (room.getId().equals(id)) return room.getColor();
        }

        return "";
    }

    public void setOnDataLoadedListener(OnDataLoadedListener onDataLoadedListener) {
        this.onDataLoadedListener = onDataLoadedListener;
    }

    public void getData(int DATA_TYPE) {
        loadRooms(DATA_TYPE);
    }

    public interface OnDataLoadedListener{
        public void onDataLoaded(ArrayList<Room> roomData,ArrayList<Task> taskData,ArrayList<User> userData);
    }

    public void setRoom(Room room) {
        this.room = room;
    }
}
