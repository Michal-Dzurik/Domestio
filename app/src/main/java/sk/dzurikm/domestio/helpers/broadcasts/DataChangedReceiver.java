package sk.dzurikm.domestio.helpers.broadcasts;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.google.firebase.firestore.DocumentChange;

import java.util.HashMap;

public class DataChangedReceiver extends BroadcastReceiver {
    private DataChangedListener dataChangedListener;

    public DataChangedReceiver(DataChangedListener dataChangedListener) {
        this.dataChangedListener = dataChangedListener;
    }

    public DataChangedReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle extras = intent.getExtras();
        HashMap<String,Object> data = (HashMap<String,Object>) extras.get("data");
        String collection = extras.getString("collection");
        DocumentChange.Type type = (DocumentChange.Type) extras.get("type");
        String documentID = extras.getString("documentID");

        dataChangedListener.onDataChanged(data,collection, documentID,type);
    }


    public interface DataChangedListener{
        public void onDataChanged(HashMap<String,Object> data,String collection,String documentID,DocumentChange.Type type);
    }
}