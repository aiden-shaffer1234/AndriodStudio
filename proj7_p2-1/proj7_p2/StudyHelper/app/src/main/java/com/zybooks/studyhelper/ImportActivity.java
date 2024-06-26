package com.zybooks.studyhelper;

import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.zybooks.studyhelper.model.Subject;
import com.zybooks.studyhelper.viewmodel.ImportViewModel;

public class ImportActivity extends AppCompatActivity {

    private LinearLayout mSubjectLayoutContainer;
    private ProgressBar mLoadingProgressBar;
    private ImportViewModel mImportViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_import);

        mSubjectLayoutContainer = findViewById(R.id.subject_layout);

        findViewById(R.id.import_button).setOnClickListener(view -> importButtonClick());

        // Show progress bar
        mLoadingProgressBar = findViewById(R.id.loading_progress_bar);
        mLoadingProgressBar.setVisibility(View.VISIBLE);

        mImportViewModel = new ViewModelProvider(this).get(ImportViewModel.class);

        // Fetch subjects from web API
        mImportViewModel.fetchSubjects();
        mImportViewModel.fetchedSubjectList.observe(this, subjectList -> {

            // Hide progress bar
            mLoadingProgressBar.setVisibility(View.GONE);

            // Create a checkbox for each subject
            for (Subject subject: subjectList) {
                CheckBox checkBox = new CheckBox(getApplicationContext());
                checkBox.setTextSize(24);
                checkBox.setText(subject.getText());
                checkBox.setTag(subject);
                mSubjectLayoutContainer.addView(checkBox);
            }
        });

        // subjectName changes once all questions are imported
        mImportViewModel.importedSubject.observe(this, subject ->
                Toast.makeText(getApplicationContext(),subject + " imported successfully",
                        Toast.LENGTH_SHORT).show());
    }

    private void importButtonClick() {

        // Add any checked subjects
        int numCheckBoxes = mSubjectLayoutContainer.getChildCount();
        for (int i = 0; i < numCheckBoxes; i++) {
            CheckBox checkBox = (CheckBox) mSubjectLayoutContainer.getChildAt(i);
            if (checkBox.isChecked()) {
                Subject subject = (Subject) checkBox.getTag();
                mImportViewModel.addSubject(subject);
            }
        }
    }
}