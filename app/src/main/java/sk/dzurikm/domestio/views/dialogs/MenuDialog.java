package sk.dzurikm.domestio.views.dialogs;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import sk.dzurikm.domestio.R;
import sk.dzurikm.domestio.activities.HomeActivity;

public class MenuDialog extends BottomSheetDialogFragment {
    private Context context;
    private FragmentManager fragmentManager;

    private AddRoomDialog addRoomDialog;

    private View rootView;
    private View addRoomButton;

    public MenuDialog(Context context, FragmentManager fragmentManager) {
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
        rootView = View.inflate(getContext(), R.layout.menu_dialog_layput, null);
        dialog.setContentView(rootView);

        addRoomButton = rootView.findViewById(R.id.CreateRoomButton);

        addRoomDialog = new AddRoomDialog(context,fragmentManager);


        ((View) rootView.getParent()).setBackgroundColor(Color.TRANSPARENT);
        /*dialog.getWindow().setDimAmount(0.0f);

        colorPickerButton = (CardView) rootView.findViewById(R.id.folderColorPickerButton);
        colorPickerButton.setCardBackgroundColor(ColorsUtil.getCurrentFolderColor(context));

        colorPickerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openColorPicker();
            }
        });*/

        addRoomButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MenuDialog.this.dismiss();
                addRoomDialog.show(fragmentManager,"AddRoom");
            }
        });
    }



}
