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
import com.google.android.material.card.MaterialCardView;

import sk.dzurikm.domestio.R;
import sk.dzurikm.domestio.helpers.Helpers;

public class AddRoomDialog extends BottomSheetDialogFragment {
    // Views
    private View colorPickInput;
    private TextView colorPickerText;
    private MaterialCardView colorPickerPreview;
    private View rootView;

    // Needed variables
    private Context context;
    private FragmentManager fragmentManager;

    public AddRoomDialog(Context context, FragmentManager fragmentManager) {
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
        rootView = View.inflate(getContext(), R.layout.add_room_dialog_layput, null);
        dialog.setContentView(rootView);

        // Views
        colorPickInput = rootView.findViewById(R.id.pickColorInput);
        colorPickerText = rootView.findViewById(R.id.colorPickerText);
        colorPickerPreview = rootView.findViewById(R.id.colorPickerPreview);

        ((View) rootView.getParent()).setBackgroundColor(Color.TRANSPARENT);

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



    }




}
