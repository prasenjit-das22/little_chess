package com.toddler.chess;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.GestureDetector;
import android.view.MotionEvent;

/** The menu: big colourful tiles, one per activity. Long-press the title to set a name. */
final class HomeView extends PlayView {
    private static final int COLORS = 0, SHAPES = 1, ANIMALS = 2, THINGS = 3,
            NUMBERS = 4, PIECES = 5, BOARD = 6, SURPRISE = 7;
    private static final String[] TITLES = {
            "Colors", "Shapes", "Animals", "Things", "Numbers", "Chess pieces", "Chess board", "Surprise!",
    };
    private static final int[] TILE_COLORS = {
            Color.rgb(255, 205, 210), Color.rgb(187, 222, 251), Color.rgb(200, 230, 201),
            Color.rgb(255, 236, 179), Color.rgb(225, 190, 231), Color.rgb(215, 204, 200),
            Color.rgb(255, 224, 178), Color.rgb(178, 235, 242),
    };

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF rect = new RectF();
    private final GestureDetector gestures;
    private int pressed = -1;
    private long pressedAt;

    HomeView(Context context) {
        super(context);
        gestures = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDown(MotionEvent e) {
                return true;
            }

            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                int tile = tileAt(e.getX(), e.getY());
                if (tile >= 0) open(tile, e.getX(), e.getY());
                return true;
            }

            @Override
            public void onLongPress(MotionEvent e) {
                if (e.getY() < titleHeight()) activity.askChildName();
            }
        });
    }

    private float titleHeight() {
        return dp(96);
    }

    private int columns() {
        return getWidth() > getHeight() ? 4 : 2;
    }

    private void tileRect(int i, RectF out) {
        int cols = columns();
        int rows = (TITLES.length + cols - 1) / cols;
        float pad = dp(12);
        float top = titleHeight();
        float tw = (getWidth() - pad * (cols + 1)) / cols;
        float th = (getHeight() - top - pad * (rows + 1)) / rows;
        int col = i % cols, row = i / cols;
        float l = pad + col * (tw + pad), t = top + pad + row * (th + pad);
        out.set(l, t, l + tw, t + th);
    }

    private int tileAt(float x, float y) {
        RectF r = new RectF();
        for (int i = 0; i < TITLES.length; i++) {
            tileRect(i, r);
            if (r.contains(x, y)) return i;
        }
        return -1;
    }

    private void open(final int tile, float x, float y) {
        pressed = tile;
        pressedAt = now();
        burst(x, y);
        activity.chimes.ding();
        // Let the bounce play before switching screens.
        postDelayed(() -> {
            switch (tile) {
                case COLORS: activity.openDeck(Decks.colors()); break;
                case SHAPES: activity.openDeck(Decks.shapes()); break;
                case ANIMALS: activity.openDeck(Decks.animals()); break;
                case THINGS: activity.openDeck(Decks.things()); break;
                case NUMBERS: activity.openDeck(Decks.numbers()); break;
                case PIECES: activity.openDeck(Decks.pieces()); break;
                case BOARD: activity.openBoard(); break;
                default: activity.openDeck(Decks.surprise()); break;
            }
        }, 350);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return gestures.onTouchEvent(event) || super.onTouchEvent(event);
    }

    @Override
    protected void paint(Canvas c) {
        c.drawColor(Color.rgb(255, 248, 240));
        float w = getWidth();
        String name = activity.childName();
        String title = name.isEmpty() ? "Let's play!" : "Hi, " + name + "!";
        Art.label(c, title, w / 2f, titleHeight() * 0.45f, dp(44), Color.rgb(236, 64, 122));
        if (name.isEmpty()) {
            Art.text(c, "(grown-ups: long-press here to add a name)", w / 2f, titleHeight() * 0.85f,
                    dp(12), Color.rgb(170, 150, 160));
        }

        for (int i = 0; i < TITLES.length; i++) {
            tileRect(i, rect);
            float scale = 1;
            if (i == pressed) scale += 0.08f * bounce(pressedAt, 500);
            c.save();
            c.scale(scale, scale, rect.centerX(), rect.centerY());
            paint.setColor(Color.argb(40, 0, 0, 0));
            rect.offset(0, dp(4));
            c.drawRoundRect(rect, dp(28), dp(28), paint);
            rect.offset(0, -dp(4));
            paint.setColor(TILE_COLORS[i]);
            c.drawRoundRect(rect, dp(28), dp(28), paint);

            float iconSize = Math.min(rect.width(), rect.height()) * 0.62f;
            float icx = rect.centerX(), icy = rect.top + rect.height() * 0.42f;
            drawIcon(c, i, icx, icy, iconSize);
            float textSize = Math.min(rect.height() * 0.13f, rect.width() * 0.13f);
            Art.text(c, TITLES[i], rect.centerX(), rect.bottom - rect.height() * 0.13f, textSize, Art.INK);
            c.restore();
        }
    }

    private void drawIcon(Canvas c, int tile, float cx, float cy, float s) {
        switch (tile) {
            case COLORS:
                Art.shape(c, Art.CIRCLE, Color.rgb(229, 57, 53), cx - s * 0.22f, cy - s * 0.12f, s * 0.55f);
                Art.shape(c, Art.CIRCLE, Color.rgb(253, 216, 53), cx + s * 0.22f, cy - s * 0.12f, s * 0.55f);
                Art.shape(c, Art.CIRCLE, Color.rgb(30, 136, 229), cx, cy + s * 0.22f, s * 0.55f);
                break;
            case SHAPES:
                Art.shape(c, Art.STAR, Color.rgb(253, 216, 53), cx - s * 0.2f, cy - s * 0.05f, s * 0.6f);
                Art.shape(c, Art.HEART, Color.rgb(240, 98, 146), cx + s * 0.25f, cy + s * 0.15f, s * 0.5f);
                break;
            case ANIMALS:
                Art.emoji(c, "🐶", cx, cy, s * 0.9f);
                break;
            case THINGS:
                Art.emoji(c, "🍎", cx, cy, s * 0.9f);
                break;
            case NUMBERS:
                Art.label(c, "1", cx - s * 0.3f, cy, s * 0.5f, Color.rgb(229, 57, 53));
                Art.label(c, "2", cx, cy, s * 0.5f, Color.rgb(67, 160, 71));
                Art.label(c, "3", cx + s * 0.3f, cy, s * 0.5f, Color.rgb(30, 136, 229));
                break;
            case PIECES:
                Pieces.draw(c, Pieces.KNIGHT, true, cx - s * 0.2f, cy, s * 0.7f);
                Pieces.draw(c, Pieces.QUEEN, false, cx + s * 0.22f, cy, s * 0.7f);
                break;
            case BOARD:
                float sq = s * 0.8f / 4f, l = cx - s * 0.4f, t = cy - s * 0.4f;
                for (int r = 0; r < 4; r++) {
                    for (int col = 0; col < 4; col++) {
                        paint.setColor((r + col) % 2 == 0 ? Color.rgb(255, 236, 200) : Color.rgb(196, 134, 88));
                        c.drawRect(l + col * sq, t + r * sq, l + (col + 1) * sq, t + (r + 1) * sq, paint);
                    }
                }
                Pieces.draw(c, Pieces.PAWN, true, cx - sq * 0.5f, cy + sq * 0.5f, sq * 1.4f);
                Pieces.draw(c, Pieces.ROOK, false, cx + sq * 0.5f, cy - sq * 0.5f, sq * 1.4f);
                break;
            default:
                Art.emoji(c, "🎁", cx, cy, s * 0.9f);
                break;
        }
    }
}
