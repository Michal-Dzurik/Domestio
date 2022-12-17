package sk.dzurikm.domestio.views.alerts;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Objects;

import sk.dzurikm.domestio.R;


public class ReauthenticateAlert extends Dialog {

    // Views
    private Button positiveButton,negativeButton;
    private TextView titleView,content;
    private EditText input;

    // Listeners
    private View.OnClickListener negativeListener;
    private OnCompleteListener<Void> positiveListener;

    // Needed variables
    private Context context;

    public ReauthenticateAlert(Context context) {
        super(context);
        this.context = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Turning on and off some things to make it better
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.input_alert_layout);
        this.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        // Views
        positiveButton = this.findViewById(R.id.positiveButton);
        negativeButton = this.findViewById(R.id.negativeButton);
        input = this.findViewById(R.id.input);
        titleView = this.findViewById(R.id.title);
        content = this.findViewById(R.id.content);

        // Setting values
        titleView.setText(context.getString(R.string.enter_your_password));
        positiveButton.setText(context.getString(R.string.ok));
        negativeButton.setText(context.getString(R.string.back));
        input.setHint("*********");
        content.setText(context.getString(R.string.need_to_authenticate));

        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

        positiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String password = ReauthenticateAlert.this.getInput().getText().toString();
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                if (password.trim().equals("")) {
                    Toast.makeText(context, context.getString(R.string.password_is_empty),Toast.LENGTH_SHORT).show();
                    return;
                }

                AuthCredential credential = EmailAuthProvider
                        .getCredential(Objects.requireNonNull(user.getEmail()), password);

                user.reauthenticate(credential).addOnCompleteListener(positiveListener);
            }
        });
        negativeButton.setOnClickListener(negativeListener);

        setOnDismissListener(new OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                input.setText("");
            }
        });
    }

    public void setCompleteOnCompleteListener(OnCompleteListener<Void> positiveButtonClickListener){
        positiveListener = positiveButtonClickListener;
    }

    public void setNegativeButtonOnClickListener(View.OnClickListener negativeButtonClickListener){
        negativeListener = negativeButtonClickListener;
    }

    public EditText getInput() {
        return input;
    }


}
