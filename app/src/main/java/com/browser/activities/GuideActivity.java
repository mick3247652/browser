/*
 * Copyright © 2016-2019 Soren Stoutner <soren@stoutner.com>.
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

package com.browser.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.WindowManager;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;  // The AndroidX toolbar must be used until the minimum API is >= 21.
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;

import com.browser.fragments.GuideTabFragment;
import com.browser.R;

public class GuideActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Get a handle for the shared preferences.
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        // Get the theme and screenshot preferences.
        boolean darkTheme = sharedPreferences.getBoolean("dark_theme", false);
        boolean allowScreenshots = sharedPreferences.getBoolean("allow_screenshots", false);

        // Disable screenshots if not allowed.
        if (!allowScreenshots) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
        }

        // Set the theme.
        if (darkTheme) {
            setTheme(R.style.PrivacyBrowserDark_SecondaryActivity);
        } else {
            setTheme(R.style.PrivacyBrowserLight_SecondaryActivity);
        }

        // Run the default commands.
        super.onCreate(savedInstanceState);

        // Set the content view.
        setContentView(R.layout.guide_coordinatorlayout);

        // The AndroidX toolbar must be used until the minimum API is >= 21.
        Toolbar toolbar = findViewById(R.id.guide_toolbar);
        setSupportActionBar(toolbar);

        // Get a handle for the action bar.
        final ActionBar actionBar = getSupportActionBar();

        // Remove the incorrect lint warning that the action bar might be null.
        assert actionBar != null;

        // Display the home arrow on the action bar.
        actionBar.setDisplayHomeAsUpEnabled(true);

        //  Setup the ViewPager.
        ViewPager aboutViewPager = findViewById(R.id.guide_viewpager);
        assert aboutViewPager != null; // This assert removes the incorrect warning in Android Studio on the following line that aboutViewPager might be null.
        aboutViewPager.setAdapter(new guidePagerAdapter(getSupportFragmentManager()));

        // Setup the TabLayout and connect it to the ViewPager.
        TabLayout aboutTabLayout = findViewById(R.id.guide_tablayout);
        assert aboutTabLayout != null; // This assert removes the incorrect warning in Android Studio on the following line that aboutTabLayout might be null.
        aboutTabLayout.setupWithViewPager(aboutViewPager);
    }

    private class guidePagerAdapter extends FragmentPagerAdapter {
        private guidePagerAdapter(FragmentManager fragmentManager) {
            // Run the default commands.
            super(fragmentManager);
        }

        @Override
        // Get the count of the number of tabs.
        public int getCount() {
            return 10;
        }

        @Override
        // Get the name of each tab.  Tab numbers start at 0.
        public CharSequence getPageTitle(int tab) {
            switch (tab) {
                case 0:
                    return getString(R.string.overview);

                case 1:
                    return getString(R.string.javascript);

                case 2:
                    return getString(R.string.local_storage);

                case 3:
                    return getString(R.string.user_agent);

                case 4:
                    return getString(R.string.requests);

                case 5:
                    return getString(R.string.domain_settings);

                case 6:
                    return getString(R.string.ssl_certificates);

                case 7:
                    return getString(R.string.tor);

                case 8:
                    return getString(R.string.tracking_ids);

                case 9:
                    return getString(R.string.bookmarks);

                default:
                    return "";
            }
        }

        @Override
        // Setup each tab.
        public Fragment getItem(int tab) {
            return GuideTabFragment.createTab(tab);
        }
    }
}
