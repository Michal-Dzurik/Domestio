package sk.dzurikm.domestio.views.dialogs;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.FragmentManager;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import sk.dzurikm.domestio.R;
import sk.dzurikm.domestio.helpers.Constants;
import sk.dzurikm.domestio.helpers.DatabaseHelper;
import sk.dzurikm.domestio.helpers.Helpers;
import sk.dzurikm.domestio.models.Room;
import sk.dzurikm.domestio.models.User;
import sk.dzurikm.domestio.views.alerts.InputAlert;

public class RoomOptionDialog extends BottomSheetDialogFragment {

    // Views
    private View rootView;
    private TextView roomTitle,roomDescription, roomTitleHint,roomDescriptionHint, roomTitleButton,roomDescriptionButton,roomColorHint;
    private CardView roomColorPicker;
    private Button membersListButton;
    private LinearLayout removeRoomButton;

    // Needed variables
    private Context context;
    private FragmentManager fragmentManager;
    private Room room;

    // Alert
    private InputAlert changeRoomTitleAlert,changeRoomDescriptionAlert;
    private ColorPickerDialog colorPickerDialog;

    // Helpers
    DatabaseHelper databaseHelper;

    // Listeners
    RoomDataChangedListener roomDataChangedListener;

    // Needed variables
    ArrayList<User> users;


    public RoomOptionDialog(Context context, FragmentManager fragmentManager, ArrayList<User> users) {
        this.context = context;
        this.fragmentManager = fragmentManager;
        this.users = users;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @SuppressLint("RestrictedApi")
    @Override
    public void setupDialog(Dialog dialog, int style) {
        super.setupDialog(dialog,style);
        rootView = View.inflate(getContext(), R.layout.room_option_dialog_layput, null);
        dialog.setContentView(rootView);

        // Views
        roomTitle = rootView.findViewById(R.id.roomName);
        roomTitleButton = rootView.findViewById(R.id.roomNameEditButton);
        roomTitleHint = rootView.findViewById(R.id.roomNameHint);

        roomDescription = rootView.findViewById(R.id.roomDescription);
        roomDescriptionButton = rootView.findViewById(R.id.roomDescriptionEditButton);
        roomDescriptionHint = rootView.findViewById(R.id.roomDescriptionHint);
        roomColorPicker = rootView.findViewById(R.id.roomColorPicker);
        roomColorHint = rootView.findViewById(R.id.roomColorHint);
        membersListButton = rootView.findViewById(R.id.membersListButton);
        removeRoomButton = rootView.findViewById(R.id.removeRoomButton);

        // Setting up the values
        roomTitleHint.setText(room.getTitle());
        roomDescriptionHint.setText(room.getDescription());
        roomColorHint.setText(room.getColor());
        roomColorPicker.setCardBackgroundColor(Color.parseColor(room.getColor()));

        // Helpers
        databaseHelper = new DatabaseHelper();

        setUpAlerts();

        ((View) rootView.getParent()).setBackgroundColor(Color.TRANSPARENT);

        roomDescriptionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeRoomDescriptionAlert.show();
            }
        });

        roomTitleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeRoomTitleAlert.show();
            }
        });

        roomColorPicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                colorPickerDialog.show();
            }
        });

        membersListButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Open user list
                getDialog().dismiss();
                ListMembersDialog listMembersDialog = new ListMembersDialog(context,fragmentManager,users,room);
                listMembersDialog.show(fragmentManager,"List of members");
            }
        });

        removeRoomButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Remove room
            }
        });

    }

    private void setUpAlerts(){
        changeRoomTitleAlert = new InputAlert(context);
        changeRoomTitleAlert.setPositiveButtonText(context.getString(R.string.change));
        changeRoomTitleAlert.setNegativeButtonText(context.getString(R.string.back));
        changeRoomTitleAlert.setTitle(getString(R.string.change_room_heading));
        changeRoomTitleAlert.setHint(room.getTitle());
        changeRoomTitleAlert.setPositiveButtonOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String roomTitle = changeRoomTitleAlert.getInput().getText().toString().trim();

                Map<String,Object> data = new HashMap<>();
                data.put(Constants.Firebase.Room.FIELD_TITLE,roomTitle);

                databaseHelper.changeDocument(Constants.Firebase.DOCUMENT_ROOMS, room.getId(), data, new OnSuccessListener() {
                    @Override
                    public void onSuccess(Object o) {
                        room.setTitle(roomTitle);

                        changeRoomTitleAlert.dismiss();
                        roomDataChangedListener.onRoomDataChangedListener(room);
                        roomTitleHint.setText(roomTitle);
                    }
                }, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(context,context.getString(R.string.something_went_wrong),Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
        changeRoomTitleAlert.setNegativeButtonOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeRoomTitleAlert.dismiss();
            }
        });

        changeRoomDescriptionAlert = new InputAlert(context);
        changeRoomDescriptionAlert.setPositiveButtonText(context.getString(R.string.change));
        changeRoomDescriptionAlert.setNegativeButtonText(context.getString(R.string.back));
        changeRoomDescriptionAlert.setTitle(getString(R.string.change_room_description));
        changeRoomDescriptionAlert.setHint(room.getDescription());
        changeRoomDescriptionAlert.setPositiveButtonOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String roomDescription = changeRoomDescriptionAlert.getInput().getText().toString();

                Map<String,Object> data = new HashMap<>();
                data.put(Constants.Firebase.Room.FIELD_DESCRIPTION,roomDescription);

                databaseHelper.changeDocument(Constants.Firebase.DOCUMENT_ROOMS, room.getId(), data, new OnSuccessListener() {
                    @Override
                    public void onSuccess(Object o) {
                        room.setDescription(roomDescription);

                        roomDescriptionHint.setText(roomDescription);
                        roomDataChangedListener.onRoomDataChangedListener(room);
                        changeRoomDescriptionAlert.dismiss();
                    }
                }, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(context,context.getString(R.string.something_went_wrong),Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        changeRoomDescriptionAlert.setNegativeButtonOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeRoomDescriptionAlert.dismiss();
            }
        });

        colorPickerDialog = new ColorPickerDialog(context);
        colorPickerDialog.setColorSelectedListener(new ColorPickerDialog.OnColorSelectedListener() {
            @Override
            public void colorSelected(int color) {
                String stringColor = Helpers.fromIntToColor(color);
                Map<String,Object> data = new HashMap<>();
                data.put(Constants.Firebase.Room.FIELD_COLOR,stringColor);

                Log.i("LMAO",stringColor);
                databaseHelper.changeDocument(Constants.Firebase.DOCUMENT_ROOMS, room.getId(), data, new OnSuccessListener() {
                    @Override
                    public void onSuccess(Object o) {

                        room.setColor(stringColor);

                        roomColorPicker.setCardBackgroundColor(color);
                        roomColorHint.setText(stringColor);
                        roomDataChangedListener.onRoomDataChangedListener(room);
                        colorPickerDialog.dismiss();
                    }
                }, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(context,context.getString(R.string.something_went_wrong),Toast.LENGTH_SHORT).show();
                    }
                });

                roomColorHint.setText(Helpers.Colors.fromIntToColor(color));
                colorPickerDialog.dismiss();
            }
        });

        colorPickerDialog.setNegativeButtonOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                colorPickerDialog.dismiss();
            }
        });
    }

    public void setRoom(Room room) {
        this.room = room;
    }

    public void setRoomDataChangedListener(RoomDataChangedListener roomDataChangedListener) {
        this.roomDataChangedListener = roomDataChangedListener;
    }

    public interface RoomDataChangedListener{
        public void onRoomDataChangedListener(Room room);
    }
}
