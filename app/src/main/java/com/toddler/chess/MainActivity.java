package com.toddler.chess;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import java.util.List;

public class MainActivity extends Activity {
    private static final String PREFS = "little_chess";
    private static final String KEY_NAME = "child_name";

    Speaker speaker;
    Chimes chimes;
    private HomeView home;
    private View current;
    private long lastBackPress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        speaker = new Speaker(this);
        chimes = new Chimes(this);
        home = new HomeView(this);
        showHome();
        String name = childName();
        speaker.say(name.isEmpty() ? "Hello! Let's play!" : "Hello " + name + "! Let's play!");
    }

    void showHome() {
        show(home);
        speaker.stop();
    }

    void openDeck(List<Item> items) {
        FlashcardView view = new FlashcardView(this, items);
        show(view);
        view.start();
    }

    void openBoard() {
        BoardView view = new BoardView(this);
        show(view);
        view.start();
    }

    private void show(View view) {
        setContentView(view);
        current = view;
        hideSystemUi();
    }

    String childName() {
        return getSharedPreferences(PREFS, MODE_PRIVATE).getString(KEY_NAME, "");
    }

    void askChildName() {
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        input.setText(childName());
        new AlertDialog.Builder(this)
                .setTitle("Child's name")
                .setView(input)
                .setPositiveButton("Save", (d, which) -> {
                    String name = input.getText().toString().trim();
                    SharedPreferences.Editor e = getSharedPreferences(PREFS, MODE_PRIVATE).edit();
                    e.putString(KEY_NAME, name).apply();
                    if (!name.isEmpty()) speaker.say("Hello " + name + "!");
                    home.invalidate();
                })
                .setNegativeButton("Cancel", null)
                .setOnDismissListener(d -> hideSystemUi())
                .show();
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onBackPressed() {
        if (current != home) {
            showHome();
            return;
        }
        // Little hands hit back by accident, so leaving needs two presses.
        long t = System.currentTimeMillis();
        if (t - lastBackPress < 2000) {
            super.onBackPressed();
        } else {
            lastBackPress = t;
            Toast.makeText(this, "Press back again to exit", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) hideSystemUi();
    }

    @SuppressWarnings("deprecation")
    private void hideSystemUi() {
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }

    @Override
    protected void onPause() {
        super.onPause();
        speaker.stop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        speaker.shutdown();
        chimes.release();
    }
}
