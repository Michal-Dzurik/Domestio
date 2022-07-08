package sk.dzurikm.domestio.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import sk.dzurikm.domestio.R;
import sk.dzurikm.domestio.helpers.Helpers;

public class RegisterActivity extends AppCompatActivity {
    private EditText name,email,password,passwordRepeat;
    private Button registerButton,loginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        name = (EditText) findViewById(R.id.nameInput);
        email = (EditText) findViewById(R.id.emailInput);
        password = (EditText) findViewById(R.id.passwordInput);
        passwordRepeat = (EditText) findViewById(R.id.passwordRepeatInput);

        registerButton = (Button) findViewById(R.id.registerButton);
        loginButton = (Button) findViewById(R.id.loginButton);

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Validate form data
                if (registrationDataValid()){
                    // Register
                }
                else {
                    // Show Toast with error
                    Toast.makeText(RegisterActivity.this, RegisterActivity.this.getString(R.string.informations_are_not_valid),Toast.LENGTH_SHORT).show();
                }
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Go to login
                Intent loginActivity = new Intent(RegisterActivity.this,LoginActivity.class);

                startActivity(loginActivity);
            }
        });

    }

    private boolean registrationDataValid(){
        return Helpers.Validation.email(getText(email)) &&
                Helpers.Validation.name(getText(name)) &&
                Helpers.Validation.passwords(getText(password),getText(passwordRepeat));
    }

    private String getText(TextView view) {
        return view.getText().toString();
    }
}