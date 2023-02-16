package sk.dzurikm.domestio.activities;

import static sk.dzurikm.domestio.helpers.Constants.Firebase.DOCUMENT_USERS;
import static sk.dzurikm.domestio.helpers.Constants.Firebase.Task.FIELD_MODIFIED_AT;
import static sk.dzurikm.domestio.helpers.Constants.Firebase.User.*;
import static sk.dzurikm.domestio.helpers.Constants.Validation.EMAIL;
import static sk.dzurikm.domestio.helpers.Constants.Validation.NAME;
import static sk.dzurikm.domestio.helpers.Helpers.Views.*;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;

import sk.dzurikm.domestio.R;
import sk.dzurikm.domestio.helpers.broadcasts.DataChangedReceiver;
import sk.dzurikm.domestio.helpers.Constants;
import sk.dzurikm.domestio.helpers.DCO;
import sk.dzurikm.domestio.helpers.DataStorage;
import sk.dzurikm.domestio.helpers.DatabaseHelper;
import sk.dzurikm.domestio.helpers.Helpers;
import sk.dzurikm.domestio.models.Room;
import sk.dzurikm.domestio.models.User;
import sk.dzurikm.domestio.views.alerts.Alert;
import sk.dzurikm.domestio.views.alerts.InputAlert;
import sk.dzurikm.domestio.views.alerts.PasswordChangeAlert;
import sk.dzurikm.domestio.views.alerts.ReauthenticateAlert;

public class ProfileActivity extends AppCompatActivity {

    // Views
    private ImageButton backButton,leaveButton;
    private TextView userNameText,userNameHint,userEmailHint;
    private String userEmail,userName;
    private Button nameEditButton,emailEditButton,passwordEditButton;

    // Databse
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private FirebaseUser user;

    // Additional variables
    private boolean authenticated;

    // Alerts
    private Alert signOutAlert;
    private InputAlert editNameAlert,editEmailAlert;
    private ReauthenticateAlert reauthenticateAlert;
    private PasswordChangeAlert passwordChangeAlert;

    // Helpers
    DatabaseHelper databaseHelper;

    // Validation
    Helpers.Validation validation;

    // DCO
    DCO dco;

    // Broadcasts
    DataChangedReceiver dataChangedReceiver;

    SharedPreferences sharedPreferences;
    SharedPreferences.Editor sharedPreferencesEditor;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        authenticated = false;

        // Shared preferences
        sharedPreferences = getSharedPreferences(Constants.SHARED_PREFERENCES_KEY,MODE_PRIVATE);
        sharedPreferencesEditor = sharedPreferences.edit();

        // Database init
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        db = FirebaseFirestore.getInstance();

        // Helpers
        dco = new DCO(new DCO.OnDataChangeListener() {
            @Override
            public void onChange(ArrayList<User> usersData, ArrayList<Room> roomData, ArrayList<sk.dzurikm.domestio.models.Task> taskData) {
                // Don't need to change anything in this activity so it is blank
            }
        });

        // Broadcasts
        dataChangedReceiver = new DataChangedReceiver(new DataChangedReceiver.DataChangedListener() {
            @Override
            public void onDataChanged(HashMap<String, Object> data, String collection, String documentID, DocumentChange.Type type) {

                switch (collection){
                    case Constants.Firebase.DOCUMENT_ROOMS:
                        Room room = new Room();
                        room.cast(documentID,data);

                        switch (type){
                            case ADDED:
                                dco.addRoom(room);
                                break;
                            case MODIFIED:
                                dco.updateRoomChangeableInfo(room);
                                break;
                            case REMOVED:

                                break;
                        }

                        break;

                    case Constants.Firebase.DOCUMENT_TASKS:
                        sk.dzurikm.domestio.models.Task task = new sk.dzurikm.domestio.models.Task();
                        task.cast(documentID,data);

                        task.setAuthor(Helpers.DataSet.getAuthorName(DataStorage.users,task.getAuthorId()));

                        HashMap<String,String> dat = Helpers.DataSet.getRoomInfo(DataStorage.rooms,task.getRoomId(), new String[]{Constants.Firebase.Room.FIELD_TITLE, Constants.Firebase.Room.FIELD_COLOR});
                        task.setRoomName(dat.get(Constants.Firebase.Room.FIELD_TITLE));
                        task.setColor(dat.get(Constants.Firebase.Room.FIELD_COLOR));


                        switch (type){
                            case ADDED:
                                dco.addTask(task);
                                break;
                            case MODIFIED:
                                dco.updateTask(task);
                                break;
                            case REMOVED:
                                dco.removeTask(task);
                                break;
                        }

                        break;

                    case Constants.Firebase.DOCUMENT_USERS:
                        User user = new User();
                        user.setId(documentID);
                        user.cast(data);

                        switch (type){
                            case ADDED:
                                dco.addUser(user);
                                break;
                            case MODIFIED:
                                dco.updatedUser(user);
                                break;
                        }

                        break;
                }
            }

        });
        IntentFilter intentSFilter = new IntentFilter("DATA_CHANGED");
        registerReceiver(dataChangedReceiver, intentSFilter);

        // Views
        backButton = findViewById(R.id.backButton);
        leaveButton = findViewById(R.id.leaveButton);
        userNameText = findViewById(R.id.userNameTextView);
        userEmailHint = findViewById(R.id.userEmailHint);
        userNameHint = findViewById(R.id.userNameHint);
        nameEditButton = findViewById(R.id.nameEditButton);
        emailEditButton = findViewById(R.id.emailEditButton);
        passwordEditButton = findViewById(R.id.passwordEditButton);

        // Helpers
        databaseHelper = new DatabaseHelper();

        // Validation
        validation = Helpers.Validation.getInstance(ProfileActivity.this);

        // Checking if user exists
        if (user != null) {
            // Name, email address, and profile photo Url
            userName = sharedPreferences.getString("user-name","");
            userEmail = user.getEmail();
            //Uri photoUrl = user.getPhotoUrl();

            userNameText.setText(userName);
            userNameHint.setText(userName);
            userEmailHint.setText(userEmail);
        }

        // Setting up alerts
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
                if (DataStorage.connected){
                    signOutAlert.show();
                }
                else Helpers.Toast.noInternet(ProfileActivity.this);

            }
        });

        // Setting up edit buttons
        nameEditButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Showing Alerts
                if (DataStorage.connected){
                    editNameAlert.show();
                }

                else Helpers.Toast.noInternet(ProfileActivity.this);

            }
        });

        emailEditButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (DataStorage.connected){
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

                                reauthenticateAlert.positiveButtonDisabled(false);
                            }
                        });
                        reauthenticateAlert.show();
                    }

                }
                else Helpers.Toast.noInternet(ProfileActivity.this);
            }
        });

        passwordEditButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (DataStorage.connected){
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

                                reauthenticateAlert.positiveButtonDisabled(false);
                            }
                        });
                        reauthenticateAlert.show();
                    }
                }
                else Helpers.Toast.noInternet(ProfileActivity.this);
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

                Intent loginActivity = new Intent(ProfileActivity.this,RegisterActivity.class);
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

                // Validation
                HashMap<String,String> map = new HashMap<>();
                map.put(NAME,input);

                ArrayList<String> errors = validation.validate(map);

                if (errors == null){
                    Helpers.Views.buttonDisabled(v,true);
                    // Update name of the user
                    databaseHelper.updateUserName(input, new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {

                                String id = auth.getUid();
                                HashMap<String, Object> updatedData = new HashMap<>();

                                updatedData.put(FIELD_NAME, input);
                                updatedData.put(FIELD_MODIFIED_AT, FieldValue.serverTimestamp());

                                if (id == null) {
                                    Helpers.Views.buttonDisabled(v,false);
                                    somethingWentWrongMessage();
                                    return;
                                }

                                db.collection(DOCUMENT_USERS).document(id).update(updatedData).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            editNameAlert.dismiss();

                                            // Changing hints in activity to be updated
                                            User userToChange = new User();
                                            userToChange.setId(auth.getUid());
                                            userToChange.setName(input);
                                            changeNamesDisplayed(userToChange);
                                            sharedPreferencesEditor.putString("user-name",input);
                                            sharedPreferencesEditor.commit();
                                            Toast.makeText(ProfileActivity.this, ProfileActivity.this.getString(R.string.name_was_changed_successfully), Toast.LENGTH_SHORT).show();

                                        } else {
                                            somethingWentWrongMessage();
                                        }

                                        Helpers.Views.buttonDisabled(v,false);
                                    }
                                });
                            } else {
                                Log.i("Rewrite", "UNSUCCESFFUL");
                                Helpers.Views.buttonDisabled(v,false);
                                somethingWentWrongMessage();
                            }
                        }
                    }, new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Helpers.Views.buttonDisabled(v,false);
                            somethingWentWrongMessage();
                        }
                    });
                }
                else {
                    Toast.makeText(ProfileActivity.this, errors.get(0), Toast.LENGTH_SHORT).show();
                }



            }
        });

        /* Alert init */
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
                String input = editEmailAlert.getInput().getText().toString().trim();

                // Validation
                HashMap<String,String> map = new HashMap<>();
                map.put(EMAIL,input);

                ArrayList<String> errors = validation.validate(map);

                if (errors == null){
                    Helpers.Views.buttonDisabled(v,true);
                    databaseHelper.updateUserEmail(input, new OnCompleteListener<Void>() {
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
                                        else {
                                            somethingWentWrongMessage();
                                        }

                                        reauthenticateAlert.positiveButtonDisabled(false);
                                        Helpers.Views.buttonDisabled(v,false);
                                    }
                                });
                                reauthenticateAlert.show();
                            }
                            if (task.isSuccessful()){
                                editEmailAlert.dismiss();

                                User userToUpdate = new User();
                                userToUpdate.setId(auth.getUid());
                                userToUpdate.setEmail(input);
                                changeEmailsDisplayed(userToUpdate);

                                Toast.makeText(ProfileActivity.this, ProfileActivity.this.getString(R.string.email_was_changed_successfully),Toast.LENGTH_SHORT).show();
                            }
                            else {
                                somethingWentWrongMessage();
                            }

                            Helpers.Views.buttonDisabled(v,false);
                        }
                    });
                }
                else Toast.makeText(ProfileActivity.this,errors.get(0),Toast.LENGTH_SHORT).show();

            }
        });

        //Making password change alert
        passwordChangeAlert = new PasswordChangeAlert(ProfileActivity.this);
        passwordChangeAlert.setPasswordMatchListener(new PasswordChangeAlert.OnPasswordMatchListener() {
            @Override
            public void onPasswordMach(View v,String password) {
                Helpers.Views.buttonDisabled(v,true);
                // Change password
                databaseHelper.updateUserPassword(password,new OnCompleteListener<Void>() {
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
                                    else {
                                        somethingWentWrongMessage();
                                    }

                                    reauthenticateAlert.positiveButtonDisabled(false);
                                    Helpers.Views.buttonDisabled(v,false);
                                }
                            });
                            reauthenticateAlert.show();
                        }
                        if(task.isSuccessful()){
                            passwordChangeAlert.dismiss();
                            Helpers.Views.buttonDisabled(v,false);

                            Toast.makeText(ProfileActivity.this, ProfileActivity.this.getString(R.string.password_was_changed_successfully),Toast.LENGTH_SHORT).show();
                        }
                        else {
                            Helpers.Views.buttonDisabled(v,false);
                            somethingWentWrongMessage();
                        }
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

    private void changeNamesDisplayed(User user){
        userNameHint.setText(user.getName());
        userNameText.setText(user.getName());

        dco.updatedUser(user);
    }

    private void changeEmailsDisplayed(User user){
        userEmailHint.setText(user.getEmail());
        dco.updatedUser(user);
    }

    private void somethingWentWrongMessage(){
        Helpers.Toast.somethingWentWrong(ProfileActivity.this);
    }

    @Override
    public void finish() {
        unregisterReceiver(dataChangedReceiver);
        super.finish();
    }
}