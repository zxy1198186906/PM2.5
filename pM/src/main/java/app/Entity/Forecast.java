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

    public Forecast(){}

    public Forecast(Long id, Long userid, String time, String outdoor, String indoor) {
        this.id = id;
        this.userid = userid;
        this.time = time;
        this.outdoor = outdoor;
        this.indoor = indoor;
    }

    /**
     * for debugging
     */
    public void print(){
        Log.e("id", String.valueOf(id));
        Log.e("userid", String.valueOf(userid));
        Log.e("time", String.valueOf(time));
        Log.e("outdoor", String.valueOf(outdoor));
        Log.e("indoor", String.valueOf(indoor));
    }


    public static JSONObject toJsonObject(Forecast forcast, String user_id){
        JSONObject object = new JSONObject();
        try{
            object.put("userid", user_id);
            object.put("time", forcast.getTime());
            object.put("outdoor", forcast.getOutdoor());
            object.put("indoor", forcast.getIndoor());
        }catch (JSONException e){
            e.printStackTrace();
        }

        return object;
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
