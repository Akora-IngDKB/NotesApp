package com.akoraingdkb.notesapp;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.support.v7.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class MainActivity extends AppCompatActivity implements Constants {
    private NoteDatabase noteDatabase;
    private SharedPreferences sharedPreferences;
    private RecyclerView recyclerView;
    private RecycleAdapter recyclerAdapter;
    private TextView noNoteText;
    private SearchView searchView;

    private List<Note> notes;
    private static Note deletedNote = new Note();
    private static String deletedNoteContent = null;
    private static int closeAppTracker = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sharedPreferences = getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE);
        boolean usingNightTheme = sharedPreferences.getBoolean(THEME_KEY, true);

        if (usingNightTheme) {
            setTheme(R.style.NightTheme);
            setContentView(R.layout.activity_main);
        } else {
            setTheme(R.style.DayTheme);
            setContentView(R.layout.activity_main_day);
        }

        // Set default preference values
        PreferenceManager.setDefaultValues(this, R.xml.preference, false);

        // Initialize the shared preferences object
        sharedPreferences = getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE);

        // Initialize the object of the NoteDatabase class
        noteDatabase = new NoteDatabase(this);

        // Obtain a reference to the recycle view and text view
        recyclerView = (RecyclerView) findViewById(R.id.recycle_view);
        noNoteText = (TextView) findViewById(R.id.text_no_existing_notes);

        // Use this setting to improve performance if you know that changes in content do not change the layout size
        recyclerView.setHasFixedSize(true);

        // Use a linear layout manager
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        if (checkForNotes()) {
            noNoteText.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            addAdapterToRecyclerView();
        } else {
            noNoteText.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        }

        // Create an object of the FAB and initialize it
        FloatingActionButton fabAddNote = (FloatingActionButton) findViewById(R.id.fab_add);
        fabAddNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, NewNoteActivity.class);
                startActivity(intent);
            }
        });
    }

    /**
     * This method check the database for existing notes and add them to the recycler view
     *
     * @return This method returns 'true' if notes are found and 'false' if otherwise.
     */
    private boolean checkForNotes() {
        // Get all items in the database as a list
        List<Note> notes = noteDatabase.getAllNotes();

        return !notes.isEmpty();
    }

    /**
     * This method gets all items from the database and adds them to the recyclerAdapter
     */
    private void addAdapterToRecyclerView() {
        // Get all items in the database as a list
        notes = noteDatabase.getAllNotes();

        // Specify an adapter
        recyclerAdapter = new RecycleAdapter(this, notes);
        recyclerView.setAdapter(recyclerAdapter);

        // Attach the item helper to the recycle view
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }

    ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
        @Override
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
            if (direction == ItemTouchHelper.RIGHT) {
                EditNoteActivity editNoteActivity = new EditNoteActivity();
                final int position = viewHolder.getAdapterPosition();       // Used by RV adapter to delete and restore
                deletedNote = notes.get(position);      // A Note object of the deleted note

                // Save deleted note content to a temporal string variable
                //deletedNoteContent = new EditNoteActivity().readNoteFromStorage(deletedNote.getTitle());

                if (notes.size() > 0) {

                    int i = noteDatabase.deleteSingleNote(String.valueOf(sharedPreferences.getInt(deletedNote.getTitle(), 0)));

                    if (i > 0) {
                        recyclerAdapter.deleteItem(position);
                        if (recyclerAdapter.getItemCount() == 0)
                            noNoteText.setVisibility(View.VISIBLE);

                        boolean status = editNoteActivity.deleteSave(deletedNote.getTitle());

                        Snackbar.make(findViewById(R.id.my_coordinator), R.string.note_deleted, Snackbar.LENGTH_LONG).
                                setAction(R.string.snackbar_undo, new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        // Add restored note to database
                                        int newRowId = noteDatabase.addNewNote(deletedNote);
                                        // Add restored note to recycler view
                                        recyclerAdapter.restoreItem(position, deletedNote);
                                        // Retrieve deleted note content from temporal string variable and save to disk
                                        //new EditNoteActivity().saveNoteToStorage(deletedNote.getTitle(), deletedNoteContent);
                                        // Add to shared preference file
                                        sharedPreferences.edit().putInt(deletedNote.getTitle(), newRowId).apply();

                                        if (recyclerAdapter.getItemCount() != 0)
                                            noNoteText.setVisibility(View.GONE);
                                    }
                                }).setActionTextColor(Color.YELLOW).show();

                        if (noteDatabase.getAllNotes().size() == 0)
                            noNoteText.setVisibility(View.VISIBLE);
                    }
                }
            }
        }
    };

    @Override
    protected void onPause() {
        super.onPause();
        closeAppTracker = 1;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (checkForNotes()) {
            noNoteText.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            addAdapterToRecyclerView();
        } else {
            noNoteText.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onBackPressed() {
        if (!searchView.isIconified()) {
            searchView.setIconified(true);
        } else if (closeAppTracker < 2) {
            Toast.makeText(this, R.string.toast_press_again, Toast.LENGTH_SHORT).show();
            closeAppTracker++;
        } else finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        MenuItem searchItem = menu.findItem(R.id.id_search);

        //searchView = (SearchView) searchItem.getActionView();
        searchView = (SearchView) MenuItemCompat.getActionView(searchItem);

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);

        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setMaxWidth(Integer.MAX_VALUE);
        searchView.setQueryHint(getString(R.string.search_hint));

        SearchView.SearchAutoComplete searchAutoComplete = searchView.findViewById(android.support.v7.appcompat.R.id.search_src_text);
        searchAutoComplete.setHintTextColor(Color.WHITE);
        searchAutoComplete.setTextSize(18);

        // Listening to search query text change
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // Filter recycler view when query submitted
                recyclerAdapter.getFilter().filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // Filter recycler view when text is changed
                recyclerAdapter.getFilter().filter(newText);
                return false;
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.id_search:
                return true;

            case R.id.id_share:
                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);
                shareIntent.putExtra(Intent.EXTRA_TEXT,
                        "Hey! Checkout this awesome app for keeping notes faster and easier. Contact the developer via " +
                                "https://api.whatsapp.com/send?phone=233578337689 " + "for the apk file.");
                shareIntent.setType("text/plain");

                if (shareIntent.resolveActivity(getPackageManager()) != null)
                    startActivity(Intent.createChooser(shareIntent, "Share app via:"));

                break;

            case R.id.id_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                break;

            case R.id.id_help_feedback:
                startActivity(new Intent(this, HelpAndFeedback.class));
                break;

            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }
}
