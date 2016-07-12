package cpe.dope;

import android.content.Context;
import android.database.Cursor;
import android.graphics.PointF;

public class Shot {
    private Integer sessionID, distanceM, distanceY, windDegree, windSpeed;
    private Float temperatureC, temperatureF;
    private String timeDate, location;
    private PointF shotCall = new PointF(), shotHit = new PointF();


/////   Getters     ///////////////////////////////////////////////////////////////////////////////

    public int getSessionID() {
        return this.sessionID;
    }

    public String getTimeDate() {
        return this.timeDate;
    }

    public String getLocation() {
        return this.location;
    }

    public float getTemperatureC() {
        return this.temperatureC;
    }

    public float getTemperatureF() {
        return this.temperatureF;
    }

    public int getDistanceM() {
        return this.distanceM;
    }

    public int getDistanceY() {
        return this.distanceY;
    }

    public PointF getShotCall() {
        return this.shotCall;
    }

    public PointF getShotHit() {
        return this.shotHit;
    }

    public int getWindDegree() {
        return this.windDegree;
    }
    public int getWindSpeed() {
        return this.windSpeed;
    }


/////   Setters     ///////////////////////////////////////////////////////////////////////////////

    public void setSessionID(int sessionID) {
        this.sessionID = sessionID;
    }

    public void setTimeDate(String timeDate) {
        this.timeDate = timeDate;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setTemperatureC(Float temperatureC) {
        this.temperatureC = temperatureC;
        this.temperatureF = (float)(temperatureC * 1.8 + 32);
    }

    public void setTemperatureF(Float temperatureF) {
        this.temperatureF = temperatureF;
        this.temperatureC = (float)((temperatureF - 32) / 1.8);
    }

    public void setDistanceM(Integer distanceM) {
        this.distanceM = distanceM;
        this.distanceY = (int) Math.round(distanceM * 1.0936);
    }

    public void setDistanceY(Integer distanceY) {
        this.distanceY = distanceY;
        this.distanceM = (int) Math.round(distanceY / 1.0936);
    }

    public void setShotCall(Float shotCallX, Float shotCallY) {
        this.shotCall.set(shotCallX, shotCallY);
    }

    public void setShotHit(Float shotHitX, Float shotHitY) {
        this.shotHit.set(shotHitX, shotHitY);
    }

    public void setWindDegree(Integer windDegree) {
        this.windDegree = windDegree;
    }
    public void setWindSpeed(Integer windSpeed) {
        this.windSpeed = windSpeed;
    }


/////       Other   ///////////////////////////////////////////////////////////////////////////////

    public void saveToDatabase(Context context) {
        DBAdapter db = new DBAdapter(context);
        db.open();
        db.submitShotData(this.sessionID, this.timeDate, this.location, this.temperatureC,
                this.distanceM, this.shotCall.x, this.shotCall.y, this.shotHit.x, this.shotHit.y,
                this.windDegree, this.windSpeed);
        db.close();
    }

    public void updateShot(Integer shotID, Context context){
        DBAdapter db = new DBAdapter(context);
        db.open();
        db.updateShotData(shotID, this.timeDate, this.location, this.temperatureC, this.distanceM,
                this.shotCall.x, this.shotCall.y, this.shotHit.x, this.shotHit.y,
                this.windDegree, this.windSpeed);
        db.close();
    }

    public int loadShotData(Integer shotID, Context context) {
        DBAdapter db = new DBAdapter(context);
        db.open();
        Cursor cursor = db.getShotData(shotID);
        if (cursor.moveToFirst()) {
            do {
                this.timeDate = cursor.getString(2);
                this.location = cursor.getString(3);
                this.temperatureC = cursor.getFloat(4);
                this.distanceM = cursor.getInt(5);
                this.shotCall.set(cursor.getFloat(6), cursor.getFloat(7));
                this.shotHit.set(cursor.getFloat(8), cursor.getFloat(9));
                this.windDegree = cursor.getInt(10);
                this.windSpeed = cursor.getInt(11);
            } while (cursor.moveToNext());
        }
        int numberOfShots = db.getNumberOfShotsInSession(sessionID);
        db.close();

        return numberOfShots;
    }

    public void deleteShot(Integer shotID, Context context) {
        DBAdapter db = new DBAdapter(context);
        db.open();
        db.deleteShotData(shotID);
        db.close();
    }
}
