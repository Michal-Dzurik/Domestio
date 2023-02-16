package sk.dzurikm.domestio.views.dialogs;

import static sk.dzurikm.domestio.helpers.Constants.Validation.Room.DESCRIPTION;
import static sk.dzurikm.domestio.helpers.Constants.Validation.Room.TITLE;
import static sk.dzurikm.domestio.helpers.Helpers.Views.getTextOfView;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;

import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import sk.dzurikm.domestio.R;
import sk.dzurikm.domestio.helpers.DataStorage;
import sk.dzurikm.domestio.helpers.DatabaseHelper;
import sk.dzurikm.domestio.helpers.Helpers;
import sk.dzurikm.domestio.models.Room;
import sk.dzurikm.domestio.views.alerts.InfoAlert;

public class AddRoomDialog extends BottomSheetDialogFragment {
    // Views
    private View rootView,colorPickInput;
    private TextView colorPickerText;
    private MaterialCardView colorPickerPreview;
    private ImageButton addRoomButton,closeButton;
    private EditText roomHeading,roomDescription;

    // Needed variables
    private Context context;
    private FragmentManager fragmentManager;

    // Validation
    Helpers.Validation validation;

    // Helpers
    DatabaseHelper databaseHelper;

    // Firebase
    FirebaseAuth auth;

    // Listeners
    OnRoomCreatedListener onRoomCreatedListener;

    public AddRoomDialog(Context context, FragmentManager fragmentManager, OnRoomCreatedListener onRoomCreatedListener) {
        this.context = context;
        this.fragmentManager = fragmentManager;
        this.onRoomCreatedListener = onRoomCreatedListener;
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
        rootView = View.inflate(getContext(), R.layout.add_room_dialog_layput, null);
        dialog.setContentView(rootView);

        // Views
        colorPickInput = rootView.findViewById(R.id.pickColorInput);
        colorPickerText = rootView.findViewById(R.id.colorPickerText);
        colorPickerPreview = rootView.findViewById(R.id.colorPickerPreview);
        addRoomButton = rootView.findViewById(R.id.addRoomButton);
        roomHeading = rootView.findViewById(R.id.headingInput);
        roomDescription = rootView.findViewById(R.id.descriptionInput);
        closeButton = rootView.findViewById(R.id.closeButton);

        ((View) rootView.getParent()).setBackgroundColor(Color.TRANSPARENT);

        // Validation
        validation = Helpers.Validation.getInstance(context);

        // helpers
        databaseHelper = new DatabaseHelper();

        // Firebase
        auth = FirebaseAuth.getInstance();

        // Setting up listeners
        colorPickInput.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ColorPickerDialog colorPickerDialog = new ColorPickerDialog(context);
                colorPickerDialog.setTitle(getString(R.string.choose_a_color));
                colorPickerDialog.setPositiveButtonText(getString(R.string.choose));
                colorPickerDialog.setNegativeButtonText(getString(R.string.back));
                colorPickerDialog.setNegativeButtonOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        colorPickerDialog.dismiss();
                    }
                });
                colorPickerDialog.setColorSelectedListener(new ColorPickerDialog.OnColorSelectedListener() {
                    @Override
                    public void colorSelected(int color) {

                        colorPickerPreview.setCardBackgroundColor(color);

                        colorPickerText.setText(Helpers.Colors.fromIntToColor(color));
                        colorPickerDialog.dismiss();

                    }
                });

                colorPickerDialog.show();
            }
        });

        addRoomButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (DataStorage.connected){
                    HashMap<String,String> map = new HashMap<>();
                    map.put(TITLE,getTextOfView(roomHeading));
                    map.put(DESCRIPTION,getTextOfView(roomDescription));

                    ArrayList<String> errors = validation.validate(map);

                    if (errors != null){
                        // print errors
                        Toast.makeText(context,errors.get(0),Toast.LENGTH_SHORT).show();
                    }else {
                        if (!colorPickerText.getText().toString().equals(context.getString(R.string.no_color_selected))){
                            Helpers.Views.buttonDisabled(v,true);

                            // add room
                            Room room = new Room();
                            room.setTitle(getTextOfView(roomHeading));
                            room.setDescription(getTextOfView(roomDescription));
                            room.setColor(colorPickerText.getText().toString());
                            room.setAdminId(auth.getUid());
                            room.setUserIds(new ArrayList<String>(Collections.singleton(auth.getUid())));
                            room.setTaskIds(new ArrayList<String>());
                            String id = databaseHelper.addRoom(room, new DatabaseHelper.OnRoomAddedListener() {
                                @Override
                                public void onRoomAdded(Task task, Room room) {
                                    if (task.isSuccessful()){
                                        dialog.dismiss();
                                        onRoomCreatedListener.onRoomCreate(room);

                                    }
                                    else Helpers.Toast.somethingWentWrong(context);

                                    Helpers.Views.buttonDisabled(v,false);

                                }
                            });
                        }
                        else Toast.makeText(context, context.getString(R.string.color_wasn_selected),Toast.LENGTH_SHORT).show();

                    }
                }
                else {
                    Helpers.Toast.noInternet(context);
                }
            }
        });

        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDialog().dismiss();
            }
        });

    }

    public interface OnRoomCreatedListener{
        public void onRoomCreate(Room room);
    }

}
