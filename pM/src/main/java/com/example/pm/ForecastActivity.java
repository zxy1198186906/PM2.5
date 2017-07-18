package com.example.pm;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import app.Entity.Forecast;
import app.Entity.State;
import app.services.DataServiceUtil;
import app.utils.DBHelper;
import app.utils.HttpUtil;
import app.utils.VolleyQueue;

import static com.facebook.FacebookSdk.getApplicationContext;
import static nl.qbusict.cupboard.CupboardFactory.cupboard;

public class ForecastActivity extends Activity {

    private Button mButton;
    private Button HttpButton;
    private Button InfoButton;
    private DBHelper dbHelper;
    private TextView AQIText;
    private TextView HTEMPText;
    private TextView LTEMPText;
    private TextView PM25Text;
    private TextView confirm;
    private TextView TOMOPM25Text;
    private String AQI = "-";
    private String HTEMP = "-";
    private String LTEMP = "-";
    private String PM25 = "-";
    private String TOMOPM25 = "";

    private DataServiceUtil mDataService;
    private NotificationManager mNotifyMgr;
    private Notification notification;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forecast);

        dbHelper = new DBHelper(getApplicationContext());
        mNotifyMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        PendingIntent contentIntent = PendingIntent.getActivity(getApplicationContext(), 0, new Intent(this, ForecastActivity.class), 0);

        notification = new Notification.Builder(this)
                .setSmallIcon(R.drawable.icon)
                .setContentTitle("PM 警报")
                .setContentText("PM爆表了")
                .setContentIntent(contentIntent)
                .build();

        viewInitial();
        listenerInital();

    }

    /**
     * Init all the views
     */
    private void viewInitial(){

        AQIText = (TextView) findViewById(R.id.AQI);
        HTEMPText = (TextView) findViewById(R.id.HTEMP);
        LTEMPText = (TextView) findViewById(R.id.LTEMP);
        PM25Text = (TextView) findViewById(R.id.PM25);
        TOMOPM25Text = (TextView) findViewById(R.id.TOMO_PM25);
        confirm = (TextView) findViewById(R.id.forecast_sure);

        AQIText.setText(AQI);
        HTEMPText.setText(HTEMP);
        LTEMPText.setText(LTEMP);
        PM25Text.setText(PM25);

        mDataService = DataServiceUtil.getInstance(getApplicationContext());

        mButton = (Button) findViewById(R.id.forecast_test_db);
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<Forecast> forecasts =  DataServiceUtil.getInstance(getApplicationContext()).getAllForecast();
                for (Forecast forecast: forecasts){
                    forecast.print();
                }
                Log.e("forcast number", String.valueOf(forecasts.size()));
                Log.e("unupload", String.valueOf(DataServiceUtil.getInstance(getApplicationContext()).seeUnUploadStatesNumber()));
            }
        });

        HttpButton = (Button) findViewById(R.id.forecast_test_http);
        HttpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pmWarningDetecter();
                checkPMDataForUpload();

            }
        });

        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ForecastActivity.this.finish();
            }
        });

        InfoButton = (Button) findViewById(R.id.forecast_test_info);
        InfoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mNotifyMgr.notify(0, notification);
                mDataService.insertForecast(mDataService.calculateOutAndInTime(mDataService.getStateToday()));
            }
        });

        String url = HttpUtil.Predict_url + DataServiceUtil.getInstance(getApplicationContext()).getCityNameFromCache();
        url = url.substring(0, url.length() - 1);
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, new JSONObject(), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.e("Wea_Back", response.toString());

                try {
                    AQI = response.getString("AQI");
                    AQIText.setText(AQI);
                    HTEMP = response.getString("HTEMP");
                    HTEMPText.setText(HTEMP);
                    LTEMP = response.getString("LTEMP");
                    LTEMPText.setText(LTEMP);
                    PM25 = response.getString("PM25");
                    PM25Text.setText(PM25);
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

        VolleyQueue.getInstance(getApplicationContext()).addToRequestQueue(request);

        // Get predict data from database
        double[] inOutData = DataServiceUtil.getInstance(getApplicationContext()).getTomorrowForecast();

        if (inOutData[1] < 1.0) {
            TOMOPM25 = "运行时间过短，无法预测！";
        }else {
            TOMOPM25 = "in:" + String.valueOf(inOutData[0]) + " out:" + String.valueOf(inOutData[1]);
        }

        TOMOPM25Text.setText(TOMOPM25);

    }

    /**
     * Init all the listener
     */
    private void listenerInital(){

    }


    public void checkPMDataForUpload() {
        final DataServiceUtil dataServiceUtil = DataServiceUtil.getInstance(getApplicationContext());

        dataServiceUtil.cacheLastUploadTime(System.currentTimeMillis());
        int idStr = dataServiceUtil.getUserIdFromCache();
        String tokenStr=dataServiceUtil.getTokenFromCache();

        if (idStr != 0) {
            final List<State> states = dataServiceUtil.getPMDataForUpload();
            //FileUtil.appendStrToFile(DBRunTime, "1.checkPMDataForUpload upload batch start size = " + states.size());
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
                        Log.e("token_status", String.valueOf(token_status));
                        if (token_status == 1) {
                            String value = response.getString("succeed_count");
                            if (Integer.valueOf(value) == size) {
                                for (int i = 0; i < size; i++) {
                                    dataServiceUtil.updateStateUpLoad(states.get(i), 1);
                                }
                            }
                            if (Integer.valueOf(value) == 0){
                                for (int i = 0; i < 1000; i++) {
                                    dataServiceUtil.updateStateUpLoad(states.get(i), 1);
                                }
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    error.printStackTrace();
                    Log.e("size", String.valueOf(states.size()));
                    if (states.size() > 1000) {
                        for (int i = 0; i < 1000; i++) {
                            dataServiceUtil.updateStateUpLoad(states.get(i), 1);
                        }
                    }
                }
            }) {
                public Map<String, String> getHeaders() throws AuthFailureError {
                    HashMap<String, String> headers = new HashMap<String, String>();
                    headers.put("Content-Type", "application/json; charset=utf-8");
                    return headers;
                }
            };

            VolleyQueue.getInstance(getApplicationContext()).addToRequestQueue(jsonObjectRequest);
        }
    }

    public void notifyUser(){

        NotificationManager mNotifyMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, new Intent(this, ForecastActivity.class), 0);

        Notification notification = new Notification.Builder(this)
                .setSmallIcon(R.drawable.icon)
                .setContentTitle("PM 警报")
                .setContentText("PM爆表了")
                .setDefaults(Notification.DEFAULT_VIBRATE)
                .setDefaults(Notification.DEFAULT_SOUND)
                .setContentIntent(contentIntent)
                .build();
        mNotifyMgr.notify(0, notification);
    }

    public void pmWarningDetecter(){
        String url = HttpUtil.Predict_url + DataServiceUtil.getInstance(getApplicationContext()).getCityNameFromCache();
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

        VolleyQueue.getInstance(getApplicationContext()).addToRequestQueue(request);
    }
}
