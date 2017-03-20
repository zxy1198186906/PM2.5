package com.example.pm;

import android.app.Activity;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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

import static nl.qbusict.cupboard.CupboardFactory.cupboard;

public class ForecastActivity extends Activity {

    private Button mButton;
    private Button HttpButton;
    private DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forecast);

        dbHelper = new DBHelper(getApplicationContext());

        viewInitial();
        listenerInital();
    }

    /**
     * Init all the views
     */
    private void viewInitial(){
        mButton = (Button) findViewById(R.id.forecast_test_db);
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<Forecast> forecasts =  DataServiceUtil.getInstance(getApplicationContext()).getAllForecast();
                for (Forecast forecast: forecasts){
                    forecast.print();
                }
                Log.e("unupload", String.valueOf(DataServiceUtil.getInstance(getApplicationContext()).seeUnUploadStatesNumber()));
            }
        });

        HttpButton = (Button) findViewById(R.id.forecast_test_http);
        HttpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = HttpUtil.Predict_url + DataServiceUtil.getInstance(getApplicationContext()).getCityNameFromCache();
                url = url.substring(0, url.length() - 1);
                StringRequest request = new StringRequest(
                        StringRequest.Method.GET,
                        url,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                Log.e("Back_Weather", response.toString());
                            }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                error.printStackTrace();
                            }
                });

//                checkPMDataForUpload();
                VolleyQueue.getInstance(getApplicationContext()).addToRequestQueue(request);
            }
        });
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
                        Toast.makeText(getApplicationContext(), response.toString() + " test ", Toast.LENGTH_LONG).show();
                        int token_status = response.getInt("token_status");
                        if (token_status == 1) {
                            String value = response.getString("succeed_count");
                            if (Integer.valueOf(value) == size) {
                                for (int i = 0; i < size; i++) {
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
}
