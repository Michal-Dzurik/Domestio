package sk.dzurikm.domestio.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import sk.dzurikm.domestio.R;

public class AboutMeActivity extends AppCompatActivity {
    private ImageButton facebook,linkedin,github,backButton;
    private LinearLayout supportMe;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_me);

        facebook = findViewById(R.id.facebook);
        linkedin = findViewById(R.id.linkedin);
        github = findViewById(R.id.github);
        backButton = findViewById(R.id.backButton);

        supportMe = findViewById(R.id.supportMeButton);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        facebook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.facebook.com/profile.php?id=100009386056819"));
                startActivity(browserIntent);
            }
        });

        linkedin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.linkedin.com/in/michal-dzurik/"));
                startActivity(browserIntent);
            }
        });

        github.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/Michal-Dzurik"));
                startActivity(browserIntent);
            }
        });

        supportMe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://buymeacoffee.com/dzurikm"));
                startActivity(browserIntent);
            }
        });
    }
}