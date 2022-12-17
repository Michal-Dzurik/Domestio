package sk.dzurikm.domestio.helpers;

import java.util.ArrayList;

import sk.dzurikm.domestio.models.Room;
import sk.dzurikm.domestio.models.Task;
import sk.dzurikm.domestio.models.User;

public class DataStorage {
    public static ArrayList<User> users;
    public static ArrayList<Task> tasks;
    public static ArrayList<Room> rooms;

    public static boolean connected = true;

}
