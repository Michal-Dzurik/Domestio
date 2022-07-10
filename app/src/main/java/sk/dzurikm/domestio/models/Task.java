package sk.dzurikm.domestio.models;

public class Task {
    String heading,description,time,owner,room;

    public Task(String task, String description, String time, String owner, String room) {
        this.heading = task;
        this.description = description;
        this.time = time;
        this.owner = owner;
        this.room = room;
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

    public String getOwner() {
        return owner;
    }

    public String getRoom() {
        return room;
    }
}
