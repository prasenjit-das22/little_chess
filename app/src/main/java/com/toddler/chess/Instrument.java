package com.toddler.chess;

import android.graphics.Color;

/** The musical instruments: names, what they teach, how they are laid out and how they sound. */
final class Instrument {
    static final int PIANO = 0, TABLA = 1, GUITAR = 2, FLUTE = 3, HARMONIUM = 4, SITAR = 5, EKTARA = 6,
            DHAK = 7, SHANKH = 8, VIOLIN = 9, XYLOPHONE = 10, BELLS = 11, DRUMS = 12, TRUMPET = 13;
    static final int COUNT = 14;

    /** Screen layouts. */
    static final int KEYS = 0, BARS = 1, HOLES = 2, NECK = 3, STRINGS = 4, PADS = 5;

    static final String[][] NAMES = {
            {"Piano", "পিয়ানো", "पियानो"},
            {"Tabla", "তবলা", "तबला"},
            {"Guitar", "গিটার", "गिटार"},
            {"Flute", "বাঁশি", "बाँसुरी"},
            {"Harmonium", "হারমোনিয়াম", "हारमोनियम"},
            {"Sitar", "সেতার", "सितार"},
            {"Ektara", "একতারা", "इकतारा"},
            {"Dhak", "ঢাক", "ढाक"},
            {"Conch", "শাঁখ", "शंख"},
            {"Violin", "বেহালা", "वायलिन"},
            {"Xylophone", "জাইলোফোন", "ज़ाइलोफ़ोन"},
            {"Bells", "ঘণ্টা", "घंटियाँ"},
            {"Drums", "ড্রাম", "ड्रम"},
            {"Trumpet", "ট্রাম্পেট", "तुरही"},
    };

    static final String[][] ABOUT = {
            {"Piano! Black and white keys. Press a key: Do, Re, Mi!",
                    "পিয়ানো! সাদা কালো চাবি। চাবি টিপে বাজাও: সা, রে, গা, মা!",
                    "पियानो! काली सफ़ेद चाबियाँ। दबाओ: सा, रे, ग, म!"},
            {"Tabla! Two drums. The small one says Na, the big one says Ge. Hit both together: Dha!",
                    "তবলা! ডান হাতে তবলা, বাঁ হাতে বাঁয়া। ছোটটা বলে না, বড়টা বলে গে। দুটো একসঙ্গে বাজাও: ধা!",
                    "तबला! दो ड्रम। छोटा बोले ना, बड़ा बोले गे। दोनों एक साथ बजाओ: धा!"},
            {"Guitar! Six strings. Swipe your finger across them all!",
                    "গিটার! ছ'টা তার। আঙুল টেনে সব তার একসঙ্গে বাজাও!",
                    "गिटार! छह तार। उँगली फिराकर सारे तार बजाओ!"},
            {"Flute! A bamboo flute. Cover the holes to make the tune.",
                    "বাঁশি! বাঁশের তৈরি। ফুঁ দিলে মিষ্টি সুর বাজে।",
                    "बाँसुरी! बाँस से बनी। फूँको तो मीठी धुन बजती है।"},
            {"Harmonium! Pump the air and press the keys. We sing songs with it.",
                    "হারমোনিয়াম! হাওয়া দিয়ে চাবি টেপো। এর সঙ্গে গান গাই।",
                    "हारमोनियम! हवा भरो और चाबी दबाओ। इसके साथ गाना गाते हैं।"},
            {"Sitar! A long neck and many strings. Pandit Ravi Shankar played it all over the world.",
                    "সেতার! লম্বা গলা, অনেক তার। পণ্ডিত রবিশঙ্কর সারা পৃথিবীতে সেতার বাজিয়েছেন।",
                    "सितार! लंबी गर्दन, कई तार। पंडित रवि शंकर ने इसे दुनिया भर में बजाया।"},
            {"Ektara! Just one string. The Baul singers of Bengal play it and sing.",
                    "একতারা! মাত্র একটা তার। বাংলার বাউলরা একতারা বাজিয়ে গান গায়।",
                    "इकतारा! बस एक तार। बंगाल के बाउल गायक इसे बजाकर गाते हैं।"},
            {"Dhak! The big drum of Durga Puja. Dhyam kur kur!",
                    "ঢাক! দুর্গাপুজোর ঢাক। ঢ্যাম কুড়াকুড়!",
                    "ढाक! दुर्गा पूजा का बड़ा ढोल। ढम कुड़ कुड़!"},
            {"Conch! The shankh is blown three times at puja. Tap and listen!",
                    "শাঁখ! পুজোয় তিনবার শাঁখ বাজায়। টিপে শোনো!",
                    "शंख! पूजा में शंख बजाते हैं। दबाओ और सुनो!"},
            {"Violin! Played with a bow. Smooth and sweet.",
                    "বেহালা! ছড় টেনে বাজায়। মিষ্টি সুর।",
                    "वायलिन! गज से बजता है। मीठी धुन।"},
            {"Xylophone! Tap the colourful bars. Ting ting!",
                    "জাইলোফোন! রঙিন কাঠিতে টুং টাং!",
                    "ज़ाइलोफ़ोन! रंगीन पट्टियों पर टन टन!"},
            {"Bells! Ding dong! Big bells ring low, small bells ring high.",
                    "ঘণ্টা! ঢং ঢং! বড় ঘণ্টা মোটা সুরে, ছোট ঘণ্টা মিহি সুরে বাজে।",
                    "घंटियाँ! टन टन! बड़ी घंटी मोटी आवाज़, छोटी घंटी पतली आवाज़।"},
            {"Drums! Boom, tak, tsss! Let's make a beat.",
                    "ড্রাম! ধুম, টাক, ছিস! চলো তাল বাজাই।",
                    "ड्रम! धम, टक, छिस! चलो ताल बजाएँ।"},
            {"Trumpet! Toot toot! A shiny brass horn.",
                    "ট্রাম্পেট! পোঁ পোঁ! ঝকঝকে পিতলের বাজনা।",
                    "तुरही! तू तू! चमकदार पीतल का बाजा।"},
    };

    static final int[] COLORS = {
            Color.rgb(121, 134, 203), Color.rgb(188, 140, 100), Color.rgb(255, 167, 38),
            Color.rgb(174, 213, 129), Color.rgb(161, 136, 127), Color.rgb(255, 138, 101),
            Color.rgb(255, 202, 40), Color.rgb(239, 83, 80), Color.rgb(244, 143, 177),
            Color.rgb(186, 104, 200), Color.rgb(77, 182, 172), Color.rgb(255, 213, 79),
            Color.rgb(100, 181, 246), Color.rgb(255, 193, 7),
    };

    /** Rainbow colours for the eight notes, the same on every instrument. */
    static final int[] NOTE_COLORS = {
            Color.rgb(229, 57, 53), Color.rgb(251, 140, 0), Color.rgb(253, 216, 53), Color.rgb(67, 160, 71),
            Color.rgb(0, 172, 193), Color.rgb(30, 136, 229), Color.rgb(94, 53, 177), Color.rgb(216, 27, 96),
    };

    static final int[] SCALE = {0, 2, 4, 5, 7, 9, 11, 12};
    static final String[][] SARGAM = {
            {"Do", "Re", "Mi", "Fa", "So", "La", "Ti", "Do"},
            {"সা", "রে", "গা", "মা", "পা", "ধা", "নি", "সা"},
            {"सा", "रे", "ग", "म", "प", "ध", "नि", "सां"},
    };

    private Instrument() {}

    static int layout(int k) {
        switch (k) {
            case PIANO: case HARMONIUM: return KEYS;
            case XYLOPHONE: case BELLS: return BARS;
            case FLUTE: case TRUMPET: return HOLES;
            case SITAR: case EKTARA: case VIOLIN: return NECK;
            case GUITAR: return STRINGS;
            default: return PADS;
        }
    }

    static boolean melodic(int k) {
        int l = layout(k);
        return l != PADS && l != STRINGS;
    }

    /** Number of separate sounds (samples) the instrument needs. */
    static int sounds(int k) {
        switch (layout(k)) {
            case KEYS: return 13;
            case STRINGS: return 6;
            case PADS: return pads(k).length == 0 ? 0 : padSounds(k);
            default: return 8;
        }
    }

    private static int padSounds(int k) {
        switch (k) {
            case TABLA: return 4;
            case DHAK: return 3;
            case SHANKH: return 1;
            default: return 6;
        }
    }

    static final double[] GUITAR_STRINGS = {82.41, 110.0, 146.83, 196.0, 246.94, 329.63};
    static final String[] GUITAR_NAMES = {"E", "A", "D", "G", "B", "E"};

    /** Renders sound number i. Melodic sounds are semitones above the instrument's low note. */
    static float[] render(int k, int i) {
        double c4 = 261.63;
        int semis = layout(k) == KEYS ? i : SCALE[Math.min(i, 7)];
        switch (k) {
            case PIANO: return Synth.piano(Synth.hz(c4, semis));
            case HARMONIUM: return Synth.harmonium(Synth.hz(c4, semis));
            case FLUTE: return Synth.flute(Synth.hz(c4 * 2, semis));
            case TRUMPET: return Synth.trumpet(Synth.hz(c4, semis));
            case XYLOPHONE: return Synth.xylophone(Synth.hz(c4 * 2, semis));
            case BELLS: return Synth.bell(Synth.hz(c4 * 2, semis));
            case SITAR: return Synth.sitar(Synth.hz(c4 / 2, semis));
            case EKTARA: return Synth.ektara(Synth.hz(c4, semis));
            case VIOLIN: return Synth.violin(Synth.hz(c4, semis));
            case GUITAR: return Synth.guitar(GUITAR_STRINGS[i]);
            case TABLA: {
                switch (i) {
                    case 0: return Synth.tablaNa();
                    case 1: return Synth.tablaTin();
                    case 2: return Synth.tablaGe();
                    default: return Synth.tablaKe();
                }
            }
            case DHAK: return i == 0 ? Synth.dhakBass() : i == 1 ? Synth.dhakKur() : Synth.kansor();
            case SHANKH: return Synth.shankh();
            default: {
                switch (i) {
                    case 0: return Synth.kick();
                    case 1: return Synth.snare();
                    case 2: return Synth.hihat();
                    case 3: return Synth.tom();
                    case 4: return Synth.crash();
                    default: return Synth.clap();
                }
            }
        }
    }

    /** A drum or other tap target, positioned as fractions of the play area (radii of its height). */
    static final class Pad {
        final float x, y, rx, ry;
        final int sound;
        final String[] label;
        final float inner;
        final int innerSound;
        final String[] innerLabel;
        final int style;

        Pad(float x, float y, float rx, float ry, int sound, String[] label, float inner, int innerSound,
            String[] innerLabel, int style) {
            this.x = x;
            this.y = y;
            this.rx = rx;
            this.ry = ry;
            this.sound = sound;
            this.label = label;
            this.inner = inner;
            this.innerSound = innerSound;
            this.innerLabel = innerLabel;
            this.style = style;
        }
    }

    /** Pad styles. */
    static final int BAYAN = 0, DAYAN = 1, DRUM_HEAD = 2, CYMBAL = 3, GONG = 4, CLAP = 5, SHELL = 6, KICK = 7;

    private static final String[] GE = {"Ge", "গে", "गे"}, KE = {"Ke", "কে", "के"},
            NA = {"Na", "না", "ना"}, TIN = {"Tin", "তিন", "तिन"};

    static Pad[] pads(int k) {
        switch (k) {
            case TABLA:
                return new Pad[]{
                        new Pad(0.3f, 0.52f, 0.42f, 0.42f, 2, GE, 0.4f, 3, KE, BAYAN),
                        new Pad(0.73f, 0.52f, 0.34f, 0.34f, 0, NA, 0.45f, 1, TIN, DAYAN),
                };
            case DHAK:
                return new Pad[]{
                        new Pad(0.2f, 0.58f, 0.2f, 0.36f, 0, new String[]{"Dhyam", "ঢ্যাম", "ढम"}, 0, 0, null, DRUM_HEAD),
                        new Pad(0.62f, 0.58f, 0.2f, 0.36f, 1, new String[]{"Kur", "কুড়", "कुड़"}, 0, 0, null, DRUM_HEAD),
                        new Pad(0.87f, 0.35f, 0.24f, 0.24f, 2, new String[]{"Kansor", "কাঁসর", "कांसा"}, 0, 0, null, GONG),
                };
            case SHANKH:
                return new Pad[]{
                        new Pad(0.5f, 0.5f, 0.46f, 0.46f, 0, new String[]{"Blow!", "বাজাও!", "बजाओ!"}, 0, 0, null, SHELL),
                };
            case DRUMS:
                return new Pad[]{
                        new Pad(0.12f, 0.32f, 0.22f, 0.22f, 2, new String[]{"Hi-hat", "হাই-হ্যাট", "हाई-हैट"}, 0, 0, null, CYMBAL),
                        new Pad(0.3f, 0.66f, 0.26f, 0.26f, 1, new String[]{"Snare", "স্নেয়ার", "स्नेयर"}, 0, 0, null, DRUM_HEAD),
                        new Pad(0.55f, 0.6f, 0.36f, 0.36f, 0, new String[]{"Boom", "ধুম", "धम"}, 0, 0, null, KICK),
                        new Pad(0.47f, 0.2f, 0.18f, 0.18f, 3, new String[]{"Tom", "টম", "टॉम"}, 0, 0, null, DRUM_HEAD),
                        new Pad(0.86f, 0.3f, 0.25f, 0.25f, 4, new String[]{"Crash", "ঝনঝন", "झन्न"}, 0, 0, null, CYMBAL),
                        new Pad(0.84f, 0.78f, 0.18f, 0.18f, 5, new String[]{"Clap", "তালি", "ताली"}, 0, 0, null, CLAP),
                };
            default:
                return new Pad[0];
        }
    }

    // ---- songs to learn on the melodic instruments ----

    static final class Song {
        final String[] name;
        final int[] notes;
        final int[] beats;

        Song(String[] name, String tune) {
            this.name = name;
            String[] t = tune.trim().split("\\s+");
            notes = new int[t.length];
            beats = new int[t.length];
            for (int i = 0; i < t.length; i++) {
                boolean longNote = t[i].endsWith(":");
                notes[i] = Integer.parseInt(longNote ? t[i].substring(0, t[i].length() - 1) : t[i]);
                beats[i] = longNote ? 2 : 1;
            }
        }
    }

    /** Tunes as notes of the scale: 0 = Sa/Do ... 7 = high Sa/Do; ':' holds a note longer. */
    static final Song[] SONGS = {
            new Song(new String[]{"Do Re Mi", "সা রে গা মা", "सा रे ग म"},
                    "0 1 2 3 4 5 6 7: 7 6 5 4 3 2 1 0:"),
            new Song(new String[]{"Twinkle Twinkle", "টুইঙ্কল টুইঙ্কল", "ट्विंकल ट्विंकल"},
                    "0 0 4 4 5 5 4: 3 3 2 2 1 1 0: 4 4 3 3 2 2 1: 4 4 3 3 2 2 1: 0 0 4 4 5 5 4: 3 3 2 2 1 1 0:"),
            new Song(new String[]{"Mary's Little Lamb", "মেরির ছোট্ট ভেড়া", "मैरी का मेमना"},
                    "2 1 0 1 2 2 2: 1 1 1: 2 4 4: 2 1 0 1 2 2 2 2 1 1 2 1 0:"),
            new Song(new String[]{"Jingle Bells", "জিঙ্গল বেলস", "जिंगल बेल्स"},
                    "2 2 2: 2 2 2: 2 4 0 1 2: 3 3 3 3 3 2 2 2 2 1 1 2 1: 4:"),
            new Song(new String[]{"Ode to Joy", "আনন্দের গান", "खुशी का गीत"},
                    "2 2 3 4 4 3 2 1 0 0 1 2 2 1 1: 2 2 3 4 4 3 2 1 0 0 1 2 1 0 0:"),
    };

    // ---- rhythms for the drums and the guitar ----

    static final class Rhythm {
        final String[] name;
        final int[][] steps;
        final String[][] bols;
        final int stepMs;

        Rhythm(String[] name, int stepMs, int[][] steps, String[][] bols) {
            this.name = name;
            this.stepMs = stepMs;
            this.steps = steps;
            this.bols = bols;
        }
    }

    private static final String[] DHA = {"Dha", "ধা", "धा"}, DHIN = {"Dhin", "ধিন", "धिन"},
            TA = {"Ta", "তা", "ता"}, TI = {"Ti", "তি", "ति"};

    static Rhythm[] rhythms(int k) {
        switch (k) {
            case TABLA: {
                int[] dha = {0, 2}, dhin = {1, 2}, na = {0}, tin = {1}, ge = {2}, ke = {3};
                return new Rhythm[]{
                        new Rhythm(new String[]{"Teentaal", "তিনতাল", "तीनताल"}, 330,
                                new int[][]{dha, dhin, dhin, dha, dha, dhin, dhin, dha, dha, tin, tin, na, na, dhin, dhin, dha},
                                new String[][]{DHA, DHIN, DHIN, DHA, DHA, DHIN, DHIN, DHA, DHA, TIN, TIN, TA, TA, DHIN, DHIN, DHA}),
                        new Rhythm(new String[]{"Kaharwa", "কাহারবা", "कहरवा"}, 300,
                                new int[][]{dha, ge, na, tin, na, ke, dhin, na},
                                new String[][]{DHA, GE, NA, TI, NA, KE, DHIN, NA}),
                        new Rhythm(new String[]{"Dadra", "দাদরা", "दादरा"}, 330,
                                new int[][]{dha, dhin, na, dha, tin, na},
                                new String[][]{DHA, DHIN, NA, DHA, TIN, NA}),
                };
            }
            case DHAK: {
                String[] dh = {"Dhyam", "ঢ্যাম", "ढम"}, kr = {"Kur", "কুড়", "कुड़"};
                return new Rhythm[]{new Rhythm(new String[]{"Puja beat", "পুজোর বোল", "पूजा की ताल"}, 260,
                        new int[][]{{0, 2}, {1}, {1}, {0}, {1}, {0}, {0, 2}, {1}, {0}, {1}, {1}, {0}, {1}, {0, 2}, {0}, {}},
                        new String[][]{dh, kr, kr, dh, kr, dh, dh, kr, dh, kr, kr, dh, kr, dh, dh, null})};
            }
            case SHANKH:
                return new Rhythm[]{new Rhythm(new String[]{"Three times", "তিনবার", "तीन बार"}, 700,
                        new int[][]{{0}, {}, {}, {0}, {}, {}, {0}, {}, {}},
                        new String[][]{{"One", "এক", "एक"}, null, null, {"Two", "দুই", "दो"}, null, null,
                                {"Three", "তিন", "तीन"}, null, null})};
            case DRUMS: {
                String[] boom = {"Boom", "ধুম", "धम"}, tak = {"Tak", "টাক", "टक"}, ts = {"ts", "ছিস", "छिस"};
                return new Rhythm[]{
                        new Rhythm(new String[]{"Rock beat", "রক বিট", "रॉक बीट"}, 260,
                                new int[][]{{0, 2}, {2}, {1, 2}, {2}, {0, 2}, {0, 2}, {1, 2}, {2}, {0, 2}, {2}, {1, 2}, {2}, {0, 2}, {0, 2}, {1, 4}, {}},
                                new String[][]{boom, ts, tak, ts, boom, boom, tak, ts, boom, ts, tak, ts, boom, boom, tak, null}),
                        new Rhythm(new String[]{"Clap along", "তালি দাও", "ताली बजाओ"}, 300,
                                new int[][]{{0}, {5}, {0}, {5}, {0, 3}, {5}, {0}, {5, 4}},
                                new String[][]{boom, {"Clap", "তালি", "ताली"}, boom, {"Clap", "তালি", "ताली"},
                                        boom, {"Clap", "তালি", "ताली"}, boom, {"Clap", "তালি", "ताली"}}),
                };
            }
            case GUITAR: {
                int[] down = {0, 1, 2, 3, 4, 5}, up = {5, 4, 3, 2, 1, 0};
                return new Rhythm[]{new Rhythm(new String[]{"Strum", "স্ট্রাম", "स्ट्रम"}, 330,
                        new int[][]{down, {}, down, up, {}, up, down, up},
                        new String[][]{{"Down", "নিচে", "नीचे"}, null, {"Down", "নিচে", "नीचे"}, {"Up", "উপরে", "ऊपर"}, null,
                                {"Up", "উপরে", "ऊपर"}, {"Down", "নিচে", "नीचे"}, {"Up", "উপরে", "ऊपर"}})};
            }
            default:
                return new Rhythm[0];
        }
    }
}
