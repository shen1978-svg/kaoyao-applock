package com.kaoyao.applock;

import android.accessibilityservice.AccessibilityService;
import android.content.Intent;
import android.view.accessibility.AccessibilityEvent;
import android.util.Log;
import com.kaoyao.applock.R;

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

                // 檢查是否為受限 App (範例：YouTube, TikTok)
                if (isRestrictedApp(topPackageName)) {
                    launchQuizOverlay();
                }
            }
        }
    }

    private boolean isRestrictedApp(String packageName) {
        // 此處邏輯應與家長設定台同步
        return packageName.equals("com.google.android.youtube") || 
               packageName.equals("com.zhiliaoapp.musically"); // TikTok
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
