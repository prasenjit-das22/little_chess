package com.toddler.chess;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;

/** The music room: one tile per instrument. */
final class MusicView extends TileMenuView {
    MusicView(Context context) {
        super(context);
    }

    @Override
    float headerHeight() {
        return dp(84);
    }

    @Override
    int tileCount() {
        return Instrument.COUNT;
    }

    @Override
    String tileTitle(int i) {
        return Lang.t(Instrument.NAMES[i]);
    }

    @Override
    int tileColor(int i) {
        return Instrument.COLORS[i];
    }

    @Override
    void drawTileIcon(Canvas c, int i, float cx, float cy, float s) {
        InstrumentArt.icon(c, i, cx, cy, s * 0.95f);
    }

    @Override
    void openTile(int i) {
        activity.openInstrument(i);
    }

    @Override
    void paintBackdrop(Canvas c) {
        backdrop(c, Color.rgb(237, 231, 246), Color.rgb(179, 157, 219));
    }

    @Override
    void onHeaderTap(float x, float y) {
        float r = dp(28), m = dp(14) + r;
        if (Math.hypot(x - m, y - m) < r * 1.4f) activity.showHome();
    }

    @Override
    void paintHeader(Canvas c) {
        float w = getWidth();
        paint.setColor(Color.argb(235, 240, 234, 248));
        c.drawRect(0, 0, w, headerHeight(), paint);
        float r = dp(28), m = dp(14) + r;
        Art.homeButton(c, m, m, r);
        float wave = (float) Math.sin(now() / 600.0) * dp(3);
        Art.label(c, Lang.t("Let's make music!", "চলো বাজাই!", "चलो बजाएँ!"), w / 2f + r * 0.6f, dp(34) + wave,
                dp(32), Color.rgb(94, 53, 177));
        Art.text(c, Lang.t("Pick an instrument", "একটা বাদ্যযন্ত্র বেছে নাও", "एक वाद्य यंत्र चुनो"), w / 2f + r * 0.6f,
                dp(68), dp(15), Color.rgb(110, 80, 130), w - m * 2.6f);
    }
}
