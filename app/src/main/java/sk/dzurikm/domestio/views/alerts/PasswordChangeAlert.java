package sk.dzurikm.domestio.views.alerts;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.tasks.OnCompleteListener;

import sk.dzurikm.domestio.R;


public class PasswordChangeAlert extends Dialog {
    private Button positiveButton,negativeButton;
    private EditText passwordInput,passwordRepeatInput;
    private View.OnClickListener negativeListener;
    private OnPasswordMatchListener passwordMatchListener;

    public PasswordChangeAlert(Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Turning on and off some things to make it better
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.password_change_alert_layout);
        this.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        positiveButton = this.findViewById(R.id.positiveButton);
        negativeButton = this.findViewById(R.id.negativeButton);
        passwordInput = this.findViewById(R.id.passwordInput);
        passwordRepeatInput = this.findViewById(R.id.passwordRepeatInput);

        // Setting values
        positiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String password = passwordInput.getText().toString();
                String passwordRepeat = passwordRepeatInput.getText().toString();

                if (password.trim().equals("") || passwordRepeat.trim().equals("")) passwordMatchListener.onPasswordDontMatch();
                else if (password.equals(passwordRepeat) && password.trim().length() >= 6 && passwordRepeat.trim().length() >= 6) passwordMatchListener.onPasswordMach(password);
                else passwordMatchListener.onPasswordNotValid();
            }
        });
        negativeButton.setOnClickListener(negativeListener);

        setOnDismissListener(new OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                passwordInput.setText("");
                passwordRepeatInput.setText("");
            }
        });
    }

    public void setPasswordMatchListener(OnPasswordMatchListener passwordMatchListener) {
        this.passwordMatchListener = passwordMatchListener;
    }

    public void setNegativeButtonOnClickListener(View.OnClickListener  negativeButtonClickListener){
        negativeListener = negativeButtonClickListener;
    }


    public interface OnPasswordMatchListener{
        public void onPasswordMach(String password);
        public void onPasswordDontMatch();
        public void onPasswordNotValid();
    }
}
