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

    Item(String label, String speech, int labelColor, int background, Painter painter) {
        this.label = label;
        this.speech = speech;
        this.labelColor = labelColor;
        this.background = background;
        this.painter = painter;
    }
}
