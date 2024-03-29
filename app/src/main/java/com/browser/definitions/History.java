/*
 * Copyright © 2016-2017 Soren Stoutner <soren@stoutner.com>.
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

package com.browser.definitions;

import android.graphics.Bitmap;

// Create a `History` object.
public class History {
    // Create the `History` package-local variables.
    public final Bitmap entryFavoriteIcon;
    public final String entryUrl;

    public History(Bitmap entryFavoriteIcon, String entryUrl){
        // Populate the package-local variables.
        this.entryFavoriteIcon = entryFavoriteIcon;
        this.entryUrl = entryUrl;
    }
}