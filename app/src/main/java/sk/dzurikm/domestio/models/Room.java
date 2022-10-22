package sk.dzurikm.domestio.models;

import static sk.dzurikm.domestio.helpers.Constants.Firebase.Room.*;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.Map;

public class Room implements Serializable {
    String id,title,description,color;
    ArrayList<String> userIds,takIds;

    public Room(String id, String title, String description, ArrayList<String> userIds, ArrayList<String> takIds) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.userIds = userIds;
        this.takIds = takIds;
    }

    public Room() {
    }

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
        return takIds.size();
    }

    public void cast(String id,Map<String, Object> data){
        this.id = id;
        this.title = (String) data.get(FIELD_TITLE);
        this.description = (String) data.get(FIELD_DESCRIPTION);
        this.userIds = data.get(FIELD_USER_IDS) == null ? new ArrayList<String>() : (ArrayList) data.get(FIELD_USER_IDS);
        this.takIds = data.get(FIELD_TASK_IDS) == null ? new ArrayList<String>() : (ArrayList) data.get(FIELD_TASK_IDS);
        this.color = (String) data.get(FIELD_COLOR);
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    @Override
    public String toString() {
        return "Room{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", userIds=" + userIds +
                ", takIds=" + takIds +
                ", color=" + color +
                '}';
    }
}
