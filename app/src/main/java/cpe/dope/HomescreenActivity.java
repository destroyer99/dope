package cpe.dope;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;


public class HomescreenActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homescreen);
    }

    public void onButtonClick(View view) {
        switch (view.getId()) {
            case R.id.btn_newSession:
                startActivity(new Intent(HomescreenActivity.this, NewSessionActivity.class));
                break;
            case R.id.btn_loadSession:
                startActivity(new Intent(HomescreenActivity.this, LoadSessionActivity.class));
                break;
            case R.id.btn_options:
                startActivity(new Intent(HomescreenActivity.this, OptionsActivity.class));
                break;
            default:
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.homescreen, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        return id == R.id.action_settings || super.onOptionsItemSelected(item);
    }
}








