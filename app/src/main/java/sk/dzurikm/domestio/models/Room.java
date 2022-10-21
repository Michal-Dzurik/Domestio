package sk.dzurikm.domestio.models;

import android.os.Parcel;
import android.os.Parcelable;

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
        this.title = (String) data.get("title");
        this.description = (String) data.get("description");
        this.userIds = data.get("user_ids") == null ? new ArrayList<String>() : (ArrayList) data.get("user_ids");
        this.takIds = data.get("task_ids") == null ? new ArrayList<String>() : (ArrayList) data.get("task_ids");
        this.color = (String) data.get("color");
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
