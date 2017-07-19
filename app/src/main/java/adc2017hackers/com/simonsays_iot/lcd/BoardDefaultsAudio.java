package adc2017hackers.com.simonsays_iot.lcd;

import android.os.Build;
import com.google.android.things.pio.PeripheralManagerService;

import java.util.List;

public class BoardDefaultsAudio {

    private static final String DEVICE_EDISON_ARDUINO = "edison_arduino";
    private static final String DEVICE_EDISON = "edison";
    private static final String DEVICE_RPI3 = "rpi3";
    private static final String DEVICE_NXP = "imx6ul";
    private static final String DEVICE_IMX7 = "imx7d_pico";
    private static String sBoardVariant = "";

    private static String getBoardVariant() {
        if (!sBoardVariant.isEmpty()) {
            return sBoardVariant;
        }
        sBoardVariant = Build.DEVICE;
        // For the edison check the pin prefix
        // to always return Edison Breakout pin name when applicable.
        if (sBoardVariant.equals(DEVICE_EDISON)) {
            PeripheralManagerService pioService = new PeripheralManagerService();
            List<String> gpioList = pioService.getGpioList();
            if (gpioList.size() != 0) {
                String pin = gpioList.get(0);
                if (pin.startsWith("IO")) {
                    sBoardVariant = DEVICE_EDISON_ARDUINO;
                }
            }
        }
        return sBoardVariant;
    }

    public static String getPwmPin() {
        switch (getBoardVariant()) {
            case DEVICE_EDISON_ARDUINO:
                return "IO3";
            case DEVICE_EDISON:
                return "GP13";
            case DEVICE_RPI3:
                return "PWM1";
            case DEVICE_NXP:
                return "PWM7";
            case DEVICE_IMX7:
                return "I2C1";
            default:
                throw new UnsupportedOperationException("Unknown device: " + Build.DEVICE);
        }
    }

}
