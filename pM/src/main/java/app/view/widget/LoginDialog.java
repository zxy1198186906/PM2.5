package app.view.widget;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.pm.R;

import org.json.JSONException;
import org.json.JSONObject;

import app.model.HttpResult;
import app.model.LogInModel;
import app.services.BackgroundService;
import app.utils.ACache;
import app.utils.Const;
import app.utils.HttpUtil;
import app.utils.VolleyQueue;

/**
 *
 */
public class LoginDialog extends Dialog implements OnClickListener {

    public static final String TAG = "LoginDialog";
    Activity mActivity;
    Button mSure;
    Button mBack;
    EditText mUser;
    EditText mPass;
    TextView resetPwd;
    String username;
    String password;
    LoadingDialog mLoadingDialog;
    Bundle mBundle;
    boolean flag;
    ACache aCache;
    Handler parentHandler;
    private InfoDialog infoDialog;

    private BackgroundService backgroundService;

    public LoginDialog(Activity mActivity) {
        super(mActivity);
        this.mActivity = mActivity;
        flag = true;
        aCache = ACache.get(mActivity);
    }

    public LoginDialog(Activity mActivity, Handler parent) {
        super(mActivity);
        this.mActivity = mActivity;
        flag = true;
        aCache = ACache.get(mActivity);
        parentHandler = parent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        setCancelable(false);
        setContentView(R.layout.widget_dialog_login);
        mLoadingDialog = new LoadingDialog(mActivity);
        mSure = (Button) findViewById(R.id.activitytitle_sure);
        mBack = (Button) findViewById(R.id.activitytitle_cancel);
        mUser = (EditText) findViewById(R.id.activitytitle_title);
        mPass = (EditText) findViewById(R.id.login_password);
        resetPwd = (TextView) findViewById(R.id.text_resetPwd);
        mSure.setOnClickListener(this);
        mBack.setOnClickListener(this);
        resetPwd.setOnClickListener(this);
    }

    @Override
    public void onClick(View arg0) {
        switch (arg0.getId()) {
            case R.id.activitytitle_sure:
                username = mUser.getText().toString();
                password = mPass.getText().toString();
                int result = 3;
                if (result == 3) {
                    mLoadingDialog.show();
                    if (flag) {
                        try {
                            login(username, password);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
                if (result == 0)
                    Toast.makeText(mActivity.getApplicationContext(), Const.Info_Login_Empty, Toast.LENGTH_SHORT).show();
                else if (result == 1)
                    Toast.makeText(mActivity.getApplicationContext(), Const.Info_Login_Short, Toast.LENGTH_SHORT).show();
                else if (result == 2)
                    Toast.makeText(mActivity.getApplicationContext(), Const.Info_Login_Space, Toast.LENGTH_SHORT).show();
                break;
            case R.id.activitytitle_cancel:
                this.dismiss();
                break;
            case R.id.text_resetPwd:
                if (mUser.getText().toString().equals("")) {
                    Toast.makeText(mActivity.getApplicationContext(), Const.Info_UserName_Empty, Toast.LENGTH_SHORT).show();
                } else {
                    infoDialog = new InfoDialog(mActivity);
                    infoDialog.setContent(Const.Info_Reset_Confirm);
                    infoDialog.setSureClickListener(new ResetPwdListener(1, infoDialog));
                    infoDialog.setCancelClickListener(new ResetPwdListener(2, infoDialog));
                    infoDialog.show();
                }


//                this.dismiss();
                break;
            default:
                break;
        }
    }

    private class ResetPwdListener implements View.OnClickListener {

        int type; // 1 sure, 2 cancel
        InfoDialog infoDialog;
        public ResetPwdListener(int type, InfoDialog infoDialog) {
            this.type = type;
            this.infoDialog = infoDialog;
        }

        @Override
        public void onClick(View view) {
            if (type == 1) {
                resetPwd();
            } else {
                infoDialog.dismiss();
            }
        }
    }

    private void login(final String name, String password) throws JSONException {
        String url = HttpUtil.Login_url;
        JSONObject object = new JSONObject();
        object.put("name", name);
        object.put("password", password);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, object, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                mLoadingDialog.dismiss();
                HttpResult result = new HttpResult();
                result.setIsSuccess(true);
                result.setResultBody(response.toString());
                LogInModel model = result.toLogInModel();
                if (result.toLogInModel().getStatus().equals("1")) {
                    Toast.makeText(mActivity.getApplicationContext(), Const.Info_Login_Success, Toast.LENGTH_SHORT).show();
                    Log.e(TAG, model.getFirstname() + model.getLastname());
                    Const.CURRENT_ACCESS_TOKEN = model.getAccess_token();
                    Const.CURRENT_USER_NAME = username;
                    Const.CURRENT_USER_NICKNAME = model.getLastname() + model.getFirstname();
                    Const.CURRENT_USER_GENDER = model.getGender();
                    aCache.put(Const.Cache_User_Id, model.getUserid());
                    aCache.put(Const.Cache_Access_Token, Const.CURRENT_ACCESS_TOKEN);
                    aCache.put(Const.Cache_User_Name, Const.CURRENT_USER_NAME);
                    aCache.put(Const.Cache_User_Nickname, Const.CURRENT_USER_NICKNAME);
                    aCache.put(Const.Cache_User_Gender, Const.CURRENT_USER_GENDER);
                    if (parentHandler != null)
                        parentHandler.sendEmptyMessage(Const.Handler_Login_Success);

                    LoginDialog.this.dismiss();
                } else {
                    mLoadingDialog.dismiss();
                    Toast.makeText(mActivity.getApplicationContext(), Const.Info_Login_Failed, Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                mLoadingDialog.dismiss();
                Toast.makeText(mActivity.getApplicationContext(), Const.Info_No_Network, Toast.LENGTH_SHORT).show();
            }
        });
        VolleyQueue.getInstance(mActivity.getApplicationContext()).addToRequestQueue(jsonObjectRequest);
    }

    //忘记密码
    private void resetPwd() {
        String url = HttpUtil.Reset_Pwd_url + mUser.getText().toString();
        JSONObject object = new JSONObject();
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, object, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    String status = response.getString("status");
                    if (status.equals("1")) {
                        Toast.makeText(mActivity.getApplicationContext(), Const.Info_Reset_Success, Toast.LENGTH_SHORT).show();
                        infoDialog.dismiss();
                    } else if (status.equals("-1")) {
                        Toast.makeText(mActivity.getApplicationContext(), Const.Info_Reset_Username_Fail, Toast.LENGTH_SHORT).show();
                    } else if (status.equals("0")) {
                        Toast.makeText(mActivity.getApplicationContext(), Const.Info_Reset_NoUser_Fail, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(mActivity.getApplicationContext(), Const.Info_Reset_Unknown_Fail, Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(mActivity.getApplicationContext(), Const.Info_No_Network, Toast.LENGTH_SHORT).show();
            }
        });
        VolleyQueue.getInstance(mActivity.getApplicationContext()).addToRequestQueue(jsonObjectRequest);
    }
}
