package com.kaoyao.applock;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.InputStream;
import java.util.Random;

public class QuizActivity extends AppCompatActivity {

    private int correctCount = 0;
    private int totalAnswered = 0;
    private int challengeMode = 0; // 0: 連續, 1: 達標
    
    private final int GOAL_CONTINUOUS = 5;
    private final int GOAL_ACCURACY_TOTAL = 10;
    private final int GOAL_ACCURACY_PASS = 8;

    private TextView questionText;
    private TextView progressText;
    private LinearLayout optionsLayout;
    private JSONArray quizArray;
    private String currentCorrectAnswer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        SharedPreferences prefs = getSharedPreferences("KaoYaoPrefs", MODE_PRIVATE);
        challengeMode = prefs.getInt("challenge_mode", 0);

        questionText = findViewById(R.id.question_text);
        optionsLayout = findViewById(R.id.options_layout);
        
        progressText = new TextView(this);
        progressText.setTextSize(18);
        progressText.setGravity(Gravity.CENTER);
        progressText.setPadding(0, 0, 0, 30);
        ((LinearLayout)questionText.getParent()).addView(progressText, 1);

        try {
            quizArray = new JSONArray(loadJSONFromAsset());
            showNextQuestion();
            updateProgress();
        } catch (Exception e) {
            questionText.setText("題庫載入失敗");
        }
    }

    private void showNextQuestion() {
        optionsLayout.removeAllViews();
        try {
            int randomIndex = new Random().nextInt(quizArray.length());
            JSONObject obj = quizArray.getJSONObject(randomIndex);
            
            questionText.setText(obj.getString("question"));
            currentCorrectAnswer = obj.getString("answer");
            JSONArray options = obj.getJSONArray("options");

            for (int i = 0; i < options.length(); i++) {
                String optionText = options.getString(i);
                Button btn = new Button(this);
                btn.setText(optionText);
                btn.setAllCaps(false);
                btn.setOnClickListener(v -> handleAnswer(optionText));
                
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, 
                    LinearLayout.LayoutParams.WRAP_CONTENT
                );
                params.setMargins(0, 10, 0, 10);
                optionsLayout.addView(btn, params);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleAnswer(String selected) {
        totalAnswered++;
        boolean isCorrect = selected.equals(currentCorrectAnswer);

        if (challengeMode == 0) { // 連續模式
            if (isCorrect) {
                correctCount++;
                if (correctCount >= GOAL_CONTINUOUS) {
                    win();
                    return;
                }
                Toast.makeText(this, "✅ 答對！連對 " + correctCount + " 題", Toast.LENGTH_SHORT).show();
            } else {
                correctCount = 0;
                Toast.makeText(this, "❌ 答錯！進度重置！", Toast.LENGTH_SHORT).show();
            }
        } else { // 達標模式 (10 題對 8 題)
            if (isCorrect) correctCount++;
            
            if (totalAnswered >= GOAL_ACCURACY_TOTAL) {
                if (correctCount >= GOAL_ACCURACY_PASS) {
                    win();
                    return;
                } else {
                    Toast.makeText(this, "💀 失敗！正確率不足，重來！", Toast.LENGTH_LONG).show();
                    correctCount = 0;
                    totalAnswered = 0;
                }
            } else {
                Toast.makeText(this, isCorrect ? "✅ 正確" : "❌ 錯誤", Toast.LENGTH_SHORT).show();
            }
        }

        updateProgress();
        showNextQuestion();
    }

    private void updateProgress() {
        if (challengeMode == 0) {
            progressText.setText("連續挑戰： " + correctCount + " / " + GOAL_CONTINUOUS);
            progressText.setTextColor(Color.BLUE);
        } else {
            progressText.setText("達標挑戰： " + correctCount + " / " + totalAnswered + " (目標: 10題中8題)");
            progressText.setTextColor(Color.MAGENTA);
        }
    }

    private void win() {
        Toast.makeText(this, "🎉 挑戰成功！放行！", Toast.LENGTH_LONG).show();
        finish();
    }

    private String loadJSONFromAsset() {
        try {
            InputStream is = getAssets().open("curriculum.json");
            byte[] buffer = new byte[is.available()];
            is.read(buffer);
            is.close();
            return new String(buffer, "UTF-8");
        } catch (Exception e) { return "[]"; }
    }
}
