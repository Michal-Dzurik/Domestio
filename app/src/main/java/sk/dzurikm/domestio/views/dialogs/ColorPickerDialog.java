package sk.dzurikm.domestio.views.dialogs;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;

import sk.dzurikm.domestio.R;

public class ColorPickerDialog extends Dialog {

    // Views
    private Button positiveButton,negativeButton;
    private TextView titleView;
    private CardView colorPreview;
    private ImageView colorWheel;

    // Additional variables
    private String title, positiveButtonText, negativeButtonText;
    Bitmap bitmap;

    // Listeners
    private View.OnClickListener negativeListener;
    private OnColorSelectedListener colorSelectedListener;

    public ColorPickerDialog(@NonNull Context context) {
        super(context);
    }

    public ColorPickerDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
    }

    protected ColorPickerDialog(@NonNull Context context, boolean cancelable, @Nullable OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Turning on and off some things to make it better
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.color_picker_dialog_layout);

        // Views
        positiveButton = this.findViewById(R.id.positiveButton);
        negativeButton = this.findViewById(R.id.negativeButton);
        titleView = this.findViewById(R.id.title);
        colorPreview = this.findViewById(R.id.colorPreview);
        colorWheel = this.findViewById(R.id.colorWheel);

        this.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        // Setting values
        titleView.setText(title == null ? "Nothing here" : title);
        positiveButton.setText(positiveButtonText == null ? "OK" : positiveButtonText);
        negativeButton.setText(negativeButtonText == null ? "Dismiss" : negativeButtonText);

        // So we can get color of pixel
        colorWheel.setDrawingCacheEnabled(true);
        colorWheel.buildDrawingCache(true);

        // Setting up listeners
        positiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                colorSelectedListener.colorSelected(colorPreview.getCardBackgroundColor().getDefaultColor());
            }
        });
        negativeButton.setOnClickListener(negativeListener);

        colorWheel.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_MOVE){
                    try{
                        bitmap = colorWheel.getDrawingCache();

                        int pixels =  bitmap.getPixel((int) event.getX(),(int) event.getY());

                        int r = Color.red(pixels);
                        int g = Color.green(pixels);
                        int b = Color.blue(pixels);

                        colorPreview.setCardBackgroundColor(Color.rgb(r,g,b));
                    }catch (IllegalArgumentException e){

                    }
                }

                return true;
            }

        });
    }

    public void setColorSelectedListener(OnColorSelectedListener colorSelectedListener) {
        this.colorSelectedListener = colorSelectedListener;
    }

    public void setNegativeButtonOnClickListener(View.OnClickListener  negativeButtonClickListener){
        negativeListener = negativeButtonClickListener;
    }

    public void setTitle(String title){
        this.title = title;
    }

    public void setPositiveButtonText(String positiveButtonText) {
        this.positiveButtonText = positiveButtonText;
    }


    public void setNegativeButtonText(String negativeButtonText) {
        this.negativeButtonText = negativeButtonText;
    }

    public interface OnColorSelectedListener{
        public void colorSelected(int color);
    }
}
