/*
 * Copyright © 2015-2019 Soren Stoutner <soren@stoutner.com>.
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

package com.browser.dialogs;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;

import androidx.annotation.NonNull;
// `ShortcutInfoCompat`, `ShortcutManagerCompat`, and `IconCompat` can be switched to the non-compat versions once the minimum API >= 26.
import androidx.core.content.pm.ShortcutInfoCompat;
import androidx.core.content.pm.ShortcutManagerCompat;
import androidx.core.graphics.drawable.IconCompat;
import androidx.fragment.app.DialogFragment;  // The AndroidX dialog fragment must be used or an error is produced on API <=22.

import com.browser.BuildConfig;
import com.browser.R;

import java.io.ByteArrayOutputStream;

public class CreateHomeScreenShortcutDialog extends DialogFragment {
    // Define the class variables.
    private EditText shortcutNameEditText;
    private EditText urlEditText;
    private RadioButton openWithPrivacyBrowserRadioButton;

    public static CreateHomeScreenShortcutDialog createDialog(String shortcutName, String urlString, Bitmap favoriteIconBitmap) {
        // Create a favorite icon byte array output stream.
        ByteArrayOutputStream favoriteIconByteArrayOutputStream = new ByteArrayOutputStream();

        // Convert the favorite icon to a PNG and place it in the byte array output stream.  `0` is for lossless compression (the only option for a PNG).
        favoriteIconBitmap.compress(Bitmap.CompressFormat.PNG, 0, favoriteIconByteArrayOutputStream);

        // Convert the byte array output stream to a byte array.
        byte[] favoriteIconByteArray = favoriteIconByteArrayOutputStream.toByteArray();

        // Create an arguments bundle.
        Bundle argumentsBundle = new Bundle();

        // Store the variables in the bundle.
        argumentsBundle.putString("shortcut_name", shortcutName);
        argumentsBundle.putString("url_string", urlString);
        argumentsBundle.putByteArray("favorite_icon_byte_array", favoriteIconByteArray);

        // Create a new instance of the dialog.
        CreateHomeScreenShortcutDialog createHomeScreenShortcutDialog = new CreateHomeScreenShortcutDialog();

        // Add the bundle to the dialog.
        createHomeScreenShortcutDialog.setArguments(argumentsBundle);

        // Return the new dialog.
        return createHomeScreenShortcutDialog;
    }

    // `@SuppressLing("InflateParams")` removes the warning about using `null` as the parent view group when inflating the `AlertDialog`.
    @SuppressLint("InflateParams")
    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Get the arguments.
        Bundle arguments = getArguments();

        // Remove the incorrect lint warning below that the arguments might be null.
        assert arguments != null;

        // Get the strings from the arguments.
        String initialShortcutName = arguments.getString("shortcut_name");
        String initialUrlString = arguments.getString("url_string");

        // Get the favorite icon byte array.
        byte[] favoriteIconByteArray = arguments.getByteArray("favorite_icon_byte_array");

        // Remove the incorrect lint warning below that the favorite icon byte array might be null.
        assert favoriteIconByteArray != null;

        // Convert the favorite icon byte array to a bitmap and store it in a class variable.
        Bitmap favoriteIconBitmap = BitmapFactory.decodeByteArray(favoriteIconByteArray, 0, favoriteIconByteArray.length);

        // Get a handle for the shared preferences.
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());

        // Get the theme and screenshot preferences.
        boolean darkTheme = sharedPreferences.getBoolean("dark_theme", false);
        boolean allowScreenshots = sharedPreferences.getBoolean("allow_screenshots", false);

        // Remove the incorrect lint warning below that the layout inflater might be null.
        assert getActivity() != null;

        // Get the activity's layout inflater.
        LayoutInflater layoutInflater = getActivity().getLayoutInflater();

        // Create a drawable version of the favorite icon.
        Drawable favoriteIconDrawable = new BitmapDrawable(getResources(), favoriteIconBitmap);

        // Use a builder to create the alert dialog.
        AlertDialog.Builder dialogBuilder;

        // Set the style according to the theme.
        if (darkTheme) {
            dialogBuilder = new AlertDialog.Builder(getActivity(), R.style.PrivacyBrowserAlertDialogDark);
        } else {
            dialogBuilder = new AlertDialog.Builder(getActivity(), R.style.PrivacyBrowserAlertDialogLight);
        }

        // Set the title and icon.
        dialogBuilder.setTitle(R.string.create_shortcut);
        dialogBuilder.setIcon(favoriteIconDrawable);

        // Set the view.  The parent view is null because it will be assigned by the alert dialog.
        dialogBuilder.setView(layoutInflater.inflate(R.layout.create_home_screen_shortcut_dialog, null));

        // Setup the close button.  Using null closes the dialog without doing anything else.
        dialogBuilder.setNegativeButton(R.string.cancel, null);

        // Set an `onClick` listener on the create button.
        dialogBuilder.setPositiveButton(R.string.create, (DialogInterface dialog, int which) -> {
            // Create the home screen shortcut.
            createHomeScreenShortcut(favoriteIconBitmap);
        });

        // Create an alert dialog from the alert dialog builder.
        final AlertDialog alertDialog = dialogBuilder.create();

        // Remove the warning below that `getWindow()` might be null.
        assert alertDialog.getWindow() != null;

        // Disable screenshots if not allowed.
        if (allowScreenshots) {
            alertDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
        }

        // The alert dialog must be shown before the contents may be edited.
        alertDialog.show();

        // Get handles for the views.
        shortcutNameEditText = alertDialog.findViewById(R.id.shortcut_name_edittext);
        urlEditText = alertDialog.findViewById(R.id.url_edittext);
        openWithPrivacyBrowserRadioButton = alertDialog.findViewById(R.id.open_with_privacy_browser_radiobutton);
        Button createButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);

        // Populate the edit texts.
        shortcutNameEditText.setText(initialShortcutName);
        urlEditText.setText(initialUrlString);

        // Add a text change listener to the shortcut name edit text.
        shortcutNameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Do nothing.
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Do nothing.
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Update the create button.
                updateCreateButton(createButton);
            }
        });

        // Add a text change listener to the URL edit text.
        urlEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Do nothing.
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Do nothing.
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Update the create button.
                updateCreateButton(createButton);
            }
        });

        // Allow the enter key on the keyboard to create the shortcut when editing the name.
        shortcutNameEditText.setOnKeyListener((View view, int keyCode, KeyEvent keyEvent) -> {
            // Check to see if the enter key was pressed.
            if ((keyEvent.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                // Check the status of the create button.
                if (createButton.isEnabled()) {  // The create button is enabled.
                    // Create the home screen shortcut.
                    createHomeScreenShortcut(favoriteIconBitmap);

                    // Manually dismiss the alert dialog.
                    alertDialog.dismiss();

                    // Consume the event.
                    return true;
                } else {  // The create button is disabled.
                    // Do not consume the event.
                    return false;
                }
            } else {  // Some other key was pressed.
                // Do not consume the event.
                return false;
            }
        });

        // Set the enter key on the keyboard to create the shortcut when editing the URL.
        urlEditText.setOnKeyListener((View view, int keyCode, KeyEvent keyEvent) -> {
            // Check to see if the enter key was pressed.
            if ((keyEvent.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                // Check the status of the create button.
                if (createButton.isEnabled()) {  // The create button is enabled.
                    // Create the home screen shortcut.
                    createHomeScreenShortcut(favoriteIconBitmap);

                    // Manually dismiss the alert dialog.
                    alertDialog.dismiss();

                    // Consume the event.
                    return true;
                } else {  // The create button is disabled.
                    // Do not consume the event.
                    return false;
                }
            } else {  // Some other key was pressed.
                // Do not consume the event.
                return false;
            }
        });

        // Return the alert dialog.
        return alertDialog;
    }

    private void updateCreateButton(Button createButton) {
        // Get the contents of the edit texts.
        String shortcutName = shortcutNameEditText.getText().toString();
        String urlString = urlEditText.getText().toString();

        // Enable the create button if both the shortcut name and the URL are not empty.
        createButton.setEnabled(!shortcutName.isEmpty() && !urlString.isEmpty());
    }

    private void createHomeScreenShortcut(Bitmap favoriteIconBitmap) {
        // Get a handle for the context.
        Context context = getContext();

        // Remove the incorrect lint warning below that the context might be null.
        assert context != null;

        // Get the strings from the edit texts.
        String shortcutName = shortcutNameEditText.getText().toString();
        String urlString = urlEditText.getText().toString();

        // Convert the favorite icon bitmap to an icon.  `IconCompat` must be used until the minimum API >= 26.
        IconCompat favoriteIcon = IconCompat.createWithBitmap(favoriteIconBitmap);

        // Create a shortcut intent.
        Intent shortcutIntent = new Intent(Intent.ACTION_VIEW);

        // Check to see if the shortcut should open up Privacy Browser explicitly.
        if (openWithPrivacyBrowserRadioButton.isChecked()) {
            // Set the current application ID as the target package.
            shortcutIntent.setPackage(BuildConfig.APPLICATION_ID);
        }

        // Add the URL to the intent.
        shortcutIntent.setData(Uri.parse(urlString));

        // Create a shortcut info builder.  The shortcut name becomes the shortcut ID.
        ShortcutInfoCompat.Builder shortcutInfoBuilder = new ShortcutInfoCompat.Builder(context, shortcutName);

        // Add the required fields to the shortcut info builder.
        shortcutInfoBuilder.setIcon(favoriteIcon);
        shortcutInfoBuilder.setIntent(shortcutIntent);
        shortcutInfoBuilder.setShortLabel(shortcutName);

        // Add the shortcut to the home screen.  `ShortcutManagerCompat` can be switched to `ShortcutManager` once the minimum API >= 26.
        ShortcutManagerCompat.requestPinShortcut(context, shortcutInfoBuilder.build(), null);
    }
}