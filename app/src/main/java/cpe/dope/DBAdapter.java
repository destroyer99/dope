package cpe.dope;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DBAdapter {

    static final String TAG = "DBAdapter";
    static final String KEY_ROWID = "_id";
    static final String KEY_SESSIONID = "sessionID";
    static final String KEY_TIME_DATE = "timeDate";
    static final String KEY_LOCATION = "location";
    static final String KEY_TEMPERATURE_C = "temperature_C";
    static final String KEY_DISTANCE_M = "distance_M";
    static final String KEY_SHOT_CALL_X = "shot_call_x";
    static final String KEY_SHOT_CALL_Y = "shot_call_y";
    static final String KEY_SHOT_HIT_X = "shot_hit_x";
    static final String KEY_SHOT_HIT_Y = "shot_hit_y";
    static final String KEY_WIND_DEGREE = "wind_degree";
    static final String KEY_WIND_SPEED = "wind_speed";

    static final String DATABASE_NAME = "DOPE";
    static final String TABLE_SHOTS = "shots";
    static final int DATABASE_VERSION = 1;

    static final String TABLE_SHOTS_CREATE = "CREATE TABLE shots ( " +
            "_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "sessionID INTEGER NOT NULL, " +
            "timeDate TEXT NOT NULL, " +
            "location TEXT NOT NULL, " +
            "temperature_C REAL DEFAULT 0, " +
            "distance_M INTEGER DEFAULT 0, " +
            "shot_call_x REAL DEFAULT 0, " +
            "shot_call_y REAL DEFAULT 0, " +
            "shot_hit_x REAL DEFAULT 0, " +
            "shot_hit_y REAL DEFAULT 0, " +
            "wind_degree INTEGER DEFAULT 0," +
            "wind_speed INTEGER DEFAULT 0" +
            ");";

    final Context context;

    DatabaseHelper DBHelper;
    SQLiteDatabase db;

    public DBAdapter(Context ctx) {
        this.context = ctx;
        DBHelper = new DatabaseHelper(context);
    }

    private static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            try {
//                db.execSQL(TABLE_SESSIONS_CREATE);
                db.execSQL(TABLE_SHOTS_CREATE);
            } catch (SQLException e) { e.printStackTrace(); }
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion + ", which will destroy old data");
            db.execSQL("DROP TABLE IF EXISTS sessions");
            db.execSQL("DROP TABLE IF EXISTS shots");
            onCreate(db);
        }
    }

    public DBAdapter open () throws SQLException {
        db = DBHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        DBHelper.close();
    }

    // Input is a SQL statement that will be executed
    public void performQuery(String query) {
        db.execSQL(query);
    }

////////    Create      /////////////////////////////////////////////////////////////////////////////////////////////////

    public boolean submitShotData(Integer sessionID, String timeDate, String location, Float temperature_C, Integer distance_M, Float shotCallX, Float shotCallY, Float shotHitX, Float shotHitY, Integer windDegree, Integer windSpeed) {
        ContentValues values = new ContentValues();
        values.put(KEY_SESSIONID, sessionID);
        values.put(KEY_TIME_DATE, timeDate);
        values.put(KEY_LOCATION, location);
        values.put(KEY_TEMPERATURE_C, temperature_C);
        values.put(KEY_DISTANCE_M, distance_M);
        values.put(KEY_SHOT_CALL_X, shotCallX);
        values.put(KEY_SHOT_CALL_Y, shotCallY);
        values.put(KEY_SHOT_HIT_X, shotHitX);
        values.put(KEY_SHOT_HIT_Y, shotHitY);
        values.put(KEY_WIND_DEGREE, windDegree);
        values.put(KEY_WIND_SPEED, windSpeed);
        return db.insert(TABLE_SHOTS, null, values) > 0;
    }

    public boolean submitShotData(Integer sessionID) {
        ContentValues values = new ContentValues();
        values.put(KEY_SESSIONID, sessionID);
        return db.insert(TABLE_SHOTS, null, values) > 0;
    }



////////    Read        /////////////////////////////////////////////////////////////////////////////////////////////////

    public Cursor getShots(Integer sessionID) {
        return db.query(TABLE_SHOTS, new String[]{KEY_ROWID, KEY_SESSIONID, KEY_TIME_DATE, KEY_LOCATION, KEY_TEMPERATURE_C, KEY_DISTANCE_M, KEY_SHOT_CALL_X, KEY_SHOT_CALL_Y, KEY_SHOT_HIT_X, KEY_SHOT_HIT_Y, KEY_WIND_DEGREE, KEY_WIND_SPEED}, KEY_SESSIONID + "=" + sessionID, null, null, null, null);
    }

    public Cursor getShotData(Integer rowID) {
        return db.query(TABLE_SHOTS, new String[]{KEY_ROWID, KEY_SESSIONID, KEY_TIME_DATE, KEY_LOCATION, KEY_TEMPERATURE_C, KEY_DISTANCE_M, KEY_SHOT_CALL_X, KEY_SHOT_CALL_Y, KEY_SHOT_HIT_X, KEY_SHOT_HIT_Y, KEY_WIND_DEGREE, KEY_WIND_SPEED}, KEY_ROWID + "=" + rowID, null, null, null, null);
    }

    public Cursor getSessions() {
        return db.query(true, TABLE_SHOTS, new String[]{KEY_ROWID, KEY_SESSIONID, KEY_TIME_DATE, KEY_LOCATION}, null, null, KEY_SESSIONID, null, null, null);
    }

    public Integer getNumberOfShotsInSession(Integer sessionID) {
        return db.query(TABLE_SHOTS, new String[]{KEY_SESSIONID, KEY_TIME_DATE, KEY_LOCATION, KEY_TEMPERATURE_C, KEY_DISTANCE_M, KEY_SHOT_CALL_X, KEY_SHOT_CALL_Y, KEY_SHOT_HIT_X, KEY_SHOT_HIT_Y, KEY_WIND_DEGREE, KEY_WIND_SPEED}, KEY_SESSIONID + "=" + sessionID, null, null, null, null).getCount();
    }


////////    Update      /////////////////////////////////////////////////////////////////////////////////////////////////

    public boolean updateShotData(Integer rowID, String timeDate, String location, Float temperature_C, Integer distance_M, Float shotCallX, Float shotCallY, Float shotHitX, Float shotHitY, Integer windDegree, Integer windSpeed) {
        ContentValues values = new ContentValues();
        values.put(KEY_TIME_DATE, timeDate);
        values.put(KEY_LOCATION, location);
        values.put(KEY_TEMPERATURE_C, temperature_C);
        values.put(KEY_DISTANCE_M, distance_M);
        values.put(KEY_SHOT_CALL_X, shotCallX);
        values.put(KEY_SHOT_CALL_Y, shotCallY);
        values.put(KEY_SHOT_HIT_X, shotHitX);
        values.put(KEY_SHOT_HIT_Y, shotHitY);
        values.put(KEY_WIND_DEGREE, windDegree);
        values.put(KEY_WIND_SPEED, windSpeed);
        return db.update(TABLE_SHOTS, values, KEY_ROWID + "=" + rowID, null) > 0;
    }


////////    Delete      /////////////////////////////////////////////////////////////////////////////////////////////////

    public boolean deleteSession(Integer sessionID) {
        return db.delete(TABLE_SHOTS, KEY_SESSIONID + "=" + sessionID, null) > 0;
    }

    public boolean deleteShotData(Integer rowID) {
        return db.delete(TABLE_SHOTS, KEY_ROWID + "=" + rowID, null) > 0;
    }


////////    Other       /////////////////////////////////////////////////////////////////////////////////////////////////



}
