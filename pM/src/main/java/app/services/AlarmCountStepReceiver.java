package app.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AlarmCountStepReceiver extends BroadcastReceiver {
    public AlarmCountStepReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
        Intent i=new Intent(context,CountStepService.class);
        context.startService(i);
    }
}
