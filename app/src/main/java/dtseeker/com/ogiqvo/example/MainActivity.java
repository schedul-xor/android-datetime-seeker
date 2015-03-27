package dtseeker.com.ogiqvo.example;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.appaholics.circularseekbar.CircularSeekBar;


public class MainActivity extends ActionBarActivity {
    static final private String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        CircularSeekBar secondsSeekBar = (CircularSeekBar) findViewById(R.id.secondsCircularSeekBar);
        final CircularSeekBar minutesSeekBar = (CircularSeekBar) findViewById(R.id.minutesCircularSeekBar);
        final CircularSeekBar hoursSeekBar = (CircularSeekBar) findViewById(R.id.hoursCircularSeekBar);
        secondsSeekBar.setMaxProgress(60);
        secondsSeekBar.setInnerAdjustmentFactor(50);
        minutesSeekBar.setMaxProgress(60);
        minutesSeekBar.setInnerAdjustmentFactor(50);
        minutesSeekBar.setOuterAdjustmentFactor(50);
        hoursSeekBar.setMaxProgress(24);
        hoursSeekBar.setOuterAdjustmentFactor(50);

        minutesSeekBar.setSeekBarChangeListener(new CircularSeekBar.OnSeekChangeListener() {
            @Override
            public void onProgressChange(CircularSeekBar view, int newProgress, CircularSeekBar.OverflowType overflowType) {
                Log.d(TAG, "minutes "+newProgress+"/"+overflowType);
                switch (overflowType) {
                    case UNDERFLOWED:
                        hoursSeekBar.setProgress(hoursSeekBar.getProgress() - 1);
                        hoursSeekBar.invalidate();
                        break;
                    case OVERFLOWED:
                        hoursSeekBar.setProgress(hoursSeekBar.getProgress() + 1);
                        hoursSeekBar.invalidate();
                        break;
                }
            }
        });

        secondsSeekBar.setSeekBarChangeListener(new CircularSeekBar.OnSeekChangeListener() {
            @Override
            public void onProgressChange(CircularSeekBar view, int newProgress, CircularSeekBar.OverflowType overflowType) {
                switch (overflowType) {
                    case UNDERFLOWED:
                        minutesSeekBar.setProgress(minutesSeekBar.getProgress() - 1);
                        minutesSeekBar.invalidate();
                        break;
                    case OVERFLOWED:
                        minutesSeekBar.setProgress(minutesSeekBar.getProgress() + 1);
                        minutesSeekBar.invalidate();
                        break;
                }
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
