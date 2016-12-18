package app.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;


/**
 * Created by crysa on 2016/5/27.
 */
public class AlarmWatchReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent i=new Intent(context,WatchService.class);
        context.startService(i);
    }
}