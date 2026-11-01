package com.toddler.chess;

import android.graphics.Canvas;

/** One flash card: a picture, the word under it and what gets spoken. */
final class Item {
    interface Painter {
        void draw(Canvas c, float cx, float cy, float size);
    }

    final String label;
    final String speech;
    final int labelColor;
    final int background;
    final Painter painter;
    /** A smaller second word under the label (the Bengali word, or English in Bengali mode). */
    String sub;
    /** Language the speech is read in, or -1 for the app language. */
    int speechLang = -1;
    /** Longer text shown on the card, such as the words of a rhyme. */
    String text;
    /** What the quiz asks for ("Where is ...?"); the label when null. */
    String ask;

    String ask() {
        return ask != null ? ask : label;
    }

    Item(String label, String speech, int labelColor, int background, Painter painter) {
        this.label = label;
        this.speech = speech;
        this.labelColor = labelColor;
        this.background = background;
        this.painter = painter;
    }
}
