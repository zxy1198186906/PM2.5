package app.services;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.PowerManager;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import app.Entity.Forecast;
import app.Entity.State;
import app.model.PMModel;
import app.utils.Const;
import app.utils.FileUtil;
import app.utils.HttpUtil;
import app.utils.ShortcutUtil;
import app.utils.StableCache;
import app.utils.VolleyQueue;
import app.utils.ACache;

import com.example.pm.ForecastActivity;
import com.example.pm.ProfileFragment;
import com.example.pm.R;

import static android.content.Context.NOTIFICATION_SERVICE;
import static android.content.Context.WIFI_SERVICE;
import static com.facebook.FacebookSdk.getApplicationContext;

import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

/**
 * Created by liuhaodong1 on 16/6/2.
 * This service (receiver) is intended to running on background and do some discrete tasks.
 * Every time there is only one background service running
 * The sequence series are shown as below
 * while onReceive (every 1 minute){
 * 1.check if not running a inner task
 * 2.keep the process wake
 * 3.start inner doing some tasks
 * 4.release the process and reset some parameters
 * for start:
 * 1.
 * }
 */
public class BackgroundService extends BroadcastReceiver {

    public static final String TAG = "BackgroundService";

    private int repeatingCycle = 0; //

    private boolean isGoingToSearchPM = false;

    private boolean isGoingToGetLocation = false;

    private boolean isLocationFinished = true;

    private boolean isSearchDensityFinished = true;

    private boolean isUploadFinished = true;

    private boolean isGetStepFinished = true;

    private boolean isStoreInOutdoorFinished = true;

    private PowerManager.WakeLock wakeLock = null;

    private Context mContext = null;

    private DataServiceUtil dataServiceUtil = null;

    private MotionServiceUtil motionServiceUtil = null;

    private Location mLocation = null; //current location

    private State state = null;

    private int stepNum = 0;

    private boolean isReset = false;    //if surpass a day, then reset the value.

    private String s="now";

    private String token_status;

    private String access_token;

    private String device_number;

    private ACache aCache;

    private  ProfileFragment profileFragment;

    private StableCache stableCache;
    private String currentWifiId;

    @Override
    public void onReceive(Context context, Intent intent) {
        mContext = context;
        FileUtil.appendStrToFile(TAG, "start get wakeLock acquire");
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "backgroundWake");
        wakeLock.acquire();
        FileUtil.appendStrToFile(TAG, "wakeLock acquire");
        initInner();
        startInner();
    }

    /**
     * set default values
     */
    private void initInner() {
        mLocation = new Location("default");
        mLocation.setLatitude(0.0);
        mLocation.setLongitude(0.0);
        dataServiceUtil = DataServiceUtil.getInstance(mContext);
        motionServiceUtil = MotionServiceUtil.getInstance(mContext);
//        stableCache = StableCache.getInstance(getApplicationContext());
    }

    /**
     * getting the last cycle params
     */
    private void getLastParams() {

        isLocationFinished = true;
        isSearchDensityFinished = true;
        isUploadFinished = true;
        isGetStepFinished = true;
        repeatingCycle = (int) dataServiceUtil.getIDToday();
        double lati = dataServiceUtil.getLatitudeFromCache();
        double longi = dataServiceUtil.getLongitudeFromCache();
        stepNum = dataServiceUtil.getStepNumFromCache();
        state = dataServiceUtil.getCurrentState();
        isReset = dataServiceUtil.isSurpassFromCache();

        if (lati != 0.0 && longi != 0.0) {
            mLocation = new Location("cache");
            mLocation.setLatitude(lati);
            mLocation.setLongitude(longi);
            isGoingToGetLocation = false;
        } else {
            getLocations(1000 * 10);
            isGoingToGetLocation = false;
        }
        dataServiceUtil.initDefaultData();
    }

    /**
     * 1. init last time data
     * 2. get current location ( time limited : 15s )
     * 3. search pm result from sever (time limited : 15s)
     * 4. save database value
     */
    private void startInner() {

        getLastParams();
        FileUtil.appendStrToFile(TAG, repeatingCycle + " ");
        SimpleDateFormat sdf=new SimpleDateFormat("yyyy年MM月dd日 HH时mm分ss秒");
//        WifiManager wifi_service = (WifiManager)getApplicationContext().getSystemService(WIFI_SERVICE);
//        WifiInfo wifiInfo = wifi_service.getConnectionInfo();
//        currentWifiId = wifiInfo.getSSID();
        if (isGoingToGetLocation || repeatingCycle % 30 == 0) {
            isLocationFinished = false;
            getLocations(1000 * 10);
            Date d1=new Date();
            String sdf1=sdf.format(d1);
            Log.e(s,sdf1+" ");
        }
        if (isGoingToSearchPM || repeatingCycle % 2 == 0) {//101
            isSearchDensityFinished = false;
            searchPMResult(String.valueOf(mLocation.getLongitude()), String.valueOf(mLocation.getLatitude()));
//            Log.i("Back wifi currently:", currentWifiId.replaceAll("\"",""));
//                    Log.i("wifi from cache:", stableCache.getAsString(Const.Cache_User_Wifi));
//            Log.i("Back wifi status:", currentWifiId.replaceAll("\"","").equals(stableCache.getAsString(Const.Cache_User_Wifi))+"");
//            if(stableCache.getAsString(Const.Cache_User_Wifi) == null){
//                searchPMResult(String.valueOf(mLocation.getLongitude()), String.valueOf(mLocation.getLatitude()));
//                Log.e("BackgroundService","Now using data from server 3");
//            } else {
//                if(stableCache.getAsString(Const.Cache_User_Wifi).equals(currentWifiId.replaceAll("\"",""))){
//                    searchPMResult(stableCache.getAsString(Const.Cache_User_Device));
//                    Log.e("BackgroundService","Now using data from 805 device");
//                } else {
//                    searchPMResult(String.valueOf(mLocation.getLongitude()), String.valueOf(mLocation.getLatitude()));
//                    Log.e("BackgroundService","Now using data from server 4");
//                }
//            }
            Date d2=new Date();
            String sdf2=sdf.format(d2);
            Log.e(s,sdf2+" ");
        }
        //every 1 hour to check if some data need to be uploaded
        if (repeatingCycle % 9 == 0) {//119
            isUploadFinished = false;
            checkPMDataForUpload();
            Date d3=new Date();
            String sdf3=sdf.format(d3);
            Log.e(s,sdf3+" ");
            Toast.makeText(mContext, "上传", Toast.LENGTH_LONG).show();
        }

        onGetSteps();
        saveInOutdoor(dataServiceUtil.getStateToday());
        saveValues();
        onFinished("saveValues");
        onFinished(repeatingCycle + "end");
    }

    /**
     *
     * @param runningTime
     */
    private void getLocations(long runningTime) {

        LocationServiceUtil locationServiceUtil = LocationServiceUtil.getInstance(mContext);

        if (locationServiceUtil.getLastKnownLocation() != null) {

            mLocation = locationServiceUtil.getLastKnownLocation();
            double last_lati = dataServiceUtil.getMaxLatitudeFromCache();
            double last_longi = dataServiceUtil.getMaxLongitudeFromCache();
            boolean isEnough = ShortcutUtil.isLocationChangeEnough(last_lati, mLocation.getLatitude(),
                    last_longi, mLocation.getLongitude());
            if(isEnough){
                FileUtil.appendStrToFile(TAG, "max latitude " + last_lati + " max longitude" +
                        last_longi + " latitude " + mLocation.getLatitude() + " " +
                        "longitude " + mLocation.getLongitude());
                dataServiceUtil.cacheMaxLocation(mLocation);
                dataServiceUtil.cacheIsSearchDensity(true);
            }
            dataServiceUtil.cacheLocation(mLocation.getLatitude(), mLocation.getLongitude());
            FileUtil.appendStrToFile(repeatingCycle, "locationInitial getLastKnownLocation " +
                    String.valueOf(mLocation.getLatitude()) + " " +
                    String.valueOf(mLocation.getLongitude()));
            isLocationFinished = true;
            onFinished("getLocations getLastKnownLocation");
        }

        locationServiceUtil.setGetTheLocationListener(new LocationServiceUtil.GetTheLocation() {
            @Override
            public void onGetLocation(Location location) {
            }

            @Override
            public void onSearchStop(Location location) {

                if (location != null) {
                    Log.e(TAG, "onSearchStop location " + location.getLongitude());
                    mLocation = location;
                    double last_lati = dataServiceUtil.getMaxLatitudeFromCache();
                    double last_longi = dataServiceUtil.getMaxLongitudeFromCache();
                    boolean isEnough = false;
                    isEnough = ShortcutUtil.isLocationChangeEnough(last_lati, mLocation.getLatitude(),
                            last_longi, mLocation.getLongitude());
                    if (isEnough) {
                        FileUtil.appendStrToFile(TAG, "max latitude " + last_lati + " max longitude" +
                                last_longi + " latitude " + mLocation.getLatitude() + " " +
                                "longitude " + mLocation.getLongitude());
                        dataServiceUtil.cacheMaxLocation(mLocation);
                        dataServiceUtil.cacheIsSearchDensity(true);
                    }
                    dataServiceUtil.cacheLocation(mLocation);
                }
                isLocationFinished = true;
                onFinished("getLocations onSearchStop");
            }
        });
        locationServiceUtil.run(LocationServiceUtil.TYPE_BAIDU);
        locationServiceUtil.setTimeIntervalBeforeStop(runningTime);
    }

    private void onGetSteps(){
        isGetStepFinished = false;
        FileUtil.appendStrToFile(TAG, "start get steps");
        motionServiceUtil.setOnGetStepListener(new MotionServiceUtil.onGetStepListener() {
            @Override
            public void onGetStep(int type, int num) {
                dataServiceUtil.cacheStepNum(num);
                isGetStepFinished = true;
                onFinished("getSteps Finished");
            }
        });
        motionServiceUtil.start(true);
    }

    /**
     * Get and Update Current PM info.
     *
     * @param longitude the current zone longitude
     * @param latitude  the current zone latitude
     */
    private void searchPMResult(String longitude, String latitude) {

        String url = HttpUtil.Search_PM_url;
        String token = dataServiceUtil.getTokenFromCache();
        url = url + "?longitude=" + longitude + "&latitude=" + latitude + "&access_token=" + token;
        FileUtil.appendStrToFile(repeatingCycle, "searchPMResult " + url);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                try {
                    Log.e("Back_search",response.toString());
                    int token_status = response.getInt("token_status");
                    if (token_status != 2) {
                        int status = response.getInt("status");
                        if (status == 1) {
                            Const.IS_USE_805 = false;
                            PMModel pmModel = PMModel.parse(response.getJSONObject("data"));
                            NotifyServiceUtil.notifyDensityChanged(mContext, pmModel.getPm25());
                            double PM25Density = Double.valueOf(pmModel.getPm25());
                            int PM25Source = pmModel.getSource();
//                            aCache.put(Const.Cache_Data_Source,String.valueOf(PM25Source));

                            dataServiceUtil.cacheIsSearchDensity(false);
                            dataServiceUtil.cachePMResult(PM25Density, PM25Source);
                            dataServiceUtil.cacheSearchPMFailed(0);

                            FileUtil.appendStrToFile(TAG, "searchPMResult success, density: " + PM25Density);
                        } else {
                            FileUtil.appendErrorToFile(TAG, "searchPMResult failed, status != 1");
                        }
                    }else if (token_status == 2){
                        checkPMDataForUpload();
                        aCache.remove(Const.Cache_User_Id);
                        aCache.remove(Const.Cache_Access_Token);
                        aCache.remove(Const.Cache_User_Name);
                        aCache.remove(Const.Cache_User_Nickname);
                        aCache.remove(Const.Cache_User_Gender);
                        Activity mActivity = (Activity)mContext;
                        Intent intent = new Intent(mActivity, ForegroundService.class);
                        mActivity.stopService(intent);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    FileUtil.appendErrorToFile(TAG, "searchPMResult JSON error");
                }
                isSearchDensityFinished = true;
                onFinished("searchPMResult success");
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                isSearchDensityFinished = true;
                onFinished("searchPMResult error");
                dataServiceUtil.cacheSearchPMFailed(dataServiceUtil.getSearchFailedCountFromCache()+1);
                FileUtil.appendErrorToFile(TAG,"searchPMResult failed msg: " + error.getMessage());
            }

        });
        VolleyQueue.getInstance(mContext.getApplicationContext()).addToRequestQueue(jsonObjectRequest);
    }

    /**
     * Get and Update Current PM info.
     *
     +     * @param devId device id
     +     */
    private void searchPMResult(String devId) {
        final DateFormat format1 = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        final Date date = new Date();
        String url = HttpUtil.Search_PM_url_wifi;
        url = url + "?devid=" + devId;
        Const.Device_Number = devId;
        Const.IS_USE_805 = true;
        aCache.put(Const.Device_Id,devId);
        aCache.put(Const.Cache_Data_Source,"3");
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                try {
                    int status = response.getInt("status");

                    if (status == 1) {
                        PMModel pmModel = PMModel.parse(response.getJSONObject("data"));
                        NotifyServiceUtil.notifyDensityChanged(getApplicationContext(), pmModel.getPm25());
                        double PM25Density = Double.valueOf(pmModel.getPm25());
                        String time = String.valueOf(pmModel.getTimePoint());
                        float diff = (float)(date.getTime() - format1.parse(time).getTime())/(1000 * 60 * 60);
                        if(time != null && diff < 1.0){
                            Toast.makeText(getApplicationContext(), PM25Density+"", Toast.LENGTH_SHORT).show();
                            int PM25Source = 0;
                            dataServiceUtil.cachePMResult(PM25Density, PM25Source);
                            dataServiceUtil.cacheSearchPMFailed(0);
                            Log.i("response from ilab:",PM25Density+", "+time);
                            FileUtil.appendStrToFile(TAG, "searchPMResult success, density == " +
                                    PM25Density);
                        } else {
                            Toast.makeText(getApplicationContext(), "ilab服务器数据过期", Toast.LENGTH_SHORT).show();
                            searchPMResult(String.valueOf(dataServiceUtil.getLongitudeFromCache()),
                                    String.valueOf(dataServiceUtil.getLatitudeFromCache()));
                            Log.e("ForeGroundService","ilab服务器数据过期");
                        }

                    } else {
                        Toast.makeText(getApplicationContext(), "设备号有误", Toast.LENGTH_SHORT).show();
                        dataServiceUtil.cacheSearchPMFailed(
                                dataServiceUtil.getSearchFailedCountFromCache() + 1);
                        FileUtil.appendErrorToFile(TAG, "searchPMResult failed, status != 1");
                        searchPMResult(String.valueOf(dataServiceUtil.getLongitudeFromCache()),
                                String.valueOf(dataServiceUtil.getLatitudeFromCache()));
                        Log.e("ForeGroundService","设备号有误");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.i("response from ilab:","error");
                    FileUtil.appendErrorToFile(TAG, "searchPMResult failed, JSON parsing error");
                    Log.e("ForeGroundService","searchPMResult failed, JSON parsing error");
                    searchPMResult(String.valueOf(dataServiceUtil.getLongitudeFromCache()),
                            String.valueOf(dataServiceUtil.getLatitudeFromCache()));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(), "ilab服务器请求出错", Toast.LENGTH_SHORT).show();
                dataServiceUtil.cacheSearchPMFailed(dataServiceUtil.getSearchFailedCountFromCache() + 1);
                Log.i("response from ilab:","error");
                FileUtil.appendErrorToFile(TAG, "searchPMResult failed error msg == " +
                        error.getMessage() + " " + error);
                Log.e("ForeGroundService","ilab服务器请求出错");
                searchPMResult(String.valueOf(dataServiceUtil.getLongitudeFromCache()),
                        String.valueOf(dataServiceUtil.getLatitudeFromCache()));
            }
        });

        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(
                Const.Default_Timeout,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        VolleyQueue.getInstance(
                getApplicationContext().getApplicationContext()).addToRequestQueue(jsonObjectRequest);
    }

    /**
     * save values
     */
    private void saveValues() {

        State last = state;
        state = dataServiceUtil.calculatePM25(mLocation.getLongitude(), mLocation.getLatitude(),stepNum);
//        Log.v("Crysa_location","saveValues()lati"+mLocation.getLatitude()+"||"+"longi"+ mLocation.getLongitude());
        Log.e("Back","save state");
        state.print();
        State now = state;

        //most of cases, isReset would be false, so when isSurpass a day happened,
        //then jump to "else code block", since a new state not inserted, isSurpass always return true
        // only using isReset to jump to "if block" again.
        if (!isSurpass(last, now) || isReset) {
            dataServiceUtil.insertState(state);
            dataServiceUtil.cacheSurpass(false);
        } else {
            reset();
            dataServiceUtil.cacheSurpass(true);
        }
        Log.e(TAG, "repeating times: " + repeatingCycle);
    }

    /**
     * save in and out times
     */
    private void saveInOutdoor(List<State> states){
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        calendar.set(year, month, day, 19, 0, 0);
        Long sevenClock = calendar.getTime().getTime();
        calendar.set(year, month, day, 0, 0, 0);
        Date date = new Date();
        Long now = date.getTime();

        if (now - sevenClock >= 0){

            if (dataServiceUtil.getLastForecast()){
                pmWarningDetecter();
                dataServiceUtil.insertForecast(dataServiceUtil.calculateOutAndInTime(states));
                onFinished("save indoor and outdoor");
            }
        }
    }

    /**
     * find the data and upload the unupload data
     */
    public void checkPMDataForUpload() {
        dataServiceUtil.cacheLastUploadTime(System.currentTimeMillis());
        FileUtil.appendStrToFile(TAG, "every 10min to check pm data for upload");
        int idStr = dataServiceUtil.getUserIdFromCache();
        String tokenStr=dataServiceUtil.getTokenFromCache();

        if (idStr != 0) {
            final List<State> states = dataServiceUtil.getPMDataForUpload();
            String url = HttpUtil.UploadBatch_url;
            JSONArray array = new JSONArray();
            final int size = states.size() < 1000 ? states.size() : 1000;
            for (int i = 0; i < size; i++) {
                JSONObject tmp = State.toJsonobject(states.get(i), String.valueOf(idStr));
                array.put(tmp);
            }
            JSONObject batchData = null;
            try {
                batchData = new JSONObject();
                batchData.put("data", array);
                batchData.put("access_token", tokenStr);
                //batchData.put(tokenStr,array);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, batchData, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try {
                        Log.e("Back_upload",response.toString());
                        int token_status = response.getInt("token_status");
                        if (token_status == 1) {
                            String value = response.getString("succeed_count");
                            FileUtil.appendStrToFile(TAG, "1.checkPMDataForUpload upload success value = " + value);
                            FileUtil.appendStrToFile(TAG, "2.checkTokenStatus upload value = " + token_status);
                            if (Integer.valueOf(value) == size) {
                                for (int i = 0; i < size; i++) {
                                    dataServiceUtil.updateStateUpLoad(states.get(i), 1);
                                }
                            }
                        }
                        else if(Integer.valueOf(token_status) == 2) {
                            Log.e("BackUpload_logoff","logoff");
                            aCache.remove(Const.Cache_User_Id);
                            aCache.remove(Const.Cache_Access_Token);
                            aCache.remove(Const.Cache_User_Name);
                            aCache.remove(Const.Cache_User_Nickname);
                            aCache.remove(Const.Cache_User_Gender);
                            Activity mActivity = (Activity)mContext;
                            Intent intent = new Intent(mActivity, ForegroundService.class);
                            mActivity.stopService(intent);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    isUploadFinished = true;
                    onFinished("checkPMDataForUpload success");
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    isUploadFinished = true;
                    FileUtil.appendStrToFile(TAG, "delete 1000 uploaded states");
                    if (states.size() > 1000) {
                        for (int i = 0; i < 1000; i++) {
                            dataServiceUtil.updateStateUpLoad(states.get(i), 1);
                        }
                    }
                    onFinished("checkPMDataForUpload error");
                    if (error.getMessage() != null)
                        FileUtil.appendErrorToFile(repeatingCycle, "1.checkPMDataForUpload error getMessage" + error.getMessage());
                    if (error.networkResponse != null)
                        FileUtil.appendErrorToFile(repeatingCycle, "1.checkPMDataForUpload networkResponse statusCode " + error.networkResponse.statusCode);
                    FileUtil.appendErrorToFile(repeatingCycle, "1.checkPMDataForUpload error " + error.toString());
                }
            }) {
                public Map<String, String> getHeaders() throws AuthFailureError {
                    HashMap<String, String> headers = new HashMap<String, String>();
                    headers.put("Content-Type", "application/json; charset=utf-8");
                    return headers;
                }
            };
            VolleyQueue.getInstance(mContext.getApplicationContext()).addToRequestQueue(jsonObjectRequest);
        }
    }

    /**
     * Check if service running surpass a day
     * @param lastTime last time state info
     * @param nowTime  current state info
     * @return result true means to reset, false means keep going
     */
    private boolean isSurpass(State lastTime, State nowTime) {
        boolean result;
        String last;
        String now;
        try {
            last = ShortcutUtil.refFormatOnlyDate(Long.valueOf(lastTime.getTime_point()));
            now = ShortcutUtil.refFormatOnlyDate(Long.valueOf(nowTime.getTime_point()));
        } catch (Exception e) {
            FileUtil.appendErrorToFile(TAG,"isSurpass parsing error or state is null");
            return false;
        }
        result = !last.equals(now);
        return result;
    }

    /**
     * if Service running surpass a day, then reset data params
     */
    private void reset() {
        mLocation.setLongitude(0.0);
        mLocation.setLatitude(0.0);
        repeatingCycle = 0;
        dataServiceUtil.reset();
    }

    private void onFinished(String tag) {
        FileUtil.appendStrToFile(TAG,tag+" onFinished");
        if(isLocationFinished == true &&
                isSearchDensityFinished == true &&
                isUploadFinished == true
                &&isGetStepFinished == true
                &&isStoreInOutdoorFinished == true) {
            if (wakeLock != null && wakeLock.isHeld()) {
                wakeLock.release();
                FileUtil.appendStrToFile(TAG, "all tasks finished,release wakelock");
            }
        }
    }


    public static void runBackgroundService(Context context) {

        context = context.getApplicationContext();
        //cancelBackgroundService(context);
        FileUtil.appendStrToFile(TAG, "------begin background service------");
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent(context, BackgroundService.class);
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, i, 0);
        am.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime()
                , 60 * 1000, pi);
        //am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 1000 * 60, pi); // Millisec * Second * Minute
    }

    public static void cancelBackgroundService(Context context) {

        FileUtil.appendStrToFile(TAG, "------begin cancel background service------");
        Intent intent = new Intent(context, BackgroundService.class);
        PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent, 0);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(sender);
    }

    /**
     * If the PM is too high, the app will notify the user
     */
    public void notifyUser(){

        try {
            NotificationManager mNotifyMgr = (NotificationManager) mContext.getSystemService(NOTIFICATION_SERVICE);
            PendingIntent contentIntent = PendingIntent.getActivity(mContext, 0, new Intent(mContext, ForecastActivity.class), 0);

            Notification notification = new Notification.Builder(mContext)
                    .setSmallIcon(R.drawable.icon)
                    .setContentTitle("PM 警报")
                    .setContentText("PM爆表了")
                    .setTicker("PM爆表了")
                    .setDefaults(Notification.DEFAULT_VIBRATE)
                    .setDefaults(Notification.DEFAULT_SOUND)
                    .setAutoCancel(true)
                    .setContentIntent(contentIntent)
                    .build();
            mNotifyMgr.notify(1, notification);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void pmWarningDetecter(){
        String url = HttpUtil.Predict_url + dataServiceUtil.getCityNameFromCache();
        url = url.substring(0, url.length() - 1);
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, new JSONObject(), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.e("Wea_Back", response.toString());

                try {
                    String pm25 = response.getString("PM25");
                    int pmVal =  Integer.valueOf(pm25);

                    if (pmVal > 100){
                        notifyUser();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        }) {
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Content-Type", "application/json; charset=utf-8");
                return headers;
            }
        };

        VolleyQueue.getInstance(mContext.getApplicationContext()).addToRequestQueue(request);
    }
}
