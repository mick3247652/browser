/*
 * Copyright © 2017-2019 Soren Stoutner <soren@stoutner.com>.
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

package com.browser.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;  // The AndroidX fragment must be used until minimum API >= 23.  Otherwise `getContext()` does not work.
import androidx.fragment.app.FragmentManager;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import com.browser.R;
import com.browser.activities.DomainsActivity;

public class DomainsListFragment extends Fragment {
    // Instantiate the dismiss snackbar interface handle.
    private DismissSnackbarInterface dismissSnackbarInterface;

    // Define the public dismiss snackbar interface.
    public interface DismissSnackbarInterface {
        void dismissSnackbar();
    }

    public void onAttach(Context context) {
        // Run the default commands.
        super.onAttach(context);

        // Get a handle for the dismiss snackbar interface.
        dismissSnackbarInterface = (DismissSnackbarInterface) context;
    }

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate `domains_list_fragment`.  `false` does not attach it to the root `container`.
        View domainsListFragmentView = inflater.inflate(R.layout.domains_list_fragment, container, false);

        // Initialize `domainsListView`.
        ListView domainsListView = domainsListFragmentView.findViewById(R.id.domains_listview);

        // Remove the incorrect lint error below that `.getSupportFragmentManager()` might be null.
        assert getActivity() != null;

        // Get a handle for the support fragment manager.
        final FragmentManager supportFragmentManager = getActivity().getSupportFragmentManager();

        domainsListView.setOnItemClickListener((AdapterView<?> parent, View view, int position, long id) -> {
            // Dismiss the snackbar if it is visible.
            dismissSnackbarInterface.dismissSnackbar();

            // Save the current domain settings if operating in two-paned mode and a domain is currently selected.
            if (DomainsActivity.twoPanedMode && DomainsActivity.deleteMenuItem.isEnabled()) {
                // Get a handle for the domain settings fragment.
                Fragment domainSettingsFragment = supportFragmentManager.findFragmentById(R.id.domain_settings_fragment_container);

                // Remove the incorrect lint error below that the domain settings fragment might be null.
                assert domainSettingsFragment != null;

                // Get a handle for the domain settings fragment view.
                View domainSettingsFragmentView = domainSettingsFragment.getView();

                // Get a handle for the domains activity.
                DomainsActivity domainsActivity = new DomainsActivity();

                // Save the domain settings.
                domainsActivity.saveDomainSettings(domainSettingsFragmentView, getResources());
            }

            // Store the new `currentDomainDatabaseId`, converting it from `long` to `int` to match the format of the domains database.
            DomainsActivity.currentDomainDatabaseId = (int) id;

            // Add `currentDomainDatabaseId` to `argumentsBundle`.
            Bundle argumentsBundle = new Bundle();
            argumentsBundle.putInt(DomainSettingsFragment.DATABASE_ID, DomainsActivity.currentDomainDatabaseId);

            // Add the arguments bundle to the domain settings fragment.
            DomainSettingsFragment domainSettingsFragment = new DomainSettingsFragment();
            domainSettingsFragment.setArguments(argumentsBundle);

            // Display the domain settings fragment.
            if (DomainsActivity.twoPanedMode) {  // The device in in two-paned mode.
                // enable `deleteMenuItem` if the system is not waiting for a `Snackbar` to be dismissed.
                if (!DomainsActivity.dismissingSnackbar) {
                    // Enable the delete menu item.
                    DomainsActivity.deleteMenuItem.setEnabled(true);

                    // Get a handle for the shared preferences.
                    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());

                    // Get the theme preferences.
                    boolean darkTheme = sharedPreferences.getBoolean("dark_theme", false);

                    // Set the delete icon according to the theme.
                    if (darkTheme) {
                        DomainsActivity.deleteMenuItem.setIcon(R.drawable.delete_dark);
                    } else {
                        DomainsActivity.deleteMenuItem.setIcon(R.drawable.delete_light);
                    }
                }

                // Display the domain settings fragment.
                supportFragmentManager.beginTransaction().replace(R.id.domain_settings_fragment_container, domainSettingsFragment).commit();
            } else { // The device in in single-paned mode
                // Show `deleteMenuItem` if the system is not waiting for a `Snackbar` to be dismissed.
                if (!DomainsActivity.dismissingSnackbar) {
                    DomainsActivity.deleteMenuItem.setVisible(true);
                }

                // Hide the add domain FAB.
                FloatingActionButton addDomainFAB = getActivity().findViewById(R.id.add_domain_fab);
                addDomainFAB.hide();

                // Display the domain settings fragment.
                supportFragmentManager.beginTransaction().replace(R.id.domains_listview_fragment_container, domainSettingsFragment).commit();
            }
        });

        return domainsListFragmentView;
    }
}