package sk.dzurikm.domestio.helpers;


import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;

import java.util.ArrayList;

import sk.dzurikm.domestio.models.Room;
import sk.dzurikm.domestio.models.Task;
import sk.dzurikm.domestio.models.User;

public class DCO {
    /* DCO stands for Data Change Object */

    private ArrayList<Room> roomData;
    private ArrayList<Task> taskData;
    private ArrayList<User> usersData;

    private OnDataChangeListener onDataChangeListener;
    
    private DatabaseHelper databaseHelper;

    public DCO(OnDataChangeListener onDataChangeListener) {
        this.onDataChangeListener = new OnDataChangeListener() {
            @Override
            public void onChange(ArrayList<User> usersData, ArrayList<Room> roomData, ArrayList<Task> taskData) {
                onDataChangeListener.onChange(usersData,roomData,taskData);

                saveToStorage();
            }
        };

        loadFromStorage();

        databaseHelper = new DatabaseHelper();
    }

    public DCO(ArrayList<Room> roomData, ArrayList<Task> taskData, ArrayList<User> usersData, OnDataChangeListener onDataChangeListener) {
        this.roomData = roomData != null ? (ArrayList<Room>) roomData.clone() : new ArrayList<Room>();
        this.taskData = taskData != null ? (ArrayList<Task>) taskData.clone() : new ArrayList<Task>();
        this.usersData = usersData != null ? (ArrayList<User>) usersData.clone() : new ArrayList<User>();
        this.onDataChangeListener = new OnDataChangeListener() {
            @Override
            public void onChange(ArrayList<User> usersData, ArrayList<Room> roomData, ArrayList<Task> taskData) {
                onDataChangeListener.onChange(usersData,roomData,taskData);

                saveToStorage();
            }
        };

        saveToStorage();
        
        databaseHelper = new DatabaseHelper();
    }

    public DCO(ArrayList<Task> taskData, ArrayList<User> usersData, OnDataChangeListener onDataChangeListener) {
        this.roomData = null;
        this.taskData = taskData;
        this.usersData = usersData;
        this.onDataChangeListener = new OnDataChangeListener() {
            @Override
            public void onChange(ArrayList<User> usersData, ArrayList<Room> roomData, ArrayList<Task> taskData) {
                onDataChangeListener.onChange(usersData,roomData,taskData);

                saveToStorage();
            }
        };

        saveToStorage();

        databaseHelper = new DatabaseHelper();
    }

    public void saveToStorage(){
        DataStorage.rooms = roomData;
        DataStorage.tasks = taskData;
        DataStorage.users = usersData;
    }

    public void loadFromStorage(){
        roomData = DataStorage.rooms;
        taskData = DataStorage.tasks ;
        usersData = DataStorage.users;

        Log.i("Loading from storage result", String.valueOf(taskData));
    }

    public void addRoom(Room room){
        if (roomData == null) return;
        roomData.add(room);
        databaseHelper.loadTasksForRoom(room, new DatabaseHelper.TasksForRoomLoadedListener() {
            @Override
            public void onTasksLoaded(ArrayList<Task> data) {
                if (data != null)
                    taskData.addAll(data);

                onDataChangeListener.onChange(usersData,roomData,taskData);
            }
        });
    }

    public void onRoomChanged(Room room){
        if (roomData == null || taskData == null) return;
        for (int i = 0; i < roomData.size(); i++) {
            if (roomData.get(i).getId().equals(room.getId())) {
                roomData.set(i, room);
                break;
            }
        }

        for (int i = 0; i < taskData.size(); i++) {
            if (taskData.get(i).getRoomId().equals(room.getId())) {
                taskData.get(i).setRoomName(room.getTitle());
                taskData.get(i).setColor(room.getColor());
                break;
            }
        }

        onDataChangeListener.onChange(usersData,roomData,taskData);

    }

    public void cleanAfterLeftRoom(Room room){
        if (roomData == null) return;
        for (int i = 0; i < roomData.size(); i++) {
            if (roomData.get(i).getId().equals(room.getId())) {
                roomData.remove(i);

                
                removeAllTasksWithRoomId(room.getId());
                
                onDataChangeListener.onChange(usersData,roomData,taskData);
                break;
            }
        }
    }

    public void removeAllTasksWithRoomId(String id){
        if (taskData == null) return;
        for (int i = 0; i < taskData.size(); i++) {
            if (taskData.get(i).getRoomId().equals(id)){
                taskData.remove(i);
                break;
            }
        }

        onDataChangeListener.onChange(usersData,roomData,taskData);
    }

    public void onTaskAdded(Task task){
        if (roomData == null) return;
        for (int i = 0; i < roomData.size(); i++) {
            if (roomData.get(i).getId().equals(task.getRoomId())){
                
                roomData.get(i).addTaskId(task.getId());
                onDataChangeListener.onChange(usersData,roomData,taskData);
                break;
            }
        }
    }

    public void addTask(Task task){

            for (int i = 0; i < roomData.size(); i++) {
                if (roomData.get(i).getId().equals(task.getRoomId())){
                    roomData.get(i).addTaskId(task.getId());

                    break;
                }
            }
            task.setColor(getRoomsColor(task.getRoomId()));


        taskData.add(task);
        onDataChangeListener.onChange(usersData,roomData,taskData);
    }

    public String getRoomsColor(String id){
        if (roomData == null) return "";
        for (int i = 0; i < roomData.size(); i++) {
            Room room = roomData.get(i);
            if (room.getId().equals(id)) return room.getColor();
        }

        return "";
    }

    public void removeTask(Task task){
        if (roomData == null || taskData == null) return;
        for (int i = 0; i < taskData.size(); i++) {
            if (taskData.get(i).getId().equals(task.getId())){
                taskData.remove(i);
                break;
            }
        }

        for (int i = 0; i < roomData.size(); i++) {
            if (roomData.get(i).getId().equals(task.getRoomId())){
                roomData.get(i).removeTaskId(task.getId());
                
                break;
            }
        }

        onDataChangeListener.onChange(usersData,roomData,taskData);
    }

    public void updateTask(Task task){
        if (taskData == null) return;
        for (int i = 0; i < taskData.size(); i++) {
            if (taskData.get(i).getId().equals(task.getId())){
                taskData.get(i).update(task);
                onDataChangeListener.onChange(usersData,roomData,taskData);
                break;
            }
        }
    }

    public void addUser(User user){
        if (usersData == null) return;
        usersData.add(user);
        onDataChangeListener.onChange(usersData,roomData,taskData);
    }

    public void updatedUser(User user){
        if (usersData == null) return;
        for (int i = 0; i < usersData.size(); i++) {
            System.out.println(user);
            System.out.println(usersData.get(i));
            if (user.getId().equals(usersData.get(i).getId())){
                user.update(user);
                onDataChangeListener.onChange(usersData,roomData,taskData);
                break;
            }
        }
    }


    // Room Activity
    public void updateRoomChangeableInfo( ArrayList<Task> taskData,Room room){
        System.out.println(room);

        for (int i = 0; i < taskData.size(); i++) {
            taskData.get(i).setColor(room.getColor());
        }

        onDataChangeListener.onChange(usersData,roomData,DCO.this.taskData);
    }

    public void updateRoomChangeableInfo(Room room){
        System.out.println(room);

        for (int i = 0; i < taskData.size(); i++) {
            taskData.get(i).setColor(room.getColor());
        }

        onDataChangeListener.onChange(usersData,roomData,taskData);
    }

    public void updateTaskRoomInfo(Room room){
        for (int i = 0; i < taskData.size(); i++) {
            taskData.get(i).setRoomName(room.getTitle());
        }

        onDataChangeListener.onChange(usersData,roomData,taskData);
    }

    public String getUserIdByEmail(String email){
        email = email.trim();
        for (int i = 0; i < usersData.size(); i++) {
            if (usersData.get(i).getEmail().equals(email)) return usersData.get(i).getId();
        }

        return null;
    }

    public ArrayList<User> filterUsersForThisRoom(ArrayList<String> users){
        ArrayList<User> filtered = new ArrayList<>();

        if(DataStorage.users == null || DataStorage.users.isEmpty() || users == null || users.isEmpty()) return filtered;

        for (int i = 0; i < DataStorage.users.size(); i++) {
            if (users.contains(DataStorage.users.get(i).getId())) filtered.add(DataStorage.users.get(i));
        }

        return filtered;
    }

    public ArrayList<Task> filterTasksForThisRoom(String id){
        ArrayList<Task> filtered = new ArrayList<>();

        for (int i = 0; i < taskData.size(); i++) {
            if (id.equals(taskData.get(i).getRoomId())) filtered.add(taskData.get(i));
        }

        return filtered;
    }

    public void leaveRoom(Room room,String uid, OnCompleteListener onCompleteListener){
        databaseHelper.leaveRoom(room, uid, new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull com.google.android.gms.tasks.Task task) {
                if (task.isSuccessful()){
                    cleanAfterLeftRoom(room);
                    onCompleteListener.onComplete(task);
                }
                else {}
            }
        });
    }

    public void removeRoom(Room room,OnCompleteListener onCompleteListener){
        databaseHelper.removeRoom(room, new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull com.google.android.gms.tasks.Task task) {
                roomData.remove(room);

                removeAllTasksWithRoomId(room.getId());

                saveToStorage();
                onCompleteListener.onComplete(task);
            }
        });



    }


    public void setRoomData(ArrayList<Room> roomData) {
        this.roomData = roomData;
    }

    public void setTaskData(ArrayList<Task> taskData) {
        this.taskData = taskData;
    }

    public void setUsersData(ArrayList<User> usersData) {
        this.usersData = usersData;
    }


    public ArrayList<Room> getRoomData() {
        return roomData;
    }

    public ArrayList<Task> getTaskData() {
        return taskData;
    }

    public ArrayList<User> getUsersData() {
        return usersData;
    }

    public Room getRoom(String id) {
        for (int i = 0; i < roomData.size(); i++) {
            if (id.equals(roomData.get(i).getId())) return roomData.get(i);
        }

        return null;
    }

    public interface OnDataChangeListener{
        public void onChange(ArrayList<User> usersData,ArrayList<Room> roomData,ArrayList<Task> taskData);
    }
}
