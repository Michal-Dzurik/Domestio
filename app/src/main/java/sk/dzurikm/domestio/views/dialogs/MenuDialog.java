package sk.dzurikm.domestio.views.dialogs;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import sk.dzurikm.domestio.R;
import sk.dzurikm.domestio.activities.AboutMeActivity;
import sk.dzurikm.domestio.activities.HomeActivity;

public class MenuDialog extends BottomSheetDialogFragment {
    private Context context;
    private FragmentManager fragmentManager;

    private AddRoomDialog addRoomDialog;
    private AddTaskDialog addTaskDialog;

    private View rootView;
    private View addRoomButton,addTaskButton,aboutButton;

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
        addTaskButton = rootView.findViewById(R.id.createTaskButton);
        aboutButton = rootView.findViewById(R.id.aboutButton);

        addRoomDialog = new AddRoomDialog(context,fragmentManager);
        addTaskDialog = new AddTaskDialog(context,fragmentManager);


        ((View) rootView.getParent()).setBackgroundColor(Color.TRANSPARENT);

        addRoomButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MenuDialog.this.dismiss();
                addRoomDialog.show(fragmentManager,"AddRoom");
            }
        });

        addTaskButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MenuDialog.this.dismiss();
                addTaskDialog.show(fragmentManager,"AddTask");
            }
        });

        aboutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getContext(), AboutMeActivity.class);
                startActivity(i);
            }
        });
    }



}
