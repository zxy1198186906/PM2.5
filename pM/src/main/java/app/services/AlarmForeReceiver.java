package app.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.pm.MyApplication;


/**
 * Created by crysa on 2016/5/27.
 */
public class AlarmForeReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.v("Crysa_start","isExit:"+MyApplication.isExit()+"");
        if(!MyApplication.isExit()){
            Intent i=new Intent(context,ForegroundService.class);
            context.startService(i);
        }

    }
}