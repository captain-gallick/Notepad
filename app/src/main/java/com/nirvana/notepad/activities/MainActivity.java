package com.nirvana.notepad.activities;

import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.nirvana.notepad.R;
import com.nirvana.notepad.adapters.NotesAdapter;
import com.nirvana.notepad.helpers.MySharedPreferences;
import com.nirvana.notepad.helpers.NotesDB;
import com.nirvana.notepad.structures.Notes;
import com.nirvana.notepad.structures.UserNotes;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public class MainActivity extends AppCompatActivity {

    Context context;
    NotesDB notesDB;
    public static ArrayList<Integer> multiList;
    RecyclerView recyclerView;
    public static int layoutMode = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        context = this;

        notesDB = new NotesDB(context);

        FloatingActionButton fab = findViewById(R.id.fab);

        multiList = new ArrayList<>();

        layoutMode = MySharedPreferences.getLAYOUT_MODE(context);

        createList();

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context,EditorActivity.class);
                intent.putExtra("serial_no",-1);
                startActivity(intent);
            }
        });

    }

    private void createList(){
        ArrayList<Notes> list = notesDB.getAllNotes();
        updateRecycler(context,list);
    }

    ArrayList<Notes> newList;

    private void updateRecycler(final Context c, ArrayList<Notes> list){
        newList = list;
        recyclerView = findViewById(R.id.note_list_recycler);
        if (newList.size() > 0) {
            findViewById(R.id.ll).setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            recyclerView.setHasFixedSize(true);
            NotesAdapter adapter = new NotesAdapter(c, newList);
            RecyclerView.LayoutManager layoutManager = null;
            if (layoutMode == 1) {
                layoutManager = new LinearLayoutManager(c);
            } else if (layoutMode == 2) {
                layoutManager = new GridLayoutManager(c, 2, RecyclerView.VERTICAL, false);
            }
            recyclerView.setLayoutManager(layoutManager);
            recyclerView.setAdapter(adapter);

            new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
                @Override
                public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                    return false;
                }

                @Override
                public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                    System.out.println("list size when swiped is :: "+newList.size());
                    final int position = viewHolder.getAdapterPosition();
                    final Notes note = newList.get(position);
                    notesDB.deleteNote(note.getSerialNo());
                    createList();
                    Snackbar snackbar = Snackbar.make(recyclerView, "1 note deleted.", Snackbar.LENGTH_LONG);
                    snackbar.setAction("Undo", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            ContentValues values = new ContentValues();
                            values.put(UserNotes.NotesEntry.COLUMN_SN, note.getSerialNo());
                            values.put(UserNotes.NotesEntry.COLUMN_TITLE, note.getTitle());
                            values.put(UserNotes.NotesEntry.COLUMN_CONTENT, note.getContent());
                            values.put(UserNotes.NotesEntry.COLUMN_DATE_TIME, note.getDateTime());
                            notesDB.addNewNote(values);
                            createList();
                        }
                    });
                    snackbar.show();
                }

                @Override
                public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                    super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
                    final ColorDrawable background = new ColorDrawable(context.getResources().getColor(R.color.red));
                    background.setBounds(viewHolder.itemView.getRight() + (int) dX, viewHolder.itemView.getTop(), viewHolder.itemView.getRight(), viewHolder.itemView.getBottom());
                    background.draw(c);
                    Drawable icon = ContextCompat.getDrawable(recyclerView.getContext(), R.drawable.delete);
                    assert icon != null;
                    int iconSize = icon.getIntrinsicHeight();
                    int halfIcon = iconSize / 2;
                    int top = viewHolder.itemView.getTop() + ((viewHolder.itemView.getBottom() - viewHolder.itemView.getTop()) / 2 - halfIcon);
                    int imgLeft = viewHolder.itemView.getRight() - 20 - halfIcon * 2;
                    icon.setBounds(imgLeft, top, viewHolder.itemView.getRight() - 20, top + icon.getIntrinsicHeight());
                    icon.draw(c);
                }
            }).attachToRecyclerView(recyclerView);
        } else {
            recyclerView.setVisibility(View.GONE);
            findViewById(R.id.ll).setVisibility(View.VISIBLE);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu,menu);
        MenuItem menuItem = menu.findItem(R.id.menu_delete_selected);
        MenuItem menuItem2 = menu.findItem(R.id.change_layout);
        MenuItem menuItem3 = menu.findItem(R.id.menu_unselect_all);
        if (multiList.isEmpty()){
            menuItem.setVisible(false);
            menuItem3.setVisible(false);
            if (layoutMode == 1){
                menuItem2.setIcon(R.drawable.grid);
            } else if (layoutMode == 2){
                menuItem2.setIcon(R.drawable.list);
            }
        } else {
            menuItem2.setVisible(false);
            menuItem3.setVisible(true);
            menuItem.setVisible(true);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.menu_mark_all){

            ArrayList<Notes> list = notesDB.getAllNotes();
            for (int i = 0; i < list.size(); i++){
                list.get(i).setSelected(true);
                if (!multiList.contains(list.get(i).getSerialNo())) {
                    multiList.add(list.get(i).getSerialNo());
                }
            }
            updateRecycler(context,list);
            invalidateOptionsMenu();

        } else if (id == R.id.menu_unselect_all){

            multiList.clear();
            ArrayList<Notes> list = notesDB.getAllNotes();
            for (int i = 0; i < list.size(); i++){
                list.get(i).setSelected(false);
            }
            updateRecycler(context,list);
            invalidateOptionsMenu();

        } else if (id == R.id.menu_delete_selected){

            if (multiList.size() > 0) {
                createDialog();
            }

        } else if (id == R.id.change_layout){
            if (layoutMode == 1){
                MySharedPreferences.setLAYOUT_MODE(context,2);
                layoutMode = 2;
                ArrayList<Notes> list = notesDB.getAllNotes();
                updateRecycler(context, list);
            } else if (layoutMode == 2){
                MySharedPreferences.setLAYOUT_MODE(context,1);
                layoutMode = 1;
                ArrayList<Notes> list = notesDB.getAllNotes();
                updateRecycler(context, list);
            }
            invalidateOptionsMenu();
        }

        return super.onOptionsItemSelected(item);
    }

    public void createDialog(){
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:

                        for (int i = 0; i < multiList.size(); i++) {
                            notesDB.deleteNote(multiList.get(i));
                        }
                        multiList.clear();
                        ArrayList<Notes> list = notesDB.getAllNotes();
                        updateRecycler(context,list);
                        invalidateOptionsMenu();

                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        //No button clicked
                        break;
                }
            }
        };
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage("Selected notes will be deleted forever. Are you sure?").setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("No", dialogClickListener).show();
    }

    @Override
    public void onBackPressed() {
        if (multiList.size() > 0){
            multiList.clear();
            ArrayList<Notes> list = notesDB.getAllNotes();
            for (int i = 0; i < list.size(); i++){
                list.get(i).setSelected(false);
            }
            updateRecycler(context,list);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onResume() {
        createList();
        super.onResume();
    }
}
