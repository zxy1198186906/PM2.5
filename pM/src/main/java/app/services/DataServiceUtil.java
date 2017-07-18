package app.services;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import app.Entity.Forecast;
import app.Entity.State;
import app.utils.ACache;
import app.utils.Const;
import app.utils.DBConstants;
import app.utils.DBHelper;
import app.utils.FileUtil;
import app.utils.ShortcutUtil;
import app.utils.StableCache;

import static nl.qbusict.cupboard.CupboardFactory.cupboard;

/**
 * Created by liuhaodong1 on 16/6/9.
 * This is a service intended to perform database related operations.
 */
public class DataServiceUtil {

    public static final String TAG = "DataService";

    private static DataServiceUtil instance = null;

    private DBHelper dbHelper = null;

    private Context mContext;

    private State state = null; // a stable holding of current state of DB

    private double venVolToday = 0.0;

    private long IDToday;

    private double PM25Density = -1; //a stable holding of pm density of cache.

    private double PM25Today;

    private int PM25Source = 1; //a stable holding of pm source

    private ACache aCache;

    private StableCache stableCache;

    private String token = null;

    private String version = Const.CURRENT_VERSION;

    private String device_number = null;

    private int avg_rate = 12; //a stable holding of current hearth rate of cache.

    public static DataServiceUtil getInstance(Context context){
       if(instance == null){
           instance = new DataServiceUtil(context.getApplicationContext());
       }
        return instance;
    }

    private DataServiceUtil(Context context){
        mContext = context;
        aCache = ACache.get(context);
        stableCache = StableCache.getInstance(context);
        initDefaultData();
        DBInitial();
    }

    private void DBInitial() {

        if (null == dbHelper)
            dbHelper = new DBHelper(mContext.getApplicationContext());

        /**Get states of today **/
        List<State> states = getStateToday();
        if (states.isEmpty()) {
            state = null;
            PM25Today = 0.0;
            venVolToday = 0.0;
            IDToday = 0L;
        } else {
            PM25Today = 0.0;
            venVolToday = 0.0;
            State state = states.get(states.size() - 1);
            this.state = state;
            state.print();
            Log.v("DataServiceUtils","calculate pm25today & venvoltoday");
            for(int i = 0;i < states.size();i++){
                State theState = states.get(i);
                PM25Today += Double.parseDouble(theState.getPm25());
                venVolToday += Double.parseDouble(theState.getVentilation_rate());
            }
//            Log.v("Crysa_log", "print97");
//            PM25Today = Double.parseDouble(state.getPm25());
//            venVolToday = Double.parseDouble(state.getVentilation_volume());
            IDToday = state.getId() + 1;
        }
    }

    /**
     * get states today
     * @return
     */
    public List<State> getStateToday(){
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        calendar.set(year, month, day, 0, 0, 0);
        Long nowTime = calendar.getTime().getTime();
        calendar.set(year, month, day, 23, 59, 59);
        Long nextTime = calendar.getTime().getTime();
        /**Get states of today **/
        List<State> states = cupboard().withDatabase(db).query(State.class).withSelection("time_point > ? AND time_point < ?", nowTime.toString(), nextTime.toString()).list();

        return states;
    }


    /**
     *
     */
    public void initDefaultData(){
        PM25Density = getPMDensityFromCache();
        PM25Source = getPM25Source();
        avg_rate = getHearthRateFromCache();
    }

    /**
     * density: (ug/m3)
     * breath:  (L/min)
     * Calculate today the number of pm2.5 breathed until now
     *
     * @param longi the longitude to be saved
     * @param lati  the latitude to be saved
     * @return the saved state
     */
    public State calculatePM25(double longi, double lati,int steps) {

        Log.e(TAG, "calculatePM25 " + System.currentTimeMillis());
        Double breath = 0.0;
        Double density = PM25Density;
        token = getTokenFromCache();
        boolean isConnected = ShortcutUtil.isNetworkAvailable(mContext);

        Const.MotionStatus mMotionStatus = MotionServiceUtil.getMotionStatus(steps);
        double static_breath =
                ShortcutUtil.calStaticBreath(stableCache.getAsString(Const.Cache_User_Weight));

        if (static_breath == 0.0) {
            static_breath = 6.6; // using the default one
        }
        if (mMotionStatus == Const.MotionStatus.STATIC) {
            breath = static_breath;
        } else if (mMotionStatus == Const.MotionStatus.WALK) {
            breath = static_breath * 2.1;
        } else if (mMotionStatus == Const.MotionStatus.RUN) {
            breath = static_breath * 6;
        }

        venVolToday += breath;
        BigDecimal new_venVolToday = new BigDecimal(venVolToday);
        venVolToday = new_venVolToday.setScale(5,BigDecimal.ROUND_HALF_UP).doubleValue();

        Double cal_breath = breath / 1000; //change L/min to m3/min
        BigDecimal new_breath = new BigDecimal(cal_breath);
        cal_breath = new_breath.setScale(5,BigDecimal.ROUND_HALF_UP).doubleValue();

        double pm25_intake = density * cal_breath;
        BigDecimal new_pm25_intake = new BigDecimal(pm25_intake);
        pm25_intake = new_pm25_intake.setScale(5,BigDecimal.ROUND_HALF_UP).doubleValue();

        PM25Today += density * cal_breath;
//        boolean source = Const.IS_USE_805;
        String data_source = aCache.getAsString(Const.Cache_Data_Source);
        if(data_source.equals("3")) {
            data_source = "3";
            device_number = aCache.getAsString(Const.Device_Id);
        }

        try {
            state = new State(IDToday, String.valueOf(getUserIdFromCache()), token, Long.toString(System.currentTimeMillis()),
                    String.valueOf(longi),
                    String.valueOf(lati),
                    String.valueOf(getInOutDoorFromCache()),
                    mMotionStatus == Const.MotionStatus.STATIC ? "1" : mMotionStatus == Const.MotionStatus.WALK ? "2" : "3",
                    Integer.toString(steps), String.valueOf(avg_rate),String.valueOf(breath),
                    String.valueOf(venVolToday), density.toString(), String.valueOf(pm25_intake),
                    data_source, device_number, version, isConnected ? 1 : 0);
            FileUtil.appendStrToFile(TAG, "state initial success");
        }catch (Exception e){
            FileUtil.appendErrorToFile(TAG,"calculatePM25 error in initialized state");
        }
        return state;
    }

    /**
     * Caculate user's outdoor and indoor time
     * @return
     */
    public Forecast calculateOutAndInTime(List<State> states){
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        calendar.set(year, month, day, 0, 0, 0);
        Long nowTime = calendar.getTime().getTime();

        int outdoorTime = 0;
        int indoorTime = 0;
        double outdoorVol = 0.0;
        double indoorVol = 0.0;

        for (State state: states){
            if (Integer.valueOf(state.getUserid()) == getUserIdFromCache()){
                // 计算在室外的时间
                if (state.getOutdoor().equals("1")){
                    outdoorTime++;
                    outdoorVol += Double.valueOf(state.getVentilation_volume());
                }
                // 计算在室内的时间
                if (state.getOutdoor().equals("0")){
                    indoorTime++;
                    indoorVol += Double.valueOf(state.getVentilation_volume());
                }
            }
        }

        indoorVol /= indoorTime;
        outdoorVol /= outdoorTime;

        try{
            Forecast forecast = new Forecast(nowTime, Long.valueOf(getUserIdFromCache()),
                    states.get(states.size() - 1).getTime_point(), String.valueOf(outdoorTime),
                    String.valueOf(indoorTime), String.valueOf(indoorVol), String.valueOf(outdoorVol));
            return forecast;
        }catch (Exception e){
            return null;
        }
    }

    /**
     * DB Operations, insert a calculated pm state model to DB
     * @param state the state you want to insert
     */
    public void insertState(State state) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Log.e("DataService","insert state");
        state.print();
        cupboard().withDatabase(db).put(state);
        IDToday++;
    }

    /**
     * DB Operations, insert a forecast entity
     * @param forecast
     */
    public void insertForecast(Forecast forecast){
        if (forecast != null){
            FileUtil.appendStrToFile(TAG, "start save indoor and outdoor info");
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            Log.e("DataService", "insert forecast");
            forecast.print();
            cupboard().withDatabase(db).put(forecast);
        }
    }

    /**
     * for testing
     * @return
     */
    public List<Forecast> getAllForecast(){
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        return cupboard().withDatabase(db).query(Forecast.class).list();
    }

    public boolean getLastForecast(){
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        calendar.set(year, month, day, 0, 0, 0);
        Long nowTime = calendar.getTime().getTime();
        calendar.set(year, month, day, 23, 59, 59);
        Long nextTime = calendar.getTime().getTime();
        /**Get forcast of today **/
        List<Forecast> forecasts = cupboard().withDatabase(db).query(Forecast.class).withSelection("time > ? AND time < ?", nowTime.toString(), nextTime.toString()).list();

        if (forecasts.size() > 0)
            return false;

        return true;
    }

    /**
     * DB Operations, update the upload value of state
     * @param state the state to update
     * @param upload the value to update
     */
    public void updateStateUpLoad(State state, int upload) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DBConstants.DB_MetaData.STATE_HAS_UPLOAD, upload);
        cupboard().withDatabase(db).update(State.class, values, "id = ?", state.getId() + "");
    }

    /**
     * return tomorrow's PM25 value by last seven days
     * @return
     */
    public double[] getTomorrowForecast(){
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        List<Forecast> forecasts = new ArrayList<>();
        forecasts = cupboard().withDatabase(db).query(Forecast.class).list();
        int num = 0;
        if (forecasts.size() > 7){
            forecasts = forecasts.subList(0, 7);
            num = 7;
        }else{
            num = forecasts.size();
        }

//        ArrayList<Integer> indoorTime = new ArrayList<>();
//        ArrayList<Integer> outdoorTime = new ArrayList<>();
        int indoorTime = 0;
        int outdoorTime = 0;
        double indoorVol = 0.0;
        double outdoorVol = 0.0;
        ArrayList<Double> rates = new ArrayList<>();
        for (Forecast forecast : forecasts){
            indoorTime += Integer.valueOf(forecast.getIndoor());
            outdoorTime += Integer.valueOf(forecast.getOutdoor());
            indoorVol += Double.valueOf(forecast.getVentilationVolIndoor());
            outdoorVol += Double.valueOf(forecast.getVentilationVolOutdoor());
            rates.add(outdoorTime * 1.0 / (indoorTime + outdoorTime));
        }

        double proptionn = 0.0;
        for (int i = 0; i < forecasts.size(); i++){
            proptionn += rates.get(i) * (Integer.valueOf(forecasts.get(i).getIndoor()) + Integer.valueOf(forecasts.get(i).getOutdoor())) * 1.0 / (indoorTime + outdoorTime);
        }

        double outRes = 24 * 60 / 2 * proptionn * (outdoorVol / num);
        double inRes = 24 * 60 / 2 * (1 - proptionn) * (indoorVol / num);

        if (num == 0) {
            return new double[]{0.0, 0.0};
        }else {
            return new double[]{outRes, inRes};
        }
    }

    /**
     * get last indoor and outdoor ratio during last seven days
     * @return the ratio of last week.
     */
    public double getLastSevenDaysInOutRatio() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        List<State> states = new ArrayList<State>();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        for (int i = 1; i <= 7; i++) {
            Calendar nowTime = Calendar.getInstance();
            nowTime.add(Calendar.DAY_OF_MONTH, -i);
            nowTime.add(Calendar.MINUTE, -5);
            String left = formatter.format(nowTime.getTime());
            nowTime.add(Calendar.MINUTE, 10);
            String right = formatter.format(nowTime.getTime());
            List<State> temp = cupboard().withDatabase(db).query(State.class).withSelection("time_point > ? AND time_point < ?", left, right).list();
            states.addAll(temp);
        }
        int count = 0;
        for (State state : states) {
            if (state.getOutdoor().equals(LocationServiceUtil.Outdoor)) {
                count++;
            }
        }
        if (states.size() == 0) {
            return 0.5;
        }
        double ratio = count * 1.0 / states.size();
        return ratio;
    }

    /**
     * get list of data to upload from DB
     * @return a list of state for uploading
     */
    public List<State> getPMDataForUpload(){
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        return cupboard().withDatabase(db).query(State.class).withSelection(DBConstants.DB_MetaData.STATE_HAS_UPLOAD +
                "=? AND " + DBConstants.DB_MetaData.STATE_CONNECTION + "=?", "0", "1").list();
    }

    /**
     * see the number of unupload
     * @return
     */
    public Integer seeUnUploadStatesNumber(){
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        return cupboard().withDatabase(db).query(State.class).withSelection(DBConstants.DB_MetaData.STATE_HAS_UPLOAD +
                "=? AND " + DBConstants.DB_MetaData.STATE_CONNECTION + "=?", "0", "1").list().size();
    }

    /*
    * reset the tmporary data for a new day after day surpass
     */
    public void reset(){
        IDToday = 0;
        PM25Today = 0;
        venVolToday = 0;
    }


    /*
    * refresh the temporary value of DataServiceUtil
     */
    public void refresh(){
        DBInitial();
    }

    /****************** Below here are cache operations ******************/
    /*
    * set user's id into cache
     */
    public void cacheUsrId(int id){
        aCache.put(Const.Cache_User_Id, String.valueOf(id));
    }

    /**
     * cache the pm density and source.
     * @param density the current pm density to cache
     * @param source the source of pm data to cache
     */
    public void cachePMResult(double density,int source){
        aCache.put(Const.Cache_PM_Density, density);
        aCache.put(Const.Cache_PM_Source, String.valueOf(source));
        PM25Density = density;
    }

    /**
     * cache the Cache_Access_Token
     * @param Token
     */
    public void cacheToken(String Token){
        aCache.put(Const.Cache_Access_Token,Token);
    }

    /**
     * cache the consecutive times for searching pm2.5 density and failed.
     * @param count the consecutive failed times for getting pm2.5 result
     */
    public void cacheSearchPMFailed(int count){
        aCache.put(Const.Cache_Search_PM_Failed_Count, String.valueOf(count));
    }

    /**
     * cache the in and outdoor status
     * @param inOutDoor must be 1 or 0, 1 means: outdoor 0 means: indoor
     * @see LocationServiceUtil indoor and outdoor
     */
    public void cacheInOutdoor(int inOutDoor){
        if(inOutDoor == LocationServiceUtil.Indoor ||
                inOutDoor == LocationServiceUtil.Outdoor)
            aCache.put(Const.Cache_Indoor_Outdoor, String.valueOf(inOutDoor));
        else FileUtil.appendErrorToFile(TAG,
                "cacheInOutdoor value == "+inOutDoor+" not meet the requirement");
    }

    /**
     * cache name of the city
     * @param city the name of city
     */
    public void cacheCityName(String city){
        aCache.put(Const.Cache_City,city);
    }

    /**
     * cache the current location
     * @param location the location to cache
     */
    public void cacheLocation(Location location){
        if(location != null) {
            aCache.put(Const.Cache_Latitude, location.getLatitude());
            aCache.put(Const.Cache_Longitude, location.getLongitude());
        }else {
            FileUtil.appendErrorToFile(TAG,
                    "cacheLocation location == NULL");
        }
    }

    /**
     * cache the current location
     * @param latitude the latitude of current location
     * @param longitude the longitude of current location
     */
    public void cacheLocation(double latitude,double longitude){
        aCache.put(Const.Cache_Latitude, latitude);
        aCache.put(Const.Cache_Longitude, longitude);
    }

    /*
    * cache if the insertion data process has surpass a day
    * if surpass, need to reset aimed temporary values.
    * isReset, true: isSurpass, false: not Surpass
     */
    public void cacheSurpass(boolean isReset){
        aCache.put(Const.Cache_Surpass_Reset,isReset);
    }

    /**
     * cache the maximum location.
     * @param location the maximum location to cache
     */
    public void cacheMaxLocation(Location location){
        aCache.put(Const.Cache_Last_Max_Lati, location.getLatitude());
        aCache.put(Const.Cache_Last_Max_Lati, location.getLongitude());
    }

    /*
    * cache the maximum location by latitude and longitude
     */
    public void cacheMaxLocation(double latitude,double longitude){
        aCache.put(Const.Cache_Last_Max_Lati, latitude);
        aCache.put(Const.Cache_Last_Max_Longi, longitude);
    }

    /**
     * cache the time for uploading to see if next time need to upload.
     * @param time the current time for uploading
     */
    public void cacheLastUploadTime(long time){
        aCache.put(Const.Cache_DB_Lastime_Upload, String.valueOf(time));
    }

    public void cacheLastSearchCityTime(long time){
        aCache.put(Const.Cache_Lasttime_Search_City,String.valueOf(time));
    }

    /**
     * cache the number of user's steps.
     * @param stepNum the number of user's steps
     */
    public void cacheStepNum(int stepNum){
        aCache.put(Const.Cache_Step_Num,String.valueOf(stepNum));
    }

    public void cacheHearthRate(int rate){
        aCache.put(Const.Cache_Hearth_Rate,String.valueOf(rate));
        avg_rate = rate;
    }

    public void cacheIsSearchDensity(boolean isTo){
        aCache.put(Const.Cache_Is_To_Search_Density,String.valueOf(isTo));
    }

    public void cacheHasStepCounter(boolean has){
        aCache.put(Const.Cache_Has_Step_Counter,String.valueOf(has));
    }

    /**
     * get the user's id from cache, if failed, return 0
     * @return the user id
     */
    public int getUserIdFromCache(){
        String usrIdStr = aCache.getAsString(Const.Cache_User_Id);
        if(ShortcutUtil.isStringOK(usrIdStr)){
            try{
                return Integer.valueOf(usrIdStr);
            }catch (Exception e){
                FileUtil.appendErrorToFile(TAG,"getUserIdFromCache parsing error"
                        +" in/outdoor == "+usrIdStr);
                return 0;
            }
        }else {
            return 0;
        }
    }

    /**
     * get the tooken from cache, if failed, return 0
     * @return the tooken
     */
    public String getTokenFromCache(){
        String TokenStr = aCache.getAsString(Const.Cache_Access_Token);
        if(ShortcutUtil.isStringOK(TokenStr)){
            try{
                return String.valueOf(TokenStr);
            }catch (Exception e){
                FileUtil.appendErrorToFile(TAG,"getTookenFromCache parsing error"
                        +" in/outdoor == "+TokenStr);
                return TokenStr;
            }
        }else {
            return TokenStr;
        }
    }

    public double getPMDensityFromCache(){

        String tmp = aCache.getAsString(Const.Cache_PM_Density);
        if(ShortcutUtil.isStringOK(tmp)){
            try {
                return Double.valueOf(aCache.getAsString(Const.Cache_PM_Density));
            } catch (Exception e) {
                FileUtil.appendErrorToFile(TAG, "getPMDensityFromCache parsing error "+
                        tmp);
                cacheIsSearchDensity(true);
                return 0.0;
            }
        }else {
            FileUtil.appendErrorToFile(TAG, "getPMDensityFromCache PM Density == NULL");
            cacheIsSearchDensity(true);
        }
        return 0.0;
    }

    public int getPMSourceFromCache(){
        String tmp = aCache.getAsString(Const.Cache_PM_Source);
        if (ShortcutUtil.isStringOK(tmp)) {
            try {
                return Integer.valueOf(aCache.getAsString(Const.Cache_PM_Source));
            } catch (Exception e) {
                FileUtil.appendErrorToFile(TAG, "getPMSourceFromCache parsing error "+
                        tmp);
            }
        }
        return 0;
    }

    /**
     * get indoor and outdoor status from cache.
     * @return 0 means indoor, 1 means outdoor
     */
    public int getInOutDoorFromCache(){

        int result = LocationServiceUtil.Indoor;
        String str = aCache.getAsString(Const.Cache_Indoor_Outdoor);
        if(ShortcutUtil.isStringOK(str)){
            try {
                result =  Integer.valueOf(str);
            }catch (Exception e){
                result = LocationServiceUtil.Indoor;
                FileUtil.appendErrorToFile(TAG,"getInOutDoorFromCache parsing error"
                        +" in/outdoor == "+str);
            }
        }
        return result;
    }

    /**
     * get the value for is surpassing a day from cache
     * @return true : surpass, false: not surpass
     */
    public boolean isSurpassFromCache(){
        boolean reset = false;
        String tmp = null;
        try {
            tmp = aCache.getAsString(Const.Cache_Surpass_Reset);
            if(ShortcutUtil.isStringOK(tmp))
                reset = Boolean.valueOf(tmp);
        }catch (Exception e){
            FileUtil.appendErrorToFile(TAG,"isSurpassFromCache parsing error"
                    +" isSurpass == "+tmp);
            return false;
        }
        return reset;
    }

    /**
     * get the current longitude from cache.
     * @return 0.0 if not found
     */
    public double getLongitudeFromCache(){
        String longi = aCache.getAsString(Const.Cache_Longitude);
        if(ShortcutUtil.isStringOK(longi)){
            try{
                return Double.valueOf(longi);
            }catch (Exception e){
                FileUtil.appendErrorToFile(TAG,"getLongitudeFromCache parsing error"
                        +" lati == "+longi);
                return 0.0;
            }
        }
        return 0.0;
    }

    /**
     * get the current latitude from cache.
     * @return 0.0 if not found
     */
    public double getLatitudeFromCache(){
        String lati = aCache.getAsString(Const.Cache_Latitude);
        if(ShortcutUtil.isStringOK(lati)){
            try{
                return Double.valueOf(lati);
            }catch (Exception e){
                FileUtil.appendErrorToFile(TAG,"getLatitudeFromCache parsing error"
                        +" lati == "+lati);
                return 0.0;
            }
        }
        return 0.0;
    }

    /**
     * get the maximum latitude from cache.
     * @return 0.0 if not found
     */
    public double getMaxLatitudeFromCache(){
        String lati = aCache.getAsString(Const.Cache_Last_Max_Lati);
        if(ShortcutUtil.isStringOK(lati)){
            try{
                return Double.valueOf(lati);
            }catch (Exception e){
                FileUtil.appendErrorToFile(TAG,"getMaxLatitudeFromCache parsing error"
                        +" lati == "+lati);
                return 0.0;
            }
        }
        return 0.0;
    }

    /**
     * get the maximum longitude from cache.
     * @return 0.0 if not found
     */
    public double getMaxLongitudeFromCache(){
        String longi = aCache.getAsString(Const.Cache_Last_Max_Longi);
        if(ShortcutUtil.isStringOK(longi)){
            try {
                return Double.valueOf(longi);
            }catch (Exception e){
                FileUtil.appendErrorToFile(TAG,"getMaxLongitudeFromCache parsing error"
                        +" longi == "+longi);
                return 0.0;
            }
        }
        return 0.0;
    }

    /**
     * get the current city name from cache.
     * @return the string of city name, return "null" if failed
     */
    public String getCityNameFromCache(){
        String city = aCache.getAsString(Const.Cache_City);
        if(ShortcutUtil.isStringOK(city)){
            return city;
        }
        return "null";
    }

    /**
     * get the number of failed searching pm density from cache.
     * @return the consecutive search pm density failed count, if failed, 0
     */
    public int getSearchFailedCountFromCache(){
        int result = 0;
        String count = aCache.getAsString(Const.Cache_Search_PM_Failed_Count);
        if(ShortcutUtil.isStringOK(count)){
            try {
                result =  Integer.valueOf(count);
            }catch (Exception e){
                result = 0;
                FileUtil.appendErrorToFile(TAG,"getSearchFailedCountFromCache parsing error"
                +" count == "+count);
            }
        }
        return result;
    }

    /**
     * get the current step number of user
     * @return the current step number
     */
    public int getStepNumFromCache(){
        int result = 0;
        String num = aCache.getAsString(Const.Cache_Step_Num);
        if(ShortcutUtil.isStringOK(num)){
            try {
                result = Integer.valueOf(num);
            }catch (Exception e){
                result = 0;
                FileUtil.appendErrorToFile(TAG,"getStepNumFromCache parsing error "+
                num);
            }
        }
        return result;
    }

    /*
     * get the hearth rate of the user from cache
     * @return the hearth rate
     */
    public int getHearthRateFromCache(){
        int rate = 12;
        String str = aCache.getAsString(Const.Cache_Hearth_Rate);
        if(ShortcutUtil.isStringOK(str)){
            try {
                rate = Integer.valueOf(str);
            }catch (Exception e){
                rate = 12;
                FileUtil.appendErrorToFile(TAG,"getHearthRateFromCache parsing error "+
                str);
            }
        }
        return rate;
    }

    /**
     * check if it is time for uploading db data from cache.
     * @return true: is going to upload, false: otherwise
     */
    public boolean isToUpload() {
        String tmp = aCache.getAsString(Const.Cache_DB_Lastime_Upload);
        if (ShortcutUtil.isStringOK(tmp)) {
            try {
                long time = Long.valueOf(tmp);
                if (System.currentTimeMillis() - time > Const.Min_Upload_Check_Time) {
                    return true;
                }
            } catch (Exception e) {
                FileUtil.appendErrorToFile(TAG, "isToUpload failed, parsing error lasttime == "
                        +tmp);
                cacheLastUploadTime(System.currentTimeMillis());
                return false;
            }
        }else {
            cacheLastUploadTime(System.currentTimeMillis());
            return true;
        }
        return false;
    }

    public boolean isToSearchCity(){
        String tmp = aCache.getAsString(Const.Cache_Lasttime_Search_City);
        if(ShortcutUtil.isStringOK(tmp)){
            try {
                long time = Long.valueOf(tmp);
                if(System.currentTimeMillis() - time > Const.Min_Search_City_Time){
                    return true;
                }
            }catch (Exception e){
                FileUtil.appendErrorToFile(TAG, "isToSearchCity failed, parsing error lastime == "
                        +tmp);
                cacheLastUploadTime(System.currentTimeMillis());
                return false;
            }
        }else {
            cacheLastUploadTime(System.currentTimeMillis());
            return true;
        }
        return false;
    }

    public boolean isToSearchPMDensity(){
        String tmp = aCache.getAsString(Const.Cache_Is_To_Search_Density);
        if(ShortcutUtil.isStringOK(tmp)){
            try {
               return Boolean.valueOf(tmp);
            }catch (Exception e){
                FileUtil.appendErrorToFile(TAG, "isToSearchPMDensity failed, parsing error tmp == "
                        +tmp);
                cacheIsSearchDensity(false);
                return false;
            }
        }
        return false;
    }

    public boolean isHasStepCounter(){
        String tmp = aCache.getAsString(Const.Cache_Has_Step_Counter);
        if(ShortcutUtil.isStringOK(tmp)){
            try {
                return Boolean.valueOf(tmp);
            }catch (Exception e){
                FileUtil.appendErrorToFile(TAG, "isHasStepCounter failed, parsing error tmp == "
                        +tmp);
                cacheHasStepCounter(false);
                return false;
            }
        }
        return false;
    }

    public State getCurrentState(){
        return state;
    }

    public void setCurrentState(State state){
        this.state = state;
    }

    public double getPM25Density() {
        return PM25Density;
    }

    public double getPM25Today() {
//        SQLiteDatabase db = dbHelper.getReadableDatabase();
//        Calendar calendar = Calendar.getInstance();
//        int year = calendar.get(Calendar.YEAR);
//        int month = calendar.get(Calendar.MONTH);
//        int day = calendar.get(Calendar.DAY_OF_MONTH);
//        calendar.set(year, month, day, 0, 0, 0);
//        Long nowTime = calendar.getTime().getTime();
//        calendar.set(year, month, day, 23, 59, 59);
//        Long nextTime = calendar.getTime().getTime();
//        /**Get states of today **/
//        List<State> states = cupboard().withDatabase(db).query(State.class).withSelection("time_point > ? AND time_point < ?", nowTime.toString(), nextTime.toString()).list();
//        if (states.isEmpty()) {
//            PM25Today = 0.0;
//        } else {
//            for(int i = 0;i < states.size();i++){
//                State theState = states.get(i);
//                PM25Today += Double.parseDouble(theState.getPm25());
//            }
//        }
        return PM25Today;
    }

    public int getPM25Source() {
        return PM25Source;
    }

    public long getIDToday() {
        return IDToday;
    }

    public double getVenVolToday() {
        return venVolToday;
    }

    public DBHelper getDBHelper() {
        return dbHelper;
    }


}
