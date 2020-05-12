package net.luclam.schedule;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DatabaseHelper {
    private static DatabaseHelper instance;
    private FirebaseDatabase database;
    private DatabaseHelper(){
        database = FirebaseDatabase.getInstance();
        database.setPersistenceEnabled(true);
    }

    public static DatabaseHelper getInstance(){
        if (instance == null) {
            instance = new DatabaseHelper();
        }
        return instance;
    }

    public DatabaseReference reference() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        return database.getReference(currentUser.getUid());
    }

    public void insertNote(Note note) {
        note.setUid(reference().push().getKey());
        reference().child(note.getUid()).setValue(note);
    }

    public void updateNote(Note note) {
        reference().child(note.getUid()).setValue(note);
    }

    public void deleteNote(Note note) {
        reference().child(note.getUid()).setValue(null);
    }

    public Query getQueryByDate(Date date) {
        String s = new SimpleDateFormat("yyyy-MM-dd").format(date);
        return reference().orderByChild("date").equalTo(s);
    }
}
