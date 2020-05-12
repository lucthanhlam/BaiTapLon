package net.luclam.schedule;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import com.github.sundeepk.compactcalendarview.CompactCalendarView;
import com.github.sundeepk.compactcalendarview.domain.Event;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity implements CompactCalendarView.CompactCalendarViewListener {
    public static final int RC_INSERT_OR_UPDATE = 1111;
    private static String TAG = "MainActivity";
    private TextView tvMonthOfYear;
    private CompactCalendarView calendarView;
    private RecyclerView recyclerView;
    private NoteAdapter noteAdapter;
    private Date dateClicked;
    private DatabaseHelper databaseHelper;
    private AlertDialog dialogConfirmLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        databaseHelper = DatabaseHelper.getInstance();

        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

        buildCalendarView();
        buildDialogConfirmLogout();

        addEventsNote();

        noteAdapter = new NoteAdapter(this, calendarView);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setAdapter(noteAdapter);

        dateClicked = Calendar.getInstance().getTime();
        getDataForAdapterByDate();
    }
    private void buildDialogConfirmLogout() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this)
                .setTitle(R.string.logout)
                .setMessage(R.string.do_you_want_to_logout)
                .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        FirebaseAuth.getInstance().signOut();
                        startActivity(new Intent(MainActivity.this, LoginActivity.class));
                        finish();
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
        dialogConfirmLogout = builder.create();
    }
    private void addEventsNote() {
        databaseHelper.reference()
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            List<Event> events = new ArrayList<>();
                            for (DataSnapshot noteSnapshot : dataSnapshot.getChildren()) {
                                Note note = noteSnapshot.getValue(Note.class);
                                Date date = new Date();
                                try {
                                    date = new SimpleDateFormat("yyyy-MM-dd").parse(note.getDate());
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                                long millis = date.getTime();
                                events.add(new Event(AppHelper.getColorPurple(), millis));
                            }
                            calendarView.addEvents(events);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.d(TAG, "Add events note error: " + databaseError.getMessage());
                    }
                });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menuItemInsertNote:
                startActivityForResult(new Intent(MainActivity.this, InsertOrUpdateNoteActivity.class), RC_INSERT_OR_UPDATE);
                break;
            case R.id.menuItemLogout:
                dialogConfirmLogout.show();
                break;
            default:
                break;
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_INSERT_OR_UPDATE) {
            if (resultCode == InsertOrUpdateNoteActivity.RESULT_INSERT) {
                Note note = (Note) data.getSerializableExtra(InsertOrUpdateNoteActivity.NOTE_RETURN);
                String date = note.getDate();
                calendarView.addEvent(new Event(AppHelper.getColorPurple(), AppHelper.convertEpochTimeFromDate(date)));
                if (AppHelper.toStringDate(dateClicked).equals(date)) {
                    getDataForAdapterByDate();
                }
            } else if (resultCode == InsertOrUpdateNoteActivity.RESULT_UPDATE) {
                Note noteNew = (Note) data.getSerializableExtra(InsertOrUpdateNoteActivity.NOTE_RETURN);
                Note noteOld = (Note) data.getSerializableExtra(InsertOrUpdateNoteActivity.NOTE_OLD_RETURN);
                if (!noteNew.getDate().equals(noteOld.getDate())) {
                    Event eventNew = new Event(AppHelper.getColorPurple(), AppHelper.convertEpochTimeFromDate(noteNew.getDate()));
                    Event eventOld = new Event(AppHelper.getColorPurple(), AppHelper.convertEpochTimeFromDate(noteOld.getDate()));
                    calendarView.removeEvent(eventOld);
                    calendarView.addEvent(eventNew);
                }
                if (AppHelper.toStringDate(dateClicked).equals(noteOld.getDate())) {
                    getDataForAdapterByDate();
                }
            }
        }
    }

    private void buildCalendarView() {
        tvMonthOfYear = findViewById(R.id.tvMonthOfYear);
        tvMonthOfYear.setText(getString(R.string.month) + new SimpleDateFormat(" MM - yyyy").format(Calendar.getInstance().getTime()));
        calendarView = findViewById(R.id.calendarView);
        calendarView.setFirstDayOfWeek(Calendar.MONDAY);
        calendarView.setDayColumnNames(getResources().getStringArray(R.array.day_column_names));
        calendarView.setListener(this);
        calendarView.shouldDrawIndicatorsBelowSelectedDays(true);
    }

    @Override
    public void onDayClick(Date dateClicked) {
        this.dateClicked = dateClicked;
        getDataForAdapterByDate();
    }

    @Override
    public void onMonthScroll(Date firstDayOfNewMonth) {
        tvMonthOfYear.setText(getString(R.string.month) + new SimpleDateFormat("MM - yyyy").format(firstDayOfNewMonth));
        this.dateClicked = firstDayOfNewMonth;
        getDataForAdapterByDate();
    }

    private void getDataForAdapterByDate() {
        databaseHelper.reference().removeEventListener(valueEventListener);
        databaseHelper.getQueryByDate(dateClicked)
                .addValueEventListener(valueEventListener);
    }

    private ValueEventListener valueEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            if (dataSnapshot.exists()) {
                List<Note> notes = new ArrayList<>();
                for (DataSnapshot noteSnapshot : dataSnapshot.getChildren()) {
                    Note note = noteSnapshot.getValue(Note.class);
                    notes.add(note);
                }
                noteAdapter.setData(notes);
            } else {
                noteAdapter.setData(null);
            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {
            Log.d(TAG, "Get notes by date error: " + databaseError.getMessage());
        }
    };
}
