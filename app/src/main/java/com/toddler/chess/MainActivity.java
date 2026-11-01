package com.toddler.chess;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.text.InputType;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import java.util.List;

public class MainActivity extends Activity {
    private static final String PREFS = "little_chess";
    private static final String KEY_NAME = "child_name";
    private static final String KEY_LANG = "language";

    Speaker speaker;
    Chimes chimes;
    SoundBank sounds;
    private HomeView home;
    private View current;
    private long lastBackPress;
    private final boolean[] warnedVoice = new boolean[3];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        Data.init(this);
        Lang.current = prefs().getInt(KEY_LANG, Lang.BN);
        speaker = new Speaker(this, lang -> runOnUiThread(() -> warnMissingVoice(lang)));
        speaker.setLanguage(Lang.current);
        chimes = new Chimes(this);
        sounds = new SoundBank(this);
        home = new HomeView(this);
        showHome();
        speaker.say(greeting());
    }

    private SharedPreferences prefs() {
        return getSharedPreferences(PREFS, MODE_PRIVATE);
    }

    private String greeting() {
        String name = childName();
        String hello = Lang.t("Hello", "হ্যালো", "नमस्ते");
        return (name.isEmpty() ? hello : hello + " " + name) + "! "
                + Lang.t("Let's play!", "চলো খেলি!", "चलो खेलें!");
    }

    void setLanguage(int lang) {
        Lang.current = lang;
        prefs().edit().putInt(KEY_LANG, lang).apply();
        chimes.next();
        speaker.setLanguage(lang);
        speaker.say(greeting());
    }

    /** Offers to download the voice when the phone has no Bengali or Hindi text-to-speech voice. */
    private void warnMissingVoice(int lang) {
        if (warnedVoice[lang] || isFinishing()) return;
        warnedVoice[lang] = true;
        new AlertDialog.Builder(this)
                .setTitle(Lang.NAMES[lang] + " voice")
                .setMessage("This phone has no " + (lang == Lang.BN ? "Bengali" : "Hindi")
                        + " speaking voice yet. Words will show in " + Lang.NAMES[lang]
                        + " but may be read in another voice.\n\nOpen the voice settings, choose "
                        + "\"Speech Services by Google\" and download the "
                        + (lang == Lang.BN ? "Bengali (India)" : "Hindi (India)") + " voice.")
                .setPositiveButton("Get voice", (d, which) -> openVoiceInstaller())
                .setNegativeButton("Later", null)
                .setOnDismissListener(d -> hideSystemUi())
                .show();
    }

    private void openVoiceInstaller() {
        try {
            startActivity(new Intent(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA));
        } catch (ActivityNotFoundException e) {
            try {
                startActivity(new Intent("com.android.settings.TTS_SETTINGS"));
            } catch (ActivityNotFoundException ignored) {
                Toast.makeText(this, "Open Settings > Accessibility > Text-to-speech", Toast.LENGTH_LONG).show();
            }
        }
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

    void openMusic() {
        MusicView view = new MusicView(this);
        show(view);
        speaker.say(Lang.t("Let's make music! Pick an instrument.", "চলো বাজাই! একটা বাদ্যযন্ত্র বেছে নাও।",
                "चलो बजाएँ! एक वाद्य यंत्र चुनो।"));
    }

    void openInstrument(int kind) {
        InstrumentView view = new InstrumentView(this, kind);
        show(view);
        view.start();
    }

    void openQuiz() {
        QuizView view = new QuizView(this);
        show(view);
        view.start();
    }

    void openMoves() {
        MovesView view = new MovesView(this);
        show(view);
        view.start();
    }

    void openBoard() {
        BoardView view = new BoardView(this);
        show(view);
        view.start();
    }

    private void show(View view) {
        if (current instanceof InstrumentView) ((InstrumentView) current).stop();
        setContentView(view);
        current = view;
        hideSystemUi();
    }

    String childName() {
        return prefs().getString(KEY_NAME, "");
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
                    prefs().edit().putString(KEY_NAME, name).apply();
                    if (!name.isEmpty()) speaker.say(greeting());
                    home.invalidate();
                })
                .setNegativeButton("Cancel", null)
                .setOnDismissListener(d -> hideSystemUi())
                .show();
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onBackPressed() {
        if (current instanceof InstrumentView) {
            openMusic();
            return;
        }
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
        if (current instanceof InstrumentView) ((InstrumentView) current).pause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        speaker.shutdown();
        chimes.release();
        sounds.release();
    }
}
