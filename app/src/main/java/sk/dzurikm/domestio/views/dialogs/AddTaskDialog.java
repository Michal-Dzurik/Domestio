package sk.dzurikm.domestio.views.dialogs;

import static sk.dzurikm.domestio.helpers.Constants.Validation.Task.DESCRIPTION;
import static sk.dzurikm.domestio.helpers.Constants.Validation.Task.HEADING;
import static sk.dzurikm.domestio.helpers.Constants.Validation.Task.ROOM_ID;
import static sk.dzurikm.domestio.helpers.Constants.Validation.Task.TIME;
import static sk.dzurikm.domestio.helpers.Constants.Validation.Task.USER_ID;
import static sk.dzurikm.domestio.helpers.Helpers.Views.getTextOfView;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.auth.FirebaseAuth;


import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;

import sk.dzurikm.domestio.R;
import sk.dzurikm.domestio.adapters.RoomSpinnerAdapter;
import sk.dzurikm.domestio.adapters.UserSpinnerAdapter;
import sk.dzurikm.domestio.helpers.DatabaseHelper;
import sk.dzurikm.domestio.helpers.Helpers;
import sk.dzurikm.domestio.models.Room;
import sk.dzurikm.domestio.models.Task;
import sk.dzurikm.domestio.models.User;

public class AddTaskDialog extends BottomSheetDialogFragment {

    // Views
    private Spinner ownerSelect,roomSelect;
    private View rootView,datePickerButton;
    private TextView datePickerButtonText,heading,description;
    private ImageButton closeButton,addTaskButton;

    // Needed variables
    private Context context;
    private FragmentManager fragmentManager;
    private Long timestamp;
    private Room selectedRoom;
    private User selectedUser;

    // Users and rooms
    private ArrayList<User> userList;
    private ArrayList<Room> roomList;

    // Validation
    Helpers.Validation validation;

    // Helpers
    DatabaseHelper databaseHelper;

    // Firebase
    FirebaseAuth auth;

    // Listeners
    OnTaskAddedListener onTaskAddedListener;

    public AddTaskDialog(Context context, FragmentManager fragmentManager,ArrayList<User> userList,ArrayList<Room> roomList, OnTaskAddedListener onTaskAddedListener) {
        this.context = context;
        this.fragmentManager = fragmentManager;
        this.userList = userList;
        this.roomList = roomList;
        this.onTaskAddedListener = onTaskAddedListener;

        selectedRoom = null;
        selectedUser = null;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @SuppressLint("RestrictedApi")
    @Override
    public void setupDialog(Dialog dialog, int style) {
        super.setupDialog(dialog,style);
        rootView = View.inflate(getContext(), R.layout.add_task_dialog_layput, null);
        dialog.setContentView(rootView);

        ((View) rootView.getParent()).setBackgroundColor(Color.TRANSPARENT);

        // Views
        roomSelect = rootView.findViewById(R.id.roomSelect);
        ownerSelect = rootView.findViewById(R.id.ownerSelect);
        datePickerButton = rootView.findViewById(R.id.dateAndTimePickerButton);
        datePickerButtonText = rootView.findViewById(R.id.datePickerButtonText);
        closeButton = rootView.findViewById(R.id.closeButton);
        addTaskButton = rootView.findViewById(R.id.addTaskButton);
        heading = rootView.findViewById(R.id.headingInput);
        description = rootView.findViewById(R.id.descriptionInput);

        // Validation and helpers
        validation = Helpers.Validation.getInstance(context);
        databaseHelper = new DatabaseHelper();
        auth = FirebaseAuth.getInstance();

        // Setting up listeners
        datePickerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar calendar = Calendar.getInstance();
                int day = calendar.get(Calendar.DAY_OF_MONTH);
                int month = calendar.get(Calendar.MONTH);
                int year = calendar.get(Calendar.YEAR);
                int hour = calendar.get(Calendar.HOUR);
                int minute = calendar.get(Calendar.MINUTE);

                DatePickerDialog dialog = new DatePickerDialog(context, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        new TimePickerDialog(context, new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                setDateAndTime(year,month,dayOfMonth,hourOfDay,minute);
                            }
                        },hour,minute,true).show();
                    }
                },year,month,day);

                dialog.show();
            }
        });

        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDialog().dismiss();
            }
        });

        addTaskButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println(selectedUser + "  \n  " + selectedRoom);
                HashMap<String,String> map = new HashMap<>();
                map.put(HEADING,getTextOfView(heading));
                map.put(DESCRIPTION,getTextOfView(description));
                map.put(TIME, String.valueOf(timestamp));
                map.put(USER_ID, selectedUser != null ? selectedUser.getId() : null);
                map.put(ROOM_ID, selectedRoom != null ? selectedRoom.getId() : null);

                ArrayList<String> errors = validation.validate(map);

                if (errors != null){
                    // print errors
                    Toast.makeText(context,errors.get(0),Toast.LENGTH_SHORT).show();
                }
                else {
                    addTaskToDatabase();
                }
            }
        });

        fillSpinners();

    }

    private void fillSpinners(){
        // TODO: fill them with an actual users and rooms
        RoomSpinnerAdapter roomAdapter = new RoomSpinnerAdapter((Activity) context, roomList);
        roomSelect.setAdapter(roomAdapter);

        if (!roomList.isEmpty()){
            selectedRoom = roomList.get(0);

            UserSpinnerAdapter userAdapter = new UserSpinnerAdapter((Activity) context, new ArrayList<User>(Collections.singleton(new User("0", getString(R.string.select_room)))));
            ownerSelect.setAdapter(userAdapter);

            roomSelect.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    if (!userList.isEmpty()){

                        selectedRoom = roomList.get(position);
                        selectedUser = null;

                        ArrayList<User> users = filterUsersByRoom(roomList.get(position),userList);

                        if(!users.isEmpty()){
                            UserSpinnerAdapter userAdapter = new UserSpinnerAdapter((Activity) context, users);
                            ownerSelect.setAdapter(userAdapter);

                            ownerSelect.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                @Override
                                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                    selectedUser = users.get(position);
                                }

                                @Override
                                public void onNothingSelected(AdapterView<?> parent) {

                                }
                            });
                        }
                        else {
                            UserSpinnerAdapter userAdapter = new UserSpinnerAdapter((Activity) context, new ArrayList<User>(Collections.singleton(new User("0", getString(R.string.no_users)))));
                            ownerSelect.setAdapter(userAdapter);
                        }
                    }
                    else {
                        UserSpinnerAdapter userAdapter = new UserSpinnerAdapter((Activity) context, new ArrayList<User>(Collections.singleton(new User("0", getString(R.string.no_users)))));
                        ownerSelect.setAdapter(userAdapter);
                    }


                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
        }
        else {
            UserSpinnerAdapter userAdapter = new UserSpinnerAdapter((Activity) context, new ArrayList<User>(Collections.singleton(new User("0", getString(R.string.no_rooms)))));
            ownerSelect.setAdapter(userAdapter);
        }




    }


    private void setDateAndTime(int year, int month, int dayOfMonth,int hour,int minute){
        String datetime = "" + year + "-" +
                Helpers.Integer.formatNumberOnTwoSpaces(month) + "-" +
                Helpers.Integer.formatNumberOnTwoSpaces(dayOfMonth) + " " +
                Helpers.Integer.formatNumberOnTwoSpaces(hour) + ":" +
                Helpers.Integer.formatNumberOnTwoSpaces(minute);

        DateTimeFormatter f = DateTimeFormatter.ofPattern( "yyyy-MM-dd HH:mm" ) ;
        LocalDateTime ldt = LocalDateTime.parse( datetime , f ) ;
        OffsetDateTime odt = ldt.atOffset( ZoneOffset.UTC ) ;

        long secondsSinceEpoch = odt.toEpochSecond() ;

        timestamp = secondsSinceEpoch;
        datePickerButtonText.setText(datetime);

    }

    private ArrayList<User> filterUsersByRoom(Room room,ArrayList<User> users){
        ArrayList<User> filtered = new ArrayList<>();
        for (int i = 0; i < users.size(); i++) {
            if (room.getUserIds().contains(users.get(i).getId()) && !users.get(i).getId().equals(auth.getUid())) filtered.add(users.get(i));
        }

        return filtered;
    }

    private void addTaskToDatabase(){
        Task task = new Task();
        task.setRoomId(selectedRoom.getId());
        task.setAuthorId(auth.getUid());
        task.setHeading(getTextOfView(heading));
        task.setDescription(getTextOfView(description));
        task.setDone(false);
        task.setReceiverId(selectedUser.getId());
        task.setTimestamp(timestamp);

        databaseHelper.addTask(task, new DatabaseHelper.OnTaskAddedListener() {
            @Override
            public void onTaskAdded(com.google.android.gms.tasks.Task t, Task task) {
                if (t.isSuccessful()){
                    getDialog().dismiss();
                    onTaskAddedListener.onTaskAdded(task);
                }
                else System.out.println("JUJ");
            }
        });

    }

    public interface OnTaskAddedListener{
        public void onTaskAdded(Task task);
    }

}
