package cpe.dope;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.view.Window;
import android.view.WindowManager;

public class OptionsActivity extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        super.onCreate(savedInstanceState);

        PreferenceManager prefMgr = getPreferenceManager();
        prefMgr.setSharedPreferencesName("appPreferences");

        addPreferencesFromResource(R.xml.activity_options);

        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);

        getPreferenceManager().findPreference("imagePath").setSummary("Current Image Path:\n" +
                getPreferenceManager().getSharedPreferences().getString("imagePath", "Bullseye (Default)"));

        getPreferenceManager().findPreference("imagePath").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                final CharSequence[] options = {"Bullseye", "Silhouette", "Custom..."};

                new AlertDialog.Builder(OptionsActivity.this)
                        .setTitle("Target Options")
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                                dialog.dismiss();
                            }
                        })
                        .setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, final int j) {
                                switch (j) {
                                    case 0: // Bullseye
                                        SharedPreferences customSharedPreference = getSharedPreferences("appPreferences", Context.MODE_PRIVATE);
                                        SharedPreferences.Editor editor = customSharedPreference.edit();
                                        editor.putString("imagePath", "Bullseye");
                                        getPreferenceManager().findPreference("imagePath").setSummary("Current Image:\nBullseye");
                                        editor.apply();
                                        break;

                                    case 1: // Silhouette
                                        SharedPreferences customSharedPreference2 = getSharedPreferences("appPreferences", Context.MODE_PRIVATE);
                                        SharedPreferences.Editor editor2 = customSharedPreference2.edit();
                                        editor2.putString("imagePath", "Silhouette");
                                        getPreferenceManager().findPreference("imagePath").setSummary("Current Image:\nSilhouette");
                                        editor2.apply();
                                        break;

                                    case 2: // Custom
                                        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                                        photoPickerIntent.setType("image/*");
                                        startActivityForResult(photoPickerIntent, 1);
                                        break;

                                    default:
                                        break;
                                }
                                dialogInterface.dismiss();
                            }
                        })
                        .show();
                return true;
            }
        });

        getPreferenceManager().findPreference("distanceIncrement").setSummary("Current Value: +/- " +
                getPreferenceManager().getSharedPreferences().getString("distanceIncrement", "10"));

        getPreferenceManager().findPreference("distanceIncrement").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                final CharSequence[] options = {"1", "5", "10", "25"};

                new AlertDialog.Builder(OptionsActivity.this)
                        .setTitle("Distance +/- Value")
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                                dialog.dismiss();
                            }
                        })
                        .setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, final int j) {
                                SharedPreferences customSharedPreference;
                                SharedPreferences.Editor editor;
                                switch (j) {
                                    case 0: // +/- 1
                                        customSharedPreference = getSharedPreferences("appPreferences", Context.MODE_PRIVATE);
                                        editor = customSharedPreference.edit();
                                        editor.putString("distanceIncrement", "1");
                                        getPreferenceManager().findPreference("distanceIncrement").setSummary("Current Value: +/- 1");
                                        editor.apply();
                                        break;

                                    case 1: // +/- 5
                                        customSharedPreference = getSharedPreferences("appPreferences", Context.MODE_PRIVATE);
                                        editor = customSharedPreference.edit();
                                        editor.putString("distanceIncrement", "5");
                                        getPreferenceManager().findPreference("distanceIncrement").setSummary("Current Value: +/- 5");
                                        editor.apply();
                                        break;

                                    case 2: // +/- 10
                                        customSharedPreference = getSharedPreferences("appPreferences", Context.MODE_PRIVATE);
                                        editor = customSharedPreference.edit();
                                        editor.putString("distanceIncrement", "10");
                                        getPreferenceManager().findPreference("distanceIncrement").setSummary("Current Value: +/- 10");
                                        editor.apply();
                                        break;

                                    case 3: // +/- 25
                                        customSharedPreference = getSharedPreferences("appPreferences", Context.MODE_PRIVATE);
                                        editor = customSharedPreference.edit();
                                        editor.putString("distanceIncrement", "25");
                                        getPreferenceManager().findPreference("distanceIncrement").setSummary("Current Value: +/- 25");
                                        editor.apply();
                                        break;

                                    default:
                                        break;
                                }
                                dialogInterface.dismiss();
                            }
                        })
                        .show();
                return true;
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (resultCode == Activity.RESULT_OK) {
                Uri selectedImage = data.getData();
                String RealPath;
                SharedPreferences customSharedPreference = getSharedPreferences("appPreferences", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = customSharedPreference.edit();
                RealPath = getRealPathFromURI(selectedImage);
                editor.putString("imagePath", RealPath);
                getPreferenceManager().findPreference("imagePath").setSummary("Current Image Path:\n" + RealPath);
                editor.apply();
            }
        }
    }

    private String getRealPathFromURI(Uri contentUri) {
        String [] proj={MediaStore.MediaColumns.DATA};
        Cursor cursor = managedQuery( contentUri, proj, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

    @Override
    protected void onDestroy() {
        getPreferenceManager().getSharedPreferences().
                unregisterOnSharedPreferenceChangeListener(this);
        super.onDestroy();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        
    }
}