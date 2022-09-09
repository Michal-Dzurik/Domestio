package sk.dzurikm.domestio.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import sk.dzurikm.domestio.models.Task;

public class NotificationService extends Service {
    private final String DOCUMENT_TASKS = "Tasks";
    private final String DOCUMENT_ROOMS = "Rooms";
    FirebaseFirestore db;
    FirebaseAuth auth;

    @Override
    public void onCreate() {
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        startListeners();
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public void startListeners(){
        Query taskQuery = db.collection(DOCUMENT_TASKS);
        Query roomQuery = db.collection(DOCUMENT_ROOMS);

        taskQuery.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                // On task change
                if (error != null)
                    Log.w("DB", "ERROR : ", error);

                if (value != null && !value.isEmpty()) {
                    for (DocumentChange doc : value.getDocumentChanges()) {
                        switch (doc.getType()){
                            case ADDED:
                                Log.w("DB", "ADDED : " + doc.getDocument().getData());
                                //doc.getDocument().getData().get();
                                Task taskAdded = new Task();
                                try{
                                    taskAdded.cast(doc.getDocument().getId(),doc.getDocument().getData());

                                    // Checking if task is for this user
                                    if (taskAdded.getReceiverId().equals(auth.getUid())){
                                        // Notify and add task others
                                    }
                                }catch (Error e){}
                                break;
                            case REMOVED:
                                Log.w("DB", "REMOVED : " + doc.getDocument().getData());
                                break;
                            case MODIFIED:
                                Log.w("DB", "MODIFIED : " + doc.getDocument().getData());
                                break;
                        }
                    }
                }
            }
        });

        roomQuery.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                // On room change
                if (error != null)
                    Log.w("DB", "ERROR : ", error);

                if (value != null && !value.isEmpty()) {
                    for (DocumentChange doc : value.getDocumentChanges()) {
                        switch (doc.getType()){
                            case ADDED:
                                Log.w("DB", "ADDED : " + doc.getDocument().getData());
                                //doc.getDocument().getData().get();
                                break;
                            case REMOVED:
                                Log.w("DB", "REMOVED : " + doc.getDocument().getData());
                                break;
                            case MODIFIED:
                                Log.w("DB", "MODIFIED : " + doc.getDocument().getData());
                                break;
                        }
                    }
                }
            }
        });
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
