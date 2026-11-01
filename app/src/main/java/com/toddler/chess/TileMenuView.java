package com.toddler.chess;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.OverScroller;

/**
 * A fixed header over a scrolling grid of big colourful tiles. Tapping a tile bounces it, says
 * its name and opens it.
 */
abstract class TileMenuView extends PlayView {
    protected final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint tilePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    protected final RectF rect = new RectF();
    private final GestureDetector gestures;
    private final OverScroller scroller;
    private Shader[] tileShaders;
    private float scrollY;
    private int pressed = -1;
    private long pressedAt;

    TileMenuView(Context context) {
        super(context);
        scroller = new OverScroller(context);
        gestures = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDown(MotionEvent e) {
                scroller.forceFinished(true);
                return true;
            }

            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                if (e.getY() < headerHeight()) {
                    onHeaderTap(e.getX(), e.getY());
                    return true;
                }
                int tile = tileAt(e.getX(), e.getY());
                if (tile >= 0) open(tile, e.getX(), e.getY());
                return true;
            }

            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float dx, float dy) {
                scrollY = clamp(scrollY + dy);
                return true;
            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float vx, float vy) {
                scroller.fling(0, (int) scrollY, 0, (int) -vy, 0, 0, 0, (int) maxScroll());
                return true;
            }

            @Override
            public void onLongPress(MotionEvent e) {
                if (e.getY() < headerHeight()) onHeaderLongPress(e.getX(), e.getY());
            }
        });
    }

    abstract int tileCount();

    abstract String tileTitle(int i);

    abstract int tileColor(int i);

    abstract void drawTileIcon(Canvas c, int i, float cx, float cy, float s);

    abstract void openTile(int i);

    abstract float headerHeight();

    abstract void paintHeader(Canvas c);

    void onHeaderTap(float x, float y) {}

    void onHeaderLongPress(float x, float y) {}

    abstract void paintBackdrop(Canvas c);

    private int columns() {
        float w = getWidth() / getResources().getDisplayMetrics().density;
        if (getWidth() > getHeight()) return w > 900 ? 6 : 5;
        return w > 600 ? 4 : 3;
    }

    private float pad() {
        return dp(12);
    }

    private float tileWidth() {
        int cols = columns();
        return (getWidth() - pad() * (cols + 1)) / cols;
    }

    private float tileHeight() {
        int cols = columns();
        int rows = (tileCount() + cols - 1) / cols;
        float fit = (getHeight() - headerHeight() - pad() * (rows + 1)) / rows;
        float tw = tileWidth();
        return fit >= tw * 0.8f ? Math.min(fit, tw * 1.15f) : tw * 0.9f;
    }

    private float maxScroll() {
        int cols = columns();
        int rows = (tileCount() + cols - 1) / cols;
        float content = pad() + rows * (tileHeight() + pad());
        return Math.max(0, content - (getHeight() - headerHeight()));
    }

    private float clamp(float y) {
        return Math.max(0, Math.min(maxScroll(), y));
    }

    /** Tile rectangle in content coordinates (before scrolling). */
    private void tileRect(int i, RectF out) {
        int cols = columns();
        float tw = tileWidth(), th = tileHeight(), pad = pad();
        int col = i % cols, row = i / cols;
        float l = pad + col * (tw + pad), t = headerHeight() + pad + row * (th + pad);
        out.set(l, t, l + tw, t + th);
    }

    private int tileAt(float x, float y) {
        RectF r = new RectF();
        for (int i = 0; i < tileCount(); i++) {
            tileRect(i, r);
            r.offset(0, -scrollY);
            if (r.contains(x, y)) return i;
        }
        return -1;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        tileShaders = new Shader[tileCount()];
        RectF r = new RectF();
        for (int i = 0; i < tileCount(); i++) {
            tileRect(i, r);
            tileShaders[i] = new LinearGradient(0, r.top, 0, r.bottom,
                    Art.tint(tileColor(i), 0.35f), tileColor(i), Shader.TileMode.CLAMP);
        }
        scrollY = clamp(scrollY);
    }

    private void open(final int tile, float x, float y) {
        pressed = tile;
        pressedAt = now();
        burst(x, y);
        activity.chimes.ding();
        activity.speaker.say(tileTitle(tile));
        // Let the bounce play before switching screens.
        postDelayed(() -> openTile(tile), 550);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return gestures.onTouchEvent(event) || super.onTouchEvent(event);
    }

    @Override
    protected void paint(Canvas c) {
        if (scroller.computeScrollOffset()) scrollY = clamp(scroller.getCurrY());
        paintBackdrop(c);
        float top = headerHeight();
        c.save();
        c.clipRect(0, top, getWidth(), getHeight());
        c.translate(0, -scrollY);
        for (int i = 0; i < tileCount(); i++) {
            tileRect(i, rect);
            if (rect.bottom < scrollY + top || rect.top > scrollY + getHeight()) continue;
            drawTile(c, i);
        }
        c.restore();
        paintHeader(c);
        if (scrollY < maxScroll() - dp(4)) drawMoreHint(c);
    }

    private void drawTile(Canvas c, int i) {
        float scale = 1;
        if (i == pressed) scale += 0.08f * bounce(pressedAt, 500);
        c.save();
        c.scale(scale, scale, rect.centerX(), rect.centerY());
        float radius = Math.min(rect.width(), rect.height()) * 0.2f;
        paint.setColor(Color.argb(50, 80, 20, 60));
        rect.offset(0, dp(5));
        c.drawRoundRect(rect, radius, radius, paint);
        rect.offset(0, -dp(5));
        if (tileShaders != null && i < tileShaders.length) tilePaint.setShader(tileShaders[i]);
        c.drawRoundRect(rect, radius, radius, tilePaint);
        paint.setColor(Color.argb(70, 255, 255, 255));
        c.drawRoundRect(rect.left + dp(6), rect.top + dp(5), rect.right - dp(6),
                rect.top + rect.height() * 0.35f, radius * 0.8f, radius * 0.8f, paint);

        float badge = Math.min(rect.width(), rect.height() * 0.78f) * 0.36f;
        float icx = rect.centerX(), icy = rect.top + rect.height() * 0.42f;
        paint.setColor(Color.argb(200, 255, 255, 255));
        c.drawCircle(icx, icy, badge, paint);
        drawTileIcon(c, i, icx, icy, badge * 1.55f);
        float textSize = Math.min(rect.height() * 0.12f, dp(20));
        Art.text(c, tileTitle(i), rect.centerX(), rect.bottom - rect.height() * 0.13f,
                textSize, Art.INK, rect.width() * 0.9f);
        c.restore();
    }

    /** A bouncing arrow at the bottom: there is more to see below. */
    private void drawMoreHint(Canvas c) {
        float r = dp(20);
        float cx = getWidth() / 2f;
        float cy = getHeight() - dp(28) + (float) Math.sin(now() / 250.0) * dp(4);
        paint.setColor(Color.argb(60, 0, 0, 0));
        c.drawCircle(cx, cy + dp(2), r, paint);
        paint.setColor(Color.WHITE);
        c.drawCircle(cx, cy, r, paint);
        Path p = new Path();
        p.moveTo(cx - r * 0.45f, cy - r * 0.15f);
        p.lineTo(cx + r * 0.45f, cy - r * 0.15f);
        p.lineTo(cx, cy + r * 0.38f);
        p.close();
        paint.setColor(Color.rgb(216, 27, 96));
        c.drawPath(p, paint);
    }
}
