package sk.dzurikm.domestio.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Objects;

import sk.dzurikm.domestio.R;
import sk.dzurikm.domestio.helpers.Helpers;
import sk.dzurikm.domestio.views.alerts.Alert;
import sk.dzurikm.domestio.views.alerts.InputAlert;
import sk.dzurikm.domestio.views.alerts.PasswordChangeAlert;
import sk.dzurikm.domestio.views.alerts.ReauthenticateAlert;

public class ProfileActivity extends AppCompatActivity {
    private final String DOCUMENT_USERS = "Users";

    private ImageButton backButton,leaveButton;
    private TextView userNameText,userNameHint,userEmailHint;
    private String userEmail,userName;
    private Button nameEditButton,emailEditButton,passwordEditButton;
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private FirebaseUser user;

    private boolean authenticated;
    private Alert signOutAlert;
    private InputAlert editNameAlert,editEmailAlert;
    private ReauthenticateAlert reauthenticateAlert;
    private PasswordChangeAlert passwordChangeAlert;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        authenticated = false;

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        db = FirebaseFirestore.getInstance();

        backButton = findViewById(R.id.backButton);
        leaveButton = findViewById(R.id.leaveButton);

        userNameText = findViewById(R.id.userNameTextView);
        userEmailHint = findViewById(R.id.userEmailHint);
        userNameHint = findViewById(R.id.userNameHint);

        nameEditButton = findViewById(R.id.nameEditButton);
        emailEditButton = findViewById(R.id.emailEditButton);
        passwordEditButton = findViewById(R.id.passwordEditButton);

        if (user != null) {
            // Name, email address, and profile photo Url
            userName = user.getDisplayName();
            userEmail = user.getEmail();
            //Uri photoUrl = user.getPhotoUrl();

            userNameText.setText(userName);
            userNameHint.setText(userName);
            userEmailHint.setText(userEmail);
        }

        setUpAlerts();


        // Setting up listeners
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        leaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signOutAlert.show();

            }
        });

        // Setting up edit buttons
        nameEditButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Showing Alerts
                editNameAlert.show();

            }
        });

        emailEditButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Showing Alerts
                if (authenticated){
                    editEmailAlert.show();
                }
                else {
                    reauthenticateAlert.setCompleteOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                Toast.makeText(ProfileActivity.this, ProfileActivity.this.getString(R.string.you_have_been_logged_in_successfully),Toast.LENGTH_SHORT).show();
                                reauthenticateAlert.dismiss();
                                waitAndShow(editEmailAlert,200);
                                authenticated = true;
                            }
                            else somethingWentWrongMessage();
                        }
                    });
                    reauthenticateAlert.show();
                }
            }
        });

        passwordEditButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Showing Alerts
                if (authenticated){
                    passwordChangeAlert.show();
                }
                else {
                    reauthenticateAlert.setCompleteOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                Toast.makeText(ProfileActivity.this, ProfileActivity.this.getString(R.string.you_have_been_logged_in_successfully),Toast.LENGTH_SHORT).show();
                                reauthenticateAlert.dismiss();
                                waitAndShow(passwordChangeAlert,1000);
                                authenticated = true;
                            }
                            else somethingWentWrongMessage();
                        }
                    });
                    reauthenticateAlert.show();
                }
            }
        });
    }

    private void setUpAlerts(){
        // Sign out alert
        signOutAlert = new Alert(ProfileActivity.this);
        signOutAlert.setTitle(getString(R.string.sign_out));
        signOutAlert.setDescription(getString(R.string.do_you_really_want_to_sign_out));
        signOutAlert.setPositiveButtonText(getString(R.string.yes));
        signOutAlert.setNegativeButtonText(getString(R.string.no));
        signOutAlert.setPositiveButtonOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                auth.signOut();

                Intent loginActivity = new Intent(ProfileActivity.this,LoginActivity.class);
                loginActivity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(loginActivity);
            }
        });
        signOutAlert.setNegativeButtonOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signOutAlert.dismiss();
            }
        });

        // Making editNameAlert
        editNameAlert = new InputAlert(ProfileActivity.this);
        editNameAlert.setTitle(getString(R.string.change_name));
        editNameAlert.setHint(user.getDisplayName());
        editNameAlert.setNegativeButtonText(getString(R.string.back));
        editNameAlert.setPositiveButtonText(getString(R.string.change));
        editNameAlert.setNegativeButtonOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editNameAlert.dismiss();
            }
        });
        editNameAlert.setPositiveButtonOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String input = editNameAlert.getInput().getText().toString();

                if(input.trim().equals("")) {
                    Toast.makeText(ProfileActivity.this, ProfileActivity.this.getString(R.string.name_is_empty),Toast.LENGTH_SHORT).show();
                    return;
                }

                // Set Name
                UserProfileChangeRequest profileUpdate = new UserProfileChangeRequest.Builder()
                        .setDisplayName(input)
                        .build();

                Objects.requireNonNull(auth.getCurrentUser()).updateProfile(profileUpdate).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){

                            db.collection(DOCUMENT_USERS).whereEqualTo("id",auth.getCurrentUser().getUid()).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                    if (task.isSuccessful()){
                                        QuerySnapshot snapshot = task.getResult();
                                        if (!snapshot.isEmpty()) {

                                            String id = snapshot.getDocuments().get(0).getId();

                                            Log.i("Name found",id);

                                            HashMap<String,Object> updatedData = new HashMap<>();
                                            updatedData.put("name",input);

                                            if (id == null) somethingWentWrongMessage();

                                            db.collection(DOCUMENT_USERS).document(id).update(updatedData).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()){
                                                        editNameAlert.dismiss();
                                                        changeNamesDisplayed(input);
                                                        Toast.makeText(ProfileActivity.this, ProfileActivity.this.getString(R.string.name_was_changed_successfully),Toast.LENGTH_SHORT).show();

                                                    }
                                                    else somethingWentWrongMessage();
                                                }
                                            });
                                        }
                                        else {
                                            Log.i("Rewrite","NOT FOUND " + auth.getCurrentUser().getUid());
                                            somethingWentWrongMessage();
                                        }
                                    }
                                    else {
                                        Log.i("Rewrite","UNSUCCESFFUL");
                                        somethingWentWrongMessage();
                                    }
                                }
                            });

                        }


                        else somethingWentWrongMessage();

                    }
                });
            }
        });

        // Making editEmailAlert
        editEmailAlert = new InputAlert(ProfileActivity.this);
        editEmailAlert.setTitle(getString(R.string.change_email));
        editEmailAlert.setHint(user.getEmail());
        editEmailAlert.setNegativeButtonText(getString(R.string.back));
        editEmailAlert.setPositiveButtonText(getString(R.string.change));
        editEmailAlert.setNegativeButtonOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editEmailAlert.dismiss();
            }
        });
        editEmailAlert.setPositiveButtonOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String input = editEmailAlert.getInput().getText().toString();
                Log.i("Email inputted",input);

                // Set Email
                if(input.trim().equals("")) {
                    Toast.makeText(ProfileActivity.this, ProfileActivity.this.getString(R.string.email_is_empty),Toast.LENGTH_SHORT).show();
                    return;
                }
                else if (!Helpers.Validation.email(input)) {
                    Toast.makeText(ProfileActivity.this, ProfileActivity.this.getString(R.string.email_is_not_valid),Toast.LENGTH_SHORT).show();
                    return;
                }

                Objects.requireNonNull(auth.getCurrentUser()).updateEmail(input).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.getException() != null) {
                            // Reauthenticate
                            editEmailAlert.dismiss();
                            reauthenticateAlert.setCompleteOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()){
                                        Toast.makeText(ProfileActivity.this, ProfileActivity.this.getString(R.string.you_have_been_logged_in_successfully),Toast.LENGTH_SHORT).show();
                                        reauthenticateAlert.dismiss();
                                        waitAndShow(editEmailAlert,200);
                                        authenticated = true;
                                    }
                                    else somethingWentWrongMessage();
                                }
                            });
                            reauthenticateAlert.show();
                        }
                        if (task.isSuccessful()){
                            editEmailAlert.dismiss();

                            Log.i("Change email shit","LMao");
                            changeEmailsDisplayed(input);

                            Toast.makeText(ProfileActivity.this, ProfileActivity.this.getString(R.string.email_was_changed_successfully),Toast.LENGTH_SHORT).show();
                        }
                        else somethingWentWrongMessage();
                    }
                });
            }
        });

        //Making password change alert
        passwordChangeAlert = new PasswordChangeAlert(ProfileActivity.this);
        passwordChangeAlert.setPasswordMatchListener(new PasswordChangeAlert.OnPasswordMatchListener() {
            @Override
            public void onPasswordMach(String password) {
                // Change password
                Objects.requireNonNull(auth.getCurrentUser()).updatePassword(password).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.getException() != null) {
                            Log.i("EXCEPTION HERE",task.getException().getMessage());
                            // Reauthenticate
                            passwordChangeAlert.dismiss();
                            reauthenticateAlert.setCompleteOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()){
                                        Toast.makeText(ProfileActivity.this, ProfileActivity.this.getString(R.string.you_have_been_logged_in_successfully),Toast.LENGTH_SHORT).show();
                                        reauthenticateAlert.dismiss();
                                        waitAndShow(passwordChangeAlert,200);
                                        authenticated = true;
                                    }
                                    else somethingWentWrongMessage();
                                }
                            });
                            reauthenticateAlert.show();
                        }
                        if(task.isSuccessful()){
                            passwordChangeAlert.dismiss();

                            Toast.makeText(ProfileActivity.this, ProfileActivity.this.getString(R.string.password_was_changed_successfully),Toast.LENGTH_SHORT).show();
                        }
                        else somethingWentWrongMessage();
                    }
                });

            }

            @Override
            public void onPasswordDontMatch() {
                Toast.makeText(ProfileActivity.this, ProfileActivity.this.getString(R.string.passwords_dont_match),Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onPasswordNotValid() {
                Toast.makeText(ProfileActivity.this, ProfileActivity.this.getString(R.string.password_at_least_6_char_long),Toast.LENGTH_SHORT).show();
            }
        });

        // Making authenticated alert
        reauthenticateAlert = new ReauthenticateAlert(ProfileActivity.this);
        reauthenticateAlert.setNegativeButtonOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reauthenticateAlert.dismiss();
            }
        });

    }

    private void changeNamesDisplayed(String name){
        userNameHint.setText(name);
        userNameText.setText(name);
    }

    private void changeEmailsDisplayed(String email){
        userEmailHint.setText(email);
    }

    private void somethingWentWrongMessage(){
        Toast.makeText(ProfileActivity.this, ProfileActivity.this.getString(R.string.something_went_wrong),Toast.LENGTH_SHORT).show();
    }

    private void waitAndShow(InputAlert alert,int delay){
        Helpers.Time.delay(new Runnable() {
            @Override
            public void run() {
                alert.show();
            }
        },delay);
    }

    private void waitAndShow(PasswordChangeAlert alert,int delay){
        Helpers.Time.delay(new Runnable() {
            @Override
            public void run() {
                alert.show();
            }
        },delay);
    }
}