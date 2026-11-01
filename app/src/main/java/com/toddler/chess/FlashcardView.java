package com.toddler.chess;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.GestureDetector;
import android.view.MotionEvent;

import java.util.Collections;
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
        speak();
    }

    private void speak() {
        Item item = items.get(index);
        activity.speaker.say(item.speech, item.speechLang);
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
        if (items.size() > 2 && dist(x, y, getWidth() - m, m) < r * 1.3f) {
            Collections.shuffle(items);
            index = 0;
            slideDir = 1;
            slideStart = now();
            bounceStart = now() + 250;
            burst(getWidth() - m, m);
            activity.chimes.ding();
            speak();
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
            speak();
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
        speak();
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
        boolean hasText = item.text != null;
        float s = hasText ? Math.min(card.width() * 0.4f, card.height() * 0.24f) : itemSize(card);
        float cx = card.centerX(), cy = hasText ? card.top + card.height() * 0.17f : itemY(card);
        c.save();
        c.scale(scale, scale, cx, cy);
        item.painter.draw(c, cx, cy, s);
        c.restore();

        boolean hasSub = item.sub != null && !item.sub.isEmpty() && !item.sub.equals(item.label);
        if (hasText) {
            float labelSize = Math.min(card.width() * 0.09f, card.height() * 0.06f);
            float ly = card.top + card.height() * 0.345f;
            Art.label(c, item.label, cx, ly, labelSize, item.labelColor);
            if (hasSub) {
                Art.text(c, item.sub, cx, ly + labelSize * 0.95f, labelSize * 0.5f,
                        Color.rgb(130, 100, 150), card.width() * 0.85f);
            }
            drawText(c, item.text, card, card.top + card.height() * 0.45f, card.bottom - card.height() * 0.04f);
        } else {
            float labelSize = Math.min(card.width() * 0.16f, card.height() * 0.12f);
            float ly = card.bottom - card.height() * (hasSub ? 0.17f : 0.14f);
            Art.label(c, item.label, cx, ly, labelSize, item.labelColor);
            if (hasSub) {
                Art.text(c, item.sub, cx, card.bottom - card.height() * 0.065f, labelSize * 0.42f,
                        Color.rgb(130, 100, 150), card.width() * 0.85f);
            }
        }

        float r = buttonRadius();
        float m = dp(16) + r;
        Art.homeButton(c, m, m, r);
        Art.arrowButton(c, m, h - m, r, -1);
        Art.arrowButton(c, w - m, h - m, r, 1);
        if (items.size() > 2) Art.shuffleButton(c, w - m, m, r);
        drawProgress(c, w / 2f, h - m);
    }

    /** The lines of a rhyme, as big as fits between top and bottom. */
    private void drawText(Canvas c, String text, RectF card, float top, float bottom) {
        String[] lines = text.split("\n");
        float lineH = Math.min((bottom - top) / lines.length, card.width() * 0.075f);
        float size = lineH * 0.72f;
        float y = top + ((bottom - top) - lineH * lines.length) / 2f + lineH / 2f;
        for (String line : lines) {
            Art.text(c, line, card.centerX(), y, size, Art.INK, card.width() * 0.88f);
            y += lineH;
        }
    }

    private void drawProgress(Canvas c, float cx, float cy) {
        int n = items.size();
        float avail = getWidth() - 4 * (dp(16) + buttonRadius());
        float gap = Math.min(dp(16), avail / n);
        if (gap < dp(6) || n > 20) {
            // Too many for dots: show "12 / 100" instead.
            String count = Lang.digit(index + 1) + " / " + Lang.digit(n);
            paint.setColor(Color.argb(170, 255, 255, 255));
            float tw = Math.min(avail, dp(130));
            c.drawRoundRect(cx - tw / 2f, cy - dp(20), cx + tw / 2f, cy + dp(20), dp(20), dp(20), paint);
            Art.text(c, count, cx, cy, dp(20), Color.rgb(120, 90, 200), tw * 0.9f);
            return;
        }
        float left = cx - gap * (n - 1) / 2f;
        for (int i = 0; i < n; i++) {
            boolean on = i == index;
            paint.setColor(on ? Color.rgb(120, 90, 200) : Color.argb(90, 120, 90, 200));
            c.drawCircle(left + i * gap, cy, on ? gap * 0.38f : gap * 0.24f, paint);
        }
    }
}
