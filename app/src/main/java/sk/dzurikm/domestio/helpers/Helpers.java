package sk.dzurikm.domestio.helpers;

import android.os.Handler;
import android.util.Log;

import java.util.Locale;

public class Helpers {

    public static int positiveValueOrDefault(int value,int defaultValue){
        if (value < 0) return defaultValue;

        return value;
    }

    public static String stringValueOrDefault(String value,String defaultValue){
        if (value == null || value.trim().equals("")) return defaultValue;

        return value;
    }

    public static class Time{
        public static long seconds(double seconds){
            return (long) (seconds * 1000L);
        }

        public static void delay(Runnable runnable,long time){
            Handler handler = new Handler();
            handler.postDelayed(runnable, time);   //5 seconds
        }
    }

    public static class Validation{
        public static boolean email(String email){
            email = email.trim();

            if (email.equals("")) return false;

            String ePattern = "^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\])|(([a-zA-Z\\-0-9]+\\.)+[a-zA-Z]{2,}))$";
            java.util.regex.Pattern p = java.util.regex.Pattern.compile(ePattern);
            java.util.regex.Matcher m = p.matcher(email);
            Log.i("Validation", "Password match "  + String.valueOf(m.matches()));
            return m.matches();
        }

        public static boolean name(String name){
            name = name.trim();

            if (name.equals("")) return false;

            String[] names = name.split(" ");

            return names.length >= 2;
        }

        public static boolean passwords(String password,String passwordRepeated){
            password = password.trim();
            passwordRepeated = passwordRepeated.trim();

            if (password.equals("") || passwordRepeated.equals("")) {
                Log.i("Validation","password not correct");
                return false;
            }

            return password.equals(passwordRepeated);

        }

        public static boolean password(String password){
            password = password.trim();

            if (password.equals("")) return false;

            return true;

        }

    }

}
