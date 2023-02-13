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

    // Views
    private Button positiveButton,negativeButton;
    private TextView titleView, descriptionView;
    private String title, description, positiveButtonText, negativeButtonText;

    // Listeners
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

        // Views
        positiveButton = this.findViewById(R.id.positiveButton);
        negativeButton = this.findViewById(R.id.negativeButton);
        titleView = this.findViewById(R.id.title);
        descriptionView = this.findViewById(R.id.content);

        // Setting values
        titleView.setText(title == null ? getContext().getString(R.string.nothing_here) : title);
        descriptionView.setText(description == null ? getContext().getString(R.string.nothing_here) : description);
        positiveButton.setText(positiveButtonText == null ? getContext().getString(R.string.ok) : positiveButtonText);
        negativeButton.setText(negativeButtonText == null ? getContext().getString(R.string.dismiss) : negativeButtonText);

        // Setting listeners
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
