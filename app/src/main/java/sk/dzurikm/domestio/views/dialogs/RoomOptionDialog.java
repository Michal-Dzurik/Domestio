package sk.dzurikm.domestio.views.dialogs;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import sk.dzurikm.domestio.R;
import sk.dzurikm.domestio.models.Room;

public class RoomOptionDialog extends BottomSheetDialogFragment {

    // Views
    private View rootView;
    private TextView roomTitle,roomDescription, roomTitleHint,roomDescriptionHint, roomTitleButton,roomDescriptionButton;

    // Needed variables
    private Context context;
    private FragmentManager fragmentManager;
    private Room room;


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

        roomTitle = rootView.findViewById(R.id.roomName);
        roomTitleButton = rootView.findViewById(R.id.roomNameEditButton);
        roomTitleHint = rootView.findViewById(R.id.roomNameHint);

        roomDescription = rootView.findViewById(R.id.roomDescription);
        roomDescriptionButton = rootView.findViewById(R.id.roomDescriptionEditButton);
        roomDescriptionHint = rootView.findViewById(R.id.roomDescriptionHint);

        roomTitleHint.setText(room.getTitle());
        roomDescriptionHint.setText(room.getDescription());


        ((View) rootView.getParent()).setBackgroundColor(Color.TRANSPARENT);

    }

    public void setRoom(Room room) {
        this.room = room;
    }
}
