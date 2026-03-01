package com.kaoyao.applock;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 訓獸神器 2.0 - 家長設定台
 * 讓老闆可以自主勾選要攔截的 App
 */
public class MainActivity extends AppCompatActivity {

    private SharedPreferences prefs;
    private Set<String> blockedApps;
    private List<AppInfo> installedApps = new ArrayList<>();

    private static class AppInfo {
        String name;
        String packageName;
        boolean isSelected;
        AppInfo(String n, String p, boolean s) { 
            name = n; 
            packageName = p; 
            isSelected = s; 
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // 初始化小筆記本 (SharedPreferences)
        prefs = getSharedPreferences("KaoYaoPrefs", MODE_PRIVATE);
        blockedApps = new HashSet<>(prefs.getStringSet("blocked_apps", new HashSet<String>()));

        // 動態建立佈局
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 40, 40, 40);

        TextView title = new TextView(this);
        title.setText("訓獸神器 2.0 - 家長設定台");
        title.setTextSize(24);
        title.setPadding(0, 0, 0, 40);
        layout.addView(title);

        Button btnAccessibility = new Button(this);
        btnAccessibility.setText("第一步：開啟監控權限 (Accessibility)");
        btnAccessibility.setOnClickListener(v -> {
            try {
                startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
            } catch (Exception e) {
                Toast.makeText(this, "無法開啟設定，請手動前往。", Toast.LENGTH_SHORT).show();
            }
        });
        layout.addView(btnAccessibility);

        TextView subTitle = new TextView(this);
        subTitle.setText("\n第二步：勾選要攔截的 App (點擊即可)");
        layout.addView(subTitle);

        ListView listView = new ListView(this);
        loadInstalledApps();
        
        // 建立列表適配器
        ArrayAdapter<AppInfo> adapter = new ArrayAdapter<AppInfo>(this, android.R.layout.simple_list_item_multiple_choice, installedApps) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if (convertView == null) {
                    convertView = getLayoutInflater().inflate(android.R.layout.simple_list_item_multiple_choice, parent, false);
                }
                AppInfo app = getItem(position);
                CheckedTextView text = (CheckedTextView) convertView.findViewById(android.R.id.text1);
                text.setText(app.name + "\n(" + app.packageName + ")");
                // 初始化勾選狀態
                listView.setItemChecked(position, app.isSelected);
                return convertView;
            }
        };
        
        listView.setAdapter(adapter);
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        
        // 點擊監聽：當勾選或取消勾選時，立即更新筆記本
        listView.setOnItemClickListener((parent, view, position, id) -> {
            AppInfo app = installedApps.get(position);
            boolean isChecked = listView.isItemChecked(position);
            app.isSelected = isChecked;
            
            if (isChecked) {
                blockedApps.add(app.packageName);
            } else {
                blockedApps.remove(app.packageName);
            }
            
            // 存入筆記本
            prefs.edit().putStringSet("blocked_apps", blockedApps).apply();
            Log.d("KaoYao", "更新黑名單: " + app.packageName + " (" + (isChecked ? "新增" : "移除") + ")");
            Toast.makeText(this, (isChecked ? "已鎖定: " : "已解鎖: ") + app.name, Toast.LENGTH_SHORT).show();
        });
        
        layout.addView(listView);

        // 2.4 版：新增挑戰模式設定
        TextView modeTitle = new TextView(this);
        modeTitle.setText("\n第三步：選擇挑戰模式");
        modeTitle.setTextSize(18);
        layout.addView(modeTitle);

        android.widget.RadioGroup modeGroup = new android.widget.RadioGroup(this);
        android.widget.RadioButton rbContinuous = new android.widget.RadioButton(this);
        rbContinuous.setText("連續答對模式 (例如連對 5 題)");
        rbContinuous.setId(View.generateViewId());
        
        android.widget.RadioButton rbAccuracy = new android.widget.RadioButton(this);
        rbAccuracy.setText("達標模式 (例如 10 題對 8 題)");
        rbAccuracy.setId(View.generateViewId());

        modeGroup.addView(rbContinuous);
        modeGroup.addView(rbAccuracy);
        
        // 讀取舊設定
        int savedMode = prefs.getInt("challenge_mode", 0); // 0: continuous, 1: accuracy
        if (savedMode == 1) rbAccuracy.setChecked(true);
        else rbContinuous.setChecked(true);

        modeGroup.setOnCheckedChangeListener((group, checkedId) -> {
            int mode = (checkedId == rbAccuracy.getId()) ? 1 : 0;
            prefs.edit().putInt("challenge_mode", mode).apply();
            Toast.makeText(this, "模式已切換", Toast.LENGTH_SHORT).show();
        });

        layout.addView(modeGroup);
        
        setContentView(layout);
    }

    private void loadInstalledApps() {
        PackageManager pm = getPackageManager();
        // 取得手機裡所有安裝的 App
        List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);
        for (ApplicationInfo packageInfo : packages) {
            // 跳過我們自己，避免自殺
            if (packageInfo.packageName.equals(getPackageName())) continue;
            
            // 過濾掉一些不必要的系統組件 (選擇性)
            if ((packageInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0) {
                // 如果是系統 App 但不是 Chrome/YouTube/PlayStore，可以考慮跳過
                if (!packageInfo.packageName.contains("chrome") && 
                    !packageInfo.packageName.contains("youtube") &&
                    !packageInfo.packageName.contains("vending")) {
                    continue;
                }
            }
            
            String name = (String) pm.getApplicationLabel(packageInfo);
            boolean isBlocked = blockedApps.contains(packageInfo.packageName);
            installedApps.add(new AppInfo(name, packageInfo.packageName, isBlocked));
        }
    }
}
