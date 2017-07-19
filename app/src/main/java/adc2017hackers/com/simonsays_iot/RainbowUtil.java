/*
 * Copyright 2017, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package adc2017hackers.com.simonsays_iot;

import android.graphics.Color;

import java.util.Arrays;

/**
 * Helper methods for computing outputs on the Rainbow HAT
 */
public class RainbowUtil {
    /* LED Strip Color Constants*/
    private static int[] sRainbowColors;
    static {
        sRainbowColors = new int[7];
        for (int i = 0; i < sRainbowColors.length; i++) {
            float[] hsv = {i * 360.f / sRainbowColors.length, 1.0f, 1.0f};
            sRainbowColors[i] = Color.HSVToColor(255, hsv);
        }
    }

    /**
     * Return an array of colors for the LED strip based on the given pressure.
     * @param colors Pressure reading to compare.
     * @return Array of colors to set on the LED strip.
     */
    public static int[] getledColor(LedColors colors) {
        int[] colorStrip = new int[sRainbowColors.length];
        switch (colors)
        {
            case RED:
                colorStrip[0] = Color.RED;
                break;
            case GREEN:
                colorStrip[2] = Color.GREEN;
                break;
            case BLUE:
                colorStrip[4] = Color.BLUE;
                break;
            case YELLOW:
                colorStrip[6] = Color.YELLOW;
                break;
            case ALL:
                Arrays.fill(colorStrip, Color.WHITE);
        }

        return colorStrip;
    }
}

