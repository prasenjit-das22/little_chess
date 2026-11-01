package com.toddler.chess;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;

/** Small pictures of the instruments for menus; drawn ones for instruments with no emoji. */
final class InstrumentArt {
    private static final Paint FILL = new Paint(Paint.ANTI_ALIAS_FLAG);
    private static final Paint LINE = new Paint(Paint.ANTI_ALIAS_FLAG);
    private static final RectF R = new RectF();
    private static final Path P = new Path();

    static final int WOOD = Color.rgb(141, 90, 55), DARK_WOOD = Color.rgb(93, 58, 36),
            SKIN = Color.rgb(250, 238, 210), INK = Color.rgb(40, 28, 30), BAMBOO = Color.rgb(222, 184, 110),
            BRASS = Color.rgb(240, 190, 60), RED = Color.rgb(198, 40, 40), GOLD = Color.rgb(255, 202, 40);

    static {
        LINE.setStyle(Paint.Style.STROKE);
        LINE.setStrokeCap(Paint.Cap.ROUND);
        LINE.setStrokeJoin(Paint.Join.ROUND);
    }

    private InstrumentArt() {}

    static void icon(Canvas c, int kind, float cx, float cy, float s) {
        String emoji = null;
        switch (kind) {
            case Instrument.PIANO: emoji = "🎹"; break;
            case Instrument.GUITAR: emoji = "🎸"; break;
            case Instrument.VIOLIN: emoji = "🎻"; break;
            case Instrument.BELLS: emoji = "🔔"; break;
            case Instrument.DRUMS: emoji = "🥁"; break;
            case Instrument.TRUMPET: emoji = "🎺"; break;
            case Instrument.SHANKH: emoji = "🐚"; break;
            default: break;
        }
        if (emoji != null) {
            Art.emoji(c, emoji, cx, cy, s * 0.85f);
            return;
        }
        c.save();
        c.translate(cx - s / 2f, cy - s / 2f);
        c.scale(s / 100f, s / 100f);
        switch (kind) {
            case Instrument.TABLA: tabla(c); break;
            case Instrument.FLUTE: flute(c); break;
            case Instrument.HARMONIUM: harmonium(c); break;
            case Instrument.SITAR: sitar(c); break;
            case Instrument.EKTARA: ektara(c); break;
            case Instrument.DHAK: dhak(c); break;
            default: xylophone(c); break;
        }
        c.restore();
    }

    private static void fill(Path p, int color, Canvas c) {
        FILL.setColor(color);
        c.drawPath(p, FILL);
        LINE.setColor(BirdArt.darker(color, 0.45f));
        LINE.setStrokeWidth(1.6f);
        c.drawPath(p, LINE);
    }

    private static void oval(Canvas c, float l, float t, float r, float b, int color) {
        R.set(l, t, r, b);
        FILL.setColor(color);
        c.drawOval(R, FILL);
        LINE.setColor(BirdArt.darker(color, 0.45f));
        LINE.setStrokeWidth(1.6f);
        c.drawOval(R, LINE);
    }

    private static void line(Canvas c, float x1, float y1, float x2, float y2, float w, int color) {
        LINE.setColor(color);
        LINE.setStrokeWidth(w);
        c.drawLine(x1, y1, x2, y2, LINE);
    }

    private static void tabla(Canvas c) {
        // Bayan: the round metal drum on the left.
        P.reset();
        P.moveTo(8, 48);
        P.cubicTo(4, 92, 56, 92, 52, 48);
        P.close();
        fill(P, Color.rgb(176, 120, 80), c);
        oval(c, 8, 40, 52, 56, SKIN);
        oval(c, 20, 44, 34, 52, INK);
        // Dayan: the taller wooden drum on the right.
        P.reset();
        P.moveTo(58, 30);
        P.lineTo(92, 30);
        P.lineTo(88, 88);
        P.lineTo(62, 88);
        P.close();
        fill(P, WOOD, c);
        for (int i = 0; i < 5; i++) line(c, 62 + i * 6.5f, 34, 64 + i * 5.5f, 86, 1.2f, Color.rgb(230, 210, 170));
        oval(c, 58, 24, 92, 36, SKIN);
        oval(c, 68, 27, 82, 33, INK);
    }

    private static void flute(Canvas c) {
        c.save();
        c.rotate(-35, 50, 50);
        R.set(4, 44, 96, 56);
        FILL.setColor(BAMBOO);
        c.drawRoundRect(R, 6, 6, FILL);
        LINE.setColor(BirdArt.darker(BAMBOO, 0.45f));
        LINE.setStrokeWidth(1.6f);
        c.drawRoundRect(R, 6, 6, LINE);
        FILL.setColor(RED);
        c.drawRect(10, 44, 14, 56, FILL);
        c.drawRect(86, 44, 90, 56, FILL);
        FILL.setColor(INK);
        c.drawCircle(22, 50, 2.6f, FILL);
        for (int i = 0; i < 6; i++) c.drawCircle(46 + i * 7, 50, 2.3f, FILL);
        c.restore();
    }

    private static void harmonium(Canvas c) {
        // Bellows at the back.
        R.set(12, 22, 88, 42);
        FILL.setColor(Color.rgb(120, 70, 45));
        c.drawRect(R, FILL);
        for (int i = 0; i < 4; i++) line(c, 12, 26 + i * 4.5f, 88, 26 + i * 4.5f, 1.3f, Color.rgb(70, 40, 25));
        P.reset();
        P.addRoundRect(new RectF(8, 38, 92, 84), 5, 5, Path.Direction.CW);
        fill(P, WOOD, c);
        R.set(14, 44, 86, 62);
        FILL.setColor(Color.WHITE);
        c.drawRect(R, FILL);
        for (int i = 1; i < 10; i++) line(c, 14 + i * 7.2f, 44, 14 + i * 7.2f, 62, 0.9f, Color.rgb(120, 120, 120));
        FILL.setColor(INK);
        int[] black = {1, 2, 4, 5, 6, 8, 9};
        for (int b : black) c.drawRect(14 + b * 7.2f - 2.2f, 44, 14 + b * 7.2f + 2.2f, 54, FILL);
        for (int i = 0; i < 5; i++) {
            FILL.setColor(Color.rgb(250, 240, 220));
            c.drawCircle(22 + i * 14, 74, 3, FILL);
        }
    }

    private static void sitar(Canvas c) {
        c.save();
        c.rotate(40, 50, 50);
        R.set(44, 2, 56, 72);
        FILL.setColor(DARK_WOOD);
        c.drawRoundRect(R, 3, 3, FILL);
        for (int i = 0; i < 8; i++) line(c, 43, 10 + i * 7, 57, 10 + i * 7, 1.4f, Color.rgb(220, 220, 225));
        for (int i = 0; i < 3; i++) line(c, 47 + i * 3, 4, 47 + i * 3, 90, 0.7f, Color.rgb(240, 240, 240));
        oval(c, 30, 64, 70, 98, Color.rgb(196, 120, 60));
        oval(c, 44, 0, 56, 8, Color.rgb(196, 120, 60));
        c.restore();
    }

    private static void ektara(Canvas c) {
        line(c, 30, 70, 62, 10, 4, BAMBOO);
        line(c, 54, 70, 62, 10, 4, BAMBOO);
        oval(c, 18, 52, 62, 96, Color.rgb(214, 130, 60));
        FILL.setColor(Color.rgb(255, 213, 79));
        for (int i = 0; i < 6; i++) {
            double a = i * Math.PI / 3;
            c.drawCircle(40 + (float) Math.cos(a) * 13, 74 + (float) Math.sin(a) * 13, 2.2f, FILL);
        }
        line(c, 40, 74, 62, 8, 1, Color.rgb(60, 60, 60));
        line(c, 58, 12, 70, 6, 3.5f, DARK_WOOD);
    }

    private static void dhak(Canvas c) {
        // Kash flower plumes on top.
        for (int i = 0; i < 7; i++) {
            double a = Math.toRadians(-150 + i * 20);
            float ex = 50 + (float) Math.cos(a) * 34, ey = 40 + (float) Math.sin(a) * 30;
            line(c, 50, 40, ex, ey, 1.4f, Color.rgb(200, 190, 170));
            R.set(ex - 4, ey - 4, ex + 4, ey + 4);
            FILL.setColor(Color.rgb(250, 248, 240));
            c.drawOval(R, FILL);
        }
        P.reset();
        P.addRoundRect(new RectF(14, 40, 86, 86), 14, 14, Path.Direction.CW);
        fill(P, RED, c);
        line(c, 18, 52, 82, 52, 3, GOLD);
        line(c, 18, 74, 82, 74, 3, GOLD);
        for (int i = 0; i < 6; i++) line(c, 22 + i * 11, 52, 27 + i * 11, 74, 1.2f, GOLD);
        oval(c, 6, 40, 22, 86, SKIN);
        oval(c, 78, 40, 94, 86, SKIN);
    }

    private static void xylophone(Canvas c) {
        line(c, 8, 34, 92, 46, 4, DARK_WOOD);
        line(c, 8, 74, 92, 64, 4, DARK_WOOD);
        for (int i = 0; i < 7; i++) {
            float x = 12 + i * 11.5f;
            float top = 28 + i * 2.2f, bottom = 82 - i * 2.2f;
            R.set(x, top, x + 9, bottom);
            FILL.setColor(Instrument.NOTE_COLORS[i]);
            c.drawRoundRect(R, 3, 3, FILL);
            FILL.setColor(Color.argb(140, 255, 255, 255));
            c.drawCircle(x + 4.5f, top + 6, 1.6f, FILL);
            c.drawCircle(x + 4.5f, bottom - 6, 1.6f, FILL);
        }
        line(c, 60, 96, 86, 74, 2.5f, BAMBOO);
        FILL.setColor(RED);
        c.drawCircle(88, 72, 5, FILL);
    }
}
