package com.toddler.chess;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.RectF;

/**
 * The menu: big colourful tiles, one per activity, and a language picker.
 * Long-press the title to set the child's name.
 */
final class HomeView extends TileMenuView {
    private static final int ANIMALS = 0, BIRDS = 1, LETTERS = 2, MUSIC = 3, QUIZ = 4, COLORS = 5,
            SHAPES = 6, NUMBERS = 7, FOOD = 8, THINGS = 9, VEHICLES = 10, BODY = 11, ABC = 12,
            RHYMES = 13, PIECES = 14, BOARD = 15, MOVES = 16, SURPRISE = 17;
    private static final String[][] TITLES = {
            {"Animals", "প্রাণী", "जानवर"},
            {"Birds", "পাখি", "पक्षी"},
            {"Bangla letters", "বর্ণমালা", "बांग्ला अक्षर"},
            {"Music", "গান-বাজনা", "संगीत"},
            {"Find it!", "খুঁজে দেখো", "ढूँढो!"},
            {"Colors", "রং", "रंग"},
            {"Shapes", "আকার", "आकार"},
            {"Numbers", "সংখ্যা", "गिनती"},
            {"Food", "খাবার", "खाना"},
            {"Things", "জিনিস", "चीज़ें"},
            {"Vehicles", "যানবাহন", "गाड़ियाँ"},
            {"My body", "আমার শরীর", "मेरा शरीर"},
            {"ABC", "এবিসি", "एबीसी"},
            {"Rhymes", "ছড়া", "कविताएँ"},
            {"Chess pieces", "দাবার ঘুঁটি", "शतरंज के मोहरे"},
            {"Chess board", "দাবার বোর্ড", "शतरंज बोर्ड"},
            {"Chess moves", "দাবার চাল", "शतरंज की चालें"},
            {"Surprise!", "চমক!", "सरप्राइज़!"},
    };
    private static final int[] TILE_COLORS = {
            Color.rgb(255, 167, 38), Color.rgb(77, 182, 172), Color.rgb(240, 98, 146),
            Color.rgb(149, 117, 205), Color.rgb(255, 213, 79), Color.rgb(255, 138, 128),
            Color.rgb(100, 181, 246), Color.rgb(186, 104, 200), Color.rgb(129, 199, 132),
            Color.rgb(255, 183, 77), Color.rgb(79, 195, 247), Color.rgb(244, 143, 177),
            Color.rgb(174, 213, 129), Color.rgb(121, 134, 203), Color.rgb(161, 136, 127),
            Color.rgb(255, 167, 38), Color.rgb(77, 182, 172), Color.rgb(240, 98, 146),
    };

    private final RectF chip = new RectF();

    HomeView(Context context) {
        super(context);
    }

    private float titleHeight() {
        return dp(76);
    }

    @Override
    float headerHeight() {
        return titleHeight() + dp(50);
    }

    @Override
    int tileCount() {
        return TITLES.length;
    }

    @Override
    String tileTitle(int i) {
        return Lang.t(TITLES[i]);
    }

    @Override
    int tileColor(int i) {
        return TILE_COLORS[i];
    }

    private void chipRect(int i, RectF out) {
        float cw = Math.min(dp(120), (getWidth() - dp(48)) / 3f), ch = dp(40), gap = dp(8);
        float left = (getWidth() - (cw * 3 + gap * 2)) / 2f;
        float top = titleHeight() + dp(2);
        out.set(left + i * (cw + gap), top, left + i * (cw + gap) + cw, top + ch);
    }

    @Override
    void onHeaderTap(float x, float y) {
        for (int i = 0; i < 3; i++) {
            chipRect(i, chip);
            chip.inset(-dp(4), -dp(6));
            if (chip.contains(x, y)) {
                activity.setLanguage(Lang.MENU_ORDER[i]);
                return;
            }
        }
    }

    @Override
    void onHeaderLongPress(float x, float y) {
        if (y < titleHeight()) activity.askChildName();
    }

    @Override
    void openTile(int tile) {
        switch (tile) {
            case ANIMALS: activity.openDeck(Decks.deck(Decks.ANIMALS)); break;
            case BIRDS: activity.openDeck(Decks.deck(Decks.BIRDS)); break;
            case LETTERS: activity.openDeck(Decks.deck(Decks.LETTERS)); break;
            case MUSIC: activity.openMusic(); break;
            case QUIZ: activity.openQuiz(); break;
            case COLORS: activity.openDeck(Decks.deck(Decks.COLORS)); break;
            case SHAPES: activity.openDeck(Decks.deck(Decks.SHAPES)); break;
            case NUMBERS: activity.openDeck(Decks.deck(Decks.NUMBERS)); break;
            case FOOD: activity.openDeck(Decks.deck(Decks.FOOD)); break;
            case THINGS: activity.openDeck(Decks.deck(Decks.THINGS)); break;
            case VEHICLES: activity.openDeck(Decks.deck(Decks.VEHICLES)); break;
            case BODY: activity.openDeck(Decks.deck(Decks.BODY)); break;
            case ABC: activity.openDeck(Decks.deck(Decks.ABC)); break;
            case RHYMES: activity.openDeck(Decks.deck(Decks.RHYMES)); break;
            case PIECES: activity.openDeck(Decks.deck(Decks.PIECES)); break;
            case BOARD: activity.openBoard(); break;
            case MOVES: activity.openMoves(); break;
            default: activity.openDeck(Decks.surprise()); break;
        }
    }

    @Override
    void paintBackdrop(Canvas c) {
        backdrop(c, Color.rgb(255, 243, 224), Color.rgb(248, 187, 208));
    }

    @Override
    void paintHeader(Canvas c) {
        float w = getWidth();
        // Soft panel behind the header so scrolled tiles don't show through the text.
        paint.setColor(Color.argb(235, 255, 240, 228));
        c.drawRect(0, 0, w, headerHeight(), paint);
        String name = activity.childName();
        String title = name.isEmpty()
                ? Lang.t("Let's play!", "চলো খেলি!", "चलो खेलें!")
                : Lang.t("Hi, ", "হ্যালো, ", "नमस्ते, ") + name + "!";
        float wave = (float) Math.sin(now() / 600.0) * dp(3);
        Art.label(c, title, w / 2f, titleHeight() * 0.45f + wave, dp(40), Color.rgb(216, 27, 96));
        if (name.isEmpty()) {
            Art.text(c, "(grown-ups: long-press here to add a name)", w / 2f, titleHeight() * 0.88f,
                    dp(12), Color.rgb(150, 110, 130));
        }

        for (int i = 0; i < 3; i++) {
            chipRect(i, rect);
            int lang = Lang.MENU_ORDER[i];
            boolean on = lang == Lang.current;
            paint.setColor(Color.argb(40, 0, 0, 0));
            rect.offset(0, dp(3));
            c.drawRoundRect(rect, rect.height() / 2, rect.height() / 2, paint);
            rect.offset(0, -dp(3));
            paint.setColor(on ? Color.rgb(216, 27, 96) : Color.WHITE);
            c.drawRoundRect(rect, rect.height() / 2, rect.height() / 2, paint);
            Art.text(c, Lang.NAMES[lang], rect.centerX(), rect.centerY(), dp(18),
                    on ? Color.WHITE : Art.INK, rect.width() * 0.85f);
        }
    }

    @Override
    void drawTileIcon(Canvas c, int tile, float cx, float cy, float s) {
        switch (tile) {
            case ANIMALS:
                Art.emoji(c, "🐯", cx, cy, s * 0.8f);
                break;
            case BIRDS:
                Art.emoji(c, Art.hasEmoji("🦜") ? "🦜" : "🐦", cx, cy, s * 0.8f);
                break;
            case LETTERS:
                Art.label(c, "অ", cx - s * 0.18f, cy - s * 0.05f, s * 0.48f, Color.rgb(216, 27, 96));
                Art.label(c, "ক", cx + s * 0.2f, cy + s * 0.08f, s * 0.48f, Color.rgb(25, 118, 210));
                break;
            case MUSIC:
                Art.emoji(c, "🎹", cx - s * 0.1f, cy + s * 0.08f, s * 0.6f);
                Art.emoji(c, "🎵", cx + s * 0.25f, cy - s * 0.2f, s * 0.36f);
                break;
            case QUIZ:
                Art.emoji(c, "🔍", cx, cy, s * 0.75f);
                break;
            case COLORS:
                Art.shape(c, Art.CIRCLE, Color.rgb(229, 57, 53), cx - s * 0.2f, cy - s * 0.12f, s * 0.5f);
                Art.shape(c, Art.CIRCLE, Color.rgb(253, 216, 53), cx + s * 0.2f, cy - s * 0.12f, s * 0.5f);
                Art.shape(c, Art.CIRCLE, Color.rgb(30, 136, 229), cx, cy + s * 0.2f, s * 0.5f);
                break;
            case SHAPES:
                Art.shape(c, Art.STAR, Color.rgb(253, 216, 53), cx - s * 0.17f, cy - s * 0.05f, s * 0.55f);
                Art.shape(c, Art.HEART, Color.rgb(240, 98, 146), cx + s * 0.22f, cy + s * 0.14f, s * 0.45f);
                break;
            case NUMBERS:
                Art.label(c, Lang.digit(1), cx - s * 0.27f, cy, s * 0.42f, Color.rgb(229, 57, 53));
                Art.label(c, Lang.digit(2), cx, cy, s * 0.42f, Color.rgb(67, 160, 71));
                Art.label(c, Lang.digit(3), cx + s * 0.27f, cy, s * 0.42f, Color.rgb(30, 136, 229));
                break;
            case FOOD:
                Art.emoji(c, "🥭", cx, cy, s * 0.8f);
                break;
            case THINGS:
                Art.emoji(c, "🧸", cx, cy, s * 0.8f);
                break;
            case VEHICLES:
                Art.emoji(c, Art.hasEmoji("🛺") ? "🛺" : "🚌", cx, cy, s * 0.8f);
                break;
            case BODY:
                Art.emoji(c, "👶", cx, cy, s * 0.8f);
                break;
            case ABC:
                Art.label(c, "A", cx - s * 0.27f, cy, s * 0.42f, Color.rgb(229, 57, 53));
                Art.label(c, "B", cx, cy, s * 0.42f, Color.rgb(67, 160, 71));
                Art.label(c, "C", cx + s * 0.27f, cy, s * 0.42f, Color.rgb(30, 136, 229));
                break;
            case RHYMES:
                Art.emoji(c, "🌙", cx - s * 0.08f, cy, s * 0.7f);
                Art.emoji(c, "🎶", cx + s * 0.26f, cy - s * 0.2f, s * 0.32f);
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
