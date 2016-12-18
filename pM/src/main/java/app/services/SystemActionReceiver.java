package app.services;


import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.widget.Toast;

import com.example.pm.MainActivity;
import com.example.pm.MyApplication;

/**
 * Created by crysa on 2016/5/27.
 * 用户解锁屏幕时启动ForegroundService
 */
public class SystemActionReceiver extends BroadcastReceiver {
    private static final String TAG = "UserPresentReceiver";

    public void onReceive(Context context, Intent intent) {

        if (!MyApplication.isExit()) {
            Intent i = new Intent(context, ForegroundService.class);
            context.startService(i);
            Log.v("SystemActionReceiver", "isExit==false");
        } else {
            Log.v("SystemActionReceiver", "isExit==true");
        }
    }
}
