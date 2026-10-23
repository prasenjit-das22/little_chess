package com.toddler.chess;

import java.util.Locale;

/** The app language. Strings are written inline as (English, Bengali, Hindi) triples. */
final class Lang {
    static final int EN = 0, BN = 1, HI = 2;
    static final String[] NAMES = {"English", "বাংলা", "हिन्दी"};

    static int current = EN;

    private Lang() {}

    /** Picks the string for the current language. */
    static String t(String en, String bn, String hi) {
        return current == BN ? bn : current == HI ? hi : en;
    }

    /** Picks from a {en, bn, hi} array. */
    static String t(String[] v) {
        return v[current];
    }

    static Locale[] locales(int lang) {
        switch (lang) {
            case BN: return new Locale[]{new Locale("bn", "IN"), new Locale("bn", "BD"), new Locale("bn")};
            case HI: return new Locale[]{new Locale("hi", "IN"), new Locale("hi")};
            default: return new Locale[]{Locale.US, Locale.UK, Locale.ENGLISH};
        }
    }

    /** Counting words 1..5. */
    static String number(int n) {
        String[][] words = {
                {"One", "এক", "एक"}, {"Two", "দুই", "दो"}, {"Three", "তিন", "तीन"},
                {"Four", "চার", "चार"}, {"Five", "পাঁচ", "पाँच"},
        };
        return t(words[n - 1]);
    }

    /** The digit as written in the current language's script. */
    static String digit(int n) {
        String[] bn = {"০", "১", "২", "৩", "৪", "৫"};
        String[] hi = {"०", "१", "२", "३", "४", "५"};
        return current == BN ? bn[n] : current == HI ? hi[n] : String.valueOf(n);
    }
}
