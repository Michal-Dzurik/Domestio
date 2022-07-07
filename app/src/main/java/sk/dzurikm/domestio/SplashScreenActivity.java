package sk.dzurikm.domestio;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import sk.dzurikm.domestio.helpers.Constants;
import sk.dzurikm.domestio.helpers.Helpers;

@SuppressLint("CustomSplashScreen")
public class SplashScreenActivity extends AppCompatActivity {
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        View logo = findViewById(R.id.logo);

        sharedPreferences = getSharedPreferences(Constants.SHARED_PREFERENCES_KEY, MODE_PRIVATE);

        logo.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_in));
        logo.getAnimation().setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}

            @Override
            public void onAnimationRepeat(Animation animation) {}

            @Override
            public void onAnimationEnd(Animation animation) {
                Helpers.Time.delay(new Runnable() {
                    @Override
                    public void run() {
                        startApp();
                    }
                },Helpers.Time.seconds(1));
            }

        });

    }

    private void startApp(){
        boolean loggedIn = sharedPreferences.getBoolean(Constants.LOGGED_IN,false);

        if (loggedIn){
            // User logged in , we can go to home screen
        }

        // User not logged in, we need to log in


    }
}