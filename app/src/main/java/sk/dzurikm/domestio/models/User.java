package sk.dzurikm.domestio.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Map;

public class User implements Serializable {
    private String id,name;

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


    public void cast(String id, Map<String, Object> data){
        this.id = id;
        this.name = (String) data.get("name");
    }

    @Override
    public String toString() {
        return "User{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
