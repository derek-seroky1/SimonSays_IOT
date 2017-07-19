package adc2017hackers.com.simonsays_iot;

import android.app.Activity;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.KeyEvent;

import com.google.android.things.contrib.driver.apa102.Apa102;
import com.google.android.things.contrib.driver.button.Button;
import com.google.android.things.contrib.driver.button.ButtonInputDriver;
import com.google.android.things.contrib.driver.ht16k33.AlphanumericDisplay;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.*;

import java.io.IOException;
import java.util.Arrays;

public class MainActivity extends Activity
{

    DatabaseReference mGameReference;
    FirebaseDatabase  mDatabase;
    DatabaseReference newGameRef, player1;
    private static final String TAG = MainActivity.class.getSimpleName();

    // Default LED brightness
    private static final int LEDSTRIP_BRIGHTNESS = 1;
    private AlphanumericDisplay mDisplay;
    private Apa102              mLedstrip;
    private boolean gameStarted = false;
    private boolean gameLoaded = false;

    private static String yellowButton = "GPIO_33";
    private static String blueButton = "GPIO_39";
    private static String greenButton = "GPIO_10";
    private static String redButton = "GPIO_35";

    private ButtonInputDriver yellowButtonInput;
    private ButtonInputDriver blueButtonInput;
    private ButtonInputDriver greenButtonInput;
    private ButtonInputDriver redButtonInput;

    private String[] colorsArray;
    private int currentColorIndex;
    private int score;
    private LedColors mCurrentColors;

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
            Log.d(TAG, "Error initializing display");
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
            Log.d(TAG, "Error initializing LED");
            throw new RuntimeException("Error initializing LED strip", e);
        }

        Log.i(TAG, "Registering button driver");
        // Initialize and register the InputDriver that will emit SPACE key events
        // on GPIO state changes.
        try
        {
            yellowButtonInput = new ButtonInputDriver(
                    yellowButton,
                    Button.LogicState.PRESSED_WHEN_LOW,
                    KeyEvent.KEYCODE_0);
            yellowButtonInput.register();

            blueButtonInput = new ButtonInputDriver(
                    blueButton,
                    Button.LogicState.PRESSED_WHEN_LOW,
                    KeyEvent.KEYCODE_1);
            blueButtonInput.register();

            greenButtonInput = new ButtonInputDriver(
                    greenButton,
                    Button.LogicState.PRESSED_WHEN_LOW,
                    KeyEvent.KEYCODE_2);
            greenButtonInput.register();

            redButtonInput = new ButtonInputDriver(
                    redButton,
                    Button.LogicState.PRESSED_WHEN_LOW,
                    KeyEvent.KEYCODE_3);
            redButtonInput.register();
        } catch (IOException e)
        {
            e.printStackTrace();
        }

        mDatabase = FirebaseDatabase.getInstance();
        mGameReference = mDatabase.getReference("message");
        newGameRef = mDatabase.getReference("newGame");
        player1 = mDatabase.getReference("player1");

        newGameRef.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                String value = dataSnapshot.getValue(String.class);
                gameLoaded = value.equals("1");
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        });

        mGameReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (gameLoaded)
                {
                    String value = dataSnapshot.getValue(String.class);
                    colorsArray = value.split(",");
                    startGame();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        //TODO: Register for sensor events here
        updateLedDisplay(LedColors.YELLOW);
//        startGame();
    }

    @Override
    protected void onStop() {
        super.onStop();

        //TODO: Unregister for sensor events here
        gameLoaded = false;
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

        if (yellowButtonInput != null) {
            yellowButtonInput.unregister();
            try {
                yellowButtonInput.close();
            } catch (IOException e) {
                Log.e(TAG, "Error closing Button driver", e);
            } finally{
                yellowButtonInput = null;
            }
        }

        if (blueButtonInput != null) {
            blueButtonInput.unregister();
            try {
                blueButtonInput.close();
            } catch (IOException e) {
                Log.e(TAG, "Error closing Button driver", e);
            } finally{
                blueButtonInput = null;
            }
        }

        if (greenButtonInput != null) {
            greenButtonInput.unregister();
            try {
                greenButtonInput.close();
            } catch (IOException e) {
                Log.e(TAG, "Error closing Button driver", e);
            } finally{
                greenButtonInput = null;
            }
        }

        if (redButtonInput != null) {
            redButtonInput.unregister();
            try {
                redButtonInput.close();
            } catch (IOException e) {
                Log.e(TAG, "Error closing Button driver", e);
            } finally{
                redButtonInput = null;
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

        new CountDownTimer(3500, 1000) {

            public void onTick(long millisUntilFinished) {
                updateDisplay((int) millisUntilFinished / 1000);
            }

            public void onFinish() {
                updateDisplay(0);
                gameStarted = true;
                nextColor();
                gameTimer();

            }
        }.start();
    }

    private void nextColor()
    {
        if (currentColorIndex < colorsArray.length)
        {


        int       value = Integer.valueOf( colorsArray[currentColorIndex] );
        switch (value)
        {
            case 1:
                mCurrentColors = LedColors.YELLOW;
                break;
            case 2:
                mCurrentColors = LedColors.BLUE;
                break;
            case 3:
                mCurrentColors = LedColors.GREEN;
                break;
            case 4:
                mCurrentColors = LedColors.RED;
                break;
            default:
                mCurrentColors = LedColors.ALL;
                break;
        }
        updateLedDisplay(mCurrentColors);
        currentColorIndex++;
        }
        else
        {
            gameStarted = false;
            player1.setValue(String.valueOf(score));
            updateLedDisplay(LedColors.ALL);
        }
    }

    private void gameTimer()
    {
        new CountDownTimer(10000, 1000) {

            public void onTick(long millisUntilFinished) {
//                updateDisplay((int) millisUntilFinished / 1000);
            }

            public void onFinish() {
                updateDisplay(0);
//                updateLedDisplay(LedColors.ALL);
                gameStarted = false;
                Log.d(TAG, "Final Score: " + score);
                player1.setValue(String.valueOf(score));
                updateLedDisplay(LedColors.RED);
            }
        }.start();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (gameStarted)
        {

            switch (keyCode)
            {
                case KeyEvent.KEYCODE_0:
                    if (mCurrentColors == LedColors.YELLOW)
                        score++;
                    else
                    {
                        score--;
                    }
                    Log.d(TAG, "Yellow!");
                    break;
                case KeyEvent.KEYCODE_1:
//
//  updateLedDisplay(LedColors.BLUE);
                    if (mCurrentColors == LedColors.BLUE)
                        score++;
                    else
                    {
                        score--;
                    }
                    Log.d(TAG, "Blue!");
                    break;
                case KeyEvent.KEYCODE_2:
//                    updateLedDisplay(LedColors.GREEN);
                    if (mCurrentColors == LedColors.GREEN)
                        score++;
                    else
                    {
                        score--;
                    }
                    Log.d(TAG, "Green!");
                    break;
                case KeyEvent.KEYCODE_3:
//                    updateLedDisplay(LedColors.RED);
                    if (mCurrentColors == LedColors.RED)
                        score++;
                    else
                    {
                        score--;
                    }
                    Log.d(TAG, "Red!");
                    break;
            }
            nextColor();
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_SPACE) {
            // Turn off the LED
//            setLedValue(false);
            return true;
        }

        return super.onKeyUp(keyCode, event);
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
