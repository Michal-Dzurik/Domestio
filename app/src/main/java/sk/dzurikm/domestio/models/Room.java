package sk.dzurikm.domestio.models;

public class Room {
    Long id;
    String title,description;
    int peopleCount,tasksCount;

    public Room(Long id, String title, String description, int peopleCount, int tasksCount) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.peopleCount = peopleCount;
        this.tasksCount = tasksCount;
    }

    public Room() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
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
        return peopleCount;
    }

    public void setPeopleCount(int peopleCount) {
        this.peopleCount = peopleCount;
    }

    public int getTasksCount() {
        return tasksCount;
    }

    public void setTasksCount(int tasksCount) {
        this.tasksCount = tasksCount;
    }
}
