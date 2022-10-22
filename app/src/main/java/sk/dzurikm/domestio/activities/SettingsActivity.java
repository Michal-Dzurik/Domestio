package sk.dzurikm.domestio.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import sk.dzurikm.domestio.R;

public class SettingsActivity extends AppCompatActivity {
    private Button notificationEditButton,backButton;
    private TextView notificationHint;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        notificationEditButton = findViewById(R.id.notificationEditButton);
        notificationHint = findViewById(R.id.notificationHint);
        backButton = findViewById(R.id.backButton);


        notificationEditButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction("android.settings.APP_NOTIFICATION_SETTINGS");
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                //for Android 5-7
                intent.putExtra("app_package", getPackageName());
                intent.putExtra("app_uid", getApplicationInfo().uid);

                // for Android 8 and above
                intent.putExtra("android.provider.extra.APP_PACKAGE", getPackageName());

                startActivity(intent);
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    public void recreate() {
        overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
        super.recreate();
    }
}