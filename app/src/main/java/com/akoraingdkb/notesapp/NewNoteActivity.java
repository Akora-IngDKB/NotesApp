package com.akoraingdkb.notesapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;

/**
 * This class will be displayed as a dialog with an editText for the name of the dialog.
 */

public class NewNoteActivity extends AppCompatActivity implements Constants{
    private NoteDatabase noteDatabase;
    private TextInputEditText editText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_note);

        // Prevent activity (dialog) from being destroyed when user clicks outside
        this.setFinishOnTouchOutside(false);

        editText = (TextInputEditText) findViewById(R.id.edit_note_title);

        // Request focus and show soft keyboard automatically
        editText.requestFocus();
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

        noteDatabase = new NoteDatabase(NewNoteActivity.this);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab_save_note);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String newNoteTile = editText.getText().toString().trim();

                // Store the new note if the a title has been provided
                if (newNoteTile.length() > 0) {
                    String newNoteFirstLetter = newNoteTile.substring(0, 1).toUpperCase();

                    Note newNote = new Note();
                    newNote.setTitle(newNoteTile);
                    newNote.setFirstLetter(newNoteFirstLetter);

                    int newNoteID = noteDatabase.addNewNote(newNote);

                    if (newNoteID > 0) {
                        SharedPreferences sharedPreferences = getSharedPreferences(MainActivity.PREF_FILE_NAME, Context.MODE_PRIVATE);
                        sharedPreferences.edit().putInt(newNote.getTitle(), newNoteID).apply();

                        NewNoteActivity.this.finish();
                    } else
                        editText.setError(getString(R.string.title_collision_error_message));
                } else {
                    // No title has been provided
                    // Issue an error
                    editText.setError(getString(R.string.invalid_title_error_message));
                }
            }
        });
    }

}
