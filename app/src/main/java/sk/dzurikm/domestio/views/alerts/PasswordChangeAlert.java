package sk.dzurikm.domestio.views.alerts;

import static sk.dzurikm.domestio.helpers.Constants.Validation.PASSWORD_REPEAT;
import static sk.dzurikm.domestio.helpers.Constants.Validation.PASSWORD_REPEAT_DELIMITER;

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

import java.util.ArrayList;
import java.util.HashMap;

import sk.dzurikm.domestio.R;
import sk.dzurikm.domestio.helpers.Helpers;


public class PasswordChangeAlert extends Dialog {

    // Views
    private Button positiveButton,negativeButton;
    private EditText passwordInput,passwordRepeatInput;

    // Listeners
    private View.OnClickListener negativeListener;
    private OnPasswordMatchListener passwordMatchListener;

    // Validation
    Helpers.Validation validation;

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

        // Views
        positiveButton = this.findViewById(R.id.positiveButton);
        negativeButton = this.findViewById(R.id.negativeButton);
        passwordInput = this.findViewById(R.id.passwordInput);
        passwordRepeatInput = this.findViewById(R.id.passwordRepeatInput);

        // Validation
        validation = Helpers.Validation.getInstance(getContext());

        // Setting values
        positiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String password = passwordInput.getText().toString().trim();
                String passwordRepeat = passwordRepeatInput.getText().toString().trim();

                if (password.equals("") || passwordRepeat.equals("")) passwordMatchListener.onPasswordDontMatch();

                HashMap<String,String> map = new HashMap<>();
                map.put(PASSWORD_REPEAT,password + PASSWORD_REPEAT_DELIMITER + passwordRepeat);
                ArrayList<String> errors = validation.validate(map);

                if (errors == null){
                    passwordMatchListener.onPasswordMach(password);
                }
                else passwordMatchListener.onPasswordNotValid();
            }
        });

        if (negativeListener == null) {
            negativeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dismiss();
                }
            });
        }
        else negativeButton.setOnClickListener(negativeListener);


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
