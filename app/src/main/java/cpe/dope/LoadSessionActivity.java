package cpe.dope;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

public class LoadSessionActivity extends Activity {

    String location;

    ListView listView;
    List<Integer> listSessionID;
    List<String> listViewList;

    Cursor cursor;

    DBAdapter db = new DBAdapter(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_load_session);

        listView = (ListView) findViewById(R.id.listView);
        listView.setOnItemClickListener(onItemClickListener);
        listView.setOnItemLongClickListener(onItemLongClickListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        listSessionID = new ArrayList<Integer>();
        listViewList = new ArrayList<String>();
        db.open();
        cursor = db.getSessions();
        if (cursor.moveToFirst()) {
            location = cursor.getString(3);
            do {
                listSessionID.add(cursor.getInt(1));
                listViewList.add(cursor.getString(2) + " - #Shots: " + db.getNumberOfShotsInSession(cursor.getInt(1)));
            } while (cursor.moveToNext());
        }
        db.close();
        ArrayAdapter<String> listViewAdapter = new ArrayAdapter<String>(getBaseContext(), android.R.layout.simple_list_item_1, listViewList);
        listView.setAdapter(listViewAdapter);
    }

    private AdapterView.OnItemClickListener onItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            startActivity(new Intent(LoadSessionActivity.this, LoadShotActivity.class)
                    .putExtra("SESSIONID", listSessionID.get(i)));
        }
    };

    private AdapterView.OnItemLongClickListener onItemLongClickListener = new AdapterView.OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(final AdapterView<?> adapterView, final View view, final int i, long l) {
            final CharSequence[] options = {"Open", "Resume", "Delete"};

            new AlertDialog.Builder(LoadSessionActivity.this)
                    .setTitle("Session Options")
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                            dialog.dismiss();
                        }
                    })
                    .setItems(options, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, final int j) {
                            switch (j) {
                                case 0: // Open
                                    startActivity(new Intent(LoadSessionActivity.this, LoadShotActivity.class)
                                            .putExtra("SESSIONID", listSessionID.get(i)));
                                    break;

                                case 1: // Resume
                                    startActivity(new Intent(LoadSessionActivity.this, ShotActivity.class)
                                            .putExtra("LOCATION", location)
                                            .putExtra("SESSIONID", (long)listSessionID.get(i))
                                            .putExtra("BTN0", "Next Shot")
                                            .putExtra("BTN1", "End Session"));
                                    LoadSessionActivity.this.finish();
                                    break;

                                case 2: // Delete
                                    DBAdapter db = new DBAdapter(LoadSessionActivity.this);
                                    db.open();
                                    db.deleteSession(listSessionID.get(i));
                                    db.close();
                                    LoadSessionActivity.this.onResume();
                                    break;

                                default:
                                    break;
                            }
                            dialogInterface.dismiss();
                        }
                    })
                    .show();
            return false;
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.load_session, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        return id == R.id.action_settings || super.onOptionsItemSelected(item);
    }
}
