package com.toddler.chess;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.view.GestureDetector;
import android.view.MotionEvent;

/**
 * The menu: big colourful tiles, one per activity, and a language picker.
 * Long-press the title to set the child's name.
 */
final class HomeView extends PlayView {
    private static final int COLORS = 0, SHAPES = 1, ANIMALS = 2, THINGS = 3, NUMBERS = 4,
            PIECES = 5, BOARD = 6, MOVES = 7, SURPRISE = 8;
    private static final String[][] TITLES = {
            {"Colors", "রং", "रंग"},
            {"Shapes", "আকার", "आकार"},
            {"Animals", "পশুপাখি", "जानवर"},
            {"Things", "জিনিস", "चीज़ें"},
            {"Numbers", "সংখ্যা", "गिनती"},
            {"Chess pieces", "দাবার ঘুঁটি", "शतरंज के मोहरे"},
            {"Chess board", "দাবার বোর্ড", "शतरंज बोर्ड"},
            {"Chess moves", "দাবার চাল", "शतरंज की चालें"},
            {"Surprise!", "চমক!", "सरप्राइज़!"},
    };
    private static final int[] TILE_COLORS = {
            Color.rgb(255, 138, 128), Color.rgb(100, 181, 246), Color.rgb(129, 199, 132),
            Color.rgb(255, 213, 79), Color.rgb(186, 104, 200), Color.rgb(161, 136, 127),
            Color.rgb(255, 167, 38), Color.rgb(77, 182, 172), Color.rgb(240, 98, 146),
    };

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint tilePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Shader[] tileShaders = new Shader[TITLES.length];
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
                int chip = chipAt(e.getX(), e.getY());
                if (chip >= 0) {
                    activity.setLanguage(chip);
                    return true;
                }
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
        return dp(80);
    }

    private float headerHeight() {
        return titleHeight() + dp(52);
    }

    private int columns() {
        return getWidth() > getHeight() ? 5 : 3;
    }

    private void chipRect(int i, RectF out) {
        float cw = Math.min(dp(120), (getWidth() - dp(48)) / 3f), ch = dp(40), gap = dp(8);
        float left = (getWidth() - (cw * 3 + gap * 2)) / 2f;
        float top = titleHeight() + dp(2);
        out.set(left + i * (cw + gap), top, left + i * (cw + gap) + cw, top + ch);
    }

    private int chipAt(float x, float y) {
        RectF r = new RectF();
        for (int i = 0; i < 3; i++) {
            chipRect(i, r);
            r.inset(-dp(4), -dp(6));
            if (r.contains(x, y)) return i;
        }
        return -1;
    }

    private void tileRect(int i, RectF out) {
        int cols = columns();
        int rows = (TITLES.length + cols - 1) / cols;
        float pad = dp(12);
        float top = headerHeight();
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

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        RectF r = new RectF();
        for (int i = 0; i < TITLES.length; i++) {
            tileRect(i, r);
            tileShaders[i] = new LinearGradient(0, r.top, 0, r.bottom,
                    Art.tint(TILE_COLORS[i], 0.35f), TILE_COLORS[i], Shader.TileMode.CLAMP);
        }
    }

    private void open(final int tile, float x, float y) {
        pressed = tile;
        pressedAt = now();
        burst(x, y);
        activity.chimes.ding();
        activity.speaker.say(Lang.t(TITLES[tile]));
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
                case MOVES: activity.openMoves(); break;
                default: activity.openDeck(Decks.surprise()); break;
            }
        }, 600);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return gestures.onTouchEvent(event) || super.onTouchEvent(event);
    }

    @Override
    protected void paint(Canvas c) {
        backdrop(c, Color.rgb(255, 243, 224), Color.rgb(248, 187, 208));
        float w = getWidth();
        String name = activity.childName();
        String title = name.isEmpty()
                ? Lang.t("Let's play!", "চলো খেলি!", "चलो खेलें!")
                : Lang.t("Hi, ", "হ্যালো, ", "नमस्ते, ") + name + "!";
        float wave = (float) Math.sin(now() / 600.0) * dp(3);
        Art.label(c, title, w / 2f, titleHeight() * 0.45f + wave, dp(42), Color.rgb(216, 27, 96));
        if (name.isEmpty()) {
            Art.text(c, "(grown-ups: long-press here to add a name)", w / 2f, titleHeight() * 0.88f,
                    dp(12), Color.rgb(150, 110, 130));
        }

        for (int i = 0; i < 3; i++) {
            chipRect(i, rect);
            boolean on = i == Lang.current;
            paint.setColor(Color.argb(40, 0, 0, 0));
            rect.offset(0, dp(3));
            c.drawRoundRect(rect, rect.height() / 2, rect.height() / 2, paint);
            rect.offset(0, -dp(3));
            paint.setColor(on ? Color.rgb(216, 27, 96) : Color.WHITE);
            c.drawRoundRect(rect, rect.height() / 2, rect.height() / 2, paint);
            Art.text(c, Lang.NAMES[i], rect.centerX(), rect.centerY(), dp(18),
                    on ? Color.WHITE : Art.INK, rect.width() * 0.85f);
        }

        for (int i = 0; i < TITLES.length; i++) {
            tileRect(i, rect);
            float scale = 1;
            if (i == pressed) scale += 0.08f * bounce(pressedAt, 500);
            c.save();
            c.scale(scale, scale, rect.centerX(), rect.centerY());
            float radius = Math.min(rect.width(), rect.height()) * 0.2f;
            paint.setColor(Color.argb(50, 80, 20, 60));
            rect.offset(0, dp(5));
            c.drawRoundRect(rect, radius, radius, paint);
            rect.offset(0, -dp(5));
            tilePaint.setShader(tileShaders[i]);
            c.drawRoundRect(rect, radius, radius, tilePaint);
            paint.setColor(Color.argb(70, 255, 255, 255));
            c.drawRoundRect(rect.left + dp(6), rect.top + dp(5), rect.right - dp(6),
                    rect.top + rect.height() * 0.35f, radius * 0.8f, radius * 0.8f, paint);

            float badge = Math.min(rect.width(), rect.height() * 0.78f) * 0.36f;
            float icx = rect.centerX(), icy = rect.top + rect.height() * 0.42f;
            paint.setColor(Color.argb(200, 255, 255, 255));
            c.drawCircle(icx, icy, badge, paint);
            drawIcon(c, i, icx, icy, badge * 1.55f);
            float textSize = Math.min(rect.height() * 0.12f, dp(22));
            Art.text(c, Lang.t(TITLES[i]), rect.centerX(), rect.bottom - rect.height() * 0.13f,
                    textSize, Art.INK, rect.width() * 0.9f);
            c.restore();
        }
    }

    private void drawIcon(Canvas c, int tile, float cx, float cy, float s) {
        switch (tile) {
            case COLORS:
                Art.shape(c, Art.CIRCLE, Color.rgb(229, 57, 53), cx - s * 0.2f, cy - s * 0.12f, s * 0.5f);
                Art.shape(c, Art.CIRCLE, Color.rgb(253, 216, 53), cx + s * 0.2f, cy - s * 0.12f, s * 0.5f);
                Art.shape(c, Art.CIRCLE, Color.rgb(30, 136, 229), cx, cy + s * 0.2f, s * 0.5f);
                break;
            case SHAPES:
                Art.shape(c, Art.STAR, Color.rgb(253, 216, 53), cx - s * 0.17f, cy - s * 0.05f, s * 0.55f);
                Art.shape(c, Art.HEART, Color.rgb(240, 98, 146), cx + s * 0.22f, cy + s * 0.14f, s * 0.45f);
                break;
            case ANIMALS:
                Art.emoji(c, "🐶", cx, cy, s * 0.8f);
                break;
            case THINGS:
                Art.emoji(c, "🍎", cx, cy, s * 0.8f);
                break;
            case NUMBERS:
                Art.label(c, Lang.digit(1), cx - s * 0.27f, cy, s * 0.42f, Color.rgb(229, 57, 53));
                Art.label(c, Lang.digit(2), cx, cy, s * 0.42f, Color.rgb(67, 160, 71));
                Art.label(c, Lang.digit(3), cx + s * 0.27f, cy, s * 0.42f, Color.rgb(30, 136, 229));
                break;
            case PIECES:
                Pieces.draw(c, Pieces.KNIGHT, true, cx - s * 0.18f, cy, s * 0.62f);
                Pieces.draw(c, Pieces.QUEEN, false, cx + s * 0.2f, cy, s * 0.62f);
                break;
            case BOARD: {
                float sq = s * 0.6f / 4f, l = cx - s * 0.3f, t = cy - s * 0.3f;
                for (int r = 0; r < 4; r++) {
                    for (int col = 0; col < 4; col++) {
                        paint.setColor((r + col) % 2 == 0 ? BoardView.LIGHT : BoardView.DARK);
                        c.drawRect(l + col * sq, t + r * sq, l + (col + 1) * sq, t + (r + 1) * sq, paint);
                    }
                }
                Pieces.draw(c, Pieces.PAWN, true, cx - sq * 0.5f, cy + sq * 0.5f, sq * 1.5f);
                Pieces.draw(c, Pieces.ROOK, false, cx + sq * 0.5f, cy - sq * 0.5f, sq * 1.5f);
                break;
            }
            case MOVES: {
                float sq = s * 0.6f / 3f;
                paint.setColor(Color.argb(200, 76, 175, 80));
                for (int k = -1; k <= 1; k += 2) {
                    c.drawCircle(cx + k * sq * 1.2f, cy - sq * 0.9f, sq * 0.22f, paint);
                    c.drawCircle(cx + k * sq * 1.2f, cy + sq * 0.9f, sq * 0.22f, paint);
                }
                Pieces.draw(c, Pieces.KNIGHT, true, cx, cy, s * 0.6f);
                break;
            }
            default:
                Art.emoji(c, "🎁", cx, cy, s * 0.8f);
                break;
        }
    }
}
