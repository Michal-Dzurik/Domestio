package sk.dzurikm.domestio.adapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.LinkedList;
import java.util.List;

import sk.dzurikm.domestio.R;
import sk.dzurikm.domestio.models.Room;

public class StringSpinnerAdapter extends ArrayAdapter<String> {

    LayoutInflater flater;

    public StringSpinnerAdapter(Activity context, LinkedList<String> list){

        super(context,0, list);
        flater = context.getLayoutInflater();
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable
            View convertView, @NonNull ViewGroup parent)
    {
        return initView(position, convertView, parent);
    }

    @Override
    public View getDropDownView(int position, @Nullable
            View convertView, @NonNull ViewGroup parent)
    {
        return initView(position, convertView, parent);
    }

    private View initView(int position, View convertView,
                          ViewGroup parent)
    {
        // It is used to set our custom view.
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.small_spinner_item_layout, parent, false);
        }

        String item = getItem(position);

        TextView title = (TextView) convertView.findViewById(R.id.title);
        title.setText(item);


        return convertView;
    }

}