package com.toddler.chess;

import android.graphics.Color;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** All the flash card decks. */
final class Decks {
    private static final int[] PASTELS = {
            Color.rgb(255, 236, 179), Color.rgb(200, 230, 255), Color.rgb(220, 245, 210),
            Color.rgb(255, 214, 230), Color.rgb(232, 220, 255), Color.rgb(255, 224, 204),
    };

    private Decks() {}

    private static int pastel(int i) {
        return PASTELS[i % PASTELS.length];
    }

    static List<Item> colors() {
        Object[][] data = {
                {"Red", Color.rgb(229, 57, 53), "🍎", "Red! A red apple."},
                {"Orange", Color.rgb(251, 140, 0), "🍊", "Orange! An orange orange."},
                {"Yellow", Color.rgb(253, 216, 53), "🍌", "Yellow! A yellow banana."},
                {"Green", Color.rgb(67, 160, 71), "🐸", "Green! A green frog."},
                {"Blue", Color.rgb(30, 136, 229), "🐳", "Blue! A blue whale."},
                {"Purple", Color.rgb(142, 36, 170), "🍇", "Purple! Purple grapes."},
                {"Pink", Color.rgb(240, 98, 146), "🐷", "Pink! A pink pig."},
                {"Brown", Color.rgb(121, 85, 72), "🐻", "Brown! A brown bear."},
                {"Black", Color.rgb(33, 33, 33), "♚", "Black! Like the black chess pieces."},
                {"White", Color.WHITE, "☁️", "White! A white cloud."},
        };
        List<Item> items = new ArrayList<>();
        for (Object[] d : data) {
            final int color = (Integer) d[1];
            final String emoji = (String) d[2];
            final boolean chess = emoji.equals("♚");
            int bg = color == Color.WHITE ? Color.rgb(176, 214, 255)
                    : Art.isDark(color) ? Color.rgb(235, 235, 240) : Art.tint(color, 0.78f);
            items.add(new Item((String) d[0], (String) d[3], color, bg, (c, cx, cy, s) -> {
                Art.shape(c, Art.CIRCLE, color, cx - s * 0.12f, cy, s * 0.8f);
                if (chess) {
                    Pieces.draw(c, Pieces.KING, false, cx + s * 0.32f, cy + s * 0.22f, s * 0.4f);
                } else {
                    Art.emoji(c, emoji, cx + s * 0.32f, cy + s * 0.25f, s * 0.36f);
                }
            }));
        }
        return items;
    }

    static List<Item> shapes() {
        Object[][] data = {
                {"Circle", Art.CIRCLE, Color.rgb(229, 57, 53), "Circle! Round and round."},
                {"Square", Art.SQUARE, Color.rgb(30, 136, 229), "Square! Four sides."},
                {"Triangle", Art.TRIANGLE, Color.rgb(67, 160, 71), "Triangle! Three sides."},
                {"Star", Art.STAR, Color.rgb(253, 216, 53), "Star! Twinkle twinkle."},
                {"Heart", Art.HEART, Color.rgb(240, 98, 146), "Heart! I love you."},
                {"Rectangle", Art.RECTANGLE, Color.rgb(251, 140, 0), "Rectangle!"},
                {"Oval", Art.OVAL, Color.rgb(142, 36, 170), "Oval! Like an egg."},
                {"Diamond", Art.DIAMOND, Color.rgb(0, 172, 193), "Diamond!"},
        };
        List<Item> items = new ArrayList<>();
        for (int i = 0; i < data.length; i++) {
            Object[] d = data[i];
            final int shape = (Integer) d[1];
            final int color = (Integer) d[2];
            items.add(new Item((String) d[0], (String) d[3], color, pastel(i),
                    (c, cx, cy, s) -> Art.shape(c, shape, color, cx, cy, s)));
        }
        return items;
    }

    static List<Item> animals() {
        return emojiDeck(new String[][]{
                {"🐶", "Dog", "Dog! Woof woof!"},
                {"🐱", "Cat", "Cat! Meow!"},
                {"🐮", "Cow", "Cow! Moo!"},
                {"🦆", "Duck", "Duck! Quack quack!"},
                {"🐷", "Pig", "Pig! Oink oink!"},
                {"🐑", "Sheep", "Sheep! Baa baa!"},
                {"🐴", "Horse", "Horse! Neigh!"},
                {"🦁", "Lion", "Lion! Roar!"},
                {"🐘", "Elephant", "Elephant! Big ears and a long trunk."},
                {"🐵", "Monkey", "Monkey! Ooh ooh, ah ah!"},
                {"🐦", "Bird", "Bird! Tweet tweet!"},
                {"🐟", "Fish", "Fish! Swim swim."},
                {"🐸", "Frog", "Frog! Ribbit!"},
                {"🐔", "Chicken", "Chicken! Cluck cluck!"},
                {"🐝", "Bee", "Bee! Buzz buzz!"},
                {"🐰", "Bunny", "Bunny! Hop hop hop!"},
                {"🐻", "Bear", "Bear! Big hug!"},
                {"🐢", "Turtle", "Turtle! Slow and steady."},
        });
    }

    static List<Item> things() {
        return emojiDeck(new String[][]{
                {"🍎", "Apple", "Apple! Crunch crunch."},
                {"🍌", "Banana", "Banana! Yummy."},
                {"🍓", "Strawberry", "Strawberry!"},
                {"🥕", "Carrot", "Carrot!"},
                {"🍪", "Cookie", "Cookie! Nom nom nom."},
                {"🍼", "Bottle", "Bottle! Milk time."},
                {"⚽", "Ball", "Ball! Bounce bounce."},
                {"🧸", "Teddy", "Teddy bear! Cuddle."},
                {"🎈", "Balloon", "Balloon! Up, up, up!"},
                {"📚", "Book", "Book! Let's read."},
                {"👟", "Shoe", "Shoe! Put on your shoes."},
                {"🎩", "Hat", "Hat!"},
                {"🥄", "Spoon", "Spoon!"},
                {"☕", "Cup", "Cup!"},
                {"🛏️", "Bed", "Bed! Night night."},
                {"🚗", "Car", "Car! Beep beep!"},
                {"🚌", "Bus", "Bus! The wheels go round and round."},
                {"🚂", "Train", "Train! Choo choo!"},
                {"✈️", "Airplane", "Airplane! Zoom!"},
                {"⛵", "Boat", "Boat! Splash."},
                {"🏠", "House", "House! Home sweet home."},
                {"🌳", "Tree", "Tree! Tall tree."},
                {"🌸", "Flower", "Flower! Smell the flower."},
                {"☀️", "Sun", "Sun! Hello sunshine."},
                {"🌙", "Moon", "Moon! Good night moon."},
                {"⭐", "Star", "Star! Twinkle twinkle."},
        });
    }

    static List<Item> numbers() {
        String[] emoji = {"☀️", "⚽", "🦆", "🍎", "⭐"};
        String[] words = {"One", "Two", "Three", "Four", "Five"};
        String[] nouns = {"sun", "balls", "ducks", "apples", "stars"};
        List<Item> items = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            final int n = i + 1;
            final String e = emoji[i];
            StringBuilder speech = new StringBuilder();
            for (int k = 0; k < n; k++) speech.append(words[k]).append("... ");
            speech.append(words[i]).append(' ').append(nouns[i]).append('!');
            items.add(new Item(String.valueOf(n), speech.toString(), Color.rgb(120, 90, 200), pastel(i),
                    (c, cx, cy, s) -> drawCount(c, e, n, cx, cy, s)));
        }
        return items;
    }

    private static void drawCount(android.graphics.Canvas c, String e, int n, float cx, float cy, float s) {
        // Dice-like layouts, as fractions of the card size.
        float[][][] layouts = {
                {{0, 0}},
                {{-0.25f, 0}, {0.25f, 0}},
                {{0, -0.24f}, {-0.26f, 0.2f}, {0.26f, 0.2f}},
                {{-0.24f, -0.24f}, {0.24f, -0.24f}, {-0.24f, 0.24f}, {0.24f, 0.24f}},
                {{-0.28f, -0.28f}, {0.28f, -0.28f}, {0, 0}, {-0.28f, 0.28f}, {0.28f, 0.28f}},
        };
        float size = n == 1 ? s * 0.75f : s * 0.38f;
        for (float[] p : layouts[n - 1]) Art.emoji(c, e, cx + p[0] * s, cy + p[1] * s, size);
    }

    static List<Item> pieces() {
        String[] speech = {
                "King! The king wears a crown with a cross.",
                "Queen! The queen has a sparkly crown.",
                "Rook! The rook looks like a castle.",
                "Bishop! The bishop has a pointy hat.",
                "Knight! The knight is a horsey. Neigh!",
                "Pawn! The little pawn. Step, step.",
        };
        List<Item> items = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            final int type = i;
            items.add(new Item(Pieces.NAMES[i], speech[i], Color.rgb(93, 64, 55), pastel(i),
                    (c, cx, cy, s) -> {
                        Pieces.draw(c, type, true, cx - s * 0.24f, cy, s * 0.62f);
                        Pieces.draw(c, type, false, cx + s * 0.24f, cy, s * 0.62f);
                    }));
        }
        return items;
    }

    static List<Item> surprise() {
        List<Item> all = new ArrayList<>();
        all.addAll(colors());
        all.addAll(shapes());
        all.addAll(animals());
        all.addAll(things());
        all.addAll(pieces());
        Collections.shuffle(all);
        return all;
    }

    private static List<Item> emojiDeck(String[][] data) {
        List<Item> items = new ArrayList<>();
        for (String[] d : data) {
            final String emoji = d[0];
            if (!Art.hasEmoji(emoji)) continue;
            items.add(new Item(d[1], d[2], Color.rgb(60, 36, 90), pastel(items.size()),
                    (c, cx, cy, s) -> Art.emoji(c, emoji, cx, cy, s * 0.9f)));
        }
        return items;
    }
}
