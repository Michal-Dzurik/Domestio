package sk.dzurikm.domestio.activities;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.widget.LinearLayout;

import sk.dzurikm.domestio.R;
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
        Intent nextActivity;

        if (loggedIn){
            // User logged in , we can go to home screen
            nextActivity = new Intent(SplashScreenActivity.this,SplashScreenActivity.class);
            nextActivity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(nextActivity);
        }

        // User not logged in, we need to log in

        startAuth();

    }

    private void startAuth(){
        View authSection;
        authSection = findViewById(R.id.authSection);

        expand(authSection);

        // Setting up buttons
        View loginButton;

        loginButton = findViewById(R.id.loginButton);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Go to register screen
                Intent registerActivity = new Intent(SplashScreenActivity.this,RegisterActivity.class);
                startActivity(registerActivity);
            }
        });



    }

    public void expand(final View view) {
        view.measure(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        final int targetHeight = view.getMeasuredHeight();

        // Set initial height to 0 and show the view
        view.getLayoutParams().height = 0;
        view.setVisibility(View.VISIBLE);

        ValueAnimator anim = ValueAnimator.ofInt(view.getMeasuredHeight(), targetHeight);
        anim.setInterpolator(new DecelerateInterpolator());
        anim.setDuration(1000);
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
                layoutParams.height = (int) (targetHeight * animation.getAnimatedFraction());
                view.setLayoutParams(layoutParams);
            }
        });
        anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                // At the end of animation, set the height to wrap content
                // This fix is for long views that are not shown on screen
                ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
                layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                SplashScreenActivity.this.onSectionExpanded();
            }
        });
        anim.start();
    }

    private void onSectionExpanded(){
        View credits;

        credits = findViewById(R.id.credits);

        credits.setAlpha(1f);
        credits.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_in));
    }


}