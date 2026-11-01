package com.toddler.chess;

import android.graphics.Canvas;
import android.graphics.Color;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** All the flash card decks, built in the current language. */
final class Decks {
    static final int ANIMALS = 0, BIRDS = 1, FOOD = 2, THINGS = 3, VEHICLES = 4, BODY = 5,
            COLORS = 6, SHAPES = 7, NUMBERS = 8, LETTERS = 9, ABC = 10, RHYMES = 11, PIECES = 12;

    private static final int[] PASTELS = {
            Color.rgb(255, 236, 179), Color.rgb(200, 230, 255), Color.rgb(220, 245, 210),
            Color.rgb(255, 214, 230), Color.rgb(232, 220, 255), Color.rgb(255, 224, 204),
    };
    private static final int INK = Color.rgb(60, 36, 90);

    private Decks() {}

    private static int pastel(int i) {
        return PASTELS[i % PASTELS.length];
    }

    static List<Item> deck(int id) {
        switch (id) {
            case ANIMALS: return listDeck("animals.txt");
            case BIRDS: return listDeck("birds.txt");
            case FOOD: return listDeck("food.txt");
            case THINGS: return listDeck("things.txt");
            case VEHICLES: return listDeck("vehicles.txt");
            case BODY: return listDeck("body.txt");
            case COLORS: return colors();
            case SHAPES: return shapes();
            case NUMBERS: return numbers();
            case LETTERS: return bengaliLetters();
            case ABC: return abc();
            case RHYMES: return rhymes();
            default: return pieces();
        }
    }

    /** The second word on a card: Bengali, or English when the app is in Bengali. */
    private static String sub(String en, String bn) {
        return Lang.current == Lang.BN ? en : bn;
    }

    // Each row: color, emoji, then {label, speech} for English, Bengali, Hindi.
    private static final Object[][] COLORS_DATA = {
            {Color.rgb(229, 57, 53), "🍎",
                    "Red", "Red! A red apple.", "লাল", "লাল! লাল আপেল।", "लाल", "लाल! लाल सेब।"},
            {Color.rgb(251, 140, 0), "🍊",
                    "Orange", "Orange! An orange orange.", "কমলা", "কমলা! কমলা লেবু।", "नारंगी", "नारंगी! नारंगी संतरा।"},
            {Color.rgb(253, 216, 53), "🍌",
                    "Yellow", "Yellow! A yellow banana.", "হলুদ", "হলুদ! হলুদ কলা।", "पीला", "पीला! पीला केला।"},
            {Color.rgb(67, 160, 71), "🐸",
                    "Green", "Green! A green frog.", "সবুজ", "সবুজ! সবুজ ব্যাঙ।", "हरा", "हरा! हरा मेंढक।"},
            {Color.rgb(30, 136, 229), "🐳",
                    "Blue", "Blue! A blue whale.", "নীল", "নীল! নীল তিমি।", "नीला", "नीला! नीली व्हेल।"},
            {Color.rgb(79, 195, 247), "🦋",
                    "Sky blue", "Sky blue! Like the sky.", "আকাশি", "আকাশি! আকাশের রং।", "आसमानी", "आसमानी! आसमान का रंग।"},
            {Color.rgb(142, 36, 170), "🍇",
                    "Purple", "Purple! Purple grapes.", "বেগুনি", "বেগুনি! বেগুনি আঙুর।", "बैंगनी", "बैंगनी! बैंगनी अंगूर।"},
            {Color.rgb(240, 98, 146), "🌸",
                    "Pink", "Pink! A pink flower.", "গোলাপি", "গোলাপি! গোলাপি ফুল।", "गुलाबी", "गुलाबी! गुलाबी फूल।"},
            {Color.rgb(121, 85, 72), "🐻",
                    "Brown", "Brown! A brown bear.", "বাদামি", "বাদামি! বাদামি ভালুক।", "भूरा", "भूरा! भूरा भालू।"},
            {Color.rgb(144, 144, 150), "🐘",
                    "Grey", "Grey! A grey elephant.", "ধূসর", "ধূসর! ধূসর হাতি।", "स्लेटी", "स्लेटी! स्लेटी हाथी।"},
            {Color.rgb(33, 33, 33), null,
                    "Black", "Black! Like the black chess pieces.", "কালো", "কালো! কালো দাবার ঘুঁটি।", "काला", "काला! काले मोहरे।"},
            {Color.WHITE, "☁️",
                    "White", "White! A white cloud.", "সাদা", "সাদা! সাদা মেঘ।", "सफ़ेद", "सफ़ेद! सफ़ेद बादल।"},
    };

    static List<Item> colors() {
        List<Item> items = new ArrayList<>();
        for (Object[] d : COLORS_DATA) {
            final int color = (Integer) d[0];
            final String emoji = (String) d[1];
            int bg = color == Color.WHITE ? Color.rgb(176, 214, 255)
                    : Art.isDark(color) ? Color.rgb(225, 225, 235) : Art.tint(color, 0.75f);
            Item it = new Item(pick(d, 2), pick(d, 3), color, bg, (c, cx, cy, s) -> {
                Art.shape(c, Art.CIRCLE, color, cx - s * 0.12f, cy, s * 0.8f);
                if (emoji == null) {
                    Pieces.draw(c, Pieces.KING, false, cx + s * 0.32f, cy + s * 0.2f, s * 0.42f);
                } else {
                    Art.emoji(c, emoji, cx + s * 0.32f, cy + s * 0.25f, s * 0.36f);
                }
            });
            it.sub = sub((String) d[2], (String) d[4]);
            items.add(it);
        }
        return items;
    }

    private static final Object[][] SHAPES_DATA = {
            {Art.CIRCLE, Color.rgb(229, 57, 53),
                    "Circle", "Circle! Round and round.", "বৃত্ত", "বৃত্ত! গোল গোল।", "गोला", "गोला! गोल गोल।"},
            {Art.SQUARE, Color.rgb(30, 136, 229),
                    "Square", "Square! Four sides.", "বর্গ", "বর্গ! চারটে বাহু।", "वर्ग", "वर्ग! चार भुजाएँ।"},
            {Art.TRIANGLE, Color.rgb(67, 160, 71),
                    "Triangle", "Triangle! Three sides.", "ত্রিভুজ", "ত্রিভুজ! তিনটে বাহু।", "त्रिकोण", "त्रिकोण! तीन भुजाएँ।"},
            {Art.STAR, Color.rgb(253, 216, 53),
                    "Star", "Star! Twinkle twinkle.", "তারা", "তারা! মিটমিট করে।", "तारा", "तारा! टिमटिम।"},
            {Art.HEART, Color.rgb(240, 98, 146),
                    "Heart", "Heart! I love you.", "হৃদয়", "হৃদয়! তোমায় খুব ভালোবাসি।", "दिल", "दिल! मुझे तुमसे प्यार है।"},
            {Art.RECTANGLE, Color.rgb(251, 140, 0),
                    "Rectangle", "Rectangle! Two long sides, two short sides.", "আয়তক্ষেত্র", "আয়তক্ষেত্র! দুটো লম্বা, দুটো ছোট বাহু।", "आयत", "आयत! दो लंबी, दो छोटी भुजाएँ।"},
            {Art.OVAL, Color.rgb(142, 36, 170),
                    "Oval", "Oval! Like an egg.", "ডিম্বাকার", "ডিম্বাকার! ডিমের মতো।", "अंडाकार", "अंडाकार! अंडे जैसा।"},
            {Art.DIAMOND, Color.rgb(0, 172, 193),
                    "Diamond", "Diamond! Shiny like a kite.", "রম্বস", "রম্বস! ঘুড়ির মতো।", "हीरा", "हीरा! पतंग जैसा।"},
    };

    static List<Item> shapes() {
        List<Item> items = new ArrayList<>();
        for (int i = 0; i < SHAPES_DATA.length; i++) {
            Object[] d = SHAPES_DATA[i];
            final int shape = (Integer) d[0];
            final int color = (Integer) d[1];
            Item it = new Item(pick(d, 2), pick(d, 3), color, pastel(i),
                    (c, cx, cy, s) -> Art.shape(c, shape, color, cx, cy, s));
            it.sub = sub((String) d[2], (String) d[4]);
            items.add(it);
        }
        return items;
    }

    private static final String[] COUNT_EMOJI = {"☀️", "⚽", "🦆", "🍎", "⭐", "🎈", "🐟", "🌸", "🍌", "🐞"};
    private static final String[][] COUNT_NOUNS = {
            {"One sun!", "একটা সূর্য!", "एक सूरज!"},
            {"Two balls!", "দুটো বল!", "दो गेंदें!"},
            {"Three ducks!", "তিনটে হাঁস!", "तीन बत्तखें!"},
            {"Four apples!", "চারটে আপেল!", "चार सेब!"},
            {"Five stars!", "পাঁচটা তারা!", "पाँच तारे!"},
            {"Six balloons!", "ছ'টা বেলুন!", "छह गुब्बारे!"},
            {"Seven fish!", "সাতটা মাছ!", "सात मछलियाँ!"},
            {"Eight flowers!", "আটটা ফুল!", "आठ फूल!"},
            {"Nine bananas!", "ন'টা কলা!", "नौ केले!"},
            {"Ten ladybugs!", "দশটা লেডিবাগ!", "दस लेडीबग!"},
    };

    static List<Item> numbers() {
        List<Item> items = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            final int n = i + 1;
            final String e = COUNT_EMOJI[i];
            StringBuilder speech = new StringBuilder();
            for (int k = 1; k <= n; k++) speech.append(Lang.number(k)).append("... ");
            speech.append(Lang.t(COUNT_NOUNS[i]));
            Item it = new Item(Lang.digit(n), speech.toString(), Color.rgb(120, 90, 200), pastel(i),
                    (c, cx, cy, s) -> drawCount(c, e, n, cx, cy, s));
            it.sub = Lang.current == Lang.BN ? Lang.numberIn(n, Lang.EN) : Lang.numberIn(n, Lang.BN);
            it.ask = Lang.number(n);
            items.add(it);
        }
        return items;
    }

    private static void drawCount(Canvas c, String e, int n, float cx, float cy, float s) {
        if (n <= 5) {
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
            return;
        }
        int[][] rows = {{3, 3}, {4, 3}, {4, 4}, {3, 3, 3}, {5, 5}};
        int[] r = rows[n - 6];
        int maxCols = 0;
        for (int k : r) maxCols = Math.max(maxCols, k);
        float step = s * 0.95f / Math.max(maxCols, r.length);
        float size = step * 0.9f;
        float top = cy - step * (r.length - 1) / 2f;
        for (int row = 0; row < r.length; row++) {
            float left = cx - step * (r[row] - 1) / 2f;
            for (int col = 0; col < r[row]; col++) {
                Art.emoji(c, e, left + col * step, top + row * step, size);
            }
        }
    }

    private static final String[][] PIECE_SPEECH = {
            {"The king wears a crown with a cross.", "রাজার মাথায় মুকুট।", "राजा के सिर पर ताज।"},
            {"The queen has a sparkly crown.", "মন্ত্রী সবচেয়ে শক্তিশালী।", "रानी सबसे ताक़तवर है।"},
            {"The rook looks like a castle.", "নৌকা দেখতে দুর্গের মতো।", "हाथी किले जैसा दिखता है।"},
            {"The bishop has a pointy hat.", "হাতির মাথায় সুঁচালো টুপি।", "ऊँट की नुकीली टोपी।"},
            {"The knight is a horsey. Neigh!", "ঘোড়া! চিঁহিঁ!", "घोड़ा! हिनहिन!"},
            {"The little pawn. Step, step.", "ছোট্ট বোড়ে, ছোট্ট সৈনিক।", "छोटा प्यादा, छोटा सिपाही।"},
    };

    static List<Item> pieces() {
        List<Item> items = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            final int type = i;
            String name = Pieces.name(i);
            Item it = new Item(name, name + "! " + Lang.t(PIECE_SPEECH[i]), Color.rgb(93, 64, 55), pastel(i),
                    (c, cx, cy, s) -> {
                        Pieces.draw(c, type, true, cx - s * 0.24f, cy, s * 0.62f);
                        Pieces.draw(c, type, false, cx + s * 0.24f, cy, s * 0.62f);
                    });
            it.sub = sub(Pieces.nameIn(i, Lang.EN), Pieces.nameIn(i, Lang.BN));
            items.add(it);
        }
        return items;
    }

    /** অ আ ই ... with a picture word for each letter. Always spoken in Bengali. */
    static List<Item> bengaliLetters() {
        List<Item> items = new ArrayList<>();
        List<String[]> rows = Data.rows("letters_bn.txt");
        for (int i = 0; i < rows.size(); i++) {
            String[] d = rows.get(i);
            if (d.length < 6) continue;
            final String letter = d[0];
            String name = d[1], word = d[2];
            final String emoji = d[3];
            boolean vowel = i < 11;
            final int color = vowel ? Color.rgb(216, 27, 96) : Color.rgb(25, 118, 210);
            String speech = word.startsWith(letter)
                    ? name + "! " + letter + "-তে " + word + "।"
                    : name + "! যেমন, " + word + "।";
            Item it = new Item(word, speech, color, pastel(i), (c, cx, cy, s) -> {
                Art.label(c, letter, cx - s * 0.16f, cy - s * 0.06f, s * 0.7f, color);
                drawArt(c, emoji, cx + s * 0.3f, cy + s * 0.26f, s * 0.4f);
            });
            it.speechLang = Lang.BN;
            it.ask = name;
            it.sub = Lang.current == Lang.HI ? d[5] : d[4];
            items.add(it);
        }
        return items;
    }

    /** A for Apple. Always spoken in English. */
    static List<Item> abc() {
        List<Item> items = new ArrayList<>();
        List<String[]> rows = Data.rows("abc.txt");
        int[] colors = {Color.rgb(229, 57, 53), Color.rgb(30, 136, 229), Color.rgb(67, 160, 71),
                Color.rgb(251, 140, 0), Color.rgb(142, 36, 170)};
        for (int i = 0; i < rows.size(); i++) {
            String[] d = rows.get(i);
            if (d.length < 5) continue;
            final String letter = d[0];
            String word = d[1];
            final String art = d[2];
            final int color = colors[i % colors.length];
            Item it = new Item(word, letter + "! " + letter + " for " + word + ".", color, pastel(i),
                    (c, cx, cy, s) -> {
                        Art.label(c, letter + letter.toLowerCase(), cx - s * 0.14f, cy - s * 0.08f, s * 0.5f, color);
                        drawArt(c, art, cx + s * 0.3f, cy + s * 0.26f, s * 0.4f);
                    });
            it.speechLang = Lang.EN;
            it.ask = "the letter " + letter;
            it.sub = Lang.current == Lang.HI ? d[4] : d[3];
            items.add(it);
        }
        return items;
    }

    /** Bengali rhymes first, then English and Hindi; each one is read in its own language. */
    static List<Item> rhymes() {
        List<Item> items = new ArrayList<>();
        for (String[] d : Data.rows("rhymes.txt")) {
            if (d.length < 6) continue;
            final String emoji = d[0];
            int lang = d[1].equals("bn") ? Lang.BN : d[1].equals("hi") ? Lang.HI : Lang.EN;
            String[] titles = {d[2], d[3], d[4]};
            String title = titles[lang];
            Item it = new Item(title, d[5].replace("/", " "), INK, pastel(items.size()),
                    (c, cx, cy, s) -> drawArt(c, emoji, cx, cy, s * 0.9f));
            it.speechLang = lang;
            it.text = d[5].replace("/", "\n");
            int subLang = Lang.current != lang ? Lang.current : lang == Lang.EN ? Lang.BN : Lang.EN;
            it.sub = titles[subLang];
            items.add(it);
        }
        return items;
    }

    static List<Item> surprise() {
        List<Item> all = new ArrayList<>();
        for (int id : new int[]{COLORS, SHAPES, ANIMALS, BIRDS, FOOD, THINGS, VEHICLES, PIECES}) {
            all.addAll(deck(id));
        }
        Collections.shuffle(all);
        return all;
    }

    private static String pick(Object[] row, int first) {
        return (String) row[first + 2 * Lang.current];
    }

    /** Cards from a word list: picture | en | fact | bn | fact | hi | fact. */
    private static List<Item> listDeck(String file) {
        List<Item> items = new ArrayList<>();
        for (String[] d : Data.rows(file)) {
            if (d.length < 7) continue;
            String label = d[1 + 2 * Lang.current];
            String fact = d[2 + 2 * Lang.current].trim();
            String speech = fact.isEmpty() ? label + "!" : label + "! " + fact;
            final String art = d[0];
            Item it = new Item(label, speech, INK, pastel(items.size()), painter(art, label));
            it.sub = sub(d[1], d[3]);
            items.add(it);
        }
        return items;
    }

    private static Item.Painter painter(final String art, final String label) {
        final int at = art.indexOf('@');
        final String emoji = at < 0 ? art : art.substring(0, at);
        if (!emoji.isEmpty() && Art.hasEmoji(emoji)) {
            return (c, cx, cy, s) -> Art.emoji(c, emoji, cx, cy, s * 0.9f);
        }
        if (at >= 0) {
            final BirdArt.Spec spec = BirdArt.parse(art.substring(at));
            if (spec != null) return (c, cx, cy, s) -> BirdArt.draw(c, spec, cx, cy, s);
        }
        return (c, cx, cy, s) -> Art.letterBadge(c, label, cx, cy, s * 0.8f);
    }

    /** Draws an emoji, a bird spec or a named drawing such as "@xylophone". */
    static void drawArt(Canvas c, String art, float cx, float cy, float s) {
        if (art.equals("@xylophone")) {
            InstrumentArt.icon(c, Instrument.XYLOPHONE, cx, cy, s);
        } else if (art.startsWith("@")) {
            BirdArt.Spec spec = BirdArt.parse(art);
            if (spec != null) BirdArt.draw(c, spec, cx, cy, s);
        } else {
            Art.emoji(c, art, cx, cy, s);
        }
    }
}
