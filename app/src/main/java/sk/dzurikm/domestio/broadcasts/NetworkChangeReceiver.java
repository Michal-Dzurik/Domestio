package sk.dzurikm.domestio.broadcasts;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import sk.dzurikm.domestio.helpers.DataStorage;
import sk.dzurikm.domestio.helpers.Helpers;

public class NetworkChangeReceiver extends BroadcastReceiver {
    NetworkStatusChangeListener listener;

    public NetworkChangeReceiver(NetworkStatusChangeListener listener) {
        this.listener = listener;
    }

    public NetworkChangeReceiver() {
    }

    @Override
    public void onReceive(final Context context, final Intent intent) {

        int status = Helpers.Network.getConnectivityStatusString(context);
        System.out.println("Status- " + status);
        if (status == Helpers.Network.NETWORK_STATUS_NOT_CONNECTED) {
            System.out.println("Not connected anywhere");
            DataStorage.connected = false;
        } else {
            DataStorage.connected = true;
        }

        if (listener != null) listener.onChange();
    }

    public interface NetworkStatusChangeListener{
        public void onChange();
    }
}
