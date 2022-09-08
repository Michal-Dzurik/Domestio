package sk.dzurikm.domestio.helpers;


import java.util.HashMap;
import java.util.Map;

import sk.dzurikm.domestio.helpers.listeners.OnDoneListener;

public class MultipleDone {
    Map<String,Boolean> register;
    OnDoneListener listener;

    public MultipleDone() {
        register = new HashMap<>();
    }

    public void add(String id){
        register.put(id,false);
    }

    public void check(String id){
        register.put(id,true);

        if (allDone()) listener.OnDone();
    }

    private boolean allDone(){

        for (String key : register.keySet()) {
            if (register.get(key) == false) return false;
        }

        return true;
    }

    public void setOnDoneListener(OnDoneListener listener){
        this.listener = listener;
    }
}
