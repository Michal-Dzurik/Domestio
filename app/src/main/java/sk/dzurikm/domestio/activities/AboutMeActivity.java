package sk.dzurikm.domestio.activities;

import static sk.dzurikm.domestio.helpers.Constants.Url.*;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import sk.dzurikm.domestio.R;

public class AboutMeActivity extends AppCompatActivity {
    // Views
    private ImageButton facebook,linkedin,github,backButton;
    private LinearLayout supportMe;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_me);

        // Views
        facebook = findViewById(R.id.facebook);
        linkedin = findViewById(R.id.linkedin);
        github = findViewById(R.id.github);
        backButton = findViewById(R.id.backButton);
        supportMe = findViewById(R.id.supportMeButton);

        // Setting up listeners
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        facebook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(FACEBOOK));
                startActivity(browserIntent);
            }
        });

        linkedin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(LINKED_ID));
                startActivity(browserIntent);
            }
        });

        github.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(GIT_HUB));
                startActivity(browserIntent);
            }
        });

        supportMe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(BUY_ME_A_COFFEE));
                startActivity(browserIntent);
            }
        });
    }
}