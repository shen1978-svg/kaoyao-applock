package com.kaoyao.applock;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.kaoyao.applock.R;

/**
 * 專業版 - 教育解鎖測驗頁面 (Android)
 * 模擬 Apple 風格介面與 108 課綱邏輯
 */
public class QuizActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz); 

        TextView questionText = findViewById(R.id.question_text);
        Button btnOption1 = findViewById(R.id.btn_opt1);
        
        // 此處邏輯會從 assets/curriculum.json 讀取資料
        questionText.setText("海的部首是什麼？");
        btnOption1.setText("氵");
        
        btnOption1.setOnClickListener(v -> {
            Toast.makeText(this, "✅ 驗證成功！解鎖 15 分鐘。", Toast.LENGTH_LONG).show();
            finish(); // 關閉覆蓋層，完成解鎖
        });
    }
}
