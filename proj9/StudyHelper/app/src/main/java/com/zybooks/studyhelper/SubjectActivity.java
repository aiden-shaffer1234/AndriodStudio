package com.zybooks.studyhelper;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.zybooks.studyhelper.model.Subject;
import com.zybooks.studyhelper.viewmodel.SubjectListViewModel;
import java.util.List;
import android.view.ActionMode;
import android.view.MenuInflater;
import android.content.SharedPreferences;
import androidx.preference.PreferenceManager;
import java.util.Comparator;
public class SubjectActivity extends AppCompatActivity
        implements SubjectDialogFragment.OnSubjectEnteredListener {

    private SubjectAdapter mSubjectAdapter;
    public enum SubjectSortOrder {
        ALPHABETIC, NEW_FIRST, OLD_FIRST
    }
    private RecyclerView mRecyclerView;
    private int[] mSubjectColors;
    private SubjectListViewModel mSubjectListViewModel;

    private Boolean mLoadSubjectList = true;
    private Subject mSelectedSubject;
    private int mSelectedSubjectPosition = RecyclerView.NO_POSITION;
    private ActionMode mActionMode = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subject);

        mSubjectListViewModel = new ViewModelProvider(this).get(SubjectListViewModel.class);

        mSubjectListViewModel = new SubjectListViewModel(getApplication());

        mSubjectColors = getResources().getIntArray(R.array.subjectColors);

        findViewById(R.id.add_subject_button).setOnClickListener(view -> addSubjectClick());

        // Create 2 grid layout columns
        mRecyclerView = findViewById(R.id.subject_recycler_view);
        RecyclerView.LayoutManager gridLayoutManager =
                new GridLayoutManager(getApplicationContext(), 2);
        mRecyclerView.setLayoutManager(gridLayoutManager);


        // Call updateUI() when the subject list changes
        mSubjectListViewModel.getSubjects().observe(this, subjects -> {
            if (mLoadSubjectList) {
                updateUI(subjects);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.subject_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        if (item.getItemId() == R.id.settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        } else if (item.getItemId() == R.id.import_questions) {
            Intent intent = new Intent(this, ImportActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void updateUI(List<Subject> subjectList) {
        mSubjectAdapter = new SubjectAdapter(subjectList);
        mSubjectAdapter.setSortOrder(getSettingsSortOrder());
        mRecyclerView.setAdapter(mSubjectAdapter);
    }

    private SubjectSortOrder getSettingsSortOrder() {

        // Set sort order from settings
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        String sortOrderPref = sharedPrefs.getString("subject_order", "alpha");
        switch (sortOrderPref) {
            case "alpha": return SubjectSortOrder.ALPHABETIC;
            case "new_first": return SubjectSortOrder.NEW_FIRST;
            default: return SubjectSortOrder.OLD_FIRST;
        }
    }

    @Override
    public void onSubjectEntered(String subjectText) {
        if (subjectText.length() > 0) {
            Subject subject = new Subject(subjectText);
            // Stop updateUI() from being called
            mLoadSubjectList = false;

            mSubjectListViewModel.addSubject(subject);

            // Add subject to RecyclerView
            mSubjectAdapter.addSubject(subject);

            Toast.makeText(this, "Added " + subjectText, Toast.LENGTH_SHORT).show();
        }
    }

    private void addSubjectClick() {
        SubjectDialogFragment dialog = new SubjectDialogFragment();
        dialog.show(getSupportFragmentManager(), "subjectDialog");
    }

    private class SubjectHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener, View.OnLongClickListener {

        private Subject mSubject;
        private final TextView mSubjectTextView;

        public SubjectHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.recycler_view_items, parent, false));
            itemView.setOnClickListener(this);
            mSubjectTextView = itemView.findViewById(R.id.subject_text_view);
            itemView.setOnLongClickListener(this);
        }

        public void bind(Subject subject, int position) {
            mSubject = subject;
            mSubjectTextView.setText(subject.getText());

            if (mSelectedSubjectPosition == position) {
                // Make selected subject stand out
                mSubjectTextView.setBackgroundColor(Color.RED);
            }
            else {
                // Make the background color dependent on the length of the subject string
                int colorIndex = subject.getText().length() % mSubjectColors.length;
                mSubjectTextView.setBackgroundColor(mSubjectColors[colorIndex]);
            }
        }

        @Override
        public void onClick(View view) {
            // Start QuestionActivity with the selected subject
            Intent intent = new Intent(SubjectActivity.this, QuestionActivity.class);
            intent.putExtra(QuestionActivity.EXTRA_SUBJECT_ID, mSubject.getId());
            intent.putExtra(QuestionActivity.EXTRA_SUBJECT_TEXT, mSubject.getText());

            startActivity(intent);
        }
        @Override
        public boolean onLongClick(View view) {
            if (mActionMode != null) {
                return false;
            }

            mSelectedSubject = mSubject;
            mSelectedSubjectPosition = getAbsoluteAdapterPosition();

            // Re-bind the selected item
            mSubjectAdapter.notifyItemChanged(mSelectedSubjectPosition);

            // Show the CAB
            mActionMode = SubjectActivity.this.startActionMode(mActionModeCallback);

            return true;
        }
    }

    private class SubjectAdapter extends RecyclerView.Adapter<SubjectHolder> {

        private final List<Subject> mSubjectList;

        public SubjectAdapter(List<Subject> subjects) {
            mSubjectList = subjects;
        }

        @NonNull
        @Override
        public SubjectHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getApplicationContext());
            return new SubjectHolder(layoutInflater, parent);
        }

        @Override
        public void onBindViewHolder(SubjectHolder holder, int position){
            holder.bind(mSubjectList.get(position), position);
        }

        @Override
        public int getItemCount() {
            return mSubjectList.size();
        }

        public void addSubject(Subject subject) {

            // Add the new subject at the beginning of the list
            mSubjectList.add(0, subject);

            // Notify the adapter that item was added to the beginning of the list
            notifyItemInserted(0);

            // Scroll to the top
            mRecyclerView.scrollToPosition(0);
        }
        public void removeSubject(Subject subject) {

            // Find subject in the list
            int index = mSubjectList.indexOf(subject);
            if (index >= 0) {

                // Remove the subject
                mSubjectList.remove(index);

                // Notify adapter of subject removal
                notifyItemRemoved(index);
            }
        }
        public void setSortOrder(SubjectSortOrder sortOrder) {
            switch (sortOrder) {
                case ALPHABETIC:
                    mSubjectList.sort(Comparator.comparing(Subject::getText));
                    break;
                case NEW_FIRST:
                    mSubjectList.sort(Comparator.comparing(Subject::getUpdateTime).reversed());
                    break;
                default:
                    mSubjectList.sort(Comparator.comparing(Subject::getUpdateTime));
            }
        }
    }

    private final ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            // Provide context menu for CAB
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.context_menu, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            // Process action item selection
            if (item.getItemId() == R.id.delete) {
                // Stop updateUI() from being called
                mLoadSubjectList = false;

                // Delete from ViewModel
                mSubjectListViewModel.deleteSubject(mSelectedSubject);

                // Remove from RecyclerView
                mSubjectAdapter.removeSubject(mSelectedSubject);

                // Close the CAB
                mode.finish();
                return true;
            }

            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            mActionMode = null;

            // CAB closing, need to deselect item if not deleted
            mSubjectAdapter.notifyItemChanged(mSelectedSubjectPosition);
            mSelectedSubjectPosition = RecyclerView.NO_POSITION;
        }
    };
}