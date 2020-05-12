package net.luclam.schedule;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.github.sundeepk.compactcalendarview.CompactCalendarView;
import com.github.sundeepk.compactcalendarview.domain.Event;

import java.util.List;

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.ViewHolder> {
    public static String NOTE_NEED_UPDATE = "note_update";
    private Context context;
    private List<Note> notes;
    private DatabaseHelper databaseHelper;
    private CompactCalendarView calendarView;
    public NoteAdapter(Context context, CompactCalendarView calendarView) {
        this.context = context;
        this.calendarView = calendarView;
        databaseHelper = DatabaseHelper.getInstance();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_note, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        final Note note = notes.get(position);
        holder.tvTitle.setText(note.getTitle());
        holder.tvTime.setText(context.getString(R.string.time_with_space) + note.getTime());
        holder.tvDescription.setText(note.getDescription());
        if (note.getTime().isEmpty() && !note.getDescription().isEmpty()) {
            holder.tvTime.setVisibility(View.GONE);
            holder.tvDescription.setVisibility(View.VISIBLE);
        } else if (!note.getTime().isEmpty() && note.getDescription().isEmpty()) {
            holder.tvTime.setVisibility(View.VISIBLE);
            holder.tvDescription.setVisibility(View.GONE);
        } else if (note.getTime().isEmpty() && note.getDescription().isEmpty()) {
            holder.tvTime.setVisibility(View.GONE);
            holder.tvDescription.setVisibility(View.VISIBLE);
        } else {
            holder.tvTime.setVisibility(View.VISIBLE);
            holder.tvDescription.setVisibility(View.VISIBLE);
        }
        holder.tvOption.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                buildPopupMenuOption(holder.tvOption, note);
            }
        });
    }
    private void buildPopupMenuOption(View anchor, final Note note) {
        PopupMenu popupMenu = new PopupMenu(context, anchor);
        popupMenu.inflate(R.menu.option_menu);
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getItemId() == R.id.itemMenuEdit) {
                    Activity activity = (Activity) context;
                    Intent intent = new Intent(activity, InsertOrUpdateNoteActivity.class);
                    intent.putExtra(NOTE_NEED_UPDATE, note);
                    activity.startActivityForResult(intent, MainActivity.RC_INSERT_OR_UPDATE);
                } else if (item.getItemId() == R.id.itemMenuDelete) {
                    AlertDialog alertDialog = getBuilderDialogConfirmDelete(note).create();
                    alertDialog.show();
                }
                return true;
            }
        });
        popupMenu.show();
    }

    private AlertDialog.Builder getBuilderDialogConfirmDelete(final Note note) {
        return new AlertDialog.Builder(context)
                .setTitle(R.string.delete_note)
                .setMessage(R.string.do_you_want_to_delete_note)
                .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Event eventNeedDelete = new Event(AppHelper.getColorPurple(), AppHelper.convertEpochTimeFromDate(note.getDate()));
                        calendarView.removeEvent(eventNeedDelete);
                        notes.remove(note);
                        notifyDataSetChanged();
                        databaseHelper.deleteNote(note);
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
    }
    @Override
    public int getItemCount() {
        return notes == null ? 0 : notes.size();
    }

    public void setData(List<Note> notes) {
        this.notes = notes;
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView tvTitle, tvDescription, tvTime, tvOption;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvOption = itemView.findViewById(R.id.tvOption);
        }
    }
}
