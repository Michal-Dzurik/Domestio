package sk.dzurikm.domestio.views.alerts;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import sk.dzurikm.domestio.R;


public class Alert extends Dialog {

    private Button positiveButton,negativeButton;
    private TextView titleView, descriptionView;
    private String title, description, positiveButtonText, negativeButtonText;
    private View.OnClickListener positiveListener,negativeListener;

    public Alert(Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Turning on and off some things to make it better
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.alert_layout);
        this.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        positiveButton = this.findViewById(R.id.positiveButton);
        negativeButton = this.findViewById(R.id.negativeButton);
        titleView = this.findViewById(R.id.title);
        descriptionView = this.findViewById(R.id.content);

        // Setting values
        titleView.setText(title == null ? "Nothing here" : title);
        descriptionView.setText(description == null ? "Nothing here" : description);
        positiveButton.setText(positiveButtonText == null ? "OK" : positiveButtonText);
        negativeButton.setText(negativeButtonText == null ? "Dismiss" : negativeButtonText);

        positiveButton.setOnClickListener(positiveListener);
        negativeButton.setOnClickListener(negativeListener);
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

    public void setDescription(String description){
        this.description = description;
    }

    public void setPositiveButtonText(String positiveButtonText) {
        this.positiveButtonText = positiveButtonText;
    }

    public void setNegativeButtonText(String negativeButtonText) {
        this.negativeButtonText = negativeButtonText;
    }
}
