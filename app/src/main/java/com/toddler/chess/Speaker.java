package com.toddler.chess;

import android.content.Context;
import android.speech.tts.TextToSpeech;

import java.util.Locale;

/** Thin wrapper around TextToSpeech with a slow, friendly voice in the app language. */
final class Speaker implements TextToSpeech.OnInitListener {
    interface MissingVoiceListener {
        void onMissingVoice(int lang);
    }

    private final TextToSpeech tts;
    private final MissingVoiceListener missingVoice;
    private boolean ready;
    private int lang = Lang.EN;
    private String pending;

    Speaker(Context context, MissingVoiceListener missingVoice) {
        this.missingVoice = missingVoice;
        tts = new TextToSpeech(context.getApplicationContext(), this);
    }

    @Override
    public void onInit(int status) {
        if (status != TextToSpeech.SUCCESS) return;
        tts.setSpeechRate(0.8f);
        tts.setPitch(1.15f);
        ready = true;
        applyLanguage();
        if (pending != null) {
            say(pending);
            pending = null;
        }
    }

    void setLanguage(int lang) {
        this.lang = lang;
        if (ready) applyLanguage();
    }

    private void applyLanguage() {
        for (Locale l : Lang.locales(lang)) {
            int r = tts.isLanguageAvailable(l);
            if (r >= TextToSpeech.LANG_AVAILABLE) {
                int set = tts.setLanguage(l);
                if (set != TextToSpeech.LANG_MISSING_DATA && set != TextToSpeech.LANG_NOT_SUPPORTED) return;
            }
        }
        tts.setLanguage(Locale.getDefault());
        if (lang != Lang.EN) missingVoice.onMissingVoice(lang);
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
