package com.zybooks.studyhelper;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import android.app.Activity;
import android.widget.Toast;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.google.android.material.snackbar.Snackbar;

import com.zybooks.studyhelper.model.Question;
import com.zybooks.studyhelper.model.Subject;
import com.zybooks.studyhelper.viewmodel.QuestionListViewModel;
import java.util.List;

public class QuestionActivity extends AppCompatActivity {

    public static final String EXTRA_SUBJECT_ID = "com.zybooks.studyhelper.subject_id";
    public static final String EXTRA_SUBJECT_TEXT  = "com.zybooks.studyhelper.subject_text";

    private QuestionListViewModel mQuestionListViewModel;
    private Subject mSubject;
    private List<Question> mQuestionList;
    private TextView mAnswerLabelTextView;
    private TextView mAnswerTextView;
    private Button mAnswerButton;
    private TextView mQuestionTextView;
    private ViewGroup mShowQuestionLayout;
    private ViewGroup mNoQuestionLayout;
    private int mCurrentQuestionIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question);

        mQuestionTextView = findViewById(R.id.question_text_view);
        mAnswerLabelTextView = findViewById(R.id.answer_label_text_view);
        mAnswerTextView = findViewById(R.id.answer_text_view);
        mAnswerButton = findViewById(R.id.answer_button);
        mShowQuestionLayout = findViewById(R.id.show_question_layout);
        mNoQuestionLayout = findViewById(R.id.no_question_layout);

        // Add click callbacks
        mAnswerButton.setOnClickListener(view -> toggleAnswerVisibility());
        findViewById(R.id.add_question_button).setOnClickListener(view -> addQuestion());

        // SubjectActivity should provide the subject ID and text
        Intent intent = getIntent();
        long subjectId = intent.getLongExtra(EXTRA_SUBJECT_ID, 0);
        String subjectText = intent.getStringExtra(EXTRA_SUBJECT_TEXT);
        mSubject = new Subject(subjectText);
        mSubject.setId(subjectId);

        // Get all questions for this subject
        mQuestionListViewModel = new ViewModelProvider(this).get(QuestionListViewModel.class);

        mQuestionListViewModel.loadQuestions(subjectId);
        mQuestionListViewModel.questionListLiveData.observe(this, questions -> {
            mQuestionList = questions;
            updateUI();
        });
    }

    private final ActivityResultLauncher<Intent> mAddQuestionResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {

                    // Display the added question, which will appear at end of list
                    mCurrentQuestionIndex = mQuestionList.size();
                    Toast.makeText(QuestionActivity.this, R.string.question_added, Toast.LENGTH_SHORT).show();
                }
            });

    private void updateUI() {
        showQuestion(mCurrentQuestionIndex);

        if (mQuestionList.isEmpty()) {
            updateAppBarTitle();
            displayQuestion(false);
        } else {
            displayQuestion(true);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.question_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        //  Determine which app bar item was chosen
        if (item.getItemId() == R.id.previous) {
            showQuestion(mCurrentQuestionIndex - 1);
            return true;
        }
        else if (item.getItemId() == R.id.next) {
            showQuestion(mCurrentQuestionIndex + 1);
            return true;
        }
        else if (item.getItemId() == R.id.add) {
            addQuestion();
            return true;
        }
        else if (item.getItemId() == R.id.edit) {
            editQuestion();
            return true;
        }
        else if (item.getItemId() == R.id.delete) {
            deleteQuestion();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void displayQuestion(boolean display) {
        if (display) {
            mShowQuestionLayout.setVisibility(View.VISIBLE);
            mNoQuestionLayout.setVisibility(View.GONE);
        }
        else {
            mShowQuestionLayout.setVisibility(View.GONE);
            mNoQuestionLayout.setVisibility(View.VISIBLE);
        }
    }

    private void updateAppBarTitle() {

        // Display subject and number of questions in app bar
        String title = getResources().getString(R.string.question_number,
                mSubject.getText(), mCurrentQuestionIndex + 1, mQuestionList.size());
        setTitle(title);
    }

    private void addQuestion() {
        Intent intent = new Intent(this, QuestionEditActivity.class);
        intent.putExtra(QuestionEditActivity.EXTRA_SUBJECT_ID, mSubject.getId());
        mAddQuestionResultLauncher.launch(intent);
    }

    private final ActivityResultLauncher<Intent> mEditQuestionResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Toast.makeText(QuestionActivity.this, R.string.question_updated, Toast.LENGTH_SHORT).show();
                }
            });

    private void editQuestion() {
        if (mCurrentQuestionIndex >= 0) {
            Intent intent = new Intent(this, QuestionEditActivity.class);
            long questionId = mQuestionList.get(mCurrentQuestionIndex).getId();
            intent.putExtra(QuestionEditActivity.EXTRA_QUESTION_ID, questionId);
            mEditQuestionResultLauncher.launch(intent);
        }
    }

    private void deleteQuestion() {
        if (mCurrentQuestionIndex >= 0) {
            Question question = mQuestionList.get(mCurrentQuestionIndex);
            mQuestionListViewModel.deleteQuestion(question);

            // Show delete message with Undo button
            Snackbar snackbar = Snackbar.make(findViewById(R.id.coordinator_layout),
                    R.string.question_deleted, Snackbar.LENGTH_LONG);
            snackbar.setAction(R.string.undo, view -> {
                // Add question back
                mQuestionListViewModel.addQuestion(question);
            });
            snackbar.show();
        }
    }

    private void showQuestion(int questionIndex) {

        // Show question at the given index
        if (mQuestionList.size() > 0) {
            if (questionIndex < 0) {
                questionIndex = mQuestionList.size() - 1;
            }
            else if (questionIndex >= mQuestionList.size()) {
                questionIndex = 0;
            }

            mCurrentQuestionIndex = questionIndex;
            updateAppBarTitle();

            Question question = mQuestionList.get(mCurrentQuestionIndex);
            mQuestionTextView.setText(question.getText());
            mAnswerTextView.setText(question.getAnswer());
        }
        else {
            // No questions yet
            mCurrentQuestionIndex = -1;
        }
    }

    private void toggleAnswerVisibility() {
        if (mAnswerTextView.getVisibility() == View.VISIBLE) {
            mAnswerButton.setText(R.string.show_answer);
            mAnswerTextView.setVisibility(View.INVISIBLE);
            mAnswerLabelTextView.setVisibility(View.INVISIBLE);
        }
        else {
            mAnswerButton.setText(R.string.hide_answer);
            mAnswerTextView.setVisibility(View.VISIBLE);
            mAnswerLabelTextView.setVisibility(View.VISIBLE);
        }
    }
}