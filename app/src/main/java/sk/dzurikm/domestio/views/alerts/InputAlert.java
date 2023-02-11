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

    // Views
    private Button positiveButton,negativeButton;
    private TextView titleView,descriptionView;
    private EditText input;

    // Additional variables
    private String title,hint, positiveButtonText, negativeButtonText, description;
    private boolean required;
    // Listeners
    private View.OnClickListener positiveListener,negativeListener;

    public InputAlert(Context context) {
        super(context);
        required = false;
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
        descriptionView = this.findViewById(R.id.content);

        // Setting values
        titleView.setText(title == null ? "Nothing here" : title);
        positiveButton.setText(positiveButtonText == null ? getContext().getString(R.string.ok) : positiveButtonText);
        negativeButton.setText(negativeButtonText == null ? getContext().getString(R.string.dismiss)  : negativeButtonText);
        descriptionView.setText(description == null ? ""  : description);
        if (description == null){
            this.findViewById(R.id.content).setVisibility(View.GONE);
        }
        else{
            descriptionView.setText(description);
        }
        input.setHint(hint);

        if (required) negativeButton.setVisibility(View.GONE);

        // Setting up listeners
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

    public void setDescription(String description) {
        this.description = description;
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

    public void setRequired(boolean required) {
        this.required = required;
    }

    public void setPositiveButtonText(String positiveButtonText) {
        this.positiveButtonText = positiveButtonText;
    }

    public void setNegativeButtonText(String negativeButtonText) {
        this.negativeButtonText = negativeButtonText;
    }


}
