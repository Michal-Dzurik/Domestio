package sk.dzurikm.domestio.views.dialogs;


import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import sk.dzurikm.domestio.R;
import sk.dzurikm.domestio.activities.YourTasksActivity;
import sk.dzurikm.domestio.helpers.DataStorage;
import sk.dzurikm.domestio.helpers.DatabaseHelper;
import sk.dzurikm.domestio.models.Task;
import sk.dzurikm.domestio.views.alerts.Alert;

public class TasksOptionDialog extends BottomSheetDialogFragment {
    // Views
    private View rootView;

    // Needed variables
    private Context context;
    private FragmentManager fragmentManager;

    // Database
    DatabaseHelper databaseHelper;

    private ImageButton editButton,deleteButton;

    private Task task;

    public TasksOptionDialog(Context context, FragmentManager fragmentManager, Task currentTask) {
        this.context = context;
        this.fragmentManager = fragmentManager;
        this.task = currentTask;

        databaseHelper = new DatabaseHelper();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @SuppressLint("RestrictedApi")
    @Override
    public void setupDialog(Dialog dialog, int style) {
        super.setupDialog(dialog,style);
        rootView = View.inflate(getContext(), R.layout.task_admin_option_dialog, null);
        dialog.setContentView(rootView);

        // Views
        editButton = rootView.findViewById(R.id.editButton);
        deleteButton = rootView.findViewById(R.id.deleteButton);

        ((View) rootView.getParent()).setBackgroundColor(Color.TRANSPARENT);

        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AddTaskDialog addTaskDialog = new AddTaskDialog(context, fragmentManager, DataStorage.users, DataStorage.rooms, new AddTaskDialog.OnTaskChangeListener() {
                    @Override
                    public void onTaskAdded(Task task) {} // We are editing

                    @Override
                    public void onTaskEdited(Task task) {
                        // when edited edit just views
                    }
                },task);
                TasksOptionDialog.this.dismiss();
                addTaskDialog.show(fragmentManager,"AddTask");
            }
        });

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Open dialog with action delete
                Alert alert = new Alert(context);
                alert.setTitle(context.getString(R.string.remove_task) + " - " + task.getHeading());
                alert.setDescription(context.getString(R.string.do_you_want_to_remove_task));
                alert.setNegativeButtonText(context.getString(R.string.no));
                alert.setPositiveButtonText(context.getString(R.string.yes));
                alert.setPositiveButtonOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Delete task
                        // TODO make alert to remove this if you are author
                        ((YourTasksActivity) context).taskRemoved(task);
                        databaseHelper.removeUnrelatedTask(task);
                        alert.dismiss();
                    }
                });
                alert.setNegativeButtonOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        alert.dismiss();
                    }
                });
                TasksOptionDialog.this.dismiss();
                alert.show();
            }
        });



    }



}
