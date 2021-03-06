package com.ogiqvo.android_datetime_seeker.example;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.ogiqvo.android_datetime_seeker.CircularSeekBar;
import com.ogiqvo.clock.Clock;

import org.joda.time.DateTime;

import java.util.Timer;
import java.util.TimerTask;


public class MainActivity extends Activity implements Clock.ClockUpdateReceivable,CircularSeekBar.BarHoldListener {
    Clock clock;
    Timer tickTimer;
    TextView timeTextView;
    Handler handler;
    CircularSeekBar secondsSeekBar, minutesSeekBar, hoursSeekBar;
    private int virtualDay = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        handler = new Handler();

        clock = new Clock();
        clock.addTickEventDelegate(this);

        secondsSeekBar = (CircularSeekBar) findViewById(R.id.secondsCircularSeekBar);
        minutesSeekBar = (CircularSeekBar) findViewById(R.id.minutesCircularSeekBar);
        hoursSeekBar = (CircularSeekBar) findViewById(R.id.hoursCircularSeekBar);
        timeTextView = (TextView) findViewById(R.id.timeTextView);

        secondsSeekBar.addBarHoldListener(this);
        secondsSeekBar.setMaxProgress(60);
        secondsSeekBar.setInnerAdjustmentFactor(50);
        minutesSeekBar.addBarHoldListener(this);
        minutesSeekBar.setMaxProgress(60);
        minutesSeekBar.setInnerAdjustmentFactor(50);
        minutesSeekBar.setOuterAdjustmentFactor(50);
        hoursSeekBar.addBarHoldListener(this);
        hoursSeekBar.setMaxProgress(24);
        hoursSeekBar.setOuterAdjustmentFactor(50);

        hoursSeekBar.addSeekBarChangeListener(new CircularSeekBar.SeekChangeListener() {
            @Override
            public void onProgressChange(CircularSeekBar view, int newProgress, CircularSeekBar.OverflowType overflowType) {
                switch (overflowType) {
                    case UNDERFLOWED:
                        virtualDay--;
                        break;
                    case OVERFLOWED:
                        virtualDay++;
                        break;
                }
                setSeekBarTimeClockText();
                setSeekBarReversibility();
            }
        });
        minutesSeekBar.addSeekBarChangeListener(new CircularSeekBar.SeekChangeListener() {
            @Override
            public void onProgressChange(CircularSeekBar view, int newProgress, CircularSeekBar.OverflowType overflowType) {
                switch (overflowType) {
                    case UNDERFLOWED:
                        hoursSeekBar.setProgress(hoursSeekBar.getProgress() - 1, true);
                        break;
                    case OVERFLOWED:
                        hoursSeekBar.setProgress(hoursSeekBar.getProgress() + 1, true);
                        break;
                }
                setSeekBarTimeClockText();
                setSeekBarReversibility();
            }
        });

        secondsSeekBar.addSeekBarChangeListener(new CircularSeekBar.SeekChangeListener() {
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
                setSeekBarReversibility();
            }
        });
    }

    private void setSeekBarTimeClockText() {
        int hours = hoursSeekBar.getProgress();
        int minutes = minutesSeekBar.getProgress();
        int seconds = secondsSeekBar.getProgress();
        updateClockText(hours, minutes, seconds);
    }

    private void setSeekBarReversibility() {
        hoursSeekBar.setReverseMode(virtualDay % 2 == 0);
        minutesSeekBar.setReverseMode(hoursSeekBar.getProgress() % 2 == 0);
        secondsSeekBar.setReverseMode(minutesSeekBar.getProgress() % 2 == 0);
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

        clock.setPrevUtcMilliseconds(prevUtcMilliseconds);
        clock.setUtcMilliseconds(prevUtcMilliseconds);
        createTickTimer();

        clock.start();

        setSeekBarReversibility();
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
        
        setSeekBarReversibility();
    }

    @Override
    public void onPowerUpdate(double v) {

    }

    private void updateClockText(int hours, int minutes, int seconds) {
        timeTextView.setText(String.format("%d:%02d:%02d", hours, minutes, seconds));
    }

    private void createTickTimer() {
        tickTimer = new Timer();
        tickTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                clock.handleClockUpdateEvent();
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
