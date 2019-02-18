package com.akoraingdkb.notesapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

import static com.akoraingdkb.notesapp.DatabaseContract.DatabaseStructure.COL_FIRST_LETTER;
import static com.akoraingdkb.notesapp.DatabaseContract.DatabaseStructure.COL_TITLE;
import static com.akoraingdkb.notesapp.DatabaseContract.DatabaseStructure.TABLE_NAME;
import static com.akoraingdkb.notesapp.DatabaseContract.DatabaseStructure._ID;

class NoteDatabase extends SQLiteOpenHelper implements Constants{

    NoteDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String createTableQuery = "CREATE TABLE " + TABLE_NAME + " ("
                + _ID + " INTEGER PRIMARY KEY, "
                + COL_TITLE + " TEXT, "
                + COL_FIRST_LETTER + " TEXT);";
        sqLiteDatabase.execSQL(createTableQuery);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(sqLiteDatabase);
    }

    int addNewNote(Note newNote) {
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();

        // Query the database for existing note with same title
        Cursor checkExist = sqLiteDatabase
                .rawQuery("SELECT " + COL_TITLE + " FROM " + TABLE_NAME + " WHERE " + COL_TITLE + " = \"" + newNote.getTitle() + "\"", null);

        if (checkExist.moveToFirst()) {
            // Existing note has been found
            // Close the cursor and return
            checkExist.close();
            return 0;
        } else {
            // No existing note found
            // Insert new note
            ContentValues contentValues = new ContentValues();
            contentValues.put(COL_TITLE, newNote.getTitle());
            contentValues.put(COL_FIRST_LETTER, newNote.getFirstLetter());

            checkExist.close();
            return (int) sqLiteDatabase.insert(TABLE_NAME, null, contentValues);
        }
    }

    List<Note> getAllNotes() {
        List<Note> notes = new ArrayList<>();
        SQLiteDatabase sqLiteDatabase = this.getReadableDatabase();
        Cursor cursor = sqLiteDatabase.rawQuery("SELECT * FROM " + TABLE_NAME, null);

        if (cursor.moveToFirst()) {
            do {
                Note note = new Note();
                note.setTitle(cursor.getString(1));
                note.setFirstLetter(cursor.getString(2));

                notes.add(note);
            } while (cursor.moveToNext());
        }

        cursor.close();
        return notes;
    }

    int deleteSingleNote(String id) {
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        return sqLiteDatabase.delete(TABLE_NAME, "_id = ?", new String[]{id});
    }

}
