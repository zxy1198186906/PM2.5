package app.services;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;

import com.example.pm.MyApplication;

public class CountStepService extends Service {
    private boolean isCountStart;
    private MotionServiceUtil motionServiceUtil = null;

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        isCountStart = false;
        motionServiceUtil = MotionServiceUtil.getInstance(getApplicationContext());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        motionServiceUtil.start(true);
//        if (isCountStart == false) {   //启动计步器
//            isCountStart = true;
//            motionServiceUtil.start(true);
//            Log.v("Crysa_motion", "start");
//        } else {   //关闭计步器
//            isCountStart = false;
//            motionServiceUtil.stop();
//            Log.v("Crysa_motion", "stop");
//        }


        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        int time_interval = 15 * 1000;//10秒
        long triggerAtTime = SystemClock.elapsedRealtime() + time_interval;

        Intent intent_re = new Intent(this, AlarmCountStepReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(this, 0, intent_re, 0);
        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtTime, pi);

        return super.onStartCommand(intent, flags, startId);
    }
}
