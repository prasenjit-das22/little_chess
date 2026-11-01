package com.toddler.chess;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.os.Build;

import java.util.HashMap;
import java.util.Map;

/** Shared drawing helpers: shapes, smiley faces, emoji and big outlined text. */
final class Art {
    static final int INK = Color.rgb(60, 36, 90);

    static final int CIRCLE = 0, SQUARE = 1, TRIANGLE = 2, STAR = 3, HEART = 4,
            RECTANGLE = 5, OVAL = 6, DIAMOND = 7;

    private static final Paint FILL = new Paint(Paint.ANTI_ALIAS_FLAG);
    private static final Paint STROKE = new Paint(Paint.ANTI_ALIAS_FLAG);
    private static final Paint TEXT = new Paint(Paint.ANTI_ALIAS_FLAG);
    private static final Paint BITMAP = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    private static final Map<String, Bitmap> EMOJI = new HashMap<>();
    private static final RectF RECT = new RectF();

    static {
        STROKE.setStyle(Paint.Style.STROKE);
        STROKE.setStrokeJoin(Paint.Join.ROUND);
        STROKE.setStrokeCap(Paint.Cap.ROUND);
        TEXT.setTextAlign(Paint.Align.CENTER);
        TEXT.setTypeface(Typeface.create("sans-serif-black", Typeface.NORMAL));
    }

    private Art() {}

    /** Mixes a colour towards white; amount 0 = unchanged, 1 = white. */
    static int tint(int color, float amount) {
        int r = (int) (Color.red(color) + (255 - Color.red(color)) * amount);
        int g = (int) (Color.green(color) + (255 - Color.green(color)) * amount);
        int b = (int) (Color.blue(color) + (255 - Color.blue(color)) * amount);
        return Color.rgb(r, g, b);
    }

    static boolean isDark(int color) {
        return Color.red(color) * 0.3 + Color.green(color) * 0.59 + Color.blue(color) * 0.11 < 90;
    }

    static Path shapePath(int shape, float cx, float cy, float s) {
        Path p = new Path();
        float h = s / 2f;
        switch (shape) {
            case CIRCLE:
                p.addCircle(cx, cy, h * 0.9f, Path.Direction.CW);
                break;
            case SQUARE:
                RECT.set(cx - h * 0.8f, cy - h * 0.8f, cx + h * 0.8f, cy + h * 0.8f);
                p.addRoundRect(RECT, s * 0.06f, s * 0.06f, Path.Direction.CW);
                break;
            case TRIANGLE:
                p.moveTo(cx, cy - h * 0.85f);
                p.lineTo(cx + h * 0.95f, cy + h * 0.75f);
                p.lineTo(cx - h * 0.95f, cy + h * 0.75f);
                p.close();
                break;
            case STAR:
                star(p, cx, cy + h * 0.06f, h * 0.95f, h * 0.42f);
                break;
            case HEART:
                p.moveTo(cx, cy + h * 0.85f);
                p.cubicTo(cx - h * 1.25f, cy, cx - h * 0.75f, cy - h * 0.95f, cx, cy - h * 0.4f);
                p.cubicTo(cx + h * 0.75f, cy - h * 0.95f, cx + h * 1.25f, cy, cx, cy + h * 0.85f);
                p.close();
                break;
            case RECTANGLE:
                RECT.set(cx - h, cy - h * 0.55f, cx + h, cy + h * 0.55f);
                p.addRoundRect(RECT, s * 0.05f, s * 0.05f, Path.Direction.CW);
                break;
            case OVAL:
                RECT.set(cx - h * 0.62f, cy - h * 0.92f, cx + h * 0.62f, cy + h * 0.92f);
                p.addOval(RECT, Path.Direction.CW);
                break;
            case DIAMOND:
                p.moveTo(cx, cy - h * 0.95f);
                p.lineTo(cx + h * 0.7f, cy);
                p.lineTo(cx, cy + h * 0.95f);
                p.lineTo(cx - h * 0.7f, cy);
                p.close();
                break;
            default:
                break;
        }
        return p;
    }

    static void star(Path p, float cx, float cy, float outer, float inner) {
        for (int i = 0; i < 10; i++) {
            double a = -Math.PI / 2 + i * Math.PI / 5;
            float r = (i % 2 == 0) ? outer : inner;
            float x = cx + (float) (Math.cos(a) * r);
            float y = cy + (float) (Math.sin(a) * r);
            if (i == 0) p.moveTo(x, y);
            else p.lineTo(x, y);
        }
        p.close();
    }

    /** A filled, outlined shape with a happy face on it. */
    static void shape(Canvas c, int shape, int color, float cx, float cy, float s) {
        Path p = shapePath(shape, cx, cy, s);
        // Candy look: a radial gradient lit from the top left, plus a soft shine.
        FILL.setShader(new RadialGradient(cx - s * 0.2f, cy - s * 0.25f, s * 0.75f,
                tint(color, 0.45f), color, Shader.TileMode.CLAMP));
        c.drawPath(p, FILL);
        FILL.setShader(null);
        c.save();
        c.clipPath(p);
        FILL.setColor(Color.argb(90, 255, 255, 255));
        RECT.set(cx - s * 0.36f, cy - s * 0.42f, cx - s * 0.02f, cy - s * 0.24f);
        c.rotate(-25, RECT.centerX(), RECT.centerY());
        c.drawOval(RECT, FILL);
        c.restore();
        STROKE.setColor(isDark(color) ? Color.rgb(120, 120, 130) : INK);
        STROKE.setStrokeWidth(s * 0.03f);
        c.drawPath(p, STROKE);
        float faceY = cy + (shape == TRIANGLE ? s * 0.12f : shape == HEART ? -s * 0.02f : 0);
        face(c, cx, faceY, s * (shape == DIAMOND || shape == TRIANGLE ? 0.32f : 0.42f), color);
    }

    /** Two eyes, rosy cheeks and a smile, sized to fit inside a box of width s. */
    static void face(Canvas c, float cx, float cy, float s, int on) {
        int ink = isDark(on) ? Color.WHITE : INK;
        FILL.setColor(ink);
        float eyeDx = s * 0.22f, eyeY = cy - s * 0.1f, eyeR = s * 0.07f;
        c.drawCircle(cx - eyeDx, eyeY, eyeR, FILL);
        c.drawCircle(cx + eyeDx, eyeY, eyeR, FILL);
        FILL.setColor(Color.WHITE);
        c.drawCircle(cx - eyeDx + eyeR * 0.35f, eyeY - eyeR * 0.35f, eyeR * 0.35f, FILL);
        c.drawCircle(cx + eyeDx + eyeR * 0.35f, eyeY - eyeR * 0.35f, eyeR * 0.35f, FILL);
        FILL.setColor(Color.argb(90, 255, 80, 120));
        c.drawCircle(cx - s * 0.36f, cy + s * 0.08f, s * 0.08f, FILL);
        c.drawCircle(cx + s * 0.36f, cy + s * 0.08f, s * 0.08f, FILL);
        STROKE.setColor(ink);
        STROKE.setStrokeWidth(s * 0.05f);
        RECT.set(cx - s * 0.18f, cy - s * 0.05f, cx + s * 0.18f, cy + s * 0.22f);
        c.drawArc(RECT, 20, 140, false, STROKE);
    }

    /** True when the device font can show this emoji (always assumed before API 23). */
    static boolean hasEmoji(String emoji) {
        return Build.VERSION.SDK_INT < 23 || TEXT.hasGlyph(emoji);
    }

    /**
     * Draws an emoji by rendering it once into a bitmap and scaling that. Very large emoji
     * text does not render reliably on hardware-accelerated canvases, bitmaps always do.
     */
    static void emoji(Canvas c, String emoji, float cx, float cy, float size) {
        Bitmap b = EMOJI.get(emoji);
        if (b == null) {
            int px = 256;
            b = Bitmap.createBitmap(px, px, Bitmap.Config.ARGB_8888);
            Canvas bc = new Canvas(b);
            Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
            p.setTextAlign(Paint.Align.CENTER);
            p.setTextSize(px * 0.8f);
            Paint.FontMetrics fm = p.getFontMetrics();
            float baseline = px / 2f - (fm.ascent + fm.descent) / 2f;
            bc.drawText(emoji, px / 2f, baseline, p);
            EMOJI.put(emoji, b);
        }
        RECT.set(cx - size / 2f, cy - size / 2f, cx + size / 2f, cy + size / 2f);
        c.drawBitmap(b, null, RECT, BITMAP);
    }

    /** A round badge with the first letter of a word, for pictures the phone cannot show. */
    static void letterBadge(Canvas c, String word, float cx, float cy, float s) {
        String first = word.isEmpty() ? "?" : word.substring(0, Character.charCount(word.codePointAt(0)));
        FILL.setShader(new RadialGradient(cx - s * 0.15f, cy - s * 0.2f, s * 0.7f,
                Color.rgb(255, 236, 179), Color.rgb(255, 179, 0), Shader.TileMode.CLAMP));
        c.drawCircle(cx, cy, s / 2f, FILL);
        FILL.setShader(null);
        STROKE.setColor(INK);
        STROKE.setStrokeWidth(s * 0.025f);
        c.drawCircle(cx, cy, s / 2f, STROKE);
        label(c, first, cx, cy, s * 0.55f, Color.rgb(216, 27, 96));
    }

    /** Big bold text with a white halo so it reads on any background. */
    static void label(Canvas c, String text, float cx, float cy, float size, int color) {
        TEXT.setTextSize(size);
        float maxWidth = c.getWidth() * 0.92f;
        float w = TEXT.measureText(text);
        if (w > maxWidth) {
            size *= maxWidth / w;
            TEXT.setTextSize(size);
        }
        float baseline = cy - (TEXT.ascent() + TEXT.descent()) / 2f;
        TEXT.setStyle(Paint.Style.STROKE);
        TEXT.setStrokeWidth(size * 0.16f);
        TEXT.setStrokeJoin(Paint.Join.ROUND);
        TEXT.setColor(isDark(color) ? Color.rgb(250, 250, 250) : Color.WHITE);
        c.drawText(text, cx, baseline, TEXT);
        TEXT.setStyle(Paint.Style.FILL);
        TEXT.setColor(color);
        c.drawText(text, cx, baseline, TEXT);
        if (color == Color.WHITE) {
            TEXT.setStyle(Paint.Style.STROKE);
            TEXT.setStrokeWidth(size * 0.025f);
            TEXT.setColor(Color.rgb(150, 150, 160));
            c.drawText(text, cx, baseline, TEXT);
            TEXT.setStyle(Paint.Style.FILL);
        }
    }

    /** Plain text without a halo. */
    static void text(Canvas c, String text, float cx, float cy, float size, int color) {
        text(c, text, cx, cy, size, color, Float.MAX_VALUE);
    }

    /** Plain text, shrunk if needed to fit maxWidth. */
    static void text(Canvas c, String text, float cx, float cy, float size, int color, float maxWidth) {
        TEXT.setTextSize(size);
        float w = TEXT.measureText(text);
        if (w > maxWidth) TEXT.setTextSize(size * maxWidth / w);
        TEXT.setStyle(Paint.Style.FILL);
        TEXT.setColor(color);
        c.drawText(text, cx, cy - (TEXT.ascent() + TEXT.descent()) / 2f, TEXT);
    }

    /** A round button with a small house on it. */
    static void homeButton(Canvas c, float cx, float cy, float r) {
        roundButton(c, cx, cy, r);
        float s = r * 0.55f;
        Path p = new Path();
        p.moveTo(cx, cy - s);
        p.lineTo(cx + s, cy - s * 0.05f);
        p.lineTo(cx + s * 0.7f, cy - s * 0.05f);
        p.lineTo(cx + s * 0.7f, cy + s * 0.8f);
        p.lineTo(cx - s * 0.7f, cy + s * 0.8f);
        p.lineTo(cx - s * 0.7f, cy - s * 0.05f);
        p.lineTo(cx - s, cy - s * 0.05f);
        p.close();
        FILL.setColor(Color.rgb(255, 122, 184));
        c.drawPath(p, FILL);
        FILL.setColor(Color.WHITE);
        RECT.set(cx - s * 0.22f, cy + s * 0.25f, cx + s * 0.22f, cy + s * 0.8f);
        c.drawRect(RECT, FILL);
    }

    /** A round button with a triangle arrow; dir is -1 for left, 1 for right. */
    static void arrowButton(Canvas c, float cx, float cy, float r, int dir) {
        roundButton(c, cx, cy, r);
        float s = r * 0.45f;
        Path p = new Path();
        p.moveTo(cx + dir * s, cy);
        p.lineTo(cx - dir * s * 0.6f, cy - s);
        p.lineTo(cx - dir * s * 0.6f, cy + s);
        p.close();
        FILL.setColor(Color.rgb(120, 90, 200));
        c.drawPath(p, FILL);
    }

    /** A round button with two crossing arrows, to shuffle the cards. */
    static void shuffleButton(Canvas c, float cx, float cy, float r) {
        roundButton(c, cx, cy, r);
        float s = r * 0.5f;
        int col = Color.rgb(255, 143, 0);
        STROKE.setColor(col);
        STROKE.setStrokeWidth(r * 0.13f);
        Path p = new Path();
        p.moveTo(cx - s, cy - s * 0.55f);
        p.cubicTo(cx - s * 0.1f, cy - s * 0.55f, cx + s * 0.1f, cy + s * 0.55f, cx + s * 0.7f, cy + s * 0.55f);
        c.drawPath(p, STROKE);
        p.reset();
        p.moveTo(cx - s, cy + s * 0.55f);
        p.cubicTo(cx - s * 0.1f, cy + s * 0.55f, cx + s * 0.1f, cy - s * 0.55f, cx + s * 0.7f, cy - s * 0.55f);
        c.drawPath(p, STROKE);
        FILL.setColor(col);
        for (int k = -1; k <= 1; k += 2) {
            p.reset();
            p.moveTo(cx + s * 1.1f, cy + k * s * 0.55f);
            p.lineTo(cx + s * 0.6f, cy + k * s * 0.55f - s * 0.35f);
            p.lineTo(cx + s * 0.6f, cy + k * s * 0.55f + s * 0.35f);
            p.close();
            c.drawPath(p, FILL);
        }
    }

    /** A round button with a circular "start again" arrow. */
    static void resetButton(Canvas c, float cx, float cy, float r) {
        roundButton(c, cx, cy, r);
        float s = r * 0.48f;
        STROKE.setColor(Color.rgb(67, 160, 71));
        STROKE.setStrokeWidth(r * 0.16f);
        RECT.set(cx - s, cy - s, cx + s, cy + s);
        c.drawArc(RECT, -60, 290, false, STROKE);
        double a = Math.toRadians(-60);
        float ex = cx + (float) Math.cos(a) * s, ey = cy + (float) Math.sin(a) * s;
        Path p = new Path();
        p.moveTo(ex + r * 0.3f, ey - r * 0.05f);
        p.lineTo(ex - r * 0.1f, ey - r * 0.3f);
        p.lineTo(ex - r * 0.05f, ey + r * 0.2f);
        p.close();
        FILL.setColor(Color.rgb(67, 160, 71));
        c.drawPath(p, FILL);
    }

    private static void roundButton(Canvas c, float cx, float cy, float r) {
        FILL.setColor(Color.argb(60, 0, 0, 0));
        c.drawCircle(cx, cy + r * 0.08f, r, FILL);
        FILL.setColor(Color.WHITE);
        c.drawCircle(cx, cy, r, FILL);
    }
}
