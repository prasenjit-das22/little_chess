package com.toddler.chess;

import android.content.Context;
import android.speech.tts.TextToSpeech;

import java.util.Locale;

/** Thin wrapper around TextToSpeech with a slow, friendly voice. */
final class Speaker implements TextToSpeech.OnInitListener {
    private final TextToSpeech tts;
    private boolean ready;
    private String pending;

    Speaker(Context context) {
        tts = new TextToSpeech(context.getApplicationContext(), this);
    }

    @Override
    public void onInit(int status) {
        if (status != TextToSpeech.SUCCESS) return;
        int result = tts.setLanguage(Locale.US);
        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
            tts.setLanguage(Locale.getDefault());
        }
        tts.setSpeechRate(0.8f);
        tts.setPitch(1.15f);
        ready = true;
        if (pending != null) {
            say(pending);
            pending = null;
        }
    }

    void say(String text) {
        if (!ready) {
            pending = text;
            return;
        }
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "say");
    }

    void stop() {
        if (ready) tts.stop();
    }

    void shutdown() {
        tts.shutdown();
    }
}
