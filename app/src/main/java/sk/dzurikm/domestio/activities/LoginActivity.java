package sk.dzurikm.domestio.activities;

import static sk.dzurikm.domestio.helpers.Constants.Validation.EMAIL;
import static sk.dzurikm.domestio.helpers.Constants.Validation.PASSWORD;
import static sk.dzurikm.domestio.helpers.Helpers.Views.getTextOfView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import sk.dzurikm.domestio.R;
import sk.dzurikm.domestio.helpers.Constants;
import sk.dzurikm.domestio.helpers.DatabaseHelper;
import sk.dzurikm.domestio.helpers.Helpers;
import sk.dzurikm.domestio.views.alerts.InfoAlert;
import sk.dzurikm.domestio.views.alerts.InputAlert;

public class LoginActivity extends AppCompatActivity {
    // Views
    private EditText email,password;
    private Button backButton,loginButton;
    private TextView forgotPasswordButton;

    // Firebase
    private FirebaseAuth mAuth;
    private DatabaseHelper databaseHelper;

    // Validation
    private Helpers.Validation validation;

    SharedPreferences sharedPreferences;
    SharedPreferences.Editor sharedPreferencesEditor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Shared preferences
        sharedPreferences = getSharedPreferences(Constants.SHARED_PREFERENCES_KEY,MODE_PRIVATE);
        sharedPreferencesEditor = sharedPreferences.edit();

        // Views
        email = (EditText) findViewById(R.id.emailInput);
        password = (EditText) findViewById(R.id.passwordInput);
        backButton = (Button) findViewById(R.id.backButton);
        loginButton = (Button) findViewById(R.id.loginButton);
        forgotPasswordButton = (TextView) findViewById(R.id.forgotPasswordButton);

        // helpers
        databaseHelper = new DatabaseHelper();

        // db
        mAuth = FirebaseAuth.getInstance();

        // validation
        validation = Helpers.Validation.getInstance(LoginActivity.this);

        InfoAlert textAlert = new InfoAlert(LoginActivity.this);
        textAlert.setTitle(getString(R.string.warning));
        textAlert.setDescription(getString(R.string.spam_warrning));

        // Setting up listeners
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // go back
                finish();
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Validate form data
                if (loginDataValid()){
                    login();
                }
            }
        });

        forgotPasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputAlert alert = new InputAlert(LoginActivity.this);
                alert.setTitle(getString(R.string.enter_your_email));
                alert.setHint("johndoe@gmail.com");
                alert.setPositiveButtonOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String email = alert.getInput().getText().toString().trim();

                        Helpers.Validation validation = new Helpers.Validation(LoginActivity.this);
                        ArrayList<String> errors = validation.validate(EMAIL,email);
                        System.out.println(email + "LMao");

                        if (errors == null){
                            databaseHelper.resetPassword(LoginActivity.this,email);
                            alert.dismiss();
                            textAlert.show();
                        }
                        else Toast.makeText(LoginActivity.this,errors.get(0),Toast.LENGTH_SHORT).show();

                    }
                });


                alert.setNegativeButtonOnClickListener(v1 -> alert.dismiss());
                alert.show();
            }
        });

    }

    private void login(){
        String emailContent,passwordContent;

        emailContent = getTextOfView(email);
        passwordContent = getTextOfView(password);

        databaseHelper.login(LoginActivity.this,emailContent,passwordContent,new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    // Sign in success, update UI with the signed-in user's information
                    onLoginSucceed();

                } else {
                    // If sign in fails, display a message to the user.
                    Log.w("Firebase Auth", "signInUserWithEmail:failure", task.getException());
                    Toast.makeText(LoginActivity.this, LoginActivity.this.getString(R.string.authentication_failed),
                            Toast.LENGTH_SHORT).show();

                }
            }
        });
    }

    private void onLoginSucceed(){
        Log.d("Firebase Auth", "signInUserWithEmail:success");

        databaseHelper.getUserFromDatabase(mAuth.getUid(), new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()){
                    Map<String, Object> data = task.getResult().getData();
                    String name = (String) data.get(Constants.Firebase.User.FIELD_NAME);

                    Toast.makeText(LoginActivity.this,
                            LoginActivity.this.getString(R.string.you_have_been_logged_in_successfully),
                            Toast.LENGTH_SHORT).show();

                    sharedPreferencesEditor.putString("user-name",name);
                    sharedPreferencesEditor.commit();

                    Intent nextActivity = new Intent(LoginActivity.this,HomeActivity.class);
                    nextActivity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(nextActivity);

                }
            }
        });

    }

    private boolean loginDataValid(){
        HashMap<String,String> map = new HashMap<>();
        map.put(EMAIL,getTextOfView(email));
        map.put(PASSWORD,getTextOfView(password));

        ArrayList<String> errors = validation.validate(map);

        if (errors != null){
            // print errors
            Toast.makeText(LoginActivity.this,errors.get(0),Toast.LENGTH_SHORT).show();
        }

        return errors == null;
    }


}