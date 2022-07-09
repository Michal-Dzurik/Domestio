package sk.dzurikm.domestio.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

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
                    FirebaseAuth mAuth = FirebaseAuth.getInstance();

                    mAuth.createUserWithEmailAndPassword(getText(email), getText(password))
                            .addOnCompleteListener(RegisterActivity.this, (OnCompleteListener<AuthResult>) task -> {
                                if (task.isSuccessful()) {
                                    // Sign in success, update UI with the signed-in user's information
                                    Log.d("Firebase Auth", "createUserWithEmail:success");
                                    FirebaseUser user = mAuth.getCurrentUser();

                                    UserProfileChangeRequest profileUpdate = new UserProfileChangeRequest.Builder()
                                            .setDisplayName(getText(name)).build();
                                    if (user != null) {
                                        user.updateProfile(profileUpdate);
                                    }

                                    onRegistrationSucceed();


                                } else {
                                    // If sign in fails, display a message to the user.
                                    Log.w("Firebase Auth", "createUserWithEmail:failure", task.getException());
                                    Toast.makeText(RegisterActivity.this,
                                            RegisterActivity.this.getString(R.string.authentication_failed),
                                            Toast.LENGTH_SHORT).show();

                                }


                            });
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

    private void onRegistrationSucceed(){
        Toast.makeText(RegisterActivity.this,
                RegisterActivity.this.getString(R.string.you_have_been_registered_successfully),
                Toast.LENGTH_SHORT).show();

        Intent nextActivity = new Intent(RegisterActivity.this,HomeActivity.class);
        nextActivity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(nextActivity);
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