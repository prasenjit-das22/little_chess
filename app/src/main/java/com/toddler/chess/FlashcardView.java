package com.toddler.chess;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.GestureDetector;
import android.view.MotionEvent;

import java.util.List;

/**
 * Shows one big picture at a time on a card and says its name. Tapping the picture says it
 * again, tapping anywhere else (or swiping) moves on to the next card.
 */
final class FlashcardView extends PlayView {
    private final List<Item> items;
    private final GestureDetector gestures;
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF card = new RectF();
    private int index;
    private long bounceStart;
    private long slideStart;
    private int slideDir;

    FlashcardView(Context context, List<Item> items) {
        super(context);
        this.items = items;
        gestures = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDown(MotionEvent e) {
                return true;
            }

            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                onTap(e.getX(), e.getY());
                return true;
            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float vx, float vy) {
                if (Math.abs(vx) < Math.abs(vy)) return false;
                go(vx < 0 ? 1 : -1);
                return true;
            }
        });
    }

    /** Announces the current card; call once the view is on screen. */
    void start() {
        index = 0;
        slideDir = 0;
        bounceStart = now();
        activity.speaker.say(items.get(index).speech);
    }

    private float buttonRadius() {
        return dp(32);
    }

    private void cardRect(RectF out) {
        float w = getWidth(), h = getHeight();
        float m = dp(16) + buttonRadius();
        float top = m * 2 - dp(8), bottom = h - m * 2 + dp(8);
        float maxW = w - dp(32);
        float cw = Math.min(maxW, (bottom - top) * 0.9f);
        if (w > h) cw = Math.min(w - 4 * m - dp(16), (bottom - top) * 1.3f);
        out.set((w - cw) / 2f, top, (w + cw) / 2f, bottom);
    }

    private float itemSize(RectF r) {
        return Math.min(r.width() * 0.8f, r.height() * 0.62f);
    }

    private float itemY(RectF r) {
        return r.top + r.height() * 0.4f;
    }

    private void onTap(float x, float y) {
        float r = buttonRadius();
        float m = dp(16) + r;
        if (dist(x, y, m, m) < r * 1.3f) {
            activity.showHome();
            return;
        }
        float by = getHeight() - m;
        if (dist(x, y, m, by) < r * 1.3f) {
            go(-1);
            return;
        }
        if (dist(x, y, getWidth() - m, by) < r * 1.3f) {
            go(1);
            return;
        }
        cardRect(card);
        if (card.contains(x, y)) {
            bounceStart = now();
            burst(x, y);
            activity.chimes.ding();
            activity.speaker.say(items.get(index).speech);
        } else {
            go(1);
        }
    }

    private void go(int dir) {
        index = (index + dir + items.size()) % items.size();
        slideDir = dir;
        slideStart = now();
        bounceStart = now() + 250;
        activity.chimes.next();
        activity.speaker.say(items.get(index).speech);
    }

    private static float dist(float x1, float y1, float x2, float y2) {
        return (float) Math.hypot(x1 - x2, y1 - y2);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return gestures.onTouchEvent(event) || super.onTouchEvent(event);
    }

    @Override
    protected void paint(Canvas c) {
        Item item = items.get(index);
        float w = getWidth(), h = getHeight();
        backdrop(c, Art.tint(item.background, 0.35f), item.background);

        float slide = 0;
        if (slideDir != 0) {
            float t = Math.min(1f, (now() - slideStart) / 280f);
            float ease = 1 - (1 - t) * (1 - t) * (1 - t);
            slide = slideDir * w * (1 - ease);
        }

        cardRect(card);
        card.offset(slide, 0);
        float radius = dp(36);
        paint.setColor(Color.argb(40, 60, 30, 90));
        card.offset(0, dp(6));
        c.drawRoundRect(card, radius, radius, paint);
        card.offset(0, -dp(6));
        paint.setColor(Color.WHITE);
        c.drawRoundRect(card, radius, radius, paint);
        float inset = dp(10);
        card.inset(inset, inset);
        paint.setColor(Art.tint(item.background, 0.55f));
        c.drawRoundRect(card, radius - inset, radius - inset, paint);
        card.inset(-inset, -inset);

        float breathe = 1 + 0.025f * (float) Math.sin(now() / 450.0);
        float scale = breathe + 0.18f * bounce(bounceStart, 700);
        float s = itemSize(card);
        float cx = card.centerX(), cy = itemY(card);
        c.save();
        c.scale(scale, scale, cx, cy);
        item.painter.draw(c, cx, cy, s);
        c.restore();

        float labelSize = Math.min(card.width() * 0.16f, card.height() * 0.13f);
        Art.label(c, item.label, cx, card.bottom - card.height() * 0.14f, labelSize, item.labelColor);

        float r = buttonRadius();
        float m = dp(16) + r;
        Art.homeButton(c, m, m, r);
        Art.arrowButton(c, m, h - m, r, -1);
        Art.arrowButton(c, w - m, h - m, r, 1);
        drawProgress(c, w / 2f, h - m);
    }

    private void drawProgress(Canvas c, float cx, float cy) {
        int n = items.size();
        float avail = getWidth() - 4 * (dp(16) + buttonRadius());
        float gap = Math.min(dp(16), avail / n);
        if (gap < dp(6)) return;
        float left = cx - gap * (n - 1) / 2f;
        for (int i = 0; i < n; i++) {
            boolean on = i == index;
            paint.setColor(on ? Color.rgb(120, 90, 200) : Color.argb(90, 120, 90, 200));
            c.drawCircle(left + i * gap, cy, on ? gap * 0.38f : gap * 0.24f, paint);
        }
    }
}
