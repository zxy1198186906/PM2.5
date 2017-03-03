package app.view.widget;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.pm.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import app.bluetooth.DeviceControlActivity;
import app.model.PMModel;
import app.services.DataServiceUtil;
import app.services.BackgroundService;
import app.services.ForegroundService;
import app.utils.Const;
import app.utils.FileUtil;
import app.utils.HttpUtil;
import app.utils.StableCache;
import app.utils.VolleyQueue;
import app.utils.ACache;

import com.example.pm.ProfileFragment;
import static android.content.Context.WIFI_SERVICE;
import static com.facebook.FacebookSdk.getApplicationContext;

/**
 * Created by liuhaodong1 on 16/1/30.
 */
public class DialogGetDensity extends Dialog implements View.OnClickListener
{

    public static final String TAG = "DialogGetDensity";

    private Context mContext;
    private DataServiceUtil dataServiceUtil;

    private boolean isRunning;
    private boolean isStop;
    private PMModel pmModel;
    private Double PM25Density;

    private TextView mLoading;
    private TextView mLati;
    private TextView mLongi;
    private TextView mDensity;
    private Button mCancel;
    private Button mSearch;
    private StableCache stableCache;
    private String currentWifiId;

    private BackgroundService backgroundService;
    private  ACache aCache;
    private ProfileFragment profileFragment;

    private Runnable mRunnable = new Runnable() {

        int num = 0;
        @Override
        public void run() {

            if(!isStop) {
                if (num == 0) {
                    mLoading.setText(mContext.getResources().getString(R.string.dialog_base_loading));
                } else if (num == 1) {
                    mLoading.setText(mContext.getResources().getString(R.string.dialog_base_loading) + ".");
                } else if (num == 2) {
                    mLoading.setText(mContext.getResources().getString(R.string.dialog_base_loading) + "..");
                } else if (num >= 3) {
                    mLoading.setText(mContext.getResources().getString(R.string.dialog_base_loading) + "...");
                    num = 0;
                }
                num++;
            }
            mHandler.postDelayed(mRunnable,300);
        }

    };

    private Handler mHandler = new Handler();

    public DialogGetDensity(Context context) {
        super(context);
        mContext = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.widget_dialog_get_density);
        isRunning = false;
        mCancel = (Button)findViewById(R.id.get_density_back);
        mSearch = (Button)findViewById(R.id.get_density_confirm);
        mLati = (TextView)findViewById(R.id.get_density_lati);
        mLongi = (TextView)findViewById(R.id.get_density_longi);
        mDensity = (TextView)findViewById(R.id.get_density_density);
        mLoading = (TextView)findViewById(R.id.get_density_loading);
        mCancel.setOnClickListener(this);
        mSearch.setOnClickListener(this);
        init();
    }

    private void init(){
        dataServiceUtil = DataServiceUtil.getInstance(mContext);
        stableCache = StableCache.getInstance(mContext);
        String longiStr = String.valueOf(dataServiceUtil.getLongitudeFromCache());
        String latiStr = String.valueOf(dataServiceUtil.getLatitudeFromCache());
        String density = String.valueOf(dataServiceUtil.getPM25Density());
        mLati.setText(latiStr);
        mLongi.setText(longiStr);
        mDensity.setText(density);
        WifiManager wifi_service = (WifiManager)getContext().getApplicationContext().getSystemService(WIFI_SERVICE);
        WifiInfo wifiInfo = wifi_service.getConnectionInfo();
        currentWifiId = wifiInfo.getSSID();
    }

    private void setStop(){
        isStop = true;
        mLoading.setText("");
    }

    private void setRun(){
        isStop = false;
    }

    private void notifyService(Double value){
        Intent intent = new Intent(Const.Action_Search_Density_ToService);
        intent.putExtra(Const.Intent_PM_Density,value);
        mContext.sendBroadcast(intent);
    }

    private void searchPMRequest(String longitude, String latitude) {
        mSearch.setEnabled(false);
        mSearch.setClickable(false);
        setRun();
        isRunning = true;
        String token = dataServiceUtil.getTokenFromCache();
        String url = HttpUtil.Search_PM_url;
        url = url + "?longitude=" + longitude + "&latitude=" + latitude + "&access_token=" + token;
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.e("Dialog_search",response.toString());
                isRunning = false;
                mSearch.setEnabled(true);
                mSearch.setClickable(true);
                setStop();
                try {
                    int data_source = response.getInt("source");
                    aCache.put(Const.Cache_Data_Source,String.valueOf(data_source));
                    int token_status = response.getInt("token_status");
                    if (token_status != 2) {
                        int status = response.getInt("status");

                        if (status == 1) {
                            Const.IS_USE_805 = false;
                            pmModel = PMModel.parse(response.getJSONObject("data"));
                            Intent intent = new Intent(Const.Action_DB_MAIN_PMDensity);
                            intent.putExtra(Const.Intent_PM_Density, pmModel.getPm25());
                            //set current pm density for calculation
                            PM25Density = Double.valueOf(pmModel.getPm25());
                            int source = pmModel.getSource();

                            mDensity.setText(String.valueOf(PM25Density));
                            dataServiceUtil.cachePMResult(PM25Density, source);
                            dataServiceUtil.cacheSearchPMFailed(0);

                            notifyService(PM25Density);
                            Toast.makeText(mContext.getApplicationContext(), Const.Info_PMDATA_Success, Toast.LENGTH_SHORT).show();

                        } else {
                            String str = response.getString("message");
                            mDensity.setText(str);
                        }
                    }else if (token_status == 2){
                        backgroundService.checkPMDataForUpload();
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
                    mDensity.setText("server error");
                }
                Log.e(TAG, "searchPMRequest resp:" + response.toString());
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                Log.e(TAG,"dialog get density searchPMRequest error "+error.toString());
                Toast.makeText(mContext.getApplicationContext(),Const.Info_Failed_PMDensity,Toast.LENGTH_SHORT).show();
                dataServiceUtil.cacheSearchPMFailed(dataServiceUtil.getSearchFailedCountFromCache()+1);

                if(error != null){
                    if(error.getMessage() != null) {
                        Log.e(TAG, error.getMessage());
                        mDensity.setText(error.getMessage());
                        if (error.getMessage().trim().equals("org.json.JSONException: End of input at character 0 of")) {
                            mDensity.setText(Const.Info_No_PMDensity);

                        }
                    }
                    if(error.networkResponse != null){
                        Log.e(TAG,"dialog get density status code = "+error.networkResponse.statusCode);
                    }
                }
                isRunning = false;
                mSearch.setEnabled(true);
                mSearch.setClickable(true);
                setStop();
            }

        });
        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(
                Const.Default_Timeout,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        VolleyQueue.getInstance(mContext.getApplicationContext()).addToRequestQueue(jsonObjectRequest);
    }

    private void searchPMRequest(String devId) {
        final DateFormat format1 = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        final Date date = new Date();
        mSearch.setEnabled(false);
        mSearch.setClickable(false);
        setRun();
        isRunning = true;
        Const.Device_Number = devId;
        Const.IS_USE_805 = true;
        aCache.put(Const.Device_Id,devId);
        aCache.put(Const.Cache_Data_Source,"3");
        String url = HttpUtil.Search_PM_url_wifi;
        url = url + "?devid=" + devId;
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                isRunning = false;
                mSearch.setEnabled(true);
                mSearch.setClickable(true);
                setStop();
                try {
                    int status = response.getInt("status");

                    if(status == 1) {
                        pmModel = PMModel.parse(response.getJSONObject("data"));
                        Toast.makeText(getApplicationContext(), PM25Density+"", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(Const.Action_DB_MAIN_PMDensity);
                        intent.putExtra(Const.Intent_PM_Density, pmModel.getPm25());
                        //set current pm density for calculation
                        PM25Density = Double.valueOf(pmModel.getPm25());
                        String time = String.valueOf(pmModel.getTimePoint());
                        float diff = (float)(date.getTime() - format1.parse(time).getTime())/(1000 * 60 * 60);
                        if(time != null && diff < 1.0){
                            int source = 0;

                            mDensity.setText(String.valueOf(PM25Density));
                            dataServiceUtil.cachePMResult(PM25Density, source);
                            dataServiceUtil.cacheSearchPMFailed(0);

                            notifyService(PM25Density);
                            Toast.makeText(mContext.getApplicationContext(), Const.Info_PMDATA_Success, Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getApplicationContext(), "ilab服务器数据过期", Toast.LENGTH_SHORT).show();
                            searchPMRequest(String.valueOf(dataServiceUtil.getLongitudeFromCache()),
                                    String.valueOf(dataServiceUtil.getLatitudeFromCache()));
                            Log.e("DialogGetDensity","ilab服务器数据过期");
                        }

                    }else {
                        Toast.makeText(getApplicationContext(), "设备号有误", Toast.LENGTH_SHORT).show();
                        String str = response.getString("message");
                        mDensity.setText(str);
                        searchPMRequest(String.valueOf(dataServiceUtil.getLongitudeFromCache()),
                                String.valueOf(dataServiceUtil.getLatitudeFromCache()));
                        Log.e("DialogGetDensity","设备号有误");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    mDensity.setText("server error");
                    searchPMRequest(String.valueOf(dataServiceUtil.getLongitudeFromCache()),
                            String.valueOf(dataServiceUtil.getLatitudeFromCache()));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                Log.e(TAG, "searchPMRequest resp:" + response.toString());
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(), "ilab服务器请求出错", Toast.LENGTH_SHORT).show();
                Log.e(TAG,"dialog get density searchPMRequest error "+error.toString());
                Toast.makeText(mContext.getApplicationContext(),Const.Info_Failed_PMDensity,Toast.LENGTH_SHORT).show();
                dataServiceUtil.cacheSearchPMFailed(dataServiceUtil.getSearchFailedCountFromCache()+1);

                if(error != null){
                    if(error.getMessage() != null) {
                        Log.e(TAG, error.getMessage());
                        mDensity.setText(error.getMessage());
                        if (error.getMessage().trim().equals("org.json.JSONException: End of input at character 0 of")) {
                            mDensity.setText(Const.Info_No_PMDensity);
                        }
                    }
                    if(error.networkResponse != null){
                        Log.e(TAG,"dialog get density status code = "+error.networkResponse.statusCode);
                    }
                }
                isRunning = false;
                mSearch.setEnabled(true);
                mSearch.setClickable(true);
                setStop();
                searchPMRequest(String.valueOf(dataServiceUtil.getLongitudeFromCache()),
                        String.valueOf(dataServiceUtil.getLatitudeFromCache()));
            }

        });
        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(
                Const.Default_Timeout,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        VolleyQueue.getInstance(mContext.getApplicationContext()).addToRequestQueue(jsonObjectRequest);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.get_density_back:
                DialogGetDensity.this.dismiss();
                break;
            case R.id.get_density_confirm:
                mRunnable.run();
                if(!isRunning){
                    if(stableCache.getAsString(Const.Cache_User_Wifi) == null){
                        searchPMRequest(String.valueOf(dataServiceUtil.getLongitudeFromCache()),
                                String.valueOf(dataServiceUtil.getLatitudeFromCache()));
                    } else {
                        if(stableCache.getAsString(Const.Cache_User_Wifi).equals(currentWifiId.replaceAll("\"",""))){
                            searchPMRequest(stableCache.getAsString(Const.Cache_User_Device));
                        } else {
                            searchPMRequest(String.valueOf(dataServiceUtil.getLongitudeFromCache()),
                                    String.valueOf(dataServiceUtil.getLatitudeFromCache()));
                        }
                    }
                }
//                if(!isRunning)
//                    searchPMRequest(mLongi.getText().toString(),mLati.getText().toString());
                break;
        }
    }
}
