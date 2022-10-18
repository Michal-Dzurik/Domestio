package sk.dzurikm.domestio.views.dialogs;

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
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;

import sk.dzurikm.domestio.R;
import sk.dzurikm.domestio.adapters.RoomSpinnerAdapter;
import sk.dzurikm.domestio.adapters.UserSpinnerAdapter;
import sk.dzurikm.domestio.helpers.Helpers;
import sk.dzurikm.domestio.models.Room;
import sk.dzurikm.domestio.models.User;

public class AddTaskDialog extends BottomSheetDialogFragment {
    private Context context;
    private FragmentManager fragmentManager;
    private Spinner ownerSelect,roomSelect;

    private View rootView,datePickerButton;
    private TextView datePickerButtonText;

    public AddTaskDialog(Context context, FragmentManager fragmentManager) {
        this.context = context;
        this.fragmentManager = fragmentManager;
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

        roomSelect = rootView.findViewById(R.id.roomSelect);
        ownerSelect = rootView.findViewById(R.id.ownerSelect);
        datePickerButton = rootView.findViewById(R.id.dateAndTimePickerButton);
        datePickerButtonText = rootView.findViewById(R.id.datePickerButtonText);

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


        fillSpinners();

    }

    private void fillSpinners(){
        List<Room> rooms = new LinkedList<>();
        rooms.add(new Room("lmasidnas","Hello"));
        rooms.add(new Room("lasde","Nol"));
        RoomSpinnerAdapter roomAdapter = new RoomSpinnerAdapter((Activity) context, rooms);
        roomSelect.setAdapter(roomAdapter);

        List<User> users = new LinkedList<>();
        users.add(new User("lmasidnas","Midzo"));
        users.add(new User("lasde","Mama"));
        UserSpinnerAdapter userAdapter = new UserSpinnerAdapter((Activity) context, users);
        ownerSelect.setAdapter(userAdapter);

        roomSelect.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.i("Room Id SELCTED",rooms.get(position).getId());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        ownerSelect.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.i("Owner Id SELCTED",users.get(position).getId());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
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

        System.out.println(secondsSinceEpoch + " - " +  datetime);

        datePickerButtonText.setText(datetime);

    }

}
