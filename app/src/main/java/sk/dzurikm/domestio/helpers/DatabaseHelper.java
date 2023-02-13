package sk.dzurikm.domestio.helpers;

import static sk.dzurikm.domestio.helpers.Constants.Firebase.DOCUMENT_ROOMS;
import static sk.dzurikm.domestio.helpers.Constants.Firebase.DOCUMENT_TASKS;
import static sk.dzurikm.domestio.helpers.Constants.Firebase.DOCUMENT_USERS;
import static sk.dzurikm.domestio.helpers.Constants.Firebase.Room.FIELD_ADMIN_ID;
import static sk.dzurikm.domestio.helpers.Constants.Firebase.Room.FIELD_COLOR;
import static sk.dzurikm.domestio.helpers.Constants.Firebase.Room.FIELD_DESCRIPTION;
import static sk.dzurikm.domestio.helpers.Constants.Firebase.Room.FIELD_TASK_IDS;
import static sk.dzurikm.domestio.helpers.Constants.Firebase.Room.FIELD_TITLE;
import static sk.dzurikm.domestio.helpers.Constants.Firebase.Room.FIELD_USER_IDS;
import static sk.dzurikm.domestio.helpers.Constants.Firebase.Task.FIELD_AUTHOR_ID;
import static sk.dzurikm.domestio.helpers.Constants.Firebase.Task.FIELD_DONE;
import static sk.dzurikm.domestio.helpers.Constants.Firebase.Task.FIELD_HEADING;
import static sk.dzurikm.domestio.helpers.Constants.Firebase.Task.FIELD_MODIFIED_AT;
import static sk.dzurikm.domestio.helpers.Constants.Firebase.Task.FIELD_ROOM_ID;
import static sk.dzurikm.domestio.helpers.Constants.Firebase.Task.FIELD_VERIFIED;
import static sk.dzurikm.domestio.helpers.Constants.Firebase.User.FIELD_CREATED_AT;
import static sk.dzurikm.domestio.helpers.Constants.Firebase.User.FIELD_EMAIL;
import static sk.dzurikm.domestio.helpers.Constants.Firebase.User.FIELD_ID;
import static sk.dzurikm.domestio.helpers.Constants.Firebase.User.FIELD_NAME;
import static sk.dzurikm.domestio.helpers.Helpers.DataSet.getRoomInfo;
import static sk.dzurikm.domestio.helpers.Helpers.firstUppercase;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import sk.dzurikm.domestio.R;
import sk.dzurikm.domestio.activities.RoomActivity;
import sk.dzurikm.domestio.models.Room;
import sk.dzurikm.domestio.models.Task;
import sk.dzurikm.domestio.models.User;

public class DatabaseHelper {
    FirebaseFirestore db;
    FirebaseUser user;
    FirebaseAuth auth;
    FirebaseFunctions functions;

    // Additional variables
    ArrayList<String> roomsIDs,userRelatedUserIds,tasksRelatedIds;

    // Datasets
    ArrayList<Room> roomData;
    ArrayList<Task> taskData;
    ArrayList<User> usersData;

    // Listeners
    OnDataLoadedListener onDataLoadedListener;

    public DatabaseHelper() {

        roomData = new ArrayList<>();
        roomsIDs = new ArrayList();
        userRelatedUserIds = new ArrayList();
        taskData = new ArrayList<Task>();
        usersData = new ArrayList<User>();
        tasksRelatedIds = new ArrayList<>();

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        functions = FirebaseFunctions.getInstance();

        // Enabling offline persistance
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build();
        db.setFirestoreSettings(settings);
    }

    int TYPE;
    String id;

    public void getUserFromDatabase(String uid, OnCompleteListener<DocumentSnapshot> onCompleteListener){
        db.collection(DOCUMENT_USERS).document(uid).get().addOnCompleteListener(onCompleteListener);
    }


    public void loadRooms(){
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

                                        ArrayList<String> tasksIds = data.get(Constants.Firebase.Room.FIELD_TASK_IDS) == null ? new ArrayList<String>() : (ArrayList) data.get(Constants.Firebase.Room.FIELD_TASK_IDS);
                                        Helpers.List.addUnique(tasksRelatedIds,tasksIds);


                                    }


                                    loadUsers();
                                    Log.i("Firebase result", String.valueOf(Arrays.toString(taskData.toArray())));

                                } else {
                                    Log.w("DB_RESULT", "Error getting documents.", task.getException());
                                }
                            }

                        });
                break;
            case Constants.Firebase.DATA_FOR_ROOM:
                loadUsers();
                break;

        }


    }

    public void loadUsers(){
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
                                user.cast(data);
                                user.setId(document.getId());

                                // Adding user to it's dataset
                                DatabaseHelper.this.usersData.add(user);
                                Log.i("Firebase result", String.valueOf(user));

                            }

                            Log.i("DB_RESULT", String.valueOf(Arrays.toString(usersData.toArray())));
                            loadTasks();

                        } else {
                            Log.w("DB_RESULT", "Error getting documents.", response.getException());
                        }
                    }
                });

    }

    public void loadTasks(){
        Query taskQuery;


        if (TYPE == Constants.Firebase.DATA_FOR_USER){
            if (tasksRelatedIds.size() == 0) {
                onDataLoadedListener.onDataLoaded(roomData,taskData,usersData);
                return;
            }
            taskQuery = db.collection(DOCUMENT_TASKS)
                    .whereEqualTo(Constants.Firebase.Task.FIELD_RECEIVER_ID,user.getUid());
            System.out.println(user.getUid());
        }
        else{
            taskQuery = db.collection(DOCUMENT_TASKS).whereEqualTo(FIELD_ROOM_ID, id);
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
                                task.setAuthor(getUsersName(task.getAuthorId()));

                                String roomID = TYPE == Constants.Firebase.DATA_FOR_USER ? task.getRoomId() : id;

                                HashMap<String,String> roomInfo = getRoomInfo(roomData,roomID, new String[]{FIELD_TITLE, Constants.Firebase.Room.FIELD_COLOR});

                                task.setRoomName(roomInfo.get(FIELD_TITLE));
                                task.setColor(roomInfo.get(Constants.Firebase.Room.FIELD_COLOR));

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


    }

    public void loadTasksForRoom(Room room, TasksForRoomLoadedListener tasksForRoomLoadedListener){
        if (room.getTaskIds() == null || room.getTaskIds().isEmpty()) {
            tasksForRoomLoadedListener.onTasksLoaded(null);
            return;
        }

        db.collection(DOCUMENT_TASKS).whereEqualTo(FIELD_ROOM_ID,room.getId()).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull com.google.android.gms.tasks.Task<QuerySnapshot> t) {
                if (t.isSuccessful()){
                    ArrayList<Task> data = new ArrayList<>();
                    for (QueryDocumentSnapshot document : t.getResult()){
                        Map<String, Object> dat = document.getData();

                        // Creating task and casting from db result to model
                        Task task = new Task();
                        task.cast(document.getId(),dat);
                        task.setAuthor(getUsersName(task.getAuthorId()));
                        task.setColor(getRoomsColor(task.getRoomId()));

                        data.add(task);
                        taskData.add(task);
                    }
                    tasksForRoomLoadedListener.onTasksLoaded(data);
                }
            }
        });
    }

    public void loadTasksCreatedByMe(TasksForRoomLoadedListener tasksForRoomLoadedListener){
        db.collection(DOCUMENT_TASKS).whereEqualTo(FIELD_AUTHOR_ID,auth.getUid()).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull com.google.android.gms.tasks.Task<QuerySnapshot> t) {
                if (t.isSuccessful()){
                    ArrayList<Task> data = new ArrayList<>();
                    for (QueryDocumentSnapshot document : t.getResult()){
                        Map<String, Object> dat = document.getData();

                        // Creating task and casting from db result to model
                        Task task = new Task();
                        task.cast(document.getId(),dat);
                        task.setAuthor(getUsersName(task.getAuthorId()));
                        task.setColor(getRoomsColor(task.getRoomId()));
                        task.setRoomName(getRoomsTitle(task.getRoomId()));

                        data.add(task);
                        taskData.add(task);
                    }
                    tasksForRoomLoadedListener.onTasksLoaded(data);
                }
            }
        });
    }

    public void login(Context context,String email, String password,OnCompleteListener onLoginCompleteListener){
        auth = FirebaseAuth.getInstance();
        auth.signInWithEmailAndPassword(email,password)
                .addOnCompleteListener((Activity) context, onLoginCompleteListener);
    }

    public void register(Context context,String email, String password,OnRegisterListener onRegisterListener){
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener((Activity) context, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull com.google.android.gms.tasks.Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d("Firebase Auth", "createUserWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();

                            onRegisterListener.onRegisterSuccess();

                        }
                        else onRegisterListener.onRegisterFailed();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        onRegisterListener.onRegisterFailed();
                    }
                });
    }

    public void resetPassword(Context context,String email){
        auth.sendPasswordResetEmail(email).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(context, R.string.password_reset_fail,Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void changeDocument(String COLLECTION,String id, Map<String,Object> dataToChange, OnFailureListener onFailureListener){
        dataToChange.put(FIELD_MODIFIED_AT, FieldValue.serverTimestamp());

        db.collection(COLLECTION)
                .document(id)
                .update(dataToChange)
                .addOnFailureListener(onFailureListener);
    }

    public void changeDocument(String COLLECTION,String id, Map<String,Object> dataToChange, OnFailureListener onFailureListener,boolean notify){
        if (notify) dataToChange.put(FIELD_MODIFIED_AT, FieldValue.serverTimestamp());

        db.collection(COLLECTION)
                .document(id)
                .update(dataToChange)
                .addOnFailureListener(onFailureListener);
    }

    public String addRoom(Room room, OnRoomAddedListener roomAddedListener){

        Map<String, Object> roomMap = new HashMap<>();
        roomMap.put(FIELD_TITLE,room.getTitle());
        roomMap.put(FIELD_DESCRIPTION,room.getDescription());
        roomMap.put(FIELD_ADMIN_ID,room.getAdminId());
        roomMap.put(FIELD_USER_IDS,room.getUserIds());
        roomMap.put(FIELD_TASK_IDS, room.getTaskIds());
        roomMap.put(FIELD_COLOR, room.getColor());
        roomMap.put(FIELD_CREATED_AT, FieldValue.serverTimestamp());
        roomMap.put(FIELD_MODIFIED_AT, FieldValue.serverTimestamp());

        DocumentReference document = db.collection(DOCUMENT_ROOMS).document();
        document.set(roomMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull com.google.android.gms.tasks.Task<Void> task) {
                room.setId(document.getId());
                roomAddedListener.onRoomAdded(task,room);
            }
        });

        return document.getId();

    }

    public String addTask(Task task,OnTaskAddedListener onTaskAddedListener){
        Map<String, Object> tasksMap = new HashMap<>();
        tasksMap.put(FIELD_HEADING,task.getHeading());
        tasksMap.put(Constants.Firebase.Task.FIELD_DESCRIPTION,task.getDescription());
        tasksMap.put(Constants.Firebase.Task.FIELD_AUTHOR_ID,task.getAuthorId());
        tasksMap.put(Constants.Firebase.Task.FIELD_DONE,task.getDone());
        tasksMap.put(Constants.Firebase.Task.FIELD_RECEIVER_ID,task.getReceiverId());
        tasksMap.put(FIELD_ROOM_ID,task.getRoomId());
        tasksMap.put(Constants.Firebase.Task.FIELD_TIME,Helpers.Time.getTimeDateForDB(task.getTimestamp()));
        tasksMap.put(FIELD_CREATED_AT, FieldValue.serverTimestamp());
        tasksMap.put(FIELD_MODIFIED_AT, FieldValue.serverTimestamp());
        tasksMap.put(FIELD_VERIFIED,false);

        DocumentReference document = db.collection(DOCUMENT_TASKS).document();
        document.set(tasksMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull com.google.android.gms.tasks.Task<Void> t) {
                task.setId(document.getId());

                db.collection(DOCUMENT_ROOMS).document(task.getRoomId()).update(FIELD_TASK_IDS,FieldValue.arrayUnion(task.getId())).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull com.google.android.gms.tasks.Task<Void> t) {
                        if (t.isSuccessful()) onTaskAddedListener.onTaskAdded(t, task);

                    }
                });

            }
        });

        return document.getId();
    }

    public void addMemberInRoom(String roomId,String userId, OnCompleteListener onCompleteListener){
        Map<String, Object> update = new HashMap<>();
        update.put("room_id", roomId);
        update.put("user_id",userId);

        functions.getHttpsCallable("addUserToRoom")
                .call(update)
                .continueWith(new Continuation<HttpsCallableResult, String>() {
                    @Override
                    public String then(@NonNull com.google.android.gms.tasks.Task<HttpsCallableResult> task) throws Exception {
                        if (task.isSuccessful()){
                            String result = (String) task.getResult().getData();
                            onCompleteListener.onComplete(task);
                            return result;
                        }

                        return null;
                    }

                });


        /*Map<String, Object> update = new HashMap<>();
        update.put(Constants.Firebase.Room.FIELD_USER_IDS, FieldValue.arrayUnion(userId));
        update.put(Constants.Firebase.Room.FIELD_MODIFIED_AT,FieldValue.serverTimestamp());
        db.collection(DOCUMENT_ROOMS).document(roomId)
                .update(update).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull com.google.android.gms.tasks.Task<Void> task) {
                        // TODO send and email to user
                        onCompleteListener.onComplete(task);
                    }
                });*/
    }

    public void updateTaskDone(Task task){
        Map<String, Object> update = new HashMap<>();
        update.put("task_id", task.getId());
        update.put("done",task.getDone());

        functions.getHttpsCallable("taskDoneToggle")
                .call(update)
                .continueWith(new Continuation<HttpsCallableResult, String>() {
                    @Override
                    public String then(@NonNull com.google.android.gms.tasks.Task<HttpsCallableResult> task) throws Exception {
                        if (task.isSuccessful()){
                            String result = (String) task.getResult().getData();
                            // TODO i dont check if this is successfull i just do it , bada55
                            return result;
                        }

                        return null;
                    }

                });

    }

    public void updateTaskVerified(Task task){
        Map<String, Object> update = new HashMap<>();
        update.put("task_id", task.getId());

        functions.getHttpsCallable("taskVerified")
                .call(update)
                .continueWith(new Continuation<HttpsCallableResult, String>() {
                    @Override
                    public String then(@NonNull com.google.android.gms.tasks.Task<HttpsCallableResult> task) throws Exception {
                        if (task.isSuccessful()){
                            String result = (String) task.getResult().getData();
                            Log.i("Task result", result);
                            // TODO i dont check if this is successfull i just do it , bada55
                            return result;
                        }

                        return null;
                    }

                });
    }

    public void updateTask(Task task,OnTaskEditedListener onTaskEditedListener,Task originalTask){

        System.out.println(task.getTimestamp());
        System.out.println(new Date(task.getTimestamp()).toString());
        System.out.println(Helpers.Time.getTimeDateForDB(task.getTimestamp()).toString());

        Map<String, Object> tasksMap = new HashMap<>();
        tasksMap.put(FIELD_HEADING,task.getHeading());
        tasksMap.put(Constants.Firebase.Task.FIELD_DESCRIPTION,task.getDescription());
        tasksMap.put(Constants.Firebase.Task.FIELD_AUTHOR_ID,task.getAuthorId());
        tasksMap.put(Constants.Firebase.Task.FIELD_DONE,task.getDone());
        tasksMap.put(Constants.Firebase.Task.FIELD_RECEIVER_ID,task.getReceiverId());
        tasksMap.put(FIELD_ROOM_ID,task.getRoomId());
        tasksMap.put(Constants.Firebase.Task.FIELD_TIME,Helpers.Time.getTimeDateForDB(task.getTimestamp()));
        tasksMap.put(FIELD_MODIFIED_AT, FieldValue.serverTimestamp());

        DocumentReference document = db.collection(DOCUMENT_TASKS).document(task.getId());
        document.update(tasksMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull com.google.android.gms.tasks.Task<Void> t) {

                db.collection(DOCUMENT_ROOMS).document(task.getRoomId()).update(FIELD_TASK_IDS,FieldValue.arrayUnion(task.getId())).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull com.google.android.gms.tasks.Task<Void> t) {
                        if (originalTask != null){
                            if (!originalTask.getRoomId().equals(task.getRoomId())){
                                task.setRoomName(getRoomsTitle(task.getRoomId()));
                                task.setColor(getRoomsColor(task.getRoomId()));
                            }
                            if (!originalTask.getReceiverId().equals(task.getReceiverId())){
                                task.setAuthor(getUsersName(task.getAuthorId()));
                            }
                        }

                        if (t.isSuccessful()) {
                            if (originalTask != null){
                                if (!originalTask.getRoomId().equals(task.getRoomId())) {
                                    moveTaskId(task.getId(), originalTask.getRoomId(), task.getRoomId(), new OnCompleteListener() {
                                        @Override
                                        public void onComplete(@NonNull com.google.android.gms.tasks.Task result) {
                                            if (result.isSuccessful()){
                                                onTaskEditedListener.onTaskEdited(t, task);
                                            }
                                        }
                                    });
                                }
                                else onTaskEditedListener.onTaskEdited(t, task);
                            }
                            else onTaskEditedListener.onTaskEdited(t, task);
                        }

                    }
                });

            }
        });


    }

    private void moveTaskId(String taskId,String from, String to, OnCompleteListener onCompleteListener){
        Map<String, Object> updateData = new HashMap<>();
        updateData.put(FIELD_TASK_IDS,FieldValue.arrayRemove(taskId));
        updateData.put(FIELD_MODIFIED_AT, FieldValue.serverTimestamp());

        DocumentReference document = db.collection(DOCUMENT_ROOMS).document(from);
        document.update(updateData).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull com.google.android.gms.tasks.Task<Void> task) {
                if (task.isSuccessful()){
                    Map<String, Object> updateData = new HashMap<>();
                    updateData.put(FIELD_TASK_IDS,FieldValue.arrayUnion(taskId));
                    updateData.put(FIELD_MODIFIED_AT, FieldValue.serverTimestamp());

                    DocumentReference document = db.collection(DOCUMENT_ROOMS).document(to);
                    document.update(updateData).addOnCompleteListener(onCompleteListener);
                }
            }
        });
    }

    public void updateUserName(String newName,OnCompleteListener<Void> onCompleteListener,OnFailureListener onFailureListener){
        db.collection(DOCUMENT_USERS).document(auth.getCurrentUser().getUid()).update(FIELD_NAME, newName).addOnCompleteListener(onCompleteListener);


    }

    public void updateUserEmail(String newEmail,OnCompleteListener<Void> onCompleteListener){

        Objects.requireNonNull(auth.getCurrentUser()).updateEmail(newEmail).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull com.google.android.gms.tasks.Task<Void> task) {
                db.collection(DOCUMENT_USERS).document(auth.getUid()).update(FIELD_EMAIL,newEmail).addOnCompleteListener(onCompleteListener);
            }
        });

    }

    public void updateUserPassword(String password,OnCompleteListener<Void> onCompleteListener){
        Objects.requireNonNull(auth.getCurrentUser()).updatePassword(password).addOnCompleteListener(onCompleteListener);
    }

    public void removeUserFromRoom(Room room,String userId, OnCompleteListener onCompleteListener){

        Map<String, Object> update = new HashMap<>();
        update.put("room_id", room.getId());
        update.put("user_id",userId);

        functions.getHttpsCallable("removeUserFromRoom")
                .call(update)
                .continueWith(new Continuation<HttpsCallableResult, String>() {
                    @Override
                    public String then(@NonNull com.google.android.gms.tasks.Task<HttpsCallableResult> task) throws Exception {
                        if (task.isSuccessful()){
                            String result = (String) task.getResult().getData();
                            onCompleteListener.onComplete(task);
                            return result;
                        }

                        return null;
                    }

                });



    }

    public void leaveRoom(Room room,String userId, OnCompleteListener onCompleteListener){
        Map<String, Object> update = new HashMap<>();
        update.put("room_id", room.getId());

        functions.getHttpsCallable("leaveRoom")
                .call(update)
                .continueWith(new Continuation<HttpsCallableResult, String>() {
                    @Override
                    public String then(@NonNull com.google.android.gms.tasks.Task<HttpsCallableResult> task) throws Exception {
                        if (task.isSuccessful()){
                            String result = (String) task.getResult().getData();
                            onCompleteListener.onComplete(task);
                            return result;
                        }

                        return null;
                    }

                });
    }

    public void removeRoom(Room room,OnCompleteListener onCompleteListener){
        db.collection(DOCUMENT_ROOMS).document(room.getId()).delete().addOnCompleteListener(onCompleteListener);
    }

    public void removeUnrelatedTask(Task task){
        db.collection(DOCUMENT_TASKS).document(task.getId()).delete();
        db.collection(DOCUMENT_ROOMS).document(task.getRoomId()).update(FIELD_TASK_IDS,FieldValue.arrayRemove(task.getId()));
    }

    /**
     *
     * @param id user UID
     * @return id name of user with  id
     */
    private String getUsersName(String id){
        ArrayList<User> users = usersData;
        if (users.isEmpty()){
            users = DataStorage.users;
        }

        for (int i = 0; i < users.size(); i++) {
            User user = users.get(i);
            if (user.getId().equals(id)) return user.getName();
        }

        return "";
    }

    /**
     * @param id user UID
     * @return title of room wit id
     */
    private String getRoomsTitle(String id){
        ArrayList<Room> rooms = roomData;
        if (rooms.isEmpty()){
            rooms = DataStorage.rooms;
        }
        for (int i = 0; i < rooms.size(); i++) {
            Room room = rooms.get(i);
            if (room.getId().equals(id)) return room.getTitle();
        }

        return "";
    }

    /**
     * @param id room UID
     * @return color of room with id
     */
    private String getRoomsColor(String id){
        ArrayList<Room> rooms = roomData;
        if (rooms.isEmpty()){
            rooms = DataStorage.rooms;
        }

        for (int i = 0; i < rooms.size(); i++) {
            Room room = rooms.get(i);
            if (room.getId().equals(id)) return room.getColor();
        }

        return "";
    }

    public void setOnDataLoadedListener(OnDataLoadedListener onDataLoadedListener) {
        this.onDataLoadedListener = onDataLoadedListener;
    }

    public void getData(int DATA_TYPE,String id) {
        this.TYPE = TYPE;
        this.id = id;

        loadRooms();
    }

    public interface OnDataLoadedListener{
        public void onDataLoaded(ArrayList<Room> roomData,ArrayList<Task> taskData,ArrayList<User> userData);
    }

    public interface OnRegisterListener{
        public void onRegisterSuccess();
        public void onRegisterFailed();
    }

    public interface OnRoomAddedListener {
        public void onRoomAdded(com.google.android.gms.tasks.Task task, Room room);
    }

    public interface OnTaskAddedListener {
        public void onTaskAdded(com.google.android.gms.tasks.Task t, Task task);
    }

    public interface OnTaskEditedListener {
        public void onTaskEdited(com.google.android.gms.tasks.Task t, Task task);
    }

    public interface TasksForRoomLoadedListener {
        public void onTasksLoaded(ArrayList<Task> data);
    }

}
