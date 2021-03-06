/*
 * Copyright (C) 2013 The Android Open Source Project
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

package android.support.v7.app;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.IntDef;
import android.support.v4.graphics.ColorUtils;
import android.support.v7.mediarouter.R;
import android.util.TypedValue;
import android.view.ContextThemeWrapper;
import android.view.View;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

final class MediaRouterThemeHelper {
    private static final float MIN_CONTRAST = 3.0f;

    @IntDef({COLOR_DARK_ON_LIGHT_BACKGROUND, COLOR_WHITE_ON_DARK_BACKGROUND})
    @Retention(RetentionPolicy.SOURCE)
    private @interface ControllerColorType {}

    private static final int COLOR_DARK_ON_LIGHT_BACKGROUND = 0xDE000000; /* Opacity of 87% */
    private static final int COLOR_WHITE_ON_DARK_BACKGROUND = Color.WHITE;

    private MediaRouterThemeHelper() {
    }

    public static Context createThemedContext(Context context) {
        int style;
        if (isLightTheme(context)) {
            if (getControllerColor(context) == COLOR_DARK_ON_LIGHT_BACKGROUND) {
                style = R.style.Theme_MediaRouter_Light;
            } else {
                style = R.style.Theme_MediaRouter_Light_DarkControlPanel;
            }
        } else {
            if (getControllerColor(context) == COLOR_DARK_ON_LIGHT_BACKGROUND) {
                style = R.style.Theme_MediaRouter_LightControlPanel;
            } else {
                style = R.style.Theme_MediaRouter;
            }
        }
        return new ContextThemeWrapper(context, style);
    }

    public static int getThemeResource(Context context, int attr) {
        TypedValue value = new TypedValue();
        return context.getTheme().resolveAttribute(attr, value, true) ? value.resourceId : 0;
    }

    public static float getDisabledAlpha(Context context) {
        TypedValue value = new TypedValue();
        return context.getTheme().resolveAttribute(android.R.attr.disabledAlpha, value, true)
                ? value.getFloat() : 0.5f;
    }

    public static @ControllerColorType int getControllerColor(Context context) {
        int primaryColor = getThemeColor(context, R.attr.colorPrimary);
        if (ColorUtils.calculateContrast(COLOR_WHITE_ON_DARK_BACKGROUND, primaryColor)
                >= MIN_CONTRAST) {
            return COLOR_WHITE_ON_DARK_BACKGROUND;
        }
        return COLOR_DARK_ON_LIGHT_BACKGROUND;
    }

    public static int getButtonTextColor(Context context) {
        int primaryColor = getThemeColor(context, R.attr.colorPrimary);
        int backgroundColor = getThemeColor(context, android.R.attr.colorBackground);

        if (ColorUtils.calculateContrast(primaryColor, backgroundColor) < MIN_CONTRAST) {
            // Default to colorAccent if the contrast ratio is low.
            return getThemeColor(context, R.attr.colorAccent);
        }
        return primaryColor;
    }

    public static void setMediaControlsBackgroundColor(
            Context context, View mainControls, View groupControls, boolean hasGroup) {
        int primaryColor = getThemeColor(context, R.attr.colorPrimary);
        int primaryDarkColor = getThemeColor(context, R.attr.colorPrimaryDark);
        if (hasGroup && ColorUtils.calculateContrast(COLOR_WHITE_ON_DARK_BACKGROUND, primaryColor)
                < MIN_CONTRAST) {
            // Instead of showing dark controls in a possibly dark (i.e. the primary dark), model
            // the white dialog and use the primary color for the group controls.
            primaryDarkColor = primaryColor;
            primaryColor = Color.WHITE;
        }
        mainControls.setBackgroundColor(primaryColor);
        groupControls.setBackgroundColor(primaryDarkColor);
        // Also store the background colors to the view tags. They are used in
        // setVolumeSliderColor() below.
        mainControls.setTag(primaryColor);
        groupControls.setTag(primaryDarkColor);
    }

    public static void setVolumeSliderColor(
            Context context, MediaRouteVolumeSlider volumeSlider, View backgroundView) {
        int controllerColor = getControllerColor(context);
        if (Color.alpha(controllerColor) != 0xFF) {
            // Composite with the background in order not to show the underlying progress bar
            // through the thumb.
            int backgroundColor = (int) backgroundView.getTag();
            controllerColor = ColorUtils.compositeColors(controllerColor, backgroundColor);
        }
        volumeSlider.setColor(controllerColor);
    }

    private static boolean isLightTheme(Context context) {
        TypedValue value = new TypedValue();
        return context.getTheme().resolveAttribute(R.attr.isLightTheme, value, true)
                && value.data != 0;
    }

    private static int getThemeColor(Context context, int attr) {
        TypedValue value = new TypedValue();
        context.getTheme().resolveAttribute(attr, value, true);
        if (value.resourceId != 0) {
            return context.getResources().getColor(value.resourceId);
        }
        return value.data;
    }
}
