package dtseeker.com.ogiqvo.example;

import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.appaholics.circularseekbar.CircularSeekBar;

import org.joda.time.DateTime;

import java.util.Timer;
import java.util.TimerTask;

import net.schedul.clock.lib.Clock;


public class MainActivity extends ActionBarActivity implements Clock.ClockUpdateReceivable,CircularSeekBar.BarHoldListener {
    static final private String TAG = "MainActivity";

    Clock clock;
    Timer tickTimer;
    TextView timeTextView;
    Handler handler;
    CircularSeekBar secondsSeekBar, minutesSeekBar, hoursSeekBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        handler = new Handler();

        clock = new Clock();
        clock.setTickEventDelegate(this);

        secondsSeekBar = (CircularSeekBar) findViewById(R.id.secondsCircularSeekBar);
        minutesSeekBar = (CircularSeekBar) findViewById(R.id.minutesCircularSeekBar);
        hoursSeekBar = (CircularSeekBar) findViewById(R.id.hoursCircularSeekBar);
        final RadioGroup ampRadioGroup = (RadioGroup) findViewById(R.id.ampRadioGroup);
        timeTextView = (TextView) findViewById(R.id.timeTextView);

        secondsSeekBar.setBarHoldListener(this);
        secondsSeekBar.setMaxProgress(60);
        secondsSeekBar.setInnerAdjustmentFactor(50);
        minutesSeekBar.setBarHoldListener(this);
        minutesSeekBar.setMaxProgress(60);
        minutesSeekBar.setInnerAdjustmentFactor(50);
        minutesSeekBar.setOuterAdjustmentFactor(50);
        hoursSeekBar.setBarHoldListener(this);
        hoursSeekBar.setMaxProgress(24);
        hoursSeekBar.setOuterAdjustmentFactor(50);

        hoursSeekBar.setSeekBarChangeListener(new CircularSeekBar.SeekChangeListener() {
            @Override
            public void onProgressChange(CircularSeekBar view, int newProgress, CircularSeekBar.OverflowType overflowType) {
                setSeekBarTimeClockText();
            }
        });
        minutesSeekBar.setSeekBarChangeListener(new CircularSeekBar.SeekChangeListener() {
            @Override
            public void onProgressChange(CircularSeekBar view, int newProgress, CircularSeekBar.OverflowType overflowType) {
                Log.d(TAG, "minutes " + newProgress + "/" + overflowType);
                switch (overflowType) {
                    case UNDERFLOWED:
                        hoursSeekBar.setProgress(hoursSeekBar.getProgress() - 1, true);
                        break;
                    case OVERFLOWED:
                        hoursSeekBar.setProgress(hoursSeekBar.getProgress() + 1, true);
                        break;
                }
                setSeekBarTimeClockText();
            }
        });

        secondsSeekBar.setSeekBarChangeListener(new CircularSeekBar.SeekChangeListener() {
            @Override
            public void onProgressChange(CircularSeekBar view, int newProgress, CircularSeekBar.OverflowType overflowType) {
                switch (overflowType) {
                    case UNDERFLOWED:
                        minutesSeekBar.setProgress(minutesSeekBar.getProgress() - 1, true);
                        break;
                    case OVERFLOWED:
                        minutesSeekBar.setProgress(minutesSeekBar.getProgress() + 1, true);
                        break;
                }
                setSeekBarTimeClockText();
            }
        });

        ampRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.rewind10Button:
                        clock.setTimePower(-60);
                        break;
                    case R.id.rewind1Button:
                        clock.setTimePower(-1);
                        break;
                    case R.id.pauseButton:
                        clock.setTimePower(0);
                        break;
                    case R.id.play1Button:
                        clock.setTimePower(1);
                        break;
                    case R.id.play10Button:
                        clock.setTimePower(60);
                        break;
                }
                clock.start();
            }
        });
    }

    private void setSeekBarTimeClockText() {
        int hours = hoursSeekBar.getProgress();
        int minutes = minutesSeekBar.getProgress();
        int seconds = secondsSeekBar.getProgress();
        updateClockText(hours, minutes, seconds);
    }

    @Override
    public void onBarHold() {
        destroyTickTimer();
    }

    @Override
    public void onBarReleased() {
        int hours = hoursSeekBar.getProgress();
        int minutes = minutesSeekBar.getProgress();
        int seconds = secondsSeekBar.getProgress();
        long prevUtcMilliseconds = clock.getUtcMilliseconds();
        DateTime dt = new DateTime(prevUtcMilliseconds);
        int prevHours = dt.getHourOfDay();
        int prevMinutes = dt.getMinuteOfHour();
        int prevSeconds = dt.getSecondOfMinute();
        int deltaHours = hours - prevHours;
        int deltaMinutes = minutes - prevMinutes;
        int deltaSeconds = seconds - prevSeconds;
        deltaSeconds += deltaMinutes * 60 + deltaHours * 3600;
        prevUtcMilliseconds += deltaSeconds * 1000;

        Log.d(TAG, "delta seconds " + deltaSeconds);

        clock.setPrevUtcMilliseconds(prevUtcMilliseconds);
        clock.setUtcMilliseconds(prevUtcMilliseconds);
        createTickTimer();

        clock.start();
    }

    @Override
    public void onClockUpdate(long utcMilliseconds) {
        final DateTime dt = new DateTime(utcMilliseconds);
        handler.post(new Runnable() {
            @Override
            public void run() {
                MainActivity.this.updateClockText(dt.getHourOfDay(), dt.getMinuteOfHour(), dt.getSecondOfMinute());
                secondsSeekBar.setProgress(dt.getSecondOfMinute(), false);
                minutesSeekBar.setProgress(dt.getMinuteOfHour(), false);
                hoursSeekBar.setProgress(dt.getHourOfDay(), false);
            }
        });
    }

    private void updateClockText(int hours, int minutes, int seconds) {
        timeTextView.setText(String.format("%d:%02d:%02d", hours, minutes, seconds));
    }

    private void createTickTimer() {
        tickTimer = new Timer();
        tickTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                clock.handleTimeUpdatedEvent();
            }
        }, 0, 16);
    }

    private void destroyTickTimer() {
        if (tickTimer != null) {
            tickTimer.cancel();
            tickTimer = null;
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        clock.start();
        createTickTimer();
    }

    @Override
    public void onPause() {
        super.onPause();

        destroyTickTimer();
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
