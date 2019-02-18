package com.akoraingdkb.notesapp;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputEditText;
import android.support.v4.content.FileProvider;
import android.support.v4.widget.ContentLoadingProgressBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class ExportAsTxtActivity extends AppCompatActivity implements Constants {
    private TextInputEditText editText;
    private ContentLoadingProgressBar progressBar;
    private Button btnOkay, btnOpen;
    private TextView textView;
    private CheckBox checkBox;

    private static String fileTitle;        // The user chosen title of file
    private static String noteTitle;        // The default title of file - the note title in the database

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_export_as_txt);

        // Get the title of the note
        // It will be used as the default highlighted title of tile in the edit text
        Bundle bundle = getIntent().getExtras();
        noteTitle = bundle.getString(SELECTED_NOTE_TITLE);

        // Prevent activity (dialog) from being destroyed when user clicks outside
        this.setFinishOnTouchOutside(false);

        editText = (TextInputEditText) findViewById(R.id.edit_note_title);
        editText.setText(noteTitle);

        // Request focus and show soft keyboard automatically
        editText.requestFocus();
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

        progressBar = (ContentLoadingProgressBar) findViewById(R.id.progress_bar);
        btnOkay = (Button) findViewById(R.id.btn_okay);
        btnOpen = (Button) findViewById(R.id.btn_open);
        textView = (TextView) findViewById(R.id.text_view);
        checkBox = (CheckBox) findViewById(R.id.check_box);

        final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab_save_note);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editText.clearFocus();
                fileTitle = editText.getText().toString().trim();

                // Store the new note if the a title has been provided
                if (fileTitle.length() > 0) {
                    // Check for other validity factors like special characters
                    // User has provided a valid file name
                    editText.setVisibility(View.GONE);
                    fab.setVisibility(View.GONE);
                    progressBar.setVisibility(View.VISIBLE);

                    boolean fileStatus = exportAsTxt(readNoteFromStorage());

                    if (fileStatus) {
                        editText.setVisibility(View.GONE);
                        progressBar.setVisibility(View.GONE);
                        textView.setVisibility(View.VISIBLE);
                        textView.setText(textView.getText().toString().concat(" " + fileTitle));
                        checkBox.setVisibility(View.VISIBLE);
                        btnOpen.setVisibility(View.VISIBLE);

                        // This OPEN button opens the exported file with an intent
                        btnOpen.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent fileIntent = new Intent(Intent.ACTION_VIEW);

                                fileIntent.setDataAndType(
                                        Uri.fromFile(new File(SDCARD_PATH
                                                .concat(File.separator + "NotesApp" + File.separator +
                                                        fileTitle + TEXT_FILE_NAME_SUFFIX))), "text/plain");

                                fileIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                fileIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                                if (fileIntent.resolveActivity(getPackageManager()) != null) {
                                    try {
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                                            Toast.makeText(ExportAsTxtActivity.this,
                                                    "This functionality is still being fixed", Toast.LENGTH_LONG).show();
                                        else
                                            startActivity(Intent.createChooser(fileIntent, "Open with:"));
                                        ExportAsTxtActivity.this.finish();
                                    } catch (ActivityNotFoundException e) {
                                        e.printStackTrace();
                                        Toast.makeText(getApplicationContext(),
                                                "No app found on your device that can open \".txt\" files", Toast.LENGTH_LONG).show();
                                    }
                                }
                            }
                        });

                        // This OKAY button closes the dialog
                        btnOkay.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                ExportAsTxtActivity.this.finish();
                            }
                        });

                        // When the checkbox is checked, show the OPEN button
                        // else, show the okay button
                        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                            @Override
                            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                                if (compoundButton.isChecked()) {
                                    btnOkay.setVisibility(View.GONE);
                                    btnOpen.setVisibility(View.VISIBLE);
                                } else {
                                    btnOpen.setVisibility(View.GONE);
                                    btnOkay.setVisibility(View.VISIBLE);
                                }
                            }
                        });
                        btnOkay.setVisibility(View.GONE);
                    } else {
                        Toast.makeText(ExportAsTxtActivity.this, "Failed to export file", Toast.LENGTH_LONG).show();
                        ExportAsTxtActivity.this.finish();
                    }
                } else {
                    // No title has been provided
                    // Issue an error
                    editText.setError(getString(R.string.invalid_title_error_message));
                }
            }
        });
    }

    private boolean exportAsTxt(String noteContent) {
        File path = new File(APP_DIR_PATH);
        if (!path.exists()) {
            boolean pathBool = path.mkdir();
            Toast.makeText(this, "Path creation " + pathBool, Toast.LENGTH_SHORT).show();
        }

        File file = new File(path, fileTitle.concat(TEXT_FILE_NAME_SUFFIX));
        // Create a file output stream object
        FileOutputStream fileOutputStream = null;           // Will be initialized later

        try {
            // Check if file exists, append; else, create a new file using the note title as file name
            fileOutputStream = new FileOutputStream(file);
            fileOutputStream.write(noteContent.getBytes());
            return true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.e(TAG_FILE_OPERATIONS, "File not found");
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG_FILE_OPERATIONS, "Error writing to file");
            return false;
        } finally {
            try {
                if (fileOutputStream != null)
                    fileOutputStream.close();           // Make sure file is written to disk before closing
            } catch (IOException e) {
                e.printStackTrace();
                Log.e(TAG_FILE_OPERATIONS, "Error closing output stream");
            }
        }
    }

    private String readNoteFromStorage() {
        // Create a FileInputStream object
        FileInputStream fileInputStream = null;   // Will be initialized later
        String fileData = null;     // To store the data read from previously saved file

        try {
            // Check if file exists, then open one
            fileInputStream = openFileInput(noteTitle); // Open file
            int size = fileInputStream.available();
            byte[] buffer = new byte[size];
            //noinspection ResultOfMethodCallIgnored
            fileInputStream.read(buffer);   // Convert read data to bytes
            fileInputStream.close();
            fileData = new String(buffer, "UTF-8"); // Convert bytes to string

        } catch (FileNotFoundException e) {
            Log.e(TAG_FILE_OPERATIONS, "FileNotFoundException: " + e.getMessage());     // Report IO exception to log
            e.printStackTrace();

        } catch (IOException e) {
            Log.e(TAG_FILE_OPERATIONS, "IOException: " + e.getMessage());     //Report io exception to log
            e.printStackTrace();

        } finally {
            try {
                if (fileInputStream != null)
                    fileInputStream.close();   // Close only if data is written to disk
            } catch (IOException e) {
                Log.e(TAG_FILE_OPERATIONS, "IOException: " + e.getMessage());     // Report io exception to log
            }
        }

        return fileData;    // Return the data obtained
    }
}
