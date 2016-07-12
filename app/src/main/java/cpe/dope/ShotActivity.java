package cpe.dope;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.TabWidget;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.DecimalFormat;

public class ShotActivity extends Activity implements SensorEventListener {

    Boolean newSession = false, editMode = false;

    EditText edtTxtTemp, edtTxtLocation, edtTxtTimeDate, edtTxtDistance, edtTxtWindSpeed;
    ImageView imgView, imgView2, imgView3;

    Bitmap targetBM, windBM, bulletBM, tempBitmap, tempBitmap2;
    Canvas canvas;
    Paint paint;

    SensorManager sensorManager;
    Sensor mTemperature;

    Shot shot = new Shot();
    SharedPreferences appPrefs;

    private Menu menu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shot);
        getActionBar().setDisplayShowTitleEnabled(false);

        TabHost tabHost = (TabHost) findViewById(android.R.id.tabhost);
        tabHost.setup();

        final TabWidget tabWidget = tabHost.getTabWidget();
        final FrameLayout tabContent = tabHost.getTabContentView();

        TextView[] originalTextViews = new TextView[tabWidget.getTabCount()];
        for (int index = 0; index < tabWidget.getTabCount(); index++) {
            originalTextViews[index] = (TextView) tabWidget.getChildTabViewAt(index);
        }
        tabWidget.removeAllViews();

        for (int index = 0; index < tabContent.getChildCount(); index++) {
            tabContent.getChildAt(index).setVisibility(View.GONE);
        }

        for (int index = 0; index < originalTextViews.length; index++) {
            final TextView tabWidgetTextView = originalTextViews[index];
            final View tabContentView = tabContent.getChildAt(index);
            TabHost.TabSpec tabSpec = tabHost.newTabSpec((String) tabWidgetTextView.getTag());
            tabSpec.setContent(new TabHost.TabContentFactory() {
                @Override
                public View createTabContent(String tag) {
                    return tabContentView;
                }
            });

            if (tabWidgetTextView.getBackground() == null) {
                tabSpec.setIndicator(tabWidgetTextView.getText());
            } else {
                tabSpec.setIndicator(tabWidgetTextView.getText(), tabWidgetTextView.getBackground());
            }
            tabHost.addTab(tabSpec);
        }

        for (int index = 0; index < tabWidget.getChildCount(); index++) {
            TextView textView = (TextView) tabWidget.getChildAt(index).findViewById(android.R.id.title);
            textView.setTextColor(getResources().getColor(android.R.color.background_light));
        }

        appPrefs = getSharedPreferences("appPreferences", MODE_PRIVATE);

        edtTxtTemp = (EditText) findViewById(R.id.edtTxt_temperature);
        edtTxtLocation = (EditText) findViewById(R.id.edtTxt_location);
        edtTxtTimeDate = (EditText) findViewById(R.id.edtTxt_timeDate);
        edtTxtDistance = (EditText) findViewById(R.id.edtTxt_distance);
        edtTxtWindSpeed = (EditText) findViewById(R.id.edtTxt_windSpeed);

        edtTxtTimeDate.setFocusable(false);
        edtTxtDistance.setFocusable(false);
        edtTxtLocation.setFocusable(false);

        imgView = (ImageView) findViewById(R.id.imageView);
        imgView2 = (ImageView) findViewById(R.id.imageView2);
        imgView3 = (ImageView) findViewById(R.id.imageView3);

        if (newSession = getIntent().getBooleanExtra("NEW", true)) {
            editMode = true;

            shot.setLocation(getIntent().getStringExtra("LOCATION"));
            shot.setSessionID((int)getIntent().getLongExtra("SESSIONID", 0));
            shot.setDistanceM(100);
            shot.setWindSpeed(10);

            initClock();
            initSensor(this);
            initImgView(imgView);
            initImgView(imgView2);
            initImgView2(imgView3);

            if (!shot.getLocation().equals("")) {
                edtTxtLocation.setText(shot.getLocation());
                edtTxtLocation.setOnClickListener(null);
            }

            edtTxtDistance.setText(String.valueOf(shot.getDistanceM()) + ((appPrefs.getBoolean("distUnitPref", true)) ? ("m") : ("yd")));
            edtTxtWindSpeed.setText(String.valueOf(shot.getWindSpeed()) + "mph");
            edtTxtWindSpeed.clearFocus();
        } else {
            edtTxtTemp.setEnabled(false);
            edtTxtLocation.setEnabled(false);
            edtTxtTimeDate.setEnabled(false);
            edtTxtDistance.setEnabled(false);
            edtTxtWindSpeed.setEnabled(false);

            initImgView(imgView);
            initImgView(imgView2);
            initImgView2(imgView3);

            imgView.setEnabled(false);
            imgView2.setEnabled(false);
            imgView3.setEnabled(false);

            shot.loadShotData(getIntent().getIntExtra("SHOTID", 0), this);
            loadShot();
        }
    }

    public void loadShot() {
        if (appPrefs.getBoolean("tempUnitPref", true)) edtTxtTemp.setText(new DecimalFormat("##0.0").format(shot.getTemperatureC()) + "°C");
        else edtTxtTemp.setText(new DecimalFormat("##0.0").format(shot.getTemperatureC() * 1.8 + 32) + "°F");
        edtTxtLocation.setText(shot.getLocation());
        edtTxtTimeDate.setText(shot.getTimeDate());
        edtTxtDistance.setText(String.valueOf(shot.getDistanceM()) + ((appPrefs.getBoolean("distUnitPref", true)) ? ("m") : ("yd")));
        edtTxtWindSpeed.setText(shot.getWindSpeed() + "mph");
    }

    public void onButtonClick(View view) {
        switch (view.getId()) {

            case R.id.imgbtn:
                if (editMode) {
                    shot.setDistanceM(shot.getDistanceM() - Integer.valueOf(appPrefs.getString("distanceIncrement", "10")));
                    edtTxtDistance.setText(String.valueOf(shot.getDistanceM()) + ((appPrefs.getBoolean("distUnitPref", true)) ? ("m") : ("yd")));
                }
                break;

            case R.id.imgbtn2:
                if (editMode) {
                    shot.setDistanceM(shot.getDistanceM() + Integer.valueOf(appPrefs.getString("distanceIncrement", "10")));
                    edtTxtDistance.setText(String.valueOf(shot.getDistanceM()) + ((appPrefs.getBoolean("distUnitPref", true)) ? ("m") : ("yd")));
                }
                break;

            case R.id.imgbtn3:
                if (editMode) {
                    shot.setWindSpeed(shot.getWindSpeed() - 1);
                    edtTxtWindSpeed.setText(String.valueOf(shot.getWindSpeed()) + "mph");
                }
                break;

            case R.id.imgbtn4:
                if (editMode) {
                    shot.setWindSpeed(shot.getWindSpeed() + 1);
                    edtTxtWindSpeed.setText(String.valueOf(shot.getWindSpeed()) + "mph");
                }
                break;

            case R.id.edtTxt_temperature:
                final EditText editText0 = new EditText(ShotActivity.this);
                editText0.setInputType(0x00002002);
                new AlertDialog.Builder(ShotActivity.this)
                        .setTitle("Temperature")
                        .setMessage("Set temperature in " + ((appPrefs.getBoolean("tempUnitPref", true)) ? ("celsius:") : ("fahrenheit:")))
                        .setView(editText0)
                        .setCancelable(false)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                                shot.setTemperatureC(Float.valueOf(editText0.getText().toString()));
                                edtTxtTemp.setText(String.valueOf(shot.getTemperatureC()) + ((appPrefs.getBoolean("tempUnitPref", true)) ? ("°C") : ("°F")));
                                getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                                dialog.cancel();
                            }
                        })
                        .show()
                        .getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                break;

            case R.id.edtTxt_location:
                final EditText editText1 = new EditText(ShotActivity.this);
                new AlertDialog.Builder(ShotActivity.this)
                        .setTitle("Location")
                        .setMessage("Set location: ")
                        .setView(editText1)
                        .setCancelable(false)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                                shot.setLocation(editText1.getText().toString());
                                edtTxtLocation.setText(shot.getLocation());
                                getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                                dialog.cancel();
                            }
                        })
                        .show()
                        .getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                break;

            case R.id.edtTxt_timeDate:
                final EditText editText3 = new EditText(ShotActivity.this);
                new AlertDialog.Builder(ShotActivity.this)
                        .setTitle("Date/Time")
                        .setMessage("Set date/time: ")
                        .setView(editText3)
                        .setCancelable(false)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                                shot.setTimeDate(editText3.getText().toString());
                                getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                                dialog.cancel();
                            }
                        })
                        .show()
                        .getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                break;

            case R.id.edtTxt_distance:
                final EditText editText2 = new EditText(ShotActivity.this);
                editText2.setInputType(0x0000002);
                new AlertDialog.Builder(ShotActivity.this)
                        .setTitle("Distance")
                        .setMessage("Set distance in " + ((appPrefs.getBoolean("distUnitPref", true)) ? ("meters:") : ("yards:")))
                        .setView(editText2)
                        .setCancelable(false)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                                shot.setDistanceM(Integer.valueOf(editText2.getText().toString()));
                                edtTxtDistance.setText(String.valueOf(shot.getDistanceM()) + ((appPrefs.getBoolean("distUnitPref", true)) ? ("m") : ("yd")));
                                getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                                dialog.cancel();
                            }
                        })
                        .show()
                        .getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                break;

            default:
                break;
        }
    }

    private ImageView.OnTouchListener OnImageTouchedListener = new ImageView.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            switch (view.getId()) {
                case R.id.imageView:
                    shot.setShotCall(motionEvent.getX(), motionEvent.getY());

                    tempBitmap = Bitmap.createBitmap(targetBM.getWidth(), targetBM.getHeight(), Bitmap.Config.ARGB_4444);
                    canvas = new Canvas(tempBitmap);
                    canvas.drawBitmap(targetBM, 0, 0, null);
                    canvas.drawCircle(motionEvent.getX(), motionEvent.getY(), 25, paint);
                    imgView.setImageDrawable(new BitmapDrawable(getResources(), tempBitmap));

                    return false;

                case R.id.imageView2:
                    shot.setShotHit(motionEvent.getX(), motionEvent.getY());

                    tempBitmap = Bitmap.createBitmap(targetBM.getWidth(), targetBM.getHeight(), Bitmap.Config.ARGB_4444);
                    canvas = new Canvas(tempBitmap);
                    canvas.drawBitmap(targetBM, 0, 0, null);
                    canvas.drawCircle(motionEvent.getX(), motionEvent.getY(), 25, paint);
                    imgView2.setImageDrawable(new BitmapDrawable(getResources(), tempBitmap));

                    return false;

                case R.id.imageView3:
                    float windDegree = (float) Math.toDegrees(Math.atan((motionEvent.getY() - windBM.getHeight() / 2) / (motionEvent.getX() - windBM.getWidth() / 2)))
                            + ((motionEvent.getX() >= windBM.getWidth() / 2) ? (90) : (270));

                    switch (motionEvent.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            return true;

                        case MotionEvent.ACTION_MOVE:
                            tempBitmap2 = Bitmap.createBitmap(windBM.getWidth(), windBM.getHeight(), Bitmap.Config.ARGB_4444);
                            canvas = new Canvas(tempBitmap2);
                            canvas.drawBitmap(windBM, 0, 0, null);
                            Matrix matrix = new Matrix();
                            matrix.preTranslate(tempBitmap2.getWidth() / 2 - bulletBM.getWidth() / 2, tempBitmap2.getHeight() / 2 - bulletBM.getHeight() / 2);
                            matrix.preRotate(windDegree, bulletBM.getWidth() / 2, bulletBM.getHeight() / 2);
                            canvas.drawBitmap(bulletBM, matrix, null);
                            imgView3.setImageDrawable(new BitmapDrawable(getResources(), tempBitmap2));
                            return true;

                        case MotionEvent.ACTION_UP:
                            shot.setWindDegree((int) windDegree);
                            return true;

                        default:
                            return false;
                    }

                default:
                    return true;
            }
        }
    };

    public void initClock() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                do {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            edtTxtTimeDate.setText(DateFormat.getDateTimeInstance().format(System.currentTimeMillis()));
                        }
                    });
                    try {
                        Thread.sleep(333);
                    } catch (InterruptedException e) { break; }
                } while (true);
            }
        }).start();
    }

    public synchronized void initSensor(final Context context) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                sensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);

                if (sensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE) != null){
                    mTemperature = sensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);
                    edtTxtTemp.setEnabled(false);
                    edtTxtTemp.setFocusable(false);
                    sensorManager.registerListener((SensorEventListener) context, mTemperature, SensorManager.SENSOR_DELAY_UI);
                }
            }
        }).start();
    }

    public synchronized void initImgView(final ImageView iv) {
        new Thread (new Runnable() {
            @Override
            public void run() {
                if (targetBM != null) {
                    targetBM.recycle();
                    targetBM = null;
                }

                try {
                    targetBM = BitmapFactory.decodeFile(appPrefs.getString("imagePath", ""));
                    targetBM = Bitmap.createScaledBitmap(targetBM, getWindowManager().getDefaultDisplay().getWidth(), (int) (targetBM.getHeight() * (float) ((float) getWindowManager().getDefaultDisplay().getWidth() / (float) targetBM.getWidth())), false);
                } catch (Exception e) {
                    if (appPrefs.getString("imagePath", "").equals("Silhouette")) {
                        targetBM = BitmapFactory.decodeResource(getResources(), R.drawable.silhouette);
                    } else {
                        targetBM = BitmapFactory.decodeResource(getResources(), R.drawable.bullseye);
                    }
                    targetBM = Bitmap.createScaledBitmap(targetBM, getWindowManager().getDefaultDisplay().getWidth(), (int) (targetBM.getHeight() * (float) ((float) getWindowManager().getDefaultDisplay().getWidth() / (float) targetBM.getWidth())), false);
                }

                paint = new Paint();
                paint.setColor(Color.GREEN);
                paint.setStrokeWidth(1);

                runOnUiThread(new Runnable() {
                    public void run() {
                        iv.setMinimumWidth(targetBM.getWidth());
                        iv.setMinimumHeight(targetBM.getHeight());
                        iv.setMaxWidth(targetBM.getWidth());
                        iv.setMaxHeight(targetBM.getHeight());
                    }
                });

                tempBitmap = Bitmap.createBitmap(targetBM.getWidth(), targetBM.getHeight(), Bitmap.Config.ARGB_4444);
                canvas = new Canvas(tempBitmap);
                canvas.drawBitmap(targetBM, 0, 0, null);
                if (!(getIntent().getBooleanExtra("NEW", true))) {
                    if (iv.getId() == R.id.imageView)
                        canvas.drawCircle(shot.getShotCall().x, shot.getShotCall().y, 25, paint);
                    else
                        canvas.drawCircle(shot.getShotHit().x, shot.getShotHit().y, 25, paint);
                }

                runOnUiThread(new Runnable() {
                    public void run() {
                        iv.setImageDrawable(new BitmapDrawable(getResources(), tempBitmap));
                        iv.setOnTouchListener(OnImageTouchedListener);
                    }
                });
            }
        }).start();
    }

    public synchronized void initImgView2(final ImageView iv) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (windBM != null) {
                    windBM.recycle();
                    windBM = null;
                }

                windBM = BitmapFactory.decodeResource(getResources(), R.drawable.windrose2);
                windBM = Bitmap.createScaledBitmap(windBM, getWindowManager().getDefaultDisplay().getWidth(), (int) (windBM.getHeight() * (float) ((float) getWindowManager().getDefaultDisplay().getWidth() / (float) windBM.getWidth())), false);

                bulletBM = BitmapFactory.decodeResource(getResources(), R.drawable.bullet);
                bulletBM = Bitmap.createScaledBitmap(bulletBM, (int) (bulletBM.getWidth() * (float) ((float) (windBM.getHeight()*2/3) / (float) bulletBM.getHeight())), windBM.getHeight()*2/3, false);

                runOnUiThread(new Runnable() {
                    public void run() {
                        iv.setMinimumWidth(windBM.getWidth());
                        iv.setMinimumHeight(windBM.getHeight());
                        iv.setMaxWidth(windBM.getWidth());
                        iv.setMaxHeight(windBM.getHeight());
                    }
                });

                tempBitmap2 = Bitmap.createBitmap(windBM.getWidth(), windBM.getHeight(), Bitmap.Config.ARGB_4444);
                canvas = new Canvas(tempBitmap2);
                canvas.drawBitmap(windBM, 0, 0, null);

                if (!(getIntent().getBooleanExtra("NEW", true))) {
                    Matrix matrix = new Matrix();
                    matrix.preTranslate(tempBitmap2.getWidth() / 2 - bulletBM.getWidth() / 2, tempBitmap2.getHeight() / 2 - bulletBM.getHeight() / 2);
                    matrix.preRotate(shot.getWindDegree(), bulletBM.getWidth() / 2, bulletBM.getHeight() / 2);
                    canvas.drawBitmap(bulletBM, matrix, null);
                }

                runOnUiThread(new Runnable() {
                    public void run() {
                        iv.setImageDrawable(new BitmapDrawable(getResources(), tempBitmap2));
                        iv.setOnTouchListener(OnImageTouchedListener);
                    }
                });
            }
        }).start();
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_AMBIENT_TEMPERATURE) {
            if (appPrefs.getBoolean("tempUnitPref", true)) edtTxtTemp.setText(new DecimalFormat("##0.0").format(event.values[0]) + "°C");
            else edtTxtTemp.setText(new DecimalFormat("##0.0").format(event.values[0] * 1.8 + 32) + "°F");
            shot.setTemperatureC(event.values[0]);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.shot, menu);
        this.menu = menu;
        if (!(getIntent().getBooleanExtra("NEW", true))) {
            menu.getItem(0).setIcon(android.R.drawable.ic_menu_edit);
            menu.getItem(1).setIcon(android.R.drawable.ic_menu_delete);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_one:
                if (getIntent().getBooleanExtra("NEW", true)) {
                    shot.setTimeDate(edtTxtTimeDate.getText().toString());
                    shot.saveToDatabase(this);
                    ShotActivity.this.recreate();
                } else {
                    editMode = !editMode;

                    edtTxtTemp.setEnabled(editMode);
                    edtTxtLocation.setEnabled(editMode);
                    edtTxtTimeDate.setEnabled(editMode);
                    edtTxtDistance.setEnabled(editMode);
                    imgView.setEnabled(editMode);
                    imgView2.setEnabled(editMode);
                    imgView3.setEnabled(editMode);

                    if (!editMode) {
                        if (appPrefs.getBoolean("tempUnitPref", true)) {
                            shot.setTemperatureC(Float.valueOf(edtTxtTemp.getText().toString().substring(0, edtTxtTemp.getText().toString().indexOf("°"))));
                        }
                        else {
                            shot.setTemperatureC((float) (Float.valueOf(edtTxtTemp.getText().toString().substring(0, edtTxtTemp.getText().toString().indexOf("°"))) - 32 / 1.8));
                        }
                        shot.setLocation(edtTxtLocation.getText().toString());
                        shot.setTimeDate(edtTxtTimeDate.getText().toString());
                        shot.updateShot(getIntent().getIntExtra("SHOTID", 0), this);
                        menu.getItem(0).setIcon(android.R.drawable.ic_menu_edit);
                    } else {
                        menu.getItem(0).setIcon(android.R.drawable.ic_menu_save);
                    }
                }
                getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
                return true;

            case R.id.action_two:
                if (getIntent().getBooleanExtra("NEW", true)) {
                    new AlertDialog.Builder(ShotActivity.this)
                            .setTitle("Confirm Exit")
                            .setMessage("End Session?")
                            .setCancelable(false)
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                                    shot.setTimeDate(edtTxtTimeDate.getText().toString());
                                    shot.saveToDatabase(ShotActivity.this);
                                    ShotActivity.this.finish();
                                }
                            })
                            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                                    dialog.cancel();
                                }
                            })
                            .show();
                } else {
                    new AlertDialog.Builder(ShotActivity.this)
                            .setTitle("Confirm Deletion")
                            .setMessage("Delete Shot?")
                            .setCancelable(false)
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                                    shot.deleteShot(getIntent().getIntExtra("SHOTID", 0), getBaseContext());
                                    shot = null;
                                    System.gc();
                                    ShotActivity.this.finish();
                                }
                            })
                            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                                    dialog.cancel();
                                }
                            })
                            .show();
                }
                return true;

            case R.id.action_cancel:
                new AlertDialog.Builder(ShotActivity.this)
                        .setTitle("Confirm Exit")
                        .setMessage("End without save?")
                        .setCancelable(false)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                                ShotActivity.this.finish();
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                                dialog.cancel();
                            }
                        })
                        .show();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        targetBM.recycle();
        targetBM = null;
        windBM.recycle();
        windBM = null;
        System.gc();
    }
}
