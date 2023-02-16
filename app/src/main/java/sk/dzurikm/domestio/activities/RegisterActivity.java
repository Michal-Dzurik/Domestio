package sk.dzurikm.domestio.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import static sk.dzurikm.domestio.helpers.Constants.Validation.EMAIL;
import static sk.dzurikm.domestio.helpers.Constants.Validation.NAME;
import static sk.dzurikm.domestio.helpers.Constants.Validation.PASSWORD;
import static sk.dzurikm.domestio.helpers.Constants.Validation.PASSWORD_REPEAT;
import static sk.dzurikm.domestio.helpers.Constants.Validation.PASSWORD_REPEAT_DELIMITER;
import static sk.dzurikm.domestio.helpers.Helpers.Views.getTextOfView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;

import sk.dzurikm.domestio.R;
import sk.dzurikm.domestio.helpers.Constants;
import sk.dzurikm.domestio.helpers.DatabaseHelper;
import sk.dzurikm.domestio.helpers.Helpers;
import sk.dzurikm.domestio.views.alerts.InputAlert;

public class RegisterActivity extends AppCompatActivity {
    // Views
    private EditText email,password,passwordRepeat;
    private Button registerButton,loginButton;

    // Helpers
    DatabaseHelper databaseHelper;

    // validation
    Helpers.Validation validation;

    // Alerts
    InputAlert changeNameAlert;

    SharedPreferences sharedPreferences;
    SharedPreferences.Editor sharedPreferencesEditor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Views
        email = (EditText) findViewById(R.id.emailInput);
        password = (EditText) findViewById(R.id.passwordInput);
        passwordRepeat = (EditText) findViewById(R.id.passwordRepeatInput);
        registerButton = (Button) findViewById(R.id.registerButton);
        loginButton = (Button) findViewById(R.id.loginButton);

        // Helpers
        databaseHelper = new DatabaseHelper();

        // Validation
        validation = Helpers.Validation.getInstance(RegisterActivity.this);

        // Shared preferences
        sharedPreferences = getSharedPreferences(Constants.SHARED_PREFERENCES_KEY,MODE_PRIVATE);
        sharedPreferencesEditor = sharedPreferences.edit();

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

        // Setup alerts
        changeNameAlert = new InputAlert(RegisterActivity.this);
        changeNameAlert.setTitle(getString(R.string.your_name));
        changeNameAlert.setDescription(getString(R.string.enter_name));
        changeNameAlert.setPositiveButtonText(RegisterActivity.this.getString(R.string.change));
        changeNameAlert.setHint("John Doe");
        changeNameAlert.setRequired(true);
        changeNameAlert.setPositiveButtonOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = changeNameAlert.getInput().getText().toString();
                if (nameValid(name)){
                    setUserName(name);
                }

            }
        });
        changeNameAlert.setCanceledOnTouchOutside(false);
        changeNameAlert.setCancelable(false);

    }

    private void register() {
        Helpers.Views.buttonDisabled(registerButton,true);
        databaseHelper.register(RegisterActivity.this,getTextOfView(email),getTextOfView(password),new DatabaseHelper.OnRegisterListener() {
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

        changeNameAlert.show();
    }

    private void setUserName(String name){
        databaseHelper.updateUserName(name, new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                sharedPreferencesEditor.putString("user-name",name);
                sharedPreferencesEditor.commit();
                openHomeActivity();
            }
        }, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Helpers.Toast.somethingWentWrong(RegisterActivity.this);
            }
        });
    }

    private boolean registrationDataValid(){
        HashMap<String,String> map = new HashMap<>();
        map.put(EMAIL,getTextOfView(email));
        map.put(PASSWORD,getTextOfView(password));
        map.put(PASSWORD_REPEAT, getTextOfView(password) + PASSWORD_REPEAT_DELIMITER + getTextOfView(passwordRepeat));

        ArrayList<String> errors = validation.validate(map);

        if (errors != null){
            // print errors
            Toast.makeText(RegisterActivity.this,errors.get(0),Toast.LENGTH_SHORT).show();
        }

        return errors == null;
    }

    private boolean nameValid(String name){
        ArrayList<String> errors = validation.validate(NAME,name);

        if (errors != null){
            // print errors
            Toast.makeText(RegisterActivity.this,errors.get(0),Toast.LENGTH_SHORT).show();
        }

        return errors == null;
    }

    private void openHomeActivity(){
        Intent nextActivity = new Intent(RegisterActivity.this,HomeActivity.class);

        nextActivity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(nextActivity);
    }

}