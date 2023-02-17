package sk.dzurikm.domestio.views.dialogs;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;

import sk.dzurikm.domestio.R;
import sk.dzurikm.domestio.adapters.MemberListAdapter;
import sk.dzurikm.domestio.helpers.DatabaseHelper;
import sk.dzurikm.domestio.helpers.Helpers;
import sk.dzurikm.domestio.models.Room;
import sk.dzurikm.domestio.models.User;

public class ListMembersDialog extends BottomSheetDialogFragment {
    // Views
    private RecyclerView membersListRecycler;
    private View rootView;

    // Needed variables
    private Context context;
    private FragmentManager fragmentManager;
    private ArrayList<User> users;
    private Room room;

    // Adapters
    private MemberListAdapter memberListAdapter;

    // Helpers
    private DatabaseHelper databaseHelper;
    private boolean admin;

    private LinearLayout noDataText;

    public ListMembersDialog(Context context, FragmentManager fragmentManager, ArrayList<User> users, Room room, boolean admin) {
        this.users = (ArrayList<User>) users.clone();
        this.context = context;
        this.fragmentManager = fragmentManager;
        this.room = room;
        this.admin = admin;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @SuppressLint("RestrictedApi")
    @Override
    public void setupDialog(Dialog dialog, int style) {
        super.setupDialog(dialog,style);
        rootView = View.inflate(getContext(), R.layout.list_members_dialog_layput, null);
        dialog.setContentView(rootView);

        // Views
        membersListRecycler = rootView.findViewById(R.id.membersListRecycler);
        noDataText = rootView.findViewById(R.id.noTasksText);

        ((View) rootView.getParent()).setBackgroundColor(Color.TRANSPARENT);
        BottomSheetBehavior<View> bottomSheetBehavior = BottomSheetBehavior.from(((View) rootView.getParent()));
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);

        // Helpers
        databaseHelper = new DatabaseHelper();

        // Adapters
        Log.i("USERS", String.valueOf(users));
        memberListAdapter = new MemberListAdapter(context, users, room.getAdminId(),admin, new MemberListAdapter.OnMemberRemoveListener() {
            @Override
            public void onMemberRemove(String uid) {

                databaseHelper.removeUserFromRoom(room, uid, new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()){
                            findAndRemoveUser(uid);
                            memberListAdapter.notifyDataSetChanged();
                        }
                        else {
                            Helpers.Toast.somethingWentWrong(context);
                        }

                    }
                });


            }
        });
        membersListRecycler.setAdapter(memberListAdapter);

        if (users == null || users.isEmpty()) noDataText.setVisibility(View.VISIBLE);


    }

    private void findAndRemoveUser(String uid){
        for (int i = 0; i < users.size(); i++) {
            if (users.get(i).getId().equals(uid)) {
                users.remove(i);
                return;
            }
        }
    }

}
