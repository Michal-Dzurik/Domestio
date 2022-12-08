package sk.dzurikm.domestio.views.dialogs;

import static android.content.Context.MODE_PRIVATE;
import static sk.dzurikm.domestio.helpers.Constants.Settings.COLLAPSED_STATE;
import static sk.dzurikm.domestio.helpers.Constants.Validation.EMAIL;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import sk.dzurikm.domestio.R;
import sk.dzurikm.domestio.activities.HomeActivity;
import sk.dzurikm.domestio.helpers.Constants;
import sk.dzurikm.domestio.helpers.Helpers;

public class SettingsDialog extends BottomSheetDialogFragment {
    // Views
    private View rootView;

    // Needed variables
    private Context context;
    private FragmentManager fragmentManager;

    private Button notificationEditButton;
    private Spinner taskCollapseEditSpinner;
    private TextView taskCollapse;

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor sharedPreferencesEditor;


    public SettingsDialog(Context context, FragmentManager fragmentManager) {
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
        rootView = View.inflate(getContext(), R.layout.settings_dialog_layput, null);
        dialog.setContentView(rootView);

        // Views
        notificationEditButton = rootView.findViewById(R.id.notificationEditButton);
        taskCollapseEditSpinner = rootView.findViewById(R.id.taskCollapseEditSpinner);
        taskCollapse = rootView.findViewById(R.id.taskCollapse);

        ((View) rootView.getParent()).setBackgroundColor(Color.TRANSPARENT);

        // SharedPreferences
        sharedPreferences = context.getSharedPreferences(Constants.SHARED_PREFERENCES_KEY,MODE_PRIVATE);
        sharedPreferencesEditor = sharedPreferences.edit();

        String collapseState = sharedPreferences.getString(COLLAPSED_STATE,String.valueOf(Constants.Settings.CollapsingState.COLLAPSED));

        taskCollapse.setText(Helpers.allLowercaseButFirstUppercase(collapseState));

        // Setting up listeners
        notificationEditButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction("android.settings.APP_NOTIFICATION_SETTINGS");
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                //for Android 5-7
                intent.putExtra("app_package", context.getPackageName());
                intent.putExtra("app_uid", context.getApplicationInfo().uid);

                // for Android 8 and above
                intent.putExtra("android.provider.extra.APP_PACKAGE", context.getPackageName());

                startActivity(intent);
            }
        });

        List<String> spinnerData = Arrays.asList(Constants.Settings.CollapsingState.getNames());
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_dropdown_item,spinnerData);
        taskCollapseEditSpinner.setAdapter(spinnerAdapter);
        taskCollapseEditSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                taskCollapse.setText(Helpers.allLowercaseButFirstUppercase(spinnerData.get(position)));

                sharedPreferencesEditor.putString(COLLAPSED_STATE,spinnerData.get(position).toUpperCase());
                System.out.println(sharedPreferences.getString(COLLAPSED_STATE,""));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        taskCollapseEditSpinner.setSelection(spinnerAdapter.getPosition(Helpers.allLowercaseButFirstUppercase(collapseState)));

    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        sharedPreferencesEditor.commit();
        ((HomeActivity) context).preferencesHasBeenChanged();
    }

    @NonNull @Override public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override public void onShow(DialogInterface dialogInterface) {
                BottomSheetDialog bottomSheetDialog = (BottomSheetDialog) dialogInterface;
                setupFullHeight(bottomSheetDialog);
            }
        });
        return  dialog;
    }


    private void setupFullHeight(BottomSheetDialog bottomSheetDialog) {
        FrameLayout bottomSheet = (FrameLayout) bottomSheetDialog.findViewById(R.id.design_bottom_sheet);
        BottomSheetBehavior behavior = BottomSheetBehavior.from(bottomSheet);
        ViewGroup.LayoutParams layoutParams = bottomSheet.getLayoutParams();

        int windowHeight = getWindowHeight();
        if (layoutParams != null) {
            layoutParams.height = windowHeight;
        }
        bottomSheet.setLayoutParams(layoutParams);
        behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
    }

    private int getWindowHeight() {
        // Calculate window height for fullscreen use
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) getContext()).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics.heightPixels;
    }
}
