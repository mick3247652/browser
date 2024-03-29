/*
 * Copyright © 2016-2018 Soren Stoutner <soren@stoutner.com>.
 *
 * This file is part of Privacy Browser <https://www.stoutner.com/privacy-browser>.
 *
 * Privacy Browser is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Privacy Browser is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Privacy Browser.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.browser.helpers;
import android.content.Context;
import android.view.View;

import androidx.fragment.app.FragmentManager;

@SuppressWarnings("unused")
public class AdHelper {
    public static void initializeAds(View view, Context applicationContext, FragmentManager fragmentManager, String googleAppId, String adUnitId) {
        // Do nothing because this is the standard flavor.
    }

    public static void loadAd(View view, Context applicationContext, String adUnitId) {
        // Do nothing because this is the standard flavor.
    }

    public static void hideAd(View view) {
        // Do nothing because this is the standard flavor.
    }

    public static void pauseAd(View view) {
        // Do nothing because this is the standard flavor.
    }

    public static void resumeAd(View view) {
        // Do nothing because this is the standard flavor.
    }
}