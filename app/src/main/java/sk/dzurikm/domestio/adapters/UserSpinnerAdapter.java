package sk.dzurikm.domestio.adapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

import sk.dzurikm.domestio.R;
import sk.dzurikm.domestio.models.Room;
import sk.dzurikm.domestio.models.User;

public class UserSpinnerAdapter extends ArrayAdapter<User> {

    LayoutInflater flater;

    public UserSpinnerAdapter(Activity context, List<User> list){

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
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.spinner_item_layout, parent, false);
        }

        User item = getItem(position);

        TextView title = (TextView) convertView.findViewById(R.id.title);
        title.setText(item.getName());


        return convertView;
    }
}