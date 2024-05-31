package com.nirvana.notepad.helpers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.nirvana.notepad.structures.Notes;
import com.nirvana.notepad.structures.UserNotes;

import java.util.ArrayList;

public class NotesDB extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 4;
    private static final String DATABASE_NAME = "Notes.db";

    private static final String SQL_CREATE_TABLE =
            "CREATE TABLE " + UserNotes.NotesEntry.TABLE_NAME + " (" +
                    UserNotes.NotesEntry.COLUMN_SN + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    UserNotes.NotesEntry.COLUMN_TITLE + " TEXT," +
                    UserNotes.NotesEntry.COLUMN_CONTENT + " TEXT, " +
                    UserNotes.NotesEntry.COLUMN_DATE_TIME + " TEXT)";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + UserNotes.NotesEntry.TABLE_NAME;

    public NotesDB(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }

    public int addNewNote(ContentValues values) {

        SQLiteDatabase db = this.getWritableDatabase();

        // Insert the new row, returning the primary key value of the new row
        long newRowId = db.insert(UserNotes.NotesEntry.TABLE_NAME, null, values);
        Log.d("rid", String.valueOf(newRowId));

        return (int) newRowId;

    }

    private static final String SQL_SELECT_ALL_NOTES = "SELECT * FROM " + UserNotes.NotesEntry.TABLE_NAME +" ORDER BY "+ UserNotes.NotesEntry.COLUMN_SN + " DESC";

    public ArrayList<Notes> getAllNotes(){

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(SQL_SELECT_ALL_NOTES, null);

        ArrayList<Notes> list = new ArrayList<>();

        while (cursor.moveToNext()){
            int serial = cursor.getInt(0);
            String title = cursor.getString(1);
            String content = cursor.getString(2);
            String dateTime = cursor.getString(3);

            list.add(new Notes(serial,title,content,dateTime));

        }
        cursor.close();

        return list;

    }

    private static final String SQL_SELECT_ONE_NOTE = "SELECT * FROM " + UserNotes.NotesEntry.TABLE_NAME +
                                                    " WHERE serial_no =";

    public Notes getOneNote(int serialNo){

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(SQL_SELECT_ONE_NOTE + serialNo, null);

        Notes notes = null;

        while (cursor.moveToNext()) {
            int serial = cursor.getInt(0);
            String title = cursor.getString(1);
            String content = cursor.getString(2);
            String dateTime = cursor.getString(3);

            notes = new Notes(serial, title, content, dateTime);
        }
        cursor.close();

        return notes;

    }

    public int updateNote(int serialNo, ContentValues values) {

        SQLiteDatabase db = this.getWritableDatabase();

        String selection = UserNotes.NotesEntry.COLUMN_SN + " LIKE ?";
        String[] selectionArgs = {String.valueOf(serialNo)};

        int count = db.update(
                UserNotes.NotesEntry.TABLE_NAME,
                values,
                selection,
                selectionArgs);

        if (count > 0){
            return 1;
        } else return -1;
    }

    public int deleteNote(int serialNo) {

        SQLiteDatabase db = this.getWritableDatabase();

        String selection = UserNotes.NotesEntry.COLUMN_SN + " LIKE ?";

        String[] selectionArgs = {String.valueOf(serialNo)};

        int deletedRows = db.delete(UserNotes.NotesEntry.TABLE_NAME, selection, selectionArgs);

        if (deletedRows > 0){
            return 1;
        } else return -1;

    }
}
