package com.nirvana.notepad.activities;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Environment;
import android.print.PrintAttributes;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.nabinbhandari.android.permissions.PermissionHandler;
import com.nabinbhandari.android.permissions.Permissions;
import com.nirvana.notepad.R;
import com.nirvana.notepad.helpers.NotesDB;
import com.nirvana.notepad.structures.Notes;
import com.nirvana.notepad.structures.UserNotes;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class EditorActivity extends AppCompatActivity {

    Context context;
    Activity activity;
    NotesDB notesDB;
    EditText title, content;
    int serialNo = -1;
    Notes note;
    boolean backPressed = false;
    boolean chechPermission = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Editor");
        setSupportActionBar(toolbar);
        ActionBar bar = getSupportActionBar();
        assert bar != null;
        bar.setDisplayHomeAsUpEnabled(true);

        context = this;
        activity = this;
        notesDB = new NotesDB(context);
        title = findViewById(R.id.editor_title);
        content = findViewById(R.id.editor_content);

        Intent intent = getIntent();

        if (intent.getIntExtra("serial_no",-2) == -1){
            content.requestFocus();
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(content, InputMethodManager.SHOW_IMPLICIT);
        }else if (intent.getIntExtra("serial_no",-2) != -1){
            serialNo = getIntent().getIntExtra("serial_no",-2);
            getNote(serialNo);
        }

        System.out.println("serial no -> " + serialNo);

    }

    @Override
    protected void onPause() {
        saveNote();
        if (backPressed) {
            finish();
        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        if (serialNo != -1){
            getNote(serialNo);
        }
        super.onResume();
    }

    @Override
    public void onBackPressed() {
        //saveNote();
        backPressed = true;
        super.onBackPressed();
    }

    private void saveNote(){

        ContentValues values = getNewNote();
        String title = values.getAsString(UserNotes.NotesEntry.COLUMN_TITLE);
        String content = values.getAsString(UserNotes.NotesEntry.COLUMN_CONTENT);

        if (values.size() > 0) {
            if (serialNo != -1){
                values.remove(UserNotes.NotesEntry.COLUMN_SN);
                if (!content.equals(note.getContent()) || !title.equals(note.getTitle())){
                    int res = notesDB.updateNote(serialNo, values);
                    if (res == 1){
                        Toast.makeText(context, "Note updated successfully!", Toast.LENGTH_SHORT).show();
                    }
                }
            } else {
                int rowId = notesDB.addNewNote(values);
                if (rowId > 0) {
                    serialNo = rowId;
                    Toast.makeText(context, "Note added successfully!", Toast.LENGTH_SHORT).show();
                }
            }
        }

    }

    private ContentValues getNewNote(){

        ContentValues values = new ContentValues();

        String t = title.getText().toString();
        String c = content.getText().toString();

        if (!c.isEmpty()) {

            SimpleDateFormat sdf = new SimpleDateFormat("EEE dd-MM-YYY HH.mm.ss", Locale.getDefault());

            String currentDateTime = sdf.format(new Date());

            if (t.isEmpty()) {
                t = currentDateTime;
            }

            values.put(UserNotes.NotesEntry.COLUMN_TITLE, t);
            values.put(UserNotes.NotesEntry.COLUMN_CONTENT, c);
            values.put(UserNotes.NotesEntry.COLUMN_DATE_TIME, currentDateTime);

            return values;

        } else return values;

    }

    private void getNote(int sn){
        note = notesDB.getOneNote(sn);
        title.setText(note.getTitle());
        content.setText(note.getContent());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.editor_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.menu_discard) {
            createDialog(1);
        } else if (id == R.id.menu_export_to_jpg) {
            checkPermissionForExport(2);
        } else if (id == R.id.menu_export_to_txt) {
            checkPermissionForExport(3);
        } else if (id == R.id.menu_delete) {
            createDialog(4);
        }

        return super.onOptionsItemSelected(item);
    }

    private void checkPermissionForExport(final int action) {
        String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE};
        String rationale = "Please provide storage permission so that you can backup your notes";
        Permissions.Options options = new Permissions.Options()
                .setRationaleDialogTitle("Info")
                .setSettingsDialogTitle("Warning");
        Permissions.check(context, permissions, rationale, options, new PermissionHandler() {
            @Override
            public void onGranted() {
                saveNote();
                getExportDetails(action);
            }

            @Override
            public void onDenied(Context context, ArrayList<String> deniedPermissions) {
                Toast.makeText(context, "You must allow permission to the app from device app settings to backup your notes,", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void getExportDetails(int exportCount){
        String parentFolder = Environment.getExternalStorageDirectory() +
                File.separator + getString(R.string.app_folder_name);

        File folder = new File(parentFolder);
        if (!folder.exists()){
            boolean create = folder.mkdir();
            if (create){
                System.out.println("folder created successfully!");
            } else {
                System.out.println("folder creation failed.");
            }
        } else {
            System.out.println("folder already exist.");
        }

        ContentValues values = getNewNote();

        String fileName = "";

        if (exportCount == 3){
            fileName = values.getAsString(UserNotes.NotesEntry.COLUMN_TITLE) + ".txt";
        } else if (exportCount == 2){
            fileName = values.getAsString(UserNotes.NotesEntry.COLUMN_TITLE) + ".png";
        }

        String fileContent = values.getAsString(UserNotes.NotesEntry.COLUMN_CONTENT);

        File file = new File(parentFolder + File.separator + fileName);

        if (file.exists()){
            if (file.delete()){
                System.out.println("a similar named file was deleted.");
            }
        }
        if (exportCount == 3){
            exportToTXT(file,fileContent);
        } else if (exportCount == 2){
            try {
                exportToPNG(file,fileContent);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void exportToTXT(File file, String fileContent){
        try {
            if (file.createNewFile()){
                FileOutputStream fileOutputStream = new FileOutputStream(file);
                fileOutputStream.write(fileContent.getBytes());
                fileOutputStream.close();
                Toast.makeText(context, "Export Successful!", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void exportToPNG(File file, String fileContent) throws IOException{

        fileContent = "\n" + fileContent;
        fileContent = fileContent.replace("\n","\t\n\t");
        fileContent = fileContent + "\n";

        final Rect bounds = new Rect();
        TextPaint textPaint = new TextPaint() {
            {
                setColor(Color.BLACK);
                setTextAlign(Paint.Align.LEFT);
                setTextSize(30f);
                setAntiAlias(true);
            }
        };
        textPaint.getTextBounds(fileContent, 0, fileContent.length(), bounds);
        StaticLayout mTextLayout = new StaticLayout(fileContent, textPaint,
                bounds.width(), Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
        int maxWidth = -1;
        for (int i = 0; i < mTextLayout.getLineCount(); i++) {
            if (maxWidth < mTextLayout.getLineWidth(i)) {
                maxWidth = (int) mTextLayout.getLineWidth(i);
            }
        }
        final Bitmap bmp = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(context.getResources(),R.drawable.background),
                maxWidth + 5, mTextLayout.getHeight() + 5,true);
        //bmp.eraseColor(Color.BLACK);// just adding black background
        final Canvas canvas = new Canvas(bmp);
        mTextLayout.draw(canvas);
        FileOutputStream stream = new FileOutputStream(file); //create your FileOutputStream here
        bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);
        bmp.recycle();
        stream.close();
        Toast.makeText(context, "Export Successful!", Toast.LENGTH_SHORT).show();
    }

    public void createDialog(final int action){
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        if (action == 1){
                            startActivity(new Intent(context, MainActivity.class));
                            finish();
                        } else if (action == 4){
                            Toast.makeText(context, "Note deleted", Toast.LENGTH_SHORT).show();
                        }
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        //No button clicked
                        break;
                }
            }
        };
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage("Are you sure?").setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("No", dialogClickListener).show();
    }

    public void openKeypad(View view) {
        content.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(content, InputMethodManager.SHOW_IMPLICIT);
        content.setSelection(content.getText().toString().length());
    }

}
