package app.utils;

import com.example.pm.R;

public class Const {

    public static boolean[] Chart_Alert_Show = new boolean[]{false,false,false,false,false,false,false,false,false,false,false,false,false};

    public static String APP_KEY_UMENG = "566d299367e58e44cb005fe2";

    public static String APP_KEY_BAIDU = "In8U2gwdA6i5Q0lyDHne342u";

    public static String Name_DB_Service = "app.services.DBService";

//    public static String CURRENT_USER_ID = "0";

    public static Long Min_Refresh_Time = Long.valueOf(30 * 60 * 1000);

    public static Long Min_Search_PM_Time = Long.valueOf(60 * 60 * 1000);

    public static Long Min_Upload_Check_Time = Long.valueOf(60 * 60 * 1000);

    public static Long Min_Search_City_Time = Long.valueOf(10 * 60 * 1000);

    public final static int Gender_Male = 1;

    public final static int Gender_Female = 2;

    public static int CURRENT_WIDTH = -1;

    public static String CURRENT_VERSION = "an.2017.6.7";

    public static String CURRENT_ACCESS_TOKEN = null;

    public static String Device_Number = null;

    public static String CURRENT_USER_NAME = "-1";

    public static String CURRENT_USER_NICKNAME = "-1";

    public static String CURRENT_USER_GENDER = "-1";

    public static int CURRENT_OUTDOOR = 0;

    public static boolean CURRENT_NEED_REFRESH = false;

    public static boolean IS_USE_805 = false;

    public static final double longitude_for_test = 116.304521;

    public static final double latitude_for_test = 39.972465;

    public static final int Default_Timeout = 1000 * 30;

    public static final int Default_Timeout_Long = 1000 * 60;

    public static final int Resolution_Small = 541;

    public static final String Not_SAVING_BATTERY = "false";

    public static final String IS_SAVING_BATTERY = "true";

    public static final String Device_Id = "Device_Id";

    /**
     * Cache*
     */
    public static final String Cache_Is_Background = "Cache_Is_Background";
    public static final String Cache_Pause_Time = "Cache_Pause_Time";
    public static final String Cache_DB_Lastime_searchDensity = "Cache_DB_Lastime_searchDensity";
    public static final String Cache_DB_Lastime_Upload = "Cache_DB_Lastime_Upload";
    public static final String Cache_DB_Lastime_Refresh = "Cache_DB_Lastime_Refresh";
    public static final String Cache_User_Id = "Cache_User_Id";
    public static final String Cache_User_Password = "Cache_User_Password";
    public static final String Cache_User_Name = "Cache_User_Name";
    public static final String Cache_User_Nickname = "Cache_User_Nickname";
    public static final String Cache_User_Gender = "Cache_User_Gender"; // 0 male, 1 female, -1 undefined
    public static final String Cache_Access_Token = "Cache_Access_Token";
    public static final String Cache_Data_Source = "Cache_Data_Source";

    public static final String Cache_PM_Density = "Cache_PM_Density";
    public static final String Cache_PM_Source = "Cache_PM_Source";
    public static final String Cache_PM_Success = "Cache_PM_Success";
    public static final String Cache_PM_LastHour = "Cache_PM_LastHour";
    public static final String Cache_PM_LastDay = "Cache_PM_LastDay";
    public static final String Cache_PM_LastWeek = "Cache_PM_LastWeek";

    public static final String Cache_Longitude = "Cache_Longitude";
    public static final String Cache_Latitude = "Cache_Latitude";
    public static final String Cache_Last_Max_Longi = "Cache_Last_Max_Longi";
    public static final String Cache_Last_Max_Lati = "Cache_Last_Max_Lati";
    public static final String Cache_City = "Cache_City";
    public static final String Cache_Indoor_Outdoor = "Cache_Indoor_Outdoor";
    public static final String Cache_Search_PM_Failed_Count = "Cache_Search_PM_Failed_Count";
    public static final String Cache_Surpass_Reset = "Cache_Surpass_Reset";
    public static final String Cache_Step_Num = "Cache_Step_Num";
    public static final String Cache_Hearth_Rate = "Cache_Hearth_Rate";
    public static final String Cache_Lasttime_Search_City = "Cache_Lasttime_Search_City";
    public static final String Cache_Is_To_Search_Density = "Cache_Is_To_Search_Density";
    public static final String Cache_Has_Step_Counter = "Cache_Has_Step_Counter";

    public static final String Cache_Chart_1 = "Cache_Chart_1";
    public static final String Cache_Chart_2 = "Cache_Chart_2";
    public static final String Cache_Chart_3 = "Cache_Chart_3";
    public static final String Cache_Chart_4 = "Cache_Chart_4";
    public static final String Cache_Chart_5 = "Cache_Chart_5";
    public static final String Cache_Chart_6 = "Cache_Chart_6";
    public static final String Cache_Chart_7 = "Cache_Chart_7";
    public static final String Cache_Chart_7_Date = "Cache_Chart_7_Date";
    public static final String Cache_Chart_8 = "Cache_Chart_8";
    public static final String Cache_Chart_8_Time = "Cache_Chart_8_Time";
    public static final String Cache_Chart_10 = "Cache_Chart_10";
    public static final String Cache_Chart_12 = "Cache_Chart_12";
    public static final String Cache_Chart_12_Date = "Cache_Chart_12_Date";
    public static final String Cache_Chart_Alert = "Cache_Chart_Alert";
    public static final String Cache_User_Weight = "Cache_User_Weight";
    public static final String Cache_User_Wifi = "Cache_User_Wifi";
    public static final String Cache_User_Device = "Cache_User_Device";
    public static final String Cache_Is_Saving_Battery = "Cache_Is_Saving_Battery";
    public static final String Cache_GPS_SATE_NUM = "Cache_GPS_SATE_NUM";

    /**
     * For stable cache
     */
    public static final String Cache_Repeating_Time = "Cache_Repeating_Time";

    /**
     * Handler Code
     */
    public static final int Handler_Login_Success = 100001;

    public static final int Handler_PM_Density = 100002;

    public static final int Handler_PM_Data = 100003;

    public static final int Handler_Modify_Pwd_Success = 100004;

    public static final int Handler_City_Name = 100005;

    public static final int Handler_Input_Weight = 100006;

    public static final int Handler_Gender_Updated = 100007;

    public static final int Handler_Add_City = 100008;

    public static final int Handler_Refresh_All = 100009;

    public static final int Handler_Refresh_Chart1 = 100010;

    public static final int Handler_Refresh_Chart2 = 100011;

    public static final int Handler_Refresh_Chart3 = 100012;

    public static final int Handler_Refresh_Text = 100013;

    public static final int Handler_Initial_Success = 100014;

    /**
     * Intent Tag Code*
     */
    public static final String Intent_PM_Density = "Intent_PM_Density";

    public static final String Intent_DB_PM_Day = "Intent_DB_PM_Day";

    public static final String Intent_DB_PM_Hour = "Intent_DB_PM_Hour";

    public static final String Intent_DB_PM_Week = "Intent_DB_PM_Week";

    public static final String Intent_DB_PM_Lati = "Intent_DB_PM_Lati";

    public static final String Intent_DB_PM_Longi = "Intent_DB_PM_Longi";

    public static final String Intent_DB_City_Ref = "Intent_DB_City_Ref";

    public static final String Intent_DB_Run_State = "Intent_DB_Run_State";

    public static final String Intent_chart1_data = "Intent_chart1_data";

    public static final String Intent_chart2_data = "Intent_chart2_data";

    public static final String Intent_chart3_data = "Intent_chart3_data";

    public static final String Intent_chart4_data = "Intent_chart4_data";

    public static final String Intent_chart5_data = "Intent_chart5_data";

    public static final String Intent_chart6_data = "Intent_chart6_data";

    public static final String Intent_chart7_data = "Intent_chart7_data";

    public static final String Intent_chart_7_data_date = "Intent_chart_7_data_date";

    public static final String Intent_chart8_data = "Intent_chart8_data";

    public static final String Intent_chart8_time = "Intent_chart8_time";

    public static final String Intent_chart10_data = "Intent_chart10_data";

    public static final String Intent_chart12_data = "Intent_chart12_data";

    public static final String Intent_chart_12_data_date = "Intent_chart_12_data_date";

    public static final String Intent_User_Weight = "Intent_User_Weight";

    public static final String Intent_Bluetooth_HearthRate = "Intent_Bluetooth_HearthRate";

    public static final String Intent_Low_Battery_State = "Intent_Low_Battery_State";

    /**
     * Service & Activity Code*
     */
    public static final String Action_DB_MAIN_PMResult = "Action_DB_MAIN_PMResult";

    public static final String Action_DB_MAIN_PMDensity = "Action_DB_MAIN_PMDensity";

    public static final String Action_DB_MAIN_Location = "Action_DB_MAIN_Location";

    public static final String Action_DB_Running_State = "Action_DB_Running_State";

    public static final String Action_Chart_Cache = "Action_Chart_Cache";

    public static final String Action_Chart_Result_1 = "Action_Chart_Result_1";

    public static final String Action_Chart_Result_2 = "Action_Chart_Result_2";

    public static final String Action_Chart_Result_3 = "Action_Chart_Result_3";

    public static final String Action_Bluetooth_Hearth = "Action_Bluetooth_Hearth";

    public static final String Action_Search_Density_ToService = "Action_Search_Density_ToService";

    public static final String Action_Get_Location_ToService = "Action_Get_Location_ToService";

    public static final String Action_Refresh_Chart_ToService = "Action_Refresh_Chart_ToService";

    public static final String Action_Low_Battery_ToService = "Action_Low_Battery_ToService";

    public static final int Action_Profile_Register = 200001;

    /**
     * GPS*
     */
//    public static final String APP_MAP_KEY = "In8U2gwdA6i5Q0lyDHne342u";
    public static final String APP_MAP_KEY = "aGvicpWUa3rRx5BPl3jPGcBhhoa6ASKQ";
    public static final String APP_MAP_MCODE = "47:2C:C4:07:3C:37:72:3D:D2:E0:63:8F:95:18:77:75:44:A4:F1:AC;com.android.application";

    /**
     * Time related values*
     */
    public final static int DB_Run_Time_INTERVAL = 1000 * 5; //5s

    public final static long Refresh_Chart_Interval = 1000 * 60 * 1; //1min

    public static final String ERROR_NO_CITY_RESULT = "Fail to locate city";

    public static final String ERROR_REGISTER_WRONG = "Unvalid registration,check network";
    //breath according to state
    //public static double Global_boy_breath = 6.6; // L/min
    //public static double girl_breath = 6.0; // L/min
    //public static double Global_static_breath = Global_boy_breath;
    //public static final double walk_breath = 2.1 * Global_static_breath;
    //public static final double bicycle_breath = 2.1 * Global_static_breath;
    //public static final double run_breath = 6 * Global_static_breath;

    /**
     * Movement*
     */
    public enum MotionStatus {
        NULL, STATIC, WALK, RUN
    }

    /****/
    // TODO: 16/2/15 Change such hard code string to xml string of resource

    public static final String Info_No_Network = "Unaccessible to server,check network settings";

    public static final String Info_No_Initial = "No data in local devices,ensure connectivity of GPS/network, press the CONFIRM button to reset";

    public static final String Info_Turn_Off_Service = "Stop background service";

    public static final String Info_Turn_On_Service = "Start background service";

    public static final String Info_Turn_Off_Upload = "Stop data uploading";

    public static final String Info_Turn_On_Upload = "Start date uploading";

    public static final String Info_Register_Success = "Register successfully";

    public static final String Info_Register_Failed = "Register fails";

    public static final String Info_Register_pwdError = "Inconsistency between two input passwords";

    public static final String Info_Register_InputEmpty = "Input necessary information";

    public static final String Info_Login_Success = "Login successfully";

    public static final String Info_Login_Failed = "Login fails";

    public static final String Info_UserName_Empty = "Input user name";

    public static final String Info_Login_Empty = "Blank in user name or passord";

    public static final String Info_Login_Short = "Too short of user name or password";

    public static final String Info_Login_Space = "SPACE in user name or password";

    public static final String Info_Login_First = "Please login";

    public static final String Info_GPS_Open = "Location service is running";

    public static final String Info_GPS_Turnoff = "please start location service";

    public static final String Info_PMDATA_Success = "Obtain PM2.5 data successfully";

    public static final String Info_PMDATA_Failed = "Obtain PM2.5 data fails";

    public static final String Info_Upload_Success = "Upload PM2.5 data successfully";

    public static final String Info_Upload_Failed = "Upload PM2.5 data fails";

    public static final String Info_Modify_Pwd_Success = "Correct password successfully";

    public static final String Info_Modify_Pwd_Error = "Incorrect user or confirmation fails";

    public static final String Info_Reset_Confirm = "Sending reset Email?";

    public static final String Info_Reset_Success = "Sending reset Email successfully";

    public static final String Info_Reset_Username_Fail = "Lack user name param";

    public static final String Info_Reset_NoUser_Fail = "User name inexists";

    public static final String Info_Reset_Unknown_Fail = "Sending reset Email fails";

    public static final String Info_GPS_Available = "Current GPS：available";

    public static final String Info_GPS_OutOFService = "Current GPS：out of service";

    public static final String Info_GPS_Pause = "Current GPS：suspend service";

    public static final String Info_GPS_No_Cache = "Unable to get last location";

    public static final String Info_DB_Not_Running = "Not running background, please quit and retry";

    public static final String Info_DB_Not_Location = "Calculate with last location and concentration";

    public static final String Info_Chart_Data_Lost = "Dispersed information in current chart, please keep running";

    public static final String Info_DB_Insert_Date_Conflict = "Failed, time inconsistency between database and now";

    public static final String Info_Bluetooth_ptc_Not_Support = "Unavailable for Bluetooth 4.3";

    public static final String Info_Bluetooth_Not_Support = "Unavailable for Bluetooth";

    public static final String Info_Away_Station_Range = "Dear, You are more than 60 km away from the nearest air pollutant monitoring station, the accuracy of your PM2.5 intake is likely to decline";

    public static final String Info_Data_Lost = "Data loss shown in chart,please keep APP running";

    public static final String Info_Input_Weight_Error = "Wrong weight input, please retry.";

    public static final String Info_Input_Weight_Saved = "Weight is saved";

    public static final String Info_Weight_Null = "Current weight is 0, please input weight in the STATE option";

    public static final String Info_No_PMDensity = "Inaccessiblity to get PM density in this location";

    public static final String Info_Input_WIFI_Error = "wifi or device id error,please input again";

    public static final String Info_Input_WIFI_Saved = "wifi & device id saved";

    public static final String Info_Failed_PMDensity = "Fail to get PM2.5 density";

    public static final String Info_Failed_Location = "Fail to get location";

    public static final String Info_Location_Saved = "Location is saved successfully";

    public static final String Info_Refresh_Chart_Success = " Chart update succcessfully";
    //服务端网络定位失败，可以反馈IMEI号和大体定位时间到loc-bugs@baidu.com，会有人追查原因
    public static final int code_file_baidu_exception1 = -21;
    //网络不通导致定位失败，请检查网络是否通畅
    public static final int code_file_baidu_exception2 = -22;

    //无法获取有效定位依据导致定位失败，一般是由于手机的原因，处于飞行模式下一般会造成这种结果，可以试着重启手机
    public static final int code_file_baidu_exception3 = -23;

    public static final int code_file_wifi_info = -24;

    public static final int code_location_queue_full = -25;

    public static final int code_get_location_failed = -26;

    public static final int code_get_lastlocation_gps_notnull = -27;

    public static final int code_get_last_location_network_notnull = -28;

    public static final int code_get_last_location_isnull = -29;


    public static String[] airQuality = {
            "Excellent", "Good", "Mild pollution", "Medium pollution", "Heavy pollution", "Severe pollution"
    };

    public static String[] heathHint = {
            "Suitable for outdoor activities", "Reduce outdoor activities for the susceptible ", "Reduce outdoor activities for the susceptible suggested", "Avoid outdoor activities"
    };

    public static String[] ringState = {
            "Bio3 bracelet unset", "Bio3 bracelet set", "Bio3 bracelet connected"
    };

    public static String[] ringState2 = {
            "Bio3 box disconnected", "Bio3 box connected"
    };

    public static int[] profileImg = {
            R.drawable.shanghai, R.drawable.beijing
    };

    public static String[] airDensity = {
            "0", "50", "100", "150", "200", "300","浓度"
    };

    /**
     * DayMaxOfTheMonth[1] = 31 January : number of days = 31
     */
    public static int[] DayMaxOfTheMonth = {
       0,31,28,31,30,31,30,31,31,30,31,30,31
    };

}