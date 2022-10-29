package sk.dzurikm.domestio.views.dialogs;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.card.MaterialCardView;

import sk.dzurikm.domestio.R;
import sk.dzurikm.domestio.helpers.Helpers;

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

    public AddRoomMemberDialog(Context context, FragmentManager fragmentManager) {
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
        rootView = View.inflate(getContext(), R.layout.add_room_member_dialog_layput, null);
        dialog.setContentView(rootView);

        // Views
        newRoomMemberEmail = rootView.findViewById(R.id.newMemberEmail);
        addNewMemberButton = rootView.findViewById(R.id.addMemberButton);
        discardChangesButton = rootView.findViewById(R.id.discardChangesButton);

        ((View) rootView.getParent()).setBackgroundColor(Color.TRANSPARENT);

        // Setting up listeners
        addNewMemberButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = newRoomMemberEmail.getText().toString();

                if (Helpers.Validation.email(email)){
                    onEmailValidListener.onEmailValid(email);
                }
                else Toast.makeText(context,context.getString(R.string.email_is_not_valid),Toast.LENGTH_SHORT).show();
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

    public interface OnEmailValidListener{
        public void onEmailValid(String email);
    }
}
