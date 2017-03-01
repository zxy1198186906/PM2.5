package app.utils;

public class DBConstants {
    public static final String DB_NAME = "Information.db";
    public static final String TABLE_NAME = "state";
    public static final int DB_VERSION = 1;

    public static class DB_MetaData {
        public static final String STATE_ID_COL = "_id";
        public static final String STATE_USER_ID_COL = "userid";
        public static final String STATE_DATABASE_ACCESS_TOKEN_COL = "datebase_access_token";
        public static final String STATE_TIME_POINT_COL = "time_point";
        public static final String STATE_LONGTITUDE_COL = "longtitude";
        public static final String STATE_LATITUDE_COL = "latitude";
        public static final String STATE_OUTDOOR_COL = "outdoor";
        public static final String STATE_STATUS_COL = "status";
        public static final String STATE_STEPS_COL = "steps";
        public static final String STATE_HEART_RATE_COL = "heart_rate";
        public static final String STATE_VENTILATION_RATE_COL = "ventilation_rate";
        public static final String STATE_VENTILATION_VOLUME_COL = "ventilation_volume";
        public static final String STATE_PM25_CONCEN_COL = "pm25_concen";
        public static final String STATE_PM25_INTAKE_COL = "pm25_intake";
        public static final String STATE_PM25_DATASOURCE_COL = "pm25_datasource";
        public static final String STATE_PM25_MONITOR_COL = "pm25_monitor";
        public static final String STATE_APP_VERSION_COL = "app_version";
        public static final String STATE_HAS_UPLOAD = "upload";
        public static final String STATE_CONNECTION = "connection";
        public static final String DEFAULT_ORDER = "_id desc";
//        public static final String STATE_VENTILATION_RATE_COL = "ventilation_rate"; //⤵️ not sure, to be fixed!
//        public static final String STATE_MONITOR_COL = "pm25_monitor";
//        public static final String STATE_PM25_INTAKE_COL = "pm25_intake";
    }

}
