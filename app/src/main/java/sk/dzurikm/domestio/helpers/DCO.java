package sk.dzurikm.domestio.helpers;

import android.graphics.Color;
import android.view.View;

import java.util.ArrayList;

import sk.dzurikm.domestio.models.Room;
import sk.dzurikm.domestio.models.Task;
import sk.dzurikm.domestio.models.User;

public class DCO {
    /* DCO stands for Data Change Object */

    private ArrayList<Room> roomData;
    private ArrayList<Task> taskData;
    private ArrayList<User> usersData;

    private Room room;

    private OnDataChangeListener onDataChangeListener;
    
    private DatabaseHelper databaseHelper;

    public DCO(ArrayList<Room> roomData, ArrayList<Task> taskData, ArrayList<User> usersData, OnDataChangeListener onDataChangeListener) {
        this.roomData = roomData;
        this.taskData = taskData;
        this.usersData = usersData;
        this.onDataChangeListener = onDataChangeListener;
        
        databaseHelper = new DatabaseHelper();
    }

    public DCO(Room room, ArrayList<Task> taskData, ArrayList<User> usersData, OnDataChangeListener onDataChangeListener) {
        this.roomData = null;
        this.room = room;
        this.taskData = taskData;
        this.usersData = usersData;
        this.onDataChangeListener = onDataChangeListener;

        databaseHelper = new DatabaseHelper();
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
        if (roomData != null) {
            for (int i = 0; i < roomData.size(); i++) {
                if (roomData.get(i).getId().equals(task.getRoomId())){
                    roomData.get(i).addTaskId(task.getId());

                    break;
                }
            }

            task.setColor(getRoomsColor(task.getRoomId()));
        }

        else{
            task.setColor(room.getColor());
        }


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
            if (usersData.get(i).getId().equals(user.getId())){
                user.update(user);
                onDataChangeListener.onChange(usersData,roomData,taskData);
                break;
            }
        }
    }


    // Room Activity
    public void updateRoomChangeableInfo(Room room){
        System.out.println(room);
        this.room = room;

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

    public ArrayList<User> filterUsersForThisRoom(){
        ArrayList<User> filtered = new ArrayList<>();

        for (int i = 0; i < usersData.size(); i++) {
            if (room.getUserIds().contains(usersData.get(i).getId())) filtered.add(usersData.get(i));
        }

        return filtered;
    }

    public Room getRoom() {
        return room;
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

    public void setRoom(Room room) {
        this.room = room;
    }

    public interface OnDataChangeListener{
        public void onChange(ArrayList<User> usersData,ArrayList<Room> roomData,ArrayList<Task> taskData);
    }
}
