package adc2017hackers.com.simonsays_iot;

import android.app.Activity;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;

import com.google.android.things.contrib.driver.apa102.Apa102;
import com.google.android.things.contrib.driver.ht16k33.AlphanumericDisplay;

import java.io.IOException;
import java.util.Arrays;

public class MainActivity extends Activity
{

    private static final String TAG = MainActivity.class.getSimpleName();

    // Default LED brightness
    private static final int LEDSTRIP_BRIGHTNESS = 1;
    private AlphanumericDisplay mDisplay;
    private Apa102              mLedstrip;
    private boolean gameStarted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "Weather Station Started on " + Build.DEVICE);

        //Done: Register peripheral drivers here
        // Initialize 7-segment display
        try {
            mDisplay = new AlphanumericDisplay(BoardDefaults.getI2cBus());
            mDisplay.setEnabled(true);
//            mDisplay.display("4321");
            Log.d(TAG, "Initialized I2C Display");
        } catch (IOException e) {
            throw new RuntimeException("Error initializing display", e);
        }

        // Initialize LED strip
        try {
            mLedstrip = new Apa102(BoardDefaults.getSpiBus(), Apa102.Mode.BGR);
            mLedstrip.setBrightness(LEDSTRIP_BRIGHTNESS);
            int[] colors = new int[7];
            Arrays.fill(colors, Color.RED);
            mLedstrip.write(colors);
            // Because of a known APA102 issue, write the initial value twice.
            mLedstrip.write(colors);

            Log.d(TAG, "Initialized SPI LED strip");
        } catch (IOException e) {
            throw new RuntimeException("Error initializing LED strip", e);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        //TODO: Register for sensor events here
        updateLedDisplay(LedColors.YELLOW);
        startGame();
    }

    @Override
    protected void onStop() {
        super.onStop();

        //TODO: Unregister for sensor events here
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        //Done: Close peripheral connections here
        if (mDisplay != null) {
            try {
                mDisplay.clear();
                mDisplay.setEnabled(false);
                mDisplay.close();
            } catch (IOException e) {
                Log.e(TAG, "Error closing display", e);
            } finally {
                mDisplay = null;
            }
        }

        if (mLedstrip != null) {
            try {
                mLedstrip.write(new int[7]);
                mLedstrip.setBrightness(0);
                mLedstrip.close();
            } catch (IOException e) {
                Log.e(TAG, "Error closing LED strip", e);
            } finally {
                mLedstrip = null;
            }
        }
    }

    /**
     * Update the 7-segment display with the latest temperature value.
     *
     * @param value Latest temperature value.
     */
    private void updateDisplay(int value) {
        //TODO: Add code to write a value to the segment display
        if (mDisplay != null) {
            try {
                mDisplay.display(value);
            } catch (IOException e) {
                Log.e(TAG, "Error updating display", e);
            }
        }
    }

    /**
     * Update LED strip based on the latest pressure value.
     *
     * @param color Latest pressure value.
     */
    private void updateLedDisplay(LedColors color) {
        //TODO: Add code to send color data to the LED strip
        if (mLedstrip != null) {
            try {
                int[] colors = RainbowUtil.getledColor(color);
                mLedstrip.write(colors);
            } catch (IOException e) {
                Log.e(TAG, "Error updating ledstrip", e);
            }
        }
    }

    private void startGame()
    {
        gameStarted = true;
        new CountDownTimer(3500, 1000) {

            public void onTick(long millisUntilFinished) {
                updateDisplay((int) millisUntilFinished / 1000);
            }

            public void onFinish() {
                updateDisplay(0);
                gameTimer();
            }
        }.start();
    }

    private void gameTimer()
    {
        new CountDownTimer(3000, 1000) {

            public void onTick(long millisUntilFinished) {
//                updateDisplay((int) millisUntilFinished / 1000);
            }

            public void onFinish() {
                updateDisplay(0);
//                updateLedDisplay(LedColors.ALL);
                gameStarted = false;
            }
        }.start();
    }

//    // Callback when SensorManager delivers new data.
//    private SensorEventListener mSensorEventListener = new SensorEventListener() {
//        @Override
//        public void onSensorChanged(SensorEvent event) {
//            final float value = event.values[0];
//
//            if (event.sensor.getType() == Sensor.TYPE_AMBIENT_TEMPERATURE) {
//                updateDisplay(value);
//            }
//            if (event.sensor.getType() == Sensor.TYPE_PRESSURE) {
//                updateLedDisplay(value);
//            }
//        }
//
//        @Override
//        public void onAccuracyChanged(Sensor sensor, int accuracy) {
//            Log.d(TAG, "accuracy changed: " + accuracy);
//        }
//    };

}
