package com.nirvana.notepad.structures;

import android.provider.BaseColumns;

public final class UserNotes {

    private UserNotes(){}

    public static class NotesEntry implements BaseColumns {
        public static final String TABLE_NAME = "user_notes";
        public static final String COLUMN_SN = "serial_no";
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_CONTENT = "content";
        public static final String COLUMN_DATE_TIME = "date_time";
    }

}
