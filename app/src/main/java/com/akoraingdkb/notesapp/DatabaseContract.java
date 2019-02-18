package com.akoraingdkb.notesapp;

import android.provider.BaseColumns;

final class DatabaseContract {

    private DatabaseContract(){}

    static final class DatabaseStructure implements BaseColumns{
        static final String TABLE_NAME = "Notes";
        static final String COL_TITLE = "TITLE";
        static final String COL_FIRST_LETTER = "FIRST_LETTER";
    }
}
