package cpe.dope;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;

public class NewSessionActivity extends Activity implements LocationListener {

    Boolean restartBool = false;

    ProgressDialog progressDialog;

    private LocationManager locationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        super.onCreate(savedInstanceState);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            showProgressBar("Retrieving location via GPS…");
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        } else {
                new AlertDialog.Builder(this)
                        .setTitle("Enable GPS")
                        .setMessage("Do you want to enable the GPS?")
                        .setCancelable(false)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                                startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                                restartBool = true;
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                                startActivity(new Intent(NewSessionActivity.this, ShotActivity.class)
                                        .putExtra("LOCATION", "")
                                        .putExtra("SESSIONID", System.currentTimeMillis()));
                                dialog.cancel();
                                NewSessionActivity.this.finish();
                            }
                        })
                        .show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (restartBool) {
            this.recreate();
            restartBool = false;
        }
    }

    public synchronized void showProgressBar(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (progressDialog == null) {
                        progressDialog = new ProgressDialog(NewSessionActivity.this);
                        progressDialog.setCancelable(false);
                        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                    }
                } catch (Exception e) { e.printStackTrace(); }

                try {
                    assert progressDialog != null;
                    if (!progressDialog.isShowing()) progressDialog.show();
                    progressDialog.setMessage(message);
                } catch (Exception e) { e.printStackTrace(); }
            }
        });
    }

    public void onLocationChanged(Location location) {
        if (location.getAccuracy() <= 25) {
            locationManager.removeUpdates(NewSessionActivity.this);

            startActivity(new Intent(NewSessionActivity.this, ShotActivity.class)
                    .putExtra("LOCATION", decimalToDMS(location.getLatitude(), true) + "  " +
                        decimalToDMS(location.getLongitude(), false))
                    .putExtra("SESSIONID", System.currentTimeMillis()));
            progressDialog.dismiss();
            NewSessionActivity.this.finish();
        }
    }

    public void onStatusChanged(String provider, int status, Bundle extras) {}

    public void onProviderEnabled(String provider) {}

    public void onProviderDisabled(String provider) {}

    public String decimalToDMS(double coord, boolean latitudeNorthSouth) {
        String output, degrees, minutes, seconds, direction;

        double mod = coord % 1;
        int intPart = (int)coord;

        degrees = String.valueOf(Math.abs(intPart));

        if (latitudeNorthSouth) {
            if (intPart > 0) direction = "N";
            else direction = "S";
        } else {
            if (intPart > 0) direction = "E";
            else direction = "W";
        }

        coord = mod * 60;
        mod = coord % 1;
        intPart = (int)coord;

        minutes = String.valueOf(Math.abs(intPart));

        coord = mod * 60;

        seconds = String.format("%.1f", Math.abs(coord));

        output = degrees + String.valueOf((char)0x00B0) + " " + minutes + String.valueOf((char)0x0027) + " " +
                seconds + String.valueOf((char)0x0022) + " " + direction;

        return output;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.new_session, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        return id == R.id.action_settings || super.onOptionsItemSelected(item);
    }
}
