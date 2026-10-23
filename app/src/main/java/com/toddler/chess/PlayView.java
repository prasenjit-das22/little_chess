package com.toddler.chess;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Shader;
import android.os.SystemClock;
import android.view.View;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/** Base view that redraws every frame and can burst sparkles out of a point. */
abstract class PlayView extends View {
    private static final int[] SPARKLE_COLORS = {
            Color.rgb(255, 202, 40), Color.rgb(255, 112, 167), Color.rgb(124, 77, 255),
            Color.rgb(41, 182, 246), Color.rgb(102, 187, 106), Color.rgb(255, 138, 101),
    };

    private static final class Sparkle {
        float x, y, vx, vy, spin;
        int color;
        long born;
    }

    protected final MainActivity activity;
    private final List<Sparkle> sparkles = new ArrayList<>();
    private final Paint sparklePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path starPath = new Path();
    private final Random random = new Random();
    private final float density;
    private final Paint backdropPaint = new Paint();
    private final Paint bubblePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final float[] bubbles = new float[24]; // x, y, radius, speed as fractions of the view
    private int backdropTop, backdropBottom, backdropHeight;

    PlayView(Context context) {
        super(context);
        activity = (MainActivity) context;
        density = getResources().getDisplayMetrics().density;
        for (int i = 0; i < bubbles.length; i += 4) {
            bubbles[i] = random.nextFloat();
            bubbles[i + 1] = random.nextFloat();
            bubbles[i + 2] = 0.04f + random.nextFloat() * 0.08f;
            bubbles[i + 3] = 0.01f + random.nextFloat() * 0.02f;
        }
    }

    /** A soft vertical gradient with big translucent bubbles drifting upwards. */
    void backdrop(Canvas c, int top, int bottom) {
        int w = getWidth(), h = getHeight();
        if (top != backdropTop || bottom != backdropBottom || h != backdropHeight) {
            backdropPaint.setShader(new LinearGradient(0, 0, 0, h, top, bottom, Shader.TileMode.CLAMP));
            backdropTop = top;
            backdropBottom = bottom;
            backdropHeight = h;
        }
        c.drawRect(0, 0, w, h, backdropPaint);
        float t = now() / 1000f;
        float unit = Math.min(w, h);
        bubblePaint.setColor(Color.argb(60, 255, 255, 255));
        for (int i = 0; i < bubbles.length; i += 4) {
            float r = bubbles[i + 2] * unit;
            float y = (bubbles[i + 1] - t * bubbles[i + 3]) % 1f;
            if (y < 0) y += 1f;
            float py = -r + y * (h + 2 * r);
            float px = bubbles[i] * w + (float) Math.sin(t * 0.7 + i) * r * 0.5f;
            c.drawCircle(px, py, r, bubblePaint);
        }
    }

    float dp(float v) {
        return v * density;
    }

    static long now() {
        return SystemClock.uptimeMillis();
    }

    /** A bounce that peaks quickly and settles: 0 at rest, about 1 at the top. */
    static float bounce(long start, long duration) {
        float t = (now() - start) / (float) duration;
        if (t < 0 || t > 1) return 0;
        return (float) (Math.sin(t * Math.PI * 3) * (1 - t));
    }

    void burst(float x, float y) {
        long t = now();
        for (int i = 0; i < 16; i++) {
            Sparkle s = new Sparkle();
            double a = random.nextDouble() * Math.PI * 2;
            float speed = dp(180 + random.nextInt(260));
            s.x = x;
            s.y = y;
            s.vx = (float) Math.cos(a) * speed;
            s.vy = (float) Math.sin(a) * speed - dp(120);
            s.spin = random.nextFloat() * 6 - 3;
            s.color = SPARKLE_COLORS[random.nextInt(SPARKLE_COLORS.length)];
            s.born = t;
            sparkles.add(s);
        }
    }

    @Override
    protected final void onDraw(Canvas canvas) {
        paint(canvas);
        drawSparkles(canvas);
        postInvalidateOnAnimation();
    }

    protected abstract void paint(Canvas canvas);

    private void drawSparkles(Canvas c) {
        long t = now();
        for (int i = sparkles.size() - 1; i >= 0; i--) {
            Sparkle s = sparkles.get(i);
            float age = (t - s.born) / 1000f;
            if (age > 1f) {
                sparkles.remove(i);
                continue;
            }
            float x = s.x + s.vx * age;
            float y = s.y + s.vy * age + dp(400) * age * age;
            float r = dp(14) * (1 - age * 0.6f);
            sparklePaint.setColor(s.color);
            sparklePaint.setAlpha((int) (255 * (1 - age)));
            starPath.reset();
            Art.star(starPath, 0, 0, r, r * 0.45f);
            c.save();
            c.translate(x, y);
            c.rotate(s.spin * age * 180);
            c.drawPath(starPath, sparklePaint);
            c.restore();
        }
    }
}
