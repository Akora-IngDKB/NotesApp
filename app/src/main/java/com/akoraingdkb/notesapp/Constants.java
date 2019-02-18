package com.akoraingdkb.notesapp;

import android.os.Environment;

interface Constants {
    String PREF_FILE_NAME = "NotePrefFile";

    String APP_DIR_PATH = "sdcard/NotesApp";

    String SDCARD_PATH = Environment.getExternalStorageDirectory().getAbsolutePath();

    String TEXT_FILE_NAME_SUFFIX = ".txt";

    String SELECTED_NOTE_TITLE = "SelectedNoteTitle";

    String DATABASE_NAME = "NotesDatabase.db";

    int DATABASE_VERSION = 1;

    int REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE = 1604;

    String SEEN_DISABLE_SAVE_DIALOG = "SeenDisableSaveDialog";

    String TAG_FILE_OPERATIONS = "FILE_OPERATIONS";

    String PREF_KEY_THEME = "pref_key_theme";

    String PREF_KEY_ABOUT = "pref_key_about";

    String THEME_KEY = "THEME_KEY";
}
