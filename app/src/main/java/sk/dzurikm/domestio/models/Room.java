package sk.dzurikm.domestio.models;

import static sk.dzurikm.domestio.helpers.Constants.Firebase.Room.*;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.Map;

public class Room implements Serializable {
    String id,title,description,color,adminId;
    ArrayList<String> userIds, taskIds;

    private boolean justLeft = false;

    public Room() {
    }

    // This is constructor just for Room dropdown when creating task
    public Room(String id, String title) {
        this.id = id;
        this.title = title;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getPeopleCount() {
        return userIds.size();
    }

    public int getTasksCount() {
        return taskIds.size();
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public void setAdminId(String adminId) {
        this.adminId = adminId;
    }

    public boolean hasJustLeft() {
        return justLeft;
    }

    public void setJustLeft(boolean justLeft) {
        this.justLeft = justLeft;
    }

    public String getAdminId() {
        return adminId;
    }

    public ArrayList<String> getTaskIds() {
        return taskIds;
    }

    public void setUserIds(ArrayList<String> userIds) {
        this.userIds = userIds;
    }

    public ArrayList<String> getUserIds() {
        return userIds;
    }

    public void setTaskIds(ArrayList<String> taskIds) {
        this.taskIds = taskIds;
    }


    public void cast(String id, Map<String, Object> data){
        this.id = id;
        this.title = (String) data.get(FIELD_TITLE);
        this.description = (String) data.get(FIELD_DESCRIPTION);
        this.userIds = data.get(FIELD_USER_IDS) == null ? new ArrayList<String>() : (ArrayList) data.get(FIELD_USER_IDS);
        this.taskIds = data.get(FIELD_TASK_IDS) == null ? new ArrayList<String>() : (ArrayList) data.get(FIELD_TASK_IDS);
        this.color = (String) data.get(FIELD_COLOR);
        this.adminId = (String) data.get(FIELD_ADMIN_ID);
    }

    public boolean isAdmin(String uid){
        return uid.equals(adminId);
    }

    public void addTaskId(String taskID){
        taskIds.add(taskID);
    }

    public void removeTaskId(String taskID){
        taskIds.remove(taskID);
    }

    public void addUserId(String userID){
        userIds.add(userID);
    }

    public void removeUserId(String userID){
        userIds.remove(userID);
    }

    @Override
    public String toString() {
        return "Room{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", userIds=" + userIds +
                ", takIds=" + taskIds +
                ", color=" + color +
                '}';
    }
}
