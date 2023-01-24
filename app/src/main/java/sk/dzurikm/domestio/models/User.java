package sk.dzurikm.domestio.models;

import static sk.dzurikm.domestio.helpers.Constants.Firebase.User.*;

import java.io.Serializable;
import java.util.Map;

public class User implements Serializable {
    private String id,name,email;

    public User(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public User() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void cast(Map<String, Object> data){
        this.id = (String) data.get(FIELD_ID);
        this.name = (String) data.get(FIELD_NAME);
        this.email = (String) data.get(FIELD_EMAIL);
    }

    public void update(User user) {
        if (user.getEmail() != null) this.email = user.getEmail();
        if (user.getName() != null) this.name = user.getName();
    }

    @Override
    public String toString() {
        return "User{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}
