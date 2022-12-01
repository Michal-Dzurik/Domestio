package sk.dzurikm.domestio.helpers;

import static sk.dzurikm.domestio.helpers.Constants.Validation.EMAIL;
import static sk.dzurikm.domestio.helpers.Constants.Validation.NAME;
import static sk.dzurikm.domestio.helpers.Constants.Validation.PASS_MIN_LENGTH;
import static sk.dzurikm.domestio.helpers.Constants.Validation.PASSWORD;
import static sk.dzurikm.domestio.helpers.Constants.Validation.PASSWORD_REPEAT;
import static sk.dzurikm.domestio.helpers.Constants.Validation.PASSWORD_REPEAT_DELIMITER;
import static sk.dzurikm.domestio.helpers.Constants.Validation.Task.ROOM_ID;
import static sk.dzurikm.domestio.helpers.Constants.Validation.Task.TIME;
import static sk.dzurikm.domestio.helpers.Constants.Validation.Task.USER_ID;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.Timestamp;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import sk.dzurikm.domestio.R;
import sk.dzurikm.domestio.models.Room;
import sk.dzurikm.domestio.models.User;
import sk.dzurikm.domestio.views.alerts.InputAlert;
import sk.dzurikm.domestio.views.alerts.PasswordChangeAlert;

public class Helpers {

    public static String limitLetters(String str,int letterNumb){
        if (str.length() <= letterNumb) return str;

        String[] arr = str.split(" ");

        if (arr[0].length() <= letterNumb) return arr[0];

        StringBuilder limitedStr = new StringBuilder();
        for (int i = 0; i < letterNumb; i++) {
            limitedStr.append(str.charAt(i));
        }

        return limitedStr.toString() + "…";
    }

    public static int positiveValueOrDefault(int value,int defaultValue){
        if (value < 0) return defaultValue;

        return value;
    }

    public static String stringValueOrDefault(String value,String defaultValue){
        if (value == null || value.trim().equals("")) return defaultValue;

        return value;
    }

    public static String fromIntToColor(@ColorInt int color){
        return String.format("#%06X", (0xFFFFFF & color));
    }

    public static String firstUppercase(String str){
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    public static class Time{
        public static long seconds(double seconds){
            return (long) (seconds * 1000L);
        }

        public static void delay(Runnable runnable,long time){
            Handler handler = new Handler();
            handler.postDelayed(runnable, time);   //5 seconds
        }

        public static String getTimeDate(long timestamp){
            try{
                @SuppressLint("SimpleDateFormat") SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
                Date netDate = (new Date(timestamp * 1000));
                return dateFormat.format(netDate);
            } catch(Exception e) {
                return "date";
            }
        }

        public static Timestamp getTimeDateForDB(long timeStamp){
            return new Timestamp(new Date(timeStamp));
        }
    }

    public static class Validation{
        private Context context;


        public Validation(Context context) {
            this.context = context;
        }

        public static Validation getInstance(Context context){
            return new Validation(context);
        }

        public ArrayList<String> validate(HashMap<String,String> data){
            ArrayList<String> errors = new ArrayList<>();

            for (Map.Entry<String, String> set :
                    data.entrySet()) {

                String value = set.getValue();
                String validation = set.getKey();

                String validationError = null;

                if (validation == null || value == null || validation.trim().equals("") || value.trim().equals("")) {
                    errors.add(context.getString(R.string.missing_informations));

                    break;
                }
                else validationError = validate(validation,value);

                if (validationError != null) errors.add(validationError);
            }

            System.out.println(errors);
            return errors.size() == 0 ? null : errors;
        }

        private String validate(String validation,String value){
            System.out.println(validation);
            switch (validation){
                case EMAIL:
                    return email(value);
                case PASSWORD:
                    return password(value);
                case PASSWORD_REPEAT:
                    return passwords(value);
                case NAME:
                    return name(value);
                case TIME:
                    return (value != null && !value.trim().equals("") && java.lang.Integer.parseInt(value) >= 0) ? null : context.getString(R.string.you_didnt_set_the_time);
                case USER_ID:
                    return (value != null && !value.trim().equals("")) ? null : context.getString(R.string.user_not_selected);
                case ROOM_ID:
                    return (value != null && !value.trim().equals("")) ? null : context.getString(R.string.room_not_selected);

            }

            return null;
        }

        public String email(String email){
            email = email.trim();

            String ePattern = "^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\])|(([a-zA-Z\\-0-9]+\\.)+[a-zA-Z]{2,}))$";
            java.util.regex.Pattern p = java.util.regex.Pattern.compile(ePattern);
            java.util.regex.Matcher m = p.matcher(email);

            return m.matches() ? null : context.getString(R.string.email_is_not_valid) ;
        }


        public String name(String name){
            name = name.trim();

            String[] names = name.split(" ");

            return names.length >= 2 ? null : context.getString(R.string.name_is_not_valid);
        }


        public String passwords(String password){
            password = password.trim();
            String[] passArr = password.split(PASSWORD_REPEAT_DELIMITER);

            String passErrors = password(passArr[0]);
            if (passErrors == null)
                return passArr[0].equals(passArr[1]) ? null : "Passwords do not match";
            else return passErrors;

        }


        public String password(String password){
            password = password.trim();

            if (password.length() < PASS_MIN_LENGTH) return context.getString(R.string.password_is_too_short) + "( min. " + PASS_MIN_LENGTH + " " + context.getString(R.string.characters) + " )"  ;

            return null;

        }

    }

    public static class List{
        public static void addUnique(ArrayList<String> list,ArrayList<String> add){

            if (add.size() < 1) return;

            for (int i = 0; i < add.size(); i++) {
                String itemToAdd = add.get(i);
                if (!list.contains(itemToAdd)){
                    list.add(itemToAdd);
                }
            }
        }
    }

    public static class Colors{
        public static int gettextContrastColor(int color,int lightColor, int darkColor) {
            int r = Color.red(color); // hexToR
            int g = Color.green(color); // hexToG
            int b = Color.blue(color); // hexToB
            return (((r * 0.299) + (g * 0.587) + (b * 0.114)) > 186) ?
                    darkColor : lightColor;
        }

        public static String fromIntToColor(int color){
            return String.format("#%06X", (0xFFFFFF & color));
        }

        public static String addOpacity(String hex,String opacity){
            StringBuilder color = new StringBuilder(opacity);
            for (int i = 1; i < hex.length(); i++) {
                color.append(hex.charAt(i));
            }

            return "#" + color;
        }

    }

    public static class Integer{
        public static String formatNumberOnTwoSpaces(int numb){
            String numbStr = numb + "";

            if (numbStr.length() < 2) return "0" + numb;


            return numbStr;

        }
    }

    public static class Views{
        public static String getTextOfView(TextView view) {
            return view.getText().toString();
        }

        public static void waitAndShow(InputAlert alert, int delay){
            Helpers.Time.delay(new Runnable() {
                @Override
                public void run() {
                    alert.show();
                }
            },delay);
        }

        public static void waitAndShow(PasswordChangeAlert alert, int delay){
            Helpers.Time.delay(new Runnable() {
                @Override
                public void run() {
                    alert.show();
                }
            },delay);
        }
    }

    public static class Notifications{

        public static NotificationCompat.Builder create(Context context, String title, String text, PendingIntent clickIntent){
            createNotificationChannel(context);
            return new NotificationCompat.Builder(context, "DomestioNotifications")
                    .setSmallIcon(R.drawable.logo)
                    .setContentTitle(title)
                    .setContentText(text)
                    .setContentIntent(clickIntent)
                    .setPriority(NotificationCompat.PRIORITY_HIGH);
        }

        private static void createNotificationChannel(Context context) {
            // Create the NotificationChannel, but only on API 26+ because
            // the NotificationChannel class is new and not in the support library
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                CharSequence name = "DomestioNotifications";
                String description = "Domestio Notification channel";
                int importance = NotificationManager.IMPORTANCE_HIGH;
                NotificationChannel channel = new NotificationChannel("DomestioNotifications", name, importance);
                channel.setDescription(description);
                // Register the channel with the system; you can't change the importance
                // or other notification behaviors after this
                NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
                notificationManager.createNotificationChannel(channel);
            }
        }

        public static void show(Context context,int id,NotificationCompat.Builder notification){
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

            // notificationId is a unique int for each notification that you must define
            notificationManager.notify(id, notification.build());
        }

    }

    public static class DataSet{
        public static String getAuthorName(ArrayList<User> usersData, String uid){
            for (int i = 0; i < usersData.size(); i++) {
                if (usersData.get(i).getId().equals(uid)) return usersData.get(i).getName();
            }

            return null;
        }

        public static String getRoomTitle(ArrayList<Room> roomData, String id){
            for (int i = 0; i < roomData.size(); i++) {
                if (roomData.get(i).getId().equals(id)) return roomData.get(i).getTitle();
            }

            return null;
        }

        public static HashMap<String,String> getRoomInfo(ArrayList<Room> roomData, String id, String[] info){
            for (int i = 0; i < roomData.size(); i++) {
                Room room = roomData.get(i);
                if (room.getId().equals(id)) {
                    HashMap<String,String> map = new HashMap<>();
                    for (int j = 0; j < info.length; j++) {
                        String key = info[j];

                        // Dynamic getters construction
                        try {
                            String methodName = "get" + firstUppercase(key);
                            Method method = Room.class.getMethod(methodName);
                            map.put(key,(String) method.invoke(room));
                        } catch (NoSuchMethodException e) {
                            e.printStackTrace();
                        } catch (InvocationTargetException e) {
                            e.printStackTrace();
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }

                    }

                    return map;
                }
            }

            return null;
        }

        public static Room getRoomById(ArrayList<Room> list,String id){
            for (int i = 0; i < list.size(); i++) {
                if (list.get(i).getId().equals(id)) return list.get(i);
            }

            return null;
        }

        public static User getUserById(ArrayList<User> list,String id){
            for (int i = 0; i < list.size(); i++) {
                if (list.get(i).getId().equals(id)) return list.get(i);
            }

            return null;
        }

    }

    public static String getAuthorName(ArrayList<User> usersData, String uid){
        for (int i = 0; i < usersData.size(); i++) {
            if (usersData.get(i).equals(uid)) return usersData.get(i).getName();
        }

        return null;
    }

}
