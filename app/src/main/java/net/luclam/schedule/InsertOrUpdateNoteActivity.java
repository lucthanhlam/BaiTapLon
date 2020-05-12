package net.luclam.schedule;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;

import java.util.Calendar;

public class InsertOrUpdateNoteActivity extends AppCompatActivity implements View.OnClickListener{
    private static String TAG = "InsertOrUpdateNoteActivity";
    public static final String NOTE_OLD_RETURN = "note_old_return";
    public static final String NOTE_RETURN = "note_return";
    public static final int RESULT_INSERT = 1;
    public static final int RESULT_UPDATE = 2;
    private EditText edtTitle, edtDescription, edtDate, edtTime;
    private DatePickerDialog datePickerDialog;
    private TimePickerDialog timePickerDialog;
    private DatabaseHelper databaseHelper;
    private Note noteNeedUpdate;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_insert_or_update_note);

        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        databaseHelper = DatabaseHelper.getInstance();
        edtTitle = findViewById(R.id.edtTitle);
        edtDescription = findViewById(R.id.edtDescription);
        edtDate = findViewById(R.id.edtDate);
        edtTime = findViewById(R.id.edtTime);

        edtDate.setKeyListener(null);
        edtDate.setOnClickListener(this);
        edtTime.setOnClickListener(this);

        edtTitle.requestFocus();

        buildDatePickerDialog();
        buildTimePickerDialog();

        findViewById(R.id.btnConfirm).setOnClickListener(this);

        noteNeedUpdate = (Note) getIntent().getSerializableExtra(NoteAdapter.NOTE_NEED_UPDATE);
        if (noteNeedUpdate == null) {
            getSupportActionBar().setTitle(R.string.insert_note);
        } else {
            getSupportActionBar().setTitle(R.string.update_note);
            edtTitle.setText(noteNeedUpdate.getTitle());
            edtDescription.setText(noteNeedUpdate.getDescription());
            edtTime.setText(noteNeedUpdate.getTime());
            edtDate.setText(noteNeedUpdate.getDate());
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            setResult(Activity.RESULT_CANCELED);
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private void buildTimePickerDialog() {
        int hourOfDay = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        int minute = Calendar.getInstance().get(Calendar.MINUTE);
        timePickerDialog = new TimePickerDialog(InsertOrUpdateNoteActivity.this,
                AlertDialog.THEME_HOLO_LIGHT,
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int hour, int minute) {
                        edtTime.setText((hour < 10 ? "0" + hour : hour) + ":" + (minute < 10 ? "0" + minute : minute));
                    }
                },
                hourOfDay,
                minute,
                true
        );
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnConfirm:
                if (noteNeedUpdate == null)
                    insertNote();
                else
                    updateNote();
                break;
            case R.id.edtDate:
                datePickerDialog.show();
                break;
            case R.id.edtTime:
                timePickerDialog.show();
                break;
        }
    }

    private void insertNote() {
        String title = getTextFormEditText(edtTitle);
        String description = getTextFormEditText(edtDescription);
        String date = getTextFormEditText(edtDate);
        String time = getTextFormEditText(edtTime);

        if (title.isEmpty()) {
            edtTitle.setError(getString(R.string.title_cannot_be_empty));
            return;
        }
        if (date.isEmpty()) {
            edtDate.setError(getString(R.string.date_cannot_be_empty));
            return;
        } else {
            Note note = new Note(title, description, time, date);
            databaseHelper.insertNote(note);
            AppHelper.showToastSuccess(InsertOrUpdateNoteActivity.this, R.string.insert_note_success);
            setResult(RESULT_INSERT, new Intent().putExtra(NOTE_RETURN, note));
            finish();
        }
    }

    private void updateNote() {
        String title = getTextFormEditText(edtTitle);
        String description = getTextFormEditText(edtDescription);
        String date = getTextFormEditText(edtDate);
        String time = getTextFormEditText(edtTime);

        if (title.isEmpty()) {
            edtTitle.setError(getString(R.string.title_cannot_be_empty));
            return;
        }
        if (date.isEmpty()) {
            edtDate.setError(getString(R.string.date_cannot_be_empty));
            return;
        } else {
            Note note = new Note(noteNeedUpdate.getUid(), title, description, time, date);
            databaseHelper.updateNote(note);
            AppHelper.showToastSuccess(InsertOrUpdateNoteActivity.this, R.string.update_note_success);
            Intent intent = new Intent();
            intent.putExtra(NOTE_RETURN, note);
            intent.putExtra(NOTE_OLD_RETURN, noteNeedUpdate);
            setResult(RESULT_UPDATE, intent);
            finish();
        }
    }


    private void buildDatePickerDialog() {
        int year, month, dayOfMonth;
        year = Calendar.getInstance().get(Calendar.YEAR);
        month = Calendar.getInstance().get(Calendar.MONTH);
        dayOfMonth = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
        datePickerDialog = new DatePickerDialog(InsertOrUpdateNoteActivity.this,
                AlertDialog.THEME_HOLO_LIGHT,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int year, int month, int date) {
                        month = month + 1;
                        edtDate.setText(AppHelper.toStringDate(AppHelper.convertStringToDate(year + "-" + month + "-" + date)));
                    }
                },
                year,
                month,
                dayOfMonth
        );
    }

    public String getTextFormEditText(EditText editText) {
        return editText.getText().toString().trim();
    }

    @Override
    public void onBackPressed() {
        setResult(Activity.RESULT_CANCELED);
        super.onBackPressed();
    }
}
