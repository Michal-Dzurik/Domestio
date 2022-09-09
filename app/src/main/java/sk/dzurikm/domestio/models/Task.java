package sk.dzurikm.domestio.models;

import com.google.firebase.Timestamp;

import java.util.Map;

import sk.dzurikm.domestio.helpers.Helpers;

public class Task {
    String id;
    String heading;
    String description;
    String time;
    String ownerId;
    String roomId;
    String owner;
    String room;
    String receiverId;
    Timestamp timestamp;

    public Task(String id,String task, String description, String time, String ownerId, String roomId,String receiverId) {
        this.id = id;
        this.heading = task;
        this.description = description;
        this.time = time;
        this.ownerId = ownerId;
        this.roomId = roomId;
        this.receiverId = receiverId;
    }

    public Task() {
    }

    public String getHeading() {
        return heading;
    }

    public String getDescription() {
        return description;
    }

    public String getTime() {
        return time;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public String getRoomId() {
        return roomId;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getRoom() {
        return room;
    }

    public void setRoom(String room) {
        this.room = room;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public void cast(String id, Map<String, Object> data){
        this.id = id;
        this.heading = (String) data.get("heading");
        this.description = (String) data.get("description");

        this.timestamp = (Timestamp) data.get("time");
        this.time = Helpers.Time.getTimeDate(timestamp.getSeconds());

        this.roomId = (String) data.get("room_id");
        this.ownerId = (String) data.get("author_user_id");
        this.receiverId = (String) data.get("receiving_user_id");

        System.out.println(ownerId);

    }

    @Override
    public String toString() {
        return "Task{" +
                "id='" + id + '\'' +
                ", heading='" + heading + '\'' +
                ", description='" + description + '\'' +
                ", time='" + time + '\'' +
                ", ownerId='" + ownerId + '\'' +
                ", roomId='" + roomId + '\'' +
                ", owner='" + owner + '\'' +
                ", room='" + room + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
