package adc2017hackers.com.simonsays_iot;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.things.contrib.driver.apa102.Apa102;
import com.google.android.things.contrib.driver.ht16k33.AlphanumericDisplay;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;

import adc2017hackers.com.simonsays_iot.lcd.ADCHackaton;
import adc2017hackers.com.simonsays_iot.lcd.ButtonGridView;

import android.view.View.OnClickListener;

import com.google.android.things.contrib.driver.pwmspeaker.Speaker;
import adc2017hackers.com.simonsays_iot.lcd.BoardDefaultsAudio;


public class MainActivity extends Activity
{

    private static final String TAG = MainActivity.class.getSimpleName();

    // Default LED brightness
    private static final int LEDSTRIP_BRIGHTNESS = 1;
    private AlphanumericDisplay mDisplay;
    private Apa102              mLedstrip;
    private boolean gameStarted = false;


    private static final int LEVEL_DIALOG = 1;
    private static final int GAME_DIALOG = 2;
    private static final int ABOUT_DIALOG = 3;
    private static final int HELP_DIALOG = 4;

    private ADCHackaton model;
    private Menu mMenu;
    private AlertDialog levelDialog;
    private AlertDialog gameDialog;
    private AlertDialog aboutDialog;
    private AlertDialog helpDialog;
    private TextView levelDisplay;
    private TextView gameDisplay;

    private Speaker speaker;

    public static final double REST = -1;
    public static final double G4 = 391.995;
    public static final double E4_FLAT = 311.127;

    public static final double[] DRAMATIC_THEME = {
            G4, REST, G4, REST, G4, REST, E4_FLAT, E4_FLAT
    };



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        model = new ADCHackaton(this);

        Log.d(TAG, "onCreate Test");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.simonsays);

        ButtonGridView grid = (ButtonGridView) this.findViewById(R.id.button_grid);
        grid.setSimonCloneModel(model);

        /* Change the default vol control of app to what is SHOULD be. */
//        this.setVolumeControlStream(AudioManager.STREAM_MUSIC);

        try {
            speaker = new Speaker(BoardDefaultsAudio.getPwmPin());
            speaker.stop();
        } catch (IOException e) {
            throw new IllegalArgumentException("Piezo can't be opened, lets end this here.");
        }

        double frequency = DRAMATIC_THEME[1];
        if (frequency == -1) {
            stopPlayingNote();
            return;
        }


        /* After all initialization, we set up our save/restore InstanceState Bundle. */
        if (savedInstanceState == null) {		// Just launched.  Set initial state.
            SharedPreferences settings = getPreferences (0); // Private mode by default.
            model.setLevel(settings.getInt(ADCHackaton.KEY_GAME_LEVEL, 1));	// Game Level
            model.setGame(settings.getInt(ADCHackaton.KEY_THE_GAME, 1)); 	// The Game
            model.setLongest(settings.getString(ADCHackaton.KEY_LONGEST_SEQUENCE, "")); 	// String Rep of Longest
//            levelDisplay.setText(String.valueOf(model.getLevel()));
//            gameDisplay.setText(String.valueOf(model.getGame()));
        } else {
        	/* If I understand the activity cycle, I can put this here and not override
        	 * onRestoreInstanceState */
            model.restoreState(savedInstanceState);
        }

        //LED Part
//
//        Log.d(TAG, "Weather Station Started on " + Build.DEVICE);
//
//        //Done: Register peripheral drivers here
//        // Initialize 7-segment display
//        try {
//            mDisplay = new AlphanumericDisplay(BoardDefaults.getI2cBus());
//            mDisplay.setEnabled(true);
////            mDisplay.display("4321");
//            Log.d(TAG, "Initialized I2C Display");
//        } catch (IOException e) {
//            throw new RuntimeException("Error initializing display", e);
//        }
//
//        // Initialize LED strip
//        try {
//            mLedstrip = new Apa102(BoardDefaults.getSpiBus(), Apa102.Mode.BGR);
//            mLedstrip.setBrightness(LEDSTRIP_BRIGHTNESS);
//            int[] colors = new int[7];
//            Arrays.fill(colors, Color.RED);
//            mLedstrip.write(colors);
//            // Because of a known APA102 issue, write the initial value twice.
//            mLedstrip.write(colors);
//
//            Log.d(TAG, "Initialized SPI LED strip");
//        } catch (IOException e) {
//            throw new RuntimeException("Error initializing LED strip", e);
//        }
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
        try {
            speaker.stop();
            speaker.close();
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to stop the piezo", e);
        }

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

    public void playNote(final double frequency) {
        try {
            speaker.play(frequency);
        } catch (IOException e) {
            throw new IllegalArgumentException("Piezo can't play note.", e);
        }
    }

    public void stopPlayingNote() {
        try {
            speaker.stop();
        } catch (IOException e) {
            throw new IllegalArgumentException("Piezo can't stop.", e);
        }
    }

    private static double toDouble(byte[] bytes) {
        return ByteBuffer.wrap(bytes).getDouble();
    }



}
