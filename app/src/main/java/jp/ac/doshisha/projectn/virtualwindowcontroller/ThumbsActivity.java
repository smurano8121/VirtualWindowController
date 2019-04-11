package jp.ac.doshisha.projectn.virtualwindowcontroller;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.*;

public class ThumbsActivity extends AppCompatActivity {
    Handler UIHandler = new Handler(Looper.getMainLooper());
    private int buttonIdCounter = 1;
    private int buttonAreaWidth;
    private int stackWidth = 0;
    private static int thumbsRowNum;
    private static int margin = 7;
    private static int padding = 13;
    private RelativeLayout buttonArea;
    private String mode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Nav barの非表示化
        View decor = this.getWindow().getDecorView();
        decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

        setContentView(R.layout.activity_thumbs);


        // Get intent
        Intent intent = getIntent();
        mode = intent.getStringExtra("MODE");

        // Send fetch thumbs command
        TextView title = findViewById(R.id.titleText);
        switch (mode) {
            case "IMAGE":
                new SocketConnection(this).execute("GET_IMAGE_THUMBS");
                title.setText("擬似窓システム - 静止画表示モード");
                break;
            case "VIDEO":
                new SocketConnection(this).execute("GET_VIDEO_THUMBS");
                title.setText("擬似窓システム - 動画表示モード");
                break;
        }

        // Load preferences
        String def = "3";
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        thumbsRowNum = Integer.parseInt(sp.getString("pref_thumbnail_count", def));

        buttonArea = findViewById(R.id.buttonArea);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // テーマの設定
        String def = "back_sky";
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        String theme = sp.getString("pref_theme", def);
        int strId = getResources().getIdentifier(theme, "drawable", getPackageName());

        ConstraintLayout layout = findViewById(R.id.thumbs_layout);
        layout.setBackground(getDrawable(strId));
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        buttonAreaWidth = buttonArea.getWidth();
    }

    public void runOnUI(Runnable runnable) {
        UIHandler.post(runnable);
    }

    public void buttonOnClick(View view) {
        switch (view.getId()) {
            case R.id.button_home:
                finish();
                break;
            default:
                int id = view.getId();
                id--;
                switch (mode) {
                    case "IMAGE":
                        new SocketConnection(this).execute("SET_IMAGE_BY_ID", String.valueOf(id));
                        break;
                    case "VIDEO":
                        new SocketConnection(this).execute("SET_VIDEO_BY_ID", String.valueOf(id));
                        break;
                }

        }
    }

    // http://tongari.webcrow.jp/frog/?p=99
    public void addThumbnailButton(Bitmap bmp) {
        // Generate Image button with bitmap sent from server
        ImageButton btn = new ImageButton(this);
        btn.setImageBitmap(bmp);
        btn.setScaleType(ImageView.ScaleType.CENTER_CROP);
        btn.setBackground(getDrawable(R.drawable.ripple_button));
        btn.setId(buttonIdCounter++);
        btn.setOnClickListener(this::buttonOnClick);

        // Get relative layout params of new ImageButton
        RelativeLayout.LayoutParams prm = new RelativeLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);

        // Add button view into buttonArea
        prm.width = (buttonAreaWidth / thumbsRowNum) - (margin*2);
        prm.height = prm.width*130/190;
        btn.setPadding(padding, padding, padding, padding);

        buttonArea.addView(btn, prm);
        btn = buttonArea.findViewById(btn.getId());

        // Modify button's LayoutParams

        if (btn.getId() != 0) {
            int prevId = btn.getId();
            prevId -= 1;

            // The case next button has to place next line
            if (buttonAreaWidth - (prm.width + stackWidth + margin *2) < 0) {
                stackWidth = 0;
                prm.addRule(RelativeLayout.BELOW, prevId);
                prm.addRule(RelativeLayout.ALIGN_LEFT, RelativeLayout.TRUE);
                prm.setMargins(margin , margin*2, margin, margin*2);
            }
            // The case next button can be placed next to previous button
            else {
                prm.addRule(RelativeLayout.ALIGN_TOP, prevId);
                prm.addRule(RelativeLayout.RIGHT_OF, prevId);
                prm.setMargins(margin , 0, margin, 0);
            }

            btn.setLayoutParams(prm);
            stackWidth += prm.width;
        }
    }

}
