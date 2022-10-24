package sk.dzurikm.domestio.views.dialogs;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.HashMap;
import java.util.Map;

import sk.dzurikm.domestio.R;
import sk.dzurikm.domestio.helpers.Constants;
import sk.dzurikm.domestio.helpers.DatabaseHelper;
import sk.dzurikm.domestio.models.Room;
import sk.dzurikm.domestio.views.alerts.InputAlert;

public class RoomOptionDialog extends BottomSheetDialogFragment {

    // Views
    private View rootView;
    private TextView roomTitle,roomDescription, roomTitleHint,roomDescriptionHint, roomTitleButton,roomDescriptionButton;

    // Needed variables
    private Context context;
    private FragmentManager fragmentManager;
    private Room room;

    // Alert
    private InputAlert changeRoomTitleAlert,changeRoomDescriptionAlert;

    // Helpers
    DatabaseHelper databaseHelper;

    // Listeners
    RoomDataChangedListener roomDataChangedListener;


    public RoomOptionDialog(Context context, FragmentManager fragmentManager) {
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
        rootView = View.inflate(getContext(), R.layout.room_option_dialog_layput, null);
        dialog.setContentView(rootView);

        // Views
        roomTitle = rootView.findViewById(R.id.roomName);
        roomTitleButton = rootView.findViewById(R.id.roomNameEditButton);
        roomTitleHint = rootView.findViewById(R.id.roomNameHint);

        roomDescription = rootView.findViewById(R.id.roomDescription);
        roomDescriptionButton = rootView.findViewById(R.id.roomDescriptionEditButton);
        roomDescriptionHint = rootView.findViewById(R.id.roomDescriptionHint);

        // Setting up the values
        roomTitleHint.setText(room.getTitle());
        roomDescriptionHint.setText(room.getDescription());

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
