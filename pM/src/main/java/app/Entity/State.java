package app.Entity;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import app.model.BaseModel;
import app.utils.DBConstants;
import app.utils.ShortcutUtil;
import nl.qbusict.cupboard.annotation.Column;

/**
 * Created by sweet on 15-10-4.
 */
public class State {

    @Column(DBConstants.DB_MetaData.STATE_ID_COL)
    private Long id;
    @Column(DBConstants.DB_MetaData.STATE_USER_ID_COL)
    private String userid;
    @Column(DBConstants.DB_MetaData.STATE_DATABASE_ACCESS_TOKEN_COL)
    private String datebase_access_token;
    @Column(DBConstants.DB_MetaData.STATE_TIME_POINT_COL)
    private String time_point;
    @Column(DBConstants.DB_MetaData.STATE_LONGTITUDE_COL)
    private String longtitude;
    @Column(DBConstants.DB_MetaData.STATE_LATITUDE_COL)
    private String latitude;
    @Column(DBConstants.DB_MetaData.STATE_OUTDOOR_COL)
    private String outdoor;
    @Column(DBConstants.DB_MetaData.STATE_STATUS_COL)
    private String status;
    @Column(DBConstants.DB_MetaData.STATE_STEPS_COL)
    private String steps;
    @Column(DBConstants.DB_MetaData.STATE_HEART_RATE_COL)
    private String heart_rate;
    @Column(DBConstants.DB_MetaData.STATE_VENTILATION_RATE_COL)
    private String ventilation_rate;
    @Column(DBConstants.DB_MetaData.STATE_VENTILATION_VOLUME_COL)
    private String ventilation_volume;
    @Column(DBConstants.DB_MetaData.STATE_DENSITY_COL)
    private String pm25_concen;
    @Column(DBConstants.DB_MetaData.STATE_PM25_COL)
    private String pm25_intake;
    @Column(DBConstants.DB_MetaData.STATE_SOURCE_COL)
    private String pm25_datasource;
    @Column(DBConstants.DB_MetaData.STATE_PM25_MONITOR_COL)
    private String pm25_monitor;
    @Column(DBConstants.DB_MetaData.STATE_APP_VERSION_COL)
    private String APP_version;
    @Column(DBConstants.DB_MetaData.STATE_HAS_UPLOAD)
    private int upload; //0 not upload, 1 upload success
    @Column(DBConstants.DB_MetaData.STATE_CONNECTION)
    private int connection;//0 not connect to network, 1 connect to network

    public State() {
    }

    public State(Long id, String userid, String datebase_access_token, String time_point, String longtitude, String latitude,
                 String outdoor, String status, String steps, String heart_rate, String ventilation_rate, String ventilation_volume, String pm25_concen, String pm25_intake, String pm25_datasource, String pm25_monitor, String APP_version,
                 int connection) {
        this(id, userid, datebase_access_token, time_point, longtitude, latitude, outdoor, status, steps, heart_rate, ventilation_rate, ventilation_volume, pm25_concen, pm25_intake, pm25_datasource, pm25_monitor, APP_version, 0,connection);
    }

    public State(Long id, String userid, String datebase_access_token, String time_point, String longtitude, String latitude,
                 String outdoor, String status, String steps, String heart_rate, String ventilation_rate, String ventilation_volume, String pm25_concen, String pm25_intake, String pm25_datasource, String pm25_monitor, String APP_version,
                 int upload,int connection) {
        this.id = id;
        this.userid = userid;
        this.datebase_access_token = datebase_access_token;
        this.time_point = time_point;
        this.longtitude = longtitude;
        this.latitude = latitude;
        this.outdoor = outdoor;
        this.status = status;
        this.steps = steps;
        this.heart_rate = heart_rate;
        this.ventilation_rate = ventilation_rate;
        this.ventilation_volume = ventilation_volume;
        this.pm25_concen = pm25_concen;
        this.pm25_intake = pm25_intake;
        this.pm25_datasource = pm25_datasource;
        this.pm25_monitor = pm25_monitor;
        this.APP_version = APP_version;
        this.upload = upload;
        this.connection = connection;
    }

    public void print() {
        Log.e("id", String.valueOf(id));
        Log.e("userid", String.valueOf(userid));
        Log.e("token", String.valueOf(datebase_access_token));
        Log.e("time_point", String.valueOf(time_point));
        Log.e("time_point_format", ShortcutUtil.refFormatNowDate(Long.valueOf(time_point)));
        Log.e("longtitude", String.valueOf(longtitude));
        Log.e("latitude", String.valueOf(latitude));
        Log.e("outdoor", String.valueOf(outdoor));
        Log.e("status", String.valueOf(status));
        Log.e("steps", String.valueOf(steps));
        Log.e("heart_rate", String.valueOf(heart_rate));
        Log.e("ventilation_rate", String.valueOf(ventilation_rate));
        Log.e("ventilation_volume", String.valueOf(ventilation_volume));
        Log.e("pm25_concen", String.valueOf(pm25_concen));
        Log.e("pm25_intake", String.valueOf(pm25_intake));
        Log.e("pm25_datasource", String.valueOf(pm25_datasource));
        Log.e("pm25_monitor", String.valueOf(pm25_monitor));
        Log.e("APP_version", String.valueOf(APP_version));
        Log.e("upload", String.valueOf(upload));
        Log.e("connection", String.valueOf(connection));
    }

    public static JSONObject toJsonobject(State state, String user_id) {
        JSONObject object = new JSONObject();
        try {
            object.put("userid", user_id);
            object.put("time_point", ShortcutUtil.refFormatNowDate(Long.valueOf(state.getTime_point())).substring(0, 19));
            object.put("database_access_token", state.getDatebase_access_token());
            object.put("longitude", state.getLongtitude());
            object.put("latitude", state.getLatitude());
            object.put("outdoor", state.getOutdoor());
            object.put("status", state.getStatus());
            object.put("steps", state.getSteps());
            object.put("heart_rate", state.getHeart_rate());
            object.put("ventilation_rate", state.getVentilation_rate());
            object.put("ventilation_vol", state.getVentilation_volume());
            object.put("pm25_concen",state.getDensity());
            object.put("pm25_intake", state.getPm25());
            object.put("pm25_datasource", state.getSource());
            object.put("pm25_monitor", state.getPm25_monitor());
            object.put("APP_version", state.getApp_version());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return object;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public String getDatebase_access_token() {
        return datebase_access_token;
    }

    public void setDatebase_access_token(String datebase_access_token) {
        this.datebase_access_token = datebase_access_token;
    }

    public String getTime_point() {
        return time_point;
    }

    public void setTime_point(String time_point) {
        this.time_point = time_point;
    }

    public String getLongtitude() {
        return longtitude;
    }

    public void setLongtitude(String longtitude) {
        this.longtitude = longtitude;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getOutdoor() {
        return outdoor;
    }

    public void setOutdoor(String outdoor) {
        this.outdoor = outdoor;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSteps() { return steps; }

    public void setSteps(String steps) { this.steps = steps; }

    public String getHeart_rate() { return heart_rate; }

    public void setHeart_rate(String heart_rate) { this.heart_rate = heart_rate; }

    public String getVentilation_rate() {
        return ventilation_rate;
    }

    public void setVentilation_rate(String ventilation_rate) {
        this.ventilation_rate = ventilation_rate;
    }

    public String getVentilation_volume() {
        return ventilation_volume;
    }

    public void setVentilation_volume(String ventilation_volume) {
        this.ventilation_volume = ventilation_volume;
    }

    public String getDensity() {
        return pm25_concen;
    }

    public void setDensity(String pm25_concen) {
        this.pm25_concen = pm25_concen;
    }

    public String getPm25() {
        return pm25_intake;
    }

    public void setPm25(String pm25_intake) {
        this.pm25_intake = pm25_intake;
    }

    public String getSource() {
        return pm25_datasource;
    }

    public void setSource(String pm25_datasource) {
        this.pm25_datasource = pm25_datasource;
    }

    public String getPm25_monitor() { return pm25_monitor; }

    public void setPm25_monitor(String pm25_monitor) {
        this.pm25_monitor = pm25_monitor;
    }

    public String getApp_version() { return APP_version; }

    public void setApp_version(String APP_version) {
        this.APP_version = APP_version;
    }

    public int getUpload() {
        return upload;
    }

    public void setUpload(int upload) {
        this.upload = upload;
    }

    public int getConnection() {
        return  connection;
    }

    public void setConnection(int connection) {
        this.connection = connection;
    }

}
