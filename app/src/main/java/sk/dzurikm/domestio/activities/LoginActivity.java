package sk.dzurikm.domestio.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import sk.dzurikm.domestio.R;
import sk.dzurikm.domestio.helpers.Helpers;

public class LoginActivity extends AppCompatActivity {
    private EditText email,password;
    private Button backButton,loginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        email = (EditText) findViewById(R.id.emailInput);
        password = (EditText) findViewById(R.id.passwordInput);

        backButton = (Button) findViewById(R.id.backButton);
        loginButton = (Button) findViewById(R.id.loginButton);

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
                    // Login
                    FirebaseAuth mAuth = FirebaseAuth.getInstance();
                    mAuth.signInWithEmailAndPassword(getText(email), getText(password))
                            .addOnCompleteListener(LoginActivity.this, (OnCompleteListener<AuthResult>) task -> {
                                if (task.isSuccessful()) {
                                    // Sign in success, update UI with the signed-in user's information
                                    Log.d("Firebase Auth", "signInUserWithEmail:success");
                                    FirebaseUser user = mAuth.getCurrentUser();

                                } else {
                                    // If sign in fails, display a message to the user.
                                    Log.w("Firebase Auth", "signInUserWithEmail:failure", task.getException());
                                    Toast.makeText(LoginActivity.this, LoginActivity.this.getString(R.string.authentication_failed),
                                            Toast.LENGTH_SHORT).show();

                                }


                            });
                }
                else {
                    // Show Toast with error
                    Toast.makeText(LoginActivity.this, LoginActivity.this.getString(R.string.informations_are_not_valid),Toast.LENGTH_SHORT).show();

                }
            }
        });
    }

    private boolean loginDataValid(){
        return Helpers.Validation.email(getText(email)) &&
                Helpers.Validation.password(getText(password));
    }

    private String getText(TextView view) {
        return view.getText().toString();
    }
}