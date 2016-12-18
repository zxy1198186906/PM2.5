package app.services;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;

public class WatchService extends Service {

    @Override
    public void onCreate() {
        Log.v("LifecycleLog", "WatchService onCreate");
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.v("LifecycleLog", "WatchService onCreate");
//        Boolean isRunning=isServiceRunning("app.services.ForegroundService");
        //
//        if(isRunning){
//            Log.v("Crysa_Watch","Fore_Yes");
//        }else {
//            Log.v("Crysa_Watch","Fore_No");
//            Intent i=new Intent(this,ForegroundService.class);
//            startService(i);
//        }


        AlarmManager alarmManager=(AlarmManager)getSystemService(Context.ALARM_SERVICE);
        int time_interval=11*1000;//11秒
        long triggerAtTime= SystemClock.elapsedRealtime()+time_interval;

        Intent intent_re=new Intent(this,AlarmWatchReceiver.class);
        PendingIntent pi=PendingIntent.getBroadcast(this,0,intent_re,0);
        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtTime, pi);
        return super.onStartCommand(intent, flags, startId);
    }

    public void CancelAlarm(){
        Intent intent=new Intent(this,AlarmWatchReceiver.class);
        PendingIntent pi=PendingIntent.getBroadcast(this, 0, intent,0);
        AlarmManager alarm=(AlarmManager)getSystemService(Context.ALARM_SERVICE);
        alarm.cancel(pi);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private boolean isServiceRunning(String ClassName) {
        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (ClassName.equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onDestroy() {
        CancelAlarm();
        super.onDestroy();
    }
}
