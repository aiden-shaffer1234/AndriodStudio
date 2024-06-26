package com.zybooks.studyhelper;

import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;
import android.widget.EditText;
import com.zybooks.studyhelper.model.Question;
import com.zybooks.studyhelper.viewmodel.QuestionDetailViewModel;

public class QuestionEditActivity extends AppCompatActivity {

    public static final String EXTRA_QUESTION_ID = "com.zybooks.studyhelper.question_id";
    public static final String EXTRA_SUBJECT_ID = "com.zybooks.studyhelper.subject_id";

    private EditText mQuestionEditText;
    private EditText mAnswerEditText;
    private long mQuestionId;
    private Question mQuestion;
    private QuestionDetailViewModel mQuestionDetailViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question_edit);
        mQuestionDetailViewModel = new ViewModelProvider(this).get(QuestionDetailViewModel.class);

        mQuestionEditText = findViewById(R.id.question_edit_text);
        mAnswerEditText = findViewById(R.id.answer_edit_text);

        findViewById(R.id.save_button).setOnClickListener(view -> saveButtonClick());

        // Get question ID from QuestionActivity
        Intent intent = getIntent();
        mQuestionId = intent.getLongExtra(EXTRA_QUESTION_ID, -1);

        if (mQuestionId == -1) {
            // Add new question
            mQuestion = new Question();
            mQuestion.setSubjectId(intent.getLongExtra(EXTRA_SUBJECT_ID, 0));

            setTitle(R.string.add_question);
        }
        else {
            // Display existing question from ViewModel
            mQuestionDetailViewModel.loadQuestion(mQuestionId);
            mQuestionDetailViewModel.questionLiveData.observe(this, question -> {
                mQuestion = question;
                updateUI();
            });

            setTitle(R.string.edit_question);
        }
    }

    private void updateUI() {
        mQuestionEditText.setText(mQuestion.getText());
        mAnswerEditText.setText(mQuestion.getAnswer());
    }

    private void saveButtonClick() {
        mQuestion.setText(mQuestionEditText.getText().toString());
        mQuestion.setAnswer(mAnswerEditText.getText().toString());

        if (mQuestionId == -1) {
            mQuestionDetailViewModel.addQuestion(mQuestion);
        }
        else {
            mQuestionDetailViewModel.updateQuestion(mQuestion);
        }

        // Send back OK result
        setResult(RESULT_OK);
        finish();
    }
}
