package jp.ac.doshisha.projectn.virtualwindowcontroller;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class StartActivity extends AppCompatActivity {
    private static Context applicationContext = null;
    Handler UIHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        // Socketの設定
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        SocketConnection.setIpAddress(sp.getString("pref_ip", "192.168.10.10"));
        SocketConnection.setPORT(sp.getString("pref_port", "50005"));

        // Connectionの確認
        new SocketConnection(this).execute("GET_MODE");

        applicationContext = getApplicationContext();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // テーマの設定
        String def = "back_sky";
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        String theme = sp.getString("pref_theme", def);
        int strId = getResources().getIdentifier(theme, "drawable", getPackageName());

        ConstraintLayout layout = findViewById(R.id.start_layout);
        layout.setBackground(getDrawable(strId));

        // Nav barの非表示化
        View decor = this.getWindow().getDecorView();
        decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }

    static Context getAppContext() {
        return applicationContext;
    }

    public void runOnUI(Runnable runnable) {
        UIHandler.post(runnable);
    }

    public void buttonOnClick(View view) {
        switch (view.getId()) {
            case R.id.button_live:
                new SocketConnection(this).execute("LIVE");
                new SocketConnection(this).execute("GET_MODE");
                break;
            case R.id.button_image:
                new SocketConnection(this).execute("IMAGE");
                new SocketConnection(this).execute("GET_MODE");
                break;
            case R.id.button_video:
                new SocketConnection(this).execute("VIDEO");
                new SocketConnection(this).execute("GET_MODE");
                break;
            case R.id.button_blank:
                new SocketConnection(this).execute("BLANK");
                new SocketConnection(this).execute("GET_MODE");
                break;
            case R.id.button_home:
                new SocketConnection(this).execute("HOME");
                new SocketConnection(this).execute("GET_MODE");
                break;
            case R.id.button_fullscreen:
                new SocketConnection(this).execute("TOGGLE_FULLSCREEN");
                break;
            case R.id.button_next:
                new SocketConnection(this).execute("NEXT");
                break;
            case R.id.button_previous:
                new SocketConnection(this).execute("PREVIOUS");
                break;
            case R.id.button_setting:
                Intent intent_setting = new Intent(StartActivity.this, SettingActivity.class);
                startActivity(intent_setting);
                break;
        }
    }
}
