package com.behrmanhouse.android.geoquiz;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

public class QuizActivity extends AppCompatActivity {
    private static final String TAG = "QuizActivity";
    private static final String KEY_INDEX = "index";
    private static final String KEY_CORRECT_COUNT = "correctCount";
    private static final String KEY_INCORRECT_COUNT = "incorrectCount";
    private static final String KEY_CHEATED_ON_QUESTION = "cheatedOnQuestion";   // Challenge Ch. 5
    private static final int REQUEST_CODE_CHEAT = 0;

    private Button mTrueButton;
    private Button mFalseButton;
    private ImageButton mPrevButton;
    private View mNextButton;   // a Button on landscape, or ImageButton on portrait
    private TextView mQuestionTextView;
    private Button mCheatButton;

    private Question[] mQuestionBank = new Question[] {
            new Question(R.string.question_australia, true),
            new Question(R.string.question_oceans, true),
            new Question(R.string.question_mideast, false),
            new Question(R.string.question_africa, false),
            new Question(R.string.question_americas, true),
            new Question(R.string.question_asia, true)
    };
    private boolean[] mCheatedOnQuestion = new boolean[mQuestionBank.length];   // Challenge Ch. 5    // defaults to false

    private int mCurrentIndex = 0;
    private int mCorrectCount = 0;
    private int mIncorrectCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate(Bundle) called");
        setContentView(R.layout.activity_quiz);

        if (savedInstanceState != null) {
            mCurrentIndex = savedInstanceState.getInt(KEY_INDEX, 0);
            mCorrectCount = savedInstanceState.getInt(KEY_CORRECT_COUNT, 0);
            mIncorrectCount = savedInstanceState.getInt(KEY_INCORRECT_COUNT, 0);
            mCheatedOnQuestion = savedInstanceState.getBooleanArray(KEY_CHEATED_ON_QUESTION);   // Challenge Ch. 5
        }

        mQuestionTextView = (TextView) findViewById(R.id.question_text_view);
        mQuestionTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCurrentIndex = (mCurrentIndex + 1) % mQuestionBank.length;
                updateQuestion();
            }
        });

        mTrueButton = (Button) findViewById(R.id.true_button);
        mTrueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkAnswer(true);
                setAnswerButtonEnabled(false);
            }
        });

        mFalseButton = (Button) findViewById(R.id.false_button);
        mFalseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkAnswer(false);
                setAnswerButtonEnabled(false);
            }
        });

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            mPrevButton = (ImageButton) findViewById(R.id.prev_button);
            mPrevButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mCurrentIndex == 0) {
                        mCurrentIndex = mQuestionBank.length - 1;
                    } else {
                        mCurrentIndex = mCurrentIndex - 1;
                    }
                    updateQuestion();
                }
            });
        }
        mNextButton = findViewById(R.id.next_button);
        mNextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCurrentIndex = (mCurrentIndex + 1) % mQuestionBank.length;
                updateQuestion();
            }
        });

        mCheatButton = (Button) findViewById(R.id.cheat_button);
        mCheatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start CheatActivity
                boolean answerIsTrue = mQuestionBank[mCurrentIndex].isAnswerTrue();
                Intent intent = CheatActivity.newIntent(QuizActivity.this, answerIsTrue);
                startActivityForResult(intent, REQUEST_CODE_CHEAT);
            }
        });

        updateQuestion();
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart() called");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume() called");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause() called");
    }

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        Log.i(TAG, "onSaveInstanceState");
        savedInstanceState.putInt(KEY_INDEX, mCurrentIndex);
        savedInstanceState.putInt(KEY_CORRECT_COUNT, mCorrectCount);
        savedInstanceState.putInt(KEY_INCORRECT_COUNT, mIncorrectCount);
        savedInstanceState.putBooleanArray(KEY_CHEATED_ON_QUESTION, mCheatedOnQuestion);    // Challenge Ch. 5
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "onStop() called");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy() called");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }

        if (requestCode == REQUEST_CODE_CHEAT) {
            if (data == null) {
                return;
            }
            mCheatedOnQuestion[mCurrentIndex] = CheatActivity.wasAnswerShown(data);
        }
    }

    private void updateQuestion() {
        Log.d(TAG, "Current question index: " + mCurrentIndex);
        int question;
        try {
            question = mQuestionBank[mCurrentIndex].getTextResId();
        } catch (ArrayIndexOutOfBoundsException ex) {
            // Log a message at "error" log level, along with an exception stack trace
            Log.e(TAG, "Index out of bounds", ex);
            return;
        }
        mQuestionTextView.setText(question);
        setAnswerButtonEnabled(true);
    }

    private void checkAnswer(boolean userPressedTrue) {
        boolean answerIsTrue = mQuestionBank[mCurrentIndex].isAnswerTrue();
        int messageResId = 0;

        if (mCheatedOnQuestion[mCurrentIndex]) {    // Challenge Ch. 5
            messageResId = R.string.judgment_toast;
        } else {
            if (userPressedTrue == answerIsTrue) {
                messageResId = R.string.correct_toast;
                mCorrectCount += 1;
            } else {
                messageResId = R.string.incorrect_toast;
                mIncorrectCount += 1;
            }
        }

        Toast toast;
        // Challenge 3:2
        if (mCurrentIndex + 1 == mQuestionBank.length) {    // end quiz
            int percentage = Math.round(100 * mCorrectCount/(mCorrectCount + mIncorrectCount));
            Log.d(TAG, String.format("Quiz is over with result %d%%", percentage));
            mCorrectCount = 0;
            mIncorrectCount = 0;
            String text = String.format(getResources().getString(R.string.results), percentage);
            toast = Toast.makeText(QuizActivity.this, text, Toast.LENGTH_LONG);
            toast.setGravity(Gravity.TOP, 0, 200);
            toast.show();
        } else {
            toast = Toast.makeText(QuizActivity.this,
                    messageResId,
                    Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.TOP, 0, 200);
            toast.show();
        }
    }

    // Challenge 3:1
    private void setAnswerButtonEnabled(boolean state) {
        mTrueButton.setEnabled(state);
        mFalseButton.setEnabled(state);
    }
}
