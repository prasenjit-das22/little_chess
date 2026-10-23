package com.toddler.chess;

import android.graphics.Canvas;
import android.graphics.Color;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** All the flash card decks, built in the current language. */
final class Decks {
    private static final int[] PASTELS = {
            Color.rgb(255, 236, 179), Color.rgb(200, 230, 255), Color.rgb(220, 245, 210),
            Color.rgb(255, 214, 230), Color.rgb(232, 220, 255), Color.rgb(255, 224, 204),
    };

    private Decks() {}

    private static int pastel(int i) {
        return PASTELS[i % PASTELS.length];
    }

    // Each row: color, emoji, then {label, speech} for English, Bengali, Hindi.
    private static final Object[][] COLORS = {
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
            {Color.rgb(142, 36, 170), "🍇",
                    "Purple", "Purple! Purple grapes.", "বেগুনি", "বেগুনি! বেগুনি আঙুর।", "बैंगनी", "बैंगनी! बैंगनी अंगूर।"},
            {Color.rgb(240, 98, 146), "🌸",
                    "Pink", "Pink! A pink flower.", "গোলাপি", "গোলাপি! গোলাপি ফুল।", "गुलाबी", "गुलाबी! गुलाबी फूल।"},
            {Color.rgb(121, 85, 72), "🐻",
                    "Brown", "Brown! A brown bear.", "বাদামি", "বাদামি! বাদামি ভালুক।", "भूरा", "भूरा! भूरा भालू।"},
            {Color.rgb(33, 33, 33), null,
                    "Black", "Black! Like the black chess pieces.", "কালো", "কালো! কালো দাবার ঘুঁটি।", "काला", "काला! काले मोहरे।"},
            {Color.WHITE, "☁️",
                    "White", "White! A white cloud.", "সাদা", "সাদা! সাদা মেঘ।", "सफ़ेद", "सफ़ेद! सफ़ेद बादल।"},
    };

    static List<Item> colors() {
        List<Item> items = new ArrayList<>();
        for (Object[] d : COLORS) {
            final int color = (Integer) d[0];
            final String emoji = (String) d[1];
            int bg = color == Color.WHITE ? Color.rgb(176, 214, 255)
                    : Art.isDark(color) ? Color.rgb(225, 225, 235) : Art.tint(color, 0.75f);
            items.add(new Item(pick(d, 2), pick(d, 3), color, bg, (c, cx, cy, s) -> {
                Art.shape(c, Art.CIRCLE, color, cx - s * 0.12f, cy, s * 0.8f);
                if (emoji == null) {
                    Pieces.draw(c, Pieces.KING, false, cx + s * 0.32f, cy + s * 0.2f, s * 0.42f);
                } else {
                    Art.emoji(c, emoji, cx + s * 0.32f, cy + s * 0.25f, s * 0.36f);
                }
            }));
        }
        return items;
    }

    private static final Object[][] SHAPES = {
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
                    "Rectangle", "Rectangle!", "আয়তক্ষেত্র", "আয়তক্ষেত্র!", "आयत", "आयत!"},
            {Art.OVAL, Color.rgb(142, 36, 170),
                    "Oval", "Oval! Like an egg.", "ডিম্বাকার", "ডিম্বাকার! ডিমের মতো।", "अंडाकार", "अंडाकार! अंडे जैसा।"},
            {Art.DIAMOND, Color.rgb(0, 172, 193),
                    "Diamond", "Diamond!", "হীরা", "হীরা!", "हीरा", "हीरा!"},
    };

    static List<Item> shapes() {
        List<Item> items = new ArrayList<>();
        for (int i = 0; i < SHAPES.length; i++) {
            Object[] d = SHAPES[i];
            final int shape = (Integer) d[0];
            final int color = (Integer) d[1];
            items.add(new Item(pick(d, 2), pick(d, 3), color, pastel(i),
                    (c, cx, cy, s) -> Art.shape(c, shape, color, cx, cy, s)));
        }
        return items;
    }

    // Each row: emoji, then label and speech for English, Bengali, Hindi.
    private static final String[][] ANIMALS = {
            {"🐶", "Dog", "Dog! Woof woof!", "কুকুর", "কুকুর! ঘেউ ঘেউ!", "कुत्ता", "कुत्ता! भौं भौं!"},
            {"🐱", "Cat", "Cat! Meow!", "বিড়াল", "বিড়াল! মিয়াও!", "बिल्ली", "बिल्ली! म्याऊँ!"},
            {"🐮", "Cow", "Cow! Moo!", "গরু", "গরু! হাম্বা!", "गाय", "गाय! मूँ!"},
            {"🦆", "Duck", "Duck! Quack quack!", "হাঁস", "হাঁস! প্যাঁক প্যাঁক!", "बत्तख", "बत्तख! क्वैक क्वैक!"},
            {"🐑", "Sheep", "Sheep! Baa baa!", "ভেড়া", "ভেড়া! ভ্যা ভ্যা!", "भेड़", "भेड़! मैं मैं!"},
            {"🐴", "Horse", "Horse! Neigh!", "ঘোড়া", "ঘোড়া! চিঁহিঁ!", "घोड़ा", "घोड़ा! हिनहिन!"},
            {"🦁", "Lion", "Lion! Roar!", "সিংহ", "সিংহ! হালুম!", "शेर", "शेर! दहाड़!"},
            {"🐯", "Tiger", "Tiger! Grrr!", "বাঘ", "বাঘ! হালুম হালুম!", "बाघ", "बाघ! गुर्र!"},
            {"🐘", "Elephant", "Elephant! Big ears and a long trunk.", "হাতি", "হাতি! লম্বা শুঁড়!", "हाथी", "हाथी! लंबी सूँड!"},
            {"🐵", "Monkey", "Monkey! Ooh ooh, ah ah!", "বানর", "বানর! উপ উপ!", "बंदर", "बंदर! ऊ ऊ आ आ!"},
            {"🐦", "Bird", "Bird! Tweet tweet!", "পাখি", "পাখি! কিচির মিচির!", "चिड़िया", "चिड़िया! चीं चीं!"},
            {"🐟", "Fish", "Fish! Swim swim.", "মাছ", "মাছ! সাঁতার কাটে।", "मछली", "मछली! तैरती है।"},
            {"🐸", "Frog", "Frog! Ribbit!", "ব্যাঙ", "ব্যাঙ! ঘ্যাঙর ঘ্যাং!", "मेंढक", "मेंढक! टर्र टर्र!"},
            {"🐔", "Chicken", "Chicken! Cluck cluck!", "মুরগি", "মুরগি! কক কক!", "मुर्गी", "मुर्गी! कुट कुट!"},
            {"🐝", "Bee", "Bee! Buzz buzz!", "মৌমাছি", "মৌমাছি! ভোঁ ভোঁ!", "मधुमक्खी", "मधुमक्खी! भिन भिन!"},
            {"🐰", "Bunny", "Bunny! Hop hop hop!", "খরগোশ", "খরগোশ! লাফ লাফ!", "खरगोश", "खरगोश! उछल उछल!"},
            {"🐻", "Bear", "Bear! Big hug!", "ভালুক", "ভালুক! বড় আদর!", "भालू", "भालू! बड़ी झप्पी!"},
            {"🐢", "Turtle", "Turtle! Slow and steady.", "কচ্ছপ", "কচ্ছপ! আস্তে আস্তে।", "कछुआ", "कछुआ! धीरे धीरे।"},
    };

    static List<Item> animals() {
        return emojiDeck(ANIMALS);
    }

    private static final String[][] THINGS = {
            {"🍎", "Apple", "Apple! Crunch crunch.", "আপেল", "আপেল! কচর মচর।", "सेब", "सेब! कुरकुरा।"},
            {"🍌", "Banana", "Banana! Yummy.", "কলা", "কলা! খুব মজা।", "केला", "केला! बहुत स्वादिष्ट।"},
            {"🥭", "Mango", "Mango! Sweet mango.", "আম", "আম! মিষ্টি আম।", "आम", "आम! मीठा आम।"},
            {"🍓", "Strawberry", "Strawberry!", "স্ট্রবেরি", "স্ট্রবেরি!", "स्ट्रॉबेरी", "स्ट्रॉबेरी!"},
            {"🥕", "Carrot", "Carrot!", "গাজর", "গাজর!", "गाजर", "गाजर!"},
            {"🍪", "Cookie", "Cookie! Nom nom nom.", "বিস্কুট", "বিস্কুট! মজা মজা।", "बिस्कुट", "बिस्कुट! मज़ेदार।"},
            {"🍼", "Bottle", "Bottle! Milk time.", "বোতল", "বোতল! দুধ খাওয়ার সময়।", "बोतल", "बोतल! दूध का समय।"},
            {"⚽", "Ball", "Ball! Bounce bounce.", "বল", "বল! লাফায়।", "गेंद", "गेंद! उछलती है।"},
            {"🧸", "Teddy", "Teddy bear! Cuddle.", "টেডি", "টেডি! জড়িয়ে ধরো।", "टेडी", "टेडी! गले लगाओ।"},
            {"🎈", "Balloon", "Balloon! Up, up, up!", "বেলুন", "বেলুন! উপরে উপরে!", "गुब्बारा", "गुब्बारा! ऊपर ऊपर!"},
            {"📚", "Book", "Book! Let's read.", "বই", "বই! চলো পড়ি।", "किताब", "किताब! चलो पढ़ें।"},
            {"👟", "Shoe", "Shoe! Put on your shoes.", "জুতো", "জুতো! জুতো পরো।", "जूता", "जूता! जूते पहनो।"},
            {"🎩", "Hat", "Hat!", "টুপি", "টুপি!", "टोपी", "टोपी!"},
            {"🥄", "Spoon", "Spoon!", "চামচ", "চামচ!", "चम्मच", "चम्मच!"},
            {"☕", "Cup", "Cup!", "কাপ", "কাপ!", "कप", "कप!"},
            {"🛏️", "Bed", "Bed! Night night.", "বিছানা", "বিছানা! ঘুমের সময়।", "बिस्तर", "बिस्तर! सोने का समय।"},
            {"🚗", "Car", "Car! Beep beep!", "গাড়ি", "গাড়ি! পিপ পিপ!", "गाड़ी", "गाड़ी! पीं पीं!"},
            {"🚌", "Bus", "Bus! The wheels go round and round.", "বাস", "বাস! চাকা ঘোরে।", "बस", "बस! पहिए घूमते हैं।"},
            {"🚂", "Train", "Train! Choo choo!", "ট্রেন", "ট্রেন! কু ঝিক ঝিক!", "रेलगाड़ी", "रेलगाड़ी! छुक छुक!"},
            {"✈️", "Airplane", "Airplane! Zoom!", "উড়োজাহাজ", "উড়োজাহাজ! শোঁ শোঁ!", "हवाई जहाज़", "हवाई जहाज़! ज़ूम!"},
            {"⛵", "Boat", "Boat! Splash.", "নৌকা", "নৌকা! ছলাৎ ছলাৎ।", "नाव", "नाव! छप छप।"},
            {"🏠", "House", "House! Home sweet home.", "বাড়ি", "বাড়ি! আমাদের বাড়ি।", "घर", "घर! प्यारा घर।"},
            {"🌳", "Tree", "Tree! Tall tree.", "গাছ", "গাছ! বড় গাছ।", "पेड़", "पेड़! ऊँचा पेड़।"},
            {"🌸", "Flower", "Flower! Smell the flower.", "ফুল", "ফুল! ফুলের গন্ধ নাও।", "फूल", "फूल! फूल सूँघो।"},
            {"☀️", "Sun", "Sun! Hello sunshine.", "সূর্য", "সূর্য! সুপ্রভাত।", "सूरज", "सूरज! नमस्ते सूरज।"},
            {"🌙", "Moon", "Moon! Good night moon.", "চাঁদ", "চাঁদ! চাঁদ মামা।", "चाँद", "चाँद! चंदा मामा।"},
            {"⭐", "Star", "Star! Twinkle twinkle.", "তারা", "তারা! মিটমিট।", "तारा", "तारा! टिमटिम।"},
    };

    static List<Item> things() {
        return emojiDeck(THINGS);
    }

    private static final String[] COUNT_EMOJI = {"☀️", "⚽", "🦆", "🍎", "⭐"};
    private static final String[][] COUNT_NOUNS = {
            {"One sun!", "একটা সূর্য!", "एक सूरज!"},
            {"Two balls!", "দুটো বল!", "दो गेंदें!"},
            {"Three ducks!", "তিনটে হাঁস!", "तीन बत्तखें!"},
            {"Four apples!", "চারটে আপেল!", "चार सेब!"},
            {"Five stars!", "পাঁচটা তারা!", "पाँच तारे!"},
    };

    static List<Item> numbers() {
        List<Item> items = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            final int n = i + 1;
            final String e = COUNT_EMOJI[i];
            StringBuilder speech = new StringBuilder();
            for (int k = 1; k <= n; k++) speech.append(Lang.number(k)).append("... ");
            speech.append(Lang.t(COUNT_NOUNS[i]));
            items.add(new Item(Lang.digit(n), speech.toString(), Color.rgb(120, 90, 200), pastel(i),
                    (c, cx, cy, s) -> drawCount(c, e, n, cx, cy, s)));
        }
        return items;
    }

    private static void drawCount(Canvas c, String e, int n, float cx, float cy, float s) {
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
            items.add(new Item(name, name + "! " + Lang.t(PIECE_SPEECH[i]), Color.rgb(93, 64, 55), pastel(i),
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

    private static String pick(Object[] row, int first) {
        return (String) row[first + 2 * Lang.current];
    }

    private static List<Item> emojiDeck(String[][] data) {
        List<Item> items = new ArrayList<>();
        for (String[] d : data) {
            final String emoji = d[0];
            if (!Art.hasEmoji(emoji)) continue;
            items.add(new Item(pick(d, 1), pick(d, 2), Color.rgb(60, 36, 90), pastel(items.size()),
                    (c, cx, cy, s) -> Art.emoji(c, emoji, cx, cy, s * 0.9f)));
        }
        return items;
    }
}
