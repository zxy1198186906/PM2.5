package app.model;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by yaopengfei on 17/7/31.
 *
 */
public class PMModel_USA {

    public static final String PM25 = "PM25";

    public static final String TIME_POINT = "time_point";

    public static final String Source = "source";

    private String pm25;

    private String time_point;

    private int source;

    public PMModel_USA() {
        pm25 = "0";
        source = 1;
    }

    public static PMModel_USA parse(JSONObject object) throws JSONException {
        PMModel_USA bean = new PMModel_USA();
        if (object.has(PM25)) {
            bean.setPm25(object.getString(PM25));
        }if(object.has(Source)){
            bean.setSource(object.getInt(Source));
        }if (object.has(TIME_POINT)) {
            bean.setTimePoint(object.getString(TIME_POINT));
        }
        return bean;
    }

    public String getPm25() {
        return pm25;
    }

    public void setPm25(String pm25) {
        this.pm25 = pm25;
    }

    public String getTimePoint() {
        return time_point;
     }

    public String setTimePoint(String time_point){
        return this.time_point = time_point;
   }

    public int getSource() {
        return source;
    }

    public void setSource(int source) {
        this.source = source;
    }
}
