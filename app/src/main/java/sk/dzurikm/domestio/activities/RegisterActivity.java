package sk.dzurikm.domestio.activities;

import androidx.appcompat.app.AppCompatActivity;
import static sk.dzurikm.domestio.helpers.Constants.Firebase.DOCUMENT_USERS;
import static sk.dzurikm.domestio.helpers.Helpers.Views.getTextOfView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

import sk.dzurikm.domestio.R;
import sk.dzurikm.domestio.helpers.DatabaseHelper;
import sk.dzurikm.domestio.helpers.Helpers;

public class RegisterActivity extends AppCompatActivity {
    // Views
    private EditText name,email,password,passwordRepeat;
    private Button registerButton,loginButton;

    // Helpers
    DatabaseHelper databaseHelper;

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


        // Setting up listeners
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Validate form data
                if (registrationDataValid()){
                    // Register
                    register();
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

    private void register() {
        databaseHelper.register(RegisterActivity.this,getTextOfView(name),getTextOfView(email),getTextOfView(password),new DatabaseHelper.OnRegisterListener() {
            @Override
            public void onRegisterSuccess() {
                onRegistrationSucceed();
            }

            @Override
            public void onRegisterFailed() {
                Toast.makeText(RegisterActivity.this, RegisterActivity.this.getString(R.string.something_went_wrong),Toast.LENGTH_SHORT).show();

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
        return Helpers.Validation.email(getTextOfView(email)) &&
                Helpers.Validation.name(getTextOfView(name)) &&
                Helpers.Validation.passwords(getTextOfView(password),getTextOfView(passwordRepeat));
    }

}