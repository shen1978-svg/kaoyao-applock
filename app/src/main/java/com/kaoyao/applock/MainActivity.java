package com.kaoyao.applock;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // 建立一個超簡單的啟動頁面
        androidx.appcompat.widget.AppCompatTextView tv = new androidx.appcompat.widget.AppCompatTextView(this);
        tv.setText("訓獸神器 1.0\n\n請點擊下方按鈕開啟『無障礙服務』以啟動監控功能。");
        tv.setGravity(android.view.Gravity.CENTER);
        
        Button btn = new Button(this);
        btn.setText("開啟設定");
        btn.setOnClickListener(v -> {
            startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
        });

        android.widget.LinearLayout layout = new android.widget.LinearLayout(this);
        layout.setOrientation(android.widget.LinearLayout.vertical);
        layout.setGravity(android.view.Gravity.CENTER);
        layout.addView(tv);
        layout.addView(btn);

        setContentView(layout);
    }
}
