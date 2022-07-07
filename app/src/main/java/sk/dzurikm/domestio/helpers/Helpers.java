package sk.dzurikm.domestio.helpers;

import android.os.Handler;

public class Helpers {

    public static class Time{
        public static long seconds(double seconds){
            return (long) (seconds * 1000L);
        }

        public static void delay(Runnable runnable,long time){
            Handler handler = new Handler();
            handler.postDelayed(runnable, time);   //5 seconds
        }
    }

}
