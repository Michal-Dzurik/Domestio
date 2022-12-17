package sk.dzurikm.domestio.views.dialogs;

import static sk.dzurikm.domestio.helpers.Constants.Validation.EMAIL;
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
import android.view.View;
import android.widget.AdapterView;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
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
import java.util.Objects;

import sk.dzurikm.domestio.R;
import sk.dzurikm.domestio.activities.YourTasksActivity;
import sk.dzurikm.domestio.adapters.RoomSpinnerAdapter;
import sk.dzurikm.domestio.adapters.UserSpinnerAdapter;
import sk.dzurikm.domestio.helpers.DataStorage;
import sk.dzurikm.domestio.helpers.DatabaseHelper;
import sk.dzurikm.domestio.helpers.Helpers;
import sk.dzurikm.domestio.models.Room;
import sk.dzurikm.domestio.models.Task;
import sk.dzurikm.domestio.models.User;
import sk.dzurikm.domestio.views.alerts.InfoAlert;

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
    private Task task;

    private DialogRole role;

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
    OnTaskChangeListener onTaskChangeListener;

    public AddTaskDialog(Context context,
                         FragmentManager fragmentManager,
                         ArrayList<User> userList,
                         ArrayList<Room> roomList,
                         OnTaskChangeListener onTaskChangeListener) {
        // Role add

        this.context = context;
        this.fragmentManager = fragmentManager;
        this.userList = userList;
        this.roomList = roomList;
        this.onTaskChangeListener = onTaskChangeListener;

        this.role = DialogRole.ADD;

        selectedRoom = null;
        selectedUser = null;
    }


    public AddTaskDialog(Context context,
                         FragmentManager fragmentManager,
                         ArrayList<User> userList,
                         ArrayList<Room> roomList,
                         OnTaskChangeListener onTaskChangeListener,
                         Task task) {
        // Role edit
        this.role = DialogRole.EDIT;

        this.context = context;
        this.fragmentManager = fragmentManager;
        this.userList = userList;
        this.roomList = roomList;
        this.onTaskChangeListener = onTaskChangeListener;
        this.task = task;


        selectedRoom = Helpers.DataSet.getRoomById(roomList,task.getRoomId());
        selectedUser = Helpers.DataSet.getUserById(userList,task.getReceiverId());

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void show(@NonNull FragmentManager manager, @Nullable String tag) {
        if (!DataStorage.connected){
            InfoAlert alert = new InfoAlert(context);
            alert.setTitle(context.getString(R.string.we_are_offline));
            alert.setDescription(context.getString(R.string.no_internet_description));
            alert.setPositiveButtonOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    alert.dismiss();
                }
            });
            alert.show();
        }else super.show(manager, tag);
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

        if (role == DialogRole.EDIT){
            addTaskButton.setImageResource(R.drawable.ic_edit);

            heading.setText(task.getHeading());
            description.setText(task.getDescription());

            selectedUser = Helpers.DataSet.getUserById(DataStorage.users,task.getReceiverId());
            selectedRoom = Helpers.DataSet.getRoomById(DataStorage.rooms,task.getRoomId());
        }

        // Setting up listeners
        datePickerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar calendar = Calendar.getInstance();
                int day,month,year,hour,minute;

                if (role == DialogRole.EDIT) calendar.setTimeInMillis(task.getTimestamp() * 1000);

                day = calendar.get(Calendar.DAY_OF_MONTH);
                month = calendar.get(Calendar.MONTH);
                year = calendar.get(Calendar.YEAR);
                hour = calendar.get(Calendar.HOUR);
                minute = calendar.get(Calendar.MINUTE);

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
                if (DataStorage.connected){
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
                        if (role == DialogRole.ADD){
                            addTaskToDatabase();
                        }
                        if (role == DialogRole.EDIT){
                            Task originalTask = Task.editTaskCopy(task);
                            task.updateFromValidationData(map);

                            System.out.println(task.getTimestamp());

                            if (!originalTask.getRoomId().equals(task.getRoomId())) updateTask(task,originalTask);
                            else updateTask(task,null);

                        }
                    }
                }
                else {
                    Helpers.Toast.noInternet(context);
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

            UserSpinnerAdapter userAdapter = new UserSpinnerAdapter((Activity) context, new ArrayList<User>(Collections.singleton(new User("0", getString(R.string.select_room)))));
            ownerSelect.setAdapter(userAdapter);

            if (role == DialogRole.EDIT){
                int userPosition = userAdapter.getPosition(selectedUser);
                int roomPosition = roomAdapter.getPosition(selectedRoom);

                roomSelect.setSelection(roomPosition);
                ownerSelect.setSelection(userPosition);
            }
            else selectedRoom = roomList.get(0);

            roomSelect.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    if (!userList.isEmpty()){

                        selectedRoom = roomList.get(position);

                        ArrayList<User> users = filterUsersByRoom(roomList.get(position),userList);

                        if(!users.isEmpty()){
                            UserSpinnerAdapter userAdapter = new UserSpinnerAdapter((Activity) context, users);
                            ownerSelect.setAdapter(userAdapter);

                            if (role == DialogRole.EDIT){
                                int userPosition = userAdapter.getPosition(selectedUser);
                                if (userPosition != -1) ownerSelect.setSelection(userPosition);
                                else ownerSelect.setSelection(0);
                            }
                            else selectedUser = null;

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

        if (role == DialogRole.EDIT){
            System.out.println(task.getTimestamp());
            timestamp = task.getTimestamp();

            Calendar calendar = Calendar.getInstance();
            int day,month,year,hour,minute;

            calendar.setTimeInMillis(task.getTimestamp() * 1000);

            day = calendar.get(Calendar.DAY_OF_MONTH);
            month = calendar.get(Calendar.MONTH);
            year = calendar.get(Calendar.YEAR);
            hour = calendar.get(Calendar.HOUR);
            minute = calendar.get(Calendar.MINUTE);

            setDateAndTime(year,month,day,hour,minute);
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

        System.out.println(secondsSinceEpoch);

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
                    Objects.requireNonNull(getDialog()).dismiss();
                    onTaskChangeListener.onTaskAdded(task);
                }
                else Helpers.Toast.somethingWentWrong(context);
            }
        });


    }

    private void updateTask(Task task,Task originalTask){
        databaseHelper.updateTask(task, new DatabaseHelper.OnTaskEditedListener() {
            @Override
            public void onTaskEdited(com.google.android.gms.tasks.Task t, Task task) {
                if (t.isSuccessful()){
                    getDialog().dismiss();
                    onTaskChangeListener.onTaskEdited(task);
                    ((YourTasksActivity) context).taskChanged(task);
                }
                else System.out.println("JUJ");
            }
        },originalTask);


    }

    public interface OnTaskChangeListener {
        public void onTaskAdded(Task task);
        public void onTaskEdited(Task task);
    }

    public enum DialogRole{
        ADD,EDIT
    }

}
