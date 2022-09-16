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
import android.widget.TextView;

import sk.dzurikm.domestio.R;


public class InputAlert extends Dialog {
    private Button positiveButton,negativeButton;
    private TextView titleView;
    private EditText input;
    private String title,hint, positiveButtonText, negativeButtonText;
    private View.OnClickListener positiveListener,negativeListener;

    public InputAlert(Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Turning on and off some things to make it better
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.input_alert_layout);
        this.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        positiveButton = this.findViewById(R.id.positiveButton);
        negativeButton = this.findViewById(R.id.negativeButton);
        input = this.findViewById(R.id.input);
        titleView = this.findViewById(R.id.title);
        this.findViewById(R.id.content).setVisibility(View.GONE);

        // Setting values
        titleView.setText(title == null ? "Nothing here" : title);
        positiveButton.setText(positiveButtonText == null ? "OK" : positiveButtonText);
        negativeButton.setText(negativeButtonText == null ? "Dismiss" : negativeButtonText);
        input.setHint(hint);

        positiveButton.setOnClickListener(positiveListener);
        negativeButton.setOnClickListener(negativeListener);

        setOnDismissListener(new OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                input.setText("");
            }
        });
    }

    public void setPositiveButtonOnClickListener(View.OnClickListener positiveButtonClickListener){
        positiveListener = positiveButtonClickListener;
    }

    public void setNegativeButtonOnClickListener(View.OnClickListener  negativeButtonClickListener){
        negativeListener = negativeButtonClickListener;
    }

    public void setTitle(String title){
        this.title = title;
    }

    public void setHint(String hint) {
        this.hint = hint;
    }

    public EditText getInput() {
        return input;
    }

    public void setPositiveButtonText(String positiveButtonText) {
        this.positiveButtonText = positiveButtonText;
    }

    public void setNegativeButtonText(String negativeButtonText) {
        this.negativeButtonText = negativeButtonText;
    }


}
