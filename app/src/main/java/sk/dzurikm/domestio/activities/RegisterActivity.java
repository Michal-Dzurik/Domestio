package sk.dzurikm.domestio.activities;

import androidx.appcompat.app.AppCompatActivity;

import static sk.dzurikm.domestio.helpers.Constants.Validation.EMAIL;
import static sk.dzurikm.domestio.helpers.Constants.Validation.NAME;
import static sk.dzurikm.domestio.helpers.Constants.Validation.PASSWORD;
import static sk.dzurikm.domestio.helpers.Constants.Validation.PASSWORD_REPEAT;
import static sk.dzurikm.domestio.helpers.Constants.Validation.PASSWORD_REPEAT_DELIMITER;
import static sk.dzurikm.domestio.helpers.Helpers.Views.getTextOfView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;

import sk.dzurikm.domestio.R;
import sk.dzurikm.domestio.helpers.DatabaseHelper;
import sk.dzurikm.domestio.helpers.Helpers;

public class RegisterActivity extends AppCompatActivity {
    // Views
    private EditText name,email,password,passwordRepeat;
    private Button registerButton,loginButton;

    // Helpers
    DatabaseHelper databaseHelper;

    // validation
    Helpers.Validation validation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Views
        name = (EditText) findViewById(R.id.nameInput);
        email = (EditText) findViewById(R.id.emailInput);
        password = (EditText) findViewById(R.id.passwordInput);
        passwordRepeat = (EditText) findViewById(R.id.passwordRepeatInput);
        registerButton = (Button) findViewById(R.id.registerButton);
        loginButton = (Button) findViewById(R.id.loginButton);

        // Helpers
        databaseHelper = new DatabaseHelper();

        // Validation
        validation = Helpers.Validation.getInstance(RegisterActivity.this);


        // Setting up listeners
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Validate form data
                if (registrationDataValid()){
                    // Register
                    register();
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

    private void register() {
        databaseHelper.register(RegisterActivity.this,getTextOfView(name),getTextOfView(email),getTextOfView(password),new DatabaseHelper.OnRegisterListener() {
            @Override
            public void onRegisterSuccess() {
                onRegistrationSucceed();
            }

            @Override
            public void onRegisterFailed() {
                Helpers.Toast.somethingWentWrong(RegisterActivity.this);

            }
        });
    }

    private void onRegistrationSucceed(){
        Toast.makeText(RegisterActivity.this,
                RegisterActivity.this.getString(R.string.you_have_been_registered_successfully),
                Toast.LENGTH_SHORT).show();

        Intent nextActivity = new Intent(RegisterActivity.this,HomeActivity.class);
        nextActivity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(nextActivity);
    }

    private boolean registrationDataValid(){
        HashMap<String,String> map = new HashMap<>();
        map.put(EMAIL,getTextOfView(email));
        map.put(PASSWORD,getTextOfView(password));
        map.put(PASSWORD_REPEAT, getTextOfView(password) + PASSWORD_REPEAT_DELIMITER + getTextOfView(passwordRepeat));
        map.put(NAME,getTextOfView(name));

        ArrayList<String> errors = validation.validate(map);

        if (errors != null){
            // print errors
            Toast.makeText(RegisterActivity.this,errors.get(0),Toast.LENGTH_SHORT).show();
        }

        return errors == null;
    }

}