package com.toddler.chess;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;

/**
 * Cartoon birds drawn from a short spec (see assets/birds.txt), so every bird in the list gets a
 * picture even when there is no emoji for it. Drawing happens in a 100x100 box, bird facing right.
 */
final class BirdArt {
    static final class Spec {
        char kind = 's';
        int body, head, wing, belly, beak, legs, accent, crest;
        String flags = "";

        boolean has(char f) {
            return flags.indexOf(f) >= 0;
        }

        int count(char f) {
            int n = 0;
            for (int i = 0; i < flags.length(); i++) if (flags.charAt(i) == f) n++;
            return n;
        }
    }

    private static final Paint FILL = new Paint(Paint.ANTI_ALIAS_FLAG);
    private static final Paint LINE = new Paint(Paint.ANTI_ALIAS_FLAG);
    private static final Path PATH = new Path();
    private static final RectF R = new RectF();

    static {
        LINE.setStyle(Paint.Style.STROKE);
        LINE.setStrokeCap(Paint.Cap.ROUND);
        LINE.setStrokeJoin(Paint.Join.ROUND);
    }

    private BirdArt() {}

    /** Parses "@kind body head wing belly beak legs accent crest flags"; null if malformed. */
    static Spec parse(String s) {
        String[] p = s.trim().substring(1).trim().split("\\s+");
        if (p.length < 9) return null;
        Spec b = new Spec();
        b.kind = p[0].charAt(0);
        b.body = color(p[1]);
        b.head = color(p[2]);
        b.wing = color(p[3]);
        b.belly = color(p[4]);
        b.beak = color(p[5]);
        b.legs = color(p[6]);
        b.accent = color(p[7]);
        b.crest = color(p[8]);
        b.flags = p.length > 9 && !p[9].equals("-") ? p[9] : "";
        return b;
    }

    private static int color(String hex) {
        if (hex.equals("-")) return 0;
        try {
            return 0xFF000000 | Integer.parseInt(hex, 16);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    static int darker(int c, float f) {
        return Color.rgb((int) (Color.red(c) * (1 - f)), (int) (Color.green(c) * (1 - f)),
                (int) (Color.blue(c) * (1 - f)));
    }

    static int lighter(int c, float f) {
        return Color.rgb((int) (Color.red(c) + (255 - Color.red(c)) * f),
                (int) (Color.green(c) + (255 - Color.green(c)) * f),
                (int) (Color.blue(c) + (255 - Color.blue(c)) * f));
    }

    private static int outline(int c) {
        float lum = Color.red(c) * 0.3f + Color.green(c) * 0.59f + Color.blue(c) * 0.11f;
        return lum < 70 ? Color.rgb(20, 16, 24) : darker(c, 0.42f);
    }

    static void draw(Canvas c, Spec b, float cx, float cy, float size) {
        c.save();
        c.translate(cx - size / 2f, cy - size / 2f);
        c.scale(size / 100f, size / 100f);
        switch (b.kind) {
            case 'o': owl(c, b); break;
            case 'f': flying(c, b); break;
            case 'h': hummingbird(c, b); break;
            default: side(c, b); break;
        }
        c.restore();
    }

    // ---- basic shapes ----

    private static void ellipse(Canvas c, float x, float y, float rx, float ry, float deg, int color) {
        c.save();
        c.rotate(deg, x, y);
        R.set(x - rx, y - ry, x + rx, y + ry);
        FILL.setColor(color);
        c.drawOval(R, FILL);
        LINE.setColor(outline(color));
        LINE.setStrokeWidth(1.3f);
        c.drawOval(R, LINE);
        c.restore();
    }

    private static void blob(Canvas c, float x, float y, float rx, float ry, float deg, int color) {
        c.save();
        c.rotate(deg, x, y);
        R.set(x - rx, y - ry, x + rx, y + ry);
        FILL.setColor(color);
        c.drawOval(R, FILL);
        c.restore();
    }

    private static void fillPath(Canvas c, Path p, int color) {
        FILL.setColor(color);
        c.drawPath(p, FILL);
        LINE.setColor(outline(color));
        LINE.setStrokeWidth(1.3f);
        c.drawPath(p, LINE);
    }

    private static void line(Canvas c, float x1, float y1, float x2, float y2, float w, int color) {
        LINE.setColor(color);
        LINE.setStrokeWidth(w);
        c.drawLine(x1, y1, x2, y2, LINE);
    }

    // ---- scenery ----

    private static void branch(Canvas c, float y) {
        line(c, 6, y + 3, 96, y - 3, 5.5f, Color.rgb(121, 85, 61));
        line(c, 78, y - 2, 88, y - 10, 2.5f, Color.rgb(121, 85, 61));
        blob(c, 90, y - 13, 6, 3, -40, Color.rgb(102, 187, 106));
        blob(c, 14, y + 1, 5, 2.5f, 30, Color.rgb(129, 199, 132));
    }

    private static void ground(Canvas c, float y) {
        blob(c, 50, y + 3, 44, 6, 0, Color.argb(150, 120, 180, 90));
        LINE.setColor(Color.rgb(104, 159, 56));
        LINE.setStrokeWidth(1.6f);
        for (int i = 0; i < 6; i++) {
            float x = 14 + i * 14;
            c.drawLine(x, y + 2, x - 2, y - 4, LINE);
            c.drawLine(x + 2, y + 2, x + 4, y - 3, LINE);
        }
    }

    private static void water(Canvas c, float y) {
        FILL.setColor(Color.argb(215, 129, 199, 245));
        PATH.reset();
        PATH.moveTo(0, y);
        for (int i = 0; i < 5; i++) {
            float x = i * 20;
            PATH.quadTo(x + 5, y - 3, x + 10, y);
            PATH.quadTo(x + 15, y + 3, x + 20, y);
        }
        PATH.lineTo(100, 100);
        PATH.lineTo(0, 100);
        PATH.close();
        c.drawPath(PATH, FILL);
        LINE.setColor(Color.argb(200, 255, 255, 255));
        LINE.setStrokeWidth(1.4f);
        c.drawLine(18, y + 6, 30, y + 6, LINE);
        c.drawLine(62, y + 9, 78, y + 9, LINE);
    }

    private static void legs(Canvas c, Spec b, float x1, float x2, float top, float bottom, boolean knees) {
        int col = b.legs != 0 ? b.legs : Color.rgb(140, 110, 90);
        for (float x : new float[]{x1, x2}) {
            if (knees) {
                float ky = top + (bottom - top) * 0.45f;
                line(c, x, top, x + 2.5f, ky, 2.4f, col);
                line(c, x + 2.5f, ky, x, bottom, 2.4f, col);
            } else {
                line(c, x, top, x, bottom, 2.4f, col);
            }
            line(c, x, bottom, x + 5, bottom + 0.5f, 1.8f, col);
            line(c, x, bottom, x - 3.5f, bottom + 0.5f, 1.8f, col);
        }
    }

    // ---- parts ----

    private static void tail(Canvas c, Spec b, float x, float y, float deg, float len, float w) {
        if (b.has('Z')) len *= 0.55f;
        if (b.has('L')) len *= 1.8f;
        double a = Math.toRadians(deg);
        float dx = (float) Math.cos(a), dy = (float) Math.sin(a);
        float nx = -dy, ny = dx;
        float tx = x + dx * len, ty = y + dy * len;
        int col = b.wing;
        if (b.has('S')) {
            line(c, x, y, tx + dx * len * 1.6f, ty + dy * len * 1.6f + 3, 2.2f, darker(col, 0.05f));
            line(c, x, y, tx + dx * len * 1.5f - nx * 3, ty + dy * len * 1.5f - ny * 3, 2.2f, darker(col, 0.05f));
        }
        PATH.reset();
        PATH.moveTo(x + nx * w * 0.5f, y + ny * w * 0.5f);
        if (b.has('F')) {
            PATH.lineTo(tx + nx * w * 0.6f, ty + ny * w * 0.6f);
            PATH.lineTo(x + dx * len * 0.65f, y + dy * len * 0.65f);
            PATH.lineTo(tx - nx * w * 0.6f, ty - ny * w * 0.6f);
        } else {
            float tw = b.has('L') ? w * 0.3f : w * 0.45f;
            PATH.lineTo(tx + nx * tw, ty + ny * tw);
            PATH.quadTo(tx + dx * 3, ty + dy * 3, tx - nx * tw, ty - ny * tw);
        }
        PATH.lineTo(x - nx * w * 0.5f, y - ny * w * 0.5f);
        PATH.close();
        fillPath(c, PATH, col);
    }

    private static void peacockFan(Canvas c, Spec b, float x, float y) {
        int green = b.accent != 0 ? b.accent : Color.rgb(46, 125, 50);
        FILL.setColor(Color.argb(170, Color.red(green), Color.green(green), Color.blue(green)));
        R.set(x - 46, y - 46, x + 46, y + 46);
        c.drawArc(R, 185, 170, true, FILL);
        for (int i = 0; i <= 8; i++) {
            double a = Math.toRadians(190 + i * 20);
            float ex = x + (float) Math.cos(a) * 40, ey = y + (float) Math.sin(a) * 40;
            line(c, x, y, ex, ey, 1.2f, darker(green, 0.2f));
            blob(c, ex, ey, 5.5f, 6.5f, i * 20 - 80, Color.rgb(255, 202, 40));
            blob(c, ex, ey, 4, 5, i * 20 - 80, Color.rgb(0, 150, 136));
            blob(c, ex, ey, 2, 2.6f, i * 20 - 80, Color.rgb(13, 71, 161));
        }
    }

    private static void wing(Canvas c, Spec b, float x, float y, float rx, float ry, float deg) {
        c.save();
        c.rotate(deg, x, y);
        PATH.reset();
        PATH.moveTo(x + rx, y - ry * 0.1f);
        PATH.cubicTo(x + rx, y - ry * 1.2f, x - rx * 0.6f, y - ry, x - rx * 1.35f, y + ry * 0.35f);
        PATH.cubicTo(x - rx * 0.4f, y + ry * 1.1f, x + rx * 0.7f, y + ry, x + rx, y - ry * 0.1f);
        PATH.close();
        fillPath(c, PATH, b.wing);
        LINE.setColor(darker(b.wing, 0.25f));
        LINE.setStrokeWidth(1.1f);
        c.drawLine(x - rx * 0.2f, y + ry * 0.35f, x - rx * 0.95f, y + ry * 0.45f, LINE);
        c.drawLine(x + rx * 0.1f, y + ry * 0.6f, x - rx * 0.7f, y + ry * 0.75f, LINE);
        if (b.has('W') && b.accent != 0) blob(c, x + rx * 0.1f, y - ry * 0.05f, rx * 0.45f, ry * 0.28f, 0, b.accent);
        c.restore();
    }

    private static void neck(Canvas c, Spec b, float x1, float y1, float x2, float y2, float w1, float w2, float bend) {
        float mx = (x1 + x2) / 2f - bend, my = (y1 + y2) / 2f;
        double a = Math.atan2(y2 - y1, x2 - x1);
        float nx = (float) -Math.sin(a), ny = (float) Math.cos(a);
        PATH.reset();
        PATH.moveTo(x1 + nx * w1, y1 + ny * w1);
        PATH.quadTo(mx + nx * (w1 + w2) / 2f, my + ny * (w1 + w2) / 2f, x2 + nx * w2, y2 + ny * w2);
        PATH.lineTo(x2 - nx * w2, y2 - ny * w2);
        PATH.quadTo(mx - nx * (w1 + w2) / 2f, my - ny * (w1 + w2) / 2f, x1 - nx * w1, y1 - ny * w1);
        PATH.close();
        FILL.setShader(new LinearGradient(x1, y1, x2, y2, b.body, b.head, Shader.TileMode.CLAMP));
        c.drawPath(PATH, FILL);
        FILL.setShader(null);
        LINE.setColor(outline(b.body));
        LINE.setStrokeWidth(1.3f);
        c.drawPath(PATH, LINE);
    }

    /** Draws the beak starting at (x, y), pointing at angle deg. */
    private static void beak(Canvas c, Spec b, float x, float y, float len, float h, float deg, int type) {
        int col = b.beak != 0 ? b.beak : Color.rgb(255, 179, 0);
        len *= (float) Math.pow(1.35, b.count('+'));
        h *= (float) Math.pow(1.15, b.count('+'));
        c.save();
        c.rotate(deg, x, y);
        PATH.reset();
        if (b.has('Y')) {
            PATH.moveTo(x, y - h * 0.4f);
            PATH.lineTo(x + len * 0.75f, y - h * 0.2f);
            PATH.lineTo(x + len * 0.75f, y + h * 0.2f);
            PATH.lineTo(x, y + h * 0.4f);
            PATH.close();
            fillPath(c, PATH, col);
            ellipse(c, x + len * 0.85f, y, len * 0.17f, h * 0.55f, 0, col);
        } else if (b.has('D') || type == 2) {
            PATH.moveTo(x, y - h / 2f);
            PATH.quadTo(x + len * 0.6f, y - h * 0.5f, x + len, y + len * 0.32f);
            PATH.quadTo(x + len * 0.5f, y + h * 0.1f, x, y + h / 2f);
            PATH.close();
            fillPath(c, PATH, col);
        } else if (b.has('H') || type == 1) {
            PATH.moveTo(x - 1, y - h / 2f);
            PATH.quadTo(x + len * 0.85f, y - h * 0.65f, x + len, y + h * 0.55f);
            PATH.quadTo(x + len * 0.55f, y + h * 0.15f, x + len * 0.45f, y + h * 0.35f);
            PATH.lineTo(x - 1, y + h / 2f);
            PATH.close();
            fillPath(c, PATH, col);
        } else if (type == 3) {
            PATH.moveTo(x - 1, y - h / 2f);
            PATH.lineTo(x + len * 0.8f, y - h * 0.32f);
            PATH.quadTo(x + len * 1.05f, y - h * 0.25f, x + len, y + h * 0.1f);
            PATH.quadTo(x + len * 0.9f, y + h * 0.42f, x + len * 0.6f, y + h * 0.35f);
            PATH.lineTo(x - 1, y + h / 2f);
            PATH.close();
            fillPath(c, PATH, col);
        } else {
            PATH.moveTo(x - 1, y - h / 2f);
            PATH.quadTo(x + len * 0.5f, y - h * 0.45f, x + len, y + h * 0.08f);
            PATH.quadTo(x + len * 0.5f, y + h * 0.4f, x - 1, y + h / 2f);
            PATH.close();
            fillPath(c, PATH, col);
            line(c, x + 1, y + h * 0.05f, x + len * 0.6f, y + h * 0.12f, 0.8f, darker(col, 0.35f));
        }
        if (b.has('Q')) {
            int pouch = b.accent != 0 ? b.accent : lighter(col, 0.2f);
            PATH.reset();
            PATH.moveTo(x + 1, y + h * 0.4f);
            PATH.quadTo(x + len * 0.35f, y + h * 2.2f, x + len * 0.85f, y + h * 0.4f);
            PATH.close();
            fillPath(c, PATH, pouch);
        }
        if (b.has('A')) {
            int cas = b.crest != 0 ? b.crest : b.accent != 0 ? b.accent : col;
            PATH.reset();
            PATH.moveTo(x - 2, y - h * 0.45f);
            PATH.quadTo(x + len * 0.15f, y - h * 1.6f, x + len * 0.55f, y - h * 0.5f);
            PATH.close();
            fillPath(c, PATH, cas);
        }
        c.restore();
    }

    private static void eye(Canvas c, Spec b, float x, float y, float r) {
        FILL.setColor(Color.WHITE);
        c.drawCircle(x, y, r, FILL);
        FILL.setColor(b.has('R') && b.accent != 0 ? b.accent : Color.rgb(62, 39, 35));
        c.drawCircle(x + r * 0.12f, y, r * 0.74f, FILL);
        FILL.setColor(Color.rgb(15, 10, 15));
        c.drawCircle(x + r * 0.18f, y, r * 0.45f, FILL);
        FILL.setColor(Color.WHITE);
        c.drawCircle(x + r * 0.38f, y - r * 0.3f, r * 0.22f, FILL);
    }

    private static void head(Canvas c, Spec b, float x, float y, float r, float beakLen, float beakH,
                             float beakDeg, int beakType) {
        if (b.has('C')) crest(c, b, x, y, r);
        ellipse(c, x, y, r, r, 0, b.head);
        if (b.accent != 0) {
            c.save();
            PATH.reset();
            PATH.addCircle(x, y, r - 0.6f, Path.Direction.CW);
            c.clipPath(PATH);
            if (b.has('K')) blob(c, x - r * 0.1f, y - r * 0.85f, r * 1.05f, r * 0.55f, 0, b.accent);
            if (b.has('T')) blob(c, x + r * 0.45f, y + r * 0.8f, r * 0.62f, r * 0.5f, 0, b.accent);
            if (b.has('M')) blob(c, x + r * 0.15f, y - r * 0.1f, r * 0.85f, r * 0.3f, 0, b.accent);
            c.restore();
        }
        if (b.has('O') && b.accent != 0) {
            for (int i = 0; i < 3; i++) blob(c, x - r * 0.25f + i * r * 0.32f, y - r * 0.95f, r * 0.2f, r * 0.32f, 0, b.accent);
            blob(c, x + r * 0.75f, y + r * 0.75f, r * 0.2f, r * 0.33f, 0, b.accent);
        }
        double a = Math.toRadians(beakDeg);
        float bx = x + (float) Math.cos(a) * r * 0.82f, by = y + (float) Math.sin(a) * r * 0.82f;
        beak(c, b, bx, by, beakLen, beakH, beakDeg, beakType);
        eye(c, b, x + r * 0.3f, y - r * 0.18f, Math.max(2.6f, r * 0.3f));
    }

    private static void crest(Canvas c, Spec b, float x, float y, float r) {
        int col = b.crest != 0 ? b.crest : b.head;
        float[] angles = {-100, -125, -150};
        for (int i = 0; i < angles.length; i++) {
            double a = Math.toRadians(angles[i]);
            float len = r * (1.25f - i * 0.12f);
            float ex = x + (float) Math.cos(a) * (r + len), ey = y + (float) Math.sin(a) * (r + len);
            float sx = x + (float) Math.cos(a) * r * 0.6f, sy = y + (float) Math.sin(a) * r * 0.6f;
            float nx = (float) -Math.sin(a) * r * 0.22f, ny = (float) Math.cos(a) * r * 0.22f;
            PATH.reset();
            PATH.moveTo(sx + nx, sy + ny);
            PATH.quadTo((sx + ex) / 2f + nx * 1.3f, (sy + ey) / 2f + ny * 1.3f, ex, ey);
            PATH.quadTo((sx + ex) / 2f - nx * 1.3f, (sy + ey) / 2f - ny * 1.3f, sx - nx, sy - ny);
            PATH.close();
            fillPath(c, PATH, col);
            if (b.has('W') && b.has('D')) {
                blob(c, ex - (float) Math.cos(a) * 2, ey - (float) Math.sin(a) * 2, 1.8f, 1.8f, 0, Color.rgb(33, 33, 33));
            }
        }
    }

    private static void bodyWithBelly(Canvas c, Spec b, float x, float y, float rx, float ry, float deg,
                                      float bellyDx, float bellyDy) {
        ellipse(c, x, y, rx, ry, deg, b.body);
        if (b.belly != 0 && b.belly != b.body) {
            c.save();
            c.rotate(deg, x, y);
            PATH.reset();
            R.set(x - rx + 0.7f, y - ry + 0.7f, x + rx - 0.7f, y + ry - 0.7f);
            PATH.addOval(R, Path.Direction.CW);
            c.clipPath(PATH);
            blob(c, x + bellyDx, y + bellyDy, rx * 0.75f, ry * 0.7f, 0, b.belly);
            c.restore();
        }
        if (b.has('V') && b.accent != 0) {
            double a = Math.toRadians(deg + 150);
            blob(c, x + (float) Math.cos(a) * rx * 0.8f, y + (float) Math.sin(a) * ry * 0.9f, rx * 0.22f, ry * 0.3f, deg, b.accent);
        }
    }

    // ---- whole birds ----

    /** Side view, used for most kinds. */
    private static void side(Canvas c, Spec b) {
        char k = b.kind;
        boolean longLegs = b.has('J');
        switch (k) {
            case 'k': {
                branch(c, 80);
                legs(c, b, 44, 49, 70, 78, false);
                tail(c, b, 34, 70, 125, 14, 9);
                bodyWithBelly(c, b, 46, 60, 18, 15, -35, 4, 6);
                wing(c, b, 42, 58, 13, 8, -45);
                head(c, b, 62, 38, 14, 24, 8, 8, 0);
                break;
            }
            case 'r': {
                branch(c, 86);
                legs(c, b, 45, 53, 76, 84, false);
                tail(c, b, 42, 76, 105, 16, 13);
                bodyWithBelly(c, b, 48, 58, 18, 24, -12, 4, 5);
                wing(c, b, 44, 60, 12, 20, -80);
                if (b.has('N') && b.accent != 0) blob(c, 52, 39, 13, 5, -10, b.accent);
                head(c, b, 57, 31, 13, 11, 10, 10, 1);
                break;
            }
            case 'p': {
                branch(c, 78);
                tail(c, b, 42, 70, 108, 26, 10);
                legs(c, b, 45, 52, 68, 76, false);
                bodyWithBelly(c, b, 47, 54, 15, 21, -14, 4, 5);
                wing(c, b, 43, 55, 11, 17, -75);
                if (b.has('N') && b.accent != 0) blob(c, 50, 38, 12, 3.2f, -15, b.accent);
                head(c, b, 55, 31, 13, 9, 12, 25, 1);
                break;
            }
            case 'w': {
                legs(c, b, 38, 46, 58, 92, true);
                water(c, 86);
                tail(c, b, 24, 52, 165, 9, 9);
                bodyWithBelly(c, b, 40, 50, 20, 12, -6, 4, 4);
                neck(c, b, 54, 45, 68, 22, 5.5f, 4, 6);
                wing(c, b, 38, 48, 16, 8, -8);
                head(c, b, 70, 20, 8, 22, 4, 8, 0);
                break;
            }
            case 'd': {
                boolean swan = b.count('G') >= 2, goose = b.has('G');
                tail(c, b, 24, 58, 205, 10, 10);
                bodyWithBelly(c, b, 46, 62, 26, 14, 0, 4, 5);
                float hx = swan ? 66 : goose ? 72 : 70, hy = swan ? 22 : goose ? 30 : 42;
                if (swan) {
                    neck(c, b, 62, 54, 70, 38, 6, 4.5f, -10);
                    neck(c, b, 70, 38, 66, 24, 4.5f, 4.5f, 6);
                } else {
                    neck(c, b, 63, 55, hx - 1, hy + 4, 7, 6, 2);
                }
                wing(c, b, 44, 58, 18, 8, -4);
                if (b.has('N') && b.accent != 0 && !swan) blob(c, hx - 4, hy + 10, 6, 2.2f, -30, b.accent);
                head(c, b, hx, hy, swan ? 9 : 11, 12, 6, swan ? 30 : 8, 3);
                water(c, 68);
                break;
            }
            case 'x': {
                float bodyY = 46;
                legs(c, b, 36, 46, 56, 92, true);
                ground(c, 92);
                blob(c, 18, 38, 9, 7, -30, b.belly != 0 ? b.belly : Color.WHITE);
                bodyWithBelly(c, b, 38, bodyY, 23, 15, -6, 0, 0);
                if (b.has('N') && b.accent != 0) blob(c, 58, 30, 3, 6, 10, b.accent);
                neck(c, b, 55, 40, 66, 14, 4.5f, 3, 4);
                wing(c, b, 34, 44, 15, 9, -4);
                head(c, b, 68, 13, 7, 9, 5, 8, 3);
                break;
            }
            case 'u': {
                boolean longNeck = b.has('G');
                ground(c, 90);
                legs(c, b, 44, 54, 82, 88, false);
                bodyWithBelly(c, b, 49, 60, 16, 28, -4, 5, 4);
                wing(c, b, 44, 62, 8, 18, -84);
                if (longNeck) {
                    neck(c, b, 52, 36, 64, 18, 5, 4, -8);
                    head(c, b, 66, 16, 8, 12, 4, 0, 0);
                } else {
                    head(c, b, 53, 28, 13, 10, 7, 4, 0);
                }
                break;
            }
            case 'g': {
                float lift = longLegs ? 10 : 0;
                ground(c, 90);
                if (b.has('P')) peacockFan(c, b, 40, 52 - lift);
                legs(c, b, 42, 51, 68 - lift, 88, longLegs);
                tail(c, b, 26, 52 - lift, b.has('L') ? 190 : 215, 13, 12);
                bodyWithBelly(c, b, 45, 57 - lift, 22, 16, -10, 4, 6);
                neck(c, b, 58, 50 - lift, 64, 36 - lift, 7, 6, 2);
                wing(c, b, 42, 55 - lift, 15, 9, -12);
                if (b.has('N') && b.accent != 0) blob(c, 60, 44 - lift, 5, 7, 20, b.accent);
                head(c, b, 65, 32 - lift, 10, 8, 6, 8, 0);
                break;
            }
            default: {
                branch(c, 80);
                legs(c, b, 44, 51, 68, 78, false);
                tail(c, b, 30, 64, 155, 22, 10);
                bodyWithBelly(c, b, 46, 58, 21, 15, -22, 5, 6);
                wing(c, b, 41, 56, 15, 8.5f, -26);
                head(c, b, 64, 38, 12, 11, 6.5f, 6, 0);
                break;
            }
        }
    }

    private static void flying(Canvas c, Spec b) {
        int far = darker(b.wing, 0.18f);
        float wingScale = b.has('+') ? 1.2f : 1f;
        PATH.reset();
        PATH.moveTo(56, 48);
        PATH.quadTo(58, 26 - 8 * wingScale, 44, 12 - 6 * wingScale);
        PATH.quadTo(46, 32, 42, 50);
        PATH.close();
        fillPath(c, PATH, far);
        tail(c, b, 28, 56, 175, 16, 10);
        bodyWithBelly(c, b, 48, 56, 24, 10, -4, 4, 4);
        PATH.reset();
        PATH.moveTo(56, 52);
        PATH.quadTo(44, 26 - 6 * wingScale, 18 - 8 * wingScale, 14 - 6 * wingScale);
        PATH.quadTo(32, 36, 36, 56);
        PATH.close();
        fillPath(c, PATH, b.wing);
        LINE.setColor(darker(b.wing, 0.3f));
        LINE.setStrokeWidth(1.1f);
        c.drawLine(40, 44, 26, 26, LINE);
        c.drawLine(46, 46, 34, 28, LINE);
        head(c, b, 72, 50, 9, 10, 5.5f, 10, 0);
    }

    private static void hummingbird(Canvas c, Spec b) {
        // A flower to sip from.
        line(c, 88, 52, 84, 96, 2.2f, Color.rgb(76, 175, 80));
        blob(c, 80, 76, 6, 3, -30, Color.rgb(102, 187, 106));
        for (int i = 0; i < 5; i++) {
            double a = Math.toRadians(i * 72);
            ellipse(c, 88 + (float) Math.cos(a) * 6, 46 + (float) Math.sin(a) * 6, 5, 5, 0, Color.rgb(240, 98, 146));
        }
        ellipse(c, 88, 46, 3.5f, 3.5f, 0, Color.rgb(255, 213, 79));
        FILL.setColor(Color.argb(110, 200, 230, 255));
        c.save();
        c.rotate(-60, 40, 46);
        R.set(10, 40, 42, 52);
        c.drawOval(R, FILL);
        c.restore();
        c.save();
        c.rotate(-110, 40, 46);
        R.set(12, 41, 42, 51);
        c.drawOval(R, FILL);
        c.restore();
        tail(c, b, 30, 64, 135, 14, 8);
        bodyWithBelly(c, b, 40, 56, 15, 9, -30, 3, 4);
        head(c, b, 52, 42, 8, 26, 2.6f, 0, 0);
    }

    private static void owl(Canvas c, Spec b) {
        branch(c, 88);
        ellipse(c, 50, 60, 25, 29, 0, b.body);
        blob(c, 50, 66, 16, 19, 0, b.belly != 0 ? b.belly : lighter(b.body, 0.5f));
        LINE.setColor(darker(b.body, 0.1f));
        LINE.setStrokeWidth(1.2f);
        for (int r = 0; r < 3; r++) {
            for (int i = -1; i <= 1; i++) {
                float x = 50 + i * 8 + (r % 2) * 4, y = 58 + r * 8;
                c.drawLine(x - 2, y, x, y + 2, LINE);
                c.drawLine(x, y + 2, x + 2, y, LINE);
            }
        }
        ellipse(c, 27, 60, 8, 19, 10, b.wing);
        ellipse(c, 73, 60, 8, 19, -10, b.wing);
        int toe = b.beak != 0 ? b.beak : Color.rgb(255, 179, 0);
        for (int i = 0; i < 3; i++) {
            ellipse(c, 40 + i * 3, 87, 1.6f, 3, 0, toe);
            ellipse(c, 54 + i * 3, 87, 1.6f, 3, 0, toe);
        }
        if (b.has('C')) {
            int tuft = b.crest != 0 ? b.crest : b.head;
            PATH.reset();
            PATH.moveTo(30, 26);
            PATH.lineTo(28, 8);
            PATH.lineTo(42, 18);
            PATH.close();
            fillPath(c, PATH, tuft);
            PATH.reset();
            PATH.moveTo(70, 26);
            PATH.lineTo(72, 8);
            PATH.lineTo(58, 18);
            PATH.close();
            fillPath(c, PATH, tuft);
        }
        ellipse(c, 50, 32, 23, 19, 0, b.head);
        int disc = b.belly != 0 ? b.belly : lighter(b.head, 0.5f);
        blob(c, 40, 33, 11, 11, 0, disc);
        blob(c, 60, 33, 11, 11, 0, disc);
        int iris = b.accent != 0 ? b.accent : Color.rgb(255, 193, 7);
        for (int s = -1; s <= 1; s += 2) {
            float ex = 50 + s * 10;
            FILL.setColor(Color.WHITE);
            c.drawCircle(ex, 33, 7.5f, FILL);
            FILL.setColor(iris);
            c.drawCircle(ex, 33, 6.2f, FILL);
            FILL.setColor(Color.rgb(15, 10, 15));
            c.drawCircle(ex, 33, 3.8f, FILL);
            FILL.setColor(Color.WHITE);
            c.drawCircle(ex + 1.6f, 31.3f, 1.4f, FILL);
        }
        int bk = b.beak != 0 ? b.beak : Color.rgb(255, 179, 0);
        PATH.reset();
        PATH.moveTo(46.5f, 41);
        PATH.lineTo(53.5f, 41);
        PATH.lineTo(50, 48);
        PATH.close();
        fillPath(c, PATH, bk);
    }
}
