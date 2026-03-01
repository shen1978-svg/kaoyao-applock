package com.kaoyao.applock;

import android.accessibilityservice.AccessibilityService;
import android.content.Intent;
import android.view.accessibility.AccessibilityEvent;
import android.util.Log;
import com.kaoyao.applock.R;

import android.content.SharedPreferences;
import java.util.Set;
import java.util.HashSet;

/**
 * 專業版 - 教育解鎖核心攔截服務 (Android)
 * 使用 Accessibility Service 監控頂層 App 變化
 */
public class KaoYaoAccessibilityService extends AccessibilityService {

    private static final String TAG = "KaoYaoLock";

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            if (event.getPackageName() != null) {
                String topPackageName = event.getPackageName().toString();
                Log.d(TAG, "偵測到頂層 App: " + topPackageName);

                // 2.0 版：改從設定中檢查是否為受限 App
                if (isRestrictedApp(topPackageName)) {
                    launchQuizOverlay();
                }
            }
        }
    }

    private boolean isRestrictedApp(String packageName) {
        // 從 SharedPreferences 讀取家長設定的黑名單
        SharedPreferences prefs = getSharedPreferences("KaoYaoPrefs", MODE_PRIVATE);
        Set<String> blockedApps = prefs.getStringSet("blocked_apps", new HashSet<String>());
        
        // 如果這個 App 在名單中，就攔截它！
        return blockedApps.contains(packageName);
    }

    private void launchQuizOverlay() {
        Log.i(TAG, "觸發教育鎖定 - 彈出測驗頁面");
        // 強制彈出測驗 Activity，並設置 FLAG_ACTIVITY_NEW_TASK
        Intent intent = new Intent(this, QuizActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    @Override
    public void onInterrupt() {
        Log.w(TAG, "服務已中斷");
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        Log.i(TAG, "靠夭教育鎖定服務已啟動 🚀");
    }
}
