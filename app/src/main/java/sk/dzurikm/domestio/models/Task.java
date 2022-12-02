package sk.dzurikm.domestio.models;

import static sk.dzurikm.domestio.helpers.Constants.Firebase.Task.*;


import com.google.firebase.Timestamp;
import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import sk.dzurikm.domestio.helpers.Constants;
import sk.dzurikm.domestio.helpers.Helpers;

public class Task implements Serializable {
    String id;
    String heading;
    String description;
    String time;
    String authorId;
    String roomId;
    String author;
    String receiverId;
    String roomName;
    Date date;
    String color;
    Boolean done;
    Long timestamp;

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

    public void setHeading(String heading) {
        this.heading = heading;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public void setAuthorId(String authorId) {
        this.authorId = authorId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }

    public void setDone(Boolean done) {
        this.done = done;
    }

    public void setTimestamp(Long timestamp) {
        Timestamp ts = new Timestamp(new Date(timestamp));
        this.date = ts.toDate();
        this.time = Helpers.Time.getTimeDate(ts.getSeconds());
        this.timestamp = timestamp;
    }

    public String getTime() {
        return time;
    }

    public String getAuthorId() {
        return authorId;
    }

    public String getRoomId() {
        return roomId;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
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
        this.timestamp = ts.getSeconds();

        this.roomId = (String) data.get(FIELD_ROOM_ID);
        this.authorId = (String) data.get(FIELD_AUTHOR_ID);
        this.receiverId = (String) data.get(FIELD_RECEIVER_ID);

        this.done = (Boolean) data.get(FIELD_DONE);
    }

    public boolean isOwner(String uid){
        return uid.equals(authorId);
    }

    public Long getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return "Task{" +
                "id='" + id + '\'' +
                ", heading='" + heading + '\'' +
                ", description='" + description + '\'' +
                ", time='" + time + '\'' +
                ", authorId='" + authorId + '\'' +
                ", roomId='" + roomId + '\'' +
                ", author='" + author + '\'' +
                ", timestamp=" + new Timestamp(date) +
                ", done=" + done +
                ", color=" + (color == null ? "Nothing" : color) +
                '}';
    }

    public void setId(String id) {
        this.id = id;
    }

    public void update(Task task) {
        this.timestamp = task.getTimestamp();
        this.heading = task.getHeading();
        this.description = task.getDescription();
        this.date = task.getDate();
        this.time = task.getTime();
        this.done = task.getDone();
        this.color = task.getColor();
        this.roomName = task.getRoomName();
    }

    public void updateFromValidationData(HashMap<String,String> map){
        this.heading = map.get(Constants.Validation.Task.HEADING);
        this.description = map.get(Constants.Validation.Task.DESCRIPTION);
        this.timestamp = Long.parseLong(map.get(Constants.Validation.Task.TIME));
        this.receiverId = map.get(Constants.Validation.Task.USER_ID);

        // TODO tu sa meni roomka treba pozriet ci sa tak deje a ak ano treba to zmenit v db teda odstranit id z tej roomky / alebo dat prec idecka a pozitat to inak to je asi lepsie rieseneie
        this.roomId = map.get(Constants.Validation.Task.ROOM_ID);
    }

    public static Task editTaskCopy(Task task){
        Task t = new Task();

        t.setId(task.getId());
        t.setReceiverId(task.getReceiverId());
        t.setRoomId(task.getRoomId());
        t.setTimestamp(task.getTimestamp());
        t.setRoomId(task.getRoomId());
        t.setAuthorId(task.getAuthorId());


        return t;
    }
}
