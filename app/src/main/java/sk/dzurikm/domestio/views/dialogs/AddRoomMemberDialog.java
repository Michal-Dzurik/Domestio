package sk.dzurikm.domestio.views.dialogs;

import static sk.dzurikm.domestio.helpers.Constants.Validation.EMAIL;
import static sk.dzurikm.domestio.helpers.Helpers.Views.waitAndShow;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;
import java.util.HashMap;

import sk.dzurikm.domestio.R;
import sk.dzurikm.domestio.helpers.DataStorage;
import sk.dzurikm.domestio.helpers.Helpers;
import sk.dzurikm.domestio.views.alerts.InfoAlert;

public class AddRoomMemberDialog extends BottomSheetDialogFragment {
    // Views
    private View rootView;
    private EditText newRoomMemberEmail;
    private ImageButton addNewMemberButton,discardChangesButton;

    // Needed variables
    private Context context;
    private FragmentManager fragmentManager;

    // Listeners
    private OnEmailValidListener onEmailValidListener;

    // Validation
    Helpers.Validation validation;

    public AddRoomMemberDialog(Context context, FragmentManager fragmentManager) {
        this.context = context;
        this.fragmentManager = fragmentManager;
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
        rootView = View.inflate(getContext(), R.layout.add_room_member_dialog_layput, null);
        dialog.setContentView(rootView);

        // Views
        newRoomMemberEmail = rootView.findViewById(R.id.newMemberEmail);
        addNewMemberButton = rootView.findViewById(R.id.addMemberButton);
        discardChangesButton = rootView.findViewById(R.id.discardChangesButton);

        ((View) rootView.getParent()).setBackgroundColor(Color.TRANSPARENT);

        // Validation
        validation = Helpers.Validation.getInstance(context);

        // Setting up listeners
        addNewMemberButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (DataStorage.connected){
                    String email = newRoomMemberEmail.getText().toString().trim();

                    // Validation
                    HashMap<String,String> map = new HashMap<>();
                    map.put(EMAIL,email);

                    ArrayList<String> errors = validation.validate(map);

                    if (errors == null){
                        Helpers.Views.buttonDisabled(v,true);
                        onEmailValidListener.onEmailValid(email);
                    }
                    else Toast.makeText(context,errors.get(0),Toast.LENGTH_SHORT).show();
                }
                else {
                    Helpers.Toast.noInternet(context);
                }

            }

        });

        discardChangesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AddRoomMemberDialog.this.dismiss();
            }
        });

    }

    public void setOnEmailValidListener(OnEmailValidListener onEmailValidListener) {
        this.onEmailValidListener = onEmailValidListener;
    }

    public void positiveButtonDisabled(boolean disabled){
        Helpers.Views.buttonDisabled(addNewMemberButton,disabled);
    }

    public interface OnEmailValidListener{
        public void onEmailValid(String email);
    }
}
