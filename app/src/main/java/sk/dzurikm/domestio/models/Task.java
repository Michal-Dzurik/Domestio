package sk.dzurikm.domestio.models;

import static sk.dzurikm.domestio.helpers.Constants.Firebase.Task.*;


import com.google.firebase.Timestamp;
import java.io.Serializable;
import java.util.Date;
import java.util.Map;

import sk.dzurikm.domestio.helpers.Helpers;

public class Task implements Serializable {
    String id;
    String heading;
    String description;
    String time;
    String ownerId;
    String roomId;
    String owner;
    String room;
    String receiverId;
    Date date;
    String color;
    Boolean done;

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

    public String getId() {
        return id;
    }

    public Date getDate() {
        return date;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Boolean getDone() {
        return done;
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
        this.heading = (String) data.get(FIELD_HEADING);
        this.description = (String) data.get(FIELD_DESCRIPTION);

        Timestamp ts = (Timestamp) data.get(FIELD_TIME);
        assert ts != null;
        this.date = ts.toDate();
        this.time = Helpers.Time.getTimeDate(ts.getSeconds());

        this.roomId = (String) data.get(FIELD_ROOM_ID);
        this.ownerId = (String) data.get(FIELD_AUTHOR_ID);
        this.receiverId = (String) data.get(FIELD_RECEIVER_ID);

        this.done = (Boolean) data.get(FIELD_DONE);
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
                ", timestamp=" + new Timestamp(date) +
                ", done=" + done +
                '}';
    }
}
