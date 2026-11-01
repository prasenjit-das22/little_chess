package com.toddler.chess;

import java.util.Locale;

/** The app language. Strings are written inline as (English, Bengali, Hindi) triples. */
final class Lang {
    static final int EN = 0, BN = 1, HI = 2;
    static final String[] NAMES = {"English", "বাংলা", "हिन्दी"};
    /** Order of the language buttons on the menu: Bengali first. */
    static final int[] MENU_ORDER = {BN, EN, HI};

    static int current = BN;

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

    private static final String[][] NUMBERS = {
            {"One", "এক", "एक"}, {"Two", "দুই", "दो"}, {"Three", "তিন", "तीन"},
            {"Four", "চার", "चार"}, {"Five", "পাঁচ", "पाँच"}, {"Six", "ছয়", "छह"},
            {"Seven", "সাত", "सात"}, {"Eight", "আট", "आठ"}, {"Nine", "নয়", "नौ"},
            {"Ten", "দশ", "दस"},
    };

    /** Counting words 1..10. */
    static String number(int n) {
        return t(NUMBERS[n - 1]);
    }

    /** Counting word 1..10 in a given language. */
    static String numberIn(int n, int lang) {
        return NUMBERS[n - 1][lang];
    }

    /** The number as written in the current language's script. */
    static String digit(int n) {
        String s = String.valueOf(n);
        if (current == EN) return s;
        char zero = current == BN ? '০' : '०';
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < s.length(); i++) b.append((char) (zero + (s.charAt(i) - '0')));
        return b.toString();
    }
}
