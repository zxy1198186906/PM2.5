package app.Entity;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import app.utils.DBConstants;
import nl.qbusict.cupboard.annotation.Column;

/**
 * Created by hongjiayong on 2017/3/20.
 */

public class Forecast {
    @Column(DBConstants.DB_MetaData.FORECAST_COL_ID)
    private Long id;
    @Column(DBConstants.DB_MetaData.FORECAST_USER_ID)
    private Long userid;
    @Column(DBConstants.DB_MetaData.FORECAST_DATE)
    private String time;
    @Column(DBConstants.DB_MetaData.FORECAST_OUTDOOR_TIME)
    private String outdoor;
    @Column(DBConstants.DB_MetaData.FORECAST_INDOOR_TIME)
    private String indoor;
    @Column(DBConstants.DB_MetaData.FORECAST_VENTILATION_VOL_INDOOR)
    private String ventilationVolIndoor;

    @Column(DBConstants.DB_MetaData.FORECAST_VENTILATION_VOL_OUTDOOR)
    private String ventilationVolOutdoor;

    public Forecast(){}

    public Forecast(Long id, Long userid, String time, String outdoor, String indoor,
                    String ventilationVolIndoor, String ventilationVolOutdoor) {
        this.id = id;
        this.userid = userid;
        this.time = time;
        this.outdoor = outdoor;
        this.indoor = indoor;
        this.ventilationVolIndoor = ventilationVolIndoor;
        this.ventilationVolOutdoor = ventilationVolOutdoor;
    }

    /**
     * for debugging
     */
    public void print(){
        Log.e(String.valueOf(id) + " " + time, "userId:" + String.valueOf(userid) + " outdoor:" +
                outdoor + " indoor: " + indoor + " ventilation_vol" + ventilationVolIndoor +
                " outdoor:" + ventilationVolOutdoor);
    }


    public static JSONObject toJsonObject(Forecast forcast, String user_id){
        JSONObject object = new JSONObject();
        try{
            object.put("userid", user_id);
            object.put("time", forcast.getTime());
            object.put("outdoor", forcast.getOutdoor());
            object.put("indoor", forcast.getIndoor());
            object.put("ventilation_vol_Indoor", forcast.getVentilationVolIndoor());
            object.put("ventilation_vol_Outdoor", forcast.getVentilationVolOutdoor());
        }catch (JSONException e){
            e.printStackTrace();
        }

        return object;
    }

    public String getVentilationVolIndoor() {
        return ventilationVolIndoor;
    }

    public void setVentilationVolIndoor(String ventilationVolIndoor) {
        this.ventilationVolIndoor = ventilationVolIndoor;
    }

    public String getVentilationVolOutdoor() {
        return ventilationVolOutdoor;
    }

    public void setVentilationVolOutdoor(String ventilationVolOutdoor) {
        this.ventilationVolOutdoor = ventilationVolOutdoor;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getOutdoor() {
        return outdoor;
    }

    public void setOutdoor(String outdoor) {
        this.outdoor = outdoor;
    }

    public String getIndoor() {
        return indoor;
    }

    public void setIndoor(String indoor) {
        this.indoor = indoor;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserid() {
        return userid;
    }

    public void setUserid(Long userid) {
        this.userid = userid;
    }
}
