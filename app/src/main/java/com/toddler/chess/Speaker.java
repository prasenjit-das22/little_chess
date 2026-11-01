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
    private int applied = -1;
    private String pending;
    private int pendingLang;

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
        apply(lang);
        if (pending != null) {
            say(pending, pendingLang);
            pending = null;
        }
    }

    void setLanguage(int lang) {
        this.lang = lang;
        if (ready) apply(lang);
    }

    private void apply(int l) {
        if (l == applied) return;
        applied = l;
        for (Locale loc : Lang.locales(l)) {
            int r = tts.isLanguageAvailable(loc);
            if (r >= TextToSpeech.LANG_AVAILABLE) {
                int set = tts.setLanguage(loc);
                if (set != TextToSpeech.LANG_MISSING_DATA && set != TextToSpeech.LANG_NOT_SUPPORTED) return;
            }
        }
        tts.setLanguage(Locale.getDefault());
        if (l != Lang.EN) missingVoice.onMissingVoice(l);
    }

    /** Speaks in the app language. */
    void say(String text) {
        say(text, -1);
    }

    /** Speaks in the given language (-1 for the app language). */
    void say(String text, int inLang) {
        int l = inLang < 0 ? lang : inLang;
        if (!ready) {
            pending = text;
            pendingLang = inLang;
            return;
        }
        apply(l);
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "say");
    }

    void stop() {
        if (ready) tts.stop();
    }

    void shutdown() {
        tts.shutdown();
    }
}
