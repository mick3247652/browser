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

package com.browser.dialogs;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.MatrixCursor;
import android.database.MergeCursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ResourceCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;  // The AndroidX dialog fragment must be used or an error is produced on API <=22.

import com.browser.R;
import com.browser.helpers.BookmarksDatabaseHelper;

import java.io.ByteArrayOutputStream;

public class EditBookmarkFolderDatabaseViewDialog extends DialogFragment {
    // Define the home folder database ID constant.
    public static final int HOME_FOLDER_DATABASE_ID = -1;

    // Define the edit bookmark folder database view listener.
    private EditBookmarkFolderDatabaseViewListener editBookmarkFolderDatabaseViewListener;


    // The public interface is used to send information back to the parent activity.
    public interface EditBookmarkFolderDatabaseViewListener {
        void onSaveBookmarkFolder(DialogFragment dialogFragment, int selectedFolderDatabaseId, Bitmap favoriteIconBitmap);
    }

    public void onAttach(Context context) {
        // Run the default commands.
        super.onAttach(context);

        // Get a handle for `EditBookmarkDatabaseViewListener` from the launching context.
        editBookmarkFolderDatabaseViewListener = (EditBookmarkFolderDatabaseViewListener) context;
    }


    public static EditBookmarkFolderDatabaseViewDialog folderDatabaseId(int databaseId, Bitmap favoriteIconBitmap) {
        // Create a favorite icon byte array output stream.
        ByteArrayOutputStream favoriteIconByteArrayOutputStream = new ByteArrayOutputStream();

        // Convert the favorite icon to a PNG and place it in the byte array output stream.  `0` is for lossless compression (the only option for a PNG).
        favoriteIconBitmap.compress(Bitmap.CompressFormat.PNG, 0, favoriteIconByteArrayOutputStream);

        // Convert the byte array output stream to a byte array.
        byte[] favoriteIconByteArray = favoriteIconByteArrayOutputStream.toByteArray();

        // Create an arguments bundle.
        Bundle argumentsBundle = new Bundle();

        // Store the variables in the bundle.
        argumentsBundle.putInt("database_id", databaseId);
        argumentsBundle.putByteArray("favorite_icon_byte_array", favoriteIconByteArray);

        // Create a new instance of the dialog.
        EditBookmarkFolderDatabaseViewDialog editBookmarkFolderDatabaseViewDialog = new EditBookmarkFolderDatabaseViewDialog();

        // Add the arguments bundle to the dialog.
        editBookmarkFolderDatabaseViewDialog.setArguments(argumentsBundle);

        // Return the new dialog.
        return editBookmarkFolderDatabaseViewDialog;
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

        // Get the bookmark database ID from the bundle.
        int folderDatabaseId = getArguments().getInt("database_id");

        // Get the favorite icon byte array.
        byte[] favoriteIconByteArray = arguments.getByteArray("favorite_icon_byte_array");

        // Remove the incorrect lint warning below that the favorite icon byte array might be null.
        assert favoriteIconByteArray != null;

        // Convert the favorite icon byte array to a bitmap.
        Bitmap favoriteIconBitmap = BitmapFactory.decodeByteArray(favoriteIconByteArray, 0, favoriteIconByteArray.length);

        // Initialize the database helper.   The `0` specifies a database version, but that is ignored and set instead using a constant in `BookmarksDatabaseHelper`.
        BookmarksDatabaseHelper bookmarksDatabaseHelper = new BookmarksDatabaseHelper(getContext(), null, null, 0);

        // Get a `Cursor` with the selected bookmark and move it to the first position.
        Cursor folderCursor = bookmarksDatabaseHelper.getBookmark(folderDatabaseId);
        folderCursor.moveToFirst();

        // Use an alert dialog builder to create the alert dialog.
        AlertDialog.Builder dialogBuilder;

        // Get a handle for the shared preferences.
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());

        // Get the screenshot and theme preferences.
        boolean darkTheme = sharedPreferences.getBoolean("dark_theme", false);
        boolean allowScreenshots = sharedPreferences.getBoolean("allow_screenshots", false);

        // Set the style according to the theme.
        if (darkTheme) {
            dialogBuilder = new AlertDialog.Builder(getActivity(), R.style.PrivacyBrowserAlertDialogDark);
        } else {
            dialogBuilder = new AlertDialog.Builder(getActivity(), R.style.PrivacyBrowserAlertDialogLight);
        }

        // Set the title.
        dialogBuilder.setTitle(R.string.edit_folder);

        // Remove the incorrect lint warning that `getActivity()` might be null.
        assert getActivity() != null;

        // Set the view.  The parent view is `null` because it will be assigned by `AlertDialog`.
        dialogBuilder.setView(getActivity().getLayoutInflater().inflate(R.layout.edit_bookmark_folder_databaseview_dialog, null));

        // Set the listener for the negative button.
        dialogBuilder.setNegativeButton(R.string.cancel, (DialogInterface dialog, int which) -> {
            // Do nothing.  The `AlertDialog` will close automatically.
        });

        // Set the listener fo the positive button.
        dialogBuilder.setPositiveButton(R.string.save, (DialogInterface dialog, int which) -> {
            // Return the `DialogFragment` to the parent activity on save.
            editBookmarkFolderDatabaseViewListener.onSaveBookmarkFolder(this, folderDatabaseId, favoriteIconBitmap);
        });

        // Create an alert dialog from the alert dialog builder.
        final AlertDialog alertDialog = dialogBuilder.create();

        // Remove the warning below that `getWindow()` might be null.
        assert alertDialog.getWindow() != null;

        // Disable screenshots if not allowed.
        if (!allowScreenshots) {
            alertDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
        }

        // The alert dialog must be shown before items in the layout can be modified.
        alertDialog.show();

        // Get handles for the layout items.
        TextView databaseIdTextView = alertDialog.findViewById(R.id.edit_folder_database_id_textview);
        RadioGroup iconRadioGroup = alertDialog.findViewById(R.id.edit_folder_icon_radiogroup);
        ImageView currentIconImageView = alertDialog.findViewById(R.id.edit_folder_current_icon_imageview);
        ImageView newFavoriteIconImageView = alertDialog.findViewById(R.id.edit_folder_webpage_favorite_icon_imageview);
        EditText nameEditText = alertDialog.findViewById(R.id.edit_folder_name_edittext);
        Spinner folderSpinner = alertDialog.findViewById(R.id.edit_folder_parent_folder_spinner);
        EditText displayOrderEditText = alertDialog.findViewById(R.id.edit_folder_display_order_edittext);
        Button editButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);

        // Store the current folder values.
        String currentFolderName = folderCursor.getString(folderCursor.getColumnIndex(BookmarksDatabaseHelper.BOOKMARK_NAME));
        int currentDisplayOrder = folderCursor.getInt(folderCursor.getColumnIndex(BookmarksDatabaseHelper.DISPLAY_ORDER));
        String parentFolder = folderCursor.getString(folderCursor.getColumnIndex(BookmarksDatabaseHelper.PARENT_FOLDER));

        // Set the database ID.
        databaseIdTextView.setText(String.valueOf(folderCursor.getInt(folderCursor.getColumnIndex(BookmarksDatabaseHelper._ID))));

        // Get the current favorite icon byte array from the `Cursor`.
        byte[] currentIconByteArray = folderCursor.getBlob(folderCursor.getColumnIndex(BookmarksDatabaseHelper.FAVORITE_ICON));

        // Convert the byte array to a `Bitmap` beginning at the first byte and ending at the last.
        Bitmap currentIconBitmap = BitmapFactory.decodeByteArray(currentIconByteArray, 0, currentIconByteArray.length);

        // Display the current icon bitmap in `edit_bookmark_current_icon`.
        currentIconImageView.setImageBitmap(currentIconBitmap);

        // Set the new favorite icon bitmap.
        newFavoriteIconImageView.setImageBitmap(favoriteIconBitmap);

        // Populate the folder name edit text.
        nameEditText.setText(currentFolderName);

        // Setup a matrix cursor for "Home Folder".
        String[] matrixCursorColumnNames = {BookmarksDatabaseHelper._ID, BookmarksDatabaseHelper.BOOKMARK_NAME};
        MatrixCursor matrixCursor = new MatrixCursor(matrixCursorColumnNames);
        matrixCursor.addRow(new Object[]{HOME_FOLDER_DATABASE_ID, getString(R.string.home_folder)});

        // Add all subfolders of the current folder to the list of folders not to display.
        String exceptFolders = getStringOfSubfolders(currentFolderName, bookmarksDatabaseHelper);

        Log.i("Folders", "String of Folders Not To Display:  " + exceptFolders);

        // Get a cursor with the list of all the folders.
        Cursor foldersCursor = bookmarksDatabaseHelper.getFoldersExcept(exceptFolders);

        // Combine the matrix cursor and the folders cursor.
        MergeCursor foldersMergeCursor = new MergeCursor(new Cursor[]{matrixCursor, foldersCursor});

        // Remove the incorrect lint warning that `getContext()` might be null.
        assert getContext() != null;

        // Create a resource cursor adapter for the spinner.
        ResourceCursorAdapter foldersCursorAdapter = new ResourceCursorAdapter(getContext(), R.layout.databaseview_spinner_item, foldersMergeCursor, 0) {
            @Override
            public void bindView(View view, Context context, Cursor cursor) {
                // Get handles for the spinner views.
                ImageView spinnerItemImageView = view.findViewById(R.id.spinner_item_imageview);
                TextView spinnerItemTextView = view.findViewById(R.id.spinner_item_textview);

                // Set the folder icon according to the type.
                if (foldersMergeCursor.getPosition() == 0) {  // Set the `Home Folder` icon.
                    // Set the gray folder image.  `ContextCompat` must be used until the minimum API >= 21.
                    spinnerItemImageView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.folder_gray));
                } else {  // Set a user folder icon.
                    // Get the folder icon byte array.
                    byte[] folderIconByteArray = cursor.getBlob(cursor.getColumnIndex(BookmarksDatabaseHelper.FAVORITE_ICON));

                    // Convert the byte array to a bitmap beginning at the first byte and ending at the last.
                    Bitmap folderIconBitmap = BitmapFactory.decodeByteArray(folderIconByteArray, 0, folderIconByteArray.length);

                    // Set the folder icon.
                    spinnerItemImageView.setImageBitmap(folderIconBitmap);
                }

                // Set the text view to display the folder name.
                spinnerItemTextView.setText(cursor.getString(cursor.getColumnIndex(BookmarksDatabaseHelper.BOOKMARK_NAME)));
            }
        };

        // Set the `ResourceCursorAdapter` drop drown view resource.
        foldersCursorAdapter.setDropDownViewResource(R.layout.databaseview_spinner_dropdown_items);

        // Set the adapter for the folder `Spinner`.
        folderSpinner.setAdapter(foldersCursorAdapter);

        // Select the current folder in the `Spinner` if the bookmark isn't in the "Home Folder".
        if (!parentFolder.equals("")) {
            // Get the database ID of the parent folder.
            int parentFolderDatabaseId = bookmarksDatabaseHelper.getFolderDatabaseId(folderCursor.getString(folderCursor.getColumnIndex(BookmarksDatabaseHelper.PARENT_FOLDER)));

            // Initialize `parentFolderPosition` and the iteration variable.
            int parentFolderPosition = 0;
            int i = 0;

            // Find the parent folder position in folders `ResourceCursorAdapter`.
            do {
                if (foldersCursorAdapter.getItemId(i) == parentFolderDatabaseId) {
                    // Store the current position for the parent folder.
                    parentFolderPosition = i;
                } else {
                    // Try the next entry.
                    i++;
                }
                // Stop when the parent folder position is found or all the items in the `ResourceCursorAdapter` have been checked.
            } while ((parentFolderPosition == 0) && (i < foldersCursorAdapter.getCount()));

            // Select the parent folder in the `Spinner`.
            folderSpinner.setSelection(parentFolderPosition);
        }

        // Store the current folder database ID.
        int currentParentFolderDatabaseId = (int) folderSpinner.getSelectedItemId();

        // Populate the display order `EditText`.
        displayOrderEditText.setText(String.valueOf(folderCursor.getInt(folderCursor.getColumnIndex(BookmarksDatabaseHelper.DISPLAY_ORDER))));

        // Initially disable the edit button.
        editButton.setEnabled(false);

        // Update the edit button if the icon selection changes.
        iconRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            // Update the edit button.
            updateEditButton(alertDialog, bookmarksDatabaseHelper, currentFolderName, currentParentFolderDatabaseId, currentDisplayOrder);
        });

        // Update the edit button if the bookmark name changes.
        nameEditText.addTextChangedListener(new TextWatcher() {
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
                // Update the edit button.
                updateEditButton(alertDialog, bookmarksDatabaseHelper, currentFolderName, currentParentFolderDatabaseId, currentDisplayOrder);
            }
        });

        // Update the edit button if the folder changes.
        folderSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Update the edit button.
                updateEditButton(alertDialog, bookmarksDatabaseHelper, currentFolderName, currentParentFolderDatabaseId, currentDisplayOrder);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        // Update the edit button if the display order changes.
        displayOrderEditText.addTextChangedListener(new TextWatcher() {
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
                // Update the edit button.
                updateEditButton(alertDialog, bookmarksDatabaseHelper, currentFolderName, currentParentFolderDatabaseId, currentDisplayOrder);
            }
        });

        // Allow the `enter` key on the keyboard to save the bookmark from the bookmark name `EditText`.
        nameEditText.setOnKeyListener((View v, int keyCode, KeyEvent event) -> {
            // Save the bookmark if the event is a key-down on the "enter" button.
            if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER) && editButton.isEnabled()) {  // The enter key was pressed and the edit button is enabled.
                // Trigger the `Listener` and return the `DialogFragment` to the parent activity.
                editBookmarkFolderDatabaseViewListener.onSaveBookmarkFolder(this, folderDatabaseId, favoriteIconBitmap);

                // Manually dismiss `alertDialog`.
                alertDialog.dismiss();

                // Consume the event.
                return true;
            } else {  // If any other key was pressed, or if the edit button is currently disabled, do not consume the event.
                return false;
            }
        });

        // Allow the "enter" key on the keyboard to save the bookmark from the display order `EditText`.
        displayOrderEditText.setOnKeyListener((View v, int keyCode, KeyEvent event) -> {
            // Save the bookmark if the event is a key-down on the "enter" button.
            if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER) && editButton.isEnabled()) {  // The enter key was pressed and the edit button is enabled.
                // Trigger the `Listener` and return the `DialogFragment` to the parent activity.
                editBookmarkFolderDatabaseViewListener.onSaveBookmarkFolder(this, folderDatabaseId, favoriteIconBitmap);

                // Manually dismiss the `AlertDialog`.
                alertDialog.dismiss();

                // Consume the event.
                return true;
            } else { // If any other key was pressed, or if the edit button is currently disabled, do not consume the event.
                return false;
            }
        });

        // `onCreateDialog` requires the return of an `AlertDialog`.
        return alertDialog;
    }

    private void updateEditButton(AlertDialog alertDialog, BookmarksDatabaseHelper bookmarksDatabaseHelper, String currentFolderName, int currentParentFolderDatabaseId, int currentDisplayOrder) {
        // Get handles for the views.
        EditText nameEditText = alertDialog.findViewById(R.id.edit_folder_name_edittext);
        Spinner folderSpinner = alertDialog.findViewById(R.id.edit_folder_parent_folder_spinner);
        EditText displayOrderEditText = alertDialog.findViewById(R.id.edit_folder_display_order_edittext);
        RadioButton currentIconRadioButton = alertDialog.findViewById(R.id.edit_folder_current_icon_radiobutton);
        Button editButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);

        // Get the values from the dialog.
        String newFolderName = nameEditText.getText().toString();
        int newParentFolderDatabaseId = (int) folderSpinner.getSelectedItemId();
        String newDisplayOrder = displayOrderEditText.getText().toString();

        // Get a cursor for the new folder name if it exists.
        Cursor folderExistsCursor = bookmarksDatabaseHelper.getFolder(newFolderName);

        // Is the new folder name empty?
        boolean folderNameNotEmpty = !newFolderName.isEmpty();

        // Does the folder name already exist?
        boolean folderNameAlreadyExists = (!newFolderName.equals(currentFolderName) && (folderExistsCursor.getCount() > 0));

        // Has the favorite icon changed?
        boolean iconChanged = !currentIconRadioButton.isChecked();

        // Has the name been renamed?
        boolean folderRenamed = (!newFolderName.equals(currentFolderName) && !folderNameAlreadyExists);

        // Has the folder changed?
        boolean parentFolderChanged = newParentFolderDatabaseId != currentParentFolderDatabaseId;

        // Has the display order changed?
        boolean displayOrderChanged = !newDisplayOrder.equals(String.valueOf(currentDisplayOrder));

        // Is the display order empty?
        boolean displayOrderNotEmpty = !newDisplayOrder.isEmpty();

        // Update the enabled status of the edit button.
        editButton.setEnabled((iconChanged || folderRenamed || parentFolderChanged || displayOrderChanged) && folderNameNotEmpty && displayOrderNotEmpty);
    }

    private String getStringOfSubfolders(String folderName, BookmarksDatabaseHelper bookmarksDatabaseHelper) {
        // Get a cursor will all the immediate subfolders.
        Cursor subfoldersCursor = bookmarksDatabaseHelper.getSubfolders(folderName);

        // Initialize a string builder to track the folders not to display in the spinner and populate it with the current folder.
        StringBuilder exceptFoldersStringBuilder = new StringBuilder(DatabaseUtils.sqlEscapeString(folderName));

        for (int i = 0; i < subfoldersCursor.getCount(); i++) {
            // Move the subfolder cursor to the current item.
            subfoldersCursor.moveToPosition(i);

            // Get the name of the subfolder.
            String subfolderName = subfoldersCursor.getString(subfoldersCursor.getColumnIndex(BookmarksDatabaseHelper.BOOKMARK_NAME));

            // Add a comma to the end of the existing string.
            exceptFoldersStringBuilder.append(",");

            // Get the folder name and run the task for any subfolders.
            String subfolderString = getStringOfSubfolders(subfolderName, bookmarksDatabaseHelper);

            // Add the folder name to the string builder.
            exceptFoldersStringBuilder.append(subfolderString);
        }

        // Return the string of folders.
        return exceptFoldersStringBuilder.toString();
    }
}