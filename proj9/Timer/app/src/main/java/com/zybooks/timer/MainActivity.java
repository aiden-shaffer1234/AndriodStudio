package com.zybooks.timer;


import android.os.Handler;
import android.os.Bundle;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;

import java.text.DecimalFormat;

public class MainActivity extends AppCompatActivity {

    private NumberPicker mHoursPicker;
    private NumberPicker mMinutesPicker;
    private NumberPicker mSecondsPicker;
    private Button mStartButton;
    private Button mPauseButton;
    private Button mCancelButton;
    private ProgressBar mProgressBar;
    private TextView mTimeLeftTextView;

    private Handler mHandler;
    private TimerModel mTimerModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initially hide the timer and progress bar
        mTimeLeftTextView = findViewById(R.id.time_left_text_view);
        mTimeLeftTextView.setVisibility(View.INVISIBLE);
        mProgressBar = findViewById(R.id.progress_bar);
        mProgressBar.setVisibility(View.INVISIBLE);

        mStartButton = findViewById(R.id.start_button);
        mPauseButton = findViewById(R.id.pause_button);
        mCancelButton = findViewById(R.id.cancel_button);

        // Add click callbacks
        mStartButton.setOnClickListener(this::startButtonClick);
        mPauseButton.setOnClickListener(this::pauseButtonClick);
        mCancelButton.setOnClickListener(this::cancelButtonClick);

        // Hide pause and cancel buttons until the timer starts
        mPauseButton.setVisibility(View.GONE);
        mCancelButton.setVisibility(View.GONE);

        // Show 2 digits in NumberPickers
        NumberPicker.Formatter numFormat = i -> new DecimalFormat("00").format(i);

        // Set min and max values for all NumberPickers
        mHoursPicker = findViewById(R.id.hours_picker);
        mHoursPicker.setMinValue(0);
        mHoursPicker.setMaxValue(99);
        mHoursPicker.setFormatter(numFormat);

        mMinutesPicker = findViewById(R.id.minutes_picker);
        mMinutesPicker.setMinValue(0);
        mMinutesPicker.setMaxValue(59);
        mMinutesPicker.setFormatter(numFormat);

        mSecondsPicker = findViewById(R.id.seconds_picker);
        mSecondsPicker.setMinValue(0);
        mSecondsPicker.setMaxValue(59);
        mSecondsPicker.setFormatter(numFormat);

        mTimerModel = new TimerModel();

        // Instantiate Handler so Runnables can be posted to UI message queue
        mHandler = new Handler(Looper.getMainLooper());
    }

    private void startButtonClick(View view) {
        // Get values from NumberPickers
        int hours = mHoursPicker.getValue();
        int minutes = mMinutesPicker.getValue();
        int seconds = mSecondsPicker.getValue();

        if (hours + minutes + seconds > 0) {
            // Show progress
            mTimeLeftTextView.setVisibility(View.VISIBLE);
            mProgressBar.setProgress(0);
            mProgressBar.setVisibility(View.VISIBLE);

            // Show only Pause and Cancel buttons
            mStartButton.setVisibility(View.GONE);
            mPauseButton.setVisibility(View.VISIBLE);
            mPauseButton.setText(R.string.pause);
            mCancelButton.setVisibility(View.VISIBLE);

            // Start the model
            mTimerModel.start(hours, minutes, seconds);

            // Start sending Runnables to message queue
            mHandler.post(mUpdateTimerRunnable);
        }
    }

    private void pauseButtonClick(View view) {
        if (mTimerModel.isRunning()) {
            // Pause and change to resume button
            mTimerModel.pause();
            mHandler.removeCallbacks(mUpdateTimerRunnable);
            mPauseButton.setText(R.string.resume);
        }
        else {
            // Resume and change to pause button
            mTimerModel.resume();
            mHandler.post(mUpdateTimerRunnable);
            mPauseButton.setText(R.string.pause);
        }
    }

    private void cancelButtonClick(View view) {
        mTimeLeftTextView.setVisibility(View.INVISIBLE);
        mProgressBar.setVisibility(View.INVISIBLE);
        timerCompleted();
    }

    @Override
    protected void onStop() {
        super.onStop();

        // Start the Worker if the timer is running
        if (mTimerModel.isRunning()) {
            WorkRequest timerWorkRequest = new OneTimeWorkRequest.Builder(TimerWorker.class)
                    .setInputData(new Data.Builder()
                            .putLong(TimerWorker.KEY_MILLISECONDS_REMAINING,
                                    mTimerModel.getRemainingMilliseconds())
                            .build()
                    ).build();

            WorkManager.getInstance(this).enqueue(timerWorkRequest);
        }
    }

    private void timerCompleted() {
        mTimerModel.stop();

        // Remove any remaining Runnables that may reside in UI message queue
        mHandler.removeCallbacks(mUpdateTimerRunnable);

        // Show only the start button
        mStartButton.setVisibility(View.VISIBLE);
        mPauseButton.setVisibility(View.GONE);
        mCancelButton.setVisibility(View.GONE);
    }

    private final Runnable mUpdateTimerRunnable = new Runnable() {
        @Override
        public void run() {

            // Update UI to show remaining time and progress
            mTimeLeftTextView.setText(mTimerModel.toString());
            int progress = mTimerModel.getProgressPercent();
            mProgressBar.setProgress(progress);

            // Only post Runnable if more time remains
            if (progress == 100) {
                timerCompleted();
            }
            else {
                mHandler.postDelayed(this, 200);
            }
        }
    };
}