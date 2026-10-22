package com.toddler.chess;

import android.content.Context;
import android.graphics.Canvas;
import android.view.GestureDetector;
import android.view.MotionEvent;

import java.util.List;

/**
 * Shows one big picture at a time and says its name. Tapping the picture says it again,
 * tapping anywhere else (or swiping) moves on to the next card.
 */
final class FlashcardView extends PlayView {
    private final List<Item> items;
    private final GestureDetector gestures;
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
        return dp(34);
    }

    private float itemSize() {
        return Math.min(getWidth() * 0.85f, getHeight() * 0.58f);
    }

    private float itemY() {
        return getHeight() * 0.43f;
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
        float s = itemSize();
        if (dist(x, y, getWidth() / 2f, itemY()) < s * 0.5f) {
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
        c.drawColor(item.background);
        float w = getWidth(), h = getHeight();

        float slide = 0;
        if (slideDir != 0) {
            float t = Math.min(1f, (now() - slideStart) / 280f);
            float ease = 1 - (1 - t) * (1 - t) * (1 - t);
            slide = slideDir * w * (1 - ease);
        }
        long t = now();
        float breathe = 1 + 0.025f * (float) Math.sin(t / 450.0);
        float scale = breathe + 0.18f * bounce(bounceStart, 700);
        float s = itemSize();
        float cx = w / 2f + slide, cy = itemY();

        c.save();
        c.scale(scale, scale, cx, cy);
        item.painter.draw(c, cx, cy, s);
        c.restore();

        float labelSize = Math.min(w, h) * 0.13f;
        Art.label(c, item.label, cx, h * 0.83f, labelSize, item.labelColor);

        float r = buttonRadius();
        float m = dp(16) + r;
        Art.homeButton(c, m, m, r);
        Art.arrowButton(c, m, h - m, r, -1);
        Art.arrowButton(c, w - m, h - m, r, 1);
    }
}
