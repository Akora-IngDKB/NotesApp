package com.akoraingdkb.notesapp;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class EditNoteActivity extends AppCompatActivity implements Constants {
    private EditText noteContent;
    private static int seenAlertDialog = 0;
    private SharedPreferences sharedPreferences;

    private static String noteTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState)

    {
        super.onCreate(savedInstanceState);

        sharedPreferences = getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE);
        boolean usingNightTheme = sharedPreferences.getBoolean(THEME_KEY, true);

        if (usingNightTheme) {
            setTheme(R.style.NightTheme);
            setContentView(R.layout.activity_edit_note);
        } else {
            setTheme(R.style.DayTheme);
            setContentView(R.layout.activity_edit_note_day);
        }

        setContentView(R.layout.activity_edit_note);

        noteContent = (EditText) findViewById(R.id.note_content);

        Bundle bundle = getIntent().getExtras();
        noteTitle = bundle.getString(SELECTED_NOTE_TITLE);
        this.setTitle(noteTitle);

    }

    /**
     * This method checks to see if app has permission to write to external storage.
     *
     * @return true if permission has been granted already, otherwise show rationale and return false.
     */
    private boolean checkPermissionWriteExternalStorage() {
        // Check if I have permission to write to storage
        return ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    private void showPermissionRationale() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                // Show an explanation to the user *asynchronously* -- don't block this tread waiting for the user's response!
                // After the user sees the explanation, try again to request the permission
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE);

            } else {
                // No explanation needed
                // The user choose "Never show again" in previous rationale
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE:
                // If request is cancelled, the result arrays are empty

                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission has been granted
                    saveNoteToStorage(noteTitle, noteContent.getText().toString());
                    Toast.makeText(this, R.string.change_save_success, Toast.LENGTH_SHORT).show();
                    this.finish();
                } else {
                    // Permission has been denied by user
                    // Disable the functionality that depends on this permission
                    this.invalidateOptionsMenu();
                    showAlert(getString(R.string.disable_save_notice), getString(R.string.neutral_okay));
                }
                break;
            // Other case lines to check for other permissions that this app might need
        }
    }

    private void hideKeyboard() {
        noteContent.clearFocus();
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }


    /* This dialog will be used when the user denies permission from rationale */
    private void showAlert(String message, String neutralButtonText) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder
                .setTitle(getString(R.string.alert_title_notice))
                .setMessage(message)
                .setCancelable(false)
                .setNeutralButton(neutralButtonText, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                }).create().show();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.edit_note_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                hideKeyboard();
                int tempSeenAlertDialog = sharedPreferences.getInt(SEEN_DISABLE_SAVE_DIALOG, 0);
                if (checkPermissionWriteExternalStorage()) {
                    // App has permission
                    // Auto-Save data
                    saveNoteToStorage(noteTitle, noteContent.getText().toString());
                    this.finish();
                    break;
                } else {
                    // App does not have permission
                    if (tempSeenAlertDialog < 1) {
                        showAlert(getString(R.string.disable_save_notice), getString(R.string.neutral_okay));
                        sharedPreferences.edit().putInt(SEEN_DISABLE_SAVE_DIALOG, ++seenAlertDialog).apply();
                        break;
                    } else {
                        this.finish();
                        Toast.makeText(getApplicationContext(), R.string.changes_discarded, Toast.LENGTH_SHORT).show();
                        break;
                    }
                }

            case R.id.menu_export:
                startActivity(new Intent(this, ExportAsTxtActivity.class).putExtra(SELECTED_NOTE_TITLE, noteTitle));
                break;

            case R.id.menu_share:
                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);
                shareIntent.putExtra(Intent.EXTRA_TEXT, readNoteFromStorage(noteTitle));
                shareIntent.setType("text/plain");

                if (shareIntent.resolveActivity(getPackageManager()) != null)
                    startActivity(Intent.createChooser(shareIntent, "Share Note via:"));
                break;

            case R.id.menu_save:
                hideKeyboard();
                // Handle save note stuff
                if (checkPermissionWriteExternalStorage()) {
                    boolean fileSaveStatus = saveNoteToStorage(noteTitle, noteContent.getText().toString());
                    if (fileSaveStatus) {
                        Toast.makeText(this, R.string.change_save_success, Toast.LENGTH_SHORT).show();
                        this.finish();
                        break;
                    } else {
                        showAlert(getString(R.string.error_saving_file_message), getString(R.string.neutral_okay));
                        break;
                    }
                } else {
                    showPermissionRationale();
                    break;
                }

            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (checkPermissionWriteExternalStorage())
            saveNoteToStorage(noteTitle, noteContent.getText().toString());     // Store note content whenever user leaves the app
    }

    @Override
    protected void onResume() {
        super.onResume();
        noteContent.setText(readNoteFromStorage(noteTitle));         // Restore note content whenever user opens activity
        sharedPreferences = getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE);
        hideKeyboard();
    }

    @Override
    public void onBackPressed() {
        int tempSeenAlertDialog = sharedPreferences.getInt(SEEN_DISABLE_SAVE_DIALOG, 0);
        if (checkPermissionWriteExternalStorage()) {
            saveNoteToStorage(noteTitle, noteContent.getText().toString());
            this.finish();
        } else {
            // App does not have permission
            // Auto-Save will disabled
            if (tempSeenAlertDialog < 1) {
                showAlert(getString(R.string.disable_save_notice), getString(R.string.neutral_okay));
                sharedPreferences.edit().putInt(SEEN_DISABLE_SAVE_DIALOG, ++seenAlertDialog).apply();
            } else {
                this.finish();
                Toast.makeText(getApplicationContext(), R.string.changes_discarded, Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Method to save file to storage
     */
    boolean saveNoteToStorage(@NonNull String title, @NonNull String noteContent) {
        //create a FileOutputStream object
        FileOutputStream fileOutputStream = null;   //will be initialized later

        try {
            //check if file exists, then open one
            fileOutputStream = openFileOutput(title, Context.MODE_PRIVATE); //open file
            fileOutputStream.write(noteContent.getBytes());     //write content (taken from writing view)

            return true;
        } catch (FileNotFoundException e) {
            Log.e(TAG_FILE_OPERATIONS, "FileNotFoundException: " + e.getMessage());     //report IO exception to log
            e.printStackTrace();

            return false;
        } catch (IOException e) {
            Log.e(TAG_FILE_OPERATIONS, "IOException: " + e.getMessage());     //report IO exception to log
            e.printStackTrace();

            return false;
        } finally {
            try {
                if (fileOutputStream != null)
                    fileOutputStream.close();   //close only if data is written to disk
            } catch (IOException e) {
                Log.e(TAG_FILE_OPERATIONS, "IOException: " + e.getMessage());     //report IO exception to log
            }
        }
    }

    /**
     * Method to read file from storage.
     *
     * @return the saved note content is found.
     */

    String readNoteFromStorage(@NonNull String title) {
        //create a FileInputStream object
        FileInputStream fileInputStream = null;   //will be initialized later
        String fileData = null;     //to store the data read from previously saved file

        try {
            //check if file exists, then open one
            fileInputStream = openFileInput(noteTitle); //open file
            int size = fileInputStream.available();
            byte[] buffer = new byte[size];
            fileInputStream.read(buffer);   //convert read data to bytes
            fileInputStream.close();
            fileData = new String(buffer, "UTF-8"); // convert bytes to string

        } catch (FileNotFoundException e) {
            Log.e(TAG_FILE_OPERATIONS, "FileNotFoundException: " + e.getMessage());     //report IO exception to log
            e.printStackTrace();

        } catch (IOException e) {
            Log.e(TAG_FILE_OPERATIONS, "IOException: " + e.getMessage());     //report io exception to log
            e.printStackTrace();

        } finally {
            try {
                if (fileInputStream != null)
                    fileInputStream.close();   //close only if data is written to disk
            } catch (IOException e) {
                Log.e(TAG_FILE_OPERATIONS, "IOException: " + e.getMessage());     //report io exception to log
            }
        }

        return fileData;    //return the data obtained
    }

    boolean deleteSave(@NonNull String title) {
        boolean status = false;
        try {
            status = saveNoteToStorage(title, null);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        return status;
    }

}
