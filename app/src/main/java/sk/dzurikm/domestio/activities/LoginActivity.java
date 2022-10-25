package sk.dzurikm.domestio.activities;

import static sk.dzurikm.domestio.helpers.Helpers.Views.getTextOfView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import sk.dzurikm.domestio.R;
import sk.dzurikm.domestio.helpers.DatabaseHelper;
import sk.dzurikm.domestio.helpers.Helpers;

public class LoginActivity extends AppCompatActivity {
    // Views
    private EditText email,password;
    private Button backButton,loginButton;
    private FirebaseAuth mAuth;

    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Views
        email = (EditText) findViewById(R.id.emailInput);
        password = (EditText) findViewById(R.id.passwordInput);
        backButton = (Button) findViewById(R.id.backButton);
        loginButton = (Button) findViewById(R.id.loginButton);

        // helpers
        databaseHelper = new DatabaseHelper();

        // db
        mAuth = FirebaseAuth.getInstance();

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
                else {
                    // Show Toast with error
                    Toast.makeText(LoginActivity.this, LoginActivity.this.getString(R.string.informations_are_not_valid),Toast.LENGTH_SHORT).show();

                }
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
        FirebaseUser user = mAuth.getCurrentUser();
        Toast.makeText(LoginActivity.this,
                LoginActivity.this.getString(R.string.you_have_been_logged_in_successfully),
                Toast.LENGTH_SHORT).show();

        Intent nextActivity = new Intent(LoginActivity.this,HomeActivity.class);
        nextActivity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(nextActivity);
    }

    private boolean loginDataValid(){
        return Helpers.Validation.email(getTextOfView(email)) &&
                Helpers.Validation.password(getTextOfView(password));
    }


}